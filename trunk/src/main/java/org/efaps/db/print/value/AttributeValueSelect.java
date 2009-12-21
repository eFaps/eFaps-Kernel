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
import org.efaps.admin.datamodel.Type;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class AttributeValueSelect
    extends AbstractValueSelect
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
    @Override()
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
    @Override()
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
    @Override()
    public Object getValue(final List<Object> _objectList)
        throws EFapsException
    {
        Object ret = this.attribute.readDBValue(_objectList);

        if (getChildValueSelect() != null) {
            if ("format".equals(getChildValueSelect().getValueType())) {
                final FormatValueSelect format = (FormatValueSelect) getChildValueSelect();
                ret = format.format(this.attribute, ret);
            }
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override()
    public int append2SQLSelect(final Type _type,
                                final SQLSelect _select,
                                final int _tableIndex,
                                final int _colIndex)
    {
        if (this.attribute == null) {
            this.attribute = _type.getAttribute(this.attrName);
        }
        int ret = 0;
        for (final String colName : this.attribute.getSqlColNames()) {
            _select.column(_tableIndex, colName);
            getColIndexs().add(_colIndex + ret);
            ret++;
        }
        return ret;

    }

    /**
     * {@inheritDoc}
     */
    @Override()
    public String getValueType()
    {
        return "attribute";
    }
}
