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

package org.efaps.admin.ui;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.efaps.admin.AbstractAdminObject;
import org.efaps.admin.EFapsClassNames;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.event.EventDefinition;
import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.user.AbstractUserObject;
import org.efaps.admin.user.Role;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.Cache;
import org.efaps.util.cache.CacheReloadException;

/**
 * This Class is the Abstract Class for all UserInterfaces in eFaps.<br/>
 * In this Class only a few Methods are defined which are common to all Class
 * inside the UserInterface Package. With this Class all
 * <code>UserInterfaceObjects</code> can be initialized, the Access is checked
 * and the Triggers for the <code>UserInterfaceObjects</code> are handled.
 *
 * @author The eFaps Team
 * @version $Id: AbstractUserInterfaceObject.java 2609 2009-06-10 22:56:38Z
 *          jan.moxter $
 */
public abstract class AbstractUserInterfaceObject extends AbstractAdminObject
{
    /**
     * This enum id used to define the different Modes a Target of a Command can
     * have, like create, edit etc.
     */
    public static enum TargetMode
    {
        /** TargetMode for connect. */
        CONNECT,
        /** TargetMode for connect. */
        CREATE,
        /** TargetMode for create. */
        EDIT,
        /** TargetMode for print. */
        PRINT,
        /** TargetMode for edit. */
        SEARCH,
        /** TargetMode for unkown. */
        UNKNOWN,
        /** TargetMode for view. */
        VIEW;
    }

    /**
     * The instance variable is an Access HashSet to store all users (person,
     * group or role) who have access to this user interface object.
     *
     * @see #getAccess
     */
    private final Set<AbstractUserObject> access = new HashSet<AbstractUserObject>();

    /**
     * Constructor to set the id, the uuid and the name of the user interface
     * object.
     *
     * @param _id id to set
     * @param _uuid uuid to set (as String)
     * @param _name name to set
     */
    protected AbstractUserInterfaceObject(final long _id, final String _uuid, final String _name)
    {
        super(_id, _uuid, _name);
    }

    /**
     * The instance method reads all needed information for this user interface
     * object. Here, only the properties are read from the database
     *
     * @see #readFromDB4Properties
     * @see #readFromDB4Links
     * @see #readFromDB4Access
     * @throws CacheReloadException on error during reload
     */
    protected void readFromDB() throws CacheReloadException
    {
        readFromDB4Properties();
        readFromDB4Links();
        readFromDB4Access();
    }

    /**
     * The instance method reads the access for this user interface object.
     *
     * @todo use SearchQuery
     * @throws CacheReloadException on error during reload
     */
    private void readFromDB4Access() throws CacheReloadException
    {
        Statement stmt = null;
        try {
            stmt = Context.getThreadContext().getConnection().createStatement();
            final ResultSet resultset = stmt.executeQuery("select " + "T_UIACCESS.USERABSTRACT " + "from T_UIACCESS "
                            + "where T_UIACCESS.UIABSTRACT=" + getId());
            while (resultset.next()) {
                final long userId = resultset.getLong(1);
                final AbstractUserObject userObject = AbstractUserObject.getUserObject(userId);
                if (userObject == null) {
                    throw new Exception("user " + userId + " does not exists!");
                } else {
                    getAccess().add(userObject);
                }
            }
            resultset.close();
        } catch (final Exception e) {
            throw new CacheReloadException("could not read access for " + "'" + getName() + "'", e);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (final SQLException e) {
                }
            }
        }
    }

    /**
     * Check, if the user of the context has access to this user interface
     * object. <br>
     * The Check is made in the following order: <br>
     * <ol>
     * <li>If no access User or role is assigned to this user interface object,
     * all user have access and the return is <i>true</i> => go on with Step 3</li>
     * <li>else check if the context person is assigned to one of the user
     * objects.</li>
     * <li>if Step 1 or Step 2 have <i>true</i> and the context an Event of the
     * Type <code>TriggerEvent.ACCESSCHECK</code>, the return of the trigger
     * initiated program is returned</li>
     * </ol>
     *
     * @param _targetMode targetmode of the access
     * @param _instance the field will represent, e.g. on edit mode
     * @return <i>true</i> if context user has access, otherwise <i>false</i> is
     *         returned
     * @throws EFapsException on error
     */
    public boolean hasAccess(final TargetMode _targetMode, final Instance _instance) throws EFapsException
    {
        boolean ret = false;
        if (getAccess().isEmpty()) {
            ret = true;
        } else {
            for (final AbstractUserObject userObject : getAccess()) {
                if (userObject.isAssigned()) {
                    ret = true;
                    break;
                }
            }
        }
        if (ret && super.hasEvents(EventType.UI_ACCESSCHECK)) {
            ret = false;
            final List<EventDefinition> events = super.getEvents(EventType.UI_ACCESSCHECK);

            final Parameter parameter = new Parameter();
            parameter.put(ParameterValues.UIOBJECT, this);
            parameter.put(ParameterValues.ACCESSMODE, _targetMode);
            parameter.put(ParameterValues.INSTANCE, _instance);
            for (final EventDefinition event : events) {
                final Return retIn = event.execute(parameter);
                ret = retIn.get(ReturnValues.TRUE) != null;
            }
        }
        return ret;
    }

    /**
     * Getter method for the HashSet instance variable {@link #access}.
     *
     * @return value of the HashSet instance variable {@link #access}
     * @see #access
     * @see #add(Role)
     */
    protected Set<AbstractUserObject> getAccess()
    {
        return this.access;
    }

    /**
     * Method to initialize the Cache of this CacheObjectInterface.
     *
     * @throws CacheReloadException on error
     */
    public static void initialize() throws CacheReloadException
    {
        Image.getCache().initialize(AbstractUserInterfaceObject.class);
        Command.getCache().initialize(AbstractUserInterfaceObject.class);
        Menu.getCache().initialize(AbstractUserInterfaceObject.class);
        Search.getCache().initialize(AbstractUserInterfaceObject.class);
        Form.getCache().initialize(AbstractUserInterfaceObject.class);
        Table.getCache().initialize(AbstractUserInterfaceObject.class);
        Picker.getCache().initialize(AbstractUserInterfaceObject.class);
        Image.getCache().readFromDB();
        Command.getCache().readFromDB();
        Menu.getCache().readFromDB();
        Search.getCache().readFromDB();
        Form.getCache().readFromDB();
        Table.getCache().readFromDB();
        Picker.getCache().readFromDB();
    }

    /**
     * Inner Class to store the UserInterfaces in a Cache.
     *
     * @param <UIObj>
     */
    protected static class UserInterfaceObjectCache<UIObj extends AbstractUserInterfaceObject> extends Cache<UIObj>
    {

        /**
         * Stores the caller class.
         */
        private final Class<UIObj> callerClass;

        /**
         * Constructor.
         *
         * @param _callerClass callerClass
         */
        protected UserInterfaceObjectCache(final Class<UIObj> _callerClass)
        {
            this.callerClass = _callerClass;
        }

        /**
         * All cached user interface objects are read into the cache.
         *
         * @throws CacheReloadException
         *
         * @throws CacheReloadException on error during reload
         */
        protected void readFromDB() throws CacheReloadException
        {
            for (final UIObj uiObj : getCache4Id().values()) {
                uiObj.readFromDB();
            }
        }

        /**
         * Get the <code>EFapsClassName</code> of this
         * <code>UserInterfaceObject</code>.
         *
         * @return <code>EFapsClassName</code>
         * @throws EFapsException on error
         */
        private EFapsClassNames getEFapsClassName() throws EFapsException
        {
            final Class<UIObj> uiObjClass = this.callerClass;
            try {
                return ((EFapsClassNames) uiObjClass.getField("EFAPS_CLASSNAME").get(null));
            } catch (final NoSuchFieldException e) {
                throw new EFapsException(AbstractUserInterfaceObject.UserInterfaceObjectCache.class,
                                "getEFapsClassName.EFapsClassNameNotExist", e, uiObjClass.getName());
            } catch (final IllegalAccessException e) {
                throw new EFapsException(AbstractUserInterfaceObject.UserInterfaceObjectCache.class,
                                "getEFapsClassName.EFapsClassNameNotAccessable", e, uiObjClass.getName());
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void readCache(final Map<Long, UIObj> _cache4Id, final Map<String, UIObj> _cache4Name,
                        final Map<UUID, UIObj> _cache4UUID) throws CacheReloadException
        {

            final Class<UIObj> uiObjClass = this.callerClass;
            try {
                if (Type.get(getEFapsClassName()) != null) {
                    final SearchQuery query = new SearchQuery();
                    query.setQueryTypes(Type.get(getEFapsClassName()).getName());
                    query.addSelect("ID");
                    query.addSelect("Name");
                    query.addSelect("UUID");
                    query.executeWithoutAccessCheck();
                    while (query.next()) {
                        final long id = (Long) query.get("ID");
                        final String name = (String) query.get("Name");
                        final String uuid = (String) query.get("UUID");
                        final Constructor<UIObj> uiObj = uiObjClass.getConstructor(Long.class, String.class,
                                        String.class);
                        final UIObj uiObj2 = uiObj.newInstance(id, uuid, name);
                        _cache4Id.put(uiObj2.getId(), uiObj2);
                        _cache4Name.put(uiObj2.getName(), uiObj2);
                        _cache4UUID.put(uiObj2.getUUID(), uiObj2);
                    }
                }
            } catch (final NoSuchMethodException e) {
                throw new CacheReloadException("class '" + uiObjClass.getName()
                                + "' does not implement contructor (Long, String, String)", e);
            } catch (final InstantiationException e) {
                throw new CacheReloadException("could not instantiate class '" + uiObjClass.getName() + "'", e);
            } catch (final IllegalAccessException e) {
                throw new CacheReloadException("could not access class '" + uiObjClass.getName() + "'", e);
            } catch (final InvocationTargetException e) {
                throw new CacheReloadException("could not invoce constructor of class '" + uiObjClass.getName() + "'",
                                e);
            } catch (final EFapsException e) {
                throw new CacheReloadException("could not initialise cache", e);
            }
        }
    }
}
