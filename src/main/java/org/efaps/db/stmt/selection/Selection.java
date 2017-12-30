/*
 * Copyright 2003 - 2017 The eFaps Team
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.stmt.selection.elements.AbstractElement;
import org.efaps.db.stmt.selection.elements.AttributeElement;
import org.efaps.db.stmt.selection.elements.IPrimed;
import org.efaps.db.stmt.selection.elements.InstanceElement;
import org.efaps.db.stmt.selection.elements.LinkfromElement;
import org.efaps.db.stmt.selection.elements.LinktoElement;
import org.efaps.db.stmt.selection.elements.PrimedElement;
import org.efaps.eql2.IAttributeSelectElement;
import org.efaps.eql2.IBaseSelectElement;
import org.efaps.eql2.ILinkfromSelectElement;
import org.efaps.eql2.ILinktoSelectElement;
import org.efaps.eql2.IPrintQueryStatement;
import org.efaps.eql2.IPrintStatement;
import org.efaps.eql2.ISelect;
import org.efaps.eql2.ISelectElement;
import org.efaps.eql2.IStatement;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class Selection.
 *
 * @author The eFaps Team
 */
public final class Selection
{

    /** The Constant BASE. */
    protected static final String BASEPATH = "baseInstSelect";

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(Selection.class);

    /** The elements. */
    private final List<Select> selects = new ArrayList<>();

    /** The inst selects. */
    private final Map<String, Select> instSelects = new HashMap<>();

    /**
     * Analyze.
     *
     * @param _stmtProvider the stmt provider
     * @return the selection
     * @throws EFapsException on error
     */
    private Selection analyze(final IStmtProvider _stmtProvider)
        throws EFapsException
    {
        final Type type = evalMainType(_stmtProvider.getTypes());
        final IStatement<?> stmt = _stmtProvider.getStmt();
        final boolean evalInst = stmt instanceof IPrintQueryStatement;
        for (final ISelect sel : ((IPrintStatement<?>) stmt).getSelection().getSelects()) {
            final Select select = Select.get(sel.getAlias());
            if (this.selects.isEmpty()) {
                this.instSelects.put(BASEPATH, Select.get().addElement(evalInst
                                ? new InstanceElement(type)
                                : new PrimedElement(((IPrimed) _stmtProvider).getObjects())));
            }
            this.selects.add(select);
            Type currentType = type;
            for (final ISelectElement ele : sel.getElements()) {
                if (ele instanceof IAttributeSelectElement) {
                    final String attrName = ((IAttributeSelectElement) ele).getName();
                    final Attribute attr = currentType.getAttribute(attrName);
                    if (attr == null) {
                        LOG.error("Could not find Attribute '{}' on Type '{}'", attrName, currentType.getName());
                    }
                    final AttributeElement element = new AttributeElement().setAttribute(attr);
                    select.addElement(element);
                } else if (ele instanceof ILinktoSelectElement) {
                    final String attrName = ((ILinktoSelectElement) ele).getName();
                    final Attribute attr = currentType.getAttribute(attrName);
                    final LinktoElement element = new LinktoElement().setAttribute(attr);
                    select.addElement(element);
                    currentType = attr.getLink();
                    if (StringUtils.isNotEmpty(element.getPath()) && !this.instSelects.containsKey(element.getPath())) {
                        final Select instSelect = Select.get();
                        for (final AbstractElement<?> selectTmp : select.getElements()) {
                            if (!selectTmp.equals(element) && selectTmp instanceof LinktoElement) {
                                instSelect.addElement(new LinktoElement()
                                                .setAttribute(((LinktoElement) selectTmp).getAttribute()));
                            }
                        }
                        instSelect.addElement(new LinktoElement().setAttribute(attr));
                        instSelect.addElement(new InstanceElement(currentType));
                        this.instSelects.put(element.getPath(), instSelect);
                    }
                } else if (ele instanceof ILinkfromSelectElement) {
                    final String typeName = ((ILinkfromSelectElement) ele).getTypeName();
                    final String attrName = ((ILinkfromSelectElement) ele).getAttribute();
                    final Type linkFromType = Type.get(typeName);
                    final Attribute attr = linkFromType.getAttribute(attrName);
                    final LinkfromElement element = new LinkfromElement().setAttribute(attr).setStartType(currentType);
                    select.addElement(element);
                    currentType = linkFromType;
                } else if (ele instanceof IBaseSelectElement) {
                    switch (((IBaseSelectElement) ele).getElement()) {
                        case INSTANCE:
                            select.addElement(new InstanceElement(currentType));
                            break;
                        default:
                            break;
                    }
                }
            }
        }
        return this;
    }

    /**
     * Gets the main type.
     *
     * @param _baseTypes the base types
     * @return the main type
     * @throws EFapsException on error
     */
    private Type evalMainType(final Collection<Type> _baseTypes)
        throws EFapsException
    {
        final Type ret;
        if (_baseTypes.size() == 1) {
            ret = _baseTypes.iterator().next();
        } else {
            final List<List<Type>> typeLists = new ArrayList<>();
            for (final Type type : _baseTypes) {
                final List<Type> typesTmp = new ArrayList<>();
                typeLists.add(typesTmp);
                Type tmpType = type;
                while (tmpType != null) {
                    typesTmp.add(tmpType);
                    tmpType = tmpType.getParentType();
                }
            }

            final Set<Type> common = new LinkedHashSet<>();
            if (!typeLists.isEmpty()) {
                final Iterator<List<Type>> iterator = typeLists.iterator();
                common.addAll(iterator.next());
                while (iterator.hasNext()) {
                    common.retainAll(iterator.next());
                }
            }
            if (common.isEmpty()) {
                throw new EFapsException(Selection.class, "noCommon", _baseTypes);
            } else {
                // first common type
                ret = common.iterator().next();
            }
        }
        return ret;
    }

    /**
     * Gets the elements.
     *
     * @return the elements
     */
    public List<Select> getSelects()
    {
        return this.selects;
    }

    /**
     * Gets the inst selects.
     *
     * @return the inst selects
     */
    public Map<String, Select> getInstSelects()
    {
        return this.instSelects;
    }

    /**
     * Gets the all selects.
     *
     * @return the all selects
     */
    public Collection<Select> getAllSelects()
    {
        final List<Select> ret = new ArrayList<>(this.selects);
        ret.addAll(this.instSelects.values());
        return Collections.unmodifiableCollection(ret);
    }

    /**
     * Gets the.
     *
     * @param _stmtProvider the stmt provider
     * @return the selection
     * @throws EFapsException on error
     */
    public static Selection get(final IStmtProvider _stmtProvider)
        throws EFapsException
    {
        return new Selection().analyze(_stmtProvider);
    }
}
