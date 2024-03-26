/*
 * Copyright © 2003 - 2024 The eFaps Team (-)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.efaps.db.stmt.selection;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.efaps.admin.access.AccessTypeEnums;
import org.efaps.admin.common.MsgPhrase;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.beans.ValueList;
import org.efaps.beans.ValueList.Token;
import org.efaps.beans.valueparser.ParseException;
import org.efaps.beans.valueparser.ValueParser;
import org.efaps.ci.CIAttribute;
import org.efaps.db.Instance;
import org.efaps.db.stmt.IFlagged;
import org.efaps.db.stmt.selection.EvalHelper.PhraseEntry;
import org.efaps.db.stmt.selection.elements.AbstractElement;
import org.efaps.db.stmt.selection.elements.AttributeElement;
import org.efaps.db.stmt.selection.elements.AttributeSetElement;
import org.efaps.db.stmt.selection.elements.FirstElement;
import org.efaps.db.stmt.selection.elements.IAuxillary;
import org.efaps.db.stmt.selection.elements.JoiningElement;
import org.efaps.db.stmt.selection.elements.LastElement;
import org.efaps.eql.JSONData;
import org.efaps.eql.builder.Print;
import org.efaps.eql2.StmtFlag;
import org.efaps.json.data.DataList;
import org.efaps.json.data.ObjectData;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class SelectionEvaluator.
 */
public final class Evaluator
{
    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(Evaluator.class);

    /** The init. */
    private boolean init;

    /** The selection. */
    private final Selection selection;

    /** The Access. */
    private Access access;

    private final EvalHelper helper;

    private EnumSet<StmtFlag> flags;

    /**
     * Instantiates a new selection evaluator.
     *
     * @param _selection the selection
     * @throws EFapsException
     */
    private Evaluator(final ISelectionProvider _selectionProvider,
                      final EvalHelper _helper)
        throws EFapsException
    {
        selection = _selectionProvider.getSelection();
        if (_selectionProvider instanceof IFlagged) {
            flags = ((IFlagged) _selectionProvider).getFlags();
        }
        helper = _helper;
    }

    /**
     * Initialize.
     *
     * @param _step the step
     * @throws EFapsException on Error
     */
    private void initialize(final boolean _step)
        throws EFapsException
    {
        if (!init) {
            squash();
            evalAccess();
            init = true;
            if (_step) {
                step(selection.getAllSelects());
            }
        }
    }

    /**
     * Squash.
     *
     * @throws EFapsException the eFaps exception
     */
    private void squash()
        throws EFapsException
    {
        if (selection.getSelects().stream().anyMatch(Select::isSquash)) {
            final Select select = selection.getInstSelects().get(Selection.BASEPATH);
            final Squashing squash = new Squashing(select);

            for (final Entry<String, Select> entry : selection.getInstSelects().entrySet()) {
                squash.execute(entry.getKey(), entry.getValue());
            }

            for (final Select currentSelect : selection.getSelects()) {
                squash.execute(null, currentSelect);
            }
        }
    }

    /**
     * Count.
     *
     * @return the int
     * @throws EFapsException the eFaps exception
     */
    public long count()
        throws EFapsException
    {
        initialize(false);
        final Select select = selection.getInstSelects().get(Selection.BASEPATH);
        return select.getObjects(this).size();
    }

    /**
     * Gets the.
     *
     * @param <T> the generic type
     * @param _idx the idx
     * @return the t
     * @throws EFapsException on error
     */
    @SuppressWarnings("unchecked")
    public <T> T get(final int _idx)
        throws EFapsException
    {
        initialize(true);
        Object ret = null;
        final int idx = _idx - 1;
        if (selection.getSelects().size() > idx) {
            final Select select = selection.getSelects().get(idx);
            ret = get(select);
        }
        return (T) ret;
    }

    /**
     * Gets the.
     *
     * @param <T> the generic type
     * @param _alias the alias
     * @return the t
     * @throws EFapsException  on error
     */
    @SuppressWarnings("unchecked")
    public <T> T get(final String _alias)
        throws EFapsException
    {
        initialize(true);
        Object ret = null;
        final Optional<Select> selectOpt = selection.getSelects().stream()
                        .filter(select -> _alias.equals(select.getAlias()))
                        .findFirst();
        if (selectOpt.isPresent()) {
            ret = get(selectOpt.get());
        } else if (helper != null) {
            final Optional<PhraseEntry> phraseOpt = helper.getPhrases().stream()
                            .filter(entry -> _alias.equals(entry.getAlias())).findFirst();
            if (phraseOpt.isPresent()) {
                final PhraseEntry phraseEntry = phraseOpt.get();
                try {
                    final ValueList list = new ValueParser(new StringReader(phraseEntry.getPhrase()))
                                    .ExpressionString();
                    final StringBuilder bldr = new StringBuilder();
                    int idx = -1;
                    for (final Token token : list.getTokens()) {
                        switch (token.getType()) {
                            case EXPRESSION:
                                idx++;
                                final Object val = get(Print.getPhraseAlias(phraseEntry.getPhraseIdx()) + "_" + idx);
                                if (val != null) {
                                    bldr.append(String.valueOf(val));
                                }
                                break;
                            case TEXT:
                                bldr.append(token.getValue());
                                break;
                            default:
                                break;
                        }
                    }
                    ret = bldr.toString();
                } catch (final ParseException e) {
                    LOG.error("Catched", e);
                }
            } else if (helper.getMsgPhrases().containsKey(_alias)) {
                final var msgPhrase = helper.getMsgPhrases().get(_alias);
                final List<Object> values = new ArrayList<>();
                for (int i = 0; i <  msgPhrase.getArguments().size(); i++) {
                    final var alias = Print.getMsgPhraseAlias(msgPhrase.getId()) + "_" + i;
                    var value = get(alias);
                    // for a MsgPharse having a value list is not very logical, s
                    // o as long there is one value convert it
                    if (value instanceof List && ((List<?>) value).size() == 1) {
                        value =  ((List<?>) value).get(0);
                    }
                    values.add(value == null ? "" : value);
                }
                ret = msgPhrase.format(values.toArray());
            }
        }
        return (T) ret;
    }

    /**
     * Gets the.
     *
     * @param <T> the generic type
     * @param _ciAttr the ci attr
     * @return the t
     * @throws EFapsException the e faps exception
     */
    public <T> T get(final CIAttribute _ciAttr)
        throws EFapsException
    {
        return get(Print.getCIAlias(_ciAttr));
    }

    /**
     * Attribute.
     *
     * @param _idx the idx
     * @return the attribute
     * @throws EFapsException the e faps exception
     */
    public Attribute attribute(final int _idx)
        throws EFapsException
    {
        initialize(true);
        Attribute ret = null;
        final int idx = _idx - 1;
        if (selection.getSelects().size() > idx) {
            final Select select = selection.getSelects().get(idx);
            ret = attribute(select);
        }
        return ret;
    }

    /**
     * Attribute.
     *
     * @param _alias the alias
     * @return the attribute
     * @throws EFapsException the e faps exception
     */
    public Attribute attribute(final String _alias)
        throws EFapsException
    {
        initialize(true);
        Attribute ret = null;
        final Optional<Select> selectOpt = selection.getSelects()
                        .stream()
                        .filter(select -> _alias.equals(select.getAlias()))
                        .findFirst();
        if (selectOpt.isPresent()) {
            ret = attribute(selectOpt.get());
        }
        return ret;
    }

    /**
     * Attribute.
     *
     * @param _select the select
     * @return the attribute
     * @throws EFapsException the e faps exception
     */
    protected Attribute attribute(final Select _select)
        throws EFapsException
    {
        Attribute ret = null;
        final AbstractElement<?> element = _select.getElements().get(_select.getElements().size() - 1);
        if (element instanceof AttributeElement) {
            ret = ((AttributeElement) element).getAttribute();
        }
        return ret;
    }

    /**
     * Gets the.
     *
     * @param <T> the generic type
     * @param _select the select
     * @return the t
     * @throws EFapsException the eFaps exception
     */
    @SuppressWarnings("unchecked")
    protected <T> T get(final Select _select)
        throws EFapsException
    {
        Object ret = null;
        final List<Boolean> accessList = access(_select);
        final Object obj = _select.getCurrent();
        if (_select.isMultiValue()) {
            if (accessList.size() == 1 && accessList.get(0)) {
                ret = obj;
            }
        } else if (obj instanceof List) {
            final Iterator<Boolean> iter = accessList.iterator();
            ret = ((List<?>) obj).stream()
                .map(ele -> iter.hasNext() && iter.next() ? ele : null)
                .collect(Collectors.toList());
            ret = agregate(_select, (List<Object>) ret);
        } else if (accessList.size() == 1 && accessList.get(0)) {
            ret = obj;
        }
        return (T) ret;
    }

    protected Object agregate(final Select _select,
                              final List<Object> _objects)
    {
        Object ret;
        final AbstractElement<?> lastElement = _select.getElements().get(_select.getElements().size() - 1);
        if (lastElement instanceof FirstElement) {
            ret = _objects.get(0);
        } else if (lastElement instanceof LastElement) {
            ret = _objects.get(_objects.size() - 1);
        } else if (lastElement instanceof JoiningElement) {
            ret = StringUtils.join(_objects, ((JoiningElement) lastElement).getSeparator());
        } else {
            ret = _objects;
        }
        return ret;
    }

    /**
     * Inst.
     *
     * @return the instance
     */
    public Instance inst()
    {
        return (Instance) selection.getInstSelects().get(Selection.BASEPATH).getCurrent();
    }

    public Instance inst(final String path)
    {
        return (Instance) selection.getInstSelects().get(path).getCurrent();
    }

    /**
     * Move the evaluator to the next value.
     * Skips values the User does not have access to.
     * @return true, if successful
     * @throws EFapsException on Error
     */
    public boolean next()
        throws EFapsException
    {
        initialize(false);
        boolean skip = true; // must step into the first one
        boolean ret = true;
        while (skip && ret) {
            ret = step(selection.getAllSelects());
            skip = !access.hasAccess(inst());
        }
        return ret;
    }

    /**
     * Checks for access.
     *
     * @param _select the select
     * @return true, if successful
     * @throws EFapsException on Error
     */
    protected List<Boolean> access(final Select _select)
        throws EFapsException
    {
        List<Boolean> ret = new ArrayList<>();
        // remove the IAuxillary to determine the size
        int size = _select.getElements().size();
        while (_select.getElements().get(size - 1) instanceof IAuxillary) {
            size--;
        }
        if (size == 1) {
            ret.add(access.hasAccess(inst()));
        } else {
            int idx = 0;
            boolean accessTemp = true;
            while (idx < size && accessTemp) {
                final AbstractElement<?> element = _select.getElements().get(idx);
                idx++;
                final Select instSelect = selection.getInstSelects().get(element.getPath());
                if (element instanceof AttributeSetElement) {
                    idx = size;
                    if (instSelect.getCurrent() != null && instSelect.getCurrent() instanceof Collection) {
                        ret = Collections.nCopies(((Collection<?>) instSelect.getCurrent()).size(), true);
                    }
                } else {
                    if (instSelect == null) {
                        LOG.error("Could not retrieve Instance Select for Path {}", element.getPath());
                    }
                    final Object obj = instSelect.getCurrent();
                    if (obj instanceof Instance) {
                        accessTemp = access.hasAccess((Instance) obj);
                        ret.clear();
                        ret.add(accessTemp);
                    } else if (obj instanceof List) {
                        if (ret.isEmpty()) {
                            ret = ((List<?>) obj).stream()
                                            .map(ele -> access.hasAccess((Instance) ele))
                                            .collect(Collectors.toList());
                        } else
                        // if the access is not empty... we are in a a child linkfrom. only if the previous
                        // one did give access , access is still evaluated
                        if (ret.size() == 1) {
                            final var acc = ret.get(0);
                            ret = ((List<?>) obj).stream()
                                            .map(ele -> acc && access.hasAccess((Instance) ele))
                                            .collect(Collectors.toList());
                        } else {
                            final Iterator<Boolean> iter = ret.iterator();
                            ret = ((List<?>) obj).stream()
                                        .map(ele -> iter.next() && access.hasAccess((Instance) ele))
                                        .collect(Collectors.toList());
                        }
                        accessTemp = ret.contains(true);
                    }
                }
            }
        }
        return ret;
    }

    /**
     * Gets the Access.
     *
     * @return the Access
     */
    protected Access getAccess()
    {
        return access;
    }

    /**
     * Gets the selection.
     *
     * @return the selection
     */
    protected Selection getSelection()
    {
        return selection;
    }

    /**
     * Move the selects to the next value.
     *
     * @param _selects the selects
     * @return true, if successful
     */
    private boolean step(final Collection<Select> _selects)
    {
        boolean ret = !CollectionUtils.isEmpty(_selects);
        for (final Select select : _selects) {
            ret = ret && select.next();
        }
        return ret;
    }

    /**
     * Evaluate the access for the instances.
     *
     * @throws EFapsException on error
     */
    private void evalAccess()
        throws EFapsException
    {
        if (flags != null && flags.contains(StmtFlag.TRIGGEROFF)) {
            access = Access.getNoOp();
        } else {
            final List<Instance> instances = new ArrayList<>();
            while (step(selection.getInstSelects().values())) {
                for (final Entry<String, Select> entry : selection.getInstSelects().entrySet()) {
                    final Object object = entry.getValue().getCurrent();
                    if (object != null) {
                        if (object instanceof List) {
                            ((List<?>) object).stream()
                                .filter(Objects::nonNull)
                                .forEach(e -> instances.add((Instance) e));
                        } else {
                            instances.add((Instance) object);
                        }
                    }
                }
            }
            for (final Entry<String, Select> entry : selection.getInstSelects().entrySet()) {
                entry.getValue().reset();
            }
            access = Access.get(AccessTypeEnums.READ.getAccessType(), instances);
        }
    }

    /**
     * Gets the data list.
     *
     * @return the data list
     * @throws EFapsException
     */
    public DataList getDataList()
        throws EFapsException
    {
        final DataList ret = new DataList();
        while (next()) {
            final ObjectData data = new ObjectData();
            int idx = 1;
            for (final Select select : selection.getSelects()) {
                final String key = select.getAlias() == null ? String.valueOf(idx) : select.getAlias();
                data.getValues().add(JSONData.getValue(key, get(select)));
                idx++;
            }
            ret.add(data);
        }
        return ret;
    }

    /**
     * Gets the data.
     *
     * @return the data
     * @throws EFapsException the e faps exception
     */
    public Collection<Map<String, ?>> getData()
        throws EFapsException
    {
        final Collection<Map<String, ?>> ret = new ArrayList<>();
        while (next()) {
            final Map<String, Object> map = new LinkedHashMap<>();
            int idx = 1;
            final Map<String, Object[]> msgphrases = new HashMap<>();
            for (final Select select : selection.getSelects()) {
                final String key = select.getAlias() == null ? String.valueOf(idx) : select.getAlias();
                if (helper == null) {
                    map.put(key, get(select));
                } else {
                    String msgPhraseKey = null;
                    for (final Entry<String, MsgPhrase> entry : helper.getMsgPhrases().entrySet()) {
                        final String alias = Print.getMsgPhraseAlias(entry.getValue().getId());
                        if (key.startsWith(alias + "_")) {
                            msgPhraseKey = entry.getKey();
                            break;
                        }
                    }
                    if (msgPhraseKey == null) {
                        boolean isPartOfPhrase = false;
                        for (final PhraseEntry entry : helper.getPhrases()) {
                            final String alias = Print.getPhraseAlias(entry.getPhraseIdx());
                            if (key.startsWith(alias + "_")) {
                                isPartOfPhrase = true;
                                break;
                            }
                        }
                        if (!isPartOfPhrase) {
                            map.put(key, get(select));
                        }
                    } else {
                        Object[] values;
                        if (!msgphrases.containsKey(msgPhraseKey)) {
                            values = new Object[0];
                        } else {
                            values = msgphrases.get(msgPhraseKey);
                        }
                        values = ArrayUtils.add(values, get(select));
                        msgphrases.put(msgPhraseKey, values);
                    }
                }
                idx++;
            }
            if (helper != null) {
                for (final PhraseEntry entry : helper.getPhrases()) {
                    map.put(entry.getAlias(), get(entry.getAlias()));
                }
            }
            for (final Entry<String, Object[]> entry : msgphrases.entrySet()) {
                final MsgPhrase msgPhrase = helper.getMsgPhrases().get(entry.getKey());
                final String value = msgPhrase.format(entry.getValue());
                map.put(entry.getKey(), value);
            }
            ret.add(map);
        }
        return ret;
    }

    /**
     * Gets the.
     *
     * @param _selection the selection
     * @return the selection evaluator
     * @throws EFapsException
     */
    public static Evaluator get(final ISelectionProvider _selectionProvider, final EvalHelper _helper)
        throws EFapsException
    {
        return new Evaluator(_selectionProvider, _helper);
    }
}
