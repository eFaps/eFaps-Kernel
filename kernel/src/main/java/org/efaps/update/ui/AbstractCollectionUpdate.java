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

import org.efaps.db.Delete;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.update.AbstractUpdate;
import org.efaps.update.event.Event;
import org.efaps.update.event.EventFactory;
import org.efaps.util.EFapsException;

/**
 * @author tmo
 * @version $Id$
 * @todo description
 */
abstract class AbstractCollectionUpdate extends AbstractUpdate {

  /** Link from field to icon */
  private final static Link      LINKFIELD2ICON = new Link("Admin_UI_LinkIcon",
                                                    "From", "Admin_UI_Image",
                                                    "To");

  private final static Set<Link> ALLLINKS       = new HashSet<Link>();

  // ///////////////////////////////////////////////////////////////////////////
  // constructors

  /**
   * @param _typeName
   *          name of the type
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
  throws IOException  {
    Digester digester = new Digester();
    digester.setValidating(false);
    digester.addObjectCreate(_xmlTagName, _createClass);

    digester.addCallMethod(_xmlTagName + "/uuid", "setUUID", 1);
    digester.addCallParam(_xmlTagName + "/uuid", 0);

    digester.addObjectCreate(_xmlTagName + "/definition", Definition.class);
    digester.addSetNext(_xmlTagName + "/definition", "addDefinition");

    digester.addCallMethod(_xmlTagName + "/definition/version", "setVersion", 4);
    digester.addCallParam(_xmlTagName + "/definition/version/application", 0);
    digester.addCallParam(_xmlTagName + "/definition/version/global", 1);
    digester.addCallParam(_xmlTagName + "/definition/version/local", 2);
    digester.addCallParam(_xmlTagName + "/definition/version/mode", 3);

    digester.addCallMethod(_xmlTagName + "/definition/name", "setName", 1);
    digester.addCallParam(_xmlTagName + "/definition/name", 0);

    digester.addCallMethod(_xmlTagName + "/definition/property", "addProperty", 2);
    digester.addCallParam(_xmlTagName + "/definition/property", 0, "name");
    digester.addCallParam(_xmlTagName + "/definition/property", 1);

    digester.addObjectCreate(_xmlTagName + "/definition/field", Field.class);
    digester.addSetNext(_xmlTagName + "/definition/field", "addField");

    digester.addCallMethod(_xmlTagName + "/definition/field", "setName", 1);
    digester.addCallParam(_xmlTagName + "/definition/field", 0, "name");

    digester.addCallMethod(_xmlTagName + "/definition/field/icon", "setIcon",
        1);
    digester.addCallParam(_xmlTagName + "/definition/field/icon", 0);

    digester.addCallMethod(_xmlTagName + "/definition/field/property", "addProperty", 2);
    digester.addCallParam(_xmlTagName + "/definition/field/property", 0, "name");
    digester.addCallParam(_xmlTagName + "/definition/field/property", 1);

    digester.addFactoryCreate(_xmlTagName + "/definition/field/trigger", new EventFactory());

    digester.addCallMethod(_xmlTagName + "/definition/field/trigger/property", "addProperty", 2);
    digester.addCallParam(_xmlTagName + "/definition/field/trigger/property", 0, "name");
    digester.addCallParam(_xmlTagName + "/definition/field/trigger/property", 1);
    digester.addSetNext(_xmlTagName + "/definition/field/trigger", "addTrigger", "org.efaps.update.event.Event");
    
    return digester;
  }

  // ///////////////////////////////////////////////////////////////////////////
  // class for a field

  public static class Field {

    /** Name of the field. */
    private String                    name       = null;

    /** Icon of the field. */
    private String                    icon       = null;

    /**
     * Property value depending on the property name for this field of a
     * definition.
     * 
     * @see #addProperty.
     */
    private final Map<String, String> properties = new HashMap<String, String>();

    /**
     * Add a new property with given name and value to this definition.
     * 
     * @param _name
     *          name of the property to add
     * @param _value
     *          value of the property to add
     * @see #properties
     */
    public void addProperty(final String _name, final String _value) {
      this.properties.put(_name, _value);
    }

    /**
     * This is the setter method for instance variable {@link #name}.
     * 
     * @param _name
     *          new value for instance variable {@link #name}
     * @see #name
     */
    public void setName(final String _name) {
      this.name = _name;
    }

    /**
     * This is the setter method for instance variable {@link #icon}.
     * 
     * @param _name
     *          new value for instance variable {@link #icon}
     * @see #icon
     */
    public void setIcon(final String _icon) {
      this.icon = _icon;
    }

    private final List<Event> triggers = new ArrayList<Event>();

    public void addTrigger(final Event _event) {
      triggers.add(_event);
    }

    /**
     * Returns a string representation with values of all instance variables of
     * a field.
     * 
     * @return string representation of this definition of a column
     */
    public String toString() {
      return new ToStringBuilder(this).append("name", this.name).append(
          "properties", this.properties).toString();
    }
  }

  // ///////////////////////////////////////////////////////////////////////////
  // class for the definitions

  public static class Definition extends DefinitionAbstract {

    /** All fields for the collection are stored in this variable */
    private List<Field> fields = new ArrayList<Field>();

    // /////////////////////////////////////////////////////////////////////////
    // instance methods

    /**
     * Updates / creates the instance in the database. Uses
     * {@link AbstractUpdate.updateInDB} for the update. Only the fields are
     * also updated for collection defined through this definiton.
     * 
     * @param _instance
     *          instance to update (or null if instance is to create)
     * @param _allLinkTypes
     * @param _insert
     *          insert instance (if new instance is to create)
     * @see #setFieldsInDB
     */
    public Instance updateInDB(final Instance _instance,
                               final Set<Link> _allLinkTypes,
                               final Insert _insert) throws EFapsException,
                                                    Exception {

      Instance instance = super.updateInDB(_instance, _allLinkTypes, _insert);
      setFieldsInDB(instance);

      return instance;
    }

    /**
     * The fields for this collection are created and / or updated in the
     * database.
     * 
     * @param _instance
     *          instance for which the fields must be updated / created
     * @todo rework that a complete cleanup and create is not needed
     * @todo remove throwing Exception
     */
    protected void setFieldsInDB(final Instance _instance)
                                                          throws EFapsException,
                                                          Exception {
      // cleanup fields (remove all fields from table)
      SearchQuery query = new SearchQuery();
      query.setExpand(_instance, "Admin_UI_Field\\Collection");
      query.addSelect("OID");
      query.executeWithoutAccessCheck();
      while (query.next()) {
        Instance field = new Instance((String) query.get("OID"));
        setPropertiesInDb(field, null);
        setLinksInDB(field, LINKFIELD2ICON, null);
        (new Delete(field)).executeWithoutAccessCheck();
      }
      query.close();

      // append new fields
      for (Field field : this.fields) {
        Insert insert = new Insert("Admin_UI_Field");
        insert.add("Collection", "" + _instance.getId());
        insert.add("Name", field.name);
        insert.executeWithoutAccessCheck();
        setPropertiesInDb(insert.getInstance(), field.properties);
        Map<String, Map<String, String>> iconsMap = new HashMap<String, Map<String, String>>();
        if (field.icon != null) {
          iconsMap.put(field.icon, null);
        }
        setLinksInDB(insert.getInstance(), LINKFIELD2ICON, iconsMap);

        for (Event event : field.triggers) {
          Instance newInstance = event.updateInDB(insert.getInstance(), field.name);
          setPropertiesInDb(newInstance, event.getProperties());
        }

      }
    }

    /**
     * Adds a new field to this definition of the table.
     * 
     * @param _field
     *          new field to add to this table
     * @see #fields
     * @see #Field
     */
    public void addField(final Field _field) {
      this.fields.add(_field);
    }

    /**
     * Returns a string representation with values of all instance variables of
     * a field.
     * 
     * @return string representation of this definition of a column
     */
    public String toString() {
      return new ToStringBuilder(this).appendSuper(super.toString()).append(
          "fields", this.fields).toString();
    }
  }
}
