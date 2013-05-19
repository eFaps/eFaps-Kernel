/*
 * Copyright 2003 - 2013 The eFaps Team
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
import org.efaps.db.print.ISelectPart;
import org.efaps.db.print.OneSelect;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.util.EFapsException;

/**
 * Abstract class used as base for all different types of ValueSelect.
 * A ValueSelct represents the part of a SQL-Statament that actual selects
 * a value.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public abstract class AbstractValueSelect
{

    /**
     * List of column indexes the values have in the ResultSet returned from the
     * eFaps database.
     */
    private final List<Integer> colIndexs = new ArrayList<Integer>();

    /**
     * Parent of this AbstractValueSelect.
     */
    private AbstractValueSelect parent;

    /**
     * Child of this AbstractValueSelect.
     */
    private AbstractValueSelect child;

    /**
     *  OneSelect this ValueSelect belongs to.
     */
    private final OneSelect oneSelect;

    /**
     * SelectPart this ValueSelect is connected to.
     */
    private ISelectPart parentSelectPart;

    /**
     * Constructor setting the OneSelect this valueselect belongs to.
     *
     * @param _oneSelect OneSelect
     */
    public AbstractValueSelect(final OneSelect _oneSelect)
    {
        this.oneSelect = _oneSelect;
        if (!this.oneSelect.getSelectParts().isEmpty()) {
            this.parentSelectPart = this.oneSelect.getSelectParts().get(this.oneSelect.getSelectParts().size() - 1);
        }
    }

    /**
     * Getter method for instance variable {@link #parent}.
     *
     * @return value of instance variable {@link #parent}
     */
    public AbstractValueSelect getParent()
    {
        return this.parent;
    }

    /**
     * Getter method for the instance variable {@link #parentSelectPart}.
     *
     * @return value of instance variable {@link #parentSelectPart}
     */
    public ISelectPart getParentSelectPart()
    {
        return this.parentSelectPart;
    }

    /**
     * Method must return a unique String to identify the class. This is used to
     * determine which class was instantiated instead of using "instanceof".
     *
     * @return unique name.
     */
    public abstract String getValueType();

    /**
     * Getter method for instance variable {@link #colIndexs}.
     *
     * @return instance variable {@link #colIndexs}
     */
    public List<Integer> getColIndexs()
    {
        return this.colIndexs;
    }

    /**
     * Method to set an AbstractValueSelect as the parent of this
     * AbstractValueSelect.
     *
     * @param _parent AbstractValueSelect to be set as parent
     */
    public void setParentValueSelect(final AbstractValueSelect _parent)
    {
        this.parent = _parent;
    }

    /**
     * Method adds an AbstractValueSelect as a child of this chain of
     * AbstractValueSelect.
     *
     * @param _valueSelect AbstractValueSelect to be added as child
     * @throws EFapsException on error
     */
    public void addChildValueSelect(final AbstractValueSelect _valueSelect)
        throws EFapsException
    {
        if (this.child == null) {
            this.child = _valueSelect;
            _valueSelect.setParentValueSelect(this);
        } else {
            this.child.addChildValueSelect(_valueSelect);
        }
    }

    /**
     * Method is used to add the select part for this ValueSelect to the select
     * statement. e.g. "select T0.ID, TO.TYPEID" etc.
     *
     * @param _type Type this ValueSelect belongs to
     * @param _select SQL select statement to be appended to
     * @param _tableIndex index of the table
     * @param _colIndex last index of the column
     * @return number of columns added to the select statement
     * @throws EFapsException on error
     */
    public int append2SQLSelect(final Type _type,
                                final SQLSelect _select,
                                final int _tableIndex,
                                final int _colIndex)
        throws EFapsException
    {
        return 0;
    }

    /**
     * Method to get the value for the current object.
     *
     * @param _object current object
     * @throws EFapsException on error
     * @return object
     */
    public Object getValue(final Object _object)
        throws EFapsException
    {
        return _object;
    }

    /**
     * Method to get the value for a list of object.
     *
     * @param _objectList list of objects
     * @throws EFapsException on error
     * @return object
     */
    public Object getValue(final List<Object> _objectList)
        throws EFapsException
    {
        final List<Object> ret = new ArrayList<Object>();
        for (final Object object : _objectList) {
            ret.add(getValue(object));
        }
        return _objectList.size() > 0 ? (ret.size() > 1 ? ret : ret.get(0)) : null;
    }

    /**
     * Getter method for instance variable {@link #child}.
     *
     * @return value of instance variable {@link #child}
     */
    public AbstractValueSelect getChildValueSelect()
    {
        return this.child;
    }

    /**
     * Method to return the attribute related to this AbstractValueSelect.
     *
     * @return Attribute if exists, else null
     */
    public Attribute getAttribute()
    {
        return null;
    }

    /**
     * Getter method for the instance variable {@link #oneSelect}.
     *
     * @return value of instance variable {@link #oneSelect}
     */
    public OneSelect getOneSelect()
    {
        return this.oneSelect;
    }
}
