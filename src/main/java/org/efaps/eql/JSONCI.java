/*
 * Copyright 2003 - 2015 The eFaps Team
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
package org.efaps.eql;

import java.util.UUID;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Type;
import org.efaps.eql.stmt.ICIPrintStmt;
import org.efaps.json.ci.AbstractCI;
import org.efaps.util.UUIDUtil;
import org.efaps.util.cache.CacheReloadException;

/**
 * The Class JSONCI.
 *
 * @author The eFaps Team
 */
public final class JSONCI
{
    /**
     * Singelton.
     */
    private JSONCI()
    {
    }

    /**
     * Gets the ci.
     *
     * @param _stmt the stmt
     * @return the ci
     * @throws CacheReloadException the cache reload exception
     */
    public static AbstractCI<?> getCI(final ICIPrintStmt _stmt)
        throws CacheReloadException
    {
        AbstractCI<?> ret = null;
        switch (_stmt.getCINature()) {
            case TYPE:
                Type type;
                if (UUIDUtil.isUUID(_stmt.getCI())) {
                    type = Type.get(UUID.fromString(_stmt.getCI()));
                } else {
                    type = Type.get(_stmt.getCI());
                }
                if (type != null) {
                    final org.efaps.json.ci.Type jsonType = new org.efaps.json.ci.Type()
                                    .setName(type.getName())
                                    .setUUID(type.getUUID())
                                    .setId(type.getId());
                    for (final Attribute attr : type.getAttributes().values()) {
                        jsonType.addAttribute(new org.efaps.json.ci.Attribute()
                                        .setName(attr.getName()));
                    }
                    ret = jsonType;
                }
                break;
            default:
                break;
        }
        return ret;
    }
}
