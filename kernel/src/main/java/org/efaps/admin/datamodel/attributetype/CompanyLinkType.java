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
 * The class is the attribute type representation for the company of a
 * business object.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class CompanyLinkType extends PersonLinkType
{
    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(CompanyLinkType.class);

    /**
     * The value of the modifier is added via the prepared statement setter
     * method. So only a question mark ('?') is added to the statement. The
     * value is set with method {@link #update}.
     *
     * @param _stmt string buffer with the statement
     * @see #update
     * @return false
     */
    @Override
    public boolean prepareUpdate(final StringBuilder _stmt)
    {
        _stmt.append("?");
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int update(final Object _object,
                      final PreparedStatement _stmt,
                      final int _index)
        throws SQLException
    {
        try {
            //if a value was explicitly set the value is used, else the company id from the context
            if (getValue() != null && getValue() instanceof Long) {
                _stmt.setLong(_index, (Long) getValue());
            } else {
                _stmt.setLong(_index, Context.getThreadContext().getCompany().getId());
            }
        } catch (final EFapsException e) {
            CompanyLinkType.LOG.error("update(Object, PreparedStatement, List<Integer>)", e);
        }
        return 1;
    }
}
