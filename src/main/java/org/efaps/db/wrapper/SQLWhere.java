/*
 * Copyright 2003 - 2018 The eFaps Team
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
    private final List<Criteria> criterias = new ArrayList<>();

    /** The started. */
    private boolean started;

    /**
     * Adds the criteria.
     *
     * @param _idx the idx
     * @param _sqlColNames the sql col names
     * @param _comparison the comparison
     * @param _values the values
     * @param _escape the escape
     */
    public void addCriteria(final int _idx, final ArrayList<String> _sqlColNames, final Comparison _comparison,
                            final String[] _values, final boolean _escape, final Connection connection)
    {
        this.criterias.add(new Criteria(_idx, _sqlColNames, _comparison, _values, _escape));
    }

    public boolean isStarted()
    {
        return this.started;
    }

    public void setStarted(final boolean _started)
    {
        this.started = _started;
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

        for (final Criteria criteria : this.criterias) {
            for (final String colName : criteria.colNames) {
                new SQLSelect.Column(_tablePrefix, criteria.tableIndex, colName).appendSQL(_cmd);
                new SQLSelect.SQLSelectPart(SQLPart.SPACE).appendSQL(_cmd);
                switch (criteria.comparison) {
                    case EQUAL:
                        new SQLSelect.SQLSelectPart(SQLPart.EQUAL).appendSQL(_cmd);
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
                new SQLSelect.SQLSelectPart(SQLPart.SPACE).appendSQL(_cmd);
                if (criteria.values.length > 1) {
                    new SQLSelect.SQLSelectPart(SQLPart.PARENTHESIS_OPEN).appendSQL(_cmd);
                }
                boolean first = true;
                for (final String value : criteria.values) {
                    if (first) {
                        first = false;
                    } else {
                        new SQLSelect.SQLSelectPart(SQLPart.COMMA).appendSQL(_cmd);
                    }
                    if (criteria.escape) {
                        new EscapedValue(value).appendSQL(_cmd);
                    } else {
                        new SQLSelect.Value(value).appendSQL(_cmd);
                    }
                }
                if (criteria.values.length > 1) {
                    new SQLSelect.SQLSelectPart(SQLPart.PARENTHESIS_CLOSE).appendSQL(_cmd);
                }
            }
        }
    }

    public static class Criteria
    {

        private final int tableIndex;
        private final ArrayList<String> colNames;
        private final Comparison comparison;
        private final String[] values;
        private final boolean escape;

        public Criteria(final int _tableIndex,
                        final ArrayList<String> _sqlColNames,
                        final Comparison _comparison,
                        final String[] _values,
                        final boolean _escape)
        {
            this.tableIndex = _tableIndex;
            this.colNames = _sqlColNames;
            this.comparison = _comparison;
            this.values = _values;
            this.escape = _escape;
        }

    }
}
