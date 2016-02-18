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

package org.efaps.update.schema.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.EnumUtils;
import org.efaps.admin.user.Company;
import org.efaps.ci.CIAdminCommon;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.InstanceQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.db.Update;
import org.efaps.update.AbstractUpdate;
import org.efaps.update.Install.InstallFile;
import org.efaps.update.UpdateLifecycle;
import org.efaps.update.util.InstallationException;
import org.efaps.util.EFapsException;

/**
 * Handles the import / update of system configurations for eFaps read from a
 * XML configuration item file.
 *
 * @author The eFaps Team
 */
public class SystemConfigurationUpdate
    extends AbstractUpdate
{

    /**
     * The Enum AttributeUpdate.
     */
    public enum AttributeUpdate
    {
        /** The default. */
        DEFAULT,
        /** Force update. */
        FORCE;
    }

    /**
     * Default constructor to initialize this system configuration update
     * instance for given <code>_url</code>.
     *
     * @param _installFile the install file
     */
    public SystemConfigurationUpdate(final InstallFile _installFile)
    {
        super(_installFile, "Admin_Common_SystemConfiguration");
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
     *
     * @author The eFaps Team
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
         * Update defintion.
         */
        private AttributeUpdate attributeUpdate = AttributeUpdate.DEFAULT;

        /**
         * Read xml.
         *
         * @param _tags         current path as list of single tags
         * @param _attributes   attributes for current path
         * @param _text         content for current path
         * @throws EFapsException on error
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
         * Update in db.
         *
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
            final boolean attrExists = query.next();

            Update update = null;
            if (attrExists && AttributeUpdate.FORCE.equals(getAttributeUpdate())) {
                update = new Update(query.getCurrentValue());
            } else if (!attrExists) {
                update = new Insert(CIAdminCommon.SystemConfigurationAttribute);
                update.add(CIAdminCommon.SystemConfigurationAttribute.AbstractLink, _instance.getId());
                update.add(CIAdminCommon.SystemConfigurationAttribute.Key, this.key);
            }
            if (update != null) {
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
         * Getter method for the instance variable {@link #attributeUpdate}.
         *
         * @return value of instance variable {@link #attributeUpdate}
         */
        public AttributeUpdate getAttributeUpdate()
        {
            return this.attributeUpdate;
        }

        /**
         * Setter method for instance variable {@link #attributeUpdate}.
         *
         * @param _attributeUpdate value for instance variable {@link #attributeUpdate}
         */
        public void setAttributeUpdate(final AttributeUpdate _attributeUpdate)
        {
            this.attributeUpdate = _attributeUpdate;
        }
    }

    /**
     * Handles the definition of one version for an system configuration
     * defined within XML configuration item file.
     *
     * @author The eFaps Team
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
         * Read xml.
         *
         * @param _tags         current path as list of single tags
         * @param _attributes   attributes for current path
         * @param _text         content for current path
         * @throws EFapsException on error
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
                    if (_attributes.containsKey("update")) {
                        this.curAttr.setAttributeUpdate(EnumUtils.getEnum(AttributeUpdate.class,
                                        _attributes.get("update").toUpperCase()));
                    }
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
