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
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.AttributeSet;
import org.efaps.admin.datamodel.Classification;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.stmt.selection.elements.AbstractDataElement;
import org.efaps.db.stmt.selection.elements.AbstractElement;
import org.efaps.db.stmt.selection.elements.AttributeElement;
import org.efaps.db.stmt.selection.elements.AttributeSetElement;
import org.efaps.db.stmt.selection.elements.ClassElement;
import org.efaps.db.stmt.selection.elements.ExecElement;
import org.efaps.db.stmt.selection.elements.FormatElement;
import org.efaps.db.stmt.selection.elements.IDElement;
import org.efaps.db.stmt.selection.elements.InstanceElement;
import org.efaps.db.stmt.selection.elements.KeyElement;
import org.efaps.db.stmt.selection.elements.LabelElement;
import org.efaps.db.stmt.selection.elements.LinkfromElement;
import org.efaps.db.stmt.selection.elements.LinktoElement;
import org.efaps.db.stmt.selection.elements.NameElement;
import org.efaps.db.stmt.selection.elements.OIDElement;
import org.efaps.db.stmt.selection.elements.StatusElement;
import org.efaps.db.stmt.selection.elements.TypeElement;
import org.efaps.db.stmt.selection.elements.UUIDElement;
import org.efaps.eql2.IAttributeSelectElement;
import org.efaps.eql2.IAttributeSetSelectElement;
import org.efaps.eql2.IBaseSelectElement;
import org.efaps.eql2.IClassSelectElement;
import org.efaps.eql2.IExecSelectElement;
import org.efaps.eql2.IFormatSelectElement;
import org.efaps.eql2.ILinkfromSelectElement;
import org.efaps.eql2.ILinktoSelectElement;
import org.efaps.eql2.IPrintStatement;
import org.efaps.eql2.ISelect;
import org.efaps.eql2.ISelectElement;
import org.efaps.eql2.IStatement;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;
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
    private final Map<String, Select> instSelects = new LinkedHashMap<>();

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
        for (final ISelect sel : ((IPrintStatement<?>) stmt).getSelection().getSelects()) {
            final Select select = Select.get(sel.getAlias());
            if (this.selects.isEmpty()) {
                this.instSelects.put(BASEPATH, Select.get().addElement(new InstanceElement(type)));
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
                    addInstSelect(select, element, attr, currentType);
                } else if (ele instanceof ILinkfromSelectElement) {
                    final String typeName = ((ILinkfromSelectElement) ele).getTypeName();
                    final String attrName = ((ILinkfromSelectElement) ele).getAttribute();
                    final Type linkFromType = Type.get(typeName);
                    final Attribute attr = linkFromType.getAttribute(attrName);
                    final LinkfromElement element = new LinkfromElement().setAttribute(attr).setStartType(currentType);
                    select.addElement(element);
                    addInstSelect(select, element, attr, currentType);
                    currentType = linkFromType;
                } else if (ele instanceof IClassSelectElement) {
                    final String typeName = ((IClassSelectElement) ele).getName();
                    final Classification classification = Classification.get(typeName);
                    final ClassElement element = new ClassElement().setClassification(classification)
                                    .setType(currentType);
                    select.addElement(element);
                    addInstSelect(select, element, classification, currentType);
                    currentType = classification;
                } else if (ele instanceof IAttributeSetSelectElement) {
                    final String attrSetName = ((IAttributeSetSelectElement) ele).getName();
                    final AttributeSet attributeSet = AttributeSet.find(currentType.getName(), attrSetName);
                    final AttributeSetElement element = new AttributeSetElement().setAttributeSet(attributeSet)
                                    .setType(currentType);
                    select.addElement(element);
                    //addInstSelect(select, element, classification, currentType);
                    currentType = attributeSet;
                } else if (ele instanceof IBaseSelectElement) {
                    switch (((IBaseSelectElement) ele).getElement()) {
                        case INSTANCE:
                            select.addElement(new InstanceElement(currentType));
                            break;
                        case OID:
                            select.addElement(new OIDElement(currentType));
                            break;
                        case STATUS:
                            select.addElement(new StatusElement(currentType));
                            break;
                        case TYPE:
                            select.addElement(new TypeElement(currentType));
                            break;
                        case KEY:
                            select.addElement(new KeyElement());
                            break;
                        case LABEL:
                            select.addElement(new LabelElement());
                            break;
                        case NAME:
                            select.addElement(new NameElement());
                            break;
                        case ID:
                            select.addElement(new IDElement());
                            break;
                        case UUID:
                            select.addElement(new UUIDElement());
                            break;
                        default:
                            break;
                    }
                } else if (ele instanceof IFormatSelectElement) {
                    final String pattern = ((IFormatSelectElement) ele).getPattern();
                    select.addElement(new FormatElement().setPattern(pattern));
                } else if (ele instanceof IExecSelectElement) {
                    select.addElement(new ExecElement(currentType)
                                    .setEsjp(((IExecSelectElement) ele).getClassName())
                                    .setParameters(((IExecSelectElement) ele).getParameters()));
                }
            }
        }
        return this;
    }

    /**
     * Adds the inst select.
     *
     * @param _select the select
     * @param _element the element
     * @param _attr the attr
     * @param _currentType the current type
     * @throws CacheReloadException on error
     */
    private void addInstSelect(final Select _select, final AbstractDataElement<?> _element, final Object _attrOrClass,
                               final Type _currentType)
        throws CacheReloadException
    {
        if (StringUtils.isNotEmpty(_element.getPath()) && !this.instSelects.containsKey(_element.getPath())) {
            final Select instSelect = Select.get();
            for (final AbstractElement<?> selectTmp : _select.getElements()) {
                if (!selectTmp.equals(_element)) {
                    if (selectTmp instanceof LinktoElement) {
                        instSelect.addElement(new LinktoElement().setAttribute(((LinktoElement) selectTmp)
                                        .getAttribute()));
                    } else if (selectTmp instanceof LinkfromElement) {
                        instSelect.addElement(new LinkfromElement().setAttribute(((LinkfromElement) selectTmp)
                                        .getAttribute()).setStartType(((LinkfromElement) selectTmp).getStartType()));
                    } else if (selectTmp instanceof ClassElement) {
                        instSelect.addElement(new ClassElement()
                                        .setClassification(((ClassElement) selectTmp).getClassification())
                                        .setType(((ClassElement) selectTmp).getType()));
                    }
                }
            }
            if (_element instanceof LinkfromElement) {
                instSelect.addElement(new LinkfromElement().setAttribute((Attribute) _attrOrClass).setStartType(
                                _currentType));
                instSelect.addElement(new InstanceElement(((Attribute) _attrOrClass).getParent()));
            } else if (_element instanceof LinktoElement) {
                instSelect.addElement(new LinktoElement().setAttribute((Attribute) _attrOrClass));
                instSelect.addElement(new InstanceElement(_currentType));
            } else if (_element instanceof ClassElement) {
                instSelect.addElement(new ClassElement().setClassification((Classification) _attrOrClass).setType(
                                _currentType));
                instSelect.addElement(new InstanceElement((Type) _attrOrClass));
            }
            this.instSelects.put(_element.getPath(), instSelect);
        }
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
     * Gets the all data selects.
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
