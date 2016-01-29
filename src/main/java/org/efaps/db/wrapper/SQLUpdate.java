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

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.dbutils.handlers.ArrayListHandler;
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
     * @return the set< string>
     * @throws SQLException if update failed or the row for given {@link #id}
     *                      does not exists
     */
    public Set<String> execute(final Connection _con)
        throws SQLException
    {
        final Set<String> ret = new HashSet<>();
        if (checkUpdateRequired(_con)) {
            final StringBuilder cmd = new StringBuilder()
                .append(Context.getDbType().getSQLPart(SQLPart.UPDATE)).append(" ")
                .append(Context.getDbType().getTableQuote())
                .append(getTableName())
                .append(Context.getDbType().getTableQuote()).append(" ")
                .append(Context.getDbType().getSQLPart(SQLPart.SET)).append(" ");

            // append SQL values
            boolean first = true;
            for (final ColumnWithSQLValue col : getColumnWithSQLValues())  {
                if (first)  {
                    first = false;
                } else  {
                    cmd.append(Context.getDbType().getSQLPart(SQLPart.COMMA));
                }
                cmd.append(Context.getDbType().getColumnQuote())
                    .append(col.getColumnName())
                    .append(Context.getDbType().getColumnQuote())
                    .append(Context.getDbType().getSQLPart(SQLPart.EQUAL))
                    .append(col.getSqlValue());
            }

            // append values
            for (final AbstractColumnWithValue<?> col : getColumnWithValues())  {
                if (first)  {
                    first = false;
                } else  {
                    cmd.append(Context.getDbType().getSQLPart(SQLPart.COMMA));
                }
                cmd.append(Context.getDbType().getColumnQuote())
                    .append(col.getColumnName())
                    .append(Context.getDbType().getColumnQuote())
                    .append(Context.getDbType().getSQLPart(SQLPart.EQUAL))
                    .append("?");
                ret.add(col.getColumnName());
            }

            // append where clause
            cmd.append(" ").append(Context.getDbType().getSQLPart(SQLPart.WHERE)).append(" ")
                .append(Context.getDbType().getColumnQuote())
                .append(getIdColumn())
                .append(Context.getDbType().getColumnQuote())
                .append(Context.getDbType().getSQLPart(SQLPart.EQUAL))
                .append("?");

            if (SQLUpdate.LOG.isDebugEnabled()) {
                SQLUpdate.LOG.debug(cmd.toString());
            }

            final PreparedStatement stmt = _con.prepareStatement(cmd.toString());
            int index = 1;
            for (final AbstractColumnWithValue<?> col : getColumnWithValues())  {
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
                    throw new SQLException("Object for SQL table '" + getTableName()
                            + "' with id '" + this.id + "' does not exists and was not updated.");
                }
            } finally  {
                stmt.close();
            }
        }
        return ret;
    }

    /**
     * Check if the execution of the update is neccesary by comparing the values.
     * @param _con connection to be used
     * @return true if the execution of the update is necessary
     * @throws SQLException on error
     */
    private boolean checkUpdateRequired(final Connection _con)
        throws SQLException
    {
        final SQLSelect select = new SQLSelect();
        for (final AbstractColumnWithValue<?> colValue : getColumnWithValues()) {
            select.column(colValue.getColumnName());
        }
        select.from(getTableName());
        select.addPart(SQLPart.WHERE).addColumnPart(null, "ID")
                        .addPart(SQLPart.EQUAL).addValuePart(getId());

        final Statement stmt = _con.createStatement();
        final ResultSet rs = stmt.executeQuery(select.getSQL());
        final ArrayListHandler handler = new ArrayListHandler(Context.getDbType().getRowProcessor());
        final List<Object[]> rows = handler.handle(rs);
        rs.close();
        stmt.close();
        if (rows.size() == 1) {
            final Object[] values = rows.get(0);
            int idx = 0;
            final Iterator<AbstractColumnWithValue<?>> colIter = getColumnWithValues().iterator();
            while (colIter.hasNext()) {
                final AbstractColumnWithValue<?> colValue = colIter.next();
                final Object dbValue = values[idx];
                final Object newValue = colValue.getValue();
                if (dbValue instanceof Long) {
                    if (newValue instanceof Long) {
                        if (dbValue.equals(newValue)) {
                            colIter.remove();
                        }
                    } else if (newValue instanceof Integer) {
                        if (dbValue.equals(new Long((Integer) newValue))) {
                            colIter.remove();
                        }
                    }
                } else if (dbValue instanceof BigDecimal) {
                    if (newValue instanceof BigDecimal) {
                        if (((BigDecimal) dbValue).compareTo((BigDecimal) newValue) == 0) {
                            colIter.remove();
                        }
                    } else if (newValue instanceof Long) {
                        if (((BigDecimal) dbValue).compareTo(BigDecimal.valueOf((Long) newValue)) == 0) {
                            colIter.remove();
                        }
                    } else if (newValue instanceof Integer) {
                        if (((BigDecimal) dbValue).compareTo(BigDecimal.valueOf(new Long((Integer) newValue))) == 0) {
                            colIter.remove();
                        }
                    }
                } else if (dbValue instanceof Timestamp) {
                    if (newValue instanceof Timestamp) {
                        if (((Timestamp) dbValue).equals((Timestamp) newValue)) {
                            colIter.remove();
                        }
                    }
                } else if (dbValue instanceof Boolean) {
                    if (newValue instanceof Boolean) {
                        if (((Boolean) dbValue).equals(newValue)) {
                            colIter.remove();
                        }
                    }
                } else if (dbValue instanceof String) {
                    if (newValue instanceof String) {
                        if (((String) dbValue).trim().equals(((String) newValue).trim())) {
                            colIter.remove();
                        }
                    }
                }
                idx++;
            }
        }
        return !getColumnWithValues().isEmpty();
    }

    /**
     * Getter method for the instance variable {@link #id}.
     *
     * @return value of instance variable {@link #id}
     */
    public long getId()
    {
        return this.id;
    }
}
