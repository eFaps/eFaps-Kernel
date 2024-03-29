/*
 * Copyright © 2003 - 2024 The eFaps Team (-)
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
 *
 */
public class CreatorLinkType
    extends PersonLinkType
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
        checkSQLColumnSize(_attribute, 1);
        try {
            _insert.column(_attribute.getSqlColNames().get(0),
                           Context.getThreadContext().getPerson().getId());
        } catch (final EFapsException e) {
            throw new SQLException("could not fetch current context person id", e);
        }
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
        throw new SQLException("not allowed");
    }
}
