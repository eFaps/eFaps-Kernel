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
package org.efaps.util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.efaps.admin.datamodel.Type;
import org.efaps.util.cache.CacheReloadException;

public final class TypeUtil
{

    public static List<Type> getTypes(final String... _types)
        throws CacheReloadException
    {
        final List<Type> ret = new ArrayList<>();
        for (final String typeStr : _types) {
            final Type type;
            if (UUIDUtil.isUUID(typeStr)) {
                type = Type.get(UUID.fromString(typeStr));
            } else {
                type = Type.get(typeStr);
            }
            if (type.isAbstract()) {
                type.getChildTypes()
                                .stream()
                                .filter(t -> !t.isAbstract())
                                .forEach(child -> ret.add(child));
            } else {
                ret.add(type);
            }
        }
        return ret;
    }
}
