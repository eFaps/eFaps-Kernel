/*
 * Copyright 2003 - 2013 The eFaps Team
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

package org.efaps.db;

import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class CachedPrintQuery
    extends PrintQuery
{

    /**
     * Key used for the Cache.
     */
    private String key;

    /**
     * @param _instance instance to be updated.
     * @param _key key used for caching
     * @throws EFapsException on error
     */
    public CachedPrintQuery(final Instance _instance,
                            final String _key)
        throws EFapsException
    {
        super(_instance);
        this.key = _key;
    }

    /**
     * @param _instance instance to be updated.
     * @throws EFapsException on error
     */
    public CachedPrintQuery(final Instance _instance)
        throws EFapsException
    {
        super(_instance);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCacheEnabled()
    {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getKey()
    {
        return this.key;
    }
}
