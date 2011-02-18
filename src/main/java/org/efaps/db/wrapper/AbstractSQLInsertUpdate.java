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

package org.efaps.db.wrapper;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.efaps.db.Context;

/**
 *
 * @param <STMT>    original SQL statement class
 * @author The eFaps Team
 * @version $Id$
 */
public abstract class AbstractSQLInsertUpdate<STMT extends AbstractSQLInsertUpdate<?>>
{
    /**
     * SQL table name to insert or update.
     *
     * @see #AbstractSQLInsertUpdate(String, String)
     */
    private final String tableName;

    /**
     * Name of the id column.
     *
     * @see #AbstractSQLInsertUpdate(String, String)
     */
    private final String idColumn;

    /**
     * Columns to insert or update.
     *
     * @see #columnWithSQLValue(String, String)
     */
    private final List<AbstractColumnWithValue<?>> columnWithValues = new ArrayList<AbstractColumnWithValue<?>>();

    /**
     * Columns to insert or update.
     *
     * @see #columnWithSQLValue(String, String)
     */
    private final List<ColumnWithSQLValue> columnWithSQLValues = new ArrayList<ColumnWithSQLValue>();

    /**
     * Initializes the {@link #tableName table name} to update.
     *
     * @param _tableName    name of the table to update
     * @param _idColumn     name of the id column
     */
    public AbstractSQLInsertUpdate(final String _tableName,
                                   final String _idColumn)
    {
        this.tableName = _tableName;
        this.idColumn = _idColumn;
    }

    /**
     * Returns the {@link #tableName table name} to insert / update.
     *
     * @return table name to insert / update
     * @see #tableName
     */
    protected final String getTableName()
    {
        return this.tableName;
    }

    /**
     * Returns the name of the {@link #idColumn id column}.
     *
     * @return name of the id column
     * @see #idColumn
     */
    protected final String getIdColumn()
    {
        return this.idColumn;
    }

    /**
     * Returns the {@link #columnWithValues column with values}.
     *
     * @return columns with values
     * @see #columnWithValues
     */
    protected final List<AbstractColumnWithValue<?>> getColumnWithValues()
    {
        return this.columnWithValues;
    }

    /**
     * Returns the {@link #columnWithSQLValues column with SQL values}.
     *
     * @return columns with SQL values
     * @see #columnWithSQLValues
     */
    protected final List<ColumnWithSQLValue> getColumnWithSQLValues()
    {
        return this.columnWithSQLValues;
    }

    /**
     * Adds a new column which will have the current time stamp to append.
     *
     * @param _columnName   name of column to append for which current time
     *                      stamp must be set
     * @return this SQL update instance
     */
    @SuppressWarnings("unchecked")
    public STMT columnWithCurrentTimestamp(final String _columnName)
    {
        this.columnWithSQLValues.add(
                new AbstractSQLInsertUpdate.ColumnWithSQLValue(_columnName,
                                                               Context.getDbType().getCurrentTimeStamp()));
        return (STMT) this;
    }

    /**
     * Defines a new column <code>_columnName</code> with {@link BigDecimal}
     * <code>_value</code> within this SQL insert / update statement.
     *
     * @param _columnName   name of the column
     * @param _value        value of the column
     * @return this SQL statement
     */
    @SuppressWarnings("unchecked")
    public STMT column(final String _columnName,
                       final BigDecimal _value)
    {
        this.columnWithValues.add(new AbstractSQLInsertUpdate.AbstractColumnWithValue<BigDecimal>(_columnName, _value) {
            @Override
            public void set(final int _index, final PreparedStatement _stmt)
                throws SQLException
            {
                if (getValue() == null)  {
                    _stmt.setNull(_index, Types.DECIMAL);
                } else  {
                    _stmt.setBigDecimal(_index, getValue());
                }
            }
        });
        return (STMT) this;
    }

    /**
     * Defines a new column <code>_columnName</code> with {@link Double}
     * <code>_value</code> within this SQL insert / update statement.
     *
     * @param _columnName   name of the column
     * @param _value        value of the column
     * @return this SQL statement
     */
    @SuppressWarnings("unchecked")
    public STMT column(final String _columnName,
                       final Double _value)
    {
        this.columnWithValues.add(new AbstractSQLInsertUpdate.AbstractColumnWithValue<Double>(_columnName, _value) {
            @Override
            public void set(final int _index, final PreparedStatement _stmt)
                throws SQLException
            {
                if (getValue() == null)  {
                    _stmt.setNull(_index, Types.DECIMAL);
                } else  {
                    _stmt.setDouble(_index, getValue());
                }
            }
        });
        return (STMT) this;
    }

    /**
     * Defines a new column <code>_columnName</code> with {@link String}
     * <code>_value</code> within this SQL insert / update statement.
     *
     * @param _columnName   name of the column
     * @param _value        value of the column
     * @return this SQL statement
     */
    @SuppressWarnings("unchecked")
    public STMT column(final String _columnName,
                       final String _value)
    {
        this.columnWithValues.add(new AbstractSQLInsertUpdate.AbstractColumnWithValue<String>(_columnName, _value) {
            @Override
            public void set(final int _index, final PreparedStatement _stmt)
                throws SQLException
            {
                if (getValue() == null)  {
                    _stmt.setNull(_index, Types.VARCHAR);
                } else  {
                    _stmt.setString(_index, getValue());
                }
            }
        });
        return (STMT) this;
    }

    /**
     * Defines a new column <code>_columnName</code> with {@link Timestamp}
     * <code>_value</code> within this SQL insert / update statement.
     *
     * @param _columnName   name of the column
     * @param _value        value of the column
     * @return this SQL statement
     */
    @SuppressWarnings("unchecked")
    public STMT column(final String _columnName,
                       final Timestamp _value)
    {
        this.columnWithValues.add(new AbstractSQLInsertUpdate.AbstractColumnWithValue<Timestamp>(_columnName, _value) {
            @Override
            public void set(final int _index, final PreparedStatement _stmt)
                throws SQLException
            {
                if (getValue() == null)  {
                    _stmt.setNull(_index, Types.TIMESTAMP);
                } else  {
                    _stmt.setTimestamp(_index, getValue());
                }
            }
        });
        return (STMT) this;
    }

    /**
     * Defines a new column <code>_columnName</code> with {@link Boolean}
     * <code>_value</code> within this SQL insert / update statement.
     *
     * @param _columnName   name of the column
     * @param _value        value of the column
     * @return this SQL statement
     */
    @SuppressWarnings("unchecked")
    public STMT column(final String _columnName,
                       final boolean _value)
    {
        this.columnWithValues.add(new AbstractSQLInsertUpdate.AbstractColumnWithValue<Boolean>(_columnName, _value) {
            @Override
            public void set(final int _index, final PreparedStatement _stmt)
                throws SQLException
            {
                if (getValue() == null)  {
                    _stmt.setNull(_index, Types.BOOLEAN);
                } else  {
                    _stmt.setBoolean(_index, getValue());
                }
            }
        });
        return (STMT) this;
    }

    /**
     * Defines a new column <code>_columnName</code> with {@link Long}
     * <code>_value</code> within this SQL insert / update statement.
     *
     * @param _columnName   name of the column
     * @param _value        value of the column
     * @return this SQL statement
     */
    @SuppressWarnings("unchecked")
    public STMT column(final String _columnName,
                       final Long _value)
    {
        this.columnWithValues.add(new AbstractSQLInsertUpdate.AbstractColumnWithValue<Long>(_columnName, _value) {
            @Override
            public void set(final int _index, final PreparedStatement _stmt)
                throws SQLException
            {
                if (getValue() == null)  {
                    _stmt.setNull(_index, Types.BIGINT);
                } else  {
                    _stmt.setLong(_index, getValue());
                }
            }
        });
        return (STMT) this;
    }

    /**
     * Defines a new column <code>_columnName</code> with {@link Long}
     * <code>_value</code> within this SQL insert / update statement.
     *
     * @param _columnName   name of the column
     * @param _value        value of the column
     * @return this SQL statement
     */
    @SuppressWarnings("unchecked")
    public STMT column(final String _columnName,
                       final Integer _value)
    {
        this.columnWithValues.add(new AbstractSQLInsertUpdate.AbstractColumnWithValue<Integer>(_columnName, _value) {
            @Override
            public void set(final int _index, final PreparedStatement _stmt)
                throws SQLException
            {
                if (getValue() == null)  {
                    _stmt.setNull(_index, Types.BIGINT);
                } else  {
                    _stmt.setInt(_index, getValue());
                }
            }
        });
        return (STMT) this;
    }

    /**
     * Abstract definition of a column.
     */
    protected abstract static class AbstractColumn
    {
        /**
         * Name of the SQL table column.
         */
        private final String columnName;

        /**
         * Initializes the {@link #columnName column name}.
         *
         * @param _columnName   name of the column
         */
        private AbstractColumn(final String _columnName)
        {
            this.columnName = _columnName;
        }

        /**
         * Returns the {@link #columnName column name}.
         *
         * @return column name
         * @see #columnName
         */
        public final String getColumnName()
        {
            return this.columnName;
        }
    }

    /**
     * Column with SQL value.
     */
    protected static final class ColumnWithSQLValue
        extends AbstractSQLInsertUpdate.AbstractColumn
    {
        /**
         * SQL value to set for the column.
         */
        private final String sqlValue;

        /**
         *
         * @param _columnName   column name
         * @param _sqlValue     SQL value
         */
        private ColumnWithSQLValue(final String _columnName,
                                   final String _sqlValue)
        {
            super(_columnName);
            this.sqlValue = _sqlValue;
        }

        /**
         * Returns related {@link #sqlValue SQL value}.
         *
         * @return SQL value
         * @see #sqlValue
         */
        public String getSqlValue()
        {
            return this.sqlValue;
        }
    }

    /**
     * Class holding the values depending on a column.
     *
     * @param <VALUE>   class of the value
     */
    protected abstract static class AbstractColumnWithValue<VALUE>
        extends AbstractSQLInsertUpdate.AbstractColumn
    {
        /**
         * SQL value to set for the column.
         */
        private final VALUE value;

        /**
         * Initializes this column depending on the <code>_columnName</code>
         * with <code>_value</code>.
         *
         * @param _columnName   column name
         * @param _value        value
         */
        private AbstractColumnWithValue(final String _columnName,
                                        final VALUE _value)
        {
            super(_columnName);
            this.value = _value;
        }

        /**
         * Returns the {@link #value} of this column.
         *
         * @return value of the column
         */
        public VALUE getValue()
        {
            return this.value;
        }

        /**
         *
         *
         * @param _index    index in the prepared statement
         * @param _stmt     prepared statement
         * @throws SQLException if value could not be set within the prepared
         *                      statement <code>_stmt</code>
         */
        public abstract void set(final int _index,
                                 final PreparedStatement _stmt)
            throws SQLException;
    }
}
