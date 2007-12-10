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

package org.efaps.update.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.digester.Digester;
import org.apache.commons.lang.builder.ToStringBuilder;

import org.efaps.admin.AbstractAdminObject.EFapsClassName;
import org.efaps.db.Delete;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.update.AbstractUpdate;
import org.efaps.update.event.Event;
import org.efaps.update.event.EventFactory;
import org.efaps.util.EFapsException;

/**
 * This class imports/updates a Form or a Table using the
 * <code>org.apache.commons.digester.Digester</code> to create ojects and to
 * execute methods.
 *
 * @author tmo
 * @author jmox
 * @version $Id$
 * @todo description
 */
abstract class AbstractCollectionUpdate extends AbstractUpdate {

  /** Link from field to icon */
  private final static Link LINKFIELD2ICON =
      new Link("Admin_UI_LinkIcon", "From", "Admin_UI_Image", "To");

  /** Link from field to table as target */
  private final static Link LINK2TARGETTABLE =
      new Link("Admin_UI_LinkTargetTable", "From", "Admin_UI_Table", "To");

  private final static Set<Link> ALLLINKS = new HashSet<Link>();

  // ///////////////////////////////////////////////////////////////////////////
  // constructors

  /**
   * @param _typeName
   *                name of the type
   */
  protected AbstractCollectionUpdate(final String _typeName) {
    super(_typeName, ALLLINKS);
  }

  // ///////////////////////////////////////////////////////////////////////////
  // static methods

  /**
   *
   */
  protected static Digester createDigester(final String _xmlTagName,
                                           final Class<?> _createClass)
                                                                       throws IOException {
    final Digester digester = new Digester();
    digester.setValidating(false);
    digester.addObjectCreate(_xmlTagName, _createClass);

    // set the UUID
    digester.addCallMethod(_xmlTagName + "/uuid", "setUUID", 1);
    digester.addCallParam(_xmlTagName + "/uuid", 0);

    // #####################
    // add a new Definition
    digester.addObjectCreate(_xmlTagName + "/definition", Definition.class);
    digester.addSetNext(_xmlTagName + "/definition", "addDefinition");
    // set the Version
    digester
        .addCallMethod(_xmlTagName + "/definition/version", "setVersion", 4);
    digester.addCallParam(_xmlTagName + "/definition/version/application", 0);
    digester.addCallParam(_xmlTagName + "/definition/version/global", 1);
    digester.addCallParam(_xmlTagName + "/definition/version/local", 2);
    digester.addCallParam(_xmlTagName + "/definition/version/mode", 3);
    // set the name
    digester.addCallMethod(_xmlTagName + "/definition/name", "setName", 1);
    digester.addCallParam(_xmlTagName + "/definition/name", 0);
    // add a Property to the Definition
    digester.addCallMethod(_xmlTagName + "/definition/property", "addProperty",
        2);
    digester.addCallParam(_xmlTagName + "/definition/property", 0, "name");
    digester.addCallParam(_xmlTagName + "/definition/property", 1);

    // #####################
    // add a new FieldDefinition to the Definition
    digester.addObjectCreate(_xmlTagName + "/definition/field",
        FieldDefinition.class);
    digester.addSetNext(_xmlTagName + "/definition/field", "addField");
    // set the name of the field
    digester.addCallMethod(_xmlTagName + "/definition/field", "setName", 1);
    digester.addCallParam(_xmlTagName + "/definition/field", 0, "name");
    // set the character of the field
    digester
        .addCallMethod(_xmlTagName + "/definition/field", "setCharacter", 1);
    digester.addCallParam(_xmlTagName + "/definition/field", 0, "character");
    // set the icon for the field
    digester
        .addCallMethod(_xmlTagName + "/definition/field/icon", "setIcon", 1);
    digester.addCallParam(_xmlTagName + "/definition/field/icon", 0);
    // add a property to the field
    digester.addCallMethod(_xmlTagName + "/definition/field/property",
        "addProperty", 2);
    digester
        .addCallParam(_xmlTagName + "/definition/field/property", 0, "name");
    digester.addCallParam(_xmlTagName + "/definition/field/property", 1);

    // assign a table as target to the field
    digester.addCallMethod(_xmlTagName + "/definition/field/table",
        "assignTargetTable", 1);
    digester.addCallParam(_xmlTagName + "/definition/field/table", 0);

    // assign the event (TableEvaluateEvent) to fill the Table with data
    digester.addFactoryCreate(_xmlTagName + "/definition/field/evaluate",
        new EventFactory("Admin_UI_TableEvaluateEvent"), false);
    // add Properties to the event
    digester.addCallMethod(_xmlTagName + "/definition/field/evaluate/property",
        "addProperty", 2);
    digester.addCallParam(_xmlTagName + "/definition/field/evaluate/property",
        0, "name");
    digester.addCallParam(_xmlTagName + "/definition/field/evaluate/property",
        1);
    digester.addSetNext(_xmlTagName + "/definition/field/evaluate", "addEvent",
        "org.efaps.update.event.Event");

    // assign a Trigger to the field
    digester.addFactoryCreate(_xmlTagName + "/definition/field/trigger",
        new EventFactory());
    // add properties to the Trigger
    digester.addCallMethod(_xmlTagName + "/definition/field/trigger/property",
        "addProperty", 2);
    digester.addCallParam(_xmlTagName + "/definition/field/trigger/property",
        0, "name");
    digester
        .addCallParam(_xmlTagName + "/definition/field/trigger/property", 1);
    digester.addSetNext(_xmlTagName + "/definition/field/trigger", "addEvent",
        "org.efaps.update.event.Event");

    return digester;
  }

  // ///////////////////////////////////////////////////////////////////////////
  // class for a field

  public static class FieldDefinition extends AbstractDefinition {

    /** Name of the field. */
    private String name = null;

    /** Icon of the field. */
    private String icon = null;

    /** set the character of the field */
    private String character;

    /**
     * This is the setter method for instance variable {@link #name}.
     *
     * @param _name
     *                new value for instance variable {@link #name}
     * @see #name
     */
    @Override
    public void setName(final String _name) {
      this.name = _name;
    }

    /**
     * This is the setter method for instance variable {@link #character}
     *
     * @param _name
     *                new value for instance variable {@link #character}
     * @see #character
     */
    public void setCharacter(final String _character) {
      this.character = _character;
    }

    /**
     * This is the setter method for instance variable {@link #icon}.
     *
     * @param _name
     *                new value for instance variable {@link #icon}
     * @see #icon
     */
    public void setIcon(final String _icon) {
      this.icon = _icon;
    }

    /**
     * Assigns a table as target for this field definition.
     *
     * @param _targetTable
     *                name of the target table
     */
    public void assignTargetTable(final String _targetTable) {
      addLink(LINK2TARGETTABLE, _targetTable);
    }

    /**
     * Returns a string representation with values of all instance variables of
     * a field.
     *
     * @return string representation of this definition of a column
     */
    @Override
    public String toString() {
      return new ToStringBuilder(this).append("name", this.name).append(
          "properties", this.getProperties()).toString();
    }
  }

  // ///////////////////////////////////////////////////////////////////////////
  // class for the definitions

  public static class Definition extends AbstractDefinition {

    /** All fields for the collection are stored in this variable */
    private final List<FieldDefinition> fields =
        new ArrayList<FieldDefinition>();

    // /////////////////////////////////////////////////////////////////////////
    // instance methods

    /**
     * Updates / creates the instance in the database. Uses
     * {@link AbstractAjaxUpdateBehavior.updateInDB} for the update. Only the fields are
     * also updated for collection defined through this definiton.
     *
     * @param _instance
     *                instance to update (or null if instance is to create)
     * @param _allLinkTypes
     * @param _insert
     *                insert instance (if new instance is to create)
     * @see #setFieldsInDB
     */
    @Override
    public Instance updateInDB(final Instance _instance,
                               final Set<Link> _allLinkTypes,
                               final Insert _insert) throws EFapsException,
                                                    Exception {

      final Instance instance =
          super.updateInDB(_instance, _allLinkTypes, _insert);
      setFieldsInDB(instance);

      return instance;
    }

    /**
     * The fields for this collection are created and / or updated in the
     * database.
     *
     * @param _instance
     *                instance for which the fields must be updated / created
     * @todo rework that a complete cleanup and create is not needed
     * @todo remove throwing Exception
     */
    protected void setFieldsInDB(final Instance _instance)
                                                          throws EFapsException,
                                                          Exception {
      // cleanup fields (remove all fields from table)
      final SearchQuery query = new SearchQuery();
      query.setExpand(_instance, "Admin_UI_Field\\Collection");
      query.addSelect("OID");
      query.executeWithoutAccessCheck();
      while (query.next()) {
        final Instance field = new Instance((String) query.get("OID"));
        setPropertiesInDb(field, null);
        setLinksInDB(field, LINKFIELD2ICON, null);
        (new Delete(field)).executeWithoutAccessCheck();
      }
      query.close();

      // append new fields
      for (FieldDefinition field : this.fields) {
        Insert insert;
        if ("Target".equals(field.character)) {
          insert = new Insert(EFapsClassName.FIELDTABLE.name);
        } else if ("Heading".equals(field.character)) {
          insert = new Insert(EFapsClassName.FIELDHEADING.name);
        } else if ("Group".equals(field.character)) {
          insert = new Insert(EFapsClassName.FIELDGROUP.name);
        } else {
          insert = new Insert("Admin_UI_Field");
        }

        insert.add("Collection", "" + _instance.getId());
        insert.add("Name", field.name);
        insert.executeWithoutAccessCheck();
        setPropertiesInDb(insert.getInstance(), field.getProperties());
        final Map<String, Map<String, String>> iconsMap =
            new HashMap<String, Map<String, String>>();
        if (field.icon != null) {
          iconsMap.put(field.icon, null);
        }

        setLinksInDB(insert.getInstance(), LINKFIELD2ICON, iconsMap);
        setLinksInDB(insert.getInstance(), LINK2TARGETTABLE, field
            .getLinks(LINK2TARGETTABLE));
        for (Event event : field.getEvents()) {
          final Instance newInstance =
              event.updateInDB(insert.getInstance(), field.name);
          setPropertiesInDb(newInstance, event.getProperties());
        }

      }
    }

    /**
     * Adds a new field to this definition of the table.
     *
     * @param _field
     *                new field to add to this table
     * @see #fields
     * @see #Field
     */
    public void addField(final FieldDefinition _field) {
      this.fields.add(_field);
    }

    /**
     * Returns a string representation with values of all instance variables of
     * a field.
     *
     * @return string representation of this definition of a column
     */
    @Override
    public String toString() {
      return new ToStringBuilder(this).appendSuper(super.toString()).append(
          "fields", this.fields).toString();
    }
  }
}
