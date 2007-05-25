/*
 * Copyright 2003-2007 The eFaps Team
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.digester.Digester;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.xml.sax.SAXException;

import org.efaps.admin.event.TriggerEvent;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.db.Update;
import org.efaps.update.AbstractUpdate;
import org.efaps.util.EFapsException;

/**
 * This Class is responsible for the Update of Type in the Database
 * 
 * @author tmo
 * @author jmo
 * @version $Id: TypeUpdate.java 726 2007-03-17 22:14:14 +0000 (Sat, 17 Mar
 *          2007) tmo $
 * @todo description
 */
public class TypeUpdate extends AbstractUpdate {

  // ///////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used to give logging information of this class.
   */
  private final static Log       LOG      = LogFactory.getLog(TypeUpdate.class);

  private final static Set<Link> ALLLINKS = new HashSet<Link>();
  {
    /*
     * ALLLINKS.add(LINK2ACCESSTYPE); ALLLINKS.add(LINK2DATAMODELTYPE);
     * ALLLINKS.add(LINK2PERSON); ALLLINKS.add(LINK2ROLE);
     * ALLLINKS.add(LINK2GROUP);
     */
  }

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

  public static TypeUpdate readXMLFile(final String _fileName)
                                                              throws IOException {
    return readXMLFile(new File(_fileName));
  }

  /**
   * Method that reads the given XML-File and than uses the
   * <code>org.apache.commons.digester</code> to create the diffrent Class and
   * invokes the Methods to Update a Type
   * 
   * @param _file
   *          XML-File to be read by the digester
   * @return TypUdate Definition read by digester
   * @throws IOException
   *           if file is not readable
   */
  public static TypeUpdate readXMLFile(final File _file) throws IOException {
    TypeUpdate ret = null;

    try {
      Digester digester = new Digester();
      digester.setValidating(false);
      digester.addObjectCreate("datamodel-type", TypeUpdate.class);

      digester.addCallMethod("datamodel-type/uuid", "setUUID", 1);
      digester.addCallParam("datamodel-type/uuid", 0);

      digester.addObjectCreate("datamodel-type/definition", Definition.class);
      digester.addSetNext("datamodel-type/definition", "addDefinition");

      digester.addCallMethod("datamodel-type/definition/version", "setVersion",
          4);
      digester.addCallParam("datamodel-type/definition/version/application", 0);
      digester.addCallParam("datamodel-type/definition/version/global", 1);
      digester.addCallParam("datamodel-type/definition/version/local", 2);
      digester.addCallParam("datamodel-type/definition/version/mode", 3);

      digester.addCallMethod("datamodel-type/definition/name", "setName", 1);
      digester.addCallParam("datamodel-type/definition/name", 0);

      digester
          .addCallMethod("datamodel-type/definition/parent", "setParent", 1);
      digester.addCallParam("datamodel-type/definition/parent", 0);

      digester.addCallMethod("datamodel-type/definition/attribute",
          "addAttribute", 5);
      digester.addCallParam("datamodel-type/definition/attribute/name", 0);
      digester.addCallParam("datamodel-type/definition/attribute/type", 1);
      digester.addCallParam("datamodel-type/definition/attribute/sqltable", 2);
      digester.addCallParam("datamodel-type/definition/attribute/sqlcolumn", 3);
      digester.addCallParam("datamodel-type/definition/attribute/typelink", 4);

      digester.addCallMethod("datamodel-type/definition/property",
          "addProperty", 2);
      digester.addCallParam("datamodel-type/definition/property", 0, "name");
      digester.addCallParam("datamodel-type/definition/property", 1);

      digester.addCallMethod("datamodel-type/definition/trigger", "addTrigger",
          4);
      digester.addCallParam("datamodel-type/definition/trigger", 0, "name");
      digester.addCallParam("datamodel-type/definition/trigger", 1, "event");
      digester.addCallParam("datamodel-type/definition/trigger", 2, "program");
      digester.addCallParam("datamodel-type/definition/trigger", 3, "index");

      ret = (TypeUpdate) digester.parse(_file);

      if (ret != null) {
        ret.setFile(_file);
      }
    } catch (SAXException e) {
      LOG.error(_file.getName() + "' seems to be invalide XML", e);
    }
    return ret;
  }

  // ///////////////////////////////////////////////////////////////////////////

  /**
   * The class defines an attribute of a type.
   */
  private static class Attribute {

    /** Name of the attribute. */
    private final String name;

    /** Name of the Attribute Type of the attribute. */
    private final String type;

    /** Name of the SQL Table of the attribute. */
    private final String sqlTable;

    /** SQL Column of the attribute. */
    private final String sqlColumn;

    /** Name of the Linked Type (used for links to another type). */
    private final String typeLink;

    private Attribute(final String _name, final String _type,
        final String _sqlTable, final String _sqlColumn, final String _typeLink) {
      this.name = _name;
      this.type = _type;
      this.sqlTable = _sqlTable;
      this.sqlColumn = _sqlColumn;
      this.typeLink = _typeLink;
    }

    /**
     * For given type defined with the instance parameter, this attribute is
     * searched by name. If the attribute exists, the attribute is updated.
     * Otherwise the attribute is created for this type.
     * 
     * @param _instance
     *          type instance to update with this attribute
     * @param _typeName
     *          name of the type to update
     * @see #getAttrTypeId
     * @see #getSqlTableId
     * @see #getTypeLinkId
     * @todo throw Exception is not allowed
     */
    protected void updateInDB(final Instance _instance, final String _typeName)
                                                                               throws Exception {
      long attrTypeId = getAttrTypeId(_typeName);
      long sqlTableId = getSqlTableId(_typeName);
      long typeLinkId = getTypeLinkId(_typeName);

      SearchQuery query = new SearchQuery();
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
      update.executeWithoutAccessCheck();
    }

    /**
     * Makes a search query to return the id of the attribute type defined in
     * {@link #type}.
     * 
     * @return id of the attribute type
     * @see #type
     */
    private long getAttrTypeId(final String _typeName) throws EFapsException {
      SearchQuery query = new SearchQuery();
      query.setQueryTypes("Admin_DataModel_AttributeType");
      query.addWhereExprEqValue("Name", this.type);
      query.addSelect("OID");
      query.executeWithoutAccessCheck();
      if (!query.next()) {
        LOG.error("type[" + _typeName + "]." + "attribute[" + this.name + "]: "
            + "attribute type '" + this.type + "' not found");
      }
      long attrTypeId = (new Instance((String) query.get("OID"))).getId();
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
      SearchQuery query = new SearchQuery();
      query.setQueryTypes("Admin_DataModel_SQLTable");
      query.addWhereExprEqValue("Name", this.sqlTable);
      query.addSelect("OID");
      query.executeWithoutAccessCheck();
      if (!query.next()) {
        LOG.error("type[" + _typeName + "]." + "attribute[" + this.name + "]: "
            + "SQL table '" + this.sqlTable + "' not found");
      }
      long sqlTableId = (new Instance((String) query.get("OID"))).getId();
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
        SearchQuery query = new SearchQuery();
        query.setQueryTypes("Admin_DataModel_Type");
        query.addWhereExprEqValue("Name", this.typeLink);
        query.addSelect("ID");
        query.executeWithoutAccessCheck();
        if (!query.next()) {
          LOG.error("type[" + _typeName + "]." + "attribute[" + this.name
              + "]: " + " Type '" + this.typeLink + "' as link not found");
        } else {
          typeLinkId = (Long) query.get("ID");
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
    public String toString() {
      return new ToStringBuilder(this).append("name", this.name).append("type",
          this.type).append("sqlTable", this.sqlTable).append("sqlColumn",
          this.sqlColumn).append("typeLink", this.typeLink).toString();
    }
  }

  // ///////////////////////////////////////////////////////////////////////////
  // class for the definitions

  public static class Definition extends DefinitionAbstract {
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
    private String                parentType = null;

    /**
     * All attributes of the type are stored in this list.
     * 
     * @see #updateInDB
     * @see #addAttribute
     */
    private final List<Attribute> attributes = new ArrayList<Attribute>();

    private final List<Trigger>   triggers   = new ArrayList<Trigger>();

    // /////////////////////////////////////////////////////////////////////////
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
    public Instance updateInDB(final Instance _instance,
                               final Set<Link> _allLinkTypes,
                               final Insert _insert) throws EFapsException,
                                                    Exception {
      // set the id of the parent type (if defined)
      if ((this.parentType != null) && (this.parentType.length() > 0)) {
        SearchQuery query = new SearchQuery();
        query.setQueryTypes("Admin_DataModel_Type");
        query.addWhereExprEqValue("Name", this.parentType);
        query.addSelect("OID");
        query.executeWithoutAccessCheck();
        if (query.next()) {
          Instance instance = new Instance((String) query.get("OID"));
          addValue("ParentType", "" + instance.getId());
        } else {
          addValue("ParentType", null);
        }
        query.close();
      } else {
        addValue("ParentType", null);
      }

      Instance instance = super.updateInDB(_instance, _allLinkTypes, _insert);

      for (Attribute attr : this.attributes) {
        attr.updateInDB(instance, getValue("Name"));
      }

      for (Trigger trig : this.triggers) {
        trig.updateInDB(instance, getValue("Name"));
      }

      return instance;
    }

    /**
     * Setter method for instance variable {@link #parentType}.
     * 
     * @param _parentType
     *          new value to set
     * @see #parentType
     */
    public void setParent(final String _parentType) {
      this.parentType = _parentType;
    }

    /**
     * adds a Attribute to the Definition
     * 
     * @param _name
     *          Name of the attribute
     * @param _type
     *          Name of the Attribute Type of the attribute.
     * @param _sqlTable
     *          Name of the SQL Table of the attribute.
     * @param _sqlColumn
     *          SQL Column of the attribute.
     * @param _typeLink
     *          Name of the Linked Type (used for links to another type).
     */
    public void addAttribute(final String _name, final String _type,
                             final String _sqlTable, final String _sqlColumn,
                             final String _typeLink) {
      this.attributes.add(new Attribute(_name, _type, _sqlTable, _sqlColumn,
          _typeLink));
    }

    /**
     * adds a Trigger to the Definition
     * 
     * @param _name
     *          name of the Trigger
     * @param _event
     *          event as defined in {@link org.efaps.admin.event. TriggerEvent}
     * @param _program
     *          name of the programm invoked in this trigger
     * @param _index
     *          index of the trigger
     */
    public void addTrigger(final String _name, final String _event,
                           final String _program, final String _index) {
      this.triggers.add(new Trigger(_name, _event, _program, _index));
    }
  }

  /**
   * The class defines an Tigger of a type.
   */
  private static class Trigger {

    /**
     * event as defined in {@link org.efaps.admin.event. TriggerEvent}
     */
    private final String event;

    /**
     * name of the programm invoked in this trigger
     */
    private final String program;

    /**
     * index of the trigger
     */
    private final String index;

    /**
     * name of the Trigger
     */
    private final String name;

    public Trigger(final String _name, final String _event,
        final String _program, final String _index) {
      this.name = _name;
      this.event = _event;
      this.program = _program;
      this.index = _index;
    }

    /**
     * For given type defined with the instance parameter, this trigger is
     * searched by typeID and indexposition. If the trigger exists, the trigger
     * is updated. Otherwise the trigger is created.
     * 
     * @param _instance
     *          type instance to update with this attribute
     * @param _typeName
     *          name of the type to update
     * 
     * 
     */
    protected void updateInDB(final Instance _instance, final String _typeName) {

      try {

        long typeID = _instance.getId();
        long progID = getProgID(_typeName);

        SearchQuery query = new SearchQuery();
        query.setQueryTypes(TriggerEvent.valueOf(this.event).name);
        query.addWhereExprEqValue("Abstract", typeID);
        query.addWhereExprEqValue("IndexPosition", this.index);
        query.addSelect("OID");
        query.executeWithoutAccessCheck();

        Update update;

        if (query.next()) {
          update = new Update((String) query.get("OID"));
        } else {
          update = new Insert(TriggerEvent.valueOf(this.event).name);
          update.add("Abstract", "" + typeID);
          update.add("IndexPosition", this.index);
          update.add("Name", this.name);
        }
        query.close();
        update.add("JavaProg", "" + progID);
        update.executeWithoutAccessCheck();

      } catch (EFapsException e) {
        LOG.error("updateInDB(Instance, String)", e);
      } catch (Exception e) {
        LOG.error("updateInDB(Instance, String)", e);
      }

    }

    /**
     * get the ID of the Program
     * 
     * @param _typeName
     *          Name of teh Type
     * @return id of the Program, 0 if not found
     * @throws EFapsException
     */
    private long getProgID(String _typeName) throws EFapsException {
      long id = 0;

      SearchQuery query = new SearchQuery();
      query.setQueryTypes("Admin_Program_Java");
      query.addSelect("ID");
      query.addWhereExprEqValue("Name", this.program);

      query.executeWithoutAccessCheck();
      if (query.next()) {
        id = (Long) query.get("ID");
      } else {
        LOG.error("type[" + _typeName + "]." + "Program [" + this.program
            + "]: " + "' not found");
      }
      return id;
    }

  }
}
