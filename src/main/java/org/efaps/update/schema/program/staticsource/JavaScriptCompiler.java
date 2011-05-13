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

package org.efaps.update.schema.program.staticsource;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.efaps.admin.EFapsSystemConfiguration;
import org.efaps.admin.common.SystemConfiguration;
import org.efaps.ci.CIAdminProgram;
import org.efaps.ci.CIType;
import org.efaps.db.Checkout;
import org.efaps.db.Instance;
import org.efaps.util.EFapsException;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class JavaScriptCompiler
    extends AbstractStaticSourceCompiler
{
    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(JavaScriptCompiler.class);

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getCompiledString(final Instance _instance)
    {
        final SystemConfiguration kernelConfig = EFapsSystemConfiguration.KERNEL.get();
        final StringBuilder ret = new StringBuilder();
        final Checkout checkout = new Checkout(_instance);
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
                        final SystemConfiguration kernelConfig = EFapsSystemConfiguration.KERNEL.get();
                        try {
                            if (kernelConfig.getAttributeValueAsBoolean("JavaScriptCompiled_Warn")) {
                                JavaScriptCompiler.LOG.warn(_warning);
                            }
                        } catch (final EFapsException e) {
                            JavaScriptCompiler.LOG.error("error during checkout of Instance:" + _instance, e);
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
            JavaScriptCompiler.LOG.error("error during checkout of Instance:" + _instance, e);
            e.printStackTrace();
        } catch (final EvaluatorException e) {
            JavaScriptCompiler.LOG.error("error during the evaluation of the JavaScript of "
                            + "Instance:" + _instance, e);
        } catch (final IOException e) {
            JavaScriptCompiler.LOG.error("error during reqding of the Inputstram of Instance:" + _instance, e);
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
                                       final Instance _instance)
    {
        return new OneJavaScript(_name, _instance);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected CIType getClassName4Type()
    {
        return CIAdminProgram.JavaScript;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected CIType getClassName4Type2Type()
    {
        return  CIAdminProgram.JavaScript2JavaScript;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected CIType getClassName4TypeCompiled()
    {
        return  CIAdminProgram.JavaScriptCompiled;
    }

    /**
     * Class to store a javascript during compelation.
     */
    protected class OneJavaScript
        extends AbstractSource
    {
        /**
         * @param _name     Name of the JavaScript
         * @param _instance Instance of the JavaScript
         */
        public OneJavaScript(final String _name,
                             final Instance _instance)
        {
            super(_name, _instance);
        }
    }
}
