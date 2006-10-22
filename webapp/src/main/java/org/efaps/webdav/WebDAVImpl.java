/*
 * Copyright 2006 The eFaps Team
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

package org.efaps.webdav;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.Cache;
import org.efaps.util.cache.CacheObjectInterface;
import org.efaps.util.cache.CacheReloadException;
import org.efaps.util.cache.CacheReloadInterface;

/**
 *
 * @author tmo
 * @version $Id$
 * @todo description
 */
public class WebDAVImpl implements WebDAVInterface, CacheReloadInterface  {
  
  /////////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used in this class.
   */
  private static final Log LOG = LogFactory.getLog(WebDAVImpl.class);

  /////////////////////////////////////////////////////////////////////////////
  // instance variables

  /** All known integrations. */
  private final RootCollectionResourceCache cache 
                                = new RootCollectionResourceCache(this);

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
   * Load all WebDAV integrations into the cache.
   *
   * @see #cache
   * @see #getWebDAVImpl
   */
  public void reloadCache() throws CacheReloadException  {
    try  {
      SearchQuery query = new SearchQuery();
      query.setQueryTypes("Admin_Integration_WebDAV");
      query.addSelect("OID");
      query.addSelect("UUID");
      query.addSelect("Name");
      query.addSelect("Path");
      query.addSelect("Modified");
      query.addSelect("Created");
      query.executeWithoutAccessCheck();
      while (query.next())  {
        String path = (String) query.get("Path");
        String name = (String) query.get("Name");
        Instance instance = new Instance((String) query.get("OID"));
        WebDAVInterface webDavImpl = getWebDAVImpl(instance, name);
        if (webDavImpl == null)  {
          LOG.error("could not initialise WebDAV implementation for "
                    + "'" + name + "'");
        } else  {
          this.cache.add(new RootCollectionResource(
              this,
              webDavImpl,
              UUID.fromString((String) query.get("UUID")),
              path,
              instance,
              (Date) query.get("Created"),
              (Date) query.get("Modified")
          ));
        }
      }
      query.close();
    } catch (EFapsException e)  {
      throw new CacheReloadException("could not get all WebDAV integrations", 
                                     e);
    }
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
      SearchQuery query = new SearchQuery();
      query.setExpand(_instance, "Admin_Property\\Abstract");
      query.addSelect("Name");
      query.addSelect("Value");
      query.executeWithoutAccessCheck();
      while (query.next())  {
        String name = (String) query.get("Name");
        String value = (String) query.get("Value");
        if ("Class".equals(name))  {
          Object obj = Class.forName(value).newInstance();
          if (obj instanceof WebDAVInterface)  {
            ret = (WebDAVInterface) obj;
          } else  {
            LOG.error("class " + value + " does not implement interface "
                      + WebDAVInterface.class);
          }
        }
      }
      query.close();
    } catch (EFapsException e)  {
      LOG.error("could not get properties for " + _name, e);
    } catch (ClassNotFoundException e)  {
      LOG.error("could not found WebDAV implementation class for " 
                + _name, e);
    } catch (InstantiationException e)  {
      LOG.error("could not instantiate implementation class for " 
                + _name, e);
    } catch (IllegalAccessException e)  {
      LOG.error("could not access implementation class for " 
                + _name, e);
    }
    
    return ret;
  }

  /**
   * @see #reloadCache
   */
  public List < AbstractResource > getSubs(final CollectionResource _collection)   {
    if (!this.cache.hasEntries())  {
      try  {
        reloadCache();
      } catch (CacheReloadException e)  {
        LOG.error("could not get all WebDAV integrations", e);
      }
    }
    return new ArrayList < AbstractResource > (this.cache.getResources());
  }


  /**
   * @param _collection collection resource representing the folder (if null,
   *                    means root folder)
   * @param _name       name of the searched collection resource
   * @return found collection resource for given instance or null if not found.
   * @see #reloadCache
   */
  public CollectionResource getCollection(final CollectionResource _collection,
                                          final String _name)  {
    if (!this.cache.hasEntries())  {
      try  {
        reloadCache();
      } catch (CacheReloadException e)  {
        LOG.error("could not get all WebDAV integrations", e);
      }
    }
    return this.cache.get(_name);
  }

  /**
   * @param _collection collection resource representing the folder (if null,
   *                    means root folder)
   * @param _name       name of the searched source resource
   * @return found source resource for given instance or null if not found.
   * @todo use EFapsException instead of Exception
   */
  public SourceResource getSource(final CollectionResource _collection,
                                  final String _name)  {
    
    return null;
  }


  public boolean deleteCollection(final CollectionResource _collection)  {
    return false;
  }
  
  public boolean deleteSource(final SourceResource _source)  {
    return false;
  }

  /**
   * New WebDAV integrations are not allowed to create within the WebDAV 
   * integration.
   *
   * @return always <i>false</i>
   */
  public boolean createCollection(final CollectionResource _collection, 
                                  final String _name)  {
    return false;
  }

  /**
   * New files are not allowed to create within the WebDAV integration.
   *
   * @return always <i>false</i>
   */
  public boolean createSource(final CollectionResource _collection, 
                              final String _name)  {
    return false;
  }

  
  public boolean checkinSource(final SourceResource _source, 
                               final InputStream _inputStream)  {
    return false;
  }

  public boolean checkoutSource(final SourceResource _source, 
                                final OutputStream _outputStream)  {
    return false;
  }

  /////////////////////////////////////////////////////////////////////////////
  
  /**
   *
   */
  private class RootCollectionResourceCache 
                                  extends Cache < RootCollectionResource >  {
    
    RootCollectionResourceCache(WebDAVImpl _webDAVImpl)  {
      super(_webDAVImpl);
    }
    
    public Collection < RootCollectionResource > getResources()  {
      return getCache4Name().values();
    }
  }

  /**
   */
  private class RootCollectionResource extends CollectionResource 
                                       implements CacheObjectInterface  {

    /** UUID for this Root Collection Resource. */
    private final UUID uuid;
    
    RootCollectionResource(final WebDAVInterface _webDAVImpl,
                           final WebDAVInterface _subWebDAVImpl,
                           final UUID _uuid,
                           final String _path,
                           final Instance _instance,
                           final Date _created,
                           final Date _modified)  {
      super(_webDAVImpl, _subWebDAVImpl, _path, _instance,
            _created, _modified, _path);
      this.uuid = _uuid;
    }
    
    /**
     * Returns the id of the instance.
     *
     * @return id of the instance
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
