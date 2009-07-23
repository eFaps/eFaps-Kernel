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

package org.efaps.update.datamodel;

import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.update.AbstractUpdate;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class StatusGroupUpdate extends AbstractUpdate
{
    /**
     * @param _url url to the file
     */
    public StatusGroupUpdate(final URL _url)
    {
        super(_url, "Admin_DataModel_Type");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected AbstractDefinition newDefinition()
    {
        return new StatusGroupDefinition();
    }

    public class StatusDefintion
    {

        private final String key;
        private String description;

        /**
         * @param _key
         */
        public StatusDefintion(final String _key)
        {
            this.key = _key;
        }

        /**
         * @param _description
         */
        public void setDescription(final String _description)
        {
            this.description = _description;
        }

        /**
         * @param _typeName
         * @throws EFapsException
         */
        public void updateInDB(final String _typeName) throws EFapsException
        {
            //TODO remove or update of existing!! In that way they are always added!!
            final Insert insert = new Insert(_typeName);
            insert.add("Key", this.key);
            insert.add("Description", this.description);
            insert.executeWithoutAccessCheck();
        }
    }

    /**
     * Class for the definition of the type.
     */
    public class StatusGroupDefinition extends AbstractDefinition
    {
        private String parentType;
        private StatusDefintion currentStatus;
        private final Set<StatusDefintion> stati = new HashSet<StatusDefintion>();

        @Override
        protected void readXML(final List<String> _tags, final Map<String, String> _attributes, final String _text)
        {
            final String value = _tags.get(0);
            if ("status".equals(value)) {
                if (_tags.size() == 1) {
                    this.currentStatus = new StatusDefintion(_attributes.get("key"));
                    this.stati.add(this.currentStatus);
                } else if (_tags.size() == 2 && "description".equals(_tags.get(1))) {
                    this.currentStatus.setDescription(_text);
                }
            } else if ("parent".equals(value)) {
                this.parentType = _text;
            } else {
                super.readXML(_tags, _attributes, _text);
            }
        }

        /**
         * If a parent type in {@link #parentType} is defined, the type id is
         * evaluated and added to attributes to update (if no parent type is
         * defined, the parent type id is set to <code>null</code>). After the
         * type is updated (or inserted if needed), all statis must be updated.
         *
         * @param _allLinkTypes set of all links
         * @throws EFapsException on error
         *
         * @see #parentType
         * @see #attributes
         */
        @Override
        public void updateInDB(final Set<Link> _allLinkTypes)
            throws EFapsException
        {
            // set the id of the parent type (if defined)
            if ((this.parentType != null) && (this.parentType.length() > 0)) {
                final SearchQuery query = new SearchQuery();
                query.setQueryTypes("Admin_DataModel_Type");
                query.addWhereExprEqValue("Name", this.parentType);
                query.addSelect("OID");
                query.executeWithoutAccessCheck();
                if (query.next()) {
                    final Instance instance = Instance.get((String) query.get("OID"));
                    addValue("ParentType", "" + instance.getId());
                } else {
                    addValue("ParentType", null);
                }
                query.close();
            } else {
                addValue("ParentType", null);
            }

            super.updateInDB(_allLinkTypes);

            for (final StatusDefintion status : this.stati) {
                status.updateInDB(getValue("Name"));
            }
        }
    }
}
