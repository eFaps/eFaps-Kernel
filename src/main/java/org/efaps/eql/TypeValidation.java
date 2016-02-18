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

package org.efaps.eql;

import java.util.UUID;

import org.efaps.admin.datamodel.Type;
import org.efaps.eql.validation.IValidation;
import org.efaps.util.UUIDUtil;
import org.efaps.util.cache.CacheReloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id: $
 */
public class TypeValidation
    implements IValidation
{
    /**
     * Logging instance for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(TypeValidation.class);

    @Override
    public boolean validate(final String... _type)
    {
        boolean ret = false;
        try {
            if (UUIDUtil.isUUID(_type[0])) {
                ret = Type.get(UUID.fromString(_type[0])) != null;
            } else {
                ret = Type.get(_type[0]) != null;
            }
        } catch (final CacheReloadException e) {
            LOG.error("Catched", e);
        }
        return ret;
    }
}
