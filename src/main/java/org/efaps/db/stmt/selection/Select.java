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
package org.efaps.db.stmt.selection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.efaps.db.stmt.selection.elements.AbstractElement;
import org.efaps.util.EFapsException;

/**
 * The Class Selection.
 *
 * @author The eFaps Team
 */
public final class Select
{

    /** The objects. */
    private final List<Object> objects = new ArrayList<>();

    /** The elements. */
    private final List<AbstractElement<?>> elements = new ArrayList<>();

    /** The alias. */
    private final String alias;

    /**
     * Instantiates a new select.
     *
     * @param _alias the alias
     */
    private Select(final String _alias)
    {
        this.alias = _alias;
    }

    /**
     * Adds the element.
     *
     * @param _element the element
     * @return the select
     */
    protected Select addElement(final AbstractElement<?> _element)
    {
        if (!this.elements.isEmpty()) {
            final AbstractElement<?> prev = this.elements.get(this.elements.size() - 1);
            _element.setPrevious(prev);
        }
        this.elements.add(_element);
        return this;
    }

    /**
     * Adds the object.
     *
     * @param _row the row
     * @throws EFapsException the e faps exception
     */
    public void addObject(final Object[] _row)
        throws EFapsException
    {
        this.objects.add(this.elements.get(0).getObject(_row));
    }

    /**
     * Gets the objects.
     *
     * @return the objects
     */
    public List<Object> getObjects()
    {
        return Collections.unmodifiableList(this.objects);
    }

    /**
     * Gets the elements.
     *
     * @return the elements
     */
    public List<AbstractElement<?>> getElements()
    {
        return this.elements;
    }

    /**
     * Gets the alias.
     *
     * @return the alias
     */
    public String getAlias()
    {
        return this.alias;
    }

    /**
     * Gets the.
     *
     * @return the select
     */
    public static Select get()
    {
        return new Select(null);
    }

    /**
     * Gets the.
     *
     * @param _alias the alias
     * @return the select
     */
    public static Select get(final String _alias)
    {
        return new Select(_alias);
    }
}
