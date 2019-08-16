/*
 * Copyright 2003 - 2019 The eFaps Team
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

import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.db.Instance;
import org.efaps.util.EFapsException;

public class TriggerEvent
{

    /** The results. */
    public static ArrayListValuedHashMap<Instance, EventType> RESULTS = new ArrayListValuedHashMap<>();

    public Return deletePre(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        RESULTS.put(_parameter.getInstance(), EventType.DELETE_PRE);
        return ret;
    }

    public Return deletePost(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        RESULTS.put(_parameter.getInstance(), EventType.DELETE_POST);
        return ret;
    }

    public Return deleteOverride(final Parameter _parameter)
        throws EFapsException
    {
        final Return ret = new Return();
        RESULTS.put(_parameter.getInstance(), EventType.DELETE_OVERRIDE);
        return ret;
    }
}
