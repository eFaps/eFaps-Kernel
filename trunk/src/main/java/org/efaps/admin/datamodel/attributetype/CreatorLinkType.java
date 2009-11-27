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

package org.efaps.admin.datamodel.attributetype;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.efaps.db.Context;
import org.efaps.util.EFapsException;

/**
 * The class is the attribute type representation for the creator person of a
 * business object.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class CreatorLinkType extends PersonLinkType
{
    /**
     * Logger for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(CreatorLinkType.class);

    // ///////////////////////////////////////////////////////////////////////////
    // interface to the data base

    /**
     * The value of the modifier is added via the prepared statement setter
     * method. So only a question mark ('?') is added to the statement. The
     * value is set with method {@link #update}.
     *
     * @param _stmt string buffer with the statement
     * @see #update
     */
    @Override
    public boolean prepareUpdate(final StringBuilder _stmt)
    {
        _stmt.append("?");
        return false;
    }

    @Override
    public String toString()
    {
        return "" + getValue();
    }

    /**
     * @see org.efaps.admin.datamodel.attributetype.AbstractLinkType#update(java.lang.Object, java.sql.PreparedStatement, int)
     * @param _object   object
     * @param _stmt     SQL statement to update the value
     * @param _index    index in the SQL statement to update the value
     * @return number of indexes used in the method, if the return value is null an error should be thrown
     * @throws SQLException on error
     */
    @Override
    public int update(final Object _object, final PreparedStatement _stmt, final int _index)
                    throws SQLException
    {
        try {
            _stmt.setLong(_index, Context.getThreadContext().getPerson().getId());
        } catch (final EFapsException e) {
            LOG.error("update(Object, PreparedStatement, List<Integer>)", e);
        }
        return 1;
    }
}
