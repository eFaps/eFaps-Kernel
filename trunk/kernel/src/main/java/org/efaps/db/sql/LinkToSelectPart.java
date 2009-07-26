/*
 * Copyright 2003 - 2009 The eFaps Team
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

package org.efaps.db.sql;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Type;

/**
 * Select Part for <code>linkto[ATTRIBUTENAME]</code>.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class LinkToSelectPart implements ISelectPart
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
    public LinkToSelectPart(final String _attrName, final Type _type)
    {
        this.attrName = _attrName;
        this.type = _type;
    }

    /**
     * {@inheritDoc}
     */
    public int join(final OneSelect _oneSelect, final StringBuilder _fromBldr, final int _relIndex)
    {
        final Attribute attr = this.type.getAttribute(this.attrName);
        Integer ret;
        final String tableName = attr.getLink().getMainTable().getSqlTable();
        ret = _oneSelect.getTableIndex(tableName, _relIndex);
        if (ret == null) {
            ret = _oneSelect.getNewTableIndex(tableName, _relIndex);
            _fromBldr.append(" left join ").append(tableName).append(" T").append(ret)
                .append(" on T").append(_relIndex).append(".").append(attr.getSqlColNames().get(0))
                .append("=T").append(ret).append(".ID");
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