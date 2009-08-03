/*
 * Copyright 2003 - 2009 The eFaps Team
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
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.db.print.value;

import java.util.ArrayList;
import java.util.List;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.IAttributeType;
import org.efaps.admin.datamodel.Type;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class AttributeValueSelect extends AbstractValueSelect
{
    /**
     * Attribute belonging to this AttributeValueSelect.
     */
    private Attribute attribute;

    /**
     * Name of the Attribute belonging to this AttributeValueSelect.
     */
    private String attrName;

    /**
     * @param _attrName name of the attribute
     */
    public AttributeValueSelect(final String _attrName)
    {
        this.attrName = _attrName;
    }

    /**
     * @param _attr Attribute
     */
    public AttributeValueSelect(final Attribute _attr)
    {
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
     * @param attribute value for instance variable {@link #attribute}
     */
    public void setAttribute(final Attribute attribute)
    {
        this.attribute = attribute;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValue(final Object _object) throws EFapsException
    {
        Object ret;
        final IAttributeType attrInterf = this.attribute.newInstance();
        final ArrayList<Object> tempList = new ArrayList<Object>();
        tempList.add(_object);
        ret = attrInterf.readValue(tempList);
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValue(final List<Object> _currentObject) throws EFapsException
    {
        final Object ret;
        final IAttributeType attrInterf = this.attribute.newInstance();
        ret = attrInterf.readValue(_currentObject);
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int append2SQLSelect(final Type _type, final StringBuilder _fromBldr, final int _tableIndex,
                                final int _colIndex)
    {
        if (this.attribute == null) {
            this.attribute = _type.getAttribute(this.attrName);
        }
        int ret = 0;
        for (final String colName : this.attribute.getSqlColNames()) {
            _fromBldr.append(",T").append(_tableIndex).append(".").append(colName);
            getColIndexs().add(_colIndex + ret);
            ret++;
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
