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

package org.efaps.update.schema.program.staticsource;

import org.efaps.ci.CIAdminProgram;
import org.efaps.update.Install.InstallFile;
import org.efaps.update.util.InstallationException;

/**
 * Class used to import cascade style sheets into eFaps.
 *
 * @author The eFaps Team
 */
public class CSSImporter
    extends AbstractStaticSourceImporter
{

    /**
     * Instantiates a new CSS importer.
     *
     * @param _installFile the install file
     * @throws InstallationException on error
     */
    public CSSImporter(final InstallFile _installFile)
        throws InstallationException
    {
        super(CIAdminProgram.CSS, _installFile);
    }
}
