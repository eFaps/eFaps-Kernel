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

package org.efaps.admin.common;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import org.efaps.db.Context;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.Cache;
import org.efaps.util.cache.CacheObjectInterface;
import org.efaps.util.cache.CacheReloadException;
import org.efaps.util.cache.CacheReloadInterface;

/**
 * TODO description
 *
 * @author jmox
 * @version $Id$
 */
public class SystemAttribute implements CacheObjectInterface {

  /**
   * this static Variable contains the SQL-Statment used to retrieve the
   * SystemAttributes from the efps-Database
   */
  private static final String SQL_SELECT =
      " select ID, UUID, NAME, VALUE from V_COMMONSYSATTRIBUTE";

  /**
   * Stores all instances of SytemAttribute.
   */
  private static Cache<SystemAttribute> SYSATTRIBUTECACHE =
      new Cache<SystemAttribute>(new CacheReloadInterface() {

        public int priority() {
          return CacheReloadInterface.Priority.SystemAttribute.number;
        };

        public void reloadCache() throws CacheReloadException
        {
          SystemAttribute.initialise();
        };
      });

  /**
   * The instance variable stores the id of this SystemAttribute.
   *
   * @see #getId()
   */
  private final long id;

  /**
   * The instance variable stores the UUID of this SystemAttribute.
   *
   * @see #getUUID()
   */
  private final UUID uuid;

  /**
   * The instance variable stores the Name of this SystemAttribute.
   *
   * @see #getName()
   */
  private final String name;

  /**
   * The instance variable stores the Value of this SystemAttribute.
   *
   * @see #getValue()
   * @see #getIntegerValue()
   * @see #getStringValue()
   */
  private final Object value;

  public SystemAttribute(final long _id, final String _uuid, final String _name,
                        final Object _value) {
    this.id = _id;
    this.uuid = UUID.fromString(_uuid);
    this.name = _name;
    this.value = _value;
  }

  /**
   * static method used to initialise the SystemAttributes<br>
   * (It is called from the RunLevel-Definitions)
   *
   * @throws CacheReloadException
   */
  public static void initialise() throws CacheReloadException {

    try {

      ConnectionResource con =
          Context.getThreadContext().getConnectionResource();
      Statement stmt = con.getConnection().createStatement();
      ResultSet resultset = stmt.executeQuery(SQL_SELECT);

      while (resultset.next()) {
        long id = resultset.getLong(1);
        String uuid = resultset.getString(2).trim();
        String name = resultset.getString(3).trim();
        Object value = resultset.getObject(4);
        SystemAttribute sysatt = new SystemAttribute(id, uuid, name, value);
        getSytemAttributeCache().add(sysatt);
      }
    } catch (EFapsException e) {
      throw new CacheReloadException("could not read SystemAttribute", e);
    } catch (SQLException e) {
      throw new CacheReloadException("could not read SystemAttribute", e);
    }

  }

  /**
   * Returns for given parameter <i>_id</i> the instance of class
   * {@link #SytemAttribute()}.
   *
   * @return instance of class {@link #SytemAttribute()}
   */
  public static SystemAttribute get(final long _id) {
    return getSytemAttributeCache().get(_id);
  }

  /**
   * Returns for given parameter <i>_name</i> the instance of class
   * {@link #SytemAttribute()}.
   *
   * @return instance of class {@link #SytemAttribute()}
   */
  public static SystemAttribute get(final String _name) {
    return getSytemAttributeCache().get(_name);
  }

  /**
   * Returns for given parameter <i>_uuid</i> the instance of class
   * {@link #SytemAttribute()}
   *
   * @return instance of class {@link #SytemAttribute()}
   */
  public static SystemAttribute get(final UUID _uuid) {
    return getSytemAttributeCache().get(_uuid);
  }

  /**
   * This is the getter method for the instance variable
   * {@link #sYSATTRIBUTECACHE}.
   *
   * @return value of instance variable {@link #sYSATTRIBUTECACHE}
   */
  static Cache<SystemAttribute> getSytemAttributeCache() {
    return SYSATTRIBUTECACHE;
  }

  /**
   * This is the getter method for the instance variable {@link #id}.
   *
   * @return value of instance variable {@link #id}
   */
  public long getId() {
    return this.id;
  }

  /**
   * This is the getter method for the instance variable {@link #uuid}.
   *
   * @return value of instance variable {@link #uuid}
   */
  public UUID getUUID() {
    return this.uuid;
  }

  /**
   * This is the getter method for the instance variable {@link #name}.
   *
   * @return value of instance variable {@link #name}
   */
  public String getName() {
    return this.name;
  }

  /**
   * This is the getter method for the instance variable {@link #value}.
   *
   * @return value of instance variable {@link #value}
   */
  public Object getValue() {
    return this.value;
  }

  /**
   * Method that returns the Value of the instance variable {@link #value}
   * casted to String;
   *
   * @return value of instance variable {@link #value} as String
   */
  public String getStringValue() {
    return this.value.toString().trim();
  }

  /**
   * Method that returns the Value of the instance variable {@link #value}
   * casted to Integer;
   *
   * @return value of instance variable {@link #value} as Integer
   */
  public int getIntegerValue() {
    return Integer.parseInt(this.value.toString());
  }

  /**
   * Method that returns the Value of the instance variable {@link #value}
   * casted to Boolean;
   *
   * @return value of instance variable {@link #value} as boolean, true if the
   *         Object equals "true", else false
   */
  public boolean getBooleanValue() {
    return Boolean.parseBoolean(this.value.toString());
  }

}
