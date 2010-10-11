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

package org.efaps.update.schema.program.staticsource;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.efaps.admin.EFapsClassNames;
import org.efaps.db.Checkout;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yahoo.platform.yui.compressor.CssCompressor;

/**
 * TODO description
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class CSSCompiler
    extends AbstractStaticSourceCompiler
{

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(CSSCompiler.class);

    /**
     * {@inheritDoc}
     */
    @Override
    protected EFapsClassNames getClassName4Type()
    {
        return EFapsClassNames.ADMIN_PROGRAM_CSS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected EFapsClassNames getClassName4Type2Type()
    {
        return EFapsClassNames.ADMIN_PROGRAM_CSS2CSS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected EFapsClassNames getClassName4TypeCompiled()
    {
        return EFapsClassNames.ADMIN_PROGRAM_CSSCOMPILED;
    }

    @Override()
    protected String getCompiledString(final String _oid)
    {
        String ret = "";
        try {
            final Checkout checkout = new Checkout(_oid);
            final BufferedReader in = new BufferedReader(new InputStreamReader(checkout.execute(), "UTF-8"));

            final CssCompressor compressor = new CssCompressor(in);
            in.close();
            checkout.close();
            final ByteArrayOutputStream byteout = new ByteArrayOutputStream();
            final OutputStreamWriter out = new OutputStreamWriter(byteout);
            compressor.compress(out, 2000);
            out.flush();

            ret = byteout.toString();
            ret += "\n";

        } catch (final EFapsException e) {
            CSSCompiler.LOG.error("error during checkout of Instance with oid:" + _oid, e);
            e.printStackTrace();
        } catch (final IOException e) {
            CSSCompiler.LOG.error("error during reqding of the Inputstram of Instance with oid:" + _oid, e);
        }
        return ret;

    }

    @Override
    public AbstractSource getNewSource(final String _name, final String _oid, final long _id)
    {
        return new OneCSS(_name, _oid, _id);
    }

    /**
     * TODO description
     *
     * @author jmox
     * @version $Id$
     */
    protected class OneCSS extends AbstractSource
    {

        public OneCSS(final String _name, final String _oid, final long _id)
        {
            super(_name, _oid, _id);
        }
    }

}
