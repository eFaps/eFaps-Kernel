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

package org.efaps.update.schema.datamodel;


import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.efaps.admin.datamodel.AttributeSet;
import org.efaps.admin.datamodel.Classification;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.event.EventType;
import org.efaps.db.Delete;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.db.Update;
import org.efaps.db.store.Store;
import org.efaps.update.AbstractUpdate;
import org.efaps.update.LinkInstance;
import org.efaps.update.UpdateLifecycle;
import org.efaps.update.event.Event;
import org.efaps.update.util.InstallationException;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Handles the import / update of types for eFaps read from a XML configuration
 * item file.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class TypeUpdate
    extends AbstractUpdate
{
    /**
     * Logging instance used to give logging information of this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(TypeUpdate.class);

    /**
     * Link the data model type to allowed event types.
     */
    private static final Link LINK2ALLOWEDEVENT = new Link("Admin_DataModel_TypeEventIsAllowedFor", "To",
                                                           "Admin_DataModel_Type", "From");

    /**
     * Link the data model type to a store.
     */
    private static final Link LINK2STORE = new Link("Admin_DataModel_Type2Store", "From", "DB_Store", "To");

    /**
     * Link the data model type to a type that it classifies.
     */
    private static final Link LINK2CLASSIFIES = new Link("Admin_DataModel_TypeClassifies", "From",
                                                         "Admin_DataModel_Type", "To");

    /**
     * Link the data model type to a tyep used for classification.
     */
    private static final Link LINK2CLASSIFYREL = new Link("Admin_DataModel_TypeClassifyRelation", "From",
                                                          "Admin_DataModel_Type", "To");

    /**
     * List of all links for the type.
     */
    private static final Set<Link> ALLLINKS = new HashSet<Link>();
    static {
        TypeUpdate.ALLLINKS.add(TypeUpdate.LINK2ALLOWEDEVENT);
        TypeUpdate.ALLLINKS.add(TypeUpdate.LINK2STORE);
        TypeUpdate.ALLLINKS.add(TypeUpdate.LINK2CLASSIFIES);
        TypeUpdate.ALLLINKS.add(TypeUpdate.LINK2CLASSIFYREL);
    }

    /**
     * Default constructor to initialize this type instance for given
     * <code>_url</code>.
     *
     * @param _url URL of the file
     */
    public TypeUpdate(final URL _url)
    {
        super(_url, "Admin_DataModel_Type", TypeUpdate.ALLLINKS);
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
        return new TypeDefinition();
    }

    /**
     * The class defines an attribute of a type.
     */
    public class AttributeDefinition extends AbstractDefinition
    {
        /** Name of the attribute. */
        private String name = null;

        /** Name of the Attribute Type of the attribute. */
        private String type = null;

        /** Name of the SQL Table of the attribute. */
        private String sqlTable = null;

        /** SQL Column of the attribute. */
        private String sqlColumn = null;

        /** Name of the Linked Type (used for links to another type). */
        private String typeLink = null;

        /** Events for this Attribute. */
        private final List<Event> events = new ArrayList<Event>();

        /** default value for this Attribute. */
        private String defaultValue = null;

        /**
         * UUID of the Dimension for this Attribute.
         * (Used in conjunction with e.g. IntegerWithUoM).
         */
        private String dimensionUUID;

        /**
         * @see org.efaps.update.AbstractUpdate.AbstractDefinition#readXML(java.util.List, java.util.Map, java.lang.String)
         * @param _tags         list of the tags
         * @param _attributes   attributes
         * @param _text         text
         */
        @Override
        protected void readXML(final List<String> _tags,
                               final Map<String, String> _attributes,
                               final String _text)
        {

            final String value = _tags.get(0);
            if ("defaultvalue".equals(value)) {
                this.defaultValue = _text;
            } else if ("dimensionUUID".equals(value)) {
                this.dimensionUUID = _text;
            } else if ("name".equals(value)) {
                this.name = _text;
            } else if ("sqlcolumn".equals(value)) {
                this.sqlColumn = _text;
            } else if ("sqltable".equals(value)) {
                this.sqlTable = _text;
            } else if ("trigger".equals(value)) {
                if (_tags.size() == 1) {
                    this.events.add(new Event(_attributes.get("name"), EventType.valueOf(_attributes.get("event")),
                                    _attributes.get("program"), _attributes.get("method"), _attributes.get("index")));
                } else if ((_tags.size() == 2) && "property".equals(_tags.get(1))) {
                    this.events.get(this.events.size() - 1).addProperty(_attributes.get("name"), _text);
                } else {
                    super.readXML(_tags, _attributes, _text);
                }
            } else if ("type".equals(value)) {
                this.type = _text;
            } else if ("typelink".equals(value)) {
                this.typeLink = _text;
            } else if ("validate".equals(value)) {
                if (_tags.size() == 1) {
                    this.events.add(new Event(_attributes.get("name"), EventType.VALIDATE, _attributes.get("program"),
                                    _attributes.get("method"), _attributes.get("index")));
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
         * @see org.efaps.update.AbstractUpdate.AbstractDefinition#addEvent(org.efaps.update.event.Event)
         * @param _event Event to add
         */
        @Override
        public void addEvent(final Event _event)
        {
            this.events.add(_event);
        }

        /**
         * Getter method for instance variable {@link #name}.
         *
         * @return value of instance variable {@link #name}
         */
        public String getName()
        {
            return this.name;
        }

        /**
         * Setter method for instance variable {@link #name}.
         *
         * @param _name value for instance variable {@link #name}
         */
        @Override
        protected void setName(final String _name)
        {
            this.name = _name;
        }

        /**
         * Getter method for instance variable {@link #type}.
         *
         * @return value of instance variable {@link #type}
         */
        @Override
        public String getType()
        {
            return this.type;
        }

        /**
         * Setter method for instance variable {@link #type}.
         *
         * @param _type value for instance variable {@link #type}
         */
        @Override
        public void setType(final String _type)
        {
            this.type = _type;
        }

        /**
         * Getter method for instance variable {@link #sqlTable}.
         *
         * @return value of instance variable {@link #sqlTable}
         */
        public String getSqlTable()
        {
            return this.sqlTable;
        }

        /**
         * Setter method for instance variable {@link #sqlTable}.
         *
         * @param _sqlTable value for instance variable {@link #sqlTable}
         */
        public void setSqlTable(final String _sqlTable)
        {
            this.sqlTable = _sqlTable;
        }

        /**
         * Getter method for instance variable {@link #sqlColumn}.
         *
         * @return value of instance variable {@link #sqlColumn}
         */
        public String getSqlColumn()
        {
            return this.sqlColumn;
        }

        /**
         * Setter method for instance variable {@link #sqlColumn}.
         *
         * @param _sqlColumn value for instance variable {@link #sqlColumn}
         */
        public void setSqlColumn(final String _sqlColumn)
        {
            this.sqlColumn = _sqlColumn;
        }

        /**
         * For given type defined with the instance parameter, this attribute is
         * searched by name. If the attribute exists, the attribute is updated.
         * Otherwise the attribute is created for this type.
         *
         * @param _instance type instance to update with this attribute
         * @param _typeName name of the type to update
         * @param _setID    id to set
         * @param _attrInstanceId
         * @throws EFapsException on error
         * @see #getAttrTypeId
         * @see #getSqlTableId
         * @see #getTypeLinkId
         * TODO:  throw Exception is not allowed
         */
        protected void updateInDB(final Instance _instance,
                                  final String _typeName,
                                  final long _setID)
            throws EFapsException
        {
            final long attrTypeId = getAttrTypeId(_typeName);
            final long sqlTableId = getSqlTableId(_typeName);
            final long typeLinkId = getTypeLinkId(_typeName);

            final String typeTmp;
            if (_setID > 0) {
                typeTmp = "Admin_DataModel_AttributeSetAttribute";
            } else {
                typeTmp = "Admin_DataModel_Attribute";
            }
            final SearchQuery query = new SearchQuery();
            query.setQueryTypes(typeTmp);
            query.addWhereExprEqValue("Name", this.name);
            query.addWhereExprEqValue("ParentType", _instance.getId());
            query.addSelect("OID");
            query.executeWithoutAccessCheck();
            Update update;

            if (query.next()) {
                update = new Update((String) query.get("OID"));
            } else {
                update = new Insert(typeTmp);
                update.add("ParentType", "" + _instance.getId());
                update.add("Name", this.name);
            }
            query.close();

            update.add("AttributeType", "" + attrTypeId);
            update.add("Table", "" + sqlTableId);
            update.add("SQLColumn", this.sqlColumn);
            if (typeLinkId == 0) {
                update.add("TypeLink", (String) null);
            } else {
                update.add("TypeLink", "" + typeLinkId);
            }
            if (this.defaultValue != null) {
                update.add("DefaultValue", this.defaultValue);
            }
            if (this.dimensionUUID != null) {
                update.add("DimensionUUID", this.dimensionUUID);
            }
            if (_setID != 0) {
                update.add("ParentAttributeSet", "" + _setID);
            }

            update.executeWithoutAccessCheck();

            for (final Event event : this.events) {
                final Instance newInstance = event.updateInDB(update.getInstance(), this.name);
                setPropertiesInDb(newInstance, event.getProperties());
            }
        }

        /**
         * Makes a search query to return the id of the attribute type defined
         * in {@link #type}.
         * @param _typeName name of the type
         * @return id of the attribute type
         * @throws EFapsException on error
         * @see #type
         */
        protected long getAttrTypeId(final String _typeName)
            throws EFapsException
        {
            final SearchQuery query = new SearchQuery();
            query.setQueryTypes("Admin_DataModel_AttributeType");
            query.addWhereExprEqValue("Name", this.type);
            query.addSelect("OID");
            query.executeWithoutAccessCheck();
            if (!query.next()) {
                TypeUpdate.LOG.error("type[" + _typeName + "]." + "attribute[" + this.name + "]: " + "attribute type '"
                                + this.type + "' not found");
            }
            final long attrTypeId = (Instance.get((String) query.get("OID"))).getId();
            query.close();
            return attrTypeId;
        }

        /**
         * Makes a search query to return the id of the SQL table defined in
         * {@link #sqlTable}.
         * @param _typeName name of the type
         * @throws EFapsException on error
         * @return id of the SQL table
         * @see #sqlTable
         */
        protected long getSqlTableId(final String _typeName)
            throws EFapsException
        {
            final SearchQuery query = new SearchQuery();
            query.setQueryTypes("Admin_DataModel_SQLTable");
            query.addWhereExprEqValue("Name", this.sqlTable);
            query.addSelect("OID");
            query.executeWithoutAccessCheck();
            if (!query.next()) {
                TypeUpdate.LOG.error("type[" + _typeName + "]." + "attribute[" + this.name + "]: " + "SQL table '"
                                + this.sqlTable + "' not found");
            }
            final long sqlTableId = (Instance.get((String) query.get("OID"))).getId();
            query.close();
            return sqlTableId;
        }

        /**
         * Makes a search query to return the id of the SQL table defined in
         * {@link #typeLink}.
         * @param _typeName name of the type
         * @throws EFapsException on error
         * @return id of the linked type (or 0 if no type link is defined)
         * @see #typeLink
         */
        private long getTypeLinkId(final String _typeName) throws EFapsException
        {
            long typeLinkId = 0;
            if ((this.typeLink != null) && (this.typeLink.length() > 0)) {
                final SearchQuery query = new SearchQuery();
                query.setQueryTypes("Admin_DataModel_Type");
                query.addWhereExprEqValue("Name", this.typeLink);
                query.addSelect("ID");
                query.executeWithoutAccessCheck();
                if (query.next()) {
                    typeLinkId = (Long) query.get("ID");
                } else {
                    TypeUpdate.LOG.error("type[" + _typeName + "]." + "attribute[" + this.name + "]: " + " Type '"
                                    + this.typeLink + "' as link not found");
                }
                query.close();
            }
            return typeLinkId;
        }

        /**
         * Returns a string representation with values of all instance variables
         * of an attribute.
         *
         * @return string representation of this definition of an attribute
         */
        @Override
        public String toString()
        {
            return new ToStringBuilder(this).append("name", this.name).append("type", this.type).append("sqlTable",
                            this.sqlTable).append("sqlColumn", this.sqlColumn).append("typeLink", this.typeLink)
                            .toString();
        }
    }

    /**
     * Class for the definition of a Attribute Set.
     */
    public class AttributeSetDefinition extends TypeUpdate.AttributeDefinition
    {

        /**
         * Current read attribute definition instance.
         *
         * @see #readXML(List, Map, String)
         */
        private AttributeDefinition curAttr = null;

        /**
         * List of the related attributes.
         */
        private final List<TypeUpdate.AttributeDefinition> attributes = new ArrayList<TypeUpdate.AttributeDefinition>();

        /**
         * Name of the parent Type.
         */
        private String parentType;

        /**
         * @param _tags         current path as list of single tags
         * @param _attributes   attributes for current path
         * @param _text         content for current path
         */
        @Override()
        public void readXML(final List<String> _tags,
                            final Map<String, String> _attributes,
                            final String _text)
        {
            final String value = _tags.get(0);
            if ("name".equals(value)) {
                setName(_text);
            } else if ("type".equals(value)) {
                setType(_text);
            } else if ("parent".equals(value)) {
                this.parentType = _text;
            } else if ("sqltable".equals(value)) {
                setSqlTable(_text);
            } else if ("sqlcolumn".equals(value)) {
                setSqlColumn(_text);
            } else if ("attribute".equals(value)) {
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
         * Method to get the id of the parent type.
         *
         * @param _typeName     name of the type
         * @return  id of the parent type
         * @throws EFapsException on error
         */
        protected long getParentTypeId(final String _typeName)
            throws EFapsException
        {
            final SearchQuery query = new SearchQuery();
            query.setQueryTypes("Admin_DataModel_Type");
            query.addWhereExprEqValue("Name", _typeName);
            query.addSelect("OID");
            query.executeWithoutAccessCheck();
            if (!query.next()) {
                TypeUpdate.LOG.error("type[" + _typeName + "]." + "attribute[" + getName() + "]: " + "Parent TYpe '"
                                + this.parentType + "' not found");
            }
            final long typeId = (Instance.get((String) query.get("OID"))).getId();
            query.close();
            return typeId;
        }

        /**
         * Set the values in the eFaps Database.
         *
         * @param _instance     instance to be updated
         * @param _typeName     name of the type
         * @throws EFapsException on error
         */
        public void updateInDB(final Instance _instance,
                               final String _typeName)
            throws EFapsException
        {
            final String name = AttributeSet.evaluateName(_typeName, getName());

            long parentTypeId = 0;
            if (this.parentType != null) {
                parentTypeId = getParentTypeId(this.parentType);
            }

            final long attrTypeId = getAttrTypeId(_typeName);
            final long sqlTableId = getSqlTableId(_typeName);

            // create/update the attributSet
            final SearchQuery query = new SearchQuery();
            query.setQueryTypes("Admin_DataModel_AttributeSet");
            query.addWhereExprEqValue("Name", getName());
            query.addWhereExprEqValue("ParentType", _instance.getId());
            query.addSelect("OID");
            query.executeWithoutAccessCheck();
            Update update = null;
            if (query.next()) {
                update = new Update((String) query.get("OID"));
            } else {
                update = new Insert("Admin_DataModel_AttributeSet");
                update.add("ParentType", "" + _instance.getId());
                update.add("Name", getName());
            }
            query.close();

            update.add("AttributeType", "" + attrTypeId);
            update.add("Table", "" + sqlTableId);
            update.add("SQLColumn", getSqlColumn());
            if (parentTypeId > 0) {
                update.add("TypeLink", "" + parentTypeId);
            }

            update.executeWithoutAccessCheck();
            final long setId = update.getInstance().getId();
            update.close();

            // add the attributes to the new Type
            for (final AttributeDefinition attr : this.attributes) {
                attr.updateInDB(_instance, name, setId);
            }
        }
    }

    /**
     * Class for the definition of the type.
     */
    public class TypeDefinition
        extends AbstractDefinition
    {

        /**
         * Stores the name of the parent type. The parent type could not be
         * evaluated because it could be that the type does not exists (and so
         * the type id is evaluated before the insert / update from method
         * {@link #updateInDB}).
         *
         * @see #setParent
         * @see #updateInDB
         */
        private String parentType = null;

        /**
         * All attributes of the type are stored in this list.
         *
         * @see #updateInDB
         * @see #addAttribute
         */
        private final List<TypeUpdate.AttributeDefinition> attributes = new ArrayList<TypeUpdate.AttributeDefinition>();

        /**
         * All attribute sets of the type are stored in this list.
         *
         * @see #updateInDB
         * @see #addAttribute
         */
        private final List<TypeUpdate.AttributeSetDefinition> attributeSets
                                            = new ArrayList<TypeUpdate.AttributeSetDefinition>();

        /**
         * Current read attribute definition instance.
         *
         * @see #readXML(List, Map, String)
         */
        private AttributeDefinition curAttr = null;

        /**
         * Current read attribute set definition instance.
         *
         * @see #readXML(List, Map, String)
         */
        private AttributeSetDefinition curAttrSet = null;


        /**
         * @see org.efaps.update.AbstractUpdate.AbstractDefinition#readXML(java.util.List, java.util.Map, java.lang.String)
         * @param _tags         tags
         * @param _attributes   attributes
         * @param _text         text
         */
        @Override
        protected void readXML(final List<String> _tags,
                               final Map<String, String> _attributes,
                               final String _text)
        {
            final String value = _tags.get(0);
            if ("purpose".equals(value)) {
                if (_tags.size() == 1) {
                    Integer valueTmp = 0;
                    if ("true".equalsIgnoreCase(_attributes.get("abstract"))) {
                        valueTmp = valueTmp + Type.Purpose.ABSTRACT.getId();
                    }
                    if ("true".equalsIgnoreCase(_attributes.get("classification"))) {
                        valueTmp = valueTmp + Type.Purpose.CLASSIFICATION.getId();
                    }
                    addValue("Purpose", valueTmp.toString());
                } else if (_tags.size() == 2) {
                    getProperties().put(Classification.Keys.LINKATTR.getValue(), _text);
                }
            } else if ("attribute".equals(value)) {
                if (_tags.size() == 1) {
                    this.curAttr = new AttributeDefinition();
                    this.attributes.add(this.curAttr);
                } else {
                    this.curAttr.readXML(_tags.subList(1, _tags.size()), _attributes, _text);
                }
            } else if ("attributeset".equals(value)) {
                if (_tags.size() == 1) {
                    this.curAttrSet = new AttributeSetDefinition();
                    this.attributeSets.add(this.curAttrSet);
                } else {
                    this.curAttrSet.readXML(_tags.subList(1, _tags.size()), _attributes, _text);
                }
            } else if ("event-for".equals(value)) {
                // Adds the name of a allowed event type
                addLink(TypeUpdate.LINK2ALLOWEDEVENT, new LinkInstance(_attributes.get("type")));
            } else if ("classifies".equals(value)) {
                addLink(TypeUpdate.LINK2CLASSIFIES,
                        new LinkInstance(_attributes.get(Classification.Keys.TYPE.getValue())));
                addLink(TypeUpdate.LINK2CLASSIFYREL,
                        new LinkInstance(_attributes.get(Classification.Keys.RELTYPE.getValue())));
                getProperties().put(Classification.Keys.RELTYPEATTR.getValue(),
                                    _attributes.get(Classification.Keys.RELTYPEATTR.getValue()));
                getProperties().put(Classification.Keys.RELLINKATTR.getValue(),
                                    _attributes.get(Classification.Keys.RELLINKATTR.getValue()));
            } else if ("parent".equals(value)) {
                this.parentType = _text;
            } else if ("store".equals(value)) {
                addLink(TypeUpdate.LINK2STORE, new LinkInstance(_attributes.get("name")));
                getProperties().put(Store.PROPERTY_ATTR_FILE_LENGTH, _attributes.get("attributeFileLength"));
                getProperties().put(Store.PROPERTY_ATTR_FILE_NAME, _attributes.get("attributeFileName"));
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
         * If a parent type in {@link #parentType} is defined, the type id is
         * evaluated and added to attributes to update (if no parent type is
         * defined, the parent type id is set to <code>null</code>). After the
         * type is updated (or inserted if needed), all attributes must be
         * updated.
         *
         * @param _step             lifecycle step
         * @param _allLinkTypes     set of all links
         * @throws InstallationException on error
         * @throws EFapsException on error
         * @see #parentType
         * @see #attributes
         */
        @Override()
        public void updateInDB(final UpdateLifecycle _step,
                               final Set<Link> _allLinkTypes)
            throws InstallationException, EFapsException
        {
            if (_step == UpdateLifecycle.EFAPS_UPDATE)  {
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
            }

            super.updateInDB(_step, _allLinkTypes);

            if (_step == UpdateLifecycle.EFAPS_UPDATE)  {
                for (final AttributeDefinition attr : this.attributes) {
                    attr.updateInDB(this.instance, getValue("Name"), 0);
                }

                for (final AttributeSetDefinition attrSet : this.attributeSets) {
                    attrSet.updateInDB(this.instance, getValue("Name"));
                }

                removeObsoleteAttributes();
            }
        }

        /**
         * @throws EFapsException on error
         */
        private void removeObsoleteAttributes()
            throws EFapsException
        {
            final Set<String> attrNames = new HashSet<String>();
            for (final AttributeDefinition attr : this.attributes) {
                attrNames.add(attr.name);
            }
            for (final AttributeSetDefinition attr : this.attributeSets) {
                attrNames.add(attr.getName());
                for (final AttributeDefinition subAttr : attr.attributes) {
                    attrNames.add(subAttr.name);
                }
            }
            final SearchQuery query = new SearchQuery();
            query.setExpand(this.instance, "Admin_DataModel_Attribute\\ParentType");
            query.addSelect("Name");
            query.addSelect("OID");
            query.executeWithoutAccessCheck();
            while (query.next()) {
                if (!attrNames.contains(query.get("Name"))) {
                    final Delete delete = new Delete(Instance.get((String) query.get("OID")));
                    delete.executeWithoutAccessCheck();
                }
            }
        }
    }
}
