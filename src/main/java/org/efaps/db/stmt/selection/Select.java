/*
 * Copyright 2003 - 2019 The eFaps Team
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

import org.apache.commons.collections4.multimap.AbstractListValuedMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.efaps.db.Instance;
import org.efaps.db.stmt.selection.elements.AbstractElement;
import org.efaps.db.stmt.selection.elements.ISquash;
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

    /** The iterator. */
    private Iterator<Object> iterator;

    /** The current. */
    private Object current;

    /** The squash able. */
    private boolean noSquashRequired = true;

    /** The squash. */
    private Squashing squash;

    /**
     * Instantiates a new select.
     *
     * @param _alias the alias
     */
    private Select(final String _alias)
    {
        alias = _alias;
    }

    /**
     * Adds the element.
     *
     * @param _element the element
     * @return the select
     */
    protected Select addElement(final AbstractElement<?> _element)
    {
        noSquashRequired = noSquashRequired && !(_element instanceof ISquash);
        if (!elements.isEmpty()) {
            final AbstractElement<?> prev = elements.get(elements.size() - 1);
            _element.setPrevious(prev);
        }
        elements.add(_element);
        return this;
    }

    /**
     * Checks if is squash able.
     *
     * @return the squash able
     */
    protected boolean isSquash()
    {
        return !noSquashRequired;
    }

    protected Squashing getSquash()
    {
        return squash;
    }

    protected void setSquash(final Squashing _squash)
    {
        squash = _squash;
    }

    protected List<Object> getObjects() {
        return objects;
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
        objects.add(elements.get(0).getObject(_row));
    }

    /**
     * Gets the objects.
     *
     * @param _evaluator the evaluator
     * @return the objects
     * @throws EFapsException the e faps exception
     */
    protected List<Object> getObjects(final Evaluator _evaluator)
        throws EFapsException
    {
        final List<Object> result;
        if (_evaluator == null) {
            result = objects;
        } else {
            result = new ArrayList<>();
            final AbstractElement<?> element = getElements().get(getElements().size() - 1);
            final String path = element.getPath();
            final Select instSelection = _evaluator.getSelection().getInstSelects().get(StringUtils.isEmpty(path)
                            ? Selection.BASEPATH
                            : path);
            final List<Object> instObjs = instSelection.getObjects(null);
            final Iterator<Object> objIter = objects.iterator();
            for (final Object instObj : instObjs) {
                if (_evaluator.getAccess().hasAccess((Instance) instObj)) {
                    result.add(objIter.next());
                } else {
                    objIter.next();
                }
            }
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Gets the elements.
     *
     * @return the elements
     */
    public List<AbstractElement<?>> getElements()
    {
        return elements;
    }

    /**
     * Gets the alias.
     *
     * @return the alias
     */
    public String getAlias()
    {
        return alias;
    }

    /**
     * Gets the current.
     *
     * @return the current
     */
    public Object getCurrent()
    {
        return current instanceof ProxiedObject ? ((ProxiedObject) current).getObject() : current;
    }

    /**
     * Next.
     *
     * @return true, if successful
     */
    public boolean next()
    {
        boolean ret = false;
        if (iterator == null) {
            iterator = objects.iterator();
        }
        if (iterator.hasNext()) {
            current = iterator.next();
            ret = true;
        }
        return ret;
    }

    /**
     * Reset.
     */
    protected void reset()
    {
        iterator = null;
        current = null;
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

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }

    /**
     * The Class SortedListValuedMap.
     *
     * @param <K> the key type
     * @param <V> the value type
     */
    public static class SortedListValuedMap<K, V>
        extends AbstractListValuedMap<K, V>
        implements Serializable
    {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;

        /**
         * Instantiates a new sorted list valued map.
         */
        public SortedListValuedMap()
        {
            super(new TreeMap<>());
        }

        @Override
        protected List<V> createCollection()
        {
            return new ArrayList<>();
        }
    }
}
