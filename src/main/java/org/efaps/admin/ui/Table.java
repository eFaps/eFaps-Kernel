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

package org.efaps.admin.ui;

import java.util.UUID;

import org.efaps.ci.CIAdminUserInterface;
import org.efaps.util.cache.CacheReloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author The eFaps Team
 * TODO:
 *          description
 */
public class Table
    extends AbstractCollection
    implements Cloneable
{

    /**
     * Logging instance used in this class.
     */
    protected static final Logger LOG = LoggerFactory.getLogger(Table.class);

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * This is the constructor to set the id and the name.
     *
     * @param _id id of the new table
     * @param _uuid UUID of the new table
     * @param _name name of the new table
     */
    public Table(final Long _id,
                 final String _uuid,
                 final String _name)
    {
        super(_id, _uuid, _name);
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
     * @param _id id to search in the cache
     * @return instance of class {@link Table}
     * @throws CacheReloadException on error
     */
    public static Table get(final long _id)
        throws CacheReloadException
    {
        return AbstractUserInterfaceObject.<Table>get(_id, Table.class, CIAdminUserInterface.Table.getType());
    }

    /**
     * Returns for given parameter <i>_name</i> the instance of class
     * {@link Table}.
     *
     * @param _name name to search in the cache
     * @return instance of class {@link Table}
     * @throws CacheReloadException on error
     */
    public static Table get(final String _name)
        throws CacheReloadException
    {
        return AbstractUserInterfaceObject.<Table>get(_name, Table.class, CIAdminUserInterface.Table.getType());
    }

    /**
     * Returns for given parameter <i>UUID</i> the instance of class
     * {@link Table}.
     *
     * @param _uuid UUID to search in the cache
     * @return instance of class {@link Table}
     * @throws CacheReloadException on error
     */
    public static Table get(final UUID _uuid)
        throws CacheReloadException
    {
        return AbstractUserInterfaceObject.<Table>get(_uuid, Table.class, CIAdminUserInterface.Table.getType());
    }
}
