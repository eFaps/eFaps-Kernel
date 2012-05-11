/*
 * Copyright 2003 - 2012 The eFaps Team
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

import java.io.IOException;
import java.io.StringReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.efaps.admin.user.Company;
import org.efaps.ci.CIAdminCommon;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.AbstractCache;
import org.efaps.util.cache.CacheObjectInterface;
import org.efaps.util.cache.CacheReloadException;

/**
 * The class handles the caching for system configurations with their attributes
 * and links.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public final class SystemConfiguration
    implements CacheObjectInterface
{

    /**
     * This static Variable contains the SQL statement used to retrieve the
     * SystemAttributes from the eFaps database.
     *
     * @see SystemConfigurationCache#readCache(Map, Map, Map)
     */
    private static final SQLSelect SELECT = new SQLSelect()
                                                .column("CONFIGID")
                                                .column("CONFIGNAME")
                                                .column("CONFIGUUID")
                                                .column("KEY")
                                                .column("VALUE")
                                                .column("UUID")
                                                .column("COMPANYID")
                                                .from("V_CMSYSCONF");

    /**
     * Caches all instances of {@link SystemConfiguration}.
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

    /**
     * Map with all attributes for this system configuration.
     */
    private final Map<Long, Map<String, String>> attributes = new HashMap<Long, Map<String, String>>();

    /**
     * Map with all links for this system configuration.
     */
    private final Map<Long, Map<String, String>> links = new HashMap<Long, Map<String, String>>();

    /**
     * Map with all object attributes for this system configuration.
     */
    private final Map<Long, Map<String, String>> objectAttributes = new HashMap<Long, Map<String, String>>();

    /**
     * Constructor setting instance variables.
     *
     * @param _id id of the SystemConfiguration
     * @param _uuid uuid of the SystemConfiguration
     * @param _name name of the SystemConfiguration
     */
    private SystemConfiguration(final long _id,
                                final String _name,
                                final String _uuid)
    {
        this.id = _id;
        this.uuid = UUID.fromString(_uuid);
        this.name = _name;
        this.attributes.put(new Long(0), new HashMap<String, String>());
        this.links.put(new Long(0), new HashMap<String, String>());
        this.objectAttributes.put(new Long(0), new HashMap<String, String>());
    }

    /**
     * Returns for given parameter <i>_id</i> the instance of class
     * {@link SystemConfiguration}.
     *
     * @param _id id of the system configuration
     * @return instance of class {@link SystemConfiguration}
     * @throws CacheReloadException
     */
    public static SystemConfiguration get(final long _id)
    {
        return SystemConfiguration.CACHE.get(_id);
    }

    /**
     * Returns for given parameter <i>_name</i> the instance of class
     * {@link SystemConfiguration}.
     *
     * @param _name name of the system configuration
     * @return instance of class {@link SystemConfiguration}
     * @throws CacheReloadException
     */
    public static SystemConfiguration get(final String _name)
    {
        return SystemConfiguration.CACHE.get(_name);
    }

    /**
     * Returns for given parameter <i>_uuid</i> the instance of class
     * {@link SystemConfiguration}.
     *
     * @param _uuid uuid of the system configuration
     * @return instance of class {@link SystemConfiguration}
     * @throws CacheReloadException
     */
    public static SystemConfiguration get(final UUID _uuid)
    {
        return SystemConfiguration.CACHE.get(_uuid);
    }

    /**
     * This is the getter method for the instance variable {@link #id}.
     *
     * @return value of instance variable {@link #id}
     */
    public long getId()
    {
        return this.id;
    }

    /**
     * This is the getter method for the instance variable {@link #uuid}.
     *
     * @return value of instance variable {@link #uuid}
     */
    public UUID getUUID()
    {
        return this.uuid;
    }

    /**
     * This is the getter method for the instance variable {@link #name}.
     *
     * @return value of instance variable {@link #name}
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * Get a value from the maps. The following logic applies:
     * <ol>
     * <li>Check if a Context exists</li>
     * <li>If a Context exist check if a company is given</li>
     * <li>If a company is given, check for a company specific map</li>
     * <li>If a company specific map is given search for the key in this map</li>
     * <li>If any of the earlier point fails the value from the default map is returned</li>
     * </ol>
     * @param _key  key the value is wanted for
     * @param _map  map the key will be search in for
     * @return  String value
     * @throws EFapsException on error
     */
    private String getValue(final String _key,
                            final Map<Long, Map<String, String>> _map)
        throws EFapsException
    {
        Company company = null;
        if (Context.isThreadActive()) {
            company = Context.getThreadContext().getCompany();
        }
        final long companyId = company == null ? 0 : company.getId();
        Map<String, String> innerMap;
        if (_map.containsKey(companyId)) {
            innerMap = _map.get(companyId);
            if (!innerMap.containsKey(_key)) {
                innerMap = _map.get(new Long(0));
            }
        } else {
            innerMap = _map.get(new Long(0));
        }
        return innerMap.get(_key);
    }

    /**
     * Returns for given <code>_key</code> the related link. If no link is found
     * <code>null</code> is returned.
     *
     * @param _key key of searched link
     * @return found link; if not found <code>null</code>
     * @throws EFapsException on error
     * @see #links
     */
    public Instance getLink(final String _key)
        throws EFapsException
    {
        return Instance.get(getValue(_key, this.links));
    }

    /**
     * Returns for given <code>Instance</code> the related attribute value. If no
     * attribute value is found <code>null</code> is returned.
     *
     * @param _instance Instance of searched objectattribute
     * @return found attribute value; if not found <code>null</code>
     * @throws EFapsException  on error
     * @see #objectAttributes
     */
    public String getObjectAttributeValue(final Instance _instance)
        throws EFapsException
    {
        return getObjectAttributeValue(_instance.getOid());
    }

    /**
     * Returns for given <code>OID</code> the related attribute value. If no
     * attribute value is found <code>null</code> is returned.
     *
     * @param _oid OID of searched objectattribute
     * @return found attribute value; if not found <code>null</code>
     * @throws EFapsException on error
     * @see #objectAttributes
     */
    public String getObjectAttributeValue(final String _oid)
        throws EFapsException
    {
        return getValue(_oid, this.objectAttributes);
    }

    /**
     * Returns for given <code>Instance</code> the related value as Properties.
     * If no attribute is found an empty Properties is returned.
     *
     * @param _instance Instance of searched attribute
     * @return Properties
     * @throws EFapsException on error
     * @see #objectAttributes
     */
    public Properties getObjectAttributeValueAsProperties(final Instance _instance)
        throws EFapsException
    {
        return getObjectAttributeValueAsProperties(_instance.getOid());
    }

    /**
     * Returns for given <code>OID</code> the related value as Properties.
     * If no attribute is found an empty Properties is returned.
     *
     * @param _key key of searched attribute
     * @return Properties
     * @throws EFapsException on error
     * @see #objectAttributes
     */
    public Properties getObjectAttributeValueAsProperties(final String _key)
        throws EFapsException
    {
        final Properties ret = new Properties();
        final String value = getValue(_key, this.objectAttributes);
        if (value != null) {
            try {
                ret.load(new StringReader(value));
            } catch (final IOException e) {
                throw new EFapsException(SystemConfiguration.class, "getObjectAttributeValueAsProperties", e);
            }
        }
        return ret;
    }

    /**
     * Returns for given <code>_key</code> the related attribute value. If no
     * attribute value is found <code>null</code> is returned.
     *
     * @param _key key of searched attribute
     * @return found attribute value; if not found <code>null</code>
     * @throws EFapsException on error
     * @see #attributes
     */
    public String getAttributeValue(final String _key)
        throws EFapsException
    {
        return getValue(_key, this.attributes);
    }

    /**
     * Returns for given <code>_key</code> the related boolean attribute value.
     * If no attribute value is found <i>false</i> is returned.
     *
     * @param _key key of searched attribute
     * @return found boolean attribute value; if not found <i>false</i>
     * @throws EFapsException on error
     * @see #attributes
     */
    public boolean getAttributeValueAsBoolean(final String _key)
        throws EFapsException
    {
        final String value = getAttributeValue(_key);
        return value == null ? false : Boolean.parseBoolean(value);
    }

    /**
     * Returns for given <code>_key</code> the related integer attribute value.
     * If no attribute is found <code>0</code> is returned.
     *
     * @param _key key of searched attribute
     * @return found integer attribute value; if not found <code>0</code>
     * @throws EFapsException on error
     * @see #attributes
     */
    public int getAttributeValueAsInteger(final String _key)
        throws EFapsException
    {
        final String value = getAttributeValue(_key);
        return value == null ? 0 : Integer.parseInt(value);
    }

    /**
     * Returns for given <code>_key</code> the related value as Properties.
     * If no attribute is found an empty Properties is returned.
     *
     * @param _key key of searched attribute
     * @return Properties
     * @throws EFapsException on error
     * @see #attributes
     */
    public Properties getAttributeValueAsProperties(final String _key)
        throws EFapsException
    {
        final Properties ret = new Properties();
        final String value = getAttributeValue(_key);
        if (value != null) {
            try {
                ret.load(new StringReader(value));
            } catch (final IOException e) {
                throw new EFapsException(SystemConfiguration.class, "getAttributeValueAsProperties", e);
            }
        }
        return ret;
    }

    /**
     * Method to initialize the {@link #CACHE cache} for the system
     * configurations.
     */
    public static void initialize()
    {
        SystemConfiguration.CACHE.initialize(SystemConfiguration.class);
    }

    /**
     * Cache for all system configurations.
     */
    private static class SystemConfigurationCache
        extends AbstractCache<SystemConfiguration>
    {

        /**
         * Reads all system configurations with their attributes and links and
         * stores them in the given mapping caches.
         *
         * @param _newCache4Id cache for the mapping between id and system
         *            configuration
         * @param _newCache4Name cache for the mapping between name and system
         *            configuration
         * @param _newCache4UUID cache for the mapping between UUID and system
         *            configuration
         * @throws CacheReloadException if cache could not be reloaded
         */
        @Override
        protected void readCache(final Map<Long, SystemConfiguration> _newCache4Id,
                                 final Map<String, SystemConfiguration> _newCache4Name,
                                 final Map<UUID, SystemConfiguration> _newCache4UUID)
            throws CacheReloadException
        {
            ConnectionResource con = null;
            try {
                con = Context.getThreadContext().getConnectionResource();

                Statement stmt = null;
                try {
                    stmt = con.getConnection().createStatement();
                    final ResultSet rs = stmt.executeQuery(SystemConfiguration.SELECT.getSQL());
                    long id = 0;
                    SystemConfiguration config = null;
                    while (rs.next()) {
                        final long configId = rs.getLong(1);
                        final String configName = rs.getString(2).trim();
                        final String configUUID = rs.getString(3).trim();
                        final String key = rs.getString(4).trim();
                        final String value = rs.getString(5).trim();
                        final String uuid = rs.getString(6).trim();
                        final Long companyId = rs.getLong(7);
                        if (id != configId) {
                            id = configId;
                            config = new SystemConfiguration(configId, configName, configUUID);
                            _newCache4Id.put(config.getId(), config);
                            _newCache4Name.put(config.getName(), config);
                            _newCache4UUID.put(config.getUUID(), config);
                        }
                        final UUID uuidTmp = UUID.fromString(uuid);
                        final Map<Long, Map<String, String>> configMap;
                        if (uuidTmp.equals(CIAdminCommon.SystemConfigurationLink.uuid)) {
                            configMap = config.links;
                        } else if (uuidTmp.equals(CIAdminCommon.SystemConfigurationObjectAttribute.uuid)) {
                            configMap = config.objectAttributes;
                        } else {
                            configMap = config.attributes;
                        }
                        final Map<String, String> map;
                        if (configMap.containsKey(companyId)) {
                            map = configMap.get(companyId);
                        } else {
                            map = new HashMap<String, String>();
                            configMap.put(companyId, map);
                        }
                        map.put(key, value);
                    }
                    rs.close();
                } finally {
                    if (stmt != null) {
                        stmt.close();
                    }
                }
                con.commit();
            } catch (final SQLException e) {
                throw new CacheReloadException("could not read attribute types", e);
            } catch (final EFapsException e) {
                throw new CacheReloadException("could not read attribute types", e);
            } finally {
                if ((con != null) && con.isOpened()) {
                    try {
                        con.abort();
                    } catch (final EFapsException e) {
                        throw new CacheReloadException("could not read attribute types", e);
                    }
                }
            }
        }
    }
}
