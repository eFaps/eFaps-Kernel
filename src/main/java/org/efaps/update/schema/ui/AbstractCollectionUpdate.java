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

package org.efaps.update.schema.ui;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.efaps.admin.event.EventType;
import org.efaps.ci.CIAdminUserInterface;
import org.efaps.db.Delete;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.update.AbstractUpdate;
import org.efaps.update.LinkInstance;
import org.efaps.update.UpdateLifecycle;
import org.efaps.update.event.Event;
import org.efaps.update.util.InstallationException;
import org.efaps.util.EFapsException;



/**
 * This class imports/updates a Form or a Table using the
 * <code>org.apache.commons.digester.Digester</code> to create objects and to
 * execute methods.
 *
 * @author The eFaps Team
 * @version $Id$
 *
 */
public abstract class AbstractCollectionUpdate
    extends AbstractUpdate
{
    /** Link from field to icon. */
    private static final Link LINKFIELD2ICON = new Link("Admin_UI_LinkIcon", "From", "Admin_UI_Image", "To");

    /** Link from field to table as target. */
    private static final Link LINK2TARGETTABLE = new Link("Admin_UI_LinkTargetTable", "From", "Admin_UI_Table", "To");

    /**
     * @param _url URL of the file
     * @param _typeName name of the type
     */
    protected AbstractCollectionUpdate(final URL _url, final String _typeName)
    {
        super(_url, _typeName);
    }

    /**
     * @param _url              url of the file
     * @param _typeName         name of the type
     * @param _allLinkTypes     set of all links
     */
    protected AbstractCollectionUpdate(final URL _url,
                                       final String _typeName,
                                       final Set<Link> _allLinkTypes)
    {
        super(_url, _typeName, _allLinkTypes);
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
     * Class for defining a field.
     */
    private final class FieldDefinition extends AbstractDefinition
    {

        /** Name of the field. */
        private final String name;

        /** Icon of the field. */
        private String icon = null;

        /** set the character of the field. */
        private final String character;

        /**
         *
         * @param _name         Name of the field
         * @param _character    charachter of the field
         */
        private FieldDefinition(final String _name, final String _character)
        {
            this.name = _name;
            this.character = _character;
        }

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
            if ("evaluate".equals(value)) {
                if (_tags.size() == 1) {
                    this.events.add(new Event(_attributes.get("name"), EventType.UI_TABLE_EVALUATE, _attributes
                                    .get("program"), _attributes.get("method"), _attributes.get("index")));
                } else if ((_tags.size() == 2) && "property".equals(_tags.get(1))) {
                    this.events.get(this.events.size() - 1).addProperty(_attributes.get("name"), _text);
                } else {
                    super.readXML(_tags, _attributes, _text);
                }
            } else if ("icon".equals(value)) {
                this.icon = _text;
            } else if ("table".equals(value)) {
                // assigns a table as target for this field definition
                addLink(AbstractCollectionUpdate.LINK2TARGETTABLE, new LinkInstance(_text));
            } else if ("trigger".equals(value)) {
                if (_tags.size() == 1) {
                    this.events.add(new Event(_attributes.get("name"), EventType.valueOf(_attributes.get("event")),
                                    _attributes.get("program"), _attributes.get("method"), _attributes.get("index")));
                } else if ((_tags.size() == 2) && "property".equals(_tags.get(1))) {
                    this.events.get(this.events.size() - 1).addProperty(_attributes.get("name"), _text);
                } else {
                    super.readXML(_tags, _attributes, _text);
                }
            } else {
                super.readXML(_tags, _attributes, _text);
            }
        }

        /**
         * Returns a string representation with values of all instance variables
         * of a field.
         *
         * @return string representation of this definition of a column
         */
        @Override
        public String toString()
        {
            return new ToStringBuilder(this).append("name", this.name).append("properties", getProperties()).toString();
        }
    }

    /**
     * Class used to define a collection.
     *
     */
    protected class Definition extends AbstractDefinition
    {
        /** All fields for the collection are stored in this variable. */
        private final List<AbstractCollectionUpdate.FieldDefinition> fields
                                                            = new ArrayList<AbstractCollectionUpdate.FieldDefinition>();

        /**
         * Current read field definition.
         *
         * @see #readXML(List, Map, String)
         */
        private FieldDefinition curField = null;

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
            if ("field".equals(value)) {
                if (_tags.size() == 1) {
                    this.curField = new FieldDefinition(_attributes.get("name"), _attributes.get("character"));
                    this.fields.add(this.curField);
                } else {
                    this.curField.readXML(_tags.subList(1, _tags.size()), _attributes, _text);
                }
            } else {
                super.readXML(_tags, _attributes, _text);
            }
        }

        /**
         * Updates / creates the instance in the database. Only the fields are
         * also updated for collection defined through this definition.
         *
         * @param _step             current update step
         * @param _allLinkTypes     set of all type of links
         * @throws EFapsException on error
         * @see #setFieldsInDB
         */
        @Override()
        public void updateInDB(final UpdateLifecycle _step,
                               final Set<Link> _allLinkTypes)
            throws InstallationException, EFapsException
        {
            super.updateInDB(_step, _allLinkTypes);
            if (_step == UpdateLifecycle.EFAPS_UPDATE)  {
                setFieldsInDB();
            }
        }

        /**
         * The fields for this collection are created and / or updated in the
         * database.
         * TODO the deletion of existing fields is nested! Thats not the best idea.
         * @throws EFapsException on error
         */
        protected void setFieldsInDB() throws EFapsException
        {
            // cleanup fields (remove all fields from table)
            final SearchQuery query = new SearchQuery();
            query.setExpand(this.instance, "Admin_UI_Field\\Collection");
            query.addSelect("OID");
            query.executeWithoutAccessCheck();
            while (query.next()) {
                final Instance field = Instance.get((String) query.get("OID"));
                setPropertiesInDb(field, null);
                removeLinksInDB(field, AbstractCollectionUpdate.LINKFIELD2ICON);
                removeLinksInDB(field, AbstractCollectionUpdate.LINK2TARGETTABLE);
                //remove events
                final SearchQuery eventQuery = new SearchQuery();
                eventQuery.setExpand(field, "Admin_Event_Definition\\Abstract");
                eventQuery.addSelect("OID");
                eventQuery.execute();
                while (eventQuery.next()) {
                    final Instance event = Instance.get((String) eventQuery.get("OID"));
                    final SearchQuery propQuery = new SearchQuery();
                    propQuery.setExpand(event, "Admin_Common_Property\\Abstract");
                    propQuery.addSelect("OID");
                    propQuery.execute();
                    while (propQuery.next()) {
                        (new Delete((String) propQuery.get("OID"))).executeWithoutAccessCheck();
                    }
                    (new Delete(event)).executeWithoutAccessCheck();
                }
                (new Delete(field)).executeWithoutAccessCheck();
            }
            query.close();

            // append new fields
            for (final FieldDefinition field : this.fields) {
                Insert insert;
                if ("Command".equals(field.character)) {
                    insert = new Insert(CIAdminUserInterface.FieldCommand);
                } else if ("Target".equals(field.character)) {
                    insert = new Insert(CIAdminUserInterface.FieldTable);
                } else if ("Heading".equals(field.character)) {
                    insert = new Insert(CIAdminUserInterface.FieldHeading);
                } else if ("Group".equals(field.character)) {
                    insert = new Insert(CIAdminUserInterface.FieldGroup);
                } else if ("Set".equals(field.character)) {
                    insert = new Insert(CIAdminUserInterface.FieldSet);
                } else if ("Classification".equals(field.character)) {
                    insert = new Insert(CIAdminUserInterface.FieldClassification);
                } else {
                    insert = new Insert(CIAdminUserInterface.Field);
                }

                insert.add("Collection", "" + this.instance.getId());
                insert.add("Name", field.name);
                insert.executeWithoutAccessCheck();
                setPropertiesInDb(insert.getInstance(), field.getProperties());

                if (field.icon != null) {
                    final Set<LinkInstance> iconset = new HashSet<LinkInstance>();
                    iconset.add(new LinkInstance(field.icon));
                    setLinksInDB(insert.getInstance(), AbstractCollectionUpdate.LINKFIELD2ICON, iconset);
                }

                // link to table
                setLinksInDB(insert.getInstance(), AbstractCollectionUpdate.LINK2TARGETTABLE,
                             field.getLinks(AbstractCollectionUpdate.LINK2TARGETTABLE));

                // append events
                for (final Event event : field.getEvents()) {
                    final Instance newInstance = event.updateInDB(insert.getInstance(), field.name);
                    setPropertiesInDb(newInstance, event.getProperties());
                }
            }
        }

        /**
         * Adds a new field to this definition of the table.
         *
         * @param _field new field to add to this table
         * @see #fields
         */
        public void addField(final FieldDefinition _field)
        {
            this.fields.add(_field);
        }

        /**
         * Returns a string representation with values of all instance variables
         * of a field.
         *
         * @return string representation of this definition of a column
         */
        @Override()
        public String toString()
        {
            return new ToStringBuilder(this).appendSuper(super.toString()).append("fields", this.fields).toString();
        }
    }
}
