/*
 * Copyright 2003 - 2017 The eFaps Team
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

import org.apache.commons.lang3.math.NumberUtils;
import org.efaps.admin.datamodel.SQLTable;
import org.efaps.admin.datamodel.Status;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.db.wrapper.TableIndexer.TableIdx;
import org.efaps.util.EFapsException;

/**
 * The Class InstanceElement.
 */
public class StatusElement
    extends AbstractDataElement<StatusElement>
{

    /** The type id. */
    private final Long typeId;

    /** The col idxs. */
    private int colIdx = -1;

    /**
     * Instantiates a new instance element.
     *
     * @param _type the type
     * @throws EFapsException
     */
    public StatusElement(final Type _type)
        throws EFapsException
    {
        if (!_type.isCheckStatus()) {
            throw new EFapsException(StatusElement.class, "Type has not Status", _type);
        }
        setDBTable(_type.getMainTable());
        this.typeId = _type.getId();
    }

    @Override
    public StatusElement getThis()
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

            final String colName = Type.get(this.typeId).getStatusAttribute().getSqlColNames().get(0);
            this.colIdx = _sqlSelect.columnIndex(tableIdx.getIdx(), colName);
        }
    }

    @Override
    public Object getObject(final Object[] _row)
        throws EFapsException
    {
        final Long statusId = getValue(_row[this.colIdx]);
        return Status.get(statusId);
    }

    /**
     * Gets the value.
     *
     * @param _object the object
     * @return the value
     */
    private Long getValue(final Object _object)
    {
        final Long ret;
        if (_object == null) {
            ret = null;
        } else if (_object instanceof Number) {
            ret = ((Number) _object).longValue();
        } else {
            ret = NumberUtils.toLong(_object.toString());
        }
        return ret;
    }
}
