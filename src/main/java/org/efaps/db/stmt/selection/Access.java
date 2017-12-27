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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections4.MultiMapUtils;
import org.apache.commons.collections4.MultiValuedMap;
import org.efaps.admin.access.AccessType;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.Instance;
import org.efaps.util.EFapsException;

/**
 * The Class Access.
 */
public final class Access
{

    /** The instance map. */
    private final MultiValuedMap<Type, Instance> instanceMap = MultiMapUtils.newSetValuedHashMap();

    /** The access type. */
    private final AccessType accessType;

    /** The access map. */
    private final Map<Instance, Boolean> accessMap = new HashMap<>();

    /** The init. */
    private boolean init;

    /**
     * Instantiates a new access.
     *
     * @param _accessType the access type
     * @param _instances the instances
     */
    private Access(final AccessType _accessType,
                   final Collection<Instance> _instances)
    {
        this.accessType = _accessType;
        _instances.forEach(inst -> {
            this.instanceMap.put(inst.getType(), inst);
        });
    }

    /**
     * Initialize.
     *
     * @throws EFapsException the e faps exception
     */
    private void initialize()
        throws EFapsException
    {
        if (!this.init) {
            this.init = true;
            for (final Entry<Type, Collection<Instance>> entry : this.instanceMap.asMap().entrySet()) {
                this.accessMap.putAll(entry.getKey().checkAccess(entry.getValue(), this.accessType));
            }
        }
    }

    /**
     * Checks for access.
     *
     * @param _instance the instance
     * @return true, if successful
     * @throws EFapsException the e faps exception
     */
    public boolean hasAccess(final Instance _instance)
        throws EFapsException
    {
        initialize();
        return this.accessMap.containsKey(_instance) ? this.accessMap.get(_instance) : false;
    }

    /**
     * Gets the.
     *
     * @param _accessType the access type
     * @param _instances the instances
     * @return the access
     */
    public static Access get(final AccessType _accessType, final Collection<Instance> _instances)
    {
        return new Access(_accessType, _instances);
    }
}
