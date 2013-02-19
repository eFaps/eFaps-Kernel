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

package org.efaps.update.schema.program.esjp;

import java.net.URL;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.efaps.ci.CIAdminProgram;
import org.efaps.update.schema.program.AbstractSourceImporter;
import org.efaps.update.util.InstallationException;

/**
 * Java source code could be imported into eFaps as ESJP with this class. The
 * class does not need any XML update files and could be called directly.
 *
 * @author The eFaps Team
 * @version $Id$
 * TODO:  encoding from java files!
 */
public class ESJPImporter
    extends AbstractSourceImporter
{
    /**
     * Constructor used to read the source code from given URL and extract the
     * class name.
     *
     * @param _url    url to the ESJP source code
     * @throws InstallationException on error
     */
    public ESJPImporter(final URL _url)
        throws InstallationException
    {
        super(CIAdminProgram.Java, _url);
    }

    /**
     * This Method extracts the package name and sets the name of this Java
     * definition (the name is the package name together with the name of the
     * file excluding the <code>.java</code>).
     *
     * @return class name of the ESJP
     */
    @Override
    protected String evalProgramName()
    {
        final String urlPath = getUrl().getPath();
        String name = urlPath.substring(urlPath.lastIndexOf('/') + 1);
        name = name.substring(0, name.lastIndexOf('.'));

        // regular expression for the package name
        final Pattern pckPattern = Pattern.compile("package +[^;]+;");
        final Matcher pckMatcher = pckPattern.matcher(getCode());
        if (pckMatcher.find()) {
            final String pkg = pckMatcher.group()
                                         .replaceFirst("^(package) +", "")
                                         .replaceFirst(";$", "");
            name = pkg + "." + name;
        }
        return name;
    }

    /**
     * This Method extracts the UUID from the ESJP.
     *
     * @return UUID of the ESJP
     */
    @Override
    protected UUID evalUUID()
    {
        UUID uuid = null;

        final Pattern uuidPattern = Pattern.compile("@EFapsUUID ?\\( ?\\\"[0-9a-z\\-]*\\\" ?\\)");
        final Matcher uuidMatcher = uuidPattern.matcher(getCode());
        if (uuidMatcher.find()) {
            final String uuidStr = uuidMatcher.group()
                                              .replaceFirst("^@EFapsUUID ?\\( ?\\\"", "")
                                              .replaceFirst("\\\" ?\\)", "");
            uuid = UUID.fromString(uuidStr);
        }
        return uuid;
    }

    /**
     * This Method extracts the Revision from the ESJP.
     *
     * @return revision of the ESJP
     */
    @Override
    protected String evalRevision()
    {
        String ret = null;
        final Pattern revisionPattern = Pattern.compile("@EFapsRevision ?\\( ?\\\".*\\\" ?\\)");
        final Matcher revisionMatcher = revisionPattern.matcher(getCode());
        if (revisionMatcher.find()) {
            ret = revisionMatcher.group()
                                 .replaceFirst("^@EFapsRevision ?\\( ?\\\"", "")
                                 .replaceFirst("\\\" ?\\)", "");
        }
        return ret;
    }
}
