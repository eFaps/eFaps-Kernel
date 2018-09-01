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

import java.util.Iterator;
import java.util.List;

import org.efaps.db.wrapper.SQLSelect;
import org.efaps.util.EFapsException;

/**
 * The Class ObjectElement.
 */
public class PrimedElement
    extends AbstractDataElement<PrimedElement>
{

    /** The objects. */
    private final Iterator<Object> objects;

    /**
     * Instantiates a new no select element.
     *
     * @param _objects the objects
     */
    public PrimedElement(final List<Object> _objects)
    {
        this.objects = _objects.iterator();
    }

    @Override
    public PrimedElement getThis()
    {
        return this;
    }

    @Override
    public Object getObject(final Object[] _row)
        throws EFapsException
    {
        return this.objects.next();
    }

    @Override
    public void append2SQLSelect(final SQLSelect _sqlSelect)
        throws EFapsException
    {
    }
}
