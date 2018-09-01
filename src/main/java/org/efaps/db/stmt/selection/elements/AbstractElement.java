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

import org.efaps.util.EFapsException;

public abstract class AbstractElement<T>
{
    /** The previous. */
    private AbstractElement<?> previous;

    /** The next. */
    private AbstractElement<?> next;

    /**
     * Gets the previous.
     *
     * @return the previous
     */
    public AbstractElement<?> getPrevious()
    {
        return this.previous;
    }

    /**
     * Sets the previous.
     *
     * @param _previous the new previous
     */
    public void setPrevious(final AbstractElement<?> _previous)
    {
        this.previous = _previous;
        _previous.setNext(this);
    }

    /**
     * Gets the next.
     *
     * @return the next
     */
    public AbstractElement<?> getNext()
    {
        return this.next;
    }

    /**
     * Sets the next.
     *
     * @param _next the new next
     */
    public void setNext(final AbstractElement<?> _next)
    {
        this.next = _next;
    }


    /**
     * Gets the path.
     *
     * @return the path
     * @throws EFapsException the eFaps exception
     */
    public String getPath()
    {
        final StringBuilder path = new StringBuilder();
        if (getPrevious() != null) {
            path.append(getPrevious().getPath());
        }
        return path.toString();
    }

    /**
     * Gets the this.
     *
     * @return the this
     */
    public abstract T getThis();

    /**
     * Gets the object.
     *
     * @param _row the row
     * @return the object
     * @throws EFapsException the eFaps exception
     */
    public abstract Object getObject(Object[] _row)
        throws EFapsException;

}
