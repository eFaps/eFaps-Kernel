/*
 * Copyright 2003 - 2011 The eFaps Team
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
import org.efaps.db.wrapper.AbstractSQLInsertUpdate;
import org.efaps.util.EFapsException;

/**
 * The class is the attribute type representation for the owner person of a
 * business object.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class OwnerLinkType
    extends PersonLinkType
{
    /**
     * The instance method sets the value in the insert statement to the id of
     * the current context user.
     *
     * @param _insertUpdate SQL insert / update statement
     * @param _attribute    SQL update statement
     * @param _values       new object value to set; values are localized and
     *                      are coming from the user interface
     * @throws SQLException on error
     */
    @Override
    protected void prepare(final AbstractSQLInsertUpdate<?> _insertUpdate,
                           final Attribute _attribute,
                           final Object... _values)
        throws SQLException
    {
        checkSQLColumnSize(_attribute, 1);
        try {
            _insertUpdate.column(_attribute.getSqlColNames().get(0),
                                 Context.getThreadContext().getPerson().getId());
        } catch (final EFapsException e) {
            throw new SQLException("could not fetch current context person id", e);
        }
    }
}
