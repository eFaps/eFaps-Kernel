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
package org.efaps.admin.index;

import org.apache.lucene.store.Directory;
import org.efaps.util.EFapsException;

/**
 * The Interface IDirectoryProvider.
 *
 * @author The eFaps Team
 */
public interface IDirectoryProvider
{

    /**
     * Gets the directory for the standart index.
     *
     * @return the directory
     * @throws EFapsException on error
     */
    Directory getDirectory()
        throws EFapsException;

    /**
     * Gets the directory for the taxonomy.
     *
     * @return the directory
     * @throws EFapsException on error
     */
    Directory getTaxonomyDirectory()
        throws EFapsException;

}
