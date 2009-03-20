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

package org.efaps.ui.webdav;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.efaps.admin.program.esjp.EFapsClassLoader;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.ui.webdav.resource.AbstractResource;
import org.efaps.ui.webdav.resource.CollectionResource;
import org.efaps.ui.webdav.resource.SourceResource;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.Cache;
import org.efaps.util.cache.CacheObjectInterface;
import org.efaps.util.cache.CacheReloadException;

/**
 * The class is used as gateway to all WebDAV integrations. Each implementation
 * of a WebDAV integration is cached as {@link #RootCollectionResource}
 * object in {@link #cache}.
 *
 * @author tmo
 * @version $Id$
 */
public class WebDAVImpl implements WebDAVInterface  {

  /////////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used in this class.
   */
  private static final Logger LOG = LoggerFactory.getLogger(WebDAVImpl.class);

  /////////////////////////////////////////////////////////////////////////////
  // instance variables

  /** All known WebDAV integrations. */
  private final RootCollectionResourceCache CACHE
                                = new RootCollectionResourceCache();

  /////////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * Return the priority (order number) in which this cache reload
   * implementation must be reloaded. Because the WebDAV has very low priority,
   * the highest number is returned.
   *
   * @return always {@link Integer.MAX_VALUE}
   */
  public int priority()  {
    return Integer.MAX_VALUE;
  }

  /**
   * The class queries for a property with the name <i>Class</i>. The value is
   * the class implementing the WebDAV implementation for given WebDAV
   * integration. The found class is instantiated and the new created instance
   * is returned.<br/>
   * If the found class is not found, could not be instaniated or does not
   * implement interface {@link WebDAVInterface}, a <code>null</code> is
   * returned.
   *
   * @param _instance instance of the WebDAV integration
   * @param _name     name of the WebDAV integration
   * @return instance of the WebDAV implementation (if found), otherwise
   *         <code>null</code> is returned
   */
  private WebDAVInterface getWebDAVImpl(final Instance _instance,
                                        final String _name)  {
    WebDAVInterface ret = null;

    try  {
      final SearchQuery query = new SearchQuery();
      query.setExpand(_instance, "Admin_Common_Property\\Abstract");
      query.addSelect("Name");
      query.addSelect("Value");
      query.executeWithoutAccessCheck();
      while (query.next())  {
        final String name = (String) query.get("Name");
        final String value = (String) query.get("Value");
        if ("Class".equals(name))  {
          final Object obj = Class.forName(value,
                                     true,
                                     new EFapsClassLoader(this.getClass().getClassLoader()))
                            .newInstance();

          if (obj instanceof WebDAVInterface)  {
            ret = (WebDAVInterface) obj;
          } else  {
            LOG.error("class " + value + " does not implement interface "
                      + WebDAVInterface.class);
          }
        }
      }
      query.close();
    } catch (final EFapsException e)  {
      LOG.error("could not get properties for " + _name, e);
    } catch (final ClassNotFoundException e)  {
      LOG.error("could not found WebDAV implementation class for "
                + _name, e);
    } catch (final InstantiationException e)  {
      LOG.error("could not instantiate implementation class for "
                + _name, e);
    } catch (final IllegalAccessException e)  {
      LOG.error("could not access implementation class for "
                + _name, e);
    }

    return ret;
  }

  /////////////////////////////////////////////////////////////////////////////
  // collection resource instance methods

  /**
   * First, it is tested, if all WebDAV integrations are loaded into the cache.
   * If not, method {@link #reloadCache} is used to initialise the cache. Then
   * all cached WebDAV integration are returned as list.
   *
   * @param _col collection resource for which the sub collections are
   *             searched, here ignored (because this WebDAV integration is the
   *             root!)
   * @return all WebDAV integration
   * @see #cache
   * @see #reloadCache
   * @see #RootCollectionResourceCache.getResources
   */
  public List < AbstractResource > getSubs(final CollectionResource _col)   {
    return new ArrayList < AbstractResource > (this.CACHE.getResources());
  }


  /**
   * First, it is tested, if all WebDAV integrations are loaded into the cache.
   * If not, method {@link #reloadCache} is used to initialise the cache. Then
   * the cache is used to found the root collection resource with the given
   * name.
   *
   * @param _col  collection resource for which the collections is
   *              searched, here ignored (because this WebDAV integration is
   *              the root!)
   * @param _name name of the searched collection resource
   * @return found collection resource for given instance or null if not found.
   * @see #cache
   * @see #reloadCache
   */
  public CollectionResource getCollection(final CollectionResource _col,
                                          final String _name)  {
    return this.CACHE.get(_name);
  }

  /**
   * New WebDAV integrations are never allowed to create within the WebDAV
   * integration itself.
   *
   * @return always <i>false</i>
   */
  public boolean createCollection(final CollectionResource _collection,
                                  final String _name)  {
    return false;
  }

  /**
   * A rename / move of a WebDAV integration is never allowed.
   *
   * @return always <i>false</i>
   */
  public boolean moveCollection(final CollectionResource _collection,
                                final CollectionResource _newParent,
                                final String _newName)  {
    return false;
  }

  /**
   * A copy of a WebDAV integration is never allowed.
   *
   * @return always <i>false</i>
   */
  public boolean copyCollection(final CollectionResource _collection,
                                final CollectionResource _newParent,
                                final String _newName)  {
    return false;
  }

  /**
   * WebDAV integrations are not allowed to delete within the WebDAV
   * integration itself.
   *
   * @return always <i>false</i>
   */
  public boolean deleteCollection(final CollectionResource _collection)  {
    return false;
  }

  /////////////////////////////////////////////////////////////////////////////
  // source resource instance methods

  /**
   * Because no files exists within the WebDAV integration itself, a source
   * could not be found.
   *
   * @return always <code>null</code>
   */
  public SourceResource getSource(final CollectionResource _collection,
                                  final String _name)  {
    return null;
  }

  /**
   * New files are not allowed to create within the WebDAV integration itself.
   *
   * @return always <i>false</i>
   */
  public boolean createSource(final CollectionResource _collection,
                              final String _name)  {
    return false;
  }

  /**
   * Because no files exists within the WebDAV integration itself, a move of
   * a source is not working.
   *
   * @return always <i>false</i>
   */
  public boolean moveSource(final SourceResource _source,
                            final CollectionResource _newParent,
                            final String _newName)  {
    return false;
  }

  /**
   * Because no files exists within the WebDAV integration itself, a copy of
   * a source is not working.
   *
   * @return always <i>false</i>
   */
  public boolean copySource(final SourceResource _source,
                            final CollectionResource _newParent,
                            final String _newName)  {
    return false;
  }

  /**
   * Because no files exists within the WebDAV integration itself, a delete
   * of a source is not working.
   *
   * @return always <i>false</i>
   */
  public boolean deleteSource(final SourceResource _source)  {
    return false;
  }

  /**
   * Because no files exists within the WebDAV integration itself, a checkin
   * to a source is not working.
   *
   * @return always <i>false</i>
   */
  public boolean checkinSource(final SourceResource _source,
                               final InputStream _inputStream)  {
    return false;
  }

  /**
   * Because no files exists within the WebDAV integration itself, a checkout
   * of a source is not working.
   *
   * @return always <i>false</i>
   */
  public boolean checkoutSource(final SourceResource _source,
                                final OutputStream _outputStream)  {
    return false;
  }

  /////////////////////////////////////////////////////////////////////////////

  /**
   * The class is used to cache all root collection resources.
   */
  private class RootCollectionResourceCache
                                  extends Cache<RootCollectionResource>  {



    /**
     *
     * @return all cached root collection resources
     */
    public Collection <RootCollectionResource> getResources()  {
      return getCache4Name().values();
    }



    /* (non-Javadoc)
     * @see org.efaps.util.cache.Cache#readCache(java.util.Map, java.util.Map, java.util.Map)
     */
    @Override
    protected void readCache(final Map<Long, RootCollectionResource> cache4Id,
        final Map<String, RootCollectionResource> cache4Name,
        final Map<UUID, RootCollectionResource> cache4UUID)
        throws CacheReloadException {
      try  {
        final SearchQuery query = new SearchQuery();
        query.setQueryTypes("Admin_Integration_WebDAV");
        query.addSelect("OID");
        query.addSelect("UUID");
        query.addSelect("Name");
        query.addSelect("Path");
        query.addSelect("Modified");
        query.addSelect("Created");
        query.executeWithoutAccessCheck();
        while (query.next())  {
          final String path = (String) query.get("Path");
          final String name = (String) query.get("Name");
          final Instance instance = Instance.get((String) query.get("OID"));
          final WebDAVInterface webDavImpl = getWebDAVImpl(instance, name);
          if (webDavImpl == null)  {
            LOG.error("could not initialise WebDAV implementation for "
                      + "'" + name + "'");
          } else  {
            final RootCollectionResource root = new RootCollectionResource(
                WebDAVImpl.this,
                webDavImpl,
                name,
                UUID.fromString((String) query.get("UUID")),
                path,
                instance,
                (Date) query.get("Created"),
                (Date) query.get("Modified"));
            cache4Name.put(root.getName(), root);
          }
        }
        query.close();
      } catch (final EFapsException e)  {
        throw new CacheReloadException("could not get all WebDAV integrations",
                                       e);
      }

    }
  }

  /**
   * The represents one implementation of a WebDAV integration. Because the
   * universal unique identifier is stored within this collection resource
   * implementation, a new class is created and derived from
   * {@link CollectionResource}.<br/>
   * Because the class also implements interface {@link CacheObjectInterface},
   * the method {@link #getId} and {@link #getUUID} are defined. The needed
   * method {@link CacheObjectInterface#getName} is defined in class
   * {@link CollectionResource} which used the path as name.
   */
  private class RootCollectionResource extends CollectionResource
                                       implements CacheObjectInterface  {

    /** UUID for this Root Collection Resource. */
    private final UUID uuid;

    /**
     * The constructor is used to create a new Java instance of one WebDAV
     * integration implentation. The display name of this collection is the
     * path and in braces behind the name together with the unique universal
     * identifier.
     *
     * @param _webDAVImpl     Java instance of this {@link @WebDAVImpl} class
     * @param _subWebDAVImpl  Java instance of the WebDAV integration
     *                        implementation
     * @param _name           name of the WebDAV integration implementation
     * @param _uuid           universal unique identifier of the WebDAV
     *                        integration implementation
     * @param _path           path of the WebDAV integration implementation
     * @param _instance       eFaps instance of the WebDAV integration
     *                        implementation
     * @param _created        creation date of the WebDAV integration
     *                        implementation
     * @param _modified       last modification dat of the WebDAV integration
     *                        implementation
     * @see #reloadCache
     */
    RootCollectionResource(final WebDAVInterface _webDAVImpl,
                           final WebDAVInterface _subWebDAVImpl,
                           final String _name,
                           final UUID _uuid,
                           final String _path,
                           final Instance _instance,
                           final Date _created,
                           final Date _modified)  {
      super(_webDAVImpl, _subWebDAVImpl, _path, _instance, _created, _modified,
            _path + " (Name: " + _name + ", UUID: " + _uuid.toString() + ")");
      this.uuid = _uuid;
    }

    /**
     * Returns the id of the {@link AbstractResource#instance}.
     *
     * @return id of the instance
     * @see AbstractResource#instance
     */
    public long getId()  {
      return getInstance().getId();
    }

    /**
     * This is the getter method for instance variable {@link #uuid}.
     *
     * @return value of instance variable {@link #uuid}
     * @see #uuid
     */
    public UUID getUUID()  {
      return this.uuid;
    }
  }
}
