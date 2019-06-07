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

import org.efaps.db.wrapper.SQLSelect.Column;
import org.efaps.db.wrapper.SQLSelect.SQLSelectPart;

public class SQLOrder
{
    private final List<Element> elements = new ArrayList<>();

    private final SQLSelect sqlSelect;

    protected SQLOrder(final SQLSelect _sqlSelect) {
        sqlSelect = _sqlSelect;
    }

    public SQLSelect getSqlSelect()
    {
        return sqlSelect;
    }

    public void addElement(final int _orderSequence,
                           final int _tableIndex,
                           final List<String> _sqlColNames,
                           final boolean _desc)
    {
        final Element element = new Element()
                        .tableIndex(_tableIndex)
                        .colNames(_sqlColNames)
                        .desc(_desc);
        if (_orderSequence > elements.size() - 1) {
            elements.add(element);
        } else {
            elements.add(_orderSequence, element);
        }
    }

    public void appendSQL(final String _tablePrefix,
                          final StringBuilder _cmd)
    {
        if (!elements.isEmpty()) {
            new SQLSelectPart(SQLPart.SPACE).appendSQL(_cmd);
            new SQLSelectPart(SQLPart.ORDERBY).appendSQL(_cmd);
            new SQLSelectPart(SQLPart.SPACE).appendSQL(_cmd);
        }
        boolean first = true;
        for (final Element element : elements) {
            if (first) {
                first = false;
            } else {
                new SQLSelectPart(SQLPart.COMMA).appendSQL(_cmd);
                new SQLSelectPart(SQLPart.SPACE).appendSQL(_cmd);
            }
            for (final String colName : element.getColNames()) {
                new Column(_tablePrefix, element.getTableIndex(), colName).appendSQL(_cmd);
                if (element.isDesc()) {
                    new SQLSelectPart(SQLPart.SPACE).appendSQL(_cmd);
                    new SQLSelectPart(SQLPart.DESC).appendSQL(_cmd);
                }
            }
       }
    }

    public static class Element {
        private int tableIndex;
        private List<String> colNames;
        private boolean desc;

        public int getTableIndex()
        {
            return tableIndex;
        }

        public Element tableIndex(final int tableIndex)
        {
            this.tableIndex = tableIndex;
            return this;
        }

        public List<String> getColNames()
        {
            return colNames;
        }

        public Element colNames(final List<String> colNames)
        {
            this.colNames = colNames;
            return this;
        }

        public boolean isDesc()
        {
            return desc;
        }

        public Element desc(final boolean desc)
        {
            this.desc = desc;
            return this;
        }
    }

}
