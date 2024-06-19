/*
 * Copyright © 2003 - 2024 The eFaps Team (-)
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
 */
package org.efaps.admin.common;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.security.Provider;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.user.Company;
import org.efaps.ci.CIAdminCommon;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.wrapper.SQLPart;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.jaas.AppAccessHandler;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheLogListener;
import org.efaps.util.cache.CacheObjectInterface;
import org.efaps.util.cache.CacheReloadException;
import org.efaps.util.cache.InfinispanCache;
import org.infinispan.Cache;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.StringPBEConfig;
import org.jasypt.iv.IvGenerator;
import org.jasypt.properties.EncryptableProperties;
import org.jasypt.salt.SaltGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The class handles the caching for system configurations with their attributes
 * and links.
 *
 * @author The eFaps Team
 */
public final class SystemConfiguration
    implements CacheObjectInterface, Serializable
{
    /**
     * Used for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * This is the SQL select statement to select the configs from the database
     * by ID.
     */
    private static final String SQL_CONFIG = new SQLSelect()
                    .column(0, "TYPEID")
                    .column(0, "KEY")
                    .column(0, "VALUE")
                    .column(0, "COMPANYID")
                    .column(0, "APPKEY")
                    .from("T_CMSYSCONF", 0)
                    .addPart(SQLPart.WHERE).addColumnPart(0, "ABSTRACTID").addPart(SQLPart.EQUAL).addValuePart("?")
                    .toString();

    /**
     * This is the SQL select statement to select a role from the database by
     * ID.
     */
    private static final String SQL_ID = new SQLSelect()
                    .column(0, "ID")
                    .column(0, "NAME")
                    .column(0, "UUID")
                    .from("T_CMABSTRACT", 0)
                    .addPart(SQLPart.WHERE).addColumnPart(0, "ID").addPart(SQLPart.EQUAL).addValuePart("?").toString();

    /**
     * This is the SQL select statement to select a role from the database by
     * Name.
     */
    private static final String SQL_NAME = new SQLSelect()
                    .column(0, "ID")
                    .column(0, "NAME")
                    .column(0, "UUID")
                    .from("T_CMABSTRACT", 0)
                    .addPart(SQLPart.WHERE).addColumnPart(0, "NAME").addPart(SQLPart.EQUAL).addValuePart("?")
                    .toString();

    /**
     * This is the SQL select statement to select a role from the database by
     * UUID.
     */
    private static final String SQL_UUID = new SQLSelect()
                    .column(0, "ID")
                    .column(0, "NAME")
                    .column(0, "UUID")
                    .from("T_CMABSTRACT", 0)
                    .addPart(SQLPart.WHERE).addColumnPart(0, "UUID").addPart(SQLPart.EQUAL).addValuePart("?")
                    .toString();

    /**
     * Name of the Cache by UUID.
     */
    private static final String UUIDCACHE = SystemConfiguration.class.getName() + ".UUID";

    /**
     * Name of the Cache by ID.
     */
    private static final String IDCACHE = SystemConfiguration.class.getName() + ".ID";

    /**
     * Name of the Cache by Name.
     */
    private static final String NAMECACHE = SystemConfiguration.class.getName() + ".Name";

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(SystemConfiguration.class);

    /**
     * The configuration for the PBE used for jasypt.
     */
    private static final EFapsPBEConfig BPECONF = new EFapsPBEConfig();

    /**
     * The encryptor for the PBE used for jasypt.
     */
    private static StandardPBEStringEncryptor ENCRYPTOR;

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

    /** The values. */
    private final List<Value> values = new ArrayList<>();

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
        id = _id;
        uuid = UUID.fromString(_uuid);
        name = _name;
    }

    /**
     * Returns for given parameter <i>_id</i> the instance of class
     * {@link SystemConfiguration}.
     *
     * @param _id id of the system configuration
     * @return instance of class {@link SystemConfiguration}
     * @throws CacheReloadException on error
     */
    public static SystemConfiguration get(final long _id)
        throws CacheReloadException
    {
        final Cache<Long, SystemConfiguration> cache = InfinispanCache.get().<Long, SystemConfiguration>getCache(
                        SystemConfiguration.IDCACHE);
        if (!cache.containsKey(_id)) {
            SystemConfiguration.getSystemConfigurationFromDB(SystemConfiguration.SQL_ID, _id);
        }
        return cache.get(_id);
    }

    /**
     * Returns for given parameter <i>_name</i> the instance of class
     * {@link SystemConfiguration}.
     *
     * @param _name name of the system configuration
     * @return instance of class {@link SystemConfiguration}
     * @throws CacheReloadException on error
     */
    public static SystemConfiguration get(final String _name)
        throws CacheReloadException
    {
        final Cache<String, SystemConfiguration> cache = InfinispanCache.get().<String, SystemConfiguration>getCache(
                        SystemConfiguration.NAMECACHE);
        if (!cache.containsKey(_name)) {
            SystemConfiguration.getSystemConfigurationFromDB(SystemConfiguration.SQL_NAME, _name);
        }
        return cache.get(_name);
    }

    /**
     * Returns for given parameter <i>_uuid</i> the instance of class
     * {@link SystemConfiguration}.
     *
     * @param _uuid uuid of the system configuration
     * @return instance of class {@link SystemConfiguration}
     * @throws CacheReloadException on error
     */
    public static SystemConfiguration get(final UUID _uuid)
        throws CacheReloadException
    {
        final Cache<UUID, SystemConfiguration> cache = InfinispanCache.get().<UUID, SystemConfiguration>getCache(
                        SystemConfiguration.UUIDCACHE);
        if (!cache.containsKey(_uuid)) {
            SystemConfiguration.getSystemConfigurationFromDB(SystemConfiguration.SQL_UUID, String.valueOf(_uuid));
        }
        return cache.get(_uuid);
    }

    /**
     * This is the getter method for the instance variable {@link #id}.
     *
     * @return value of instance variable {@link #id}
     */
    @Override
    public long getId()
    {
        return id;
    }

    /**
     * This is the getter method for the instance variable {@link #uuid}.
     *
     * @return value of instance variable {@link #uuid}
     */
    @Override
    public UUID getUUID()
    {
        return uuid;
    }

    /**
     * This is the getter method for the instance variable {@link #name}.
     *
     * @return value of instance variable {@link #name}
     */
    @Override
    public String getName()
    {
        return name;
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
        return Instance.get(getValue(_key, ConfType.LINK));
    }

    /**
     * Returns for given <code>Instance</code> the related attribute value. If
     * no attribute value is found <code>null</code> is returned.
     *
     * @param _instance Instance of searched objectattribute
     * @return found attribute value; if not found <code>null</code>
     * @throws EFapsException on error
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
        return getValue(_oid, ConfType.OBJATTR);
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
     * Returns for given <code>OID</code> the related value as Properties. If no
     * attribute is found an empty Properties is returned.
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
        final String value = getValue(_key, ConfType.OBJATTR);
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
     * Contains attribute value.
     *
     * @param _key the key
     * @return true, if successful
     * @throws EFapsException on error
     */
    public boolean containsAttributeValue(final String _key)
        throws EFapsException
    {
        return getValue(_key, ConfType.ATTRIBUTE) != null;
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
        return getValue(_key, ConfType.ATTRIBUTE);
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
     * Returns for given <code>_key</code> the related value as Properties. If
     * no attribute is found an empty Properties is returned.
     *
     * @param _key key of searched attribute
     * @return Properties
     * @throws EFapsException on error
     * @see #attributes
     */
    public Properties getAttributeValueAsEncryptedProperties(final String _key)
        throws EFapsException
    {
        final Properties properties = getAttributeValueAsProperties(_key, false);
        final Properties props = new EncryptableProperties(properties, SystemConfiguration.ENCRYPTOR);
        return props;
    }

    /**
     * Returns for given <code>_key</code> the related value as Properties. If
     * no attribute is found an empty Properties is returned.
     *
     * @param _key key of searched attribute
     * @param _concatenate is concatenate or not
     * @return Properties
     * @throws EFapsException on error
     * @see #attributes
     */
    public Properties getAttributeValueAsEncryptedProperties(final String _key,
                                                             final boolean _concatenate)
        throws EFapsException
    {
        final Properties properties = getAttributeValueAsProperties(_key, _concatenate);
        final StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setConfig(SystemConfiguration.getPBEConfig());
        final Properties props = new EncryptableProperties(properties, encryptor);
        return props;
    }

    /**
     * Returns for given <code>_key</code> the related value as Properties. If
     * no attribute is found an empty Properties is returned.
     *
     * @param _key key of searched attribute
     * @return Properties
     * @throws EFapsException on error
     * @see #attributes
     */
    public Properties getAttributeValueAsProperties(final String _key)
        throws EFapsException
    {
        return getAttributeValueAsProperties(_key, false);
    }

    /**
     * Returns for given <code>_key</code> the related value as Properties. If
     * no attribute is found an empty Properties is returned.
     * Can concatenates Properties for Keys.<br/>
     * e.b. Key, Key01, Key02, Key03
     *
     * @param _key key of searched attribute
     * @param _concatenate  concatenate or not
     * @return map with properties
     * @throws EFapsException on error
     */
    public Properties getAttributeValueAsProperties(final String _key,
                                                    final boolean _concatenate)
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
        if (_concatenate) {
            for (int i = 1; i < 100; i++) {
                final String keyTmp = _key + String.format("%02d", i);
                final String valueTmp = getAttributeValue(keyTmp);
                final Properties propsTmp = new Properties();
                if (valueTmp != null) {
                    try {
                        propsTmp.load(new StringReader(valueTmp));
                    } catch (final IOException e) {
                        throw new EFapsException(SystemConfiguration.class, "getAttributeValueAsPropertiesConcat", e);
                    }
                } else {
                    break;
                }
                if (propsTmp.isEmpty()) {
                    break;
                } else {
                    ret.putAll(propsTmp);
                }
            }
        }
        return ret;
    }

    /**
     * Gets the value.
     *
     * @param _key the key
     * @param _type the type
     * @return the value
     * @throws EFapsException on error
     */
    private String getValue(final String _key,
                            final ConfType _type)
        throws EFapsException
    {
        final List<Value> fv = values.stream()
                        .filter(p -> p.type.equals(_type))
                        .filter(p -> p.key.equals(_key))
                        .filter(p -> priority(p) > 0)
                        .sorted((_o1,
                         _o2) -> Integer.compare(priority(_o2), priority(_o1))).collect(Collectors.toList());
        SystemConfiguration.LOG.debug("Analyzed for key {}: {}", _key, fv);
        final String ret;
        if (fv.isEmpty()) {
            ret = null;
        } else {
            ret = fv.get(0).value;
        }
        return ret;
    }

    /**
     * Priority.
     *
     * @param _value the value
     * @return the int
     */
    private int priority(final Value _value)
    {
        int ret = 1;
        try {
            if (StringUtils.isNotEmpty(_value.appKey)
                            && !_value.appKey.equals(AppAccessHandler.getApplicationKey())) {
                ret = -1;
            } else {
                final long companyId;
                if (Context.isThreadActive()) {
                    final Company company = Context.getThreadContext().getCompany();
                    companyId = company == null ? 0 : company.getId();
                } else {
                    companyId = 0;
                }
                if (_value.companyId == companyId) {
                    ret = ret + 100;
                } else if (_value.companyId == 0) {
                    ret = ret + 10;
                } else if (_value.companyId != companyId) {
                    ret = -1;
                }
            }
        } catch (final EFapsException e) {
            SystemConfiguration.LOG.error("Catched", e);
        }
        return ret;
    }

    /**
     * Reload the current SystemConfiguration by removing it from the Cache.
     */
    public void reload()
    {
        InfinispanCache.get().<UUID, SystemConfiguration>getCache(SystemConfiguration.UUIDCACHE).remove(uuid);
        InfinispanCache.get().<Long, SystemConfiguration>getCache(SystemConfiguration.IDCACHE).remove(id);
        InfinispanCache.get().<String, SystemConfiguration>getCache(SystemConfiguration.NAMECACHE).remove(name);
    }

    /**
     * Read the config.
     * @throws CacheReloadException on error
     */
    private void readConfig()
        throws CacheReloadException
    {
        Connection con = null;
        try {
            boolean closeContext = false;
            if (!Context.isThreadActive()) {
                Context.begin();
                closeContext = true;
            }
            final List<Object[]> dbValues = new ArrayList<>();
            con = Context.getConnection();
            PreparedStatement stmt = null;
            try {
                stmt = con.prepareStatement(SystemConfiguration.SQL_CONFIG);
                stmt.setObject(1, getId());
                final ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    dbValues.add(new Object[] {
                                    rs.getLong(1),
                                    rs.getString(2),
                                    rs.getString(3),
                                    rs.getLong(4),
                                    rs.getString(5)
                    });
                }
                rs.close();
            } finally {
                if (stmt != null) {
                    stmt.close();
                }
            }
            con.commit();
            if (closeContext) {
                Context.rollback();
            }
            for (final Object[] row : dbValues) {
                final Long typeId = (Long) row[0];
                final String key = (String) row[1];
                final String value = (String) row[2];
                final Long companyId = (Long) row[3];
                final String appkey = (String) row[4];
                final Type type = Type.get(typeId);
                final ConfType confType;
                if (type.equals(CIAdminCommon.SystemConfigurationLink.getType())) {
                    confType = ConfType.LINK;
                } else if (type.equals(CIAdminCommon.SystemConfigurationObjectAttribute.getType())) {
                    confType = ConfType.OBJATTR;
                } else {
                    confType = ConfType.ATTRIBUTE;
                }
                values.add(new Value(confType, key, value, companyId, appkey));
            }
        } catch (final SQLException e) {
            throw new CacheReloadException("could not read SystemConfiguration attributes", e);
        } catch (final EFapsException e) {
            throw new CacheReloadException("could not read SystemConfiguration attributes", e);
        } finally {
            try {
                if (con != null && !con.isClosed()) {
                    con.close();
                }
            } catch (final SQLException e) {
                throw new CacheReloadException("Cannot read a type for an attribute.", e);
            }
        }
    }

    /**
     * Method to initialize the {@link #CACHE cache} for the system
     * configurations.
     */
    public static void initialize()
    {
        if (InfinispanCache.get().exists(SystemConfiguration.UUIDCACHE)) {
            InfinispanCache.get().<UUID, SystemConfiguration>getCache(SystemConfiguration.UUIDCACHE).clear();
        } else {
            InfinispanCache.get().<UUID, SystemConfiguration>getCache(SystemConfiguration.UUIDCACHE)
                            .addListener(new CacheLogListener(SystemConfiguration.LOG));
        }
        if (InfinispanCache.get().exists(SystemConfiguration.IDCACHE)) {
            InfinispanCache.get().<Long, SystemConfiguration>getCache(SystemConfiguration.IDCACHE).clear();
        } else {
            InfinispanCache.get().<Long, SystemConfiguration>getCache(SystemConfiguration.IDCACHE)
                            .addListener(new CacheLogListener(SystemConfiguration.LOG));
        }
        if (InfinispanCache.get().exists(SystemConfiguration.NAMECACHE)) {
            InfinispanCache.get().<String, SystemConfiguration>getCache(SystemConfiguration.NAMECACHE).clear();
        } else {
            InfinispanCache.get().<String, SystemConfiguration>getCache(SystemConfiguration.NAMECACHE)
                            .addListener(new CacheLogListener(SystemConfiguration.LOG));
        }

        SystemConfiguration.ENCRYPTOR = new StandardPBEStringEncryptor();
        SystemConfiguration.ENCRYPTOR.setConfig(SystemConfiguration.getPBEConfig());
    }

    public static void clearCache() {

    }

    /**
     * @param _sysConfig SystemConfiguration to be cached
     */
    private static void cacheSytemConfig(final SystemConfiguration _sysConfig)
    {
        final Cache<UUID, SystemConfiguration> cache4UUID = InfinispanCache.get()
                        .<UUID, SystemConfiguration>getIgnReCache(SystemConfiguration.UUIDCACHE);
        cache4UUID.put(_sysConfig.getUUID(), _sysConfig);

        final Cache<String, SystemConfiguration> nameCache = InfinispanCache.get()
                        .<String, SystemConfiguration>getIgnReCache(SystemConfiguration.NAMECACHE);
        nameCache.put(_sysConfig.getName(), _sysConfig);

        final Cache<Long, SystemConfiguration> idCache = InfinispanCache.get()
                        .<Long, SystemConfiguration>getIgnReCache(SystemConfiguration.IDCACHE);
        idCache.putIfAbsent(_sysConfig.getId(), _sysConfig);
    }

    /**
     * @param _sql sql statement to be executed
     * @param _criteria filter criteria
     * @throws CacheReloadException on error
     * @return false
     */
    private static boolean getSystemConfigurationFromDB(final String _sql,
                                                        final Object _criteria)
        throws CacheReloadException
    {
        boolean ret = false;
        Connection con = null;
        try {
            boolean closeContext = false;
            if (!Context.isThreadActive()) {
                Context.begin();
                closeContext = true;
            }
            SystemConfiguration sysConfig = null;
            con = Context.getConnection();
            PreparedStatement stmt = null;
            try {
                stmt = con.prepareStatement(_sql);
                stmt.setObject(1, _criteria);
                final ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    final long id = rs.getLong(1);
                    final String name = rs.getString(2).trim();
                    final String uuid = rs.getString(3).trim();
                    SystemConfiguration.LOG.debug("read SystemConfiguration '{}' (id = {}), format = '{}'", name, id);
                    sysConfig = new SystemConfiguration(id, name, uuid);
                }
                ret = true;
                rs.close();
            } finally {
                if (stmt != null) {
                    stmt.close();
                }
            }
            con.commit();
            con.close();
            if (closeContext) {
                Context.rollback();
            }
            if (sysConfig != null) {
                sysConfig.readConfig();
                SystemConfiguration.cacheSytemConfig(sysConfig);
            }
        } catch (final SQLException e) {
            throw new CacheReloadException("could not read SystemConfiguration", e);
        } catch (final EFapsException e) {
            throw new CacheReloadException("could not read SystemConfiguration", e);
        } finally {
            try {
                if (con != null && !con.isClosed()) {
                    con.close();
                }
            } catch (final SQLException e) {
                throw new CacheReloadException("Cannot read a type for an attribute.", e);
            }
        }
        return ret;
    }

    /**
     * @return the BPE Configuration.
     */
    public static EFapsPBEConfig getPBEConfig()
    {
        return SystemConfiguration.BPECONF;
    }

    @Override
    public boolean equals(final Object _obj)
    {
        final boolean ret;
        if (_obj instanceof SystemConfiguration) {
            ret = ((SystemConfiguration) _obj).getId() == getId();
        } else {
            ret = super.equals(_obj);
        }
        return ret;
    }

    @Override
    public int hashCode()
    {
        return  Long.valueOf(getId()).intValue();
    }

    /**
     * Value class.
     */
    private enum ConfType
    {

        /** The attribute. */
        ATTRIBUTE,

        /** The link. */
        LINK,

        /** The objattr. */
        OBJATTR
    }

    /**
     * Value class.
     */
    private static class Value
        implements Serializable
    {

        private static final long serialVersionUID = 1L;

        /** The type. */
        private final ConfType type;

        /** The key. */
        private final String key;

        /** The value. */
        private final String value;

        /** The company id. */
        private final long companyId;

        /** The app key. */
        private final String appKey;

        /**
         * Instantiates a new value.
         *
         * @param _confType the conf type
         * @param _key the key
         * @param _value the value
         * @param _companyId the company id
         * @param _appkey the appkey
         */
        Value(final ConfType _confType,
              final String _key,
              final String _value,
              final Long _companyId,
              final String _appkey)
        {
            type = _confType;
            key = StringUtils.trim(_key);
            value = StringUtils.trim(_value);
            companyId = _companyId;
            appKey = StringUtils.trim(_appkey);
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.NO_CLASS_NAME_STYLE);
        }
    }

    /**
     * Configuration class. Currently only the password is used. For all other
     * <code>null</code>is returnred to use the default values from jasyprt.
     */
    public static final class EFapsPBEConfig
        implements StringPBEConfig, Serializable
    {
        private static final long serialVersionUID = 1L;

        /**
         * Password.
         */
        private char[] password = null;

        /**
         * Sets the password to be used for encryption.
         * <p>
         * Determines the result of: {@link #getPassword()} and
         * {@link #getPasswordCharArray()}.
         * </p>
         *
         * @param _password the password to be used.
         */
        public void setPassword(final String _password)
        {
            if (_password == null) {
                password = null;
            } else {
                password = _password.toCharArray();
            }
        }

        @Override
        public String getPassword()
        {
            return new String(password);
        }

        @Override
        public String getAlgorithm()
        {
            return null;
        }

        @Override
        public Integer getKeyObtentionIterations()
        {
            return null;
        }

        @Override
        public SaltGenerator getSaltGenerator()
        {
            return null;
        }

        @Override
        public String getProviderName()
        {
            return null;
        }

        @Override
        public Provider getProvider()
        {
            return null;
        }

        @Override
        public Integer getPoolSize()
        {
            return null;
        }

        @Override
        public String getStringOutputType()
        {
            return null;
        }

        @Override
        public IvGenerator getIvGenerator()
        {
            return null;
        }
    }
}
