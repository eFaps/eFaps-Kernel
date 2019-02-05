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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.efaps.db.wrapper.SQLSelect.EscapedValue;
import org.efaps.db.wrapper.SQLSelect.SQLSelectPart;
import org.efaps.eql2.Comparison;
import org.efaps.eql2.Connection;

/**
 * The Class SQLWhere.
 */
public class SQLWhere
{

    /** The criterias. */
    private final List<Section> sections = new ArrayList<>();

    /** The started. */
    private boolean started;

    /** The sql select. */
    private SQLSelect sqlSelect;

    /**
     * Adds the criteria.
     *
     * @param _idx the idx
     * @param _sqlColNames the sql col names
     * @param _comparison the comparison
     * @param _values the values
     * @param _escape the escape
     */
    public Criteria addCriteria(final int _idx, final List<String> _sqlColNames, final Comparison _comparison,
                            final Set<String> _values, final boolean _escape, final Connection _connection)
    {
        final Criteria criteria = new Criteria()
                        .tableIndex(_idx)
                        .colNames(_sqlColNames)
                        .comparison(_comparison)
                        .values(_values)
                        .escape(_escape)
                        .connection(_connection);
        this.sections.add(criteria);
        return criteria;
    }

    public Criteria addCriteria(final int _idx, final String _sqlColName, final Comparison _comparison,
                            final String _value, final Connection _connection)
    {
        return addCriteria(_idx, Collections.singletonList(_sqlColName), _comparison, Collections.singleton(_value),
                        false, _connection);
    }

    public boolean isStarted()
    {
        return this.started;
    }

    public void setStarted(final boolean _started)
    {
        this.started = _started;
    }

    public SQLWhere select(final SQLSelect _sqlSelect) {
        this.sqlSelect = _sqlSelect;
        return this;
    }

    /**
     * Gets the sql select.
     *
     * @return the sql select
     */
    public SQLSelect getSqlSelect()
    {
        return this.sqlSelect;
    }

    public SQLWhere section(final Section _section) {
        this.sections.add(_section);
        return this;
    }

    /**
     * Append SQL.
     *
     * @param _tablePrefix the table prefix
     * @param _cmd the cmd
     */
    protected void appendSQL(final String _tablePrefix,
                             final StringBuilder _cmd)
    {
        if (isStarted()) {
            new SQLSelectPart(SQLPart.AND).appendSQL(_cmd);
            new SQLSelectPart(SQLPart.SPACE).appendSQL(_cmd);
        } else {
            new SQLSelectPart(SQLPart.WHERE).appendSQL(_cmd);
            new SQLSelectPart(SQLPart.SPACE).appendSQL(_cmd);
        }
        addSectionsSQL(_tablePrefix, _cmd, this.sections);
    }

    protected void addSectionsSQL(final String _tablePrefix,
                                  final StringBuilder _cmd,
                                  final List<Section> sections) {
        boolean first = true;
        for (final Section section : sections) {
            if (first) {
                first = false;
            } else {
                switch (section.getConnection()) {
                    case AND:
                        new SQLSelectPart(SQLPart.SPACE).appendSQL(_cmd);
                        new SQLSelect.SQLSelectPart(SQLPart.AND).appendSQL(_cmd);
                        new SQLSelectPart(SQLPart.SPACE).appendSQL(_cmd);
                        break;
                    case OR:
                        new SQLSelectPart(SQLPart.SPACE).appendSQL(_cmd);
                        new SQLSelect.SQLSelectPart(SQLPart.OR).appendSQL(_cmd);
                        new SQLSelectPart(SQLPart.SPACE).appendSQL(_cmd);
                        break;
                    default:
                        break;
                }
            }
            if (section instanceof Group) {
                final Group group = (Group) section;
                new SQLSelect.SQLSelectPart(SQLPart.PARENTHESIS_OPEN).appendSQL(_cmd);
                addSectionsSQL(_tablePrefix, _cmd, group);
                new SQLSelect.SQLSelectPart(SQLPart.PARENTHESIS_CLOSE).appendSQL(_cmd);
            } else {
                final Criteria criteria = (Criteria) section;
                for (final String colName : criteria.colNames) {
                    new SQLSelect.Column(_tablePrefix, criteria.tableIndex, colName).appendSQL(_cmd);
                    new SQLSelect.SQLSelectPart(SQLPart.SPACE).appendSQL(_cmd);
                    switch (criteria.comparison) {
                        case EQUAL:
                            if (criteria.values == null) {
                                new SQLSelect.SQLSelectPart(SQLPart.IS).appendSQL(_cmd);
                                new SQLSelect.SQLSelectPart(SQLPart.NULL).appendSQL(_cmd);
                            } else {
                                new SQLSelect.SQLSelectPart(SQLPart.EQUAL).appendSQL(_cmd);
                            }
                            break;
                        case LIKE:
                            new SQLSelect.SQLSelectPart(SQLPart.LIKE).appendSQL(_cmd);
                            break;
                        case LESS:
                            new SQLSelect.SQLSelectPart(SQLPart.LESS).appendSQL(_cmd);
                            break;
                        case LESSEQ:
                            new SQLSelect.SQLSelectPart(SQLPart.LESS).appendSQL(_cmd);
                            new SQLSelect.SQLSelectPart(SQLPart.EQUAL).appendSQL(_cmd);
                            break;
                        case GREATER:
                            new SQLSelect.SQLSelectPart(SQLPart.GREATER).appendSQL(_cmd);
                            break;
                        case GREATEREQ:
                            new SQLSelect.SQLSelectPart(SQLPart.GREATER).appendSQL(_cmd);
                            new SQLSelect.SQLSelectPart(SQLPart.EQUAL).appendSQL(_cmd);
                            break;
                        case UNEQUAL:
                            new SQLSelect.SQLSelectPart(SQLPart.UNEQUAL).appendSQL(_cmd);
                            break;
                        case IN:
                            new SQLSelect.SQLSelectPart(SQLPart.IN).appendSQL(_cmd);
                            break;
                        case NOTIN:
                            new SQLSelect.SQLSelectPart(SQLPart.NOT).appendSQL(_cmd);
                            new SQLSelect.SQLSelectPart(SQLPart.SPACE).appendSQL(_cmd);
                            new SQLSelect.SQLSelectPart(SQLPart.IN).appendSQL(_cmd);
                            break;
                        default:
                            break;
                    }
                    if (criteria.values != null) {
                        new SQLSelect.SQLSelectPart(SQLPart.SPACE).appendSQL(_cmd);
                        if (criteria.values.size() > 1) {
                            new SQLSelect.SQLSelectPart(SQLPart.PARENTHESIS_OPEN).appendSQL(_cmd);
                        }
                        boolean firstValue = true;
                        for (final String value : criteria.values) {
                            if (firstValue) {
                                firstValue = false;
                            } else {
                                new SQLSelect.SQLSelectPart(SQLPart.COMMA).appendSQL(_cmd);
                            }
                            if (criteria.escape) {
                                new EscapedValue(value).appendSQL(_cmd);
                            } else {
                                new SQLSelect.Value(value).appendSQL(_cmd);
                            }
                        }
                        if (criteria.values.size() > 1) {
                            new SQLSelect.SQLSelectPart(SQLPart.PARENTHESIS_CLOSE).appendSQL(_cmd);
                        }
                    }
                }
            }
        }
    }

    public interface Section {
        Connection getConnection();
    }

    public static class Group
        extends ArrayList<Section>
        implements Section
    {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        private Connection connection;

        @Override
        public Connection getConnection()
        {
            return this.connection;
        }

        public Group setConnection(final Connection _connection)
        {
            this.connection = _connection;
            return this;
        }
    }

    public static class Criteria
        implements Section
    {

        private int tableIndex;
        private List<String> colNames;
        private Comparison comparison;
        private Set<String> values;
        private boolean escape;
        private Connection connection;

        public Criteria values(final Set<String> _values)
        {
            this.values = _values;
            return this;
        }

        public Criteria value(final String _value)
        {
            if (this.values == null) {
                this.values = new HashSet<>();
            }
            this.values.add(_value);
            return this;
        }

        @Override
        public Connection getConnection()
        {
            return this.connection;
        }

        public int getTableIndex()
        {
            return this.tableIndex;
        }

        public List<String> getColNames()
        {
            return this.colNames;
        }

        public Comparison getComparison()
        {
            return this.comparison;
        }

        public Set<String> getValues()
        {
            return this.values;
        }

        public boolean isEscape()
        {
            return this.escape;
        }

        public Criteria tableIndex(final int _tableIndex)
        {
            this.tableIndex = _tableIndex;
            return this;
        }

        public Criteria colNames(final List<String> _colNames)
        {
            this.colNames = _colNames;
            return this;
        }

        public Criteria colName(final String _colName)
        {
            if (this.colNames == null) {
                this.colNames = new ArrayList<>();
            }
            this.colNames.add(_colName);
            return this;
        }

        public Criteria comparison(final Comparison _comparison)
        {
            this.comparison = _comparison;
            return this;
        }

        public Criteria escape(final boolean _escape)
        {
            this.escape = _escape;
            return this;
        }

        public Criteria connection(final Connection _connection)
        {
            this.connection = _connection;
            return this;
        }
    }
}
