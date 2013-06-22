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

package org.efaps.db;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.apache.commons.dbutils.BasicRowProcessor;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class RowProcessor
    extends BasicRowProcessor
{

    /**
     * Convert a <code>ResultSet</code> row into an <code>Object[]</code>. This
     * implementation copies column values into the array in the same order
     * they're returned from the <code>ResultSet</code>. Array elements will be
     * set to <code>null</code> if the column was SQL NULL.
     *
     * @see org.apache.commons.dbutils.RowProcessor#toArray(java.sql.ResultSet)
     * @param _rs ResultSet that supplies the array data
     * @throws SQLException if a database access error occurs
     * @return the newly created array
     */
    @Override
    public Object[] toArray(final ResultSet _rs)
        throws SQLException
    {
        final ResultSetMetaData metaData = _rs.getMetaData();
        final int cols = metaData.getColumnCount();
        final Object[] result = new Object[cols];

        for (int i = 0; i < cols; i++) {
            switch (metaData.getColumnType(i + 1)) {
                case java.sql.Types.TIMESTAMP:
                    result[i] = _rs.getTimestamp(i + 1);
                    break;
                default:
                    result[i] = _rs.getObject(i + 1);
            }
        }
        return result;
    }
}
