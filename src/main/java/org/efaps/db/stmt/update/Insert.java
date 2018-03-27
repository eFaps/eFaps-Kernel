/*
 * Copyright 2003 - 2018 The eFaps Team
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
package org.efaps.db.stmt.update;

import java.util.UUID;

import org.efaps.admin.datamodel.Type;
import org.efaps.eql2.IInsertStatement;
import org.efaps.util.UUIDUtil;
import org.efaps.util.cache.CacheReloadException;

/**
 * The Class Insert.
 */
public class Insert
    extends AbstractUpdate
{
    /** The type. */
    private Type type;

    /**
     * Instantiates a new insert.
     *
     * @param _eqlStmt the eql stmt
     * @throws CacheReloadException
     */
    public Insert(final IInsertStatement _eqlStmt)
        throws CacheReloadException
    {
        super(_eqlStmt);
        final String typeStr = ((IInsertStatement) getEqlStmt()).getTypeName();
        if (UUIDUtil.isUUID(typeStr)) {
            this.type = Type.get(UUID.fromString(typeStr));
        } else {
            this.type = Type.get(typeStr);
        }
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public Type getType()
    {
        return this.type;
    }
}
