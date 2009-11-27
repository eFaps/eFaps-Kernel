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
 * TODO description
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class JavaScriptCompiler extends AbstractSourceCompiler
{

    private static final Logger LOG = LoggerFactory.getLogger(JavaScriptCompiler.class);

    @Override
    protected String getCompiledString(final String _oid)
    {
        final Checkout checkout = new Checkout(_oid);
        BufferedReader in;
        ByteArrayOutputStream byteout = null;
        try {
            in = new BufferedReader(new InputStreamReader(checkout.execute()));

            final JavaScriptCompressor compressor = new JavaScriptCompressor(in, new ErrorReporter() {

                public void error(final String arg0, final String arg1, final int arg2, final String arg3,
                                final int arg4)
                {
                    LOG.error(arg0);
                }

                public EvaluatorException runtimeError(final String arg0, final String arg1, final int arg2,
                                final String arg3, final int arg4)
                {
                    return null;
                }

                public void warning(final String arg0, final String arg1, final int arg2, final String arg3,
                                final int arg4)
                {
                    // Admin_Program_JavaScriptCompiled_Warn: do we want
                    // warnings?
                    final SystemConfiguration kernelConfig = SystemConfiguration.get(UUID
                                    .fromString("acf2b19b-f7c4-4e4a-a724-fb2d9ed30079"));
                    if (kernelConfig.getAttributeValueAsBoolean("JavaScriptCompiled_Warn")) {
                        LOG.warn(arg0);
                    }
                }
            });

            in.close();
            checkout.close();
            byteout = new ByteArrayOutputStream();
            final OutputStreamWriter out = new OutputStreamWriter(byteout);
            compressor.compress(out, 2000, false, true, false, true);
            out.flush();
        } catch (final EFapsException e) {
            LOG.error("error during checkout of Instance with oid:" + _oid, e);
            e.printStackTrace();
        } catch (final EvaluatorException e) {
            LOG.error("error during the evaluation of the JavaScript of Instance with oid:" + _oid, e);
        } catch (final IOException e) {
            LOG.error("error during reqding of the Inputstram of Instance with oid:" + _oid, e);
        }
        String ret = byteout.toString();
        ret += "\n";
        return ret;
    }

    @Override
    public AbstractSource getNewSource(final String _name, final String _oid, final long _id)
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
     *
     */
    protected class OneJavaScript extends AbstractSource
    {

        public OneJavaScript(final String _name, final String _oid, final long _id)
        {
            super(_name, _oid, _id);
        }

    }
}
