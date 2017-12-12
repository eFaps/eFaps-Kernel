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
import org.efaps.admin.datamodel.Type;
import org.efaps.db.Instance;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.db.wrapper.TableIndexer.Tableidx;
import org.efaps.util.EFapsException;

/**
 * The Class InstanceElement.
 */
public class InstanceElement
    extends AbstractElement<InstanceElement>
{

    /** The type id. */
    private final Long typeId;

    /** The col idxs. */
    private int idColIdxs = -1;

    /** The type col idxs. */
    private int typeColIdxs = -1;

    /**
     * Instantiates a new instance element.
     *
     * @param _type the type
     */
    public InstanceElement(final Type _type)
    {
        setDBTable(_type.getMainTable());
        this.typeId = _type.getId();
    }

    @Override
    public InstanceElement getThis()
    {
        return this;
    }

    @Override
    public void append2SQLSelect(final SQLSelect _sqlSelect)
        throws EFapsException
    {
        if (getTable() instanceof SQLTable) {
            final SQLTable table = (SQLTable) getTable();
            final String key;
            if (getPrevious() != null && getPrevious() instanceof LinktoElement) {
                key = ((SQLTable) ((LinktoElement) getPrevious()).getTable()).getSqlTable() + "--" + table
                                .getSqlTable();
            } else {
                key = table.getSqlTable();
            }
            final Tableidx tableidx = _sqlSelect.getIndexer().getTableIdx(table.getSqlTable(), key);
            this.idColIdxs = _sqlSelect.columnIndex(tableidx.getIdx(), table.getSqlColId());
            if (table.getSqlColType() != null) {
                this.typeColIdxs = _sqlSelect.columnIndex(tableidx.getIdx(), table.getSqlColType());
            }
            if (tableidx.isCreated()) {
                _sqlSelect.from(tableidx.getTable(), tableidx.getIdx());
            }
        }
    }

    @Override
    public Object getObject(final Object[] _row)
        throws EFapsException
    {
        final Long idObject = getValue(_row[this.idColIdxs]);
        final Type type;
        if (this.typeColIdxs > -1) {
            final Long typeIdTemp = getValue(_row[this.typeColIdxs]);
            type = Type.get(typeIdTemp);
        } else {
            type = Type.get(this.typeId);
        }
        return idObject == null ? null : Instance.get(type, idObject);
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
