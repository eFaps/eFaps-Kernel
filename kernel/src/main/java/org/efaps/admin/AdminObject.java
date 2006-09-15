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
import java.util.UUID;

import org.apache.commons.lang.builder.ToStringBuilder;

import org.efaps.db.CacheInterface;
import org.efaps.db.Context;

/**
 *
 *
 * @author tmo
 * @version $Id$
 */
public abstract class AdminObject implements CacheInterface  {

  /**
   *
   */
  public enum EFapsClassName  {
    DATAMODEL_TYPE("Admin_DataModel_Type"),

    USER_ABSTRACT("Admin_User_Abstract"),
    USER_PERSON("Admin_User_Person"),
    USER_ROLE("Admin_User_Role"),
    USER_GROUP("Admin_User_Group"),
    USER_JAASKEY("Admin_User_JAASKey"),
    USER_JAASSYSTEM("Admin_User_JAASSystem"),

    USER_ABSTRACT2ABSTRACT("Admin_User_Abstract2Abstract"),
    USER_PERSON2ROLE("Admin_User_Person2Role"),
    USER_PERSON2GROUP("Admin_User_Person2Group"),

    LIFECYCLE_POLICY("Admin_LifeCycle_Policy"),
    LIFECYCLE_STATUS("Admin_LifeCycle_Status"),

    EVENT_DEFINITION("Admin_Event_Definition"),

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
  // instance variables

  /**
   * The instance variable stores the id of the collections object.
   *
   * @see #getId
   */
  private final long id;

  /**
   * This is the instance variable for the universal unique identifier of this
   * admin object.
   *
   * @see #getUUID
   */
  private final UUID uuid;

  /**
   * This is the instance variable for the name of this admin object.
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
  private final Map < String, String > properties 
                                          = new HashMap < String, String > ();

  /////////////////////////////////////////////////////////////////////////////
  // constructors

  /**
   * Constructor to set instance variables {@link #id}, {@link #uuid} and 
   * {@link #name} of this administrational object.
   *
   * @param _id         id to set
   * @param _uuid       universal unique identifier
   * @param _name name  to set
   * @see #id
   * @see #uuid
   * @see #name
   */
  protected AdminObject(final long _id, 
                        final String _uuid, 
                        final String _name)  {
    this.id = _id;
    this.uuid = (_uuid == null) ? null : UUID.fromString(_uuid.trim());
    setName(_name);
  }

  /////////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * Sets the link properties for this object.
   *
   * @param _context  eFaps context for this request
   * @param _linkType type of the link property
   * @param _toId     to id
   * @param _toType   to type
   * @param _toName   to name
   */
  protected void setLinkProperty(final Context _context, 
                                 final EFapsClassName _linkType, 
                                 final long _toId, 
                                 final EFapsClassName _toType, 
                                 final String _toName) throws Exception  {
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
  protected void setProperty(final Context _context, 
                             final String _name, 
                             final String _value) throws Exception  {
    getProperties().put(_name, _value);
  }

  /**
   * The value of the given property is returned.
   *
   * @see #properties
   * @param _name     name of the property (key)
   * @return value of the property with the given name / key.
   */
  public String getProperty(final String _name)  {
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
      append("uuid", getUUID()).
      append("id", getId()).
      append("properties", getProperties()).
      toString();
  }

  /////////////////////////////////////////////////////////////////////////////
  // getter and setter instance methods
  
  /**
   * This is the getter method for instance variable {@link #id}.
   *
   * @return value of instance variable {@id}
   * @see #id
   */
  public long getId()  {
    return this.id;
  }

  /**
   * This is the getter method for instance variable {@link #uuid}.
   *
   * @return value of instance variable {@uuid}
   * @see #uuid
   */
  public UUID getUUID()  {
    return this.uuid;
  }

  /**
   * This is the setter method for instance variable {@link #name}.
   *
   * @param _name new value for instance variable {@link #name}
   * @see #name
   * @see #getName
   */
  protected void setName(final String _name)  {
    this.name = (_name == null) ? null : _name.trim();
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
  protected Map < String, String > getProperties()  {
    return this.properties;
  }
}