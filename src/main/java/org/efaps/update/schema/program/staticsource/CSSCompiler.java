/*
 * Copyright 2003 - 2016 The eFaps Team
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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.efaps.ci.CIAdminProgram;
import org.efaps.ci.CIType;
import org.efaps.db.Checkout;
import org.efaps.db.Instance;
import org.efaps.update.schema.program.staticsource.CSSCompiler.OneCSS;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yahoo.platform.yui.compressor.CssCompressor;

/**
 * TODO description!
 *
 * @author The eFaps Team
 *
 */
public class CSSCompiler
    extends AbstractStaticSourceCompiler<OneCSS>
{

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(CSSCompiler.class);

    /**
     * {@inheritDoc}
     */
    @Override
    protected CIType getClassName4Type()
    {
        return CIAdminProgram.CSS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected CIType getClassName4Type2Type()
    {
        return CIAdminProgram.CSS2CSS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected CIType getClassName4TypeCompiled()
    {
        return  CIAdminProgram.CSSCompiled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getCompiledString(final Instance _instance)
    {
        String ret = "";
        try {
            final Checkout checkout = new Checkout(_instance);
            final BufferedReader in = new BufferedReader(new InputStreamReader(checkout.execute(), "UTF-8"));

            final CssCompressor compressor = new CssCompressor(in);
            in.close();
            checkout.close();
            final ByteArrayOutputStream byteout = new ByteArrayOutputStream();
            final OutputStreamWriter out = new OutputStreamWriter(byteout, "UTF-8");
            compressor.compress(out, 2000);
            out.flush();
            ret = byteout.toString("UTF-8");
            ret += "\n";
        } catch (final EFapsException e) {
            CSSCompiler.LOG.error("error during checkout of Instance:" + _instance, e);
            e.printStackTrace();
        } catch (final IOException e) {
            CSSCompiler.LOG.error("error during reqding of the Inputstram of Instance with oid:" + _instance, e);
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OneCSS getNewSource(final String _name,
                                       final Instance _instance)
    {
        return new OneCSS(_name, _instance);
    }

    /**
     * Class represents on stylesheet.
     */
    public static class OneCSS
        extends AbstractStaticSourceCompiler.AbstractSource
    {
        /**
         * @param _name     Name
         * @param _instance Instance
         */
        public OneCSS(final String _name,
                      final Instance _instance)
        {
            super(_name, _instance);
        }
    }

}
