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

package org.efaps.db.wrapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.efaps.db.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <p>An easy wrapper for a SQL update statement. To initialize this class use
 * {@link org.efaps.db.databases.AbstractDatabase#newUpdate(String, String, long)}
 * to get a database specific update.</p>
 *
 * <p><b>Example:</b><br/>
 * <pre>
 * SQLUpdate insert = Context.getDbType().newUpdate("MYTABLE", "ID", 12);
 * </pre></p>
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class SQLUpdate
    extends AbstractSQLInsertUpdate<SQLUpdate>
{
    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(SQLUpdate.class);

    /**
     * Id of the row which is updated.
     */
    private final long id;

    /**
     * Initializes this update. Do not call the constructor directly, instead
     * use
     * {@link org.efaps.db.databases.AbstractDatabase#newUpdate(String, String, long)}
     * to get the database specific implementation.
     *
     * @param _tableName    name of the table to update
     * @param _idCol        name of the column with the id
     * @param _id           id to update
     */
    public SQLUpdate(final String _tableName,
                     final String _idCol,
                     final long _id)
    {
        super(_tableName, _idCol);
        this.id = _id;
    }

    /**
     * Executes the SQL update.
     *
     * @param _con      SQL connection
     * @throws SQLException if update failed or the row for given {@link #id}
     *                      does not exists
     */
    public void execute(final Connection _con)
        throws SQLException
    {
        final StringBuilder cmd = new StringBuilder()
            .append("update ")
            .append(Context.getDbType().getTableQuote())
            .append(this.getTableName())
            .append(Context.getDbType().getTableQuote())
            .append(" set ");

        // append SQL values
        boolean first = true;
        for (final ColumnWithSQLValue col : this.getColumnWithSQLValues())  {
            if (first)  {
                first = false;
            } else  {
                cmd.append(',');
            }
            cmd.append(Context.getDbType().getColumnQuote())
                .append(col.getColumnName())
                .append(Context.getDbType().getColumnQuote())
                .append("=")
                .append(col.getSqlValue());
        }

        // append values
        for (final ColumnWithValue<?> col : this.getColumnWithValues())  {
            if (first)  {
                first = false;
            } else  {
                cmd.append(',');
            }
            cmd.append(Context.getDbType().getColumnQuote())
                .append(col.getColumnName())
                .append(Context.getDbType().getColumnQuote())
                .append("=?");
        }

        // append where clause
        cmd.append(" where ")
            .append(Context.getDbType().getColumnQuote())
            .append(this.getIdColumn())
            .append(Context.getDbType().getColumnQuote())
            .append("=?");

        if (SQLUpdate.LOG.isDebugEnabled()) {
            SQLUpdate.LOG.debug(cmd.toString());
        }

        final PreparedStatement stmt = _con.prepareStatement(cmd.toString());
        int index = 1;
        for (final ColumnWithValue<?> col : this.getColumnWithValues())  {
            if (SQLUpdate.LOG.isDebugEnabled()) {
                SQLUpdate.LOG.debug("    " + index + " = " + col.getValue());
            }
            col.set(index++, stmt);
        }

        if (SQLUpdate.LOG.isDebugEnabled()) {
            SQLUpdate.LOG.debug("    " + index + " = " + this.id);
        }
        stmt.setLong(index, this.id);

        try  {
            final int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Object for SQL table '" + this.getTableName()
                        + "' with id '" + this.id + "' does not exists and was not updated.");
            }
        } finally  {
            stmt.close();
        }
    }
}
