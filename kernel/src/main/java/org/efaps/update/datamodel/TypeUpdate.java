/*
 * Copyright 2003-2008 The eFaps Team
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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.digester.Digester;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.efaps.admin.event.EventType;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.db.Update;
import org.efaps.update.AbstractUpdate;
import org.efaps.update.event.Event;
import org.efaps.update.event.EventFactory;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

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
  private final static Logger LOG = LoggerFactory.getLogger(TypeUpdate.class);

  private final static Set<Link> ALLLINKS = new HashSet<Link>();

  // ///////////////////////////////////////////////////////////////////////////
  // constructors

  /**
   *
   */
  public TypeUpdate() {
    super("Admin_DataModel_Type", ALLLINKS);
  }

  // ///////////////////////////////////////////////////////////////////////////
  // static methods

  /**
   * Method that reads the given XML-File and than uses the
   * <code>org.apache.commons.digester</code> to create the diffrent Class and
   * invokes the Methods to Update a Type
   *
   * @param _file
   *                XML-File to be read by the digester
   * @return TypUdate Definition read by digester
   */
  public static TypeUpdate readXMLFile(final URL _url) {
    TypeUpdate ret = null;

    try {
      final Digester digester = new Digester();
      digester.setValidating(false);
      // create a new Type
      digester.addObjectCreate("datamodel-type", TypeUpdate.class);

      // set the UUID for the Type
      digester.addCallMethod("datamodel-type/abstract", "setAbstractType", 1);
      digester.addCallParam("datamodel-type/abstract", 0);

      // set the UUID for the Type
      digester.addCallMethod("datamodel-type/uuid", "setUUID", 1);
      digester.addCallParam("datamodel-type/uuid", 0);

      // add a new Definition for the Type
      digester.addObjectCreate("datamodel-type/definition", Definition.class);
      digester.addSetNext("datamodel-type/definition", "addDefinition");

      // set the Version of the Type-Definition
      digester.addCallMethod("datamodel-type/definition/version", "setVersion",
          4);
      digester.addCallParam("datamodel-type/definition/version/application", 0);
      digester.addCallParam("datamodel-type/definition/version/global", 1);
      digester.addCallParam("datamodel-type/definition/version/local", 2);
      digester.addCallParam("datamodel-type/definition/version/mode", 3);

      // set the Name of the Type-Definition
      digester.addCallMethod("datamodel-type/definition/name", "setName", 1);
      digester.addCallParam("datamodel-type/definition/name", 0);

      // set the parent of the Type-Definition
      digester
          .addCallMethod("datamodel-type/definition/parent", "setParent", 1);
      digester.addCallParam("datamodel-type/definition/parent", 0);

      // add an Attribute to the Type-Definition
      digester.addObjectCreate("datamodel-type/definition/attribute",
          Attribute.class);

      // set Name, Type, Tabel etc, of the Attribute
      digester.addCallMethod("datamodel-type/definition/attribute",
          "setDefinitions", 6);
      digester.addCallParam("datamodel-type/definition/attribute/name", 0);
      digester.addCallParam("datamodel-type/definition/attribute/type", 1);
      digester.addCallParam("datamodel-type/definition/attribute/sqltable", 2);
      digester.addCallParam("datamodel-type/definition/attribute/sqlcolumn", 3);
      digester.addCallParam("datamodel-type/definition/attribute/typelink", 4);
      digester.addCallParam("datamodel-type/definition/attribute/defaultvalue",
          5);

      // add a Trigger-Event to the Attribute
      digester.addFactoryCreate("datamodel-type/definition/attribute/trigger",
          new EventFactory(), false);
      digester.addCallMethod(
          "datamodel-type/definition/attribute/trigger/property",
          "addProperty", 2);
      digester.addCallParam(
          "datamodel-type/definition/attribute/trigger/property", 0, "name");
      digester.addCallParam(
          "datamodel-type/definition/attribute/trigger/property", 1);
      digester.addSetNext("datamodel-type/definition/attribute/trigger",
          "addEvent", "org.efaps.update.event.Event");

      // add a Validate-Event to the Attribute
      digester.addFactoryCreate("datamodel-type/definition/attribute/validate",
          new EventFactory(EventType.VALIDATE.name), false);
      digester.addCallMethod(
          "datamodel-type/definition/attribute/validate/property",
          "addProperty", 2);
      digester.addCallParam(
          "datamodel-type/definition/attribute/validate/property", 0, "name");
      digester.addCallParam(
          "datamodel-type/definition/attribute/validate/property", 1);
      digester.addSetNext("datamodel-type/definition/attribute/validate",
          "addEvent", "org.efaps.update.event.Event");

      digester
          .addSetNext("datamodel-type/definition/attribute", "addAttribute");

      // add Properties to the Type-Definition
      digester.addCallMethod("datamodel-type/definition/property",
          "addProperty", 2);
      digester.addCallParam("datamodel-type/definition/property", 0, "name");
      digester.addCallParam("datamodel-type/definition/property", 1);

      // add Trigger-Event to the Type-Definition
      digester.addFactoryCreate("datamodel-type/definition/trigger",
          new EventFactory(), false);
      digester.addCallMethod("datamodel-type/definition/trigger/property",
          "addProperty", 2);
      digester.addCallParam("datamodel-type/definition/trigger/property", 0,
          "name");
      digester.addCallParam("datamodel-type/definition/trigger/property", 1);
      digester.addSetNext("datamodel-type/definition/trigger", "addEvent",
          "org.efaps.update.event.Event");

      ret = (TypeUpdate) digester.parse(_url);

      if (ret != null) {
        ret.setURL(_url);
      }
    } catch (IOException e) {
      LOG.error(_url.toString() + " is not readable", e);
    } catch (SAXException e) {
      LOG.error(_url.toString() + " seems to be invalide XML", e);
    }
    return ret;
  }

  // ///////////////////////////////////////////////////////////////////////////
  // ///////////////////////////////////////////////////////////////////////////
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * The class defines an attribute of a type.
   */
  public static class Attribute extends AbstractDefinition {

    /** Name of the attribute. */
    private String name;

    /** Name of the Attribute Type of the attribute. */
    private String type;

    /** Name of the SQL Table of the attribute. */
    private String sqlTable;

    /** SQL Column of the attribute. */
    private String sqlColumn;

    /** Name of the Linked Type (used for links to another type). */
    private String typeLink;

    /** Events for this Attribute */
    private final List<Event> events = new ArrayList<Event>();

    /** Defaultvalue for this Attribute */
    private String defaultValue;

    public void setDefinitions(final String _name, final String _type,
                               final String _sqltable, final String _sqlcolumn,
                               final String _typelink,
                               final String _defaultvalue) {
      this.name = _name;
      this.type = _type;
      this.sqlTable = _sqltable;
      this.sqlColumn = _sqlcolumn;
      this.typeLink = _typelink;
      this.defaultValue = _defaultvalue;
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
     * @param _instance
     *                type instance to update with this attribute
     * @param _typeName
     *                name of the type to update
     * @see #getAttrTypeId
     * @see #getSqlTableId
     * @see #getTypeLinkId
     * @todo throw Exception is not allowed
     */
    protected void updateInDB(final Instance _instance, final String _typeName)
                                                                               throws Exception {
      final long attrTypeId = getAttrTypeId(_typeName);
      final long sqlTableId = getSqlTableId(_typeName);
      final long typeLinkId = getTypeLinkId(_typeName);

      final SearchQuery query = new SearchQuery();
      query.setQueryTypes("Admin_DataModel_Attribute");
      query.addWhereExprEqValue("Name", this.name);
      query.addWhereExprEqValue("ParentType", _instance.getId());
      query.addSelect("OID");
      query.executeWithoutAccessCheck();
      Update update;

      if (query.next()) {
        update = new Update((String) query.get("OID"));
      } else {
        update = new Insert("Admin_DataModel_Attribute");
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
      update.executeWithoutAccessCheck();

      for (Event event : this.events) {
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
    private long getAttrTypeId(final String _typeName) throws EFapsException {
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
      final long attrTypeId = (new Instance((String) query.get("OID"))).getId();
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
    private long getSqlTableId(final String _typeName) throws EFapsException {
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
      final long sqlTableId = (new Instance((String) query.get("OID"))).getId();
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
    private long getTypeLinkId(final String _typeName) throws EFapsException {
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
    public String toString() {
      return new ToStringBuilder(this).append("name", this.name).append("type",
          this.type).append("sqlTable", this.sqlTable).append("sqlColumn",
          this.sqlColumn).append("typeLink", this.typeLink).toString();
    }
  }

  // ///////////////////////////////////////////////////////////////////////////
  // ///////////////////////////////////////////////////////////////////////////
  // ///////////////////////////////////////////////////////////////////////////
  // class for the definitions

  public static class Definition extends AbstractDefinition {

    // /////////////////////////////////////////////////////////////////////////
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
    private final List<Attribute> attributes = new ArrayList<Attribute>();

    ///////////////////////////////////////////////////////////////////////////
    // instance methods

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
    public Instance updateInDB(final Instance _instance,
                               final Set<Link> _allLinkTypes)
        throws EFapsException,Exception
    {
      // set the id of the parent type (if defined)
      if ((this.parentType != null) && (this.parentType.length() > 0)) {
        final SearchQuery query = new SearchQuery();
        query.setQueryTypes("Admin_DataModel_Type");
        query.addWhereExprEqValue("Name", this.parentType);
        query.addSelect("OID");
        query.executeWithoutAccessCheck();
        if (query.next()) {
          final Instance instance = new Instance((String) query.get("OID"));
          addValue("ParentType", "" + instance.getId());
        } else {
          addValue("ParentType", null);
        }
        query.close();
      } else {
        addValue("ParentType", null);
      }

      final Instance instance = super.updateInDB(_instance, _allLinkTypes);

      for (Attribute attr : this.attributes) {
        attr.updateInDB(instance, getValue("Name"));
      }

      return instance;
    }

    /**
     * Setter method for instance variable {@link #parentType}.
     *
     * @param _parentType
     *                new value to set
     * @see #parentType
     */
    public void setParent(final String _parentType) {
      this.parentType = _parentType;
    }

    /**
     * adds a Attribute to the Definition
     *
     * @param _attribute
     *                Attribute to add
     */
    public void addAttribute(final Attribute _attribute) {
      this.attributes.add(_attribute);
    }

  }

}
