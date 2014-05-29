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

package org.efaps.admin.datamodel.attributetype;

import java.sql.SQLException;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.IAttributeType;
import org.efaps.db.Instance;
import org.efaps.db.wrapper.AbstractSQLInsertUpdate;
import org.efaps.db.wrapper.SQLInsert;
import org.efaps.db.wrapper.SQLUpdate;
import org.efaps.util.EFapsException;

/**
 * @author The eFaps Team
 * @version $Id$
 */
public abstract class AbstractType
    implements IAttributeType
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * {@inheritDoc}
     */
    @Override
    public void prepareInsert(final SQLInsert _insert,
                              final Attribute _attribute,
                              final Object... _values)
        throws SQLException
    {
        prepare(_insert, _attribute, _values);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void prepareUpdate(final SQLUpdate _update,
                              final Attribute _attribute,
                              final Object... _values)
        throws SQLException
    {
        prepare(_update, _attribute, _values);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void valiate4Update(final Attribute _attribute,
                               final Instance _instance,
                               final Object[] _value)
        throws EFapsException
    {
        // as default the value is valid and therefore no error must be thrown
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void valiate4Insert(final Attribute _attribute,
                               final Instance _instance,
                               final Object[] _value)
        throws EFapsException
    {
        // as default the value is valid and therefore no error must be thrown
    }

    /**
     *
     * @param _insertUpdate SQL insert / update statement
     * @param _attribute    SQL update statement
     * @param _values       new object value to set; values are localized and
     *                      are coming from the user interface
     * @throws SQLException always, because the method must be overwritten
     */
    protected void prepare(final AbstractSQLInsertUpdate<?> _insertUpdate,
                           final Attribute _attribute,
                           final Object... _values)
        throws SQLException
    {
        throw new SQLException("not implemented for " + this.getClass());
    }

    /**
     * Checks for expected <code>_size</code> of SQL columns for
     * <code>_attribute</code>.
     *
     * @param _attribute    attribute to check
     * @param _size         expected size of SQL columns
     * @throws SQLException if the size of the SQL columns in
     *                      <code>_attribute</code> is not the expected
     *                      <code>_size</code>
     */
    public void checkSQLColumnSize(final Attribute _attribute,
                                   final int _size)
        throws SQLException
    {
        if ((_attribute.getSqlColNames() == null) || _attribute.getSqlColNames().isEmpty())  {
            throw new SQLException("no SQL column for attribute defined");
        }
        if (_attribute.getSqlColNames().size() > _size)  {
            throw new SQLException("more than " + _size + " SQL columns defined (is "
                    + _attribute.getSqlColNames().size() + ")");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString4Where(final Object _value)
        throws EFapsException
    {
        return _value.toString();
    }

}
