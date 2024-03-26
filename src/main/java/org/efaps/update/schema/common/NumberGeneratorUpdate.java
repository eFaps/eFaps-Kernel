/*
 * Copyright © 2003 - 2024 The eFaps Team (-)
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
 */
package org.efaps.update.schema.common;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.efaps.db.Context;
import org.efaps.db.Insert;
import org.efaps.update.AbstractUpdate;
import org.efaps.update.Install.InstallFile;
import org.efaps.update.util.InstallationException;
import org.efaps.util.EFapsException;

/**
 * Handles the import / update of system configurations for eFaps read from a
 * XML configuration item file.
 *
 * @author The eFaps Team
 */
public class NumberGeneratorUpdate
    extends AbstractUpdate
{

    /**
     * Instantiates a new number generator update.
     *
     * @param _installFile the install file
     */
    public NumberGeneratorUpdate(final InstallFile _installFile)
    {
        super(_installFile, "Admin_Common_NumGen");
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
        private long startvalue;

        /**
         * {@inheritDoc}
         * @throws EFapsException
         */
        @Override
        protected void readXML(final List<String> _tags,
                               final Map<String, String> _attributes,
                               final String _text)
            throws EFapsException
        {
            final String value = _tags.get(0);
            if ("format".equals(value)) {
                addValue("Format", _text);
            } else if ("startvalue".equals(value)) {
                this.startvalue = Long.parseLong(_text);
            } else {
                super.readXML(_tags, _attributes, _text);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void createInDB(final Insert _insert)
            throws InstallationException
        {
            try  {
                _insert.add("Format", getValue("Format"));
            } catch (final EFapsException e)  {
                throw new InstallationException("Format could not be defined", e);
            }
            super.createInDB(_insert);
            final Connection con;
            try {
                con = Context.getConnection();
            } catch (final EFapsException e) {
                throw new InstallationException("Connection resource could not be fetched", e);
            }
            final String name = "numgen_" + ((Long) getInstance().getId()).toString() + "_seq";
            try {
                if (!Context.getDbType().existsSequence(con, name)) {
                    Context.getDbType().createSequence(con, name, this.startvalue);
                }
            } catch (final SQLException e) {
                throw new InstallationException("Sequence '" + name + "' could not be created", e);
            } finally {
                try {
                    if (con != null && con.isClosed()) {
                        con.close();
                    }
                } catch (final SQLException e) {
                    throw new InstallationException("Cannot read a type for an attribute.", e);
                }
            }
        }
    }
}
