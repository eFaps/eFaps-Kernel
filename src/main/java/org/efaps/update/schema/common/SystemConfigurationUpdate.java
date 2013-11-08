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

package org.efaps.update.schema.common;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.efaps.admin.user.Company;
import org.efaps.ci.CIAdminCommon;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.InstanceQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.db.Update;
import org.efaps.update.AbstractUpdate;
import org.efaps.update.UpdateLifecycle;
import org.efaps.update.util.InstallationException;
import org.efaps.util.EFapsException;

/**
 * Handles the import / update of system configurations for eFaps read from a
 * XML configuration item file.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class SystemConfigurationUpdate
    extends AbstractUpdate
{
    /**
     * Default constructor to initialize this system configuration update
     * instance for given <code>_url</code>.
     *
     * @param _url        URL of the file
     */
    public SystemConfigurationUpdate(final URL _url)
    {
        super(_url, "Admin_Common_SystemConfiguration");
    }

    /**
     * Creates new instance of class {@link Definition}.
     *
     * @return new definition instance
     * @see Definition
     */
    @Override
    protected AbstractDefinition newDefinition()
    {
        return new Definition();
    }

    /**
     * Handles the definition of one version for an attribute definition
     * defined within XML configuration item file.
     */
    public class AttributeDefinition
        extends AbstractDefinition
    {
        /**
         * Key of a property attribute.
         */
        private String key;

        /**
         * Value of a property attribute.
         */
        private String value;

        /**
         * Description of a property attribute.
         */
        private String description;

        /**
         * UUID of a Company. Optional!
         */
        private String companyUUID;

        /**
         *
         * @param _tags         current path as list of single tags
         * @param _attributes   attributes for current path
         * @param _text         content for current path
         * @throws EFapsException
         */
        @Override
        protected void readXML(final List<String> _tags,
                               final Map<String, String> _attributes,
                               final String _text)
            throws EFapsException
        {
            final String tmpValue = _tags.get(0);
            if ("key".equals(tmpValue)) {
                this.key = _text;
            } else if ("value".equals(tmpValue)) {
                this.value = _text;
            } else if ("description".equals(tmpValue)) {
                this.description = _text;
            } else if ("company".equals(tmpValue)) {
                this.companyUUID = _text;
            } else {
                super.readXML(_tags, _attributes, _text);
            }
        }

        /**
         * @param _instance     instance to update
         * @throws EFapsException if update failed
         */
        public void updateInDB(final Instance _instance)
            throws EFapsException
        {
            Company company = null;
            if (this.companyUUID != null) {
                company = Company.get(UUID.fromString(this.companyUUID));
            }
            final QueryBuilder queryBldr = new QueryBuilder(CIAdminCommon.SystemConfigurationAttribute);
            queryBldr.addWhereAttrEqValue(CIAdminCommon.SystemConfigurationAttribute.Key, this.key);
            queryBldr.addWhereAttrEqValue(CIAdminCommon.SystemConfigurationAttribute.AbstractLink, _instance);
            if (company == null) {
                queryBldr.addWhereAttrEqValue(CIAdminCommon.SystemConfigurationAttribute.CompanyLink, 0);
            } else {
                queryBldr.addWhereAttrEqValue(CIAdminCommon.SystemConfigurationAttribute.CompanyLink, company.getId());
            }
            final InstanceQuery query = queryBldr.getQuery();
            query.executeWithoutAccessCheck();
            Update update = null;
            if (query.next()) {
                update = new Update(query.getCurrentValue());
            } else {
                update = new Insert(CIAdminCommon.SystemConfigurationAttribute);
                update.add(CIAdminCommon.SystemConfigurationAttribute.AbstractLink, _instance.getId());
                update.add(CIAdminCommon.SystemConfigurationAttribute.Key, this.key);
            }
            if (company != null) {
                update.add(CIAdminCommon.SystemConfigurationAttribute.CompanyLink, company.getId());
            } else {
                update.add(CIAdminCommon.SystemConfigurationAttribute.CompanyLink, 0);
            }
            update.add(CIAdminCommon.SystemConfigurationAttribute.Value, this.value);
            update.add(CIAdminCommon.SystemConfigurationAttribute.Description, this.description);
            update.executeWithoutAccessCheck();
        }
    }

    /**
     * Handles the definition of one version for an system configuration
     * defined within XML configuration item file.
     */
    public class Definition
        extends AbstractDefinition
    {
        /**
         * Current parsed attribute definition.
         *
         * @see #readXML(List, Map, String)
         */
        private AttributeDefinition curAttr;

        /**
         * List of all read attribute definition.
         *
         * @see #readXML(List, Map, String)
         */
        private final List<SystemConfigurationUpdate.AttributeDefinition> attributes
            = new ArrayList<SystemConfigurationUpdate.AttributeDefinition>();

        /**
         *
         * @param _tags         current path as list of single tags
         * @param _attributes   attributes for current path
         * @param _text         content for current path
         * @throws EFapsException
         */
        @Override
        protected void readXML(final List<String> _tags,
                               final Map<String, String> _attributes,
                               final String _text)
            throws EFapsException
        {
            final String value = _tags.get(0);
            if ("attribute".equals(value)) {
                if (_tags.size() == 1) {
                    this.curAttr = new AttributeDefinition();
                    this.attributes.add(this.curAttr);
                } else {
                    this.curAttr.readXML(_tags.subList(1, _tags.size()), _attributes, _text);
                }
            } else {
                super.readXML(_tags, _attributes, _text);
            }
        }

        /**
         * If the current life cycle <code>step</code> is
         * {@link UpdateLifecycle#EFAPS_UPDATE EFAPS_UPDATE}, the
         * {@link #attributes} are updated.
         *
         * @param _step             current life cycle update step
         * @param _allLinkTypes     all link types to update
         * @throws InstallationException if update failed
         * @see #attributes
         */
        @Override
        public void updateInDB(final UpdateLifecycle _step,
                               final Set<Link> _allLinkTypes)
            throws InstallationException
        {
            super.updateInDB(_step, _allLinkTypes);
            try {
                if (_step == UpdateLifecycle.EFAPS_UPDATE)  {
                    for (final AttributeDefinition attr : this.attributes) {
                        attr.updateInDB(getInstance());
                    }
                }
            } catch (final EFapsException e) {
                throw new InstallationException(" Type can not be updated", e);
            }
        }
    }
}
