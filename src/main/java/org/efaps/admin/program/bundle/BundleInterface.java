/*
 * Copyright 2003 - 2011 The eFaps Team
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

package org.efaps.admin.program.bundle;

import java.io.InputStream;
import java.util.List;

import org.efaps.util.EFapsException;

/**
 * Interface for bundles.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public interface BundleInterface
{
    /**
     * @param _gziped zipped or not
     * @return InputStream with the content
     * @throws EFapsException on error
     */
    InputStream getInputStream(final boolean _gziped) throws EFapsException;

    /**
     * @return mime type of the content
     */
    String getContentType();

    /**
     * @return time in miliseconds of the creation of the file.
     */
    long getCreationTime();

    /**
     * @param _key key
     * @param _oids list of oids
     */
    void setKey(final String _key, final List<String> _oids);
}
