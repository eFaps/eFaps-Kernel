/*
 * Copyright © 2003 - 2024 The eFaps Team (-)
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
 */
package org.efaps.db.stmt.selection;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections4.MultiMapUtils;
import org.apache.commons.collections4.MultiValuedMap;
import org.efaps.admin.access.AccessType;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.Instance;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class Access.
 */
public class Access
{
    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(Access.class);

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
        accessType = _accessType;
        _instances.forEach(inst -> {
            instanceMap.put(inst.getType(), inst);
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
        if (!init) {
            init = true;
            for (final Entry<Type, Collection<Instance>> entry : instanceMap.asMap().entrySet()) {
                accessMap.putAll(entry.getKey().checkAccess(entry.getValue(), accessType));
            }
        }
    }

    /**
     * Checks for access.
     *
     * @param _instance the instance
     * @return true, if successful
     */
    public boolean hasAccess(final Instance _instance)
    {
        try {
            initialize();
        } catch (final EFapsException e) {
            LOG.error("Problems while evaluation access.", e);
        }
        return accessMap.containsKey(_instance) ? accessMap.get(_instance) : false;
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

    public static Access getNoOp()
    {
        return new Access(null, Collections.emptyList())
        {
            @Override
            public boolean hasAccess(final Instance _instance)
            {
                return true;
            }
        };
    }
}
