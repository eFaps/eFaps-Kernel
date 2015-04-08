/*
 * Copyright 2003 - 2013 The eFaps Team
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

package org.efaps.update.schema.datamodel;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Dimension;
import org.efaps.admin.datamodel.Type;
import org.efaps.ci.CIAdminDataModel;
import org.efaps.db.Insert;
import org.efaps.db.InstanceQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.db.Update;
import org.efaps.update.AbstractUpdate;
import org.efaps.update.Install.InstallFile;
import org.efaps.update.UpdateLifecycle;
import org.efaps.update.util.InstallationException;
import org.efaps.util.EFapsException;

/**
 * Handles the import / update of status groups for eFaps read from a XML
 * configuration item file. Remark: The StatusGroupDefinition is actual just a
 * normal Admin_DataModel_Type and the stati belonging to it are instances of
 * this Type. That leads to the problem that if a StatusGroupUpdate is executed
 * in one version the cache for the Types must be reloaded.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class StatusGroupUpdate
    extends AbstractUpdate
{

    /**
     * Default constructor to initialize this status group instance for given
     * <code>_url</code>.
     *
     * @param _url url to the file
     */
    public StatusGroupUpdate(final InstallFile _installFile)
    {
        super(_installFile, "Admin_DataModel_Type");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected AbstractDefinition newDefinition()
    {
        return new StatusGroupDefinition();
    }

    /**
     * Definition of one status.
     */
    public class StatusDefintion
    {

        /**
         * Key of this status.
         */
        private final String key;

        /**
         * Description for this status.
         */
        private String description;

        /**
         * @param _key key
         */
        public StatusDefintion(final String _key)
        {
            this.key = _key;
        }

        /**
         * @param _description description
         */
        public void setDescription(final String _description)
        {
            this.description = _description;
        }

        /**
         * @param _typeName name of the type
         * @throws EFapsException on error during insert
         */
        public void updateInDB(final String _typeName)
            throws EFapsException
        {
            final QueryBuilder queryBldr = new QueryBuilder(Type.get(_typeName));
            queryBldr.addWhereAttrEqValue("Key", this.key);
            final InstanceQuery query = queryBldr.getQuery();
            query.executeWithoutAccessCheck();
            final Update update;
            if (query.next()) {
                update = new Update(query.getCurrentValue());
            } else {
                update = new Insert(_typeName);
            }
            update.add("Key", this.key);
            update.add("Description", this.description);
            update.executeWithoutAccessCheck();
        }
    }

    /**
     * Class for the definition of the type.
     */
    public class StatusGroupDefinition
        extends AbstractDefinition
    {

        /**
         * Name of the parent type.
         */
        private String parentType;

        /**
         * current definition.
         */
        private StatusDefintion currentStatus;

        /**
         * Set of all definitions.
         */
        private final Set<StatusGroupUpdate.StatusDefintion> stati = new HashSet<StatusGroupUpdate.StatusDefintion>();

        /**
         *
         * @param _tags current path as list of single tags
         * @param _attributes attributes for current path
         * @param _text content for current path
         * @throws EFapsException on error
         */
        @Override
        protected void readXML(final List<String> _tags,
                               final Map<String, String> _attributes,
                               final String _text)
            throws EFapsException
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
         * @param _step current step in the Update Lifecycle
         * @param _allLinkTypes set of all links
         * @throws InstallationException on error
         * @see #parentType
         * @see #attributes
         */
        @Override
        public void updateInDB(final UpdateLifecycle _step,
                               final Set<Link> _allLinkTypes)
            throws InstallationException
        {
            try {
                if (_step == UpdateLifecycle.STATUSGROUP_CREATE) {
                    super.updateInDB(UpdateLifecycle.EFAPS_CREATE, _allLinkTypes);
                }

                if (_step == UpdateLifecycle.STATUSGROUP_UPDATE) {
                    // set the id of the parent type (if defined)
                    if (this.parentType != null && this.parentType.length() > 0) {
                        final QueryBuilder queryBldr = new QueryBuilder(CIAdminDataModel.Type);
                        queryBldr.addWhereAttrEqValue(CIAdminDataModel.Type.Name, this.parentType);
                        final InstanceQuery query = queryBldr.getQuery();
                        query.executeWithoutAccessCheck();
                        if (query.next()) {
                            addValue("ParentType", "" + query.getCurrentValue().getId());
                        } else {
                            addValue("ParentType", null);
                        }
                    } else {
                        addValue("ParentType", null);
                    }
                    super.updateInDB(UpdateLifecycle.EFAPS_UPDATE, _allLinkTypes);
                }

                if (_step == UpdateLifecycle.STATUS_CREATE) {
                    // before the Stati can be created it must be checked if the
                    // type (StatusGroup)
                    // is already cached.
                    if (Type.get(getValue("Name")) == null) {
                        Type.initialize(StatusGroupUpdate.class);
                        Dimension.initialize(StatusGroupUpdate.class);
                        Attribute.initialize(StatusGroupUpdate.class);
                    }
                    for (final StatusDefintion status : this.stati) {
                        status.updateInDB(getValue("Name"));
                    }
                }
            } catch (final EFapsException e) {
                throw new InstallationException(" SQLTable can not be updated", e);
            }
        }
    }
}
