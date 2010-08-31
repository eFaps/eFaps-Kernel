/*
 * Copyright 2003 - 2010 The eFaps Team
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
import java.sql.SQLException;
import java.sql.Statement;

import org.efaps.db.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * An easy wrapper for a SQL delete statement. To initialize this class use
 * {@link org.efaps.db.databases.AbstractDatabase#newInsert(String, String, boolean)}
 * to get a database specific insert.
 * </p>
 *
 * <p>
 * <b>Example:</b><br/>
 *
 * <pre>
 *
 * SQLInsert insert = Context.getDbType().newDelete(&quot;MYTABLE&quot;, &quot;ID&quot;);
 * </pre>
 *
 * </p>
 *
 * @see org.efaps.db.databases.AbstractDatabase#newInsert(String, String,
 *      boolean)
 * @author The eFaps Team
 * @version $Id$
 */
public class SQLDelete
{

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(SQLDelete.class);

    /**
     * List of deletets that will be executed.
     */
    private final DeleteDefintion[] definitions;


    /**
     * @param _deleteDefintions defintion of a delete
     */
    public SQLDelete(final DeleteDefintion... _deleteDefintions)
    {
        this.definitions = _deleteDefintions;
    }

    /**
     * Getter method for the instance variable {@link #definitions}.
     *
     * @return value of instance variable {@link #definitions}
     */
    protected DeleteDefintion[] getDefinitions()
    {
        return this.definitions;
    }

    /**
     * @param _con Connection the delete will be executed in
     * @throws SQLException on error during deletion
     */
    public void execute(final Connection _con)
        throws SQLException
    {
        final Statement stmt = _con.createStatement();

        for (final DeleteDefintion def : getDefinitions()) {
            final StringBuilder cmd = new StringBuilder();
            cmd.append(Context.getDbType().getSQLPart(SQLPart.DELETE)).append(" ")
                .append(Context.getDbType().getSQLPart(SQLPart.FROM)).append(" ")
                .append(Context.getDbType().getTableQuote())
                .append(def.getTablename())
                .append(Context.getDbType().getTableQuote()).append(" ")
                .append(Context.getDbType().getSQLPart(SQLPart.WHERE)).append(" ")
                .append(Context.getDbType().getColumnQuote())
                .append(def.getIdColumn())
                .append(Context.getDbType().getColumnQuote())
                .append(Context.getDbType().getSQLPart(SQLPart.EQUAL))
                .append(def.getId());
            stmt.addBatch(cmd.toString());
            if (SQLDelete.LOG.isDebugEnabled()) {
                SQLDelete.LOG.debug(cmd.toString());
            }
        }

        try  {
            final int[] rows =  stmt.executeBatch();
            for (final int row : rows) {
                if (Statement.EXECUTE_FAILED == row) {
                    throw new SQLException("Deletion of the '" + row + "' object was not executed successfully.");
                }
            }
        } finally  {
            stmt.close();
        }
    }


    /**
     * Defintion of a Delete.
     */
    public static class DeleteDefintion
    {

        /**
         * Name of the tabel to be deleet from.
         */
        private final String tablename;

        /**
         * Name of the ID column.
         */
        private final String idColumn;

        /**
         * Id to be deleted.
         */
        private final Long id;

        /**
         * @param _tableName array with name of the tables
         * @param _idColumn array with name of the id column
         * @param _id array with ids to be deleted
         */
        public DeleteDefintion(final String _tableName,
                               final String _idColumn,
                               final Long _id)
        {
            this.tablename = _tableName;
            this.idColumn = _idColumn;
            this.id = _id;
        }

        /**
         * Getter method for the instance variable {@link #tablename}.
         *
         * @return value of instance variable {@link #tablename}
         */
        public String getTablename()
        {
            return this.tablename;
        }

        /**
         * Getter method for the instance variable {@link #idColumn}.
         *
         * @return value of instance variable {@link #idColumn}
         */
        public String getIdColumn()
        {
            return this.idColumn;
        }

        /**
         * Getter method for the instance variable {@link #id}.
         *
         * @return value of instance variable {@link #id}
         */
        public Long getId()
        {
            return this.id;
        }
    }

}
