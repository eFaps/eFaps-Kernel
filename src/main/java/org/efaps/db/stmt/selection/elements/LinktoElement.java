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
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.db.wrapper.TableIndexer.TableIdx;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class LinktoElement.
 */
public class LinktoElement
    extends AbstractDataElement<LinktoElement>
    implements IJoinTableIdx
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
        throws EFapsException
    {
        if (getTable() instanceof SQLTable) {
            // evaluated if the attribute that is used as the base for the linkTo is inside a child table
            TableIdx childIdx = null;
            if (this.attribute != null && !getTable().equals(this.attribute.getParent().getMainTable())) {
                final TableIdx mainTableIdx = _sqlSelect.getIndexer().getTableIdx(this.attribute.getParent()
                                .getMainTable().getSqlTable());
                if (mainTableIdx.isCreated()) {
                    _sqlSelect.from(mainTableIdx.getTable(), mainTableIdx.getIdx());
                }
                childIdx = _sqlSelect.getIndexer().getTableIdx(((SQLTable) getTable()).getSqlTable(),
                                this.attribute.getParent().getMainTable().getSqlTable(), "ID");
                if (childIdx.isCreated()) {
                    _sqlSelect.leftJoin(childIdx.getTable(), childIdx.getIdx(), "ID", mainTableIdx.getIdx(), "ID");
                }
            } else if (_sqlSelect.getFromTables().isEmpty()) {
                final TableIdx tableidx = _sqlSelect.getIndexer().getTableIdx(((SQLTable) getTable()).getSqlTable());
                if (tableidx.isCreated()) {
                    _sqlSelect.from(tableidx.getTable(), tableidx.getIdx());
                }
            }

            final TableIdx joinTableidx = getJoinTableIdx(_sqlSelect);

            if (joinTableidx.isCreated()) {
                final TableIdx tableidx;
                if (getPrevious() != null && getPrevious() instanceof IJoinTableIdx) {
                    tableidx = ((IJoinTableIdx) getPrevious()).getJoinTableIdx(_sqlSelect);
                } else {
                    tableidx = childIdx == null ? _sqlSelect.getIndexer().getTableIdx(((SQLTable) getTable())
                                    .getSqlTable()) : childIdx;
                }
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
     * @param _sqlSelect the sql select
     * @return the join table idx
     * @throws EFapsException the e faps exception
     */
    @Override
    public TableIdx getJoinTableIdx(final SQLSelect _sqlSelect)
        throws EFapsException
    {
        final String linktoColName = this.attribute.getSqlColNames().get(0);
        final String tableName = ((SQLTable) getTable()).getSqlTable();
        final Attribute joinAttr = this.attribute.getLink().getAttribute("ID");
        final String joinTableName = joinAttr.getTable().getSqlTable();
        return _sqlSelect.getIndexer().getTableIdx(joinTableName, tableName, linktoColName);
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
