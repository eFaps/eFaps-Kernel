/*
 * Copyright 2003 - 2019 The eFaps Team
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.efaps.db.Context;
import org.efaps.db.search.section.AbstractQSection;
import org.efaps.util.EFapsException;

/**
 * An easy wrapper for a SQL select statement.
 *
 * @author The eFaps Team
 */
public class SQLSelect
{

    /**
     * Parts that will be added to the created SQL Statement.
     */
    private final List<SQLSelectPart> parts = new ArrayList<>();

    /**
     * Selected columns.
     *
     * @see #column(String)
     */
    private final List<Column> columns = new ArrayList<>();

    /**
     * Selected tables.
     *
     * @see #from(String)
     */
    private final List<FromTable> fromTables = new ArrayList<>();

    /**
     * Must the select be distinct.
     *
     */
    private boolean distinct = false;

    /** The table prefix. */
    private final String tablePrefix;

    /** The indexer. */
    private final TableIndexer indexer = new TableIndexer();

    /** The where. */
    private SQLWhere where;

    /**
     * Instantiates a new SQL select.
     */
    public SQLSelect()
    {
        this("T");
    }

    /**
     * Instantiates a new SQL select.
     *
     * @param _prefix the _prefix
     */
    public SQLSelect(final String _prefix)
    {
        this.tablePrefix = _prefix;
    }

    /**
     * Gets the indexer.
     *
     * @return the indexer
     */
    public TableIndexer getIndexer()
    {
        return this.indexer;
    }

    /**
     * Appends a selected column.
     *
     * @param _name name of the column
     * @return this SQL select statement
     * @see #columns
     */
    public SQLSelect column(final String _name)
    {
        this.columns.add(new Column(this.tablePrefix, null, _name));
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
        this.columns.add(new Column(this.tablePrefix, _tableIndex, _columnName));
        return this;
    }

    /**
     * Column index.
     *
     * @param _tableIndex the table index
     * @param _columnName the column name
     * @return the int
     */
    public int columnIndex(final int _tableIndex, final String _columnName)
    {
        final Optional<Column> colOpt = getColumns().stream()
                        .filter(column -> column.tableIndex == _tableIndex && column.columnName.equals(_columnName))
                        .findFirst();
        final int ret;
        if (colOpt.isPresent()) {
            ret = getColumns().indexOf(colOpt.get());
        } else {
            this.columns.add(new Column(this.tablePrefix, _tableIndex, _columnName));
            ret = getColumnIdx();
        }
        return ret;
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
     * Gets the column idx.
     *
     * @return the column idx
     */
    public int getColumnIdx()
    {
        return getColumns().size() - 1;
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
        this.fromTables.add(new FromTable(this.tablePrefix, _name, null));
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
        this.fromTables.add(new FromTable(this.tablePrefix, _tableName, _tableIndex));
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
        this.fromTables.add(new FromTableLeftJoin(this.tablePrefix, _tableName, _tableIndex, _columnName,
                                                  _joinTableIndex, _joinColumnName));
        return this;
    }

    /**
     *
     * @param _tableName name of the SQL table
     * @param _tableIndex index of the table used within the SQL select
     *            statement
     * @param _columnNames names of the columns of table <code>_tableName</code>
     *            used for &quot;left join&quot;
     * @param _joinTableIndex index of the table from which is joined
     * @param _joinColumnNames names of the column of the table from which is
     *            joined
     * @return this SQL select statement instance
     */
    public SQLSelect leftJoin(final String _tableName,
                              final int _tableIndex,
                              final String[] _columnNames,
                              final int _joinTableIndex,
                              final String[] _joinColumnNames)
    {
        this.fromTables.add(new FromTableLeftJoin(this.tablePrefix, _tableName, _tableIndex, _columnNames,
                                                 _joinTableIndex, _joinColumnNames));
        return this;
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
    public SQLSelect innerJoin(final String _tableName,
                              final int _tableIndex,
                              final String _columnName,
                              final int _joinTableIndex,
                              final String _joinColumnName)
    {
        this.fromTables.add(new FromTableInnerJoin(this.tablePrefix, _tableName, _tableIndex, _columnName,
                                                 _joinTableIndex, _joinColumnName));
        return this;
    }

    /**
    *
    * @param _tableName name of the SQL table
    * @param _tableIndex index of the table used within the SQL select
    *            statement
    * @param _columnNames names of the columns of table <code>_tableName</code>
    *            used for &quot;left join&quot;
    * @param _joinTableIndex index of the table from which is joined
    * @param _joinColumnNames names of the columns of the table from which is
    *            joined
    * @return this SQL select statement instance
    */
    public SQLSelect innerJoin(final String _tableName,
                              final int _tableIndex,
                              final String[] _columnNames,
                              final int _joinTableIndex,
                              final String[] _joinColumnNames)
    {
        this.fromTables.add(new FromTableInnerJoin(this.tablePrefix, _tableName, _tableIndex, _columnNames,
                                                 _joinTableIndex, _joinColumnNames));
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
            .append(Context.getDbType().getSQLPart(SQLPart.SELECT)).append(" ");
        if (this.distinct) {
            cmd.append(Context.getDbType().getSQLPart(SQLPart.DISTINCT)).append(" ");
        }
        boolean first = true;
        for (final Column column : this.columns) {
            if (first) {
                first = false;
            } else {
                cmd.append(Context.getDbType().getSQLPart(SQLPart.COMMA));
            }
            column.appendSQL(cmd);
        }
        cmd.append(" ").append(Context.getDbType().getSQLPart(SQLPart.FROM)).append(" ");
        first = true;
        for (final FromTable fromTable : this.fromTables) {
            fromTable.appendSQL(first, cmd);
            if (first) {
                first = false;
            }
        }
        cmd.append(" ");
        boolean whereAdded = false;
        for (final SQLSelectPart part : this.parts) {
            part.appendSQL(cmd);
            cmd.append(" ");
            whereAdded = whereAdded || !whereAdded && part.sqlpart.equals(SQLPart.WHERE);
        }

        if (this.where != null) {
            this.where.setStarted(whereAdded);
            this.where.appendSQL(this.tablePrefix, cmd);
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
        this.parts.add(new Column(this.tablePrefix, _tableIndex, _columnName));
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
        this.parts.add(new FromTable(this.tablePrefix, _tableName, _tableIndex));
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

    /**
     * Add a timestamp value to the select.
     * @param _isoDateTime String to be casted to a timestamp
     * @return this
     */
    public SQLSelect addTimestampValue(final String _isoDateTime)
    {
        this.parts.add(new Value(Context.getDbType().getTimestampValue(_isoDateTime)));
        return this;
    }

    /**
     * Add a timestamp value to the select.
     * @param _value String to be casted to a Boolean
     * @return this
     */
    public SQLSelect addBooleanValue(final Boolean _value)
    {
        this.parts.add(new BooleanValue(_value));
        return this;
    }

    /**
     * @return get a new instance of this SQLSelect
     */
    public SQLSelect getCopy()
    {
        final SQLSelect select = new SQLSelect();
        select.columns.addAll(this.columns);
        select.parts.addAll(this.parts);
        select.fromTables.addAll(this.fromTables);
        select.distinct = this.distinct;
        return select;
    }

    /**
     * Gets the current.
     *
     * @return the current
     */
    public SQLSelectPart getCurrentPart()
    {
        return this.parts.isEmpty() ? null : this.parts.get(this.parts.size() - 1);
    }

    /**
     * Where.
     *
     * @param _where the where
     * @return the SQL select part
     */
    public SQLSelect where(final SQLWhere _where)
    {
        this.where = _where.select(this);
        return this;
    }

    /**
     * Gets the where.
     *
     * @return the where
     */
    public SQLWhere getWhere()
    {
        if (this.where == null) {
            this.where = new SQLWhere().select(this);
        }
        return this.where;
    }

    @Override
    public String toString()
    {
        return getSQL();
    }

    /**
     *
     */
    protected static class FromTable
        extends SQLSelectPart
    {

        /** SQL name of the table. */
        private final String tableName;

        /** Index of the table within the SQL select statement. */
        private final Integer tableIndex;

        /** The table prefix. */
        private final String tablePrefix;

        /**
         * Default constructor.
         *
         * @param _tablePrefix the _table prefix
         * @param _tableName name of the SQL table
         * @param _tableIndex index of the table
         */
        protected FromTable(final String _tablePrefix,
                            final String _tableName,
                            final Integer _tableIndex)
        {
            this.tablePrefix = _tablePrefix;
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

        @Override
        public void appendSQL(final StringBuilder _cmd)
        {
            this.appendSQL(true, _cmd);
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
                _cmd.append(Context.getDbType().getSQLPart(SQLPart.COMMA));
            }
            _cmd.append(Context.getDbType().getTableQuote())
                .append(this.tableName)
                .append(Context.getDbType().getTableQuote());
            if (this.tableIndex != null) {
                _cmd.append(" ").append(this.tablePrefix).append(this.tableIndex);
            }
        }


        /**
         * Getter method for the instance variable {@link #tablePrefix}.
         *
         * @return value of instance variable {@link #tablePrefix}
         */
        public String getTablePrefix()
        {
            return this.tablePrefix;
        }
    }

    /**
     *
     */
    protected static class FromTableLeftJoin
        extends SQLSelect.FromTable
    {

        /**
         * Name of the columns used for the &quot;left join&quot;.
         */
        private final String[] columnNames;

        /**
         * Index of the table from which is joined.
         */
        private final int joinTableIndex;

        /**
         * Name of the columns of the table from which is joined.
         */
        private final String[] joinColumnNames;

        /**
         * Instantiates a new from table left join.
         *
         * @param _tablePrefix the _table prefix
         * @param _tableName name of the SQL table
         * @param _tableIndex index of the table used within the SQL select
         *            statement
         * @param _columnName name of the column of table
         *            <code>_tableName</code> used for &quot;left join&quot;
         * @param _joinTableIndex index of the table from which is joined
         * @param _joinColumnName name of the column of the table from which is
         *            joined
         */
        protected FromTableLeftJoin(final String _tablePrefix,
                                    final String _tableName,
                                    final Integer _tableIndex,
                                    final String _columnName,
                                    final int _joinTableIndex,
                                    final String _joinColumnName)
        {
            super(_tablePrefix, _tableName, _tableIndex);
            this.columnNames = new String[] {_columnName};
            this.joinTableIndex = _joinTableIndex;
            this.joinColumnNames = new String[] {_joinColumnName};
        }

        /**
         * Constructor used to join on more than one column.
         *
         * @param _tablePrefix the _table prefix
         * @param _tableName        name of the SQL table
         * @param _tableIndex       index of the table used within the
         *                          SQL select statement
         * @param _columnNames      names of the columns of table <code>_tableName</code>
         *                          used for &quot;left join&quot;
         * @param _joinTableIndex   index of the table from which is joined
         * @param _joinColumnNames  names of the columns of the table from
         *                          which is joined
         */
        private FromTableLeftJoin(final String _tablePrefix,
                                  final String _tableName,
                                  final Integer _tableIndex,
                                  final String[] _columnNames,
                                  final int _joinTableIndex,
                                  final String[] _joinColumnNames)
        {
            super(_tablePrefix, _tableName, _tableIndex);
            this.columnNames = _columnNames;
            this.joinTableIndex = _joinTableIndex;
            this.joinColumnNames = _joinColumnNames;
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

            for (int i = 0; i < this.columnNames.length; i++) {
                if (i == 0) {
                    _cmd.append(Context.getDbType().getSQLPart(getJoin()))
                        .append(" ").append(Context.getDbType().getSQLPart(SQLPart.JOIN)).append(" ")
                        .append(Context.getDbType().getTableQuote())
                        .append(getTableName())
                        .append(Context.getDbType().getTableQuote())
                        .append(" ").append(getTablePrefix()).append(getTableIndex()).append(" ")
                        .append(Context.getDbType().getSQLPart(SQLPart.ON));
                } else {
                    _cmd.append(" ").append(Context.getDbType().getSQLPart(SQLPart.AND)).append(" ");
                }
                _cmd.append(" ").append(getTablePrefix()).append(this.joinTableIndex).append('.')
                    .append(Context.getDbType().getColumnQuote())
                    .append(this.joinColumnNames[i])
                    .append(Context.getDbType().getColumnQuote())
                    .append(Context.getDbType().getSQLPart(SQLPart.EQUAL))
                    .append(getTablePrefix()).append(getTableIndex()).append('.')
                    .append(Context.getDbType().getColumnQuote())
                    .append(this.columnNames[i])
                    .append(Context.getDbType().getColumnQuote());
            }
        }

        /**
         * @return the join for this class
         */
        protected SQLPart getJoin()
        {
            return SQLPart.LEFT;
        }
    }

    /**
     * Render an inner join.
     */
    protected static class FromTableInnerJoin
        extends SQLSelect.FromTableLeftJoin
    {

        /**
         * Instantiates a new from table inner join.
         *
         * @param _tablePrefix the _table prefix
         * @param _tableName name of the SQL table
         * @param _tableIndex index of the table used within the SQL select
         *            statement
         * @param _columnName name of the column of table
         *            <code>_tableName</code> used for &quot;left join&quot;
         * @param _joinTableIndex index of the table from which is joined
         * @param _joinColumnName name of the column of the table from which is
         *            joined
         */
        protected FromTableInnerJoin(final String _tablePrefix,
                                     final String _tableName,
                                     final Integer _tableIndex,
                                     final String _columnName,
                                     final int _joinTableIndex,
                                     final String _joinColumnName)
        {
            super(_tablePrefix, _tableName, _tableIndex, _columnName, _joinTableIndex, _joinColumnName);
        }

        /**
         * Instantiates a new from table inner join.
         *
         * @param _tablePrefix the _table prefix
         * @param _tableName        name of the SQL table
         * @param _tableIndex       index of the table used within the SQL select statement
         * @param _columnNames      name of the column of table <code>_tableName</code> used for &quot;left join&quot;
         * @param _joinTableIndex   index of the table from which is joined
         * @param _joinColumnNames  name of the column of the table from which is joined
         */
        private FromTableInnerJoin(final String _tablePrefix,
                                   final String _tableName,
                                   final Integer _tableIndex,
                                   final String[] _columnNames,
                                   final int _joinTableIndex,
                                   final String[] _joinColumnNames)
        {
            super(_tablePrefix, _tableName, _tableIndex, _columnNames, _joinTableIndex, _joinColumnNames);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected SQLPart getJoin()
        {
            return SQLPart.INNER;
        }
    }

    /**
     * Nested Select.
     */
    protected static class NestedSelect
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
    public static class Value
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
    public static class EscapedValue
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
     * Value to be escaped.
     */
    protected static class BooleanValue
        extends SQLSelectPart
    {

        /**
         * Value.
         */
        private final Boolean value;

        /**
         * @param _value Value
         */
        public BooleanValue(final Boolean _value)
        {
            this.value = _value;
        }

        @Override
        public void appendSQL(final StringBuilder _cmd)
        {
            _cmd.append(Context.getDbType().getBooleanValue(this.value));
        }

        @Override
        public String toString()
        {
            final String ret = Context.getDbType().getBooleanValue(this.value).toString();
            return ret;
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
    public static class Column
        extends SQLSelect.SQLSelectPart
    {
        /**
         * Index of the table in the select statement where this column is
         * defined.
         */
        private final Integer tableIndex;

        /** SQL name of the column. */
        private final String columnName;

        /** The table prefix. */
        private final String tablePrefix;

        /**
         * Default constructor.
         *
         * @param _tablePrefix the _table prefix
         * @param _tableIndex related index of the table
         * @param _columnName SQL name of the column
         */
        protected Column(final String _tablePrefix,
                         final Integer _tableIndex,
                         final String _columnName)
        {
            this.tablePrefix = _tablePrefix;
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
                _cmd.append(this.tablePrefix).append(this.tableIndex).append(".");
            }
            _cmd.append(Context.getDbType().getColumnQuote())
                            .append(this.columnName)
                            .append(Context.getDbType().getColumnQuote());
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
