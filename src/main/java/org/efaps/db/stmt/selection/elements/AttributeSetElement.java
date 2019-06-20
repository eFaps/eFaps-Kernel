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

package org.efaps.db.stmt.selection.elements;

import java.util.Set;

import org.efaps.admin.datamodel.AttributeSet;
import org.efaps.admin.datamodel.SQLTable;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.stmt.filter.TypeCriterion;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.db.wrapper.TableIndexer.TableIdx;
import org.efaps.util.EFapsException;

/**
 * The Class ClassElement.
 */
public class AttributeSetElement
    extends AbstractDataElement<AttributeSetElement>
    implements IJoinTableIdx, ISquash, ITypeCriterion
{
    /** The type. */
    private AttributeSet attributeSet;

    /** The type. */
    private Type type;

    public AttributeSet getAttributeSet()
    {
        return attributeSet;
    }

    public AttributeSetElement setAttributeSet(final AttributeSet _attributeSet)
    {
        attributeSet = _attributeSet;
        setDBTable(attributeSet.getMainTable());
        return this;
    }

    public Type getType()
    {
        return type;
    }

    public AttributeSetElement setType(final Type _type)
    {
        type = _type;
        return this;
    }

    @Override
    public TableIdx getJoinTableIdx(final SQLSelect _sqlSelect)
        throws EFapsException
    {
        final String tableName = ((SQLTable) getTable()).getSqlTable();
        final String joinTableName = attributeSet.getMainTable().getSqlTable();
        return _sqlSelect.getIndexer().getTableIdx(joinTableName, tableName, attributeSet.getName());
    }

    @Override
    public void append2SQLSelect(final SQLSelect _sqlSelect)
        throws EFapsException
    {
        if (getTable() instanceof SQLTable) {
            appendBaseTable(_sqlSelect, getType().getMainTable());

            final TableIdx joinTableidx = getJoinTableIdx(_sqlSelect);
            if (joinTableidx.isCreated()) {
                final TableIdx tableidx;
                if (getPrevious() != null && getPrevious() instanceof IJoinTableIdx) {
                    tableidx = ((IJoinTableIdx) getPrevious()).getJoinTableIdx(_sqlSelect);
                } else {
                    tableidx = _sqlSelect.getIndexer().getTableIdx(((SQLTable) getTable()).getSqlTable());
                }
                final String joinTableName = attributeSet.getMainTable().getSqlTable();
                final String linktoColName = type.getAttribute("ID").getSqlColNames().get(0);
                _sqlSelect.leftJoin(joinTableName, joinTableidx.getIdx(), attributeSet.getSqlColNames().get(0),
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
        return super.getPath() + "->ATTRIBUTESET-" + attributeSet.getName();
    }

    @Override
    public AttributeSetElement getThis()
    {
        return this;
    }

    @Override
    public void add2TypeCriteria(final SQLSelect _sqlSelect, final Set<TypeCriterion> _typeCriterias)
        throws EFapsException
    {
        if (((SQLTable) getTable()).getSqlColType() != null) {
            final TableIdx tableidx = getJoinTableIdx(_sqlSelect);
            _typeCriterias.add(TypeCriterion.of(tableidx, ((SQLTable) getTable()).getSqlColType(), getAttributeSet().getId()));
        }
    }
}
