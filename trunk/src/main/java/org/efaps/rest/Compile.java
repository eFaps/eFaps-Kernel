/*
 * Copyright 2003 - 2010 The eFaps Team
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

package org.efaps.rest;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.efaps.admin.common.SystemConfiguration;
import org.efaps.update.schema.program.esjp.ESJPCompiler;
import org.efaps.update.schema.program.jasperreport.JasperReportCompiler;
import org.efaps.update.schema.program.staticsource.CSSCompiler;
import org.efaps.update.schema.program.staticsource.JavaScriptCompiler;
import org.efaps.update.schema.program.staticsource.WikiCompiler;
import org.efaps.update.util.InstallationException;
import org.efaps.util.EFapsException;

/**
 * Rest API to compile the different program froms eFaps.
 *
 * @author The eFaps Team
 * @version $Id$
 */
@Path("/compile")
public class Compile
    extends AbstractRest
{

    /**
     * Called to compile java, css etc.
     *
     * @param _type type tobe compiled
     * @return Response
     */
    @GET
    public Response compile(@QueryParam("type") final String _type)
    {
        boolean success = false;
        try {
            if (hasAccess()) {
                AbstractRest.LOG.info("===Starting Compiler via REST===");
                if ("java".equalsIgnoreCase(_type)) {
                    AbstractRest.LOG.info("==Compiling Java==");
                    new ESJPCompiler(getClassPathElements()).compile(null, false);
                } else if ("css".equalsIgnoreCase(_type)) {
                    AbstractRest.LOG.info("==Compiling CSS==");
                    new CSSCompiler().compile();
                } else if ("js".equalsIgnoreCase(_type)) {
                    AbstractRest.LOG.info("==Compiling Javascript==");
                    new JavaScriptCompiler().compile();
                } else if ("wiki".equalsIgnoreCase(_type)) {
                    AbstractRest.LOG.info("==Compiling Wiki==");
                    new WikiCompiler().compile();
                } else if ("jasper".equalsIgnoreCase(_type)) {
                    AbstractRest.LOG.info("==Compiling JasperReports==");
                    new JasperReportCompiler(getClassPathElements()).compile();
                }
                success = true;
                AbstractRest.LOG.info("===Ending Compiler via REST===");
            }
        } catch (final InstallationException e) {
            AbstractRest.LOG.error("InstallationException", e);
        } catch (final EFapsException e) {
            AbstractRest.LOG.error("EFapsException", e);
        }
        return success ? Response.ok().build() : Response.noContent().build();
    }

    /**
     * @return lsit of classpath elements
     */
    private List<String> getClassPathElements()
    {
        final List<String> ret = new ArrayList<String>();
        final SystemConfiguration config = SystemConfiguration.get(
                        UUID.fromString("acf2b19b-f7c4-4e4a-a724-fb2d9ed30079"));
        final String paths = config.getAttributeValue("ClassPaths");

        final File folder = new File(paths);
        File[] files = null;
        if (folder.isDirectory()) {
            files = folder.listFiles(new FilenameFilter() {

                @Override
                public boolean accept(final File _dir,
                                      final String _name)
                {
                    final boolean ret;
                    if (new File(_dir, _name).isDirectory()) {
                        ret = false;
                    } else {
                        final String name = _name.toLowerCase();
                        ret = name.endsWith(".jar");
                    }
                    return ret;
                }
            });
            for (final File file : files) {
                ret.add(file.getAbsolutePath());
            }
        }
        return ret;
    }
}
