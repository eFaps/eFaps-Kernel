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
import org.efaps.db.wrapper.AbstractSQLInsertUpdate;

/**
 * The class is the attribute type representation for the modified date time of
 * a business object.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class ModifiedType
    extends DateTimeType
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The instance method appends
     * {@link org.efaps.db.databases.AbstractDatabase#getCurrentTimeStamp()} to
     * the SQL statement.
     *
     *  @param _insertUpdate SQL insert / update statement
     * @param _attribute    SQL update statement
     * @param _values       new object value to set; values are localized and
     *                      are coming from the user interface
     * @throws SQLException if SQL columns for the attribute are not correctly
     *                      defined
     */
    @Override
    protected void prepare(final AbstractSQLInsertUpdate<?> _insertUpdate,
                           final Attribute _attribute,
                           final Object... _values)
        throws SQLException
    {
        checkSQLColumnSize(_attribute, 1);
        _insertUpdate.columnWithCurrentTimestamp(_attribute.getSqlColNames().get(0));
    }
}
