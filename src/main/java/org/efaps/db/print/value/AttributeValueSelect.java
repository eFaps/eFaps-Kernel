/*
 * Copyright 2003 - 2016 The eFaps Team
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

package org.efaps.db.print.value;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.Instance;
import org.efaps.db.print.LinkToSelectPart;
import org.efaps.db.print.OneSelect;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id: AttributeValueSelect.java 3520 2009-12-21 12:37:44Z tim.moxter
 *          $
 */
public class AttributeValueSelect
    extends AbstractValueSelect
{

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(OneSelect.class);

    /**
     * Attribute belonging to this AttributeValueSelect.
     */
    private Attribute attribute;

    /**
     * Name of the Attribute belonging to this AttributeValueSelect.
     */
    private String attrName;

    /**
     * @param _oneSelect OneSelect
     * @param _attrName name of the attribute
     */
    public AttributeValueSelect(final OneSelect _oneSelect,
                                final String _attrName)
    {
        super(_oneSelect);
        this.attrName = _attrName;
    }

    /**
     * @param _oneSelect OneSelect
     * @param _attr Attribute
     */
    public AttributeValueSelect(final OneSelect _oneSelect,
                                final Attribute _attr)
    {
        super(_oneSelect);
        this.attribute = _attr;
    }

    /**
     * Getter method for instance variable {@link #attrName}.
     *
     * @return value of instance variable {@link #attrName}
     */
    public String getAttrName()
    {
        return this.attrName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Attribute getAttribute()
    {
        return this.attribute;
    }

    /**
     * Setter method for instance variable {@link #attribute}.
     *
     * @param _attribute value for instance variable {@link #attribute}
     */
    public void setAttribute(final Attribute _attribute)
    {
        this.attribute = _attribute;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValue(final Object _object)
        throws EFapsException
    {
        final ArrayList<Object> tempList = new ArrayList<Object>();
        tempList.add(_object);
        return getValue(tempList);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValue(final List<Object> _objectList)
        throws EFapsException
    {
        // check it the right attribute was selected during preparation
        if (getParentSelectPart() != null && getParentSelectPart() instanceof LinkToSelectPart) {
            final Instance linkInstance = ((LinkToSelectPart) getParentSelectPart()).getCurrentInstance();
            if (linkInstance != null && linkInstance.isValid()) {
                if (linkInstance.getType().getId() != this.attribute.getParentId()) {
                    this.attribute = linkInstance.getType().getAttribute(this.attribute.getName());
                }
            }
        } else if (this.attribute.getParent().isAbstract() && getOneSelect().getQuery().getCurrentInstance() != null
                        && getOneSelect().getQuery().getCurrentInstance().isValid()) {
            final Attribute attrTmp = getOneSelect().getQuery().getCurrentInstance().getType().getAttribute(
                            this.attribute.getName());
            if (attrTmp != null) {
                this.attribute = attrTmp;
            }
        }

        Object ret = this.attribute.readDBValue(_objectList);
        int i = this.attribute.getSqlColNames().size();
        for (final Attribute attr : this.attribute.getDependencies().values()) {
            final List<Object> tmpObjectList = new ArrayList<Object>();
            for (final Object object : _objectList) {
                final Object[] inner = new Object[attr.getSqlColNames().size()];
                tmpObjectList.add(inner);
                for (int j = 0; j < attr.getSqlColNames().size(); j++) {
                    inner[j] = ((Object[]) object)[i + j];
                }
            }

            final Object tmpRet  = attr.readDBValue(tmpObjectList);
            if (ret instanceof List<?>) {
                final Iterator<?> iter = ((List<?>) tmpRet).iterator();
                for (Object object : (List<?>) ret) {
                    object = getVal(object, iter.next());
                }
            } else {
                ret = getVal(ret, tmpRet);
            }
            i++;
        }
        if (getChildValueSelect() != null) {
            if (getChildValueSelect() instanceof IAttributeChildValueSelect) {
                final IAttributeChildValueSelect val = (IAttributeChildValueSelect) getChildValueSelect();
                ret = val.get(this.attribute, ret);
            }
        }
        return ret;
    }

    /**
     * @param _existingVal alllready existing
     * @param _toAdd to add
     * @return objetc
     */
    private Object getVal(final Object _existingVal,
                          final Object _toAdd)
    {
        final List<Object> tmpRetList = new ArrayList<Object>();
        if (_existingVal instanceof Object[]) {
            for (final Object object : (Object[]) _existingVal) {
                tmpRetList.add(object);
            }
        } else {
            tmpRetList.add(_existingVal);
        }
        if (_toAdd instanceof Object[]) {
            for (final Object object : (Object[]) _toAdd) {
                tmpRetList.add(object);
            }
        } else {
            tmpRetList.add(_toAdd);
        }
        return tmpRetList.toArray();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int append2SQLSelect(final Type _type,
                                final SQLSelect _select,
                                final int _tableIndex,
                                final int _colIndex)
        throws EFapsException
    {
        if (this.attribute == null) {
            this.attribute = _type.getAttribute(this.attrName);
        }
        if (this.attribute == null) {
            AttributeValueSelect.LOG.error("Could not get an attribute with name '{} 'for type: '{}'", this.attrName,
                            _type);
            throw new EFapsException(AttributeValueSelect.class, "appendNoAttribute");
        }
        int ret = 0;
        for (final String colName : this.attribute.getSqlColNames()) {
            _select.column(_tableIndex, colName);
            getColIndexs().add(_colIndex + ret);
            ret++;
        }
        // in case of dependencies for the attribute they must be selected also
        for (final Attribute attr : this.attribute.getDependencies().values()) {
            for (final String colName : attr.getSqlColNames()) {
                _select.column(_tableIndex, colName);
                getColIndexs().add(_colIndex + ret);
                ret++;
            }
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getValueType()
    {
        return "attribute";
    }
}
