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

import org.efaps.admin.datamodel.SQLTable;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.db.wrapper.TableIndexer.TableIdx;
import org.efaps.util.EFapsException;

public abstract class AbstractAttributeElement<T>
    extends AbstractDataElement<T>
{

    protected TableIdx getTableIdx(final SQLSelect _sqlSelect)
        throws EFapsException
    {
        TableIdx tableIdx = null;
        if (getTable() instanceof SQLTable) {
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
                tableIdx = _sqlSelect.getIndexer().getTableIdx(((SQLTable) getTable()).getSqlTable(), mainTable
                                .getSqlTable(), "ID");
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
        }
        return tableIdx;
    }

    protected Object callAuxillary(final Object _object)
        throws EFapsException
    {
        final Object ret;
        if (getNext() != null && getNext() instanceof IAuxillary) {
            ret = getNext().getObject(new Object[] { _object });
        } else {
            ret = _object;
        }
        return ret;
    }
}
