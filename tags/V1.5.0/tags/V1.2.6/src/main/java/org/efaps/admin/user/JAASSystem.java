/*
 * Copyright 2003 - 2010 The eFaps Team
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

import java.lang.reflect.Method;
import java.security.Principal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.efaps.admin.AbstractAdminObject;
import org.efaps.db.Context;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.Cache;
import org.efaps.util.cache.CacheReloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The class handles the caching for JAAS systems.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public final class JAASSystem
    extends AbstractAdminObject
{
    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(JAASSystem.class);

    /**
     * This is the SQL select statement to select all JAAS systems from the
     * database.
     */
    private static final String SQL_SELECT = "select "
                                                  + "ID,"
                                                  + "NAME,"
                                                  + "CLASSNAMEPERSON,"
                                                  + "METHODPERSONKEY,"
                                                  + "METHODPERSONNAME,"
                                                  + "METHODPERSONFIRSTNAME,"
                                                  + "METHODPERSONLASTNAME,"
                                                  + "METHODPERSONEMAIL,"
                                                  + "CLASSNAMEROLE,"
                                                  + "METHODROLEKEY,"
                                                  + "CLASSNAMEGROUP,"
                                                  + "METHODGROUPKEY "
                                             + "from V_USERJAASSYSTEM";

    /**
     * Stores all instances of class {@link JAASSystem}.
     */
    private static final JAASSystemCache CACHE = new JAASSystemCache();

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
     */
    public static void initialize()
    {
        JAASSystem.CACHE.initialize(JAASSystem.class);
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

        if ((_method != null) && (_method.trim().length() > 0)) {
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
            } else if ((ret.getParameterTypes() != null) && (ret.getParameterTypes().length > 0)) {
                JAASSystem.LOG.error("could not get a " + _type
                    + " method returning java.lang.String for JAAS System '" + _jaasName + "' (id = " + _jaasId + ")");
                ret = null;
            }
        }
        return ret;
    }

    /**
     * Returns for given parameter <i>_id</i> the instance of class
     * {@link JAASSystem}.
     *
     * @param _id       id to search in the cache
     * @return instance of class {@link JAASSystem}
     */
    public static JAASSystem getJAASSystem(final long _id)
    {
        return JAASSystem.CACHE.get(_id);
    }

    /**
     * Returns for given parameter <i>_name</i> the instance of class
     * {@link JAASSystem}.
     *
     * @param _name     name to search in the cache
     * @return instance of class {@link JAASSystem}
     */
    public static JAASSystem getJAASSystem(final String _name)
    {
        return JAASSystem.CACHE.get(_name);
    }

    /**
     * Returns all cached JAAS system in a set.
     *
     * @return set of all loaded and cached JAAS systems
     */
    public static Set<JAASSystem> getAllJAASSystems()
    {
        final Set<JAASSystem> ret = new HashSet<JAASSystem>();
        for (final Map.Entry<Long, JAASSystem> entry : JAASSystem.CACHE.getCache4Id().entrySet()) {
            ret.add(entry.getValue());
        }
        return ret;
    }

    /**
     * Cache for all JAAS systems.
     */
    private static final class JAASSystemCache
        extends Cache<JAASSystem>
    {
        /**
         * Reads all JAAS systems and stores them in the given mapping caches.
         *
         * @param _cache4Id     cache for the mapping between id and JAAS
         *                      system
         * @param _cache4Name   cache for the mapping between name and JAAS
         *                      system
         * @param _cache4UUID   cache for the mapping between UUID and JAAS
         *                      system
         * @throws CacheReloadException if cache could not be reloaded
         */
        @Override()
        protected void readCache(final Map<Long, JAASSystem> _cache4Id,
                                 final Map<String, JAASSystem> _cache4Name,
                                 final Map<UUID, JAASSystem> _cache4UUID)
            throws CacheReloadException
        {
            ConnectionResource con = null;
            try {
                con = Context.getThreadContext().getConnectionResource();

                Statement stmt = null;
                try {

                    stmt = con.getConnection().createStatement();

                    final ResultSet resultset = stmt.executeQuery(JAASSystem.SQL_SELECT);
                    while (resultset.next()) {
                        final long id = resultset.getLong(1);
                        final String name = resultset.getString(2).trim();
                        final String personClassName = resultset.getString(3);
                        final String personMethodKey = resultset.getString(4);
                        final String personMethodName = resultset.getString(5);
                        final String personMethodFirstName = resultset.getString(6);
                        final String personMethodLastName = resultset.getString(7);
                        final String personMethodEmail = resultset.getString(8);
                        final String roleClassName = resultset.getString(9);
                        final String roleMethodKey = resultset.getString(10);
                        final String groupClassName = resultset.getString(11);
                        final String groupMethodKey = resultset.getString(12);

                        JAASSystem.LOG.debug("read JAAS System '" + name + "' (id = " + id + ")");

                        try {
                            final JAASSystem system = new JAASSystem(id, name);
                            @SuppressWarnings("unchecked")
                            final Class<Principal> forName = (Class<Principal>) Class.forName(personClassName.trim());
                            system.personJAASPrincipleClass = forName;
                            system.personMethodKey = getMethod(system.personJAASPrincipleClass,
                                                               personMethodKey,
                                                               "person key", name, id);
                            system.personMethodName = getMethod(system.personJAASPrincipleClass,
                                                                personMethodName,
                                                                "person name", name, id);
                            Method method = getMethod(system.personJAASPrincipleClass,
                                                      personMethodFirstName,
                                                      "person first name", name, id);
                            if (method != null) {
                                system.personMethodAttributes.put(Person.AttrName.FIRSTNAME,
                                                                  method);
                            }
                            method = getMethod(system.personJAASPrincipleClass,
                                               personMethodLastName,
                                               "person last name", name, id);
                            if (method != null) {
                                system.personMethodAttributes.put(Person.AttrName.LASTNAME,
                                                                  method);
                            }
                            method = getMethod(system.personJAASPrincipleClass,
                                               personMethodEmail,
                                               "person email", name, id);
                            if (method != null) {
// TODO: person email method
//    system.personMethodAttributes.put(Person.AttrName.Email, method);
                            }
                            if ((roleClassName != null) && (roleClassName.trim().length() > 0)) {
                                @SuppressWarnings("unchecked")
                                final Class<Principal> fn = (Class<Principal>) Class.forName(roleClassName.trim());
                                system.roleJAASPrincipleClass = fn;
                                system.roleMethodKey = getMethod(system.roleJAASPrincipleClass,
                                                                 roleMethodKey,
                                                                 "role key", name, id);
                            }
                            if ((groupClassName != null) && (groupClassName.trim().length() > 0)) {
                                @SuppressWarnings("unchecked")
                                final Class<Principal> fn = (Class<Principal>) Class.forName(groupClassName.trim());
                                system.groupJAASPrincipleClass = fn;
                                system.groupMethodKey = getMethod(system.groupJAASPrincipleClass,
                                                                  groupMethodKey,
                                                                  "group key", name, id);
                            }
                            if ((system.personMethodKey != null)
                                    && (system.personMethodName != null)
                                    && ((system.roleJAASPrincipleClass == null)
                                            || ((system.roleJAASPrincipleClass != null)
                                                    && (system.roleMethodKey != null)))
                                    && ((system.groupJAASPrincipleClass == null)
                                            || ((system.groupJAASPrincipleClass != null)
                                                    && (system.groupMethodKey != null)))) {

                                _cache4Id.put(system.getId(), system);
                                _cache4Name.put(system.getName(), system);

                            }
                        } catch (final ClassNotFoundException e) {
                            JAASSystem.LOG.error("could not get a class for JAAS System '"
                                    + name + "' (id = " + id + ")", e);
                        }
                    }
                    resultset.close();
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
                if ((con != null) && con.isOpened()) {
                    try {
                        con.abort();
                    } catch (final EFapsException e) {
                        throw new CacheReloadException("could not read roles", e);
                    }
                }
            }
        }
    };
}
