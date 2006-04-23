/*
 * Copyright 2006 The eFaps Team
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

package org.efaps.admin;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;

import org.efaps.db.CacheInterface;
import org.efaps.db.Context;

/**
 *
 */
public abstract class AdminObject implements CacheInterface  {

  /**
   *
   */
  public enum EFapsClassName  {
    DATAMODEL_TYPE("Admin_DataModel_Type"),

    EVENT_DEFINITION                ("Admin_Event_Definition"),

    COLLECTION("Admin_UI_Collection"),
    FIELD("Admin_UI_Field"),
    FORM("Admin_UI_Form"),
    TABLE("Admin_UI_Table"),
    COMMAND("Admin_UI_Command"),
    MENU("Admin_UI_Menu"),
    SEARCH("Admin_UI_Search"),
    IMAGE("Admin_UI_Image"),

    LINK_ICON("Admin_UI_LinkIcon"),
    LINK_TARGET_FORM("Admin_UI_LinkTargetForm"),
    LINK_TARGET_MENU("Admin_UI_LinkTargetMenu"),
    LINK_TARGET_SEARCH("Admin_UI_LinkTargetSearch"),
    LINK_TARGET_TABLE("Admin_UI_LinkTargetTable");

    public final String name;

    private EFapsClassName(final String _name)  {
      this.name = _name;
mapper.put(this.name, this);
    }

static public EFapsClassName getEnum(final String _name) {
  return mapper.get(_name);
}
  }
static private Map<String,EFapsClassName> mapper = new HashMap<String,EFapsClassName>();


  /////////////////////////////////////////////////////////////////////////////

  /**
   * Constructor to set the id and name of the user interface object.
   *
   * @param _id         id to set
   * @param _name name  to set
   */
  protected AdminObject(long _id, String _name)  {
    setId(_id);
    setName(_name);
  }

  /**
   * Sets the link properties for this object.
   *
   * @param _context  eFaps context for this request
   * @param _linkType type of the link property
   * @param _toId     to id
   * @param _toType   to type
   * @param _toName   to name
   */
  protected void setLinkProperty(Context _context, EFapsClassName _linkType, long _toId, EFapsClassName _toType, String _toName) throws Exception  {
  }

  /**
   * The instance method sets all properties of this administrational object.
   * All properties are stores in instance variable {@link #properties}.
   *
   * @see #properties
   * @param _context  context for this request
   * @param _name     name of the property (key)
   * @param _value    value of the property
   */
  protected void setProperty(Context _context, String _name, String _value) throws Exception  {
    getProperties().put(_name, _value);
  }

  /**
   * The value of the given property is returned.
   *
   * @see #properties
   * @param _name     name of the property (key)
   * @return value of the property with the given name / key.
   */
  public String getProperty(String _name)  {
    return getProperties().get(_name);
  }

  /**
   * The method overrides the original method 'toString' and returns the name
   * of the user interface object.
   *
   * @return name of the user interface object
   */
  public String toString()  {
    return new ToStringBuilder(this).
      append("name", getName()).
      append("id", getId()).
      append("properties", getProperties()).
      toString();
  }

  /////////////////////////////////////////////////////////////////////////////

  /**
   * The instance variable stores the id of the collections object.
   *
   * @see #setId
   * @see #getId
   */
  private long id = 0;

  /**
   * This is the instance variable for the name of the collection instance.
   *
   * @see #setName
   * @see #getName
   */
  private String name = null;

  /**
   * This is the instance variable for the properties.
   *
   * @getProperties
   */
  private Map<String,String> properties = new HashMap<String,String>();

  /////////////////////////////////////////////////////////////////////////////

  /**
   * This is the setter method for instance variable {@link #id}.
   *
   * @param _id new value for instance variable {@link #id}
   * @see #id
   * @see #getId
   */
  public void setId(long _id)  {
    this.id = _id;
  }

  /**
   * This is the getter method for instance variable {@link #id}.
   *
   * @return value of instance variable {@id}
   * @see #id
   * @see #setId
   */
  public long getId()  {
    return this.id;
  }

  /**
   * This is the setter method for instance variable {@link #name}.
   *
   * @param _name new value for instance variable {@link #name}
   * @see #name
   * @see #getName
   */
  public void setName(String _name)  {
    this.name = (_name==null ? null : _name.trim());
  }

  /**
   * This is the getter method for instance variable {@link #name}.
   *
   * @return value of instance variable {@name}
   * @see #name
   * @see #setName
   */
  public String getName()  {
    return this.name;
  }

  /**
   * This is the getter method for instance variable {@link #properties}.
   *
   * @return value of instance variable {@properties}
   * @see #properties
   */
  protected Map<String,String> getProperties()  {
    return this.properties;
  }
}