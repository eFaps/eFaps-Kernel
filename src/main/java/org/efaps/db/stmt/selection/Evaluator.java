/*
 * Copyright 2003 - 2018 The eFaps Team
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
 *
 */

package org.efaps.db.stmt.selection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.ListValuedMap;
import org.apache.commons.collections4.MultiMapUtils;
import org.efaps.admin.access.AccessTypeEnums;
import org.efaps.db.Instance;
import org.efaps.db.stmt.selection.elements.AbstractElement;
import org.efaps.eql.JSONData;
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

    /**
     * Instantiates a new selection evaluator.
     *
     * @param _selection the selection
     */
    private Evaluator(final Selection _selection)
    {
        this.selection = _selection;
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
        if (!this.init) {
            squash();
            evalAccess();
            this.init = true;
            if (_step) {
                step(this.selection.getAllSelects());
            }
        }
    }

    /**
     * Squash.
     *
     * @throws EFapsException the e faps exception
     */
    private void squash()
        throws EFapsException
    {
        if (!this.selection.getSelects().stream().anyMatch(Select::isSquashable)) {
            final Select select = this.selection.getInstSelects().get(Selection.BASEPATH);
            final List<Object> instances = select.getObjects(null);
            Integer idx = 0;
            final ListValuedMap<Object, Integer> map = MultiMapUtils.newListValuedHashMap();
            for (final Object instance : instances) {
                map.put(instance, idx);
                idx++;
            }
            final Map<Integer, Integer> address = new HashMap<>();
            for (final Collection<Integer> set : map.asMap().values()) {
                Integer current = -1;
                for (final Integer ele : set) {
                    if (current < 0) {
                        address.put(ele, -1);
                        current = ele;
                    } else {
                        address.put(ele, current);
                    }
                }
            }
            for (final Select currentSelect : this.selection.getAllSelects()) {
                currentSelect.squash(address);
            }
        }
    }

    /**
     * Count.
     *
     * @return the int
     * @throws EFapsException the eFaps exception
     */
    public int count()
        throws EFapsException
    {
        initialize(false);
        final Select select = this.selection.getInstSelects().get(Selection.BASEPATH);
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
        if (this.selection.getSelects().size() > idx) {
            final Select select = this.selection.getSelects().get(idx);
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
        final Optional<Select> selectOpt = this.selection.getSelects().stream()
                        .filter(select -> _alias.equals(select.getAlias()))
                        .findFirst();
        if (selectOpt.isPresent()) {
            ret = get(selectOpt.get());
        }
        return (T) ret;
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
        if (obj instanceof List) {
            final Iterator<Boolean> iter = accessList.iterator();
            ret = ((List<?>) obj).stream()
                .map(ele -> iter.next() ? ele : null)
                .collect(Collectors.toList());
        } else if (accessList.size() == 1 && accessList.get(0)) {
            ret = obj;
        }
        return (T) ret;
    }

    /**
     * Inst.
     *
     * @return the instance
     */
    public Instance inst()
    {
        return (Instance) this.selection.getInstSelects().get(Selection.BASEPATH).getCurrent();
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
        boolean stepForward = true;
        boolean ret = true;
        while (stepForward && ret) {
            ret = step(this.selection.getAllSelects());
            stepForward = !this.access.hasAccess(inst());
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
        final int size = _select.getElements().size();
        if (size == 1) {
            ret.add(this.access.hasAccess(inst()));
        } else {
            int idx = 0;
            boolean accessTemp = true;
            while (idx < size && accessTemp) {
                final AbstractElement<?> element = _select.getElements().get(idx);
                idx++;
                final Select instSelect = this.selection.getInstSelects().get(element.getPath());
                if (instSelect == null) {
                    LOG.error("Could not retrieve Instance Select for Path {}", element.getPath());
                }
                final Object obj = instSelect.getCurrent();
                if (obj instanceof Instance) {
                    accessTemp = this.access.hasAccess((Instance) obj);
                    ret.clear();
                    ret.add(accessTemp);
                } else if (obj instanceof List) {
                    if (ret.isEmpty()) {
                        ret = ((List<?>) obj).stream()
                                        .map(ele -> this.access.hasAccess((Instance) ele))
                                        .collect(Collectors.toList());
                    } else {
                        final Iterator<Boolean> iter = ret.iterator();
                        ret = ((List<?>) obj).stream()
                                        .map(ele -> iter.next() && this.access.hasAccess((Instance) ele))
                                        .collect(Collectors.toList());
                    }
                    accessTemp = ret.contains(true);
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
        return this.access;
    }

    /**
     * Gets the selection.
     *
     * @return the selection
     */
    protected Selection getSelection()
    {
        return this.selection;
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
        final List<Instance> instances = new ArrayList<>();
        while (step(this.selection.getInstSelects().values())) {
            for (final Entry<String, Select> entry : this.selection.getInstSelects().entrySet()) {
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
        for (final Entry<String, Select> entry : this.selection.getInstSelects().entrySet()) {
            entry.getValue().reset();
        }
        this.access = Access.get(AccessTypeEnums.READ.getAccessType(), instances);
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
            for (final Select select : this.selection.getSelects()) {
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
    public final Collection<Map<String, ?>> getData()
        throws EFapsException
    {
        final Collection<Map<String, ?>> ret = new ArrayList<>();
        while (next()) {
            final Map<String, ?> map = new LinkedHashMap<>();
            int idx = 1;
            for (final Select select : this.selection.getSelects()) {
                final String key = select.getAlias() == null ? String.valueOf(idx) : select.getAlias();
                map.put(key, get(select));
                idx++;
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
     */
    public static Evaluator get(final Selection _selection)
    {
        return new Evaluator(_selection);
    }
}
