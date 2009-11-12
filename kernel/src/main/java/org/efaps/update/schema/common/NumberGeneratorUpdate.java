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

package org.efaps.update.schema.common;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.efaps.db.Context;
import org.efaps.db.Insert;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.update.AbstractUpdate;
import org.efaps.util.EFapsException;

/**
 * Handles the import / update of system configurations for eFaps read from a
 * XML configuration item file.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class NumberGeneratorUpdate extends AbstractUpdate
{
    /**
     * @param _url URL to the xml file
     */
    public NumberGeneratorUpdate(final URL _url)
    {
        super(_url, "Admin_Common_NumGen");
    }

    /**
     * @see org.efaps.update.AbstractUpdate#newDefinition()
     * @return new NumberGeneratorDefinition
     */
    @Override
    protected AbstractDefinition newDefinition()
    {
        return new NumberGeneratorDefinition();
    }

    /**
     * Definition for the NumberGenerator.
     */
    public class NumberGeneratorDefinition extends AbstractDefinition
    {

        /**
         * Start value for this NumberGenerator.
         */
        private String startvalue;

        /**
         * {@inheritDoc}
         */
        @Override()
        protected void readXML(final List<String> _tags,
                               final Map<String, String> _attributes,
                               final String _text)
        {
            final String value = _tags.get(0);
            if ("format".equals(value)) {
                addValue("Format", _text);
            } else if ("startvalue".equals(value)) {
                this.startvalue = _text;
            } else  {
                super.readXML(_tags, _attributes, _text);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void createInDB(final Insert _insert)
            throws EFapsException
        {
            _insert.add("Format", getValue("Format"));
            super.createInDB(_insert);
            final Context context = Context.getThreadContext();
            final ConnectionResource con = context.getConnectionResource();
            try {
                final String name = "numgen_" + ((Long) this.instance.getId()).toString() + "_seq";
                if (!Context.getDbType().existsSequence(con.getConnection(), name)) {
                    Context.getDbType().createSequence(con.getConnection(), name, this.startvalue);
                }
            } catch (final SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    }
}
