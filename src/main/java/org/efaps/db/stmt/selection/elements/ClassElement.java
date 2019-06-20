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

package org.efaps.db.stmt.selection.elements;

import java.util.Set;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Classification;
import org.efaps.admin.datamodel.SQLTable;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.stmt.filter.TypeCriterion;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.db.wrapper.TableIndexer.TableIdx;
import org.efaps.util.EFapsException;

/**
 * The Class ClassElement.
 */
public class ClassElement
    extends AbstractDataElement<ClassElement>
    implements IJoinTableIdx, ITypeCriterion
{
    /** The type. */
    private Classification classification;

    /** The type. */
    private Type type;

    /** The added. */
    private final boolean added = false;

    public Classification getClassification()
    {
        return classification;
    }

    public ClassElement setClassification(final Classification _classification)
    {
        classification = _classification;
        return this;
    }

    public Type getType()
    {
        return type;
    }

    public ClassElement setType(final Type _type)
    {
        type = _type;
        setDBTable(type.getMainTable());
        return this;
    }

    @Override
    public TableIdx getJoinTableIdx(final SQLSelect _sqlSelect)
        throws EFapsException
    {
        final String tableName = ((SQLTable) getTable()).getSqlTable();
        final String joinTableName = classification.getMainTable().getSqlTable();
        return _sqlSelect.getIndexer().getTableIdx(joinTableName, tableName, "ID");
    }

    @Override
    public void append2SQLSelect(final SQLSelect _sqlSelect)
        throws EFapsException
    {
        if (getTable() instanceof SQLTable) {
            appendBaseTable(_sqlSelect, (SQLTable) getTable());

            final TableIdx joinTableidx = getJoinTableIdx(_sqlSelect);
            if (joinTableidx.isCreated()) {
                final TableIdx tableidx;
                if (getPrevious() != null && getPrevious() instanceof IJoinTableIdx) {
                    tableidx = ((IJoinTableIdx) getPrevious()).getJoinTableIdx(_sqlSelect);
                } else {
                    tableidx = _sqlSelect.getIndexer().getTableIdx(((SQLTable) getTable()).getSqlTable());
                }
                final Attribute joinAttr = classification.getAttribute(classification.getLinkAttributeName());
                final String joinTableName = joinAttr.getTable().getSqlTable();
                final String linktoColName = type.getAttribute("ID").getSqlColNames().get(0);
                _sqlSelect.leftJoin(joinTableName, joinTableidx.getIdx(), joinAttr.getSqlColNames().get(0),
                                tableidx.getIdx(), linktoColName);
            }
        }
    }

    /**
     * Append base table if not added already from other element.
     *
     * @param _sqlSelect the sql select
     * @param _sqlTable the sql table
     */
    protected void appendBaseTable(final SQLSelect _sqlSelect, final SQLTable _sqlTable)
    {
        if (_sqlSelect.getFromTables().isEmpty()) {
            final TableIdx tableidx = _sqlSelect.getIndexer().getTableIdx(_sqlTable.getSqlTable());
            if (tableidx.isCreated()) {
                _sqlSelect.from(tableidx.getTable(), tableidx.getIdx());
            }
        }
    }

    @Override
    public Object getObject(final Object[] _row)
        throws EFapsException
    {
        return getNext().getObject(_row);
    }

    @Override
    public String getPath()
    {
        return super.getPath() + "->CLASS-" + classification.getName();
    }

    @Override
    public ClassElement getThis()
    {
        return this;
    }

    @Override
    public void add2TypeCriteria(final SQLSelect _sqlSelect, final Set<TypeCriterion> _typeCriterias)
        throws EFapsException
    {
        if (getClassification().getMainTable().getSqlColType() != null) {
            final TableIdx tableidx = getJoinTableIdx(_sqlSelect);
            _typeCriterias.add(TypeCriterion.of(tableidx, ((SQLTable) getTable()).getSqlColType(), getClassification().getId()));
        }
    }
}
