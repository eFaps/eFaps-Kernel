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

package org.efaps.admin.access;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.efaps.admin.AbstractAdminObject;
import org.efaps.admin.datamodel.Status;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.Context;
import org.efaps.db.wrapper.SQLPart;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheLogListener;
import org.efaps.util.cache.CacheReloadException;
import org.efaps.util.cache.InfinispanCache;
import org.infinispan.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author The eFaps Team
 */
public final class AccessSet
    extends AbstractAdminObject
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AccessSet.class);

    /**
     * This is the SQL select statement to select a role from the database by
     * ID.
     */
    private static final String SQL_ID = new SQLSelect()
                    .column("ID")
                    .column("UUID")
                    .column("NAME")
                    .from("T_ACCESSSET", 0)
                    .addPart(SQLPart.WHERE).addColumnPart(0, "ID").addPart(SQLPart.EQUAL).addValuePart("?").toString();

    /**
     * This is the SQL select statement to select a role from the database by
     * Name.
     */
    private static final String SQL_NAME = new SQLSelect()
                    .column("ID")
                    .column("UUID")
                    .column("NAME")
                    .from("T_ACCESSSET", 0)
                    .addPart(SQLPart.WHERE).addColumnPart(0, "NAME").addPart(SQLPart.EQUAL).addValuePart("?")
                    .toString();

    /**
     * This is the SQL select statement to select a role from the database by
     * UUID.
     */
    private static final String SQL_UUID = new SQLSelect()
                    .column("ID")
                    .column("UUID")
                    .column("NAME")
                    .from("T_ACCESSSET", 0)
                    .addPart(SQLPart.WHERE).addColumnPart(0, "UUID").addPart(SQLPart.EQUAL).addValuePart("?")
                    .toString();

    /**
     * Name of the Cache by UUID.
     */
    private static final String UUIDCACHE = AccessSet.class.getName() + ".UUID";

    /**
     * Name of the Cache by ID.
     */
    private static final String IDCACHE = AccessSet.class.getName() + ".ID";

    /**
     * Name of the Cache by Name.
     */
    private static final String NAMECACHE = AccessSet.class.getName() + ".Name";

    /**
     * This is the sql select statement to select the links from all access sets
     * to all access types in the database.
     *
     * @see #init4ReadLinks2AccessTypes
     */
    private static final String SQL_SET2TYPE = new SQLSelect()
                    .column("ACCESSTYPE")
                    .from("T_ACCESSSET2TYPE", 0)
                    .addPart(SQLPart.WHERE).addColumnPart(0, "ACCESSSET").addPart(SQLPart.EQUAL).addValuePart("?")
                    .toString();
    /**
     * This is the sql select statement to select the links from all access sets
     * to all data model types in the database.
     *
     * @see #init4ReadLinks2DMTypes
     */
    private static final String SQL_SET2DMTYPE = new SQLSelect()
                    .column("DMTYPE")
                    .from("T_ACCESSSET2DMTYPE", 0)
                    .addPart(SQLPart.WHERE).addColumnPart(0, "ACCESSSET").addPart(SQLPart.EQUAL).addValuePart("?")
                    .toString();

    /**
     * This is the sql select statement to select the links from all access sets
     * to all stati in the database.
     *
     * @see #init4ReadLinks2DMTypes
     */
    private static final String SQL_SET2STATUS = new SQLSelect()
                    .column("ACCESSSTATUS")
                    .from("T_ACCESSSET2STATUS", 0)
                    .addPart(SQLPart.WHERE).addColumnPart(0, "ACCESSSET").addPart(SQLPart.EQUAL).addValuePart("?")
                    .toString();

    /**
     * This is the sql select statement to select the links from all access sets
     * to all userAbstract in the database.
     */
    private static final String SQL_SET2PERSON = new SQLSelect()
                    .column("USERABSTRACT")
                    .from("T_ACCESSSET2USER", 0)
                    .addPart(SQLPart.WHERE).addColumnPart(0, "ACCESSSET").addPart(SQLPart.EQUAL).addValuePart("?")
                    .toString();

    /**
     * All related access types of this access set are referenced in this
     * instance variable.
     *
     * @see #getAccessTypes
     */
    private final Set<AccessType> accessTypes = new HashSet<>();

    /**
     * All related data models types of this access set are referenced in this
     * instance variable.
     *
     * @see #getDataModelTypes
     */
    private final Set<Long> dataModelTypes = new HashSet<>();

    /**
     * All related Status of this access set are referenced in this instance
     * variable.
     */
    private final Set<Status> statuses = new HashSet<>();

    /**
     * All related Abstract User (Roles, Person) of this access set are
     * referenced in this instance variable.
     */
    private final Set<Long> userIds = new HashSet<>();

    /**
     * This is the constructor.
     *
     * @param _id id of this access type
     * @param _uuid universal unique identifier of this access type
     * @param _name name of this access type
     */
    private AccessSet(final long _id,
                      final String _uuid,
                      final String _name)
    {
        super(_id, _uuid, _name);
    }

    /**
     * This is the getter method for instance variable {@link #accessTypes}.
     *
     * @return the value of the instance variable {@link #accessTypes}.
     * @see #accessTypes
     */
    public Set<AccessType> getAccessTypes()
    {
        return this.accessTypes;
    }

    /**
     * This is the getter method for instance variable {@link #dataModelTypes}.
     *
     * @return the value of the instance variable {@link #dataModelTypes}.
     * @see #dataModelTypes
     * @throws CacheReloadException on error
     */
    public Set<Type> getDataModelTypes()
        throws CacheReloadException
    {
        final Set<Type> ret = new HashSet<>();
        for (final Long id : this.dataModelTypes) {
            ret.add(Type.get(id));
        }
        return Collections.unmodifiableSet(ret);
    }

    /**
     * This is the getter method for instance variable {@link #dataModelTypes}.
     *
     * @return the value of the instance variable {@link #dataModelTypes}.
     * @see #dataModelTypes
     */
    public Set<Long> getDataModelTypesIds()
    {
        return this.dataModelTypes;
    }

    /**
     * Getter method for instance variable {@link #stati}.
     *
     * @return value of instance variable {@link #stati}
     */
    public Set<Status> getStatuses()
    {
        return this.statuses;
    }

    /**
     * Getter method for instance variable {@link #userIds}.
     *
     * @return value of instance variable {@link #userIds}
     */
    public Set<Long> getUserIds()
    {
        return this.userIds;
    }

    /**
     * Read the related {@link org.efaps.admin.access.AccessTypes}.
     * @throws CacheReloadException on error
     */
    private void readLinks2AccessTypes()
        throws CacheReloadException
    {
        Connection con = null;
        try {
            final List<Long> values = new ArrayList<>();
            con = Context.getConnection();
            PreparedStatement stmt = null;
            try {
                stmt = con.prepareStatement(AccessSet.SQL_SET2TYPE);
                stmt.setObject(1, getId());
                final ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    values.add(rs.getLong(1));
                }
                rs.close();
            } finally {
                if (stmt != null) {
                    stmt.close();
                }
            }
            con.commit();
            for (final Long accessTypeId : values) {
                final AccessType accessType = AccessType.getAccessType(accessTypeId);
                if (accessType == null) {
                    AccessSet.LOG.error("could not found access type with id " + "'" + accessTypeId + "'");
                } else {
                    AccessSet.LOG.debug(
                            "read link from AccessSet '{}' (id = {}, uuid = {}) to AccessType '{}' (id = {} uuid = {})",
                                    getName(), getId(), getUUID(), accessType.getName(), accessType.getId(),
                                    accessType.getUUID());
                    getAccessTypes().add(accessType);
                }
            }
        } catch (final SQLException e) {
            throw new CacheReloadException("could not read roles", e);
        } catch (final EFapsException e) {
            throw new CacheReloadException("could not read roles", e);
        } finally {
            try {
                if (con != null && con.isClosed()) {
                    con.close();
                }
            } catch (final SQLException e) {
                throw new CacheReloadException("Cannot read a type for an attribute.", e);
            }
        }
    }

    /**
     * Read the related {@link org.efaps.admin.datamodel.Type}.
     * @throws CacheReloadException on error
     */
    private void readLinks2DMTypes()
        throws CacheReloadException
    {
        Connection con = null;
        try {
            final List<Long> values = new ArrayList<>();
            con = Context.getConnection();
            PreparedStatement stmt = null;
            try {
                stmt = con.prepareStatement(AccessSet.SQL_SET2DMTYPE);
                stmt.setObject(1, getId());
                final ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    values.add(rs.getLong(1));
                }
                rs.close();
            } finally {
                if (stmt != null) {
                    stmt.close();
                }
            }
            con.commit();
            for (final Long dataModelTypeId : values) {
                final Type dataModelType = Type.get(dataModelTypeId);
                if (dataModelType == null) {
                    AccessSet.LOG.error("could not found data model type with id " + "'" + dataModelTypeId + "'");
                } else {
                    AccessSet.LOG.debug(
                         "read link from AccessSet '{}' (id = {}, uuid = {}) to DataModelType '{}' (id = {} uuid = {})",
                                    getName(), getId(), getUUID(), dataModelType.getName(), dataModelType.getId(),
                                    dataModelType.getUUID());
                    this.dataModelTypes.add(dataModelType.getId());
                    dataModelType.addAccessSet(this);
                }
            }
        } catch (final SQLException e) {
            throw new CacheReloadException("could not read roles", e);
        } catch (final EFapsException e) {
            throw new CacheReloadException("could not read roles", e);
        } finally {
            try {
                if (con != null && con.isClosed()) {
                    con.close();
                }
            } catch (final SQLException e) {
                throw new CacheReloadException("Cannot read a type for an attribute.", e);
            }
        }
    }

    /**
     * Read the related {@link org.efaps.admin.datamodel.Status}.
     * @throws CacheReloadException on error
     */
    private void readLinks2Status()
        throws CacheReloadException
    {
        Connection con = null;
        try {
            final List<Long> values = new ArrayList<>();
            con = Context.getConnection();
            PreparedStatement stmt = null;
            try {
                stmt = con.prepareStatement(AccessSet.SQL_SET2STATUS);
                stmt.setObject(1, getId());
                final ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    values.add(rs.getLong(1));
                }
                rs.close();
            } finally {
                if (stmt != null) {
                    stmt.close();
                }
            }
            con.commit();
            for (final Long statusId : values) {
                final Status status = Status.get(statusId);
                if (status == null) {
                    AccessSet.LOG.error("could not found status with id " + "'" + statusId + "'");
                } else {
                    AccessSet.LOG.debug(
                                "read link from AccessSet '{}' (id = {}, uuid = {}) to status '{}' (id = {})",
                                    getName(), getId(), getUUID(), status.getKey(), status.getId());
                    getStatuses().add(status);
                }
            }
        } catch (final SQLException e) {
            throw new CacheReloadException("could not read roles", e);
        } catch (final EFapsException e) {
            throw new CacheReloadException("could not read roles", e);
        } finally {
            try {
                if (con != null && con.isClosed()) {
                    con.close();
                }
            } catch (final SQLException e) {
                throw new CacheReloadException("Cannot read a type for an attribute.", e);
            }
        }
    }

    /**
     * Read the related Abstract Person (Role, Person).
     * @throws CacheReloadException on error
     */
    private void readLinks2Person()
        throws CacheReloadException
    {
        Connection con = null;
        try {
            final List<Long> values = new ArrayList<>();
            con = Context.getConnection();
            PreparedStatement stmt = null;
            try {
                stmt = con.prepareStatement(AccessSet.SQL_SET2PERSON);
                stmt.setObject(1, getId());
                final ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    values.add(rs.getLong(1));
                }
                rs.close();
            } finally {
                if (stmt != null) {
                    stmt.close();
                }
            }
            con.commit();
            for (final Long personId : values) {
                AccessSet.LOG.debug(
                                "read link from AccessSet '{}' (id = {}, uuid = {}) to abstract person (id = {})",
                                getName(), getId(), getUUID(), personId);
                getUserIds().add(personId);
            }
        } catch (final SQLException e) {
            throw new CacheReloadException("could not read persons for accessset", e);
        } catch (final EFapsException e) {
            throw new CacheReloadException("could not read persons for accessset", e);
        } finally {
            try {
                if (con != null && con.isClosed()) {
                    con.close();
                }
            } catch (final SQLException e) {
                throw new CacheReloadException("Cannot read a type for an attribute.", e);
            }
        }
    }

    /**
     * Method to initialize the Cache of this CacheObjectInterface.
     */
    public static void initialize()
    {
        if (InfinispanCache.get().exists(AccessSet.UUIDCACHE)) {
            InfinispanCache.get().<UUID, AccessSet>getCache(AccessSet.UUIDCACHE).clear();
        } else {
            InfinispanCache.get().<UUID, AccessSet>getCache(AccessSet.UUIDCACHE)
                            .addListener(new CacheLogListener(AccessSet.LOG));
        }
        if (InfinispanCache.get().exists(AccessSet.IDCACHE)) {
            InfinispanCache.get().<Long, AccessSet>getCache(AccessSet.IDCACHE).clear();
        } else {
            InfinispanCache.get().<Long, AccessSet>getCache(AccessSet.IDCACHE)
                            .addListener(new CacheLogListener(AccessSet.LOG));
        }
        if (InfinispanCache.get().exists(AccessSet.NAMECACHE)) {
            InfinispanCache.get().<String, AccessSet>getCache(AccessSet.NAMECACHE).clear();
        } else {
            InfinispanCache.get().<String, AccessSet>getCache(AccessSet.NAMECACHE)
                            .addListener(new CacheLogListener(AccessSet.LOG));
        }
    }

    /**
     * Returns for given identifier in <i>_id</i> the cached instance of class
     * AccessSet.
     *
     * @param _id id the AccessSet is wanted for
     * @return instance of class AccessSet
     * @throws CacheReloadException on error
     */
    public static AccessSet get(final long _id)
        throws CacheReloadException
    {
        final Cache<Long, AccessSet> cache = InfinispanCache.get().<Long, AccessSet>getCache(AccessSet.IDCACHE);
        if (!cache.containsKey(_id)) {
            AccessSet.getAccessSetFromDB(AccessSet.SQL_ID, _id);
        }
        return cache.get(_id);
    }

    /**
     * Returns for given name in <i>_name</i> the cached instance of class
     * AccessSet.
     *
     * @param _name name the AccessSet is wanted for
     * @return instance of class AccessSet
     * @throws CacheReloadException on error
     */
    public static AccessSet get(final String _name)
        throws CacheReloadException
    {
        final Cache<String, AccessSet> cache = InfinispanCache.get().<String, AccessSet>getCache(AccessSet.NAMECACHE);
        if (!cache.containsKey(_name)) {
            AccessSet.getAccessSetFromDB(AccessSet.SQL_NAME, _name);
        }
        return cache.get(_name);
    }

    /**
     * Returns for given universal unique identifier in <i>_uuid</i> the cached
     * instance of class AccessSet.
     *
     * @param _uuid UUID the AccessSet is wanted for
     * @return instance of class AccessSet
     * @throws CacheReloadException on error
     */
    public static AccessSet get(final UUID _uuid)
        throws CacheReloadException
    {
        final Cache<UUID, AccessSet> cache = InfinispanCache.get().<UUID, AccessSet>getCache(AccessSet.UUIDCACHE);
        if (!cache.containsKey(_uuid)) {
            AccessSet.getAccessSetFromDB(AccessSet.SQL_UUID, String.valueOf(_uuid));
        }
        return cache.get(_uuid);
    }

    /**
     * @param _role Role to be cached
     */
    private static void cacheAccessSet(final AccessSet _role)
    {
        final Cache<UUID, AccessSet> cache4UUID = InfinispanCache.get().<UUID, AccessSet>getIgnReCache(
                        AccessSet.UUIDCACHE);
        cache4UUID.put(_role.getUUID(), _role);

        final Cache<String, AccessSet> nameCache = InfinispanCache.get().<String, AccessSet>getIgnReCache(
                        AccessSet.NAMECACHE);
        nameCache.put(_role.getName(), _role);

        final Cache<Long, AccessSet> idCache = InfinispanCache.get().<Long, AccessSet>getIgnReCache(
                        AccessSet.IDCACHE);
        idCache.put(_role.getId(), _role);
    }

    /**
     * Read the AccessSet from the DataBase.
     * @param _sql  SQL Statement to be executed
     * @param _criteria filter criteria
     * @return true if founr
     * @throws CacheReloadException on error
     */
    private static boolean getAccessSetFromDB(final String _sql,
                                              final Object _criteria)
        throws CacheReloadException
    {
        boolean ret = false;
        Connection con = null;
        try {
            AccessSet accessSet = null;
            con = Context.getConnection();
            PreparedStatement stmt = null;
            try {
                stmt = con.prepareStatement(_sql);
                stmt.setObject(1, _criteria);
                final ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    final long id = rs.getLong(1);
                    final String uuid = rs.getString(2);
                    final String name = rs.getString(3);
                    AccessSet.LOG.debug("read AccessSet '{}' (id = {}, uuid ={})", name, id, uuid);
                    accessSet = new AccessSet(id, uuid, name);
                }
                ret = true;
                rs.close();
            } finally {
                if (stmt != null) {
                    stmt.close();
                }
            }
            con.commit();
            if (accessSet != null) {
                accessSet.readLinks2AccessTypes();
                accessSet.readLinks2DMTypes();
                accessSet.readLinks2Status();
                accessSet.readLinks2Person();
                // needed due to cluster serialization that does not update automatically
                AccessSet.cacheAccessSet(accessSet);
            }
        } catch (final SQLException e) {
            throw new CacheReloadException("could not read roles", e);
        } catch (final EFapsException e) {
            throw new CacheReloadException("could not read roles", e);
        } finally {
            try {
                if (con != null && !con.isClosed()) {
                    con.close();
                }
            } catch (final SQLException e) {
                throw new CacheReloadException("could not read child type ids", e);
            }
        }
        return ret;
    }

    @Override
    public boolean equals(final Object _obj)
    {
        final boolean ret;
        if (_obj instanceof AccessSet) {
            ret = ((AccessSet) _obj).getId() == getId();
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
