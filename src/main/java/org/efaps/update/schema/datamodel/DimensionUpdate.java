/*
 * Copyright 2003 - 2011 The eFaps Team
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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.efaps.ci.CIAdminDataModel;
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
 * Handles the import / update of dimensions for eFaps read from a XML
 * configuration item file.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class DimensionUpdate
    extends AbstractUpdate
{
    /**
     * Default constructor to initialize this dimension update instance for
     * given <code>_url</code>.
     *
     * @param _url URL of the file
     */
    public DimensionUpdate(final URL _url)
    {
        super(_url, "Admin_DataModel_Dimension");
    }

    /**
     * Creates new instance of class {@link DimensionUpdate}.
     *
     * @return new definition instance
     * @see DimensionUpdate
     */
    @Override
    protected AbstractDefinition newDefinition()
    {
        return new DimensionDefinition();
    }

    /**
     * Handles the related unit of measure definition for one version of a
     * dimension.
     */
    public class UoMDefinition
        extends AbstractDefinition
    {

        /**
         * Numerator for this UoM.
         */
        private String numerator;

        /**
         * Denominator for this UoM.
         */
        private String denominator;

        /**
         * Is this UoM the base UoM.
         */
        private boolean base;

        /**
         * {@inheritDoc}
         */
        @Override
        protected void readXML(final List<String> _tags,
                               final Map<String, String> _attributes,
                               final String _text)
        {
            final String value = _tags.get(0);
            if ("numerator".equals(value)) {
                this.numerator = _text;
            } else if ("denominator".equals(value)) {
                this.denominator = _text;
            } else if ("base".equals(value)) {
                this.base = "true".equalsIgnoreCase(_text);
            } else {
                super.readXML(_tags, _attributes, _text);
            }
        }

        /**
         * For given type defined with the instance parameter, this attribute is
         * searched by name. If the attribute exists, the attribute is updated.
         * Otherwise the attribute is created for this type.
         *
         * @param _instance type instance to update with this attribute
         * @throws InstallationException on error
         */
        protected void updateInDB(final Instance _instance)
            throws InstallationException
        {
            try {
                final QueryBuilder queryBldr = new QueryBuilder(CIAdminDataModel.UoM);
                queryBldr.addWhereAttrEqValue(CIAdminDataModel.UoM.Dimension, _instance.getId());
                queryBldr.addWhereAttrEqValue(CIAdminDataModel.UoM.Name, getValue("Name"));
                final InstanceQuery query = queryBldr.getQuery();
                query.executeWithoutAccessCheck();
                Update update;
                if (query.next()) {
                    update = new Update(query.getCurrentValue());
                } else {
                    update = new Insert(CIAdminDataModel.UoM);
                    update.add(CIAdminDataModel.UoM.Dimension, _instance.getId());
                    update.add(CIAdminDataModel.UoM.Name, getValue("Name"));
                }
                update.add(CIAdminDataModel.UoM.Numerator,  this.numerator);
                update.add(CIAdminDataModel.UoM.Denominator,  this.denominator);
                update.executeWithoutAccessCheck();

                if (this.base) {
                    final Update dimUp = new Update(_instance);
                    dimUp.add("BaseUoM", "" + update.getInstance().getId());
                    dimUp.executeWithoutAccessCheck();
                    dimUp.close();
                }

                update.close();
            } catch (final EFapsException e) {
                throw new InstallationException("Dimension can not be updated in DB", e);
            }
        }
    }

    /**
     * Handles the definition of one version for a dimension defined within XML
     * configuration item file.
     */
    public class DimensionDefinition
        extends AbstractDefinition
    {
        /**
         * All attributes of the type are stored in this list.
         *
         * @see #updateInDB
         * @see #addAttribute
         */
        private final List<DimensionUpdate.UoMDefinition> uoms = new ArrayList<DimensionUpdate.UoMDefinition>();

        /**
         * Current read attribute definition instance.
         *
         * @see #readXML(List, Map, String)
         */
        private UoMDefinition curUoM = null;

        /**
         * The description for this Dimension.
         */
        private String description;

        /**
         * {@inheritDoc}
         */
        @Override
        protected void readXML(final List<String> _tags,
                               final Map<String, String> _attributes,
                               final String _text)
        {
            final String value = _tags.get(0);
            if ("description".equals(value)) {
                this.description = _text;
            } else if ("uom".equals(value)) {
                if (_tags.size() == 1) {
                    this.curUoM = new UoMDefinition();
                    this.uoms.add(this.curUoM);
                } else {
                    this.curUoM.readXML(_tags.subList(1, _tags.size()), _attributes, _text);
                }
            }  else {
                super.readXML(_tags, _attributes, _text);
            }
        }

        /**
         * Overwritten to set the description.
         *
         * @see org.efaps.update.AbstractUpdate.AbstractDefinition#createInDB(org.efaps.db.Insert)
         * @param _insert insert to be executed
         * @throws InstallationException on error
         */
        @Override
        protected void createInDB(final Insert _insert)
            throws InstallationException
        {
            final String name = super.getValue("Name");
            try {
                _insert.add(CIAdminDataModel.Dimension.Name, (name == null) ? "-" : name);
            } catch (final EFapsException e) {
                throw new InstallationException("Name attribute could not be defined", e);
            }
            try {
                _insert.add(CIAdminDataModel.Dimension.Description, this.description == null ? "-" : this.description);
            } catch (final EFapsException e) {
                throw new InstallationException("Description attribute could not be defined", e);
            }
            AbstractUpdate.LOG.info("    Insert " + _insert.getInstance().getType().getName() + " '" + name + "'");
            try {
                _insert.executeWithoutAccessCheck();
            } catch (final EFapsException e) {
                throw new InstallationException("Insert failed", e);
            }
            this.instance = _insert.getInstance();
        }

        /**
         * Only in the life cycle step {@link UpdateLifecycle#EFAPS_UPDATE} the
         * {@link #description} and the {@link #uoms} are updated.
         *
         * @param _step         current step of the update life cycle
         * @param _allLinkTypes set of all links
         * @throws InstallationException on error
         * @see #uoms
         */
        @Override
        public void updateInDB(final UpdateLifecycle _step,
                               final Set<Link> _allLinkTypes)
            throws InstallationException
        {
            if (_step == UpdateLifecycle.EFAPS_UPDATE)  {
                addValue(CIAdminDataModel.Dimension.Description.name, this.description);
            }

            super.updateInDB(_step, _allLinkTypes);

            if (_step == UpdateLifecycle.EFAPS_UPDATE)  {
                for (final UoMDefinition uom : this.uoms) {
                    uom.updateInDB(this.instance);
                }
            }
        }
    }
}
