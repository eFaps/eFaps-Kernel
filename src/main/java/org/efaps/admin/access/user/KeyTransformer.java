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

package org.efaps.admin.access.user;

import org.apache.commons.lang3.ArrayUtils;
import org.infinispan.query.Transformer;

/**
 * The Class KeyTransformer.
 *
 * @author The eFaps Team
 */
public class KeyTransformer
    implements Transformer
{

    @Override
    public Object fromString(final String _str)
    {
        final String[] arr = _str.split("\\|");
        final Key ret;
        if (ArrayUtils.isNotEmpty(arr)) {
            ret = new Key();
            ret.setPersonId(Long.parseLong(arr[0]))
                .setCompanyId(Long.parseLong(arr[1]))
                .setTypeId(Long.parseLong(arr[2]));
        } else {
            ret = null;
        }
        return ret;
    }

    @Override
    public String toString(final Object _customType)
    {
        final Key key = (Key) _customType;
        return key.getPersonId() + "|" + key.getCompanyId() + "|" + key.getTypeId();
    }
}
