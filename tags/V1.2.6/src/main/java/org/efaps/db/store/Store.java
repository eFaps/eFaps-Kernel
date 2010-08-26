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

package org.efaps.db.store;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.efaps.admin.AbstractAdminObject;
import org.efaps.admin.datamodel.Type;
import org.efaps.ci.CIAdminCommon;
import org.efaps.ci.CIDB;
import org.efaps.db.Instance;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.db.store.Resource.Compress;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.Cache;
import org.efaps.util.cache.CacheReloadException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public final class Store
    extends AbstractAdminObject
{

    /**
     * Property name of the file name attribute used in store actions (checkin,
     * checkout).
     */
    public static final String PROPERTY_ATTR_FILE_NAME = "StoreAttributeFileName";

    /**
     * Property name of the file length attribute used in store actions
     * (checkin).
     */
    public static final String PROPERTY_ATTR_FILE_LENGTH = "StoreAttributeFileLength";

    /**
     * Property to get the compress for this store.
     */
    public static final String PROPERTY_COMPRESS = "StoreCompress";

    /**
     * Property name to get the JDNI Name.
     */
    public static final String PROPERTY_JNDINAME = "StoreJNDIName";

    /**
     * Cache for Stores.
     */
    private static final StoreCache CACHE = new StoreCache();

    /**
     * Name of the Resource class of this store.
     */
    private String resource;

    /**
     * Properties for the StoreResource.
     */
    private Map<String, String> resourceProperties;

    /**
     * FileSystemManager of this store.
     */
    private DefaultFileSystemManager fileSytemManager;

    /**
     * @param _id id of this store
     * @param _uuid uuid of this store
     * @param _name name of thi store
     */
    private Store(final long _id,
                  final String _uuid,
                  final String _name)
    {
        super(_id, _uuid, _name);
    }

    /**
     * Sets the link properties for this object.
     *
     * @param _linkType type of the link property
     * @param _toId to id
     * @param _toType to type
     * @param _toName to name
     * @throws EFapsException error
     */
    @Override
    protected void setLinkProperty(final Type _linkType,
                                   final long _toId,
                                   final Type _toType,
                                   final String _toName)
        throws EFapsException
    {
        if (_linkType.isKindOf(CIDB.Store2Resource.getType())) {
            this.resource = _toName;
            this.resourceProperties = getProperties(_toId);
        } else {
            super.setLinkProperty(_linkType, _toId, _toType, _toName);
        }
    }

    /**
     * Method to get the properties for the resource.
     *
     * @param _id id of the resource
     * @return Properties of the resource
     * @throws EFapsException on error
     */
    private Map<String, String> getProperties(final long _id)
        throws EFapsException
    {
        final Map<String, String> ret = new HashMap<String, String>();
        final QueryBuilder queryBldr = new QueryBuilder(CIAdminCommon.Property);
        queryBldr.addWhereAttrEqValue(CIAdminCommon.Property.Abstract, _id);
        final MultiPrintQuery multi = queryBldr.getPrint();
        multi.addAttribute(CIAdminCommon.Property.Name, CIAdminCommon.Property.Value);
        multi.executeWithoutAccessCheck();
        while (multi.next()) {
            ret.put(multi.<String> getAttribute(CIAdminCommon.Property.Name),
                            multi.<String> getAttribute(CIAdminCommon.Property.Value));
        }
        return ret;
    }

    /**
     * Method to get a instance of the resource.
     *
     * @param _instance instance the resource is wanted for
     * @return Resource
     * @throws EFapsException on error
     */
    public Resource getResource(final Instance _instance)
        throws EFapsException
    {
        Resource ret = null;
        try {
            ret = (Resource) (Class.forName(this.resource).newInstance());
            Compress compress;
            if (this.resourceProperties.containsKey(Store.PROPERTY_COMPRESS)) {
                compress = Compress.valueOf(this.resourceProperties.get(Store.PROPERTY_COMPRESS).toUpperCase());
            } else {
                compress = Compress.NONE;
            }
            ret.initialize(_instance, this.resourceProperties, compress);

            if (ret.isVFS()) {
                DefaultFileSystemManager tmpMan = null;
                if (getProperty(Store.PROPERTY_JNDINAME) != null) {
                    final InitialContext initialContext = new InitialContext();
                    final Context context = (Context) initialContext.lookup("java:comp/env");
                    final NamingEnumeration<NameClassPair> nameEnum = context.list("");
                    while (nameEnum.hasMoreElements()) {
                        final NameClassPair namePair = nameEnum.next();
                        if (namePair.getName().equals(getProperty(Store.PROPERTY_JNDINAME))) {
                            tmpMan = (DefaultFileSystemManager) context.lookup(getProperty(Store.PROPERTY_JNDINAME));
                            break;
                        }
                    }
                }
                if (tmpMan == null && this.fileSytemManager == null) {
                    this.fileSytemManager = ret.evaluateFileSystemManager();
                    tmpMan = this.fileSytemManager;
                } else {
                    tmpMan = this.fileSytemManager;
                }
                ret.setFileSystemManager(tmpMan);
            }
        } catch (final InstantiationException e) {
            throw new EFapsException(Store.class, "getResource.InstantiationException", e, this.resource);
        } catch (final IllegalAccessException e) {
            throw new EFapsException(Store.class, "getResource.IllegalAccessException", e, this.resource);
        } catch (final ClassNotFoundException e) {
            throw new EFapsException(Store.class, "getResource.ClassNotFoundException", e, this.resource);
        } catch (final NamingException e) {
            throw new EFapsException(Store.class, "getResource.NamingException", e, this.resource,
                            getProperty(Store.PROPERTY_JNDINAME));
        }
        return ret;
    }

    /**
     * Method to initialize the Cache of this CacheObjectInterface.
     *
     * @throws CacheReloadException on error during loading of cache
     */
    public static void initialize()
        throws CacheReloadException
    {
        // check is need to work on first installation
        if (CIDB.Store.getType() != null) {
            Store.CACHE.initialize(Store.class);
            for (final Store store : Store.CACHE.getCache4Id().values()) {
                store.readFromDB4Properties();
                store.readFromDB4Links();
            }
        }
    }

    /**
     * Returns for given parameter <i>_id</i> the instance of class
     * {@link Store}.
     *
     * @param _id id of the type to get
     * @return instance of class {@link Store}
     * @throws CacheReloadException
     */
    public static Store get(final long _id)
    {
        return Store.CACHE.get(_id);
    }

    /**
     * Returns for given parameter <i>_name</i> the instance of class
     * {@link Store}.
     *
     * @param _name name of the type to get
     * @return instance of class {@link Store}
     * @throws CacheReloadException
     */
    public static Store get(final String _name)
    {
        return Store.CACHE.get(_name);
    }

    /**
     * Returns for given parameter <i>_uuid</i> the instance of class
     * {@link Store}.
     *
     * @param _uuid uuid of the type to get
     * @return instance of class {@link Store}
     * @throws CacheReloadException
     */
    public static Store get(final UUID _uuid)
    {
        return Store.CACHE.get(_uuid);
    }

    /**
     * Cache for Stores.
     */
    public static class StoreCache
        extends Cache<Store>
    {

        /**
         * Method to fill this cache with objects.
         *
         * @param _cache4Id Cache for id
         * @param _cache4Name Cache for name
         * @param _cache4UUID Cache for UUID
         * @throws CacheReloadException on error during reading
         */
        @Override
        protected void readCache(final Map<Long, Store> _cache4Id,
                                 final Map<String, Store> _cache4Name,
                                 final Map<UUID, Store> _cache4UUID)
            throws CacheReloadException
        {
            try {
                final QueryBuilder queryBldr = new QueryBuilder(CIDB.Store);
                final MultiPrintQuery multi = queryBldr.getPrint();
                multi.addAttribute(CIDB.Store.Name, CIDB.Store.ID, CIDB.Store.UUID);
                multi.executeWithoutAccessCheck();
                while (multi.next()) {
                    final long id = multi.<Long> getAttribute(CIDB.Store.ID);
                    final String name = multi.<String> getAttribute(CIDB.Store.Name);
                    final String uuid = multi.<String> getAttribute(CIDB.Store.UUID);
                    final Store store = new Store(id, uuid, name);
                    _cache4Id.put(store.getId(), store);
                    _cache4Name.put(store.getName(), store);
                    _cache4UUID.put(store.getUUID(), store);
                }
            } catch (final EFapsException e) {
                throw new CacheReloadException("Could not read Store.", e);
            }
        }
    }
}