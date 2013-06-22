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

import java.util.List;

import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class CachedMultiPrintQuery
    extends MultiPrintQuery
{

    /**
     * Key used for Caching.
     */
    private final String key;

    /**
     * @param _instances instance to be updated.
     * @param _key key used for caching
     * @throws EFapsException on error
     */
    public CachedMultiPrintQuery(final List<Instance> _instances,
                                 final String _key)
        throws EFapsException
    {
        super(_instances);
        this.key = _key;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCacheEnabled()
    {
        return true;
    }

    @Override
    public String getKey()
    {
        return this.key;
    }

}
