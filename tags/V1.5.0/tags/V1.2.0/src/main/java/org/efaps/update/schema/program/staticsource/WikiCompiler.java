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

import org.efaps.admin.EFapsClassNames;
import org.efaps.db.Checkout;
import org.efaps.util.EFapsException;
import org.efaps.wikiutil.export.html.WEMHtml;
import org.efaps.wikiutil.parser.gwiki.GWikiParser;
import org.efaps.wikiutil.parser.gwiki.javacc.ParseException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class WikiCompiler
    extends AbstractStaticSourceCompiler
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected EFapsClassNames getClassName4Type()
    {
        return EFapsClassNames.ADMIN_PROGRAM_WIKI;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected EFapsClassNames getClassName4Type2Type()
    {
        return EFapsClassNames.ADMIN_PROGRAM_WIKI2WIKI;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected EFapsClassNames getClassName4TypeCompiled()
    {
        return EFapsClassNames.ADMIN_PROGRAM_WIKICOMPILED;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getCompiledString(final String _oid)
    {
        final Checkout checkout = new Checkout(_oid);
        final WEMHtml wemhtml = new WEMHtml();
        try {
            GWikiParser.parse(wemhtml, checkout.execute(), "UTF-8");
        } catch (final ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final EFapsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return wemhtml.getHtml();
    }

     /**
      * {@inheritDoc}
      */
    @Override
    protected AbstractSource getNewSource(final String _name,
                                          final String _oid,
                                          final long _id)
    {
        return new OneWiki(_name, _oid, _id);
    }

    /**
     * Class representing one wiki file in the eFaps DataBase.
     *
     */
    protected class OneWiki
        extends AbstractSource
    {

        /**
         * Constructor.
         * @param _name name
         * @param _oid  OID
         * @param _id   ID
         */
        public OneWiki(final String _name,
                       final String _oid,
                       final long _id)
        {
            super(_name, _oid, _id);
        }
    }
}
