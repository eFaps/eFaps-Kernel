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

package org.efaps.admin.ui;

import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.UUID;

import org.efaps.admin.EFapsClassNames;
import org.efaps.db.Context;

/**
 * @author The eFaps Team
 * @version $Id$
 * TODO: description
 */
public class Table
    extends AbstractCollection
    implements Cloneable
{
    /**
     * The static variable defines the class name in eFaps.
     */
    public static EFapsClassNames EFAPS_CLASSNAME = EFapsClassNames.TABLE;

    /**
     * Stores all instances of class {@link Table}.
     *
     * @see #getCache()
     */
    private static TableCache CACHE = new TableCache();

    /**
     * This is the constructor to set the id and the name.
     *
     * @param _id       id of the new table
     * @param _uuid     UUID of the new table
     * @param _name     name of the new table
     */
    public Table(final Long _id,
                 final String _uuid,
                 final String _name)
    {
        super(_id, _uuid, _name);
    }

    /**
     * The instance method returns the title of the table.
     *
     * @param _context    context for this request
     * @return title of the form
     */
    public String getViewableName(final Context _context)
    {
// TODO: method really required?
        String title = "";
        final ResourceBundle msgs = ResourceBundle.getBundle("org.efaps.properties.AttributeRessource",
                                                             _context.getLocale());
        try {
            title = msgs.getString("Table.Title." + getName());
        } catch (final MissingResourceException e) {
        }
        return title;
    }

    /**
     * Creates and returns a copy of this table object.
     *
     * @return cloned table
     */
    public Table cloneTable()
    {
        Table ret = null;
        try {
            ret = (Table) super.clone();
        } catch (final CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * Returns for given parameter <i>_id</i> the instance of class
     * {@link Table}.
     *
     * @param _id     id to search in the cache
     * @return instance of class {@link Table}
     * @see #CACHE
     */
    public static Table get(final long _id)
    {
        return Table.CACHE.get(_id);
    }

    /**
     * Returns for given parameter <i>_name</i> the instance of class
     * {@link Table}.
     *
     * @param _name   name to search in the cache
     * @return instance of class {@link Table}
     * @see #CACHE
     */
    public static Table get(final String _name)
    {
        return Table.CACHE.get(_name);
    }

    /**
     * Returns for given parameter <i>UUID</i> the instance of class
     * {@link Table}.
     *
     * @param _uuid   UUID to search in the cache
     * @return instance of class {@link Table}
     * @see #CACHE
     */
    public static Table get(final UUID _uuid)
    {
        return Table.CACHE.get(_uuid);
    }

    /**
     * Static getter method for the table {@link #CACHE}.
     *
     * @return value of static variable {@link #CACHE}
     */
    protected static UserInterfaceObjectCache<Table> getCache()
    {
        return Table.CACHE;
    }

    private static class TableCache
        extends UserInterfaceObjectCache<Table>
    {
        protected TableCache()
        {
            super(Table.class);
        }
    }
}
