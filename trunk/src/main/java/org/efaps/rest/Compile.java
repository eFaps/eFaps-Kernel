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
import org.efaps.update.schema.program.staticsource.CSSCompiler;
import org.efaps.update.schema.program.staticsource.JavaScriptCompiler;
import org.efaps.update.schema.program.staticsource.WikiCompiler;
import org.efaps.update.util.InstallationException;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@Path("/compile")
public class Compile
{

    /**
     * Logging instance used to give logging information of this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Compile.class);

    /**
     * Called to copmile java, css etc.
     * @param _type type tobe compiled
     * @return Response
     */
    @GET
    public Response compile(@QueryParam("type") final String _type)
    {
        Compile.LOG.info("===Starting Compiler via REST===");
        boolean success = false;
        try {
            if ("java".equalsIgnoreCase(_type)) {
                Compile.LOG.info("==Compiling Java==");
                compileJava();
            } else if ("css".equalsIgnoreCase(_type)) {
                Compile.LOG.info("==Compiling CSS==");
                new CSSCompiler().compile();
            } else if ("js".equalsIgnoreCase(_type)) {
                Compile.LOG.info("==Compiling Javascript==");
                new JavaScriptCompiler().compile();
            } else if ("wiki".equalsIgnoreCase(_type)) {
                Compile.LOG.info("==Compiling Wiki==");
                new WikiCompiler().compile();
            }
            success = true;
            Compile.LOG.info("===Ending Compiler via REST===");
        } catch (final InstallationException e) {
            Compile.LOG.error("InstallationException", e);
        } catch (final EFapsException e) {
            Compile.LOG.error("EFapsException", e);
        }
        return success ? Response.ok().build() : Response.noContent().build();
    }

    /**
     * Compile Java.
     * @throws InstallationException on error
     */
    private void compileJava()
        throws InstallationException
    {
        // Kernel-Configuration
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
                    if (new File(_dir, _name).isDirectory())
                    {
                        ret = false;
                    } else {
                        final String name = _name.toLowerCase();
                        ret = name.endsWith(".jar");
                    }
                    return ret;
                }
            });
            final List<String> classpathElements = new ArrayList<String>();
            for (final File file : files) {
                classpathElements.add(file.getAbsolutePath());
            }
            new ESJPCompiler(classpathElements).compile(null);
        }
    }
}
