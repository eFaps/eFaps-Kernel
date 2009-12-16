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

import java.util.ArrayList;
import java.util.List;

import org.efaps.db.Context;

/**
 *
 * @author The eFaps Team
 * @version $Id$
 * TODO: where clause
 * TODO: order
 * TODO: tables for columns
 */
public class SQLSelect
{
    /**
     * Selected columns.
     *
     * @see #column(String)
     */
    private final List<Column> columns = new ArrayList<Column>();

    /**
     * Selected tables.
     *
     * @see #from(String)
     */
    private final List<FromTable> fromTables = new ArrayList<FromTable>();

    /**
     * Appends a selected column.
     *
     * @param _name     name of the column
     * @return this SQL select statement
     * @see #columns
     */
    public SQLSelect column(final String _name)
    {
        this.columns.add(new Column(_name, null));
        return this;
    }

    /**
     * Appends a table as from selected table.
     *
     * @param _name     name of the table
     * @return this SQL select statement
     * @see #fromTables
     */
    public SQLSelect from(final String _name)
    {
        this.fromTables.add(new FromTable(_name, null));
        return this;
    }

    /**
     * Returns the depending SQL statement.
     *
     * @return SQL statement
     */
    public String getSQL()
    {
        final StringBuilder cmd = new StringBuilder()
                .append("select ");
        boolean first = true;
        for (final Column column : this.columns)  {
            if (first)  {
                first = false;
            } else  {
                cmd.append(',');
            }
            column.appendSQL(cmd);
        }
        cmd.append(" from ");
        first = true;
        for (final FromTable fromTable : this.fromTables)  {
            if (first)  {
                first = false;
            } else  {
                cmd.append(',');
            }
            fromTable.appendSQL(cmd);
        }

        return cmd.toString();
    }

    /**
     *
     */
    private static class FromTable
    {
        /** SQL name of the table. */
        private final String sqlName;

        /** Internal used name in the select statement. */
        private final String internalName;

        /**
         * Default constructor.
         *
         * @param _sqlName          SQL name
         * @param _internalName     internal name
         */
        FromTable(final String _sqlName,
                  final String _internalName)
        {
            this.sqlName = _sqlName;
            this.internalName = _internalName;
        }

        /**
         *
         * @param _cmd      string builder used to append SQL statement for
         *                  this table
         */
        public void appendSQL(final StringBuilder _cmd)
        {
            _cmd.append(Context.getDbType().getTableQuote())
                .append(this.sqlName)
                .append(Context.getDbType().getTableQuote());
            if (this.internalName != null)  {
                _cmd.append(' ').append(this.internalName);
            }
        }
    }

    /**
     *
     */
    private static class Column
    {
        /** SQL name of the column. */
        private final String sqlName;

        /** Internal used name in the select statement. */
        private final String internalName;

        /**
         * Default constructor.
         *
         * @param _sqlName          SQL name
         * @param _internalName     internal name
         */
        Column(final String _sqlName,
               final String _internalName)
        {
            this.sqlName = _sqlName;
            this.internalName = _internalName;
        }

        /**
         *
         * @param _cmd      string builder used to append SQL statement for
         *                  this column
         */
        public void appendSQL(final StringBuilder _cmd)
        {
            _cmd.append(Context.getDbType().getTableQuote())
                .append(this.sqlName)
                .append(Context.getDbType().getTableQuote());
            if (this.internalName != null)  {
                _cmd.append(' ').append(this.internalName);
            }
        }
    }
}
