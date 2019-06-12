/*
 * Copyright 2003 - 2019 The eFaps Team
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

package org.efaps.admin.common;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.efaps.admin.datamodel.Type;
import org.efaps.ci.CIAdminCommon;
import org.efaps.db.Context;
import org.efaps.db.InstanceQuery;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.db.wrapper.SQLPart;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheLogListener;
import org.efaps.util.cache.CacheObjectInterface;
import org.efaps.util.cache.CacheReloadException;
import org.efaps.util.cache.InfinispanCache;
import org.infinispan.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Association
    implements CacheObjectInterface, Serializable
{

    /** */
    private static final long serialVersionUID = 1L;

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Association.class);

    /**
     * Name of the Cache by UUID.
     */
    private static final String UUIDCACHE = Association.class.getName() + ".UUID";

    /**
     * Name of the Cache by ID.
     */
    private static final String IDCACHE = Association.class.getName() + ".ID";

    /**
     * Name of the Cache by Name.
     */
    private static final String NAMECACHE = Association.class.getName() + ".Name";

    /**
     * Name of the Cache by UUID.
     */
    private static final String KEYCACHE = Association.class.getName() + ".Key";

    private static final String SQL = new SQLSelect()
                    .column(0, "ID")
                    .from("T_CMASSOC", 0)
                    .leftJoin("T_CMASSOCDEF", 1, "ASSOCID", 0, "ID")
                    .leftJoin("T_CMASSOCMAP", 2, "ASSOCID", 0, "ID")
                    .addPart(SQLPart.WHERE)
                        .addColumnPart(1, "COMPANYID").addPart(SQLPart.EQUAL).addValuePart("?")
                        .addPart(SQLPart.AND)
                        .addColumnPart(2, "TYPEID").addPart(SQLPart.EQUAL).addValuePart("?")
                        .toString();

    /**
     * The instance variable stores the id of this Association.
     */
    private final long id;

    /**
     * The instance variable stores the UUID of this Association.
     */
    private final UUID uuid;

    /**
     * The instance variable stores the Name of this Association.
     */
    private final String name;

    private Association(final long _id,
                        final String _name,
                        final String _uuid)
    {
        id = _id;
        name = _name;
        uuid = UUID.fromString(_uuid);
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public UUID getUUID()
    {
        return uuid;
    }

    @Override
    public long getId()
    {
        return id;
    }

    /**
     * Method to initialize the {@link #CACHE cache} for the Associations.
     */
    public static void initialize()
    {
        if (InfinispanCache.get().exists(Association.UUIDCACHE)) {
            InfinispanCache.get().<UUID, Association>getCache(Association.UUIDCACHE).clear();
        } else {
            InfinispanCache.get().<UUID, Association>getCache(Association.UUIDCACHE)
                            .addListener(new CacheLogListener(Association.LOG));
        }
        if (InfinispanCache.get().exists(Association.IDCACHE)) {
            InfinispanCache.get().<Long, Association>getCache(Association.IDCACHE).clear();
        } else {
            InfinispanCache.get().<Long, Association>getCache(Association.IDCACHE)
                            .addListener(new CacheLogListener(Association.LOG));
        }
        if (InfinispanCache.get().exists(Association.NAMECACHE)) {
            InfinispanCache.get().<String, Association>getCache(Association.NAMECACHE).clear();
        } else {
            InfinispanCache.get().<String, Association>getCache(Association.NAMECACHE)
                            .addListener(new CacheLogListener(Association.LOG));
        }
    }

    public static Association get(final long _id)
        throws EFapsException
    {
        final Cache<Long, Association> cache = InfinispanCache.get().<Long, Association>getCache(Association.IDCACHE);
        if (!cache.containsKey(_id)) {
            Association.loadAssociation(_id);
        }
        return cache.get(_id);
    }

    private static void loadAssociation(final long _id)
        throws EFapsException
    {
        final QueryBuilder queryBldr = new QueryBuilder(CIAdminCommon.AssociationAbstract);
        queryBldr.addWhereAttrEqValue(CIAdminCommon.AssociationAbstract.ID, _id);
        final MultiPrintQuery multi = queryBldr.getPrint();
        multi.addAttribute(CIAdminCommon.AssociationAbstract.Name, CIAdminCommon.AssociationAbstract.UUID);
        multi.executeWithoutAccessCheck();
        if (multi.next()) {
            final String name = multi.getAttribute(CIAdminCommon.AssociationAbstract.Name);
            final String uuid = multi.getAttribute(CIAdminCommon.AssociationAbstract.UUID);
            final Association association = new Association(_id, name, uuid);
            cacheAssociation(association);
        } else {
            // TODO error
        }
    }

    private static void cacheAssociation(final Association _association)
    {
        final Cache<UUID, Association> cache4UUID = InfinispanCache.get().<UUID, Association>getIgnReCache(
                        Association.UUIDCACHE);
        cache4UUID.putIfAbsent(_association.getUUID(), _association);

        final Cache<String, Association> nameCache = InfinispanCache.get().<String, Association>getIgnReCache(
                        Association.NAMECACHE);
        nameCache.putIfAbsent(_association.getName(), _association);

        final Cache<Long, Association> idCache = InfinispanCache.get().<Long, Association>getIgnReCache(
                        Association.IDCACHE);
        idCache.putIfAbsent(_association.getId(), _association);
    }

    public static Association evaluate(final Type _type)
        throws EFapsException
    {
        final Long companyId = Context.getThreadContext().getCompany().getId();
        final Long typeId = _type.getId();
        final AssociationKey key = AssociationKey.get(companyId, typeId);

        final Cache<AssociationKey, Long> cache = InfinispanCache.get().<AssociationKey, Long>getCache(Association.KEYCACHE);
        if (!cache.containsKey(key)) {
            load(companyId, _type);
        }
        final Long associationId = cache.get(key);
        return Association.get(associationId);
    }

    private static void load(final long _companyId,
                             final Type _type)
        throws EFapsException
    {
        final Set<Long> typeIds = new HashSet<>();
        Long assocId = null;
        Type currentType = _type;
        while (assocId == null && currentType.getParentType() != null) {
            assocId = loadFromDB(_companyId, currentType.getId());
            typeIds.add(currentType.getId());
            currentType = currentType.getParentType();
        }
        if (assocId == null) {
            final Cache<AssociationKey, Long> cache = InfinispanCache.get().<AssociationKey, Long>getCache(Association.KEYCACHE);
            assocId = loadDefault(_companyId);
            for (final Long typeId: typeIds) {
                cache.put(AssociationKey.get(_companyId, typeId), assocId);
            }
        } else {
            final Cache<AssociationKey, Long> cache = InfinispanCache.get().<AssociationKey, Long>getCache(Association.KEYCACHE);
            for (final Long typeId: typeIds) {
                cache.put(AssociationKey.get(_companyId, typeId), assocId);
            }
        }
    }

    private static Long loadDefault(final long _companyId)
        throws EFapsException
    {
        Long ret = null;
        final QueryBuilder attrQueryBldr = new QueryBuilder(CIAdminCommon.AssociationDefinition);
        attrQueryBldr.addWhereAttrEqValue(CIAdminCommon.AssociationDefinition.CompanyLink, _companyId);

        final QueryBuilder queryBldr = new QueryBuilder(CIAdminCommon.AssociationDefault);
        queryBldr.addWhereAttrInQuery(CIAdminCommon.AssociationDefault.ID,
                        attrQueryBldr.getAttributeQuery(CIAdminCommon.AssociationDefinition.AssociationLink));

        final InstanceQuery query = queryBldr.getQuery();
        query.executeWithoutAccessCheck();
        if (query.next()) {
            ret = query.getCurrentValue().getId();
        } else {
            // TODO error
        }
        return ret;
    }

    private static Long loadFromDB(final long _companyId,
                                   final long _typeid)
        throws EFapsException
    {
        Long ret = null;
        Connection con = null;
        try {
            con = Context.getConnection();
            PreparedStatement stmt = null;
            try {
                stmt = con.prepareStatement(SQL);
                stmt.setObject(1, _companyId);
                stmt.setObject(2, _typeid);
                final ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    ret = rs.getLong(1);
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
}
