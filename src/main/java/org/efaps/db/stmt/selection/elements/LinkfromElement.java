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

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.SQLTable;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.db.wrapper.TableIndexer.TableIdx;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;

/**
 * The Class LinkfromElement.
 */
public class LinkfromElement
    extends AbstractElement<LinkfromElement>
    implements IJoinTableIdx
{
    /** The attribute. */
    private Attribute attribute;

    /** The type. */
    private Type startType;

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
     * @param _attribute the new attribute
     * @return the linkfrom element
     */
    public LinkfromElement setAttribute(final Attribute _attribute)
    {
        this.attribute = _attribute;
        setDBTable(this.attribute.getTable());
        return this;
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public Type getStartType()
    {
        return this.startType;
    }

    /**
     * Sets the start type.
     *
     * @param _startType the start type
     * @return the linkfrom element
     */
    public LinkfromElement setStartType(final Type _startType)
    {
        this.startType = _startType;
        return this;
    }

    @Override
    public LinkfromElement getThis()
    {
        return this;
    }

    @Override
    public Object getObject(final Object[] _row)
        throws EFapsException
    {
        return getNext().getObject(_row);
    }

    @Override
    public void append2SQLSelect(final SQLSelect _sqlSelect)
        throws EFapsException
    {
        if (getTable() instanceof SQLTable) {
            appendBaseTable(_sqlSelect);
            final TableIdx joinTableidx = getJoinTableIdx(_sqlSelect);
            if (joinTableidx.isCreated()) {
                final TableIdx tableidx;
                if (getPrevious() != null && getPrevious() instanceof IJoinTableIdx) {
                    tableidx = ((IJoinTableIdx) getPrevious()).getJoinTableIdx(_sqlSelect);
                } else {
                    tableidx = _sqlSelect.getIndexer().getTableIdx(this.startType.getMainTable().getSqlTable());
                }
                final String linktoColName = this.attribute.getSqlColNames().get(0);
                final String tableName = ((SQLTable) getTable()).getSqlTable();
                _sqlSelect.leftJoin(tableName, joinTableidx.getIdx(), linktoColName, tableidx.getIdx(), "ID");
            }
        }
    }

    /**
     * Gets the join table idx.
     *
     * @param _sqlSelect the sql select
     * @return the join table idx
     * @throws CacheReloadException the cache reload exception
     */
    @Override
    public TableIdx getJoinTableIdx(final SQLSelect _sqlSelect)
        throws CacheReloadException
    {
        final String tableName = ((SQLTable) getTable()).getSqlTable();
        return _sqlSelect.getIndexer().getTableIdx(tableName, this.attribute.getName(),
                        this.startType.getMainTable().getSqlTable());
    }

    /**
     * Append base table if not added already from other element.
     *
     * @param _sqlSelect the sql select
     */
    protected void appendBaseTable(final SQLSelect _sqlSelect)
    {
        if (_sqlSelect.getFromTables().isEmpty()) {
            final TableIdx tableidx = _sqlSelect.getIndexer().getTableIdx(this.startType.getMainTable().getSqlTable());
            if (tableidx.isCreated()) {
                _sqlSelect.from(tableidx.getTable(), tableidx.getIdx());
            }
        }
    }

    @Override
    public String getPath()
    {
        return super.getPath() + "<-" + getAttribute().getName() + ":" + getAttribute().getParentId();
    }
}