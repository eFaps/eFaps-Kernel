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

import org.efaps.ci.CIAdminProgram;
import org.efaps.ci.CIType;
import org.efaps.db.Checkout;
import org.efaps.db.Instance;
import org.efaps.update.schema.program.staticsource.WikiCompiler.OneWiki;
import org.efaps.util.EFapsException;
import org.efaps.wikiutil.export.html.WEMHtml;
import org.efaps.wikiutil.parser.gwiki.GWikiParser;
import org.efaps.wikiutil.parser.gwiki.javacc.ParseException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class WikiCompiler
    extends AbstractStaticSourceCompiler<OneWiki>
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected CIType getClassName4Type()
    {
        return CIAdminProgram.Wiki;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected CIType getClassName4Type2Type()
    {
        return CIAdminProgram.Wiki2Wiki;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected CIType getClassName4TypeCompiled()
    {
        return CIAdminProgram.WikiCompiled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getCompiledString(final Instance _instance)
        throws EFapsException
    {
        final Checkout checkout = new Checkout(_instance);
        final WEMHtml wemhtml = new WEMHtml();
        try {
            GWikiParser.parse(wemhtml, checkout.execute(), "UTF-8");
        } catch (final ParseException e) {
            throw new EFapsException(WikiCompiler.class, "ParseException", e);
        }
        return wemhtml.getHtml();
    }

     /**
      * {@inheritDoc}
      */
    @Override
    protected OneWiki getNewSource(final String _name,
                                          final Instance _instance)
    {
        return new OneWiki(_name, _instance);
    }

    /**
     * Class representing one wiki file in the eFaps DataBase.
     *
     */
    public static class OneWiki
        extends AbstractStaticSourceCompiler.AbstractSource
    {
        /**
         * Constructor.
         * @param _name name
         * @param _instance  Instance
         */
        public OneWiki(final String _name,
                       final Instance _instance)
        {
            super(_name, _instance);
        }
    }
}
