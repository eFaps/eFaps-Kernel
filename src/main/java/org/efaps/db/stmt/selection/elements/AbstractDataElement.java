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
import org.efaps.admin.datamodel.DBTable;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.db.wrapper.SQLWhere;
import org.efaps.util.EFapsException;

/**
 * The Class AbstractElement.
 *
 * @author The eFaps Team
 * @param <T> the generic type
 */
public abstract class AbstractDataElement<T>
    extends AbstractElement<T>
{

    /** The table. */
    private DBTable table;

    /**
     * Gets the table.
     *
     * @return the table
     */
    public DBTable getTable()
    {
        return this.table;
    }

    /**
     * Sets the DB table.
     *
     * @param _table the table
     * @return the t
     */
    public T setDBTable(final DBTable _table)
    {
        this.table = _table;
        return getThis();
    }

    protected Long getLongValue(final Object _object)
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

    /**
     * Append two SQL select.
     *
     * @param _sqlSelect the sql select
     * @throws EFapsException the eFaps exception
     */
    public abstract void append2SQLSelect(SQLSelect _sqlSelect)
        throws EFapsException;

    public abstract void append2SQLWhere(SQLWhere _sqlWhere)
                    throws EFapsException;
}
