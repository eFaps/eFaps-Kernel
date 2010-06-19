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

package org.efaps.update.schema.db;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.efaps.ci.CIDB;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.db.SelectBuilder;
import org.efaps.db.Update;
import org.efaps.db.store.Store;
import org.efaps.update.AbstractUpdate;
import org.efaps.update.UpdateLifecycle;
import org.efaps.update.util.InstallationException;
import org.efaps.util.EFapsException;

/**
 * Handles the import / update of stores for eFaps read from a XML configuration
 * item file.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class StoreUpdate
    extends AbstractUpdate
{

    /**
     * @param _url url to the XML file
     */
    public StoreUpdate(final URL _url)
    {
        super(_url, "DB_Store");
    }

    /**
     * Creates a new definition instance used from
     * {@link #readXML(List, Map, String)}.
     *
     * @return new definition instance
     */
    @Override()
    protected AbstractDefinition newDefinition()
    {
        return new StoreDefinition();
    }

    /**
     * Definition for a Resource.
     */
    protected final class ResourceDefinition
        extends AbstractDefinition
    {

        /**
         * Name of the class.
         */
        private final String clazz;

        /**
         * @param _class name of the class
         * @param _compress compress
         */
        private ResourceDefinition(final String _class,
                                   final String _compress)
        {
            this.clazz = _class;
            getProperties().put(Store.PROPERTY_COMPRESS, _compress);
        }

        /**
         * Read the xml.
         *
         * @see AbstractDefinition#readXML(List, Map, String)
         * @param _tags list of tags
         * @param _attributes attributes
         * @param _text text
         */
        @Override()
        protected void readXML(final List<String> _tags,
                               final Map<String, String> _attributes,
                               final String _text)
        {
            super.readXML(_tags, _attributes, _text);
        }
    }

    /**
     * Definition for a store.
     *
     */
    protected class StoreDefinition
        extends AbstractDefinition
    {

        /**
         * Resource definition for htis store.
         */
        private ResourceDefinition resource;

        /**
         * Read the XML.
         *
         * @param _tags List of tags
         * @param _attributes map of attributes
         * @param _text text
         */
        @Override
        protected void readXML(final List<String> _tags,
                               final Map<String, String> _attributes,
                               final String _text)
        {
            final String value = _tags.get(0);
            if ("resource".equals(value)) {
                if (_tags.size() == 1) {
                    this.resource = new ResourceDefinition(_attributes.get("class"),
                                                           _attributes.get("compress"));
                } else {
                    this.resource.readXML(_tags.subList(1, _tags.size()), _attributes, _text);
                }
            } else if ("jndi-name".equals(value)) {
                getProperties().put(Store.PROPERTY_JNDINAME, _text);
            } else {
                super.readXML(_tags, _attributes, _text);
            }
        }

        /**
         * Update the store in the database.
         *
         * @param _step current life cycle update step
         * @param _allLinkTypes set of all links
         * @throws InstallationException on error
         * @see AbstractUpdate.AbstractDefinition#updateInDB(UpdateLifecycle,Set)
         */
        @Override()
        public void updateInDB(final UpdateLifecycle _step,
                               final Set<Link> _allLinkTypes)
            throws InstallationException, EFapsException
        {
            super.updateInDB(_step, _allLinkTypes);
            if (_step == UpdateLifecycle.EFAPS_UPDATE) {
                setSourceInDB();
            }
        }

        /**
         * @throws EFapsException o error
         *
         */
        private void setSourceInDB()
            throws EFapsException
        {
            boolean old = false;
            final Update update;
            final QueryBuilder queryBldr = new QueryBuilder(CIDB.Store2Resource);
            queryBldr.addWhereAttrEqValue(CIDB.Store2Resource.From, this.instance.getId());
            final MultiPrintQuery multi = queryBldr.getPrint();
            final SelectBuilder sel = new SelectBuilder().linkto(CIDB.Store2Resource.To).oid();
            multi.addSelect(sel);
            multi.executeWithoutAccessCheck();
            if (multi.next()) {
                final Instance resourceInst = Instance.get(multi.<String> getSelect(sel));
                update = new Update(resourceInst);
                old = true;
            } else {
                update = new Insert(CIDB.Resource);
            }
            update.add(CIDB.Resource.Name, this.resource.clazz);
            update.executeWithoutAccessCheck();
            setPropertiesInDb(update.getInstance(), this.resource.getProperties());

            if (!old) {
                final Insert insert = new Insert(CIDB.Store2Resource);
                insert.add(CIDB.Store2Resource.From, ((Long) this.instance.getId()).toString());
                insert.add(CIDB.Store2Resource.To, ((Long) update.getInstance().getId()).toString());
                insert.executeWithoutAccessCheck();
                insert.close();
            }
            update.close();
        }
    }
}
