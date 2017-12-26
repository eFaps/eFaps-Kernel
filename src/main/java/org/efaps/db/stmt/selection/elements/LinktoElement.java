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
import org.efaps.db.wrapper.TableIndexer;
import org.efaps.db.wrapper.TableIndexer.TableIdx;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class LinktoElement.
 */
public class LinktoElement
    extends AbstractElement<LinktoElement>
{
    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(LinktoElement.class);

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
            // evaluated if the attribute that is used as the base for the linkTo is inside a child table
            if (this.attribute != null && !getTable().equals(this.attribute.getParent().getMainTable())) {
                LOG.error("STILL MISSING");
            } else {
                appendBaseTable(_sqlSelect, (SQLTable) getTable());
            }

            final TableIdx joinTableidx = getJoinTableIdx(_sqlSelect.getIndexer());

            if (joinTableidx.isCreated()) {
                final TableIdx tableidx = _sqlSelect.getIndexer().getTableIdx(tableName);
                final Attribute joinAttr = this.attribute.getLink().getAttribute("ID");
                final String joinTableName = joinAttr.getTable().getSqlTable();
                final String linktoColName = this.attribute.getSqlColNames().get(0);
                _sqlSelect.leftJoin(joinTableName, joinTableidx.getIdx(), "ID", tableidx.getIdx(), linktoColName);
            }
        }
    }

    /**
     * Gets the join table idx.
     *
     * @param _tableIndexer the table indexer
     * @return the join table idx
     * @throws CacheReloadException the cache reload exception
     */
    protected TableIdx getJoinTableIdx(final TableIndexer _tableIndexer)
        throws CacheReloadException
    {
        final String linktoColName = this.attribute.getSqlColNames().get(0);
        final String tableName = ((SQLTable) getTable()).getSqlTable();
        final Attribute joinAttr = this.attribute.getLink().getAttribute("ID");
        final String joinTableName = joinAttr.getTable().getSqlTable();
        return _tableIndexer.getTableIdx(joinTableName, tableName, linktoColName);
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
        return super.getPath() + "->" + getAttribute().getName();
    }
}
