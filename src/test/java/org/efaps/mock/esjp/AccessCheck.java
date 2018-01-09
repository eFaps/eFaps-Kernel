/*
 * Copyright 2003 - 2017 The eFaps Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.efaps.mock.esjp;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.db.Instance;
import org.efaps.util.EFapsException;

/**
 * The Class AccessCheck.
 */
public class AccessCheck
{

    /** The results. */
    public static final Map<Instance, Boolean> RESULTS = new HashMap<>();

    /**
     * Execute.
     *
     * @param _parameter the parameter
     * @return the return
     * @throws EFapsException the eFaps exception
     */
    public Return execute(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        @SuppressWarnings("unchecked")
        final Collection<Instance> instances = (Collection<Instance>) _parameter.get(ParameterValues.OTHERS);
        if (instances == null) {
            final Instance instance = (Instance) _parameter.get(ParameterValues.INSTANCE);
            if (RESULTS.containsKey(instance) && RESULTS.get(instance) || !RESULTS.containsKey(instance)) {
                ret.put(ReturnValues.TRUE, true);
            }
        } else {
            final Map<Instance, Boolean> map = new HashMap<>();
            instances.forEach(inst -> map.put(inst, RESULTS.containsKey(inst) ? RESULTS.get(inst) : true));
            ret.put(ReturnValues.VALUES, map);
        }
        return ret;
    }
}
