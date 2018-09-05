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

import java.util.Collections;

import org.apache.commons.lang3.ArrayUtils;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.SQLTable;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.db.wrapper.TableIndexer.TableIdx;
import org.efaps.util.EFapsException;

/**
 * The Class AttributeSelect.
 *
 * @author The eFaps Team
 */
public class AttributeElement
    extends AbstractDataElement<AttributeElement>
{

    /** The col idxs. */
    private int[] colIdxs;

    /** The attribute. */
    private Attribute attribute;

    /**
     * Gets the attribute.
     *
     * @return the attribute
     */
    public Attribute getAttribute()
    {
        return this.attribute;
    }

    /**
     * Sets the attribute.
     *
     * @param _attribute the attribute
     * @return the attribute element
     */
    public AttributeElement setAttribute(final Attribute _attribute)
    {
        this.attribute = _attribute;
        setDBTable(this.attribute.getTable());
        return this;
    }

    @Override
    public AttributeElement getThis()
    {
        return this;
    }

    @Override
    public void append2SQLSelect(final SQLSelect _sqlSelect)
        throws EFapsException
    {
        if (getTable() instanceof SQLTable) {
            final TableIdx tableIdx;
            final SQLTable mainTable = ((SQLTable) getTable()).getMainTable();
            if (mainTable != null) {
                final TableIdx mainTableIdx;
                if (getPrevious() != null && getPrevious() instanceof IJoinTableIdx) {
                    mainTableIdx = ((IJoinTableIdx) getPrevious()).getJoinTableIdx(_sqlSelect);
                } else {
                    mainTableIdx = _sqlSelect.getIndexer().getTableIdx(mainTable.getSqlTable());
                }
                if (mainTableIdx.isCreated()) {
                    _sqlSelect.from(mainTableIdx.getTable(), mainTableIdx.getIdx());
                }
                tableIdx = _sqlSelect.getIndexer().getTableIdx(((SQLTable) getTable()).getSqlTable(),
                                mainTable.getSqlTable(), "ID");
                if (tableIdx.isCreated()) {
                    _sqlSelect.leftJoin(tableIdx.getTable(), tableIdx.getIdx(), "ID", mainTableIdx.getIdx(), "ID");
                }
            } else {
                if (getPrevious() != null && getPrevious() instanceof IJoinTableIdx) {
                    tableIdx = ((IJoinTableIdx) getPrevious()).getJoinTableIdx(_sqlSelect);
                } else {
                    tableIdx = _sqlSelect.getIndexer().getTableIdx(((SQLTable) getTable()).getSqlTable());
                }
                if (tableIdx.isCreated()) {
                    _sqlSelect.from(tableIdx.getTable(), tableIdx.getIdx());
                }
            }

            for (final String colName : this.attribute.getSqlColNames()) {
                this.colIdxs = ArrayUtils.add(this.colIdxs, _sqlSelect.columnIndex(tableIdx.getIdx(), colName));
            }

            // in case of dependencies for the attribute they must be selected also
            for (final Attribute attr : this.attribute.getDependencies().values()) {
                for (final String colName : attr.getSqlColNames()) {
                    final int colidx = _sqlSelect.column(tableIdx.getIdx(), colName).getColumnIdx();
                    ArrayUtils.add(this.colIdxs, colidx);
                }
            }
        }
    }

    @Override
    public Object getObject(final Object[] _row)
        throws EFapsException
    {
        final Object ret;
        if (this.colIdxs.length == 1) {
            ret = _row[this.colIdxs[0]];
        } else {
            ret = new Object[this.colIdxs.length];
            for (int i = 0; i < this.colIdxs.length; i++) {
                ((Object[]) ret)[i] = this.colIdxs[i];
            }
        }
        return this.attribute.readDBValue(Collections.singletonList(ret));
    }
}
