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

package org.efaps.admin.common;

import static org.efaps.admin.EFapsClassNames.CONFIG_ATTR;
import static org.efaps.admin.EFapsClassNames.CONFIG_LINK;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.Cache;
import org.efaps.util.cache.CacheObjectInterface;
import org.efaps.util.cache.CacheReloadException;

/**
 * TODO description.
 *
 * @author jmox
 * @version $Id$
 */
public class SystemConfiguration implements CacheObjectInterface {

  /**
   * this static Variable contains the SQL-Statment used to retrieve the
   * SystemAttributes from the efps-Database.
   */
  private static final String SQL_SELECT =
      " select CONFIGID, CONFIGNAME, CONFIGUUID, KEY, VALUE, UUID "
     + "from V_CMSYSCONF";

  /**
   * Stores all instances of SytemAttribute.
   */
  private static SystemConfigurationCache CACHE = new SystemConfigurationCache();

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

  private final Map<String,String> attributes = new HashMap<String,String>();

  private final Map<String,String> links = new HashMap<String,String>();
  /**
   * Constructor setting instance variables.
   *
   * @param _id     id of the SystemConfiguration
   * @param _uuid   uuid of the SystemConfiguration
   * @param _name   name of the SystemConfiguration
   *
   */
  protected SystemConfiguration(final long _id, final String _name,
                                final String _uuid) {
    this.id = _id;
    this.uuid = UUID.fromString(_uuid);
    this.name = _name;
  }

  /**
   * @param _key
   * @param _value
   */
  private void addAttribute(final String _key, final String _value) {
    this.attributes.put(_key, _value);
  }

  /**
   * @param _key
   * @param _value
   */
  private void addLink(final String _key, final String _value) {
    this.links.put(_key, _value);
  }

  /**
   * Returns for given parameter <i>_id</i> the instance of class
   * {@link #SytemAttribute()}.
   * @param _id  id of the SystemConfiguration
   * @return instance of class {@link #SytemAttribute()}
   * @throws CacheReloadException
   */
  public static SystemConfiguration get(final long _id) {
    return CACHE.get(_id);
  }

  /**
   * Returns for given parameter <i>_name</i> the instance of class
   * {@link #SytemAttribute()}.
   * @param _name  name of the Systemattribute
   * @return instance of class {@link #SytemAttribute()}
   * @throws CacheReloadException
   */
  public static SystemConfiguration get(final String _name) {
    return CACHE.get(_name);
  }

  /**
   * Returns for given parameter <i>_uuid</i> the instance of class
   * {@link #SytemAttribute()}.
   *
   * @param _uuid  uuid of the SystemConfiguration
   * @return instance of class {@link #SytemAttribute()}
   * @throws CacheReloadException
   */
  public static SystemConfiguration get(final UUID _uuid) {
    return CACHE.get(_uuid);
  }

  /**
   * This is the getter method for the instance variable {@link #id}.
   *
   * @return value of instance variable {@link #id}
   */
  public final long getId() {
    return this.id;
  }

  /**
   * This is the getter method for the instance variable {@link #uuid}.
   *
   * @return value of instance variable {@link #uuid}
   */
  public final UUID getUUID() {
    return this.uuid;
  }

  /**
   * This is the getter method for the instance variable {@link #name}.
   *
   * @return value of instance variable {@link #name}
   */
  public final String getName() {
    return this.name;
  }

  /**
   * This is the getter method for the instance variable {@link #value}.
   *
   * @return value of instance variable {@link #value}
   */
  public final Instance getLink(final String _key) {
    return Instance.get(this.links.get(_key));
  }

  public final String getAttributeValue(final String _key) {
    return this.attributes.get(_key);
  }

  public final boolean getAttributeValueAsBoolean(final String _key) {
    return Boolean.parseBoolean(this.attributes.containsKey(_key)
                                ? this.attributes.get(_key)
                                : "false");
  }

  public final int getAttributeValueAsInteger(final String _key) {
    return Integer.parseInt(this.attributes.containsKey(_key)
                            ? this.attributes.get(_key)
                            : "0");
  }

  /**
   * Method to initialize the Cache of this CacheObjectInterface.
   */
  public static void initialize() {
    CACHE.initialize(SystemConfiguration.class);
  }

  private static class SystemConfigurationCache extends Cache<SystemConfiguration> {

    @Override
    protected void readCache(final Map<Long, SystemConfiguration> _newCache4Id,
                             final Map<String, SystemConfiguration> _newCache4Name,
                             final Map<UUID, SystemConfiguration> _newCache4UUID)
        throws CacheReloadException {
      try {

        final ConnectionResource con =
            Context.getThreadContext().getConnectionResource();
        final Statement stmt = con.getConnection().createStatement();
        final ResultSet resultset = stmt.executeQuery(SQL_SELECT);
        long id = 0;
        SystemConfiguration config = null;
        while (resultset.next()) {
          final long configId = resultset.getLong(1);
          final String configName = resultset.getString(2).trim();
          final String configUUID = resultset.getString(3).trim();
          final String key = resultset.getString(4).trim();
          final String value = resultset.getString(5).trim();
          final String uuid = resultset.getString(6).trim();
          if (id != configId) {
            id = configId;
            config = new SystemConfiguration(configId,
                                             configName,
                                             configUUID);
            _newCache4Id.put(config.getId(), config);
            _newCache4Name.put(config.getName(), config);
            _newCache4UUID.put(config.getUUID(), config);
          }
          final UUID uuidTmp = UUID.fromString(uuid);
          if (uuidTmp.equals(CONFIG_ATTR.getUuid())) {
            config.addAttribute(key, value);
          } else if (uuidTmp.equals(CONFIG_LINK.getUuid())) {
            config.addLink(key, value);
          }
        }
      } catch (final EFapsException e) {
        throw new CacheReloadException("could not read SystemConfiguration", e);
      } catch (final SQLException e) {
        throw new CacheReloadException("could not read SystemConfiguration", e);
      }

    }
  }
}
