/*
 * Copyright 2003 - 2010 The eFaps Team
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

import java.util.UUID;

import org.efaps.admin.datamodel.Type;
import org.efaps.ci.CIAdminUserInterface;
import org.efaps.util.EFapsException;

/**
 * @author The eFaps Team
 * @version $Id$
 */
public class Picker
    extends AbstractCollection
{

    /**
     * Cache for the Pickers.
     */
    private static final PickerCache CACHE = new PickerCache();

    /**
     * @param _id ID
     * @param _uuid UUID
     * @param _name Name
     */
    public Picker(final Long _id,
                  final String _uuid,
                  final String _name)
    {
        super(_id, _uuid, _name);
    }

    /**
     * Returns for given parameter <i>_id</i> the instance of class
     * {@link Picker}.
     *
     * @param _id id to search in the cache
     * @return instance of class {@link Picker}
     * @see #CACHE
     */
    public static Picker get(final long _id)
    {
        return Picker.CACHE.get(_id);
    }

    /**
     * Returns for given parameter <i>_name</i> the instance of class
     * {@link Picker}.
     *
     * @param _name name to search in the cache
     * @return instance of class {@link Picker}
     * @see #CACHE
     */
    public static Picker get(final String _name)
    {
        return Picker.CACHE.get(_name);
    }

    /**
     * Returns for given parameter <i>_uuid</i> the instance of class
     * {@link Picker}.
     *
     * @param _uuid UUID to search in the cache
     * @return instance of class {@link Picker}
     * @see #CACHE
     */
    public static Picker get(final UUID _uuid)
    {
        return Picker.CACHE.get(_uuid);
    }

    /**
     * Static getter method for the picker {@link #CACHE}.
     *
     * @return value of static variable {@link #CACHE}
     */
    protected static AbstractUserInterfaceObjectCache<Picker> getCache()
    {
        return Picker.CACHE;
    }

    /**
     * Cache for Picker.
     */
    private static class PickerCache
        extends AbstractUserInterfaceObjectCache<Picker>
    {

        /**
         *
         */
        protected PickerCache()
        {
            super(Picker.class);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected Type getType()
            throws EFapsException
        {
            return CIAdminUserInterface.Picker.getType();
        }
    }
}
