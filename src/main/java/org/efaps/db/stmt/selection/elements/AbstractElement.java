/*
 * Copyright 2003 - 2016 The eFaps Team
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

import org.efaps.admin.datamodel.DBTable;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.util.EFapsException;

/**
 * The Class AbstractElement.
 *
 * @author The eFaps Team
 */
public abstract class AbstractElement<T>
{

    /** The table. */
    private DBTable table;

    private AbstractElement<?> previous;

    private AbstractElement<?> next;

    public AbstractElement<?> getPrevious()
    {
        return this.previous;
    }

    public void setPrevious(final AbstractElement<?> _previous)
    {
        this.previous = _previous;
        _previous.setNext(this);
    }

    public AbstractElement<?> getNext()
    {
        return this.next;
    }

    public void setNext(final AbstractElement<?> next)
    {
        this.next = next;
    }

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
        return this.getThis();
    }

    /**
     * Gets the this.
     *
     * @return the this
     */
    public abstract T getThis();

    public abstract void append2SQLSelect(final SQLSelect _sqlSelect)
        throws EFapsException;

    public abstract Object getObject(Object[] _row)throws EFapsException;
}
