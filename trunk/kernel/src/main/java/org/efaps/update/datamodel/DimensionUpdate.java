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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.db.Update;
import org.efaps.update.AbstractUpdate;
import org.efaps.update.datamodel.TypeUpdate.TypeDefinition;
import org.efaps.util.EFapsException;

/**
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class DimensionUpdate extends AbstractUpdate
{


     /**
     *
     * @param _url URL of the file
     */
    public DimensionUpdate(final URL _url)
    {
        super(_url, "Admin_DataModel_Dimension");
    }

    /**
     * Creates new instance of class {@link TypeDefinition}.
     *
     * @return new definition instance
     * @see TypeDefinition
     */
    @Override
    protected AbstractDefinition newDefinition()
    {
        return new DimensionDefinition();
    }

    /**
     * The class defines an attribute of a type.
     */
    public class UoMDefinition extends AbstractDefinition
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
         * @see org.efaps.update.AbstractUpdate.AbstractDefinition#readXML(java.util.List, java.util.Map, java.lang.String)
         * @param _tags         list of the tags
         * @param _attributes   attributes
         * @param _text         text
         */
        @Override
        protected void readXML(final List<String> _tags, final Map<String, String> _attributes, final String _text)
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
         * @throws EFapsException on error
         */
        protected void updateInDB(final Instance _instance)
                throws EFapsException
        {
            final SearchQuery query = new SearchQuery();
            query.setExpand(_instance, "Admin_DataModel_UoM\\Dimension");
            query.addWhereExprEqValue("Name", getValue("Name"));
            query.addSelect("OID");
            query.executeWithoutAccessCheck();
            Update update;

            if (query.next()) {
                update = new Update((String) query.get("OID"));
            } else {
                update = new Insert("Admin_DataModel_UoM");
                update.add("Dimension", "" + _instance.getId());
                update.add("Name", getValue("Name"));
            }
            query.close();

            update.add("Numerator",  this.numerator);
            update.add("Denominator",  this.denominator);

            update.executeWithoutAccessCheck();

            if (this.base) {
                final Update dimUp = new Update(_instance);
                dimUp.add("BaseUoM", "" + update.getInstance().getId());
                dimUp.executeWithoutAccessCheck();
                dimUp.close();
            }

            update.close();
        }
    }


    /**
     * Class for the definition of the type.
     */
    public class DimensionDefinition extends AbstractDefinition
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
         * @see org.efaps.update.AbstractUpdate.AbstractDefinition#readXML(java.util.List, java.util.Map, java.lang.String)
         * @param _tags         tags
         * @param _attributes   attributes
         * @param _text         text
         */
        @Override
        protected void readXML(final List<String> _tags, final Map<String, String> _attributes, final String _text)
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

        @Override
        protected void createInDB(final Insert _insert) throws EFapsException
        {
            final String name = super.getValue("Name");
            _insert.add("Name", (name == null) ? "-" : name);
            _insert.add("Description", (this.description == null) ? "-" : this.description);
            _insert.executeWithoutAccessCheck();
            this.instance = _insert.getInstance();
        }

        /**
         * If a parent type in {@link #parentType} is defined, the type id is
         * evaluated and added to attributes to update (if no parent type is
         * defined, the parent type id is set to <code>null</code>). After the
         * type is updated (or inserted if needed), all attributes must be
         * updated.
         * @param _allLinkTypes set of all links
         * @throws EFapsException on error
         *
         * @see #parentType
         * @see #uoms
         */
        @Override
        public void updateInDB(final Set<Link> _allLinkTypes)
                throws EFapsException
        {
            addValue("Description", this.description);

            super.updateInDB(_allLinkTypes);

            for (final UoMDefinition uom : this.uoms) {
                uom.updateInDB(this.instance);
            }
        }
    }
}
