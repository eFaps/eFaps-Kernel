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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.efaps.admin.AdminObject.EFapsClassName;
import org.efaps.db.Context;
import org.efaps.db.Insert;
import org.efaps.db.SearchQuery;
import org.efaps.db.Update;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.util.EFapsException;

/**
 * @author jmox
 * @version $Id$
 */
public class UserAttributesSet {

  public static final String CONTEXTMAPKEY = "eFapsUserAttributes";

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
    readUserAttributes();
  }

  public boolean containsKey(final String _key) {
    return this.attributes.containsKey(_key);
  }

  public String getString(final String _key) {
    return this.attributes.get(_key).getValue();
  }

  public void set(final String _key, final String _value) {

    if (_key == null || _value == null) {
      // TODO Fehler schmeissen
    } else {
      final UserAttribute userattribute = this.attributes.get(_key);
      if (userattribute == null) {
        this.attributes.put(_key, new UserAttribute(_value.trim(), true));
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
          SearchQuery query = new SearchQuery();
          query.setQueryTypes(EFapsClassName.USER_ATTRIBUTE.name);
          query.addSelect("OID");
          query.addWhereExprEqValue("UserLink", this.userId.toString());
          query.addWhereExprEqValue("Key", entry.getKey());
          query.execute();
          Update update;
          if (query.next()) {
            update = new Update(query.get("OID").toString());
          } else {
            update = new Insert(EFapsClassName.USER_ATTRIBUTE.name);
            update.add("Key", entry.getKey());
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
    ConnectionResource rsrc = null;
    try {
      rsrc = Context.getThreadContext().getConnectionResource();
      Statement stmt = null;
      try {
        stmt = rsrc.getConnection().createStatement();

        String sql =
            " select ATTRIBUTEKEY,"
                + "        VALUE "
                + "from    V_USERATTRIBUTES "
                + "where   USERABSTRACTID = "
                + this.userId;

        final ResultSet resultset = stmt.executeQuery(sql);
        while (resultset.next()) {
          this.attributes.put(resultset.getString(1).trim(), new UserAttribute(
              resultset.getString(2).trim(), false));
        }

        resultset.close();
      } catch (SQLException e) {

      }
      finally {
        try {
          if (stmt != null) {
            stmt.close();
          }
        } catch (SQLException e) {

        }
      }
      rsrc.commit();
    } catch (EFapsException e) {

      e.printStackTrace();
    }
    finally {
      if ((rsrc != null) && rsrc.isOpened()) {
        try {
          rsrc.abort();
        } catch (EFapsException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }

  }

  private class UserAttribute {

    private String value;

    private boolean update;

    public UserAttribute(final String _value, final boolean _update) {
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
     * @param value
     *                the value to set
     */
    public void setValue(String value) {
      this.value = value;
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
     * @param update
     *                the update to set
     */
    public void setUpdate(boolean update) {
      this.update = update;
    }

  }

}
