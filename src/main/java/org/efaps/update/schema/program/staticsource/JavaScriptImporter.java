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
 * Class used to import javascript programs into eFaps.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class JavaScriptImporter
    extends AbstractStaticSourceImporter
{
    /**
     * @param _url URL of the javascript file
     * @throws InstallationException on error
     */
    public JavaScriptImporter(final InstallFile _installFile)
        throws InstallationException
    {
        super(CIAdminProgram.JavaScript, _installFile);
    }
}
