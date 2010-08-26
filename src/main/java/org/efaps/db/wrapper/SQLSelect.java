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

import java.util.ArrayList;
import java.util.List;

import org.efaps.db.Context;
import org.efaps.db.search.section.AbstractQSection;
import org.efaps.util.EFapsException;

/**
 * An easy wrapper for a SQL select statement.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class SQLSelect
{
    /**
     * Simple SQLpart.
     */
    public enum SQLPart {
        /** and. */
        AND("and"),
        /** asc. */
        ASC("asc"),
        /** ,. */
        COMMA(","),
        /** distinct. */
        DISTINCT("distinct"),
        /** desc. */
        DESC("desc"),
        /** from. */
        FROM("from"),
        /** =. */
        EQUAL("="),
        /** >. */
        GREATER(">"),
        /** FALSE. */
        FALSE("FALSE"),
        /** in. */
        IN("in"),
        /** is. */
        IS("is"),
        /** left. */
        JOIN("join"),
        /** left. */
        LEFT("left"),
        /** <. */
        LESS("<"),
        /** like. */
        LIKE("like"),
        /** limit. */
        LIMIT("limit"),
        /** null. */
        NULL("null"),
        /** not. */
        NOT("not"),
        /** on. */
        ON("on"),
        /** or. */
        OR("or"),
        /** order by. */
        ORDERBY("order by"),
        /** ). */
        PARENTHESIS_CLOSE(")"),
        /** (. */
        PARENTHESIS_OPEN("("),
        /** select. */
        SELECT("select"),
        /** .*/
        SPACE(" "),
        /** timestamp. */
        TIMESTAMP("timestamp"),
        /** TRUE. */
        TRUE("TRUE"),
        /** !=. */
        UNEQUAL("!="),
        /** upper. */
        UPPER("upper"),
        /** where. */
        WHERE("where");

        /**
         * Default Value.
         */
        private final String defaultValue;

        /**
         * @param _default default value
         */
        private SQLPart(final String _default)
        {
            this.defaultValue = _default;
        }

        /**
         * Getter method for the instance variable {@link #defaultValue}.
         *
         * @return value of instance variable {@link #defaultValue}
         */
        public String getDefaultValue()
        {
            return this.defaultValue;
        }
    }

    /**
     * Parts that will be added to the created SQL Statement.
     */
    private final List<SQLSelectPart> parts = new ArrayList<SQLSelectPart>();

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
     * Must the select be distinct.
     *
     */
    private boolean distinct = false;

    /**
     * Appends a selected column.
     *
     * @param _name name of the column
     * @return this SQL select statement
     * @see #columns
     */
    public SQLSelect column(final String _name)
    {
        this.columns.add(new Column(null, _name));
        return this;
    }

    /**
     * Appends a selected column <code>_name</code> for given
     * <code>_tableIndex</code>.
     *
     * @param _tableIndex index of the table
     * @param _columnName name of the column
     * @return this SQL select statement
     * @see #columns
     */
    public SQLSelect column(final int _tableIndex,
                            final String _columnName)
    {
        this.columns.add(new Column(_tableIndex, _columnName));
        return this;
    }

    /**
     * Getter method for the instance variable {@link #columns}.
     *
     * @return value of instance variable {@link #columns}
     */
    public List<Column> getColumns()
    {
        return this.columns;
    }

    /**
     * Appends a table as from selected table.
     *
     * @param _name name of the table
     * @return this SQL select statement
     * @see #fromTables
     */
    public SQLSelect from(final String _name)
    {
        this.fromTables.add(new FromTable(_name, null));
        return this;
    }

    /**
     * Appends a table as from selected table.
     *
     * @param _tableName name of the SQL table
     * @param _tableIndex index of the table within the SQL statement
     * @return this SQL select statement
     * @see #fromTables
     */
    public SQLSelect from(final String _tableName,
                          final int _tableIndex)
    {
        this.fromTables.add(new FromTable(_tableName, _tableIndex));
        return this;
    }

    /**
     * Getter method for the instance variable {@link #fromTables}.
     *
     * @return value of instance variable {@link #fromTables}
     */
    public List<FromTable> getFromTables()
    {
        return this.fromTables;
    }

    /**
     *
     * @param _tableName name of the SQL table
     * @param _tableIndex index of the table used within the SQL select
     *            statement
     * @param _columnName name of the column of table <code>_tableName</code>
     *            used for &quot;left join&quot;
     * @param _joinTableIndex index of the table from which is joined
     * @param _joinColumnName name of the column of the table from which is
     *            joined
     * @return this SQL select statement instance
     */
    public SQLSelect leftJoin(final String _tableName,
                              final int _tableIndex,
                              final String _columnName,
                              final int _joinTableIndex,
                              final String _joinColumnName)
    {
        this.fromTables.add(new FromTableLeftJoin(_tableName, _tableIndex, _columnName,
                                                  _joinTableIndex, _joinColumnName));
        return this;
    }

    /**
     * Returns the depending SQL statement.
     *
     * @return SQL statement
     */
    public String getSQL()
    {
        final StringBuilder cmd = new StringBuilder().append(" ")
                        .append(Context.getDbType().getSQLPart(SQLSelect.SQLPart.SELECT)).append(" ");
        if (this.distinct) {
            cmd.append(Context.getDbType().getSQLPart(SQLSelect.SQLPart.DISTINCT)).append(" ");
        }
        boolean first = true;
        for (final Column column : this.columns) {
            if (first) {
                first = false;
            } else {
                cmd.append(Context.getDbType().getSQLPart(SQLSelect.SQLPart.COMMA));
            }
            column.appendSQL(cmd);
        }
        cmd.append(" ").append(Context.getDbType().getSQLPart(SQLSelect.SQLPart.FROM)).append(" ");
        first = true;
        for (final FromTable fromTable : this.fromTables) {
            fromTable.appendSQL(first, cmd);
            if (first) {
                first = false;
            }
        }
        cmd.append(" ");
        for (final SQLSelectPart part : this.parts) {
            part.appendSQL(cmd);
            cmd.append(" ");
        }
        return cmd.toString();
    }

    /**
     * Must this SQLSelect be distinct.
     *
     * @param _distinct distinct
     * @return this
     */
    public SQLSelect distinct(final boolean _distinct)
    {
        this.distinct = _distinct;
        return this;
    }

    /**
     * @param _section Sectin o to be added
     * @throws EFapsException on error
     * @return this
     */
    public SQLSelect addSection(final AbstractQSection _section)
        throws EFapsException
    {
        if (_section != null) {
            _section.appendSQL(this);
        }
        return this;
    }

    /**
     * @param _part Part to be added
     * @return this
     */
    public SQLSelect addPart(final SQLPart _part)
    {
        this.parts.add(new SQLSelectPart(_part));
        return this;
    }

    /**
     * Add a column as part.
     * @param _tableIndex index of the table
     * @param _columnName name of the column
     * @return this
     */
    public SQLSelect addColumnPart(final Integer _tableIndex,
                                   final String _columnName)
    {
        this.parts.add(new Column(_tableIndex, _columnName));
        return this;
    }

    /**
     * Add a table as part.
     * @param _tableName    name of the table
     * @param _tableIndex    index of the table
     * @return this
     */
    public SQLSelect addTablePart(final String _tableName,
                                  final Integer _tableIndex)
    {
        this.parts.add(new FromTable(_tableName, _tableIndex));
        return this;
    }

    /**
     * @param _char val;ue to be added as nested Select part
     * @return this
     */
    public SQLSelect addNestedSelectPart(final CharSequence _char)
    {
        this.parts.add(new NestedSelect(_char));
        return this;
    }

    /**
     * @param _value value to be added as part
     * @return this
     */
    public SQLSelect addValuePart(final Object _value)
    {
        this.parts.add(new Value(_value));
        return this;
    }

    /**
     * @param _value add the value that must be escaped
     * @return this
     */
    public SQLSelect addEscapedValuePart(final String _value)
    {
        this.parts.add(new EscapedValue(_value));
        return this;
    }

    @Override
    public String toString()
    {
        return getSQL();
    }

    /**
     *
     */
    private static class FromTable
        extends SQLSelectPart
    {

        /** SQL name of the table. */
        private final String tableName;

        /** Index of the table within the SQL select statement. */
        private final Integer tableIndex;

        /**
         * Default constructor.
         *
         * @param _tableName name of the SQL table
         * @param _tableIndex index of the table
         */
        FromTable(final String _tableName,
                  final Integer _tableIndex)
        {
            this.tableName = _tableName;
            this.tableIndex = _tableIndex;
        }

        /**
         * Returns the related {@link #tableName SQL table name} which is
         * represented by this class.
         *
         * @return name of the SQL table
         * @see #tableName
         */
        public String getTableName()
        {
            return this.tableName;
        }

        /**
         * Returns the related {@link #tableIndex table index} in the SQL select
         * statement.
         *
         * @return table index
         * @see #tableIndex
         */
        public Integer getTableIndex()
        {
            return this.tableIndex;
        }

        /**
         * Appends the {@link #tableName name} of this table depending on a
         * given {@link #tableIndex index} to the SQL select statement in
         * <code>_cmd</code>. If <code>_first</code> is <i>true</i> a comma ','
         * is defined in the front.
         *
         * @param _first <i>true</i> if first statement and a comma must be
         *            prefixed; otherwise <i>false</i>
         * @param _cmd string builder used to append SQL statement for this
         *            table
         */
        public void appendSQL(final boolean _first,
                              final StringBuilder _cmd)
        {
            if (!_first) {
                _cmd.append(Context.getDbType().getSQLPart(SQLSelect.SQLPart.COMMA));
            }
            _cmd.append(Context.getDbType().getTableQuote())
                .append(this.tableName)
                .append(Context.getDbType().getTableQuote());
            if (this.tableIndex != null) {
                _cmd.append(" T").append(this.tableIndex);
            }
        }
    }

    /**
     *
     */
    private static class FromTableLeftJoin
        extends SQLSelect.FromTable
    {

        /**
         * Name of the column used for the &quot;left join&quot;.
         */
        private final String columnName;

        /**
         * Index of the table from which is joined.
         */
        private final int joinTableIndex;

        /**
         * Name of the column of the table from which is joined.
         */
        private final String joinColumnName;

        /**
         *
         * @param _tableName name of the SQL table
         * @param _tableIndex index of the table used within the SQL select
         *            statement
         * @param _columnName name of the column of table
         *            <code>_tableName</code> used for &quot;left join&quot;
         * @param _joinTableIndex index of the table from which is joined
         * @param _joinColumnName name of the column of the table from which is
         *            joined
         */
        FromTableLeftJoin(final String _tableName,
                          final Integer _tableIndex,
                          final String _columnName,
                          final int _joinTableIndex,
                          final String _joinColumnName)
        {
            super(_tableName, _tableIndex);
            this.columnName = _columnName;
            this.joinTableIndex = _joinTableIndex;
            this.joinColumnName = _joinColumnName;
        }

        /**
         * Appends the SQL statement for this left join.
         *
         * @param _first <i>true</i> if first statement and a space must be
         *            prefixed; otherwise <i>false</i>
         * @param _cmd string builder used to append SQL statement for this left
         *            join
         */
        @Override
        public void appendSQL(final boolean _first,
                              final StringBuilder _cmd)
        {
            if (!_first) {
                _cmd.append(' ');
            }
            _cmd.append(Context.getDbType().getSQLPart(SQLSelect.SQLPart.LEFT))
                .append(" ").append(Context.getDbType().getSQLPart(SQLSelect.SQLPart.JOIN)).append(" ")
                .append(Context.getDbType().getTableQuote())
                .append(getTableName())
                .append(Context.getDbType().getTableQuote())
                .append(" T").append(getTableIndex()).append(" ")
                .append(Context.getDbType().getSQLPart(SQLSelect.SQLPart.ON))
                .append(" T").append(this.joinTableIndex).append('.')
                .append(Context.getDbType().getColumnQuote())
                .append(this.joinColumnName)
                .append(Context.getDbType().getColumnQuote())
                .append(Context.getDbType().getSQLPart(SQLSelect.SQLPart.EQUAL))
                .append("T").append(getTableIndex()).append('.')
                .append(Context.getDbType().getColumnQuote())
                .append(this.columnName)
                .append(Context.getDbType().getColumnQuote());
        }
    }

    /**
     * Nested Select.
     */
    private static class NestedSelect
        extends SQLSelectPart
    {
        /**
         * Value.
         */
        private final CharSequence value;

        /**
         * @param _value Value
         */
        public NestedSelect(final CharSequence _value)
        {
            this.value = _value;
        }

        @Override
        public void appendSQL(final StringBuilder _cmd)
        {
            _cmd.append(this.value);
        }
    }

    /**
     * Value .
     */
    private static class Value
        extends SQLSelectPart
    {

        /**
         * Value.
         */
        private final Object value;

        /**
         * @param _value Value
         */
        public Value(final Object _value)
        {
            this.value = _value;
        }

        @Override
        public void appendSQL(final StringBuilder _cmd)
        {
            _cmd.append(this.value);
        }

        @Override
        public String toString()
        {
            return this.value.toString();
        }
    }

    /**
     * Value to be escaped.
     */
    private static class EscapedValue
        extends SQLSelectPart
    {

        /**
         * Value.
         */
        private final String value;

        /**
         * @param _value Value
         */
        public EscapedValue(final String _value)
        {
            this.value = _value;
        }

        @Override
        public void appendSQL(final StringBuilder _cmd)
        {
            _cmd.append(Context.getDbType().escapeForWhere(this.value));
        }

        @Override
        public String toString()
        {
            return Context.getDbType().escapeForWhere(this.value);
        }
    }

    /**
     *
     */
    public static class SQLSelectPart
    {

        /**
         * Part.
         */
        private SQLPart sqlpart;

        /**
         * Constructor.
         * @param _part SQLPart
         */
        public SQLSelectPart(final SQLPart _part)
        {
            this.sqlpart = _part;
        }

        /**
         * Constructor.
         */
        protected SQLSelectPart()
        {
        }

        /**
         * @param _cmd StringBuilder to append to
         */
        public void appendSQL(final StringBuilder _cmd)
        {
            _cmd.append(Context.getDbType().getSQLPart(this.sqlpart));
        }

        @Override
        public String toString()
        {
            return Context.getDbType().getSQLPart(this.sqlpart);
        }
    }

    /**
     * Column.
     */
    private static class Column
        extends SQLSelect.SQLSelectPart
    {
        /**
         * Index of the table in the select statement where this column is
         * defined.
         */
        private final Integer tableIndex;

        /** SQL name of the column. */
        private final String columnName;

        /**
         * Default constructor.
         *
         * @param _tableIndex related index of the table
         * @param _columnName SQL name of the column
         */
        Column(final Integer _tableIndex,
               final String _columnName)
        {
            this.tableIndex = _tableIndex;
            this.columnName = _columnName;
        }

        /**
         *
         * @param _cmd string builder used to append SQL statement for this
         *            column
         */
        @Override
        public void appendSQL(final StringBuilder _cmd)
        {
            if (this.tableIndex != null) {
                _cmd.append("T").append(this.tableIndex).append(".");
            }
            _cmd.append(Context.getDbType().getTableQuote())
                            .append(this.columnName)
                            .append(Context.getDbType().getTableQuote());
        }

        @Override
        public String toString()
        {
            final StringBuilder cmd = new StringBuilder();
            appendSQL(cmd);
            return cmd.toString();
        }
    }
}
