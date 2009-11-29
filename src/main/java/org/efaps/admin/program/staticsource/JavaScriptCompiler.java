/*

 * Copyright 2003 - 2009 The eFaps Team
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

package org.efaps.admin.program.staticsource;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.UUID;

import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.efaps.admin.EFapsClassNames;
import org.efaps.admin.common.SystemConfiguration;
import org.efaps.db.Checkout;
import org.efaps.util.EFapsException;

import com.yahoo.platform.yui.compressor.JavaScriptCompressor;

/**
 * Compiler for JavaScript. Compiling actually means that the JavaScripts are
 * compressed to be smaller. e.g. removing of comments, linebreaks etc.
 * The compression can be deactivated by setting the boolean attribute
 * "JavaScript_deactivate_Compression"  in the
 * kernel SystemConfiguration to "true".
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class JavaScriptCompiler extends AbstractSourceCompiler
{
    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(JavaScriptCompiler.class);

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getCompiledString(final String _oid)
    {
        final SystemConfiguration kernelConfig = SystemConfiguration.get(UUID
                        .fromString("acf2b19b-f7c4-4e4a-a724-fb2d9ed30079"));
        final StringBuilder ret = new StringBuilder();
        final Checkout checkout = new Checkout(_oid);
        try {
            final BufferedReader in = new BufferedReader(new InputStreamReader(checkout.execute(), "UTF-8"));
            if (JavaScriptCompiler.LOG.isDebugEnabled()) {
                final BufferedReader in2 = new BufferedReader(new InputStreamReader(checkout.execute(), "UTF-8"));
                final StringBuilder bldr = new StringBuilder();
                String line = "";
                while (line != null) {
                    line = in2.readLine();
                    bldr.append(line != null ? line : "").append("\n");
                }
                JavaScriptCompiler.LOG.debug(bldr.toString());
                in2.close();
            }
            if (kernelConfig.getAttributeValueAsBoolean("JavaScript_deactivate_Compression")) {
                String line = "";
                while (line != null) {
                    line = in.readLine();
                    ret.append(line != null ? line : "").append("\n");
                }
            } else {
                final JavaScriptCompressor compressor = new JavaScriptCompressor(in, new ErrorReporter() {

                    /**
                     * @see org.mozilla.javascript.ErrorReporter#error(java.lang.String, java.lang.String, int, java.lang.String, int)
                     * @param _error error to be written to the log
                     * @param _arg1 not used
                     * @param _arg2 not used
                     * @param _arg3 not used
                     * @param _arg4 not used
                     */
                    public void error(final String _error,
                                      final String _arg1,
                                      final int _arg2,
                                      final String _arg3,
                                      final int _arg4)
                    {
                        JavaScriptCompiler.LOG.error(_error);
                    }

                    /**
                     * @see org.mozilla.javascript.ErrorReporter#runtimeError(java.lang.String, java.lang.String, int, java.lang.String, int)
                     * @param _arg0 not used
                     * @param _arg1 not used
                     * @param _arg2 not used
                     * @param _arg3 not used
                     * @param _arg4 not used
                     * @return null not used
                     */
                    public EvaluatorException runtimeError(final String _arg0,
                                                           final String _arg1,
                                                           final int _arg2,
                                                           final String _arg3,
                                                           final int _arg4)
                    {
                        return null;
                    }

                    /**
                     * @see org.mozilla.javascript.ErrorReporter#warning(java.lang.String, java.lang.String, int, java.lang.String, int)
                     * @param _warning  warning to be shown
                     * @param _arg1     arg1 not used
                     * @param _arg2     arg2 not used
                     * @param _arg3     arg3 not used
                     * @param _arg4     arg4 not used
                     */
                    public void warning(final String _warning,
                                        final String _arg1,
                                        final int _arg2,
                                        final String _arg3,
                                        final int _arg4)
                    {
                        // Admin_Program_JavaScriptCompiled_Warn: do we want
                        // warnings?
                        final SystemConfiguration kernelConfig = SystemConfiguration.get(UUID
                                        .fromString("acf2b19b-f7c4-4e4a-a724-fb2d9ed30079"));
                        if (kernelConfig.getAttributeValueAsBoolean("JavaScriptCompiled_Warn")) {
                            JavaScriptCompiler.LOG.warn(_warning);
                        }
                    }
                });

                in.close();
                checkout.close();
                final ByteArrayOutputStream byteout = new ByteArrayOutputStream();
                final OutputStreamWriter out = new OutputStreamWriter(byteout);

                compressor.compress(out, 140, false, true, true, true);
                out.flush();
                ret.append(byteout.toString());
            }
        } catch (final EFapsException e) {
            JavaScriptCompiler.LOG.error("error during checkout of Instance with oid:" + _oid, e);
            e.printStackTrace();
        } catch (final EvaluatorException e) {
            JavaScriptCompiler.LOG.error("error during the evaluation of the JavaScript of "
                            + "Instance with oid:" + _oid, e);
        } catch (final IOException e) {
            JavaScriptCompiler.LOG.error("error during reqding of the Inputstram of Instance with oid:" + _oid, e);
        }
        ret.append("\n");
        if (JavaScriptCompiler.LOG.isDebugEnabled()) {
            JavaScriptCompiler.LOG.debug(ret.toString());
        }
        return ret.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractSource getNewSource(final String _name,
                                       final String _oid,
                                       final long _id)
    {
        return new OneJavaScript(_name, _oid, _id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected EFapsClassNames getClassName4Type()
    {
        return EFapsClassNames.ADMIN_PROGRAM_JAVASCRIPT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected EFapsClassNames getClassName4Type2Type()
    {
        return EFapsClassNames.ADMIN_PROGRAM_JAVASCRIPT2JAVASCRIPT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected EFapsClassNames getClassName4TypeCompiled()
    {
        return EFapsClassNames.ADMIN_PROGRAM_JAVASCRIPTCOMPILED;
    }

    /**
     * Class to store a javascript during compelation.
     */
    protected class OneJavaScript extends AbstractSource
    {
        /**
         * @param _name     Name of the JavaScript
         * @param _oid      oid of the JavaScript
         * @param _id       id of the JavaScript
         */
        public OneJavaScript(final String _name,
                             final String _oid,
                             final long _id)
        {
            super(_name, _oid, _id);
        }
    }
}
