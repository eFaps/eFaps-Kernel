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

package org.efaps.admin.ui;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.efaps.admin.AbstractAdminObject;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.event.EventDefinition;
import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.bundle.BundleMaker;
import org.efaps.admin.ui.field.Field;
import org.efaps.admin.user.AbstractUserObject;
import org.efaps.admin.user.Company;
import org.efaps.ci.CIAdmin;
import org.efaps.ci.CIAttribute;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.InstanceQuery;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.jaas.AppAccessHandler;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheLogListener;
import org.efaps.util.cache.CacheReloadException;
import org.efaps.util.cache.InfinispanCache;
import org.infinispan.Cache;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * This Class is the Abstract Class for all UserInterfaces in eFaps.<br/>
 * In this Class only a few Methods are defined which are common to all Class
 * inside the UserInterface Package. With this Class all
 * <code>UserInterfaceObjects</code> can be initialized, the Access is checked
 * and the Triggers for the <code>UserInterfaceObjects</code> are handled.
 *
 * @author The eFaps Team
 */
public abstract class AbstractUserInterfaceObject
    extends AbstractAdminObject
{
    /**
     * This enum id used to define the different Modes a Target of a Command can
     * have, like create, edit etc.
     */
    public enum TargetMode
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
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Used as <code>null</code> replacement for the cache.
     */
    private static AbstractUserInterfaceObject NULL = new AbstractUserInterfaceObject(Long.valueOf(0), null, null) {
        private static final long serialVersionUID = 1L;
    };

    /**
     * The instance variable is an Access HashSet to store all userIds (person,
     * group or role) who have access to this user interface object.
     *
     * @see #getAccess
     */
    private final Set<Long> access = new HashSet<Long>();

    /**
     * Constructor to set the id, the uuid and the name of the user interface
     * object.
     *
     * @param _id id to set
     * @param _uuid uuid to set (as String)
     * @param _name name to set
     */
    protected AbstractUserInterfaceObject(final long _id,
                                          final String _uuid,
                                          final String _name)
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
    protected void readFromDB()
        throws CacheReloadException
    {
        readFromDB4Properties();
        readFromDB4Links();
        readFromDB4Access();
    }

    /**
     * The instance method reads the access for this user interface object.
     *
     * @throws CacheReloadException on error during reload
     */
    private void readFromDB4Access()
        throws CacheReloadException
    {
        Statement stmt = null;
        try {
            stmt = Context.getThreadContext().getConnection().createStatement();
            final ResultSet resultset = stmt.executeQuery("select "
                            + "T_UIACCESS.USERABSTRACT "
                            + "from T_UIACCESS "
                            + "where T_UIACCESS.UIABSTRACT=" + getId());
            while (resultset.next()) {
                final long userId = resultset.getLong(1);
                final AbstractUserObject userObject = AbstractUserObject.getUserObject(userId);
                if (userObject == null) {
                    throw new CacheReloadException("user " + userId + " does not exists!");
                } else {
                    getAccess().add(userId);
                }
            }
            resultset.close();
        } catch (final SQLException e) {
            throw new CacheReloadException("could not read access for " + "'" + getName() + "'", e);
        } catch (final EFapsException e) {
            throw new CacheReloadException("could not read access for " + "'" + getName() + "'", e);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (final SQLException e) {
                    throw new CacheReloadException("could not read access for " + "'" + getName() + "'", e);
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
    public boolean hasAccess(final TargetMode _targetMode,
                             final Instance _instance)
        throws EFapsException
    {
        return hasAccess(_targetMode, _instance, null, null);
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
     * @param _callCmd the cmd that called this UI-Object
     * @param _callInstance the instance the object is called in
     * @return <i>true</i> if context user has access, otherwise <i>false</i> is
     *         returned
     * @throws EFapsException on error
     */
    public boolean hasAccess(final TargetMode _targetMode,
                             final Instance _instance,
                             final AbstractCommand _callCmd,
                             final Instance _callInstance)
        throws EFapsException
    {
        boolean ret = false;
        if (getAccess().isEmpty() && !AppAccessHandler.excludeMode()) {
            ret = true;
        } else {
            // first must be checked for the company
            boolean company = false;
            boolean checked = false;
            for (final Long userId : getAccess()) {
                final Company companyObj = Company.get(userId);
                if (companyObj != null) {
                    checked = true;
                    if (companyObj.isAssigned()) {
                        company = true;
                        break;
                    }
                }
            }
            // second it must be checked for the others, if no companies had to
            // be checked or
            // if the check on companies was positiv
            if (!company && !checked || company && checked) {
                for (final Long userId : getAccess()) {
                    final AbstractUserObject userObject = AbstractUserObject.getUserObject(userId);
                    if (!(userObject instanceof Company)) {
                        if (userObject.isAssigned()) {
                            ret = true;
                            break;
                        }
                    }
                }
            }
        }
        if ((ret || AppAccessHandler.excludeMode()) && super.hasEvents(EventType.UI_ACCESSCHECK)) {
            ret = false;
            final List<EventDefinition> events = super.getEvents(EventType.UI_ACCESSCHECK);
            final Parameter parameter = new Parameter();
            parameter.put(ParameterValues.UIOBJECT, this);
            parameter.put(ParameterValues.ACCESSMODE, _targetMode);
            parameter.put(ParameterValues.INSTANCE, _instance);
            parameter.put(ParameterValues.CALL_CMD, _callCmd);
            parameter.put(ParameterValues.CALL_INSTANCE, _callInstance);
            for (final EventDefinition event : events) {
                final Return retIn = event.execute(parameter);
                if (retIn.get(ReturnValues.TRUE) == null) {
                    ret = false;
                    break;
                } else {
                    ret = true;
                }
            }
        }
        return ret;
    }

    /**
     * Getter method for the HashSet instance variable {@link #access}.
     *
     * @return value of the HashSet instance variable {@link #access}
     * @see #access
     */
    protected Set<Long> getAccess()
    {
        return this.access;
    }

    /**
     * @param _uuid             UUUI of the UIObject wanted
     * @param _componentType    type of the UIObject
     * @param _type             datamodel type of the object
     * @param <V> Object type
     * @return UIObject
     * @throws CacheReloadException on error
     */
    @SuppressWarnings("unchecked")
    protected static <V> V get(final UUID _uuid,
                               final Class<V> _componentType,
                               final Type _type)
        throws CacheReloadException
    {
        final Cache<UUID, V> cache = InfinispanCache.get().<UUID, V>getCache(
                        AbstractUserInterfaceObject.getUUIDCacheName(_componentType));
        if (!cache.containsKey(_uuid)
                        && !AbstractUserInterfaceObject
                                        .readObjectFromDB(_componentType, _type, CIAdmin.Abstract.UUID, _uuid)) {
            cache.put(_uuid, (V) AbstractUserInterfaceObject.NULL, 100, TimeUnit.SECONDS);
        }
        final V ret = cache.get(_uuid);
        return ret.equals(AbstractUserInterfaceObject.NULL) ? null : ret;
    }

    /**
     * @param _id ID of the UIObject wanted
     * @param _componentType type of the UIObject
     * @param _type datamodel type of the object
     * @param <V> Object type
     * @return UIObject
     * @throws CacheReloadException on error
     */
    @SuppressWarnings("unchecked")
    protected static <V> V get(final Long _id,
                               final Class<V> _componentType,
                               final Type _type)
        throws CacheReloadException
    {
        final Cache<Long, V> cache = InfinispanCache.get().<Long, V>getCache(
                        AbstractUserInterfaceObject.getIDCacheName(_componentType));
        if (!cache.containsKey(_id) && !
                        AbstractUserInterfaceObject.readObjectFromDB(_componentType, _type, CIAdmin.Abstract.ID, _id)) {
            cache.put(_id, (V) AbstractUserInterfaceObject.NULL, 100, TimeUnit.SECONDS);
        }
        final V ret = cache.get(_id);
        return ret.equals(AbstractUserInterfaceObject.NULL) ? null : ret;
    }

    /**
     * @param _name Name of the UIObject wanted
     * @param _componentType type of the UIObject
     * @param _type datamodel type of the object
     * @param <V> Object type
     * @return UIObject
     * @throws CacheReloadException on error
     */
    @SuppressWarnings("unchecked")
    protected static <V> V get(final String _name,
                               final Class<V> _componentType,
                               final Type _type)
        throws CacheReloadException
    {
        final Cache<String, V> cache = InfinispanCache.get().<String, V>getCache(
                        AbstractUserInterfaceObject.getNameCacheName(_componentType));
        if (!cache.containsKey(_name)
                        && !AbstractUserInterfaceObject.readObjectFromDB(_componentType, _type, CIAdmin.Abstract.Name,
                                        _name)) {
            cache.put(_name, (V) AbstractUserInterfaceObject.NULL, 100, TimeUnit.SECONDS);
        }
        final V ret = cache.get(_name);
        return ret.equals(AbstractUserInterfaceObject.NULL) ? null : ret;
    }

    /**
     * @param _componentType class of the UIObject
     * @return name for the UUID Cache
     */
    protected static String getUUIDCacheName(final Class<?> _componentType)
    {
        return _componentType.getName() + ".UUID";
    }
    /**
     * @param _componentType class of the UIObject
     * @return name for the ID Cache
     */

    protected static String getIDCacheName(final Class<?> _componentType)
    {
        return _componentType.getName() + ".ID";
    }

    /**
     * @param _componentType class of the UIObject
     * @return name for the Name Cache
     */
    protected static String getNameCacheName(final Class<?> _componentType)
    {
        return _componentType.getName() + ".Name";
    }

    /**
     * @param _object UIObject to be cache
     */
    @SuppressFBWarnings("RV_RETURN_VALUE_OF_PUTIFABSENT_IGNORED")
    protected static void cacheUIObject(final AbstractUserInterfaceObject _object)
    {
        final Cache<UUID, AbstractUserInterfaceObject> cache4UUID = InfinispanCache.get()
                        .<UUID, AbstractUserInterfaceObject>getIgnReCache(
                                        AbstractUserInterfaceObject.getUUIDCacheName(_object.getClass()));
        cache4UUID.putIfAbsent(_object.getUUID(), _object);

        final Cache<String, AbstractUserInterfaceObject> nameCache = InfinispanCache.get()
                        .<String, AbstractUserInterfaceObject>getIgnReCache(
                                        AbstractUserInterfaceObject.getNameCacheName(_object.getClass()));
        nameCache.putIfAbsent(_object.getName(), _object);

        final Cache<Long, AbstractUserInterfaceObject> idCache = InfinispanCache.get()
                        .<Long, AbstractUserInterfaceObject>getIgnReCache(
                                        AbstractUserInterfaceObject.getIDCacheName(_object.getClass()));
        idCache.putIfAbsent(_object.getId(), _object);
    }

    /**
     * @param _componentType  c lass of UIObject to be retrieved
     * @param _type             DataModel TYpe of UIObject to be retrieved
     * @param _ciAttr           Attribute used for filtered
     * @param _value            value to filtered
     * @return true if successful
     * @throws CacheReloadException on error
     */
    private static boolean readObjectFromDB(final Class<?> _componentType,
                                            final Type _type,
                                            final CIAttribute _ciAttr,
                                            final Object _value)
        throws CacheReloadException
    {
        boolean ret = false;
        try {
            final QueryBuilder queryBldr = new QueryBuilder(_type);
            queryBldr.addWhereAttrEqValue(_ciAttr, _value.toString());
            final InstanceQuery query = queryBldr.getQuery();
            query.setIncludeChildTypes(false);

            final List<Instance> instances = query.execute();
            final MultiPrintQuery multi = new MultiPrintQuery(instances);
            multi.addAttribute(CIAdmin.Abstract.Name,
                            CIAdmin.Abstract.UUID);
            multi.executeWithoutAccessCheck();
            if (multi.next()) {
                final long id = multi.getCurrentInstance().getId();
                final String name = multi.<String>getAttribute(CIAdmin.Abstract.Name);
                final String uuid = multi.<String>getAttribute(CIAdmin.Abstract.UUID);
                final Constructor<?> uiObjConst = _componentType.getConstructor(Long.class, String.class,
                                String.class);
                final AbstractUserInterfaceObject uiObje = (AbstractUserInterfaceObject) uiObjConst.newInstance(id,
                                uuid, name);
                AbstractUserInterfaceObject.cacheUIObject(uiObje);
                uiObje.readFromDB();
                ret = true;
            }
        } catch (final NoSuchMethodException e) {
            throw new CacheReloadException("NoSuchMethodException", e);
        } catch (final EFapsException e) {
            throw new CacheReloadException("EFapsException", e);
        } catch (final IllegalArgumentException e) {
            throw new CacheReloadException("IllegalArgumentException", e);
        } catch (final InstantiationException e) {
            throw new CacheReloadException("InstantiationException", e);
        } catch (final IllegalAccessException e) {
            throw new CacheReloadException("IllegalAccessException", e);
        } catch (final InvocationTargetException e) {
            throw new CacheReloadException("InvocationTargetException", e);
        }
        return ret;
    }

    /**
     * Method to initialize the Cache of this CacheObjectInterface.
     *
     * @throws CacheReloadException on error
     */
    public static void initialize()
        throws CacheReloadException
    {
        Field.initialize();

        if (InfinispanCache.get().exists(AbstractUserInterfaceObject.getUUIDCacheName(Command.class))) {
            InfinispanCache.get().<UUID, Type>getCache(AbstractUserInterfaceObject.getUUIDCacheName(Command.class))
                            .clear();
            InfinispanCache.get().<UUID, Type>getCache(AbstractUserInterfaceObject.getIDCacheName(Command.class))
                            .clear();
            InfinispanCache.get().<UUID, Type>getCache(AbstractUserInterfaceObject.getNameCacheName(Command.class))
                            .clear();
            InfinispanCache.get().<UUID, Type>getCache(AbstractUserInterfaceObject.getUUIDCacheName(Menu.class))
                            .clear();
            InfinispanCache.get().<UUID, Type>getCache(AbstractUserInterfaceObject.getIDCacheName(Menu.class)).clear();
            InfinispanCache.get().<UUID, Type>getCache(AbstractUserInterfaceObject.getNameCacheName(Menu.class))
                            .clear();
            InfinispanCache.get().<UUID, Type>getCache(AbstractUserInterfaceObject.getUUIDCacheName(Image.class))
                            .clear();
            InfinispanCache.get().<UUID, Type>getCache(AbstractUserInterfaceObject.getIDCacheName(Image.class)).clear();
            InfinispanCache.get().<UUID, Type>getCache(AbstractUserInterfaceObject.getNameCacheName(Image.class))
                            .clear();

            InfinispanCache.get().<UUID, Type>getCache(AbstractUserInterfaceObject.getUUIDCacheName(Search.class))
                            .clear();
            InfinispanCache.get().<UUID, Type>getCache(AbstractUserInterfaceObject.getIDCacheName(Search.class))
                            .clear();
            InfinispanCache.get().<UUID, Type>getCache(AbstractUserInterfaceObject.getNameCacheName(Search.class))
                            .clear();

            InfinispanCache.get().<UUID, Type>getCache(AbstractUserInterfaceObject.getUUIDCacheName(Form.class))
                            .clear();
            InfinispanCache.get().<UUID, Type>getCache(AbstractUserInterfaceObject.getIDCacheName(Form.class)).clear();
            InfinispanCache.get().<UUID, Type>getCache(AbstractUserInterfaceObject.getNameCacheName(Form.class))
                            .clear();

            InfinispanCache.get().<UUID, Type>getCache(AbstractUserInterfaceObject.getUUIDCacheName(Table.class))
                            .clear();
            InfinispanCache.get().<UUID, Type>getCache(AbstractUserInterfaceObject.getIDCacheName(Table.class)).clear();
            InfinispanCache.get().<UUID, Type>getCache(AbstractUserInterfaceObject.getNameCacheName(Table.class))
                            .clear();
        } else {
            InfinispanCache.get().<UUID, Type>getCache(AbstractUserInterfaceObject.getUUIDCacheName(Command.class))
                            .addListener(new CacheLogListener(Command.LOG));
            InfinispanCache.get().<UUID, Type>getCache(AbstractUserInterfaceObject.getIDCacheName(Command.class))
                            .addListener(new CacheLogListener(Command.LOG));
            InfinispanCache.get().<UUID, Type>getCache(AbstractUserInterfaceObject.getNameCacheName(Command.class))
                            .addListener(new CacheLogListener(Command.LOG));

            InfinispanCache.get().<UUID, Type>getCache(AbstractUserInterfaceObject.getUUIDCacheName(Menu.class))
                            .addListener(new CacheLogListener(Menu.LOG));
            InfinispanCache.get().<UUID, Type>getCache(AbstractUserInterfaceObject.getIDCacheName(Menu.class))
                            .addListener(new CacheLogListener(Menu.LOG));
            InfinispanCache.get().<UUID, Type>getCache(AbstractUserInterfaceObject.getNameCacheName(Menu.class))
                            .addListener(new CacheLogListener(Menu.LOG));

            InfinispanCache.get().<UUID, Type>getCache(AbstractUserInterfaceObject.getUUIDCacheName(Image.class))
                            .addListener(new CacheLogListener(Image.LOG));
            InfinispanCache.get().<UUID, Type>getCache(AbstractUserInterfaceObject.getIDCacheName(Image.class))
                            .addListener(new CacheLogListener(Image.LOG));
            InfinispanCache.get().<UUID, Type>getCache(AbstractUserInterfaceObject.getNameCacheName(Image.class))
                            .addListener(new CacheLogListener(Image.LOG));

            InfinispanCache.get().<UUID, Type>getCache(AbstractUserInterfaceObject.getUUIDCacheName(Search.class))
                            .addListener(new CacheLogListener(Search.LOG));
            InfinispanCache.get().<UUID, Type>getCache(AbstractUserInterfaceObject.getIDCacheName(Search.class))
                            .addListener(new CacheLogListener(Search.LOG));
            InfinispanCache.get().<UUID, Type>getCache(AbstractUserInterfaceObject.getNameCacheName(Search.class))
                            .addListener(new CacheLogListener(Search.LOG));

            InfinispanCache.get().<UUID, Type>getCache(AbstractUserInterfaceObject.getUUIDCacheName(Form.class))
                            .addListener(new CacheLogListener(Form.LOG));
            InfinispanCache.get().<UUID, Type>getCache(AbstractUserInterfaceObject.getIDCacheName(Form.class))
                            .addListener(new CacheLogListener(Form.LOG));
            InfinispanCache.get().<UUID, Type>getCache(AbstractUserInterfaceObject.getNameCacheName(Form.class))
                            .addListener(new CacheLogListener(Form.LOG));

            InfinispanCache.get().<UUID, Type>getCache(AbstractUserInterfaceObject.getUUIDCacheName(Table.class))
                            .addListener(new CacheLogListener(Table.LOG));
            InfinispanCache.get().<UUID, Type>getCache(AbstractUserInterfaceObject.getIDCacheName(Table.class))
                            .addListener(new CacheLogListener(Table.LOG));
            InfinispanCache.get().<UUID, Type>getCache(AbstractUserInterfaceObject.getNameCacheName(Table.class))
                            .addListener(new CacheLogListener(Table.LOG));
        }
        BundleMaker.initialize();
    }
}
