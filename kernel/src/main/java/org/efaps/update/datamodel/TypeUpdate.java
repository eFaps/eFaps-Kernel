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

import static org.efaps.db.store.Store.PROPERTY_ATTR_FILE_LENGTH;
import static org.efaps.db.store.Store.PROPERTY_ATTR_FILE_NAME;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.efaps.admin.datamodel.AttributeSet;
import org.efaps.admin.event.EventType;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.db.Update;
import org.efaps.update.AbstractUpdate;
import org.efaps.update.LinkInstance;
import org.efaps.update.event.Event;
import org.efaps.util.EFapsException;
/**
 * This Class is responsible for the Update of Type in the Database.<br>
 * It reads with <code>org.apache.commons.digester</code> a XML-File to create
 * the different Classes and invokes the Methods to Update a Type
 *
 * @author tmo
 * @author jmox
 * @version $Id$
 */
public class TypeUpdate extends AbstractUpdate {

  // ///////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used to give logging information of this class.
   */
  private static final Logger LOG = LoggerFactory.getLogger(TypeUpdate.class);

  /**
   * Link the data model type to allowed event types.
   */
  private static final Link LINK2ALLOWEDEVENT
      = new Link("Admin_DataModel_TypeEventIsAllowedFor",
                 "To",
                 "Admin_DataModel_Type", "From");

  /**
   * Link the data model type to a store.
   */
  private static final Link LINK2STORE = new Link("Admin_DataModel_Type2Store",
                                                  "From",
                                                  "DB_Store", "To");


  /**
   * List of all links for the type.
   */
  private static final Set<Link> ALLLINKS = new HashSet<Link>();
  static  {
    ALLLINKS.add(LINK2ALLOWEDEVENT);
    ALLLINKS.add(LINK2STORE);
  }

  /////////////////////////////////////////////////////////////////////////////
  // constructors

  /**
   *
   * @param _url        URL of the file
   */
  public TypeUpdate(final URL _url) {
    super(_url, "Admin_DataModel_Type", ALLLINKS);
  }

  /////////////////////////////////////////////////////////////////////////////
  // intance methods

  /**
   * Creates new instance of class {@link TypeDefinition}.
   *
   * @return new definition instance
   * @see TypeDefinition
   */
  @Override
  protected AbstractDefinition newDefinition() {
    return new TypeDefinition();
  }

  /////////////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////

  /**
   * The class defines an attribute of a type.
   */
  public class AttributeDefinition extends AbstractDefinition {
    /** Name of the attribute. */
    protected String name = null;

    /** Name of the Attribute Type of the attribute. */
    protected String type = null;

    /** Name of the SQL Table of the attribute. */
    protected String sqlTable = null;

    /** SQL Column of the attribute. */
    protected String sqlColumn = null;

    /** Name of the Linked Type (used for links to another type). */
    private String typeLink = null;

    /** Events for this Attribute */
    private final List<Event> events = new ArrayList<Event>();

    /** default value for this Attribute */
    private String defaultValue = null;

    @Override
    protected void readXML(final List<String> _tags,
                           final Map<String,String> _attributes,
                           final String _text)
    {
      final String value = _tags.get(0);
      if ("defaultvalue".equals(value)) {
        this.defaultValue = _text;
      } else if ("name".equals(value))  {
        this.name = _text;
      } else if ("sqlcolumn".equals(value))  {
        this.sqlColumn = _text;
      } else if ("sqltable".equals(value))  {
        this.sqlTable = _text;
      } else if ("trigger".equals(value))  {
        if (_tags.size() == 1)  {
          this.events.add(new Event(_attributes.get("name"),
                                    EventType.valueOf(_attributes.get("event")),
                                    _attributes.get("program"),
                                    _attributes.get("method"),
                                    _attributes.get("index")));
        } else if ((_tags.size() == 2) && "property".equals(_tags.get(1))) {
          this.events.get(this.events.size() - 1).addProperty(_attributes.get("name"),
                                                              _text);
        } else  {
          super.readXML(_tags, _attributes, _text);
        }
      } else if ("type".equals(value))  {
        this.type = _text;
      } else if ("typelink".equals(value))  {
        this.typeLink = _text;
      } else if ("validate".equals(value))  {
        if (_tags.size() == 1)  {
          this.events.add(new Event(_attributes.get("name"),
                                    EventType.VALIDATE,
                                    _attributes.get("program"),
                                    _attributes.get("method"),
                                    _attributes.get("index")));
        } else if ((_tags.size() == 2) && "property".equals(_tags.get(1))) {
          this.events.get(this.events.size() - 1).addProperty(_attributes.get("name"),
                                                              _text);
        } else  {
          super.readXML(_tags, _attributes, _text);
        }
      } else  {
        super.readXML(_tags, _attributes, _text);
      }
    }

    @Override
    public void addEvent(final Event _event) {
      this.events.add(_event);
    }

    /**
     * For given type defined with the instance parameter, this attribute is
     * searched by name. If the attribute exists, the attribute is updated.
     * Otherwise the attribute is created for this type.
     *
     * @param _instance   type instance to update with this attribute
     * @param _typeName   name of the type to update
     * @param _attrInstanceId
     * @see #getAttrTypeId
     * @see #getSqlTableId
     * @see #getTypeLinkId
     * @todo throw Exception is not allowed
     */
    protected void updateInDB(final Instance _instance,
                              final String _typeName,
                              final long _setID)
        throws EFapsException
    {
      final long attrTypeId = getAttrTypeId(_typeName);
      final long sqlTableId = getSqlTableId(_typeName);
      final long typeLinkId = getTypeLinkId(_typeName);

      final String type;
      if (_setID>0) {
        type = "Admin_DataModel_AttributeSetAttribute";
      } else {
        type = "Admin_DataModel_Attribute";
      }
      final SearchQuery query = new SearchQuery();
      query.setQueryTypes(type);
      query.addWhereExprEqValue("Name", this.name);
      query.addWhereExprEqValue("ParentType", _instance.getId());
      query.addSelect("OID");
      query.executeWithoutAccessCheck();
      Update update;

      if (query.next()) {
        update = new Update((String) query.get("OID"));
      } else {
        update = new Insert(type);
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
      if (_setID!=0){
        update.add("ParentAttributeSet","" + _setID);
      }

      update.executeWithoutAccessCheck();

      for (final Event event : this.events) {
        final Instance newInstance =
            event.updateInDB(update.getInstance(), this.name);
        setPropertiesInDb(newInstance, event.getProperties());
      }
    }

    /**
     * Makes a search query to return the id of the attribute type defined in
     * {@link #type}.
     *
     * @return id of the attribute type
     * @see #type
     */
    protected long getAttrTypeId(final String _typeName)
        throws EFapsException {
      final SearchQuery query = new SearchQuery();
      query.setQueryTypes("Admin_DataModel_AttributeType");
      query.addWhereExprEqValue("Name", this.type);
      query.addSelect("OID");
      query.executeWithoutAccessCheck();
      if (!query.next()) {
        LOG.error("type["
            + _typeName
            + "]."
            + "attribute["
            + this.name
            + "]: "
            + "attribute type '"
            + this.type
            + "' not found");
      }
      final long attrTypeId = (Instance.get((String) query.get("OID"))).getId();
      query.close();
      return attrTypeId;
    }

    /**
     * Makes a search query to return the id of the SQL table defined in
     * {@link #sqlTable}.
     *
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
        LOG.error("type["
            + _typeName
            + "]."
            + "attribute["
            + this.name
            + "]: "
            + "SQL table '"
            + this.sqlTable
            + "' not found");
      }
      final long sqlTableId = (Instance.get((String) query.get("OID"))).getId();
      query.close();
      return sqlTableId;
    }

    /**
     * Makes a search query to return the id of the SQL table defined in
     * {@link #typeLink}.
     *
     * @return id of the linked type (or 0 if no type link is defined)
     * @see #typeLink
     */
    private long getTypeLinkId(final String _typeName)
        throws EFapsException
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
          LOG.error("type["
              + _typeName
              + "]."
              + "attribute["
              + this.name
              + "]: "
              + " Type '"
              + this.typeLink
              + "' as link not found");
        }
        query.close();
      }
      return typeLinkId;
    }

    /**
     * Returns a string representation with values of all instance variables of
     * an attribute.
     *
     * @return string representation of this definition of an attribute
     */
    @Override
    public String toString()
    {
      return new ToStringBuilder(this)
                .append("name", this.name)
                .append("type", this.type)
                .append("sqlTable", this.sqlTable)
                .append("sqlColumn", this.sqlColumn)
                .append("typeLink", this.typeLink)
                .toString();
    }
  }

  public class AttributeSetDefinition extends AttributeDefinition {


    /**
     * Current read attribute definition instance.
     *
     * @see #readXML(List, Map, String)
     */
    private AttributeDefinition curAttr = null;

    private final List<AttributeDefinition> attributes = new ArrayList<AttributeDefinition>();

    private String parentType;
    /**
     * @param _tags
     * @param _attributes
     * @param _text
     */
    @Override
    public void readXML(final List<String> _tags,
                        final Map<String, String> _attributes,
                        final String _text) {

      final String value = _tags.get(0);
      if ("name".equals(value))  {
        this.name = _text;
      } else if ("type".equals(value))  {
        this.type = _text;
      } else if ("parent".equals(value))  {
        this.parentType = _text;
      } else if ("sqltable".equals(value))  {
          this.sqlTable = _text;
      } else if ("sqlcolumn".equals(value))  {
          this.sqlColumn = _text;
      } else if ("attribute".equals(value))  {
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

    protected long getParentTypeId(final String _typeName)
    throws EFapsException
    {
      final SearchQuery query = new SearchQuery();
      query.setQueryTypes("Admin_DataModel_Type");
      query.addWhereExprEqValue("Name", _typeName);
      query.addSelect("OID");
      query.executeWithoutAccessCheck();
      if (!query.next()) {
        LOG.error("type["
            + _typeName
            + "]."
            + "attribute["
            + this.name
            + "]: "
            + "Parent TYpe '"
            + this.parentType
            + "' not found");
      }
      final long typeId = (Instance.get((String) query.get("OID"))).getId();
      query.close();
      return typeId;
    }

    /**
     * @param instance
     * @param value
     * @throws EFapsException
     */
    public void updateInDB(final Instance _instance, final String _typeName) throws EFapsException {
      final String name = AttributeSet.evaluateName(_typeName, this.name);

      long parentTypeId = 0;
      if (this.parentType!=null) {
        parentTypeId = getParentTypeId(this.parentType);
      }

      final long attrTypeId = getAttrTypeId(_typeName);
      final long sqlTableId = getSqlTableId(_typeName);

      //create/update the attributSet
      final SearchQuery query = new SearchQuery();
      query.setQueryTypes("Admin_DataModel_AttributeSet");
      query.addWhereExprEqValue("Name", this.name);
      query.addWhereExprEqValue("ParentType", _instance.getId());
      query.addSelect("OID");
      query.executeWithoutAccessCheck();
      Update update = null;
      if (query.next()) {
        update = new Update((String) query.get("OID"));
      } else {
        update = new Insert("Admin_DataModel_AttributeSet");
        update.add("ParentType", "" + _instance.getId());
        update.add("Name", this.name);
      }
      query.close();

      update.add("AttributeType", "" + attrTypeId);
      update.add("Table", "" + sqlTableId);
      update.add("SQLColumn", this.sqlColumn);
      if (parentTypeId > 0){
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

  /////////////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////
  // class for the definitions

  public class TypeDefinition extends AbstractDefinition {

    ///////////////////////////////////////////////////////////////////////////
    // instance variables

    /**
     * Stores the name of the parent type. The parent type could not be
     * evaluated because it could be that the type does not exists (and so the
     * type id is evaluated before the insert / update from method
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
    private final List<AttributeDefinition> attributes = new ArrayList<AttributeDefinition>();

    private final List<AttributeSetDefinition> attributeSets = new ArrayList<AttributeSetDefinition>();

    /**
     * Current read attribute definition instance.
     *
     * @see #readXML(List, Map, String)
     */
    private AttributeDefinition curAttr = null;


    private AttributeSetDefinition curAttrSet = null;
    ///////////////////////////////////////////////////////////////////////////
    // instance methods

    @Override
    protected void readXML(final List<String> _tags,
                           final Map<String,String> _attributes,
                           final String _text)
    {
      final String value = _tags.get(0);
      if ("abstract".equals(value))  {
        addValue("Abstract", _text);
      } else if ("attribute".equals(value))  {
        if (_tags.size() == 1)  {
          this.curAttr = new AttributeDefinition();
          this.attributes.add(this.curAttr);
        } else  {
          this.curAttr.readXML(_tags.subList(1, _tags.size()), _attributes, _text);
        }
      } else if ("attributeset".equals(value)) {
        if (_tags.size() == 1) {
          this.curAttrSet = new AttributeSetDefinition();
          this.attributeSets.add(this.curAttrSet);
        } else {
          this.curAttrSet.readXML(_tags.subList(1, _tags.size()), _attributes, _text);
        }
      } else if ("event-for".equals(value))  {
        // Adds the name of a allowed event type
        addLink(LINK2ALLOWEDEVENT, new LinkInstance(_attributes.get("type")));
      } else if ("parent".equals(value))  {
        this.parentType = _text;
      } else if ("store".equals(value))  {
        addLink(LINK2STORE, new LinkInstance(_attributes.get("name")));
        getProperties().put(PROPERTY_ATTR_FILE_LENGTH,
                            _attributes.get("attributeFileLength"));
        getProperties().put(PROPERTY_ATTR_FILE_NAME,
                            _attributes.get("attributeFileName"));

      } else if ("trigger".equals(value))  {
        if (_tags.size() == 1)  {
          this.events.add(new Event(_attributes.get("name"),
                                    EventType.valueOf(_attributes.get("event")),
                                    _attributes.get("program"),
                                    _attributes.get("method"),
                                    _attributes.get("index")));
        } else if ((_tags.size() == 2) && "property".equals(_tags.get(1))) {
          this.events.get(this.events.size() - 1).addProperty(_attributes.get("name"),
                                                              _text);
        } else  {
          super.readXML(_tags, _attributes, _text);
        }
      } else  {
        super.readXML(_tags, _attributes, _text);
      }
    }

    /**
     * If a parent type in {@link #parentType} is defined, the type id is
     * evaluated and added to attributes to update (if no parent type is
     * defined, the parent type id is set to <code>null</code>). After the
     * type is updated (or inserted if needed), all attributes must be updated.
     *
     * @todo throw Exception is not allowed
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

      for (final AttributeDefinition attr : this.attributes) {
        attr.updateInDB(this.instance, getValue("Name"), 0);
      }

      for (final AttributeSetDefinition attrSet : this.attributeSets) {
        attrSet.updateInDB(this.instance, getValue("Name"));
      }

    }

    /* (non-Javadoc)
     * @see org.efaps.update.AbstractUpdate.AbstractDefinition#setLinksInDB(org.efaps.db.Instance, org.efaps.update.AbstractUpdate.Link, java.util.Set)
     */
    @Override
    protected void setLinksInDB(final Instance _instance, final Link _linktype,
        final Set<LinkInstance> _links) throws EFapsException {
      // TODO Auto-generated method stub
      super.setLinksInDB(_instance, _linktype, _links);
    }
  }

}
