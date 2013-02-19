/*
 * Copyright 2003 - 2013 The eFaps Team
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

import org.efaps.admin.datamodel.SQLTable;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.wrapper.SQLSelect;

/**
 * Select Part that connects a child tablt ot his parent table.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class ChildTableSelectPart
    extends AbstractSelectPart
{

    /**
     * Type this SelectPart belongs to.
     */
    private final Type type;

    /**
     * ChildTable of the main table of the {@link #type}.
     */
    private final SQLTable table;

    /**
     * @param _type type this Type this SelectPart belongs to.
     * @param _table childtable this SelectPart belongs to
     */
    public ChildTableSelectPart(final Type _type,
                                final SQLTable _table)
    {
        this.type = _type;
        this.table = _table;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Type getType()
    {
        return this.type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int join(final OneSelect _oneSelect,
                    final SQLSelect _select,
                    final int _relIndex)
    {
        Integer ret;
        final String tableName = this.table.getSqlTable();
        ret = _oneSelect.getTableIndex(tableName, "ID", _relIndex);
        if (ret == null) {
            ret = _oneSelect.getNewTableIndex(tableName, "ID", _relIndex);
            _select.leftJoin(tableName, ret, "ID", _relIndex, "ID");
        }
        return ret;
    }
}
