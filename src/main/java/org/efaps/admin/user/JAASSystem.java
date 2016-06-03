/*
 * Copyright 2003 - 2016 The eFaps Team
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
 */

package org.efaps.admin.user;

import java.lang.reflect.Method;
import java.security.Principal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.efaps.admin.AbstractAdminObject;
import org.efaps.db.Context;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.db.wrapper.SQLPart;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheLogListener;
import org.efaps.util.cache.CacheReloadException;
import org.efaps.util.cache.InfinispanCache;
import org.infinispan.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * The class handles the caching for JAAS systems.
 *
 * @author The eFaps Team
 *
 */
public final class JAASSystem
    extends AbstractAdminObject
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(JAASSystem.class);

    /**
     * This is the SQL select statement to select all JAAS systems from the
     * database.
     */
    private static final String SQL_SELECT = new SQLSelect()
                    .column("ID")
                    .column("NAME")
                    .column("CLASSNAMEPERSON")
                    .column("METHODPERSONKEY")
                    .column("METHODPERSONNAME")
                    .column("METHODPERSONFIRSTNAME")
                    .column("METHODPERSONLASTNAME")
                    .column("METHODPERSONEMAIL")
                    .column("CLASSNAMEROLE")
                    .column("METHODROLEKEY")
                    .column("CLASSNAMEGROUP")
                    .column("METHODGROUPKEY")
                    .from("V_USERJAASSYSTEM").toString();

    /**
     * This is the SQL select statement to select a JAASSystem from the database
     * by ID.
     */
    private static final String SQL_ID = new SQLSelect()
                    .column("ID")
                    .column("NAME")
                    .column("CLASSNAMEPERSON")
                    .column("METHODPERSONKEY")
                    .column("METHODPERSONNAME")
                    .column("METHODPERSONFIRSTNAME")
                    .column("METHODPERSONLASTNAME")
                    .column("METHODPERSONEMAIL")
                    .column("CLASSNAMEROLE")
                    .column("METHODROLEKEY")
                    .column("CLASSNAMEGROUP")
                    .column("METHODGROUPKEY")
                    .from("V_USERJAASSYSTEM", 0)
                    .addPart(SQLPart.WHERE).addColumnPart(0, "ID").addPart(SQLPart.EQUAL).addValuePart("?").toString();

  /**
     * This is the SQL select statement to select a JAASSystem from the database
     * by Name.
     */
    private static final String SQL_NAME = new SQLSelect()
                    .column("ID")
                    .column("NAME")
                    .column("CLASSNAMEPERSON")
                    .column("METHODPERSONKEY")
                    .column("METHODPERSONNAME")
                    .column("METHODPERSONFIRSTNAME")
                    .column("METHODPERSONLASTNAME")
                    .column("METHODPERSONEMAIL")
                    .column("CLASSNAMEROLE")
                    .column("METHODROLEKEY")
                    .column("CLASSNAMEGROUP")
                    .column("METHODGROUPKEY")
                    .from("V_USERJAASSYSTEM", 0)
                    .addPart(SQLPart.WHERE).addColumnPart(0, "NAME").addPart(SQLPart.EQUAL).addValuePart("?")
                    .toString();

    /**
     * Name of the Cache by ID.
     */
    private static String IDCACHE = JAASSystem.class.getName() + ".ID";

    /**
     * Name of the Cache by Name.
     */
    private static String NAMECACHE = JAASSystem.class.getName() + ".Name";

    /**
     * The class used as principle for persons for this JAAS system is stored
     * in this instance variable.
     *
     * @see #getPersonJAASPrincipleClass
     */
    private Class<Principal> personJAASPrincipleClass;

    /**
     * @see #getPersonMethodKey()
     */
    private Method personMethodKey;

    /**
     * @see #getPersonMethodName()
     */
    private Method personMethodName;

    /**
     * Map between person attributes the the method of the JAAS.
     */
    private final Map<Person.AttrName, Method> personMethodAttributes
        = new HashMap<Person.AttrName, Method>();

    /**
     * The class used as principle for roles for this JAAS system is stored in
     * this instance variable.
     *
     * @see #getRoleJAASPrincipleClass()
     */
    private Class<Principal> roleJAASPrincipleClass;

    /**
     * @see #getRoleMethodKey()
     */
    private Method roleMethodKey;

    /**
     * The class used as principle for groups for this JAAS system is stored in
     * this instance variable.
     *
     * @see #getGroupJAASPrincipleClass
     */
    private Class<Principal> groupJAASPrincipleClass;

    /**
     * @see #getGroupMethodKey()
     */
    private Method groupMethodKey;

    /**
     * Constructor to set the id and name of the user object.
     *
     * @param _id       id to set
     * @param _name     name to set
     */
    private JAASSystem(final long _id,
                       final String _name)
    {
        super(_id, null, _name);
    }

    /**
     * This is the getter method for instance variable
     * {@link #personJAASPrincipleClass}.
     *
     * @return the value of the instance variable
     *         {@link #personJAASPrincipleClass}.
     * @see #personJAASPrincipleClass
     */
    public Class<Principal> getPersonJAASPrincipleClass()
    {
        return this.personJAASPrincipleClass;
    }

    /**
     * This is the getter method for instance variable {@link #personMethodKey}.
     *
     * @return the value of the instance variable {@link #personMethodKey}.
     * @see #personMethodKey
     */
    public Method getPersonMethodKey()
    {
        return this.personMethodKey;
    }

    /**
     * This is the getter method for instance variable
     * {@link #personMethodName}.
     *
     * @return the value of the instance variable {@link #personMethodName}
     * @see #personMethodName
     */
    public Method getPersonMethodName()
    {
        return this.personMethodName;
    }

    /**
     * This is the getter method for instance variable
     * {@link #personMethodAttributes}.
     *
     * @return the value of the instance variable
     *         {@link #personMethodAttributes}
     * @see #personMethodAttributes
     */
    public Map<Person.AttrName, Method> getPersonMethodAttributes()
    {
        return this.personMethodAttributes;
    }

    /**
     * This is the getter method for instance variable
     * {@link #roleJAASPrincipleClass}.
     *
     * @return the value of the instance variable {@link #roleJAASPrincipleClass}.
     * @see #roleJAASPrincipleClass
     */
    public Class<Principal> getRoleJAASPrincipleClass()
    {
        return this.roleJAASPrincipleClass;
    }

    /**
     * This is the getter method for instance variable {@link #roleMethodKey}.
     *
     * @return the value of the instance variable {@link #roleMethodKey}
     * @see #roleMethodKey
     */
    public Method getRoleMethodKey()
    {
        return this.roleMethodKey;
    }

    /**
     * This is the getter method for instance variable
     * {@link #groupJAASPrincipleClass}.
     *
     * @return the value of the instance variable
     *         {@link #groupJAASPrincipleClass}
     * @see #groupJAASPrincipleClass
     */
    public Class<Principal> getGroupJAASPrincipleClass()
    {
        return this.groupJAASPrincipleClass;
    }

    /**
     * This is the getter method for instance variable {@link #groupMethodKey}.
     *
     * @return the value of the instance variable {@link #groupMethodKey}
     * @see #groupMethodKey
     */
    public Method getGroupMethodKey()
    {
        return this.groupMethodKey;
    }

    /**
     * Method to initialize the cache of JAAS systems.
     * @throws CacheReloadException on error
     */
    public static void initialize()
        throws CacheReloadException
    {
        if (InfinispanCache.get().exists(JAASSystem.IDCACHE)) {
            InfinispanCache.get().<Long, JAASSystem>getCache(JAASSystem.IDCACHE).clear();
        } else {
            InfinispanCache.get().<Long, JAASSystem>getCache(JAASSystem.IDCACHE)
                            .addListener(new CacheLogListener(JAASSystem.LOG));
        }
        if (InfinispanCache.get().exists(JAASSystem.NAMECACHE)) {
            InfinispanCache.get().<String, JAASSystem>getCache(JAASSystem.NAMECACHE).clear();
        } else {
            InfinispanCache.get().<String, JAASSystem>getCache(JAASSystem.NAMECACHE)
                            .addListener(new CacheLogListener(JAASSystem.LOG));
        }
        JAASSystem.getJAASSystemFromDB(JAASSystem.SQL_SELECT, null);
    }

    /**
     * Returns for given parameter <i>_id</i> the instance of class
     * {@link JAASSystem}.
     *
     * @param _id       id to search in the cache
     * @return instance of class {@link JAASSystem}
     * @throws CacheReloadException on error
     */
    public static JAASSystem getJAASSystem(final long _id)
        throws CacheReloadException
    {
        final Cache<Long, JAASSystem> cache = InfinispanCache.get().<Long, JAASSystem>getCache(JAASSystem.IDCACHE);
        if (!cache.containsKey(_id)) {
            JAASSystem.getJAASSystemFromDB(JAASSystem.SQL_ID, _id);
        }
        return cache.get(_id);
    }

    /**
     * Returns for given parameter <i>_name</i> the instance of class
     * {@link JAASSystem}.
     *
     * @param _name name to search in the cache
     * @return instance of class {@link JAASSystem}
     * @throws CacheReloadException on error
     */
    public static JAASSystem getJAASSystem(final String _name)
        throws CacheReloadException
    {
        final Cache<String, JAASSystem> cache = InfinispanCache.get()
                        .<String, JAASSystem>getCache(JAASSystem.NAMECACHE);
        if (!cache.containsKey(_name)) {
            JAASSystem.getJAASSystemFromDB(JAASSystem.SQL_NAME, _name);
        }
        return cache.get(_name);
    }

    /**
     * Returns all cached JAAS system in a set.
     *
     * @return set of all loaded and cached JAAS systems
     */
    public static Set<JAASSystem> getAllJAASSystems()
    {
        final Set<JAASSystem> ret = new HashSet<JAASSystem>();
        final Cache<Long, JAASSystem> cache = InfinispanCache.get().<Long, JAASSystem>getCache(JAASSystem.IDCACHE);
        for (final Map.Entry<Long, JAASSystem> entry : cache.entrySet()) {
            ret.add(entry.getValue());
        }
        return ret;
    }

    /**
     * Returns for the given method name the method found in the given class.
     * The found method is tested, if the method is returning string and has no
     * parameters.<br/>
     * If the checks fails or the method is not found, an error log is written
     * and <code>null</code> is returned.
     *
     * @param _class        class on which the method is searched
     * @param _method       method name
     * @param _type         text string for which the method is searched
     * @param _jaasName     name of the JAAS system
     * @param _jaasId       id of the JAAS system
     * @return found method, or <code>null</code> if no method found
     * @see #initialize()
     */
    private static Method getMethod(final Class<?> _class,
                                    final String _method,
                                    final String _type,
                                    final String _jaasName,
                                    final long _jaasId)
    {
        Method ret = null;

        if (_method != null && _method.trim().length() > 0) {
            try {
                ret = _class.getMethod(_method.trim(), new Class[] {});
            } catch (final NoSuchMethodException e) {
                JAASSystem.LOG.error("could not get a " + _type
                    + " method for JAAS System '" + _jaasName + "' (id = " + _jaasId + ")", e);
            } catch (final SecurityException e) {
                JAASSystem.LOG.error("could not get a " + _type
                    + " method for JAAS System '" + _jaasName + "' (id = " + _jaasId + ")", e);
            }
            if (!ret.getReturnType().equals(String.class)) {
                JAASSystem.LOG.error("could not get a " + _type
                    + " method returning java.lang.String for JAAS System '" + _jaasName + "' (id = " + _jaasId + ")");
                ret = null;
            } else if (ret.getParameterTypes() != null && ret.getParameterTypes().length > 0) {
                JAASSystem.LOG.error("could not get a " + _type
                    + " method returning java.lang.String for JAAS System '" + _jaasName + "' (id = " + _jaasId + ")");
                ret = null;
            }
        }
        return ret;
    }

    /**
     * @param _group Group to be cached
     */
    @SuppressFBWarnings("RV_RETURN_VALUE_OF_PUTIFABSENT_IGNORED")
    private static void cacheJAASSystem(final JAASSystem _group)
    {
        final Cache<String, JAASSystem> nameCache = InfinispanCache.get().<String, JAASSystem>getIgnReCache(
                        JAASSystem.NAMECACHE);
        nameCache.putIfAbsent(_group.getName(), _group);

        final Cache<Long, JAASSystem> idCache = InfinispanCache.get().<Long, JAASSystem>getIgnReCache(
                        JAASSystem.IDCACHE);
        idCache.putIfAbsent(_group.getId(), _group);
    }

    /**
     * @param _sql      SQL Statment to be execuetd
     * @param _criteria filter criteria
     * @throws CacheReloadException on error
     */
    private static void getJAASSystemFromDB(final String _sql,
                                            final Object _criteria)
        throws CacheReloadException
    {
        ConnectionResource con = null;
        try {
            con = Context.getThreadContext().getConnectionResource();

            PreparedStatement stmt = null;
            try {
                stmt = con.getConnection().prepareStatement(_sql);
                if (!_sql.equals(JAASSystem.SQL_SELECT)) {
                    stmt.setObject(1, _criteria);
                }
                final ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    final long id = rs.getLong(1);
                    final String name = rs.getString(2).trim();
                    final String personClassName = rs.getString(3);
                    final String personMethodKey = rs.getString(4);
                    final String personMethodName = rs.getString(5);
                    final String personMethodFirstName = rs.getString(6);
                    final String personMethodLastName = rs.getString(7);
                    final String personMethodEmail = rs.getString(8);
                    final String roleClassName = rs.getString(9);
                    final String roleMethodKey = rs.getString(10);
                    final String groupClassName = rs.getString(11);
                    final String groupMethodKey = rs.getString(12);

                    JAASSystem.LOG.debug("read JAAS System '{}' (id = {})", name, id);

                    try {
                        final JAASSystem system = new JAASSystem(id, name);
                        @SuppressWarnings("unchecked")
                        final Class<Principal> forName = (Class<Principal>) Class.forName(personClassName.trim());
                        system.personJAASPrincipleClass = forName;
                        system.personMethodKey = JAASSystem.getMethod(system.personJAASPrincipleClass,
                                        personMethodKey,
                                        "person key", name, id);
                        system.personMethodName = JAASSystem.getMethod(system.personJAASPrincipleClass,
                                        personMethodName,
                                        "person name", name, id);
                        Method method = JAASSystem.getMethod(system.personJAASPrincipleClass,
                                        personMethodFirstName,
                                        "person first name", name, id);
                        if (method != null) {
                            system.personMethodAttributes.put(Person.AttrName.FIRSTNAME,
                                            method);
                        }
                        method = JAASSystem.getMethod(system.personJAASPrincipleClass,
                                        personMethodLastName,
                                        "person last name", name, id);
                        if (method != null) {
                            system.personMethodAttributes.put(Person.AttrName.LASTNAME,
                                            method);
                        }
                        method = JAASSystem.getMethod(system.personJAASPrincipleClass,
                                        personMethodEmail,
                                        "person email", name, id);
                        if (method != null) {
                            JAASSystem.LOG.debug("method '{}' not implemented yet.", method);
                            // TODO: person email method
                        }
                        if (roleClassName != null && roleClassName.trim().length() > 0) {
                            @SuppressWarnings("unchecked")
                            final Class<Principal> fn = (Class<Principal>) Class.forName(roleClassName.trim());
                            system.roleJAASPrincipleClass = fn;
                            system.roleMethodKey = JAASSystem.getMethod(system.roleJAASPrincipleClass,
                                            roleMethodKey,
                                            "role key", name, id);
                        }
                        if (groupClassName != null && groupClassName.trim().length() > 0) {
                            @SuppressWarnings("unchecked")
                            final Class<Principal> fn = (Class<Principal>) Class.forName(groupClassName.trim());
                            system.groupJAASPrincipleClass = fn;
                            system.groupMethodKey = JAASSystem.getMethod(system.groupJAASPrincipleClass,
                                            groupMethodKey,
                                            "group key", name, id);
                        }
                        JAASSystem.cacheJAASSystem(system);
                    } catch (final ClassNotFoundException e) {
                        JAASSystem.LOG.error("read JAAS System '{}' (id = {})", name, id, e);
                    }
                }
                rs.close();
            } finally {
                if (stmt != null) {
                    stmt.close();
                }
            }
            con.commit();
        } catch (final SQLException e) {
            throw new CacheReloadException("could not read roles", e);
        } catch (final EFapsException e) {
            throw new CacheReloadException("could not read roles", e);
        } finally {
            if (con != null && con.isOpened()) {
                try {
                    con.abort();
                } catch (final EFapsException e) {
                    throw new CacheReloadException("could not read roles", e);
                }
            }
        }
    }

    @Override
    public boolean equals(final Object _obj)
    {
        final boolean ret;
        if (_obj instanceof JAASSystem) {
            ret = ((JAASSystem) _obj).getId() == getId();
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
}
