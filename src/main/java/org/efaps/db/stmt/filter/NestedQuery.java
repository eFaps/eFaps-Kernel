/*
 * Copyright © 2003 - 2024 The eFaps Team (-)
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
 */
package org.efaps.db.stmt.filter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.SQLTable;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.db.wrapper.TableIndexer.TableIdx;
import org.efaps.eql2.Comparison;
import org.efaps.eql2.Connection;
import org.efaps.eql2.IAttributeSelectElement;
import org.efaps.eql2.INestedQuery;
import org.efaps.eql2.ISelect;
import org.efaps.eql2.ISelectElement;
import org.efaps.eql2.IWhereElement;
import org.efaps.util.EFapsException;
import org.efaps.util.TypeUtil;

public class NestedQuery
{

    private final IWhereElement whereElement;

    public NestedQuery(final IWhereElement _whereElement)
    {
        whereElement = _whereElement;
    }

    public void append2SQLSelect(final List<Type> _parentTypes, final SQLSelect _parentSqlSelect)
        throws EFapsException
    {
        final INestedQuery nestedQuery = whereElement.getNestedQuery();
        final List<Type> types = TypeUtil.getTypes(nestedQuery.getTypes());
        final SQLSelect sqlSelect = new SQLSelect("N");

        final Set<TypeCriterion> typeCriteria = new HashSet<>();
        for (final Type type : types) {
            final String tableName = type.getMainTable().getSqlTable();
            final TableIdx tableIdx = sqlSelect.getIndexer().getTableIdx(tableName);
            if (tableIdx.isCreated()) {
                sqlSelect.from(tableIdx.getTable(), tableIdx.getIdx());
            }
            if (type.getMainTable().getSqlColType() != null) {
                typeCriteria.add(TypeCriterion.of(tableIdx, type.getMainTable().getSqlColType(), type.getId()));
            }
        }

        boolean added = false;
        if (nestedQuery.getSelection() != null) {
            for (final ISelect select : nestedQuery.getSelection().getSelects()) {
                for (final ISelectElement element : select.getElements()) {
                    if (element instanceof IAttributeSelectElement) {
                        for (final Type type : types) {
                            final String attrName = ((IAttributeSelectElement) element).getName();
                            final Attribute attr = type.getAttribute(attrName);
                            if (attr != null) {
                                final SQLTable table = attr.getTable();
                                final String tableName = table.getSqlTable();
                                final TableIdx tableidx = sqlSelect.getIndexer().getTableIdx(tableName);
                                sqlSelect.column(tableidx.getIdx(), attr.getSqlColNames().get(0));
                                added = true;
                                break;
                            }
                        }
                    }
                }
            }
        }
        if (!added) {
            final SQLTable table = types.get(0).getMainTable();
            final String tableName = table.getSqlTable();
            final TableIdx tableidx = sqlSelect.getIndexer().getTableIdx(tableName);
            sqlSelect.column(tableidx.getIdx(), "ID");
        }

        final Filter filter = Filter.get(nestedQuery.getWhere(), types.toArray(new Type[types.size()]));
        filter.append2SQLSelect(sqlSelect, typeCriteria);

        // add if to the parent part
        String attrName = null;
        if (whereElement.getAttribute() != null) {
            attrName = whereElement.getAttribute();
        } else {
            for (final ISelectElement ele : whereElement.getSelect().getElements()) {
               if (ele instanceof IAttributeSelectElement) {
                    attrName = ((IAttributeSelectElement) ele).getName();
                }
            }
        }
        for (final Type type : _parentTypes) {
            final Attribute attr = type.getAttribute(attrName);
            if (attr != null) {
                final SQLTable table = attr.getTable();
                final String tableName = table.getSqlTable();
                final TableIdx tableidx = _parentSqlSelect.getIndexer().getTableIdx(tableName);
                _parentSqlSelect.getWhere().addCriteria(tableidx.getIdx(), attr.getSqlColNames().get(0),Comparison.IN,
                                sqlSelect.toString(), Connection.NONE);
            }
            break;
        }
    }
}
