/*
 * Copyright 2003 - 2014 The eFaps Team
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
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.db.print;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.Instance;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Select Part for <code>linkto[ATTRIBUTENAME]</code>.
 * After invoking <code>{@link #next()}</code> currentinstance contains
 * the instance it was linked to.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class LinkToSelectPart
    extends AbstractSelectPart
{
    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(OneSelect.class);

    /**
     * Name of the Attribute the link to is based on.
     */
    private final String attrName;

    /**
     * Type the {@link #attrName} belongs to.
     */
    private final Type type;

    /**
     * index of the id Column.
     */
    private int idColumnIndex;

    /**
     * index of the id Column.
     */
    private int typeColumnIndex = -1;

    /**
     * List of ids retrieved from the ResultSet returned
     * from the eFaps database. It represent one row in a result set.
     */
    private final List<Long> idList = new ArrayList<Long>();

    /**
     * List of typeIds retrieved from the ResultSet returned
     * from the eFaps database. It represent one row in a result set.
     */
    private final List<Long> typeIdList = new ArrayList<Long>();

    /**
     * Iterator for the ids.
     */
    private Iterator<Long> idIterator;

    /**
     * Iterator for the ypeIds.
     */
    private Iterator<Long> typeIdIterator;

    /**
     * The instance this Select is linked to.
     */
    private Instance currentInstance;

    /**
     * @param _attrName attribute name
     * @param _type     type
     */
    public LinkToSelectPart(final String _attrName,
                            final Type _type)
    {
        this.attrName = _attrName;
        this.type = _type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int join(final OneSelect _oneSelect,
                    final SQLSelect _select,
                    final int _relIndex)
        throws EFapsException
    {
        // it must be evaluated if the attribute that is used as the base for the linkto is inside a child table
        final Attribute attr = this.type.getAttribute(this.attrName);
        if (attr == null) {
            LinkToSelectPart.LOG.error("Could not find an Attribute with name '{}' for type:{}", this.attrName,
                            this.type);
            throw new EFapsException(LinkToSelectPart.class, "joinNoAttribute");
        }
        Integer relIndex = _relIndex;
        if (attr != null && !attr.getTable().equals(this.type.getMainTable())) {
            final String childTableName = attr.getTable().getSqlTable();
            relIndex = _oneSelect.getTableIndex(childTableName, "ID", _relIndex, null);
            if (relIndex == null) {
                relIndex = _oneSelect.getNewTableIndex(childTableName, "ID", _relIndex, null);
                _select.leftJoin(childTableName, relIndex, "ID", _relIndex, "ID");
            }
        }
        Integer ret;
        final String tableName = attr.getLink().getMainTable().getSqlTable();
        final String column = attr.getSqlColNames().get(0);
        ret = _oneSelect.getTableIndex(tableName, column, relIndex, null);
        if (ret == null) {
            ret = _oneSelect.getNewTableIndex(tableName, column, relIndex, null);
            _select.leftJoin(tableName, ret, "ID", relIndex, column);
        }
        _select.column(ret, "ID");
        this.idColumnIndex = _select.getColumns().size();
        // select the type column if it has one
        if (attr.getLink().getMainTable().getSqlColType() != null) {
            _select.column(ret, attr.getLink().getMainTable().getSqlColType());
            this.typeColumnIndex = _select.getColumns().size();
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Type getType()
    {
        final Attribute attr = this.type.getAttribute(this.attrName);
        if (attr == null) {
            LinkToSelectPart.LOG.error("Could not find an Attribute with name '{}' for type:{}", this.attrName,
                            this.type);
        }
        Type ret = null;
        try {
            ret = attr.getLink();
            if (ret == null) {
                LinkToSelectPart.LOG.error("No link for Attribute '{}'", attr);
            }
        } catch (final CacheReloadException e) {
            LinkToSelectPart.LOG.error("Could not get Link for Attribute '{}'", attr);
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addObject(final Object[] _row)
        throws SQLException
    {
        this.idList.add((Long) _row[this.idColumnIndex - 1]);
        if (this.typeColumnIndex > -1) {
            this.typeIdList.add((Long) _row[this.typeColumnIndex - 1]);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getObject()
    {
        return this.idList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void next()
        throws EFapsException
    {
        if (this.typeIdIterator == null) {
            this.typeIdIterator = this.typeIdList.iterator();
        }
        if (this.idIterator == null) {
            this.idIterator = this.idList.iterator();
        }
        if (this.idIterator.hasNext()) {
            final Long idTmp = this.idIterator.next();
            if (idTmp == null) {
                this.currentInstance = Instance.get("");
            } else {
                if (this.typeIdList.isEmpty()) {
                    this.currentInstance = Instance.get(this.type.getAttribute(this.attrName).getLink(), idTmp);
                } else {
                    final Long typeIdTmp = this.typeIdIterator.next();
                    if (typeIdTmp != null) {
                        this.currentInstance = Instance.get(Type.get(typeIdTmp), idTmp);
                    }
                }
            }
        }
    }

    /**
     * Getter method for the instance variable {@link #currentInstance}.
     *
     * @return value of instance variable {@link #currentInstance}
     */
    public Instance getCurrentInstance()
    {
        return this.currentInstance;
    }
}
