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

package org.efaps.admin.user;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.efaps.admin.AbstractAdminObject.EFapsClassName;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.Insert;
import org.efaps.db.SearchQuery;
import org.efaps.db.Update;
import org.efaps.util.EFapsException;

/**
 * @author jmox
 * @version $Id$
 */
public class UserAttributesSet {

  public static final String CONTEXTMAPKEY = "eFapsUserAttributes";

  public enum UserAttributesDefinition {
    ATTRIBUTE("Admin_User_Attribute", "Key", "Value"),
    LOCALE("Admin_User_Attribute2Locale", null, "Locale.Language");

    public final String name;

    public final String keyAttribute;

    public final String valueAttribute;

    private UserAttributesDefinition(final String _name,
                                     final String _keyAttribute,
                                     final String _value) {
      this.name = _name;
      this.keyAttribute = _keyAttribute;
      this.valueAttribute = _value;
      MAPPER.put(_name, this);
    }

  }

  private final static Map<String, UserAttributesDefinition> MAPPER =
      new HashMap<String, UserAttributesDefinition>();

  private final Map<String, UserAttribute> attributes =
      new HashMap<String, UserAttribute>();

  private final Long userId;

  public UserAttributesSet(final String _userName) {
    long id = 0;
    try {
      id = Person.get(_userName).getId();
    } catch (EFapsException e) {
      e.printStackTrace();
    }
    this.userId = id;
    initialise();
  }

  public UserAttributesSet(final long _userId) {
    this.userId = _userId;
    initialise();
  }

  public void initialise() {
    UserAttributesDefinition.values();
    readUserAttributes();
  }

  public boolean containsKey(final String _key) {
    return this.attributes.containsKey(_key);
  }

  public String getString(final String _key) {
    String ret = null;
    if (this.attributes.containsKey(_key)) {
      ret = this.attributes.get(_key).getValue();
    }
    return ret;
  }

  public void set(final String _key, final String _value) {
    if (MAPPER.containsKey(_key)) {
      set(_key, _value, MAPPER.get(_key));
    } else {
      set(_key, _value, UserAttributesDefinition.ATTRIBUTE);
    }
  }

  public void set(final String _key, final String _value,
                  final UserAttributesDefinition _definition) {
    if (_key == null || _value == null) {
      // TODO Fehler schmeissen
    } else {
      final UserAttribute userattribute = this.attributes.get(_key);
      if (userattribute == null) {
        this.attributes.put(_key, new UserAttribute(_definition.name, _value
            .trim(), true));
      } else if (!userattribute.getValue().equals(_value.trim())) {
        userattribute.setUpdate(true);
        userattribute.setValue(_value.trim());
      }

    }
  }

  public void storeInDb() {
    try {
      for (Entry<String, UserAttribute> entry : this.attributes.entrySet()) {
        if (entry.getValue().isUpdate()) {
         final SearchQuery query = new SearchQuery();
          query.setQueryTypes(entry.getValue().getType());
          query.addSelect("OID");
          query.addWhereExprEqValue("UserLink", this.userId.toString());
          if (MAPPER.get(entry.getValue().getType()).keyAttribute != null) {
            query.addWhereExprEqValue(
                MAPPER.get(entry.getValue().getType()).keyAttribute, entry
                    .getKey());
          }
          query.execute();
          Update update;
          if (query.next()) {
            update = new Update(query.get("OID").toString());
          } else {
            update = new Insert(entry.getValue().getType());
            if (MAPPER.get(entry.getValue().getType()).keyAttribute != null) {
              update.add(MAPPER.get(entry.getValue().getType()).keyAttribute,
                  entry.getKey());
            }
            update.add("UserLink", this.userId.toString());
          }
          update.add("Value", entry.getValue().getValue());
          update.execute();
          update.close();
          query.close();
        }
      }
      this.attributes.clear();
    } catch (EFapsException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  private void readUserAttributes() {
    try {

     final Set<Type> types =
          Type.get(EFapsClassName.USER_ATTRIBUTEABSTRACT.name).getChildTypes();
      for (Type type : types) {
        if (MAPPER.containsKey(type.getName())) {
         final UserAttributesDefinition definition = MAPPER.get(type.getName());
         final SearchQuery query = new SearchQuery();

          query.setQueryTypes(definition.name);

          query.addSelect(definition.valueAttribute);
          if (definition.keyAttribute != null) {
            query.addSelect(definition.keyAttribute);
          }
          query.addWhereExprEqValue("UserLink", this.userId);
          query.execute();
          while (query.next()) {
            String key;
            if (definition.keyAttribute == null) {
              key = definition.name;
            } else {
              key = query.get(definition.keyAttribute).toString().trim();
            }
            this.attributes.put(key, new UserAttribute(definition.name, query
                .get(definition.valueAttribute).toString().trim(), false));
          }
        }
      }
    } catch (EFapsException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private class UserAttribute {

    private String value;

    private boolean update;

    private final String type;

    public UserAttribute(final String _type, final String _value,
                         final boolean _update) {
      this.type = _type;
      this.value = _value;
      this.update = _update;
    }

    /**
     * This is the getter method for the instance variable {@link #value}.
     *
     * @return value of instance variable {@link #value}
     */
    public String getValue() {
      return this.value;
    }

    /**
     * This is the setter method for the instance variable {@link #value}.
     *
     * @param _value
     *                the value to set
     */
    public void setValue(final String _value) {
      this.value = _value;
    }

    /**
     * This is the getter method for the instance variable {@link #update}.
     *
     * @return value of instance variable {@link #update}
     */
    public boolean isUpdate() {
      return this.update;
    }

    /**
     * This is the setter method for the instance variable {@link #update}.
     *
     * @param _update
     *                the update to set
     */
    public void setUpdate(final boolean _update) {
      this.update = _update;
    }

    /**
     * This is the getter method for the instance variable {@link #type}.
     *
     * @return value of instance variable {@link #type}
     */
    public String getType() {
      return this.type;
    }

  }

}
