/*
 * Copyright 2003 - 2010 The eFaps Team
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

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.wrapper.SQLSelect;

/**
 * Select Part for <code>linkto[ATTRIBUTENAME]</code>.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class LinkToSelectPart
    implements ISelectPart
{
    /**
     * Name of the Attribute the link to is based on.
     */
    private final String attrName;

    /**
     * Type the {@link #attrName} belongs to.
     */
    private final Type type;

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
    public int join(final OneSelect _oneSelect,
                    final SQLSelect _select,
                    final int _relIndex)
    {
        // it must be evaluated if the attribute that is used as the base for the linkto is inside a child table
        final Attribute attr = this.type.getAttribute(this.attrName);
        Integer relIndex = _relIndex;
        if (attr != null && !attr.getTable().equals(this.type.getMainTable())) {
            final String childTableName = attr.getTable().getSqlTable();
            relIndex = _oneSelect.getTableIndex(childTableName, "ID", _relIndex);
            if (relIndex == null) {
                relIndex = _oneSelect.getNewTableIndex(childTableName, "ID", _relIndex);
                _select.leftJoin(childTableName, relIndex, "ID", _relIndex, "ID");
            }
        }
        Integer ret;
        final String tableName = attr.getLink().getMainTable().getSqlTable();
        final String column = attr.getSqlColNames().get(0);
        ret = _oneSelect.getTableIndex(tableName, column, relIndex);
        if (ret == null) {
            ret = _oneSelect.getNewTableIndex(tableName, column, relIndex);
            _select.leftJoin(tableName, ret, "ID", relIndex, column);
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    public Type getType()
    {
        return this.type.getAttribute(this.attrName).getLink();
    }
}