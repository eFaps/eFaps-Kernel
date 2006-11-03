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

import org.efaps.db.Checkin;
import org.efaps.db.Checkout;
import org.efaps.db.Delete;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.db.Update;
import org.efaps.util.EFapsException;
import org.efaps.webdav.resource.AbstractResource;
import org.efaps.webdav.resource.CollectionResource;
import org.efaps.webdav.resource.SourceResource;

/**
 *
 * @author tmo
 * @version $Id$
 * @todo description
 */
public class TeamCenterWebDAVImpl implements WebDAVInterface  {
  
  /////////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used in this class.
   */
  private static final Log LOG = LogFactory.getLog(TeamCenterWebDAVImpl.class);

  
/*  getRoot(final String _name)   {
  }
  */
  
  /////////////////////////////////////////////////////////////////////////////
  // instance methods

  public List < AbstractResource > getSubs(final CollectionResource _collection)   {
    
    List < AbstractResource > subs = new ArrayList < AbstractResource > ();

    try  {
      if (_collection.getWebDAVImpl() instanceof TeamCenterWebDAVImpl)  {
        // append sub folders
        SearchQuery query = new SearchQuery();
        query.setExpand(_collection.getInstance(), 
                        "TeamCenter_Folder\\ParentFolder");
        query.addSelect("OID");
        query.addSelect("Name");
        query.addSelect("Created");
        query.addSelect("Modified");
        query.execute();
        while (query.next())  {
          String name = query.get("Name").toString();
          subs.add(new CollectionResource(
              this,
              this,
              name,
              new Instance(query.get("OID").toString()),
              (Date) query.get("Created"),
              (Date) query.get("Modified"),
              name
          ));
        }
        query.close();
    
        // append sub files
        query = new SearchQuery();
        query.setExpand(_collection.getInstance(), 
                        "TeamCenter_Document2Folder\\Folder.Document");
        query.addSelect("OID");
        query.addSelect("Name");
        query.addSelect("FileLength");
        query.addSelect("Created");
        query.addSelect("Modified");
        query.execute();
        while (query.next())  {
          String name = (String) query.get("Name");
          subs.add(new SourceResource(
              this,
              name,
              new Instance((String) query.get("OID")),
              (Date) query.get("Created"),
              (Date) query.get("Modified"),
              name,
              (Long) query.get("FileLength")
          ));
        }
        query.close();
      } else  {
        SearchQuery query = new SearchQuery();
        query.setQueryTypes("TeamCenter_RootFolder");
        query.addSelect("OID");
        query.addSelect("Name");
        query.addSelect("Modified");
        query.addSelect("Created");
        query.execute();
        while (query.next())  {
          String name = query.get("Name").toString();
          subs.add(new CollectionResource(
              this,
              this,
              name,
              new Instance(query.get("OID").toString()),
              (Date) query.get("Created"),
              (Date) query.get("Modified"),
              name
          ));
        }
        query.close();
      }
    } catch (Exception e)  {
      LOG.error("could not get subs from collection "
                + "'" + _collection.getName() + "'", e);
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
      if (_collection.getWebDAVImpl() instanceof TeamCenterWebDAVImpl)  {
        query.setQueryTypes("TeamCenter_Folder");
        query.addWhereExprEqValue("Name", _name);
        query.addWhereExprEqValue("ParentFolder", 
                                  _collection.getInstance().getId());
      } else  {
        query.setQueryTypes("TeamCenter_RootFolder");
        query.addWhereExprEqValue("Name", _name);
      }
      query.addSelect("OID");
      query.addSelect("Created");
      query.addSelect("Modified");
      query.execute();
      if (query.next())  {
        collection = new CollectionResource(
            this,
            this,
            _name,
            new Instance(query.get("OID").toString()),
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

  public boolean createCollection(final CollectionResource _collection, 
                                  final String _name)  {
    boolean ok = false;

    try  {
      Insert insert = new Insert("TeamCenter_Folder");
      insert.add("ParentFolder",  "" + _collection.getInstance().getId());
      insert.add("Name",          _name);
      insert.execute();
      insert.close();
      ok = true;
    } catch (Exception e)  {
      LOG.error("could not create collection "
                + "'" + _name + "'", e);
    }
    return ok;
  }

  /**
   * A collection is moved to a new collection with a new name. Attention! The
   * new location (new parent) could be the same parent as currently specified!
   *
   * @param _collection collection to move
   * @param _newParent  new parent collection
   * @param _newName    new name of the collection to move in the new parent
   *                    collection
   * @return <i>true</i> if the move of the collection is allowed, otherwise
   *         <i>false</i>
   */
  public boolean moveCollection(final CollectionResource _collection,
                                final CollectionResource _newParent,
                                final String _newName)  {
    boolean ok = false;
    
    try  {
      Update update = new Update(_collection.getInstance());

      if (_collection.getParent().getInstance().getId() 
                                      != _newParent.getInstance().getId())  {
        update.add("ParentFolder", "" + _newParent.getInstance().getId()); 
      }
      
      update.add("Name", _newName);
      update.execute();
      ok = true;
    } catch (Exception e)  {
      LOG.error("could not move collection "
                + "'" + _collection.getName() + "'", e);
    }
    return ok;
  }
  
  public boolean deleteCollection(final CollectionResource _collection)  {
    boolean ok = false;
    
    try  {
      List < AbstractResource > subs = getSubs(_collection);
      for (AbstractResource rsrc : subs)  {
        if (rsrc instanceof CollectionResource)  {
          deleteCollection((CollectionResource) rsrc);
        } else  {
          deleteSource((SourceResource) rsrc);
        }
      }

      Delete delete = new Delete(_collection.getInstance());
      delete.execute();
      ok = true;
    } catch (Exception e)  {
      LOG.error("could not delete collection "
                + "'" + _collection.getName() + "'", e);
    }
    return ok;
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
    
    SourceResource source = null;

    if (_collection != null)  {
      try  {
        SearchQuery query = new SearchQuery();
        query.setExpand(_collection.getInstance(), 
                        "TeamCenter_Document2Folder\\Folder.Document");
        query.addSelect("OID");
        query.addSelect("Name");
        query.execute();
        Instance instance = null;
        while (query.next())  {
          String docName = (String) query.get("Name");
          if ((docName != null) && _name.equals(docName))  {
            instance = new Instance((String) query.get("OID"));
            break;
          }
        }
        query.close();

        if (instance != null)  {
          query = new SearchQuery();
          query.setObject(instance);
          query.addSelect("FileLength");
          query.addSelect("Created");
          query.addSelect("Modified");
          query.execute();
          query.next();
          source = new SourceResource(
              this,
              _name,
              instance,
              (Date) query.get("Created"),
              (Date) query.get("Modified"),
              _name,
              (Long) query.get("FileLength")
          );
          query.close();
        }
      } catch (Exception e)  {
        LOG.error("could not get information about source '" + _name + "'", e);
      }
    }
    return source;
  }

  public boolean createSource(final CollectionResource _collection, 
                              final String _name)  {
    boolean ok = false;

    try  {
      Insert insert = new Insert("TeamCenter_Document");
      insert.add("Name", _name);
      insert.execute();
      Instance fileInstance = insert.getInstance();
      insert.close();
          
      insert = new Insert("TeamCenter_Document2Folder");
      insert.add("Document", "" + fileInstance.getId());
      insert.add("Folder",   "" + _collection.getInstance().getId());
      insert.execute();
      insert.close();

      ok = true;
    } catch (Exception e)  {
      LOG.error("could not create source "
                + "'" + _name + "'", e);
    }
    return ok;
  }

  /**
   * A source resource is moved to a new collection with a new name. Attention! 
   * The new location (new parent) could be the same parent as currently
   * specified!
   *
   * @param _collection collection to move
   * @param _newParent  new parent collection
   * @param _newName    new name of the collection to move in the new parent
   *                    collection
   * @return <i>true</i> if the move of the collection is allowed, otherwise
   *         <i>false</i>
   */
  public boolean moveSource(final SourceResource _source,
                            final CollectionResource _newParent,
                            final String _name)  {
    boolean ok = false;

    try  {
      SearchQuery query = new SearchQuery();
      query.setExpand(_source.getInstance(), 
                      "TeamCenter_Document2Folder\\Document");
      query.addSelect("OID");
      query.execute();
      while (query.next())  {
        Update update = new Update((String) query.get("OID"));
        update.add("Folder", "" + _newParent.getInstance().getId());
        update.execute();
      }
      query.close();
      
      Update update = new Update(_source.getInstance());
      update.add("Name", _name);
      update.execute();

      ok = true;
    } catch (Exception e)  {
      LOG.error("could not move source "
                + "'" + _source.getName() + "'", e);
    }
    return ok;
  }

  public boolean deleteSource(final SourceResource _source)  {
    boolean ok = false;

    try  {
      SearchQuery query = new SearchQuery();
      query.setExpand(_source.getInstance(), 
                      "TeamCenter_Document2Folder\\Document");
      query.addSelect("OID");
      query.execute();
      while (query.next())  {
        Delete delete = new Delete((String) query.get("OID"));
        delete.execute();
      }
      query.close();

      Delete delete = new Delete(_source.getInstance());
      delete.execute();

      ok = true;
    } catch (Exception e)  {
      LOG.error("could not delete source "
                + "'" + _source.getName() + "'", e);
    }
    return ok;
  }
  
  public boolean checkinSource(final SourceResource _source, 
                               final InputStream _inputStream)  {

    boolean ok = false;

    try  {
      Checkin checkin = new Checkin(_source.getInstance());
      checkin.execute(_source.getName(), _inputStream, -1);
      
      ok = true;
    } catch (EFapsException e)  {
      LOG.error("could not checkin source "
                + "'" + _source.getName() + "'", e);
    }
    return ok;
  }

  public boolean checkoutSource(final SourceResource _source, 
                                final OutputStream _outputStream)  {

    boolean ok = false;

    try  {
      Checkout checkout = new Checkout(_source.getInstance());
      checkout.preprocess();
      checkout.execute(_outputStream);
      
      ok = true;
    } catch (Exception e)  {
      LOG.error("could not checkout source "
                + "'" + _source.getName() + "'", e);
    }
    return ok;
  }
}
