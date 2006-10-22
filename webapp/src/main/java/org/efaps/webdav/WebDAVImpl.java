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
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;
//import org.efaps.util.cache.Cache;
//import org.efaps.util.cache.CacheObjectInterface;

/**
 *
 * @author tmo
 * @version $Id$
 * @todo description
 */
public class WebDAVImpl implements WebDAVInterface  {
  
  /////////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used in this class.
   */
  private static final Log LOG = LogFactory.getLog(WebDAVImpl.class);

  /////////////////////////////////////////////////////////////////////////////
  // instance variables

  /** All known integrations. */
//  private final 

  /////////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * @todo hard coded webdav impl
   */
  public List < AbstractResource > getSubs(final CollectionResource _collection)   {
    
    List < AbstractResource > subs = new ArrayList < AbstractResource > ();

    try  {
      SearchQuery query = new SearchQuery();
      query.setQueryTypes("Admin_Integration_WebDAV");
      query.addSelect("OID");
      query.addSelect("Path");
      query.addSelect("Modified");
      query.addSelect("Created");
      query.execute();
      while (query.next())  {
        String path = query.get("Path").toString();
        subs.add(new CollectionResource(
            this,
            new TeamCenterWebDAVImpl(),
            path,
            new Instance((String) query.get("OID")),
            (Date) query.get("Created"),
            (Date) query.get("Modified"),
            path
        ));
      }
      query.close();
    } catch (Exception e)  {
      LOG.error("could not get all WebDAV integrations", e);
    }

    return subs;
  }


  /**
   * @param _collection collection resource representing the folder (if null,
   *                    means root folder)
   * @param _name       name of the searched collection resource
   * @return found collection resource for given instance or null if not found.
   * @todo use EFapsException instead of Exception
   */
  public CollectionResource getCollection(final CollectionResource _collection,
                                          final String _name)  {
    
    CollectionResource collection = null;
    
    try  {
      SearchQuery query = new SearchQuery();
      query.setQueryTypes("Admin_Integration_WebDAV");
      query.addWhereExprEqValue("Path", _name);
      query.addSelect("OID");
      query.addSelect("Created");
      query.addSelect("Modified");
      query.execute();
      if (query.next())  {
        collection = new CollectionResource(
            this,
            new TeamCenterWebDAVImpl(),
            _name,
            new Instance((String) query.get("OID")),
            (Date) query.get("Created"),
            (Date) query.get("Modified"),
            _name
        );
      }
      query.close();
    } catch (Exception e)  {
      LOG.error("could not get information about collection "
                + "'" + _name + "'", e);
    }
    return collection;
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
}
