/*
 * Copyright 2003 - 2016 The eFaps Team
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
 */

package org.efaps.db.wrapper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.efaps.db.Context;
import org.efaps.db.transaction.ConnectionResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>An easy wrapper for a SQL update statement. To initialize this class use
 * {@link org.efaps.db.databases.AbstractDatabase#newInsert(String, String, boolean)}
 * to get a database specific insert.</p>
 *
 * <p><b>Example:</b><br/>
 * <pre>
 * SQLInsert insert = Context.getDbType().newInsert("MYTABLE", "ID", true);
 * </pre></p>
 *
 * @see org.efaps.db.databases.AbstractDatabase#newInsert(String, String, boolean)
 * @author The eFaps Team
 *
 */
public class SQLInsert
    extends AbstractSQLInsertUpdate<SQLInsert>
{
    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(SQLInsert.class);

    /**
     * Must a new id created within this insert?
     */
    private final boolean newId;

    /**
     * Initializes the insert. Do not call the constructor directly, instead
     * use
     * {@link org.efaps.db.databases.AbstractDatabase#newInsert(String, String, boolean)}
     * to get the database specific implementation.
     *
     * @param _tableName    name of the table to insert
     * @param _idCol        column holding the id
     * @param _newId        <i>true</i> if a new id must be created; otherwise
     *                      <i>false</i>
     */
    public SQLInsert(final String _tableName,
                     final String _idCol,
                     final boolean _newId)
    {
        super(_tableName, _idCol);
        this.newId = _newId;
    }

    /**
     * Executes the SQL insert.
     *
     * @param _con  SQL connection
     * @return if a new id must be created (defined in {@link #newId}) this new
     *         generated id is returned
     * @throws SQLException if insert failed
     */
    public Long execute(final ConnectionResource _con)
        throws SQLException
    {
        final boolean supGenKey = Context.getDbType().supportsGetGeneratedKeys();

        Long ret = null;
        if (this.newId && !supGenKey)  {
            ret = Context.getDbType().getNewId(_con, getTableName(), getIdColumn());
            this.column(getIdColumn(), ret);
        }

        final StringBuilder cmd = new StringBuilder()
            .append(Context.getDbType().getSQLPart(SQLPart.INSERT))
            .append(" ")
            .append(Context.getDbType().getSQLPart(SQLPart.INTO))
            .append(" ")
            .append(Context.getDbType().getTableQuote())
            .append(getTableName())
            .append(Context.getDbType().getTableQuote())
            .append(" ")
            .append(Context.getDbType().getSQLPart(SQLPart.PARENTHESIS_OPEN));

        final StringBuilder val = new StringBuilder();

        // append SQL values
        boolean first = true;
        for (final ColumnWithSQLValue col : getColumnWithSQLValues())  {
            if (first)  {
                first = false;
            } else  {
                cmd.append(Context.getDbType().getSQLPart(SQLPart.COMMA));
                val.append(Context.getDbType().getSQLPart(SQLPart.COMMA));
            }
            cmd.append(Context.getDbType().getColumnQuote())
                .append(col.getColumnName())
                .append(Context.getDbType().getColumnQuote());
            val.append(col.getSqlValue());
        }

        // append values
        for (final AbstractColumnWithValue<?> col : getColumnWithValues())  {
            if (first)  {
                first = false;
            } else  {
                cmd.append(Context.getDbType().getSQLPart(SQLPart.COMMA));
                val.append(Context.getDbType().getSQLPart(SQLPart.COMMA));
            }
            cmd.append(Context.getDbType().getColumnQuote())
                .append(col.getColumnName())
                .append(Context.getDbType().getColumnQuote());
            val.append('?');
        }
        cmd.append(Context.getDbType().getSQLPart(SQLPart.PARENTHESIS_CLOSE))
            .append(Context.getDbType().getSQLPart(SQLPart.VALUES))
            .append(Context.getDbType().getSQLPart(SQLPart.PARENTHESIS_OPEN))
            .append(val)
            .append(Context.getDbType().getSQLPart(SQLPart.PARENTHESIS_CLOSE));

        SQLInsert.LOG.debug("Executing SQL: {}", cmd.toString());

        final PreparedStatement stmt;
        if (this.newId && supGenKey) {
            if (Context.getDbType().supportsMultiGeneratedKeys()) {
                stmt = _con.prepareStatement(cmd.toString(), new String[]{ getIdColumn() });
            } else {
                stmt = _con.prepareStatement(cmd.toString(), Statement.RETURN_GENERATED_KEYS);
            }
        } else {
            stmt = _con.prepareStatement(cmd.toString());
        }

        int index = 1;
        for (final AbstractColumnWithValue<?> col : getColumnWithValues())  {
            if (SQLInsert.LOG.isDebugEnabled()) {
                SQLInsert.LOG.debug("    " + index + " = " + col.getValue());
            }
            col.set(index++, stmt);
        }

        try {
            final int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Object for SQL table '" + getTableName()
                        + "' does not exists and was not inserted.");
            }

            // if auto generated get new id
            if (this.newId && supGenKey) {
                final ResultSet resultset = stmt.getGeneratedKeys();
                if (resultset.next()) {
                    ret = resultset.getLong(1);
                }
                resultset.close();
            }
        } finally  {
            stmt.close();
        }

        if (this.newId && SQLInsert.LOG.isDebugEnabled()) {
            SQLInsert.LOG.debug("new generated id " + ret);
        }

        return ret;
    }
}
