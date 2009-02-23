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

package org.efaps.update.ui;

import static org.efaps.admin.EFapsClassNames.FIELD;
import static org.efaps.admin.EFapsClassNames.FIELDCOMMAND;
import static org.efaps.admin.EFapsClassNames.FIELDGROUP;
import static org.efaps.admin.EFapsClassNames.FIELDHEADING;
import static org.efaps.admin.EFapsClassNames.FIELDSET;
import static org.efaps.admin.EFapsClassNames.FIELDTABLE;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;

import org.efaps.admin.datamodel.Type;
import org.efaps.admin.event.EventType;
import org.efaps.db.Delete;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.update.AbstractUpdate;
import org.efaps.update.LinkInstance;
import org.efaps.update.event.Event;
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
public abstract class AbstractCollectionUpdate extends AbstractUpdate {

  /** Link from field to icon. */
  private static final Link LINKFIELD2ICON = new Link("Admin_UI_LinkIcon",
                                                      "From",
                                                      "Admin_UI_Image", "To");

  /** Link from field to table as target. */
  private static final Link LINK2TARGETTABLE
                                          = new Link("Admin_UI_LinkTargetTable",
                                                     "From",
                                                     "Admin_UI_Table", "To");

  /**
   * @param _url        URL of the file
   * @param _typeName   name of the type
   */
  protected AbstractCollectionUpdate(final URL _url,
                                     final String _typeName) {
    super(_url, _typeName);
  }

  /**
   * Creates new instance of class {@link Definition}.
   *
   * @return new definition instance
   * @see Definition
   */
  @Override
  protected AbstractDefinition newDefinition() {
    return new Definition();
  }

  private class FieldDefinition extends AbstractDefinition {

    /** Name of the field. */
    private final String name;

    /** Icon of the field. */
    private String icon = null;

    /** set the character of the field. */
    private final String character;

    /**
     *
     * @param _name
     * @param _character
     */
    private FieldDefinition(final String _name,
                            final String _character) {
      this.name = _name;
      this.character = _character;
    }

    @Override
    protected void readXML(final List<String> _tags,
                           final Map<String, String> _attributes,
                           final String _text) {
      final String value = _tags.get(0);
      if ("evaluate".equals(value))  {
        if (_tags.size() == 1)  {
          this.events.add(new Event(_attributes.get("name"),
                                  EventType.UI_TABLE_EVALUATE,
                                  _attributes.get("program"),
                                  _attributes.get("method"),
                                  _attributes.get("index")));
        } else if ((_tags.size() == 2) && "property".equals(_tags.get(1))) {
          this.events.get(this.events.size() - 1).addProperty(_attributes.get("name"),
                                                            _text);
        } else  {
          super.readXML(_tags, _attributes, _text);
        }
      } else if ("icon".equals(value))  {
        this.icon = _text;
      } else if ("table".equals(value))  {
        // assigns a table as target for this field definition
        addLink(LINK2TARGETTABLE, new LinkInstance(_text));
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
     * Returns a string representation with values of all instance variables of
     * a field.
     *
     * @return string representation of this definition of a column
     */
    @Override
    public String toString() {
      return new ToStringBuilder(this).append("name", this.name).append(
          "properties", getProperties()).toString();
    }
  }

  private class Definition extends AbstractDefinition {

    /** All fields for the collection are stored in this variable */
    private final List<FieldDefinition> fields
                                            = new ArrayList<FieldDefinition>();

    /**
     * Current read field definition.
     *
     * @see #readXML(List, Map, String)
     */
    private FieldDefinition curField = null;

    @Override
    protected void readXML(final List<String> _tags,
                           final Map<String, String> _attributes,
                           final String _text) {
      final String value = _tags.get(0);
      if ("field".equals(value))  {
        if (_tags.size() == 1)  {
          this.curField = new FieldDefinition(_attributes.get("name"),
                                              _attributes.get("character"));
          this.fields.add(this.curField);
        } else  {
          this.curField.readXML(_tags.subList(1, _tags.size()),
                                _attributes,
                                _text);
        }
      } else  {
        super.readXML(_tags, _attributes, _text);
      }
    }

    /**
     * Updates / creates the instance in the database. Only the
     * fields are also updated for collection defined through this definiton.
     *
     * @param _instance instance to update (or null if instance is to create)
     * @param _allLinkTypes
     * @see #setFieldsInDB
     */
    @Override
    public void updateInDB(final Set<Link> _allLinkTypes)
        throws EFapsException {
      super.updateInDB(_allLinkTypes);
      setFieldsInDB();
    }

    /**
     * The fields for this collection are created and / or updated in the
     * database.
     *
     * @todo rework that a complete cleanup and create is not needed
     */
    protected void setFieldsInDB()
        throws EFapsException {
      // cleanup fields (remove all fields from table)
      final SearchQuery query = new SearchQuery();
      query.setExpand(this.instance, "Admin_UI_Field\\Collection");
      query.addSelect("OID");
      query.executeWithoutAccessCheck();
      while (query.next()) {
        final Instance field = new Instance((String) query.get("OID"));
        setPropertiesInDb(field, null);
        removeLinksInDB(field, LINKFIELD2ICON);
        removeLinksInDB(field, LINK2TARGETTABLE);
        (new Delete(field)).executeWithoutAccessCheck();
      }
      query.close();

      // append new fields
      for (final FieldDefinition field : this.fields) {
        Insert insert;
        if ("Command".equals(field.character)) {
          insert = new Insert(Type.get(FIELDCOMMAND));
        } else if ("Target".equals(field.character)) {
          insert = new Insert(Type.get(FIELDTABLE));
        } else if ("Heading".equals(field.character)) {
          insert = new Insert(Type.get(FIELDHEADING));
        } else if ("Group".equals(field.character)) {
          insert = new Insert(Type.get(FIELDGROUP));
        } else if ("Set".equals(field.character)) {
          insert = new Insert(Type.get(FIELDSET));
        } else {
          insert = new Insert(Type.get(FIELD));
        }

        insert.add("Collection", "" + this.instance.getId());
        insert.add("Name", field.name);
        insert.executeWithoutAccessCheck();
        setPropertiesInDb(insert.getInstance(), field.getProperties());

        if (field.icon != null) {
          final Set<LinkInstance> iconset = new HashSet<LinkInstance>();
          iconset.add(new LinkInstance(field.icon));
          setLinksInDB(insert.getInstance(), LINKFIELD2ICON, iconset);
        }

        // link to table
        setLinksInDB(insert.getInstance(),
                     LINK2TARGETTABLE,
                     field.getLinks(LINK2TARGETTABLE));

        // append events
        for (final Event event : field.getEvents()) {
          final Instance newInstance = event.updateInDB(insert.getInstance(),
                                                        field.name);
          setPropertiesInDb(newInstance, event.getProperties());
        }

      }
    }

    /**
     * Adds a new field to this definition of the table.
     *
     * @param _field  new field to add to this table
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
