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
import java.util.Set;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.user.Company;
import org.efaps.db.Context;
import org.efaps.db.wrapper.AbstractSQLInsertUpdate;
import org.efaps.util.EFapsException;

/**
 * The class is the attribute type representation for the consortium of a
 * business object.
 *
 * @author The eFaps Team
 *
 */
public class ConsortiumLinkType
    extends PersonLinkType
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param _insertUpdate     insert / update SQL statement
     * @param _attribute        Attribute to be prepared
     * @param _values           values for the insert or update
     * @throws SQLException if not exact one SQL column for the attribute is
     *                      defined of the company id could not be fetched
     */
    @Override
    protected void prepare(final AbstractSQLInsertUpdate<?> _insertUpdate,
                           final Attribute _attribute,
                           final Object... _values)
        throws SQLException
    {
        checkSQLColumnSize(_attribute, 1);
        // if a value was explicitly set the value is used, else the company
        // id from the context
        if ((_values != null) && (_values.length > 0) && _values[0] != null) {
            if (_values[0] instanceof Long) {
                _insertUpdate.column(_attribute.getSqlColNames().get(0), (Long) _values[0]);
            } else {
                _insertUpdate.column(_attribute.getSqlColNames().get(0), Long.parseLong(_values[0].toString()));
            }
        } else {
            // as a defaultvalue the first found Consortium will be inserted
            try {
                final Company company = Context.getThreadContext().getCompany();
                final Set<Long> consortiums = company.getConsortiums();
                if (!consortiums.isEmpty()) {
                    _insertUpdate.column(_attribute.getSqlColNames().get(0), consortiums.iterator().next());
                }
            } catch (final EFapsException e) {
                throw new SQLException("could not fetch consortiums id", e);
            }
        }
    }
}
