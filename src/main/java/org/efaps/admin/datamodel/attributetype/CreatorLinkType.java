/*
 * Copyright 2003 - 2010 The eFaps Team
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
import org.efaps.db.Context;
import org.efaps.db.wrapper.SQLInsert;
import org.efaps.db.wrapper.SQLUpdate;
import org.efaps.util.EFapsException;

/**
 * The class is the attribute type representation for the creator person of a
 * business object.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class CreatorLinkType
    extends PersonLinkType
{
    /**
     * @see org.efaps.admin.datamodel.attributetype.AbstractLinkType#update(java.lang.Object, java.sql.PreparedStatement, int)
     * @param _object   object
     * @param _stmt     SQL statement to update the value
     * @param _index    index in the SQL statement to update the value
     * @return number of indexes used in the method, if the return value is null an error should be thrown
     * @throws SQLException on error
     */
    @Override()
    public void prepareInsert(final SQLInsert _insert,
                              final Attribute _attribute,
                              final Object... _values)
        throws SQLException
    {
        checkSQLColumnSize(_attribute, 1);
        try {
            _insert.column(_attribute.getSqlColNames().get(0),
                           Context.getThreadContext().getPerson().getId());
        } catch (final EFapsException e) {
            throw new SQLException("could not fetch current context person id", e);
        }
    }

    /**
     * An update of a creator link is not allowed and therefore a
     * {@link SQLException} is always thrown.
     *
     * @param _update   update SQL statement
     * @param _values   ignored because update is not allowed
     * @throws SQLException always because update is not allowed
     */
    @Override()
    public void prepareUpdate(final SQLUpdate _update,
                              final Attribute _attribute,
                              final Object... _values)
        throws SQLException
    {
        throw new SQLException("not allowed");
    }
}
