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

package org.efaps.update.schema.program.staticsource;

import org.efaps.ci.CIAdminProgram;
import org.efaps.update.Install.InstallFile;
import org.efaps.update.util.InstallationException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class WikiImporter
    extends AbstractStaticSourceImporter
{
    /**
     * @param _url  url to the file to be imported
     * @throws InstallationException on error
     */
    public WikiImporter(final InstallFile _installFile)
        throws InstallationException
    {
        super(CIAdminProgram.Wiki, _installFile);
    }
}
