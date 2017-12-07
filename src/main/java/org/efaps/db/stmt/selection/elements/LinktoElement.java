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

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.SQLTable;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.db.wrapper.TableIndexer.Tableidx;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;

/**
 * The Class LinktoElement.
 */
public class LinktoElement
    extends AbstractElement<LinktoElement>
{

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
    public LinktoElement setAttribute(final Attribute _attribute)
    {
        this.attribute = _attribute;
        setDBTable(this.attribute.getTable());
        return this;
    }

    @Override
    public LinktoElement getThis()
    {
        return this;
    }

    @Override
    public void append2SQLSelect(final SQLSelect _sqlSelect)
        throws CacheReloadException
    {
        if (getTable() instanceof SQLTable) {
            final String tableName = ((SQLTable) getTable()).getSqlTable();
            final String key;
            if (getPrevious() != null && getPrevious() instanceof LinktoElement) {
                key = ((SQLTable) ((LinktoElement) getPrevious()).getTable()).getSqlTable() + "--" + tableName;
            } else {
                key = tableName;
            }
            if (_sqlSelect.getFromTables().isEmpty()) {
                final Tableidx tableidx = _sqlSelect.getIndexer().getTableIdx(tableName, tableName);
                if (tableidx.isCreated()) {
                    _sqlSelect.from(tableidx.getTable(), tableidx.getIdx());
                }
            }

            final Tableidx tableidx = _sqlSelect.getIndexer().getTableIdx(tableName, key);

            final Attribute joinAttr = this.attribute.getLink().getAttribute("ID");
            final String joinTableName = joinAttr.getTable().getSqlTable();
            final Tableidx joinTableidx = _sqlSelect.getIndexer().getTableIdx(joinTableName, tableName + "--"
                            + joinTableName);

            if (joinTableidx.isCreated()) {
                _sqlSelect.leftJoin(joinTableName, joinTableidx.getIdx(), "ID",
                                tableidx.getIdx(), this.attribute.getSqlColNames().get(0));
            }
        }
    }

    @Override
    public Object getObject(final Object[] _row)
        throws EFapsException
    {
        return getNext().getObject(_row);
    }
}
