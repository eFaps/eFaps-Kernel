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

package org.efaps.admin.access;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.efaps.admin.AbstractAdminObject;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.Context;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.Cache;
import org.efaps.util.cache.CacheReloadException;

/**
 * @author tmo
 * @version $Id$
 * @todo description
 */
public class AccessSet extends AbstractAdminObject  {

  /////////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used in this class.
   */
  private static final Logger LOG = LoggerFactory.getLogger(AccessSet.class);

  /**
   * This is the sql select statement to select all access types from the
   * database.
   *
   * @see #init4ReadAllAccessSets
   */
  private static final String SQL_SELECT  = "select "
                                                + "ID,"
                                                + "UUID,"
                                                + "NAME "
                                              + "from T_ACCESSSET";

  /**
   * This is the sql select statement to select the links from all access sets
   * to all access types in the database.
   *
   * @see #init4ReadLinks2AccessTypes
   */
  private static final String SQL_SET2TYPE = "select "
                                                + "ACCESSSET,"
                                                + "ACCESSTYPE "
                                              + "from T_ACCESSSET2TYPE";

  /**
   * This is the sql select statement to select the links from all access sets
   * to all data model types in the database.
   *
   * @see #init4ReadLinks2DMTypes
   */
  private static final String SQL_SET2DMTYPE = "select "
                                                  + "ACCESSSET,"
                                                  + "DMTYPE "
                                                + "from T_ACCESSSET2DMTYPE";

  /**
   * All related access types of this access set are referenced in this
   * instance variable.
   *
   * @see #getAccessTypes
   */
  private final Set<AccessType> accessTypes = new HashSet <AccessType>();

  /**
   * All related data models types of this access set are referenced in this
   * instance variable.
   *
   * @see #getDataModelTypes
   */
  private final Set < Type > dataModelTypes = new HashSet < Type > ();

  private static final AccessSetCache CACHE = new AccessSetCache();

  /**
   * This is the constructor.
   *
   * @param _id     id of this access type
   * @param _uuid   universal unique identifier of this access type
   * @param _name   name of this access type
   */
  private AccessSet(final long _id,
                    final String _uuid,
                    final String _name)  {
    super(_id, _uuid, _name);
  }

  /////////////////////////////////////////////////////////////////////////////
  // getter and setter instance methods

  /**
   * This is the getter method for instance variable {@link #accessTypes}.
   *
   * @return the value of the instance variable {@link #accessTypes}.
   * @see #accessTypes
   */
  public Set<AccessType> getAccessTypes()  {
    return this.accessTypes;
  }

  /**
   * This is the getter method for instance variable {@link #dataModelTypes}.
   *
   * @return the value of the instance variable {@link #dataModelTypes}.
   * @see #dataModelTypes
   */
  public Set < Type > getDataModelTypes()  {
    return this.dataModelTypes;
  }

  /**
   * Method to initialize the Cache of this CacheObjectInterface.
   */
  public static void initialize() {
    CACHE.initialize(AccessSet.class);
  }

  /**
   * Returns for given identifier in  <i>_id</i> the cached instance of class
   * AccessSet.
   *
   * @return instance of class AccessSet
   * @throws CacheReloadException
   */
  public static AccessSet getAccessSet(final long _id)
     {
    return CACHE.get(_id);
  }

  /**
   * Returns for given name in <i>_name</i> the cached instance of class
   * AccessSet.
   *
   * @return instance of class AccessSet
   * @throws CacheReloadException
   */
  public static AccessSet getAccessSet(final String _name) {
    return CACHE.get(_name);
  }

  /**
   * Returns for given universal unique identifier in <i>_uuid</i> the cached
   * instance of class AccessSet.
   *
   * @return instance of class AccessSet
   * @throws CacheReloadException
   */
  public static AccessSet getAccessSet(final UUID _uuid) {
    return CACHE.get(_uuid);
  }

  private static class AccessSetCache extends Cache<AccessSet> {

    @Override
    protected void readCache(final Map<Long, AccessSet> _newCache4Id,
                             final Map<String, AccessSet> _newCache4Name,
                             final Map<UUID, AccessSet> _newCache4UUID)
        throws CacheReloadException {

      ConnectionResource con = null;
      try  {
        con = Context.getThreadContext().getConnectionResource();

        init4ReadAllAccessSets(con, _newCache4Id, _newCache4Name,
            _newCache4UUID);
        init4ReadLinks2AccessTypes(con, _newCache4Id);
        init4ReadLinks2DMTypes(con, _newCache4Id);

        con.commit();

      } catch (final EFapsException e)  {
        throw new CacheReloadException("could not create connection resource",
                                       e);
      } finally  {
        if ((con != null) && con.isOpened())  {
          try  {
            con.abort();
          } catch (final EFapsException e)  {
            throw new CacheReloadException("could not abort transaction", e);
          }
        }
      }
    }
    /**
     * All access sets are read from the database.
     *
     * @param _con  connection resource
     * @param cache4UUID
     * @param cache4Name
     * @param cache4Id
     * @see #SQL_SELECT
     */
    private static void init4ReadAllAccessSets(final ConnectionResource _con,
                                       final Map<Long, AccessSet> _cache4Id,
                                       final Map<String, AccessSet> _cache4Name,
                                       final Map<UUID, AccessSet> _cache4UUID)
        throws CacheReloadException  {
      Statement stmt = null;
      try  {

        stmt = _con.getConnection().createStatement();

        final ResultSet rs = stmt.executeQuery(SQL_SELECT);
        while (rs.next())  {
          final long id                       = rs.getLong(1);
          final String uuid                   = rs.getString(2);
          final String name                   = rs.getString(3);

          LOG.debug("read access set '" + name + "' "
                    + "(id = " + id + ", uuid = " + uuid + ")");
          final AccessSet accessSet = new AccessSet(id, uuid, name);
          _cache4Id.put(accessSet.getId(), accessSet);
          _cache4Name.put(accessSet.getName(), accessSet);
          _cache4UUID.put(accessSet.getUUID(), accessSet);
        }
      } catch (final SQLException e)  {
        throw new CacheReloadException("could not read access set", e);
      } finally  {
        if (stmt != null)  {
          try  {
            stmt.close();
          } catch (final SQLException e)  {
          }
        }
      }
    }

    /**
     * All access set links to the access types are read from the database.
     *
     * @param _con  connection resource
     * @see #SQL_SET2TYPE
     */
    private static void init4ReadLinks2AccessTypes(final ConnectionResource _con,
                                          final Map<Long, AccessSet> _cache4Id)
        throws CacheReloadException  {
      Statement stmt = null;
      try  {

        stmt = _con.getConnection().createStatement();

        final ResultSet rs = stmt.executeQuery(SQL_SET2TYPE);
        while (rs.next())  {
          final long accessSetId              = rs.getLong(1);
          final long accessTypeId             = rs.getLong(2);

          final AccessSet accessSet   = _cache4Id.get(accessSetId);
          final AccessType accessType = AccessType.getAccessType(accessTypeId);
          if (accessSet == null)  {
            LOG.error("could not found access set with id "
                                                  + "'" + accessSetId + "'");
          } else if (accessType == null)  {
            LOG.error("could not found access type with id "
                                                  + "'" + accessTypeId + "'");
          } else  {
            LOG.debug("read link from "
                      + "access set '" + accessSet.getName() + "' "
                      + "(id = " + accessSet.getId() + ", "
                          + "uuid = " + accessSet.getUUID() + ") to "
                      + "access type '" + accessType.getName() + "' "
                      + "(id = " + accessType.getId() + ", "
                          + "uuid = " + accessType.getUUID() + ")");
            accessSet.getAccessTypes().add(accessType);
          }
         }
        rs.close();

      } catch (final SQLException e)  {
        throw new CacheReloadException("could not read access links", e);
      } finally  {
        if (stmt != null)  {
          try  {
            stmt.close();
          } catch (final SQLException e)  {
          }
        }
      }
    }

    /**
     * All access set links to the data model types are read from the database.
     *
     * @param _con  connection resource
     * @see #SQL_SET2DMTYPE
     */
    private static void init4ReadLinks2DMTypes(final ConnectionResource _con,
                                          final Map<Long, AccessSet> _cache4Id)
        throws CacheReloadException  {
      Statement stmt = null;
      try  {

        stmt = _con.getConnection().createStatement();

        final ResultSet rs = stmt.executeQuery(SQL_SET2DMTYPE);
        while (rs.next())  {
          final long accessSetId              = rs.getLong(1);
          final long dataModelTypeId          = rs.getLong(2);

          final AccessSet accessSet   = _cache4Id.get(accessSetId);
          final Type dataModelType    = Type.get(dataModelTypeId);
          if (accessSet == null)  {
            LOG.error("could not found access set with id "
                                                  + "'" + accessSetId + "'");
          } else if (dataModelType == null)  {
            LOG.error("could not found data model type with id "
                                                 + "'" + dataModelTypeId + "'");
          } else  {
            LOG.debug("read link from "
                      + "access set '" + accessSet.getName() + "' "
                      + "(id = " + accessSet.getId() + ", "
                          + "uuid = " + accessSet.getUUID() + ") to "
                      + "data model type '" + dataModelType.getName() + "' "
                      + "(id = " + dataModelType.getId() + ", "
                          + "uuid = " + dataModelType.getUUID() + ")");
            accessSet.getDataModelTypes().add(dataModelType);
            dataModelType.addAccessSet(accessSet);
          }
         }
        rs.close();

      } catch (final SQLException e)  {
        throw new CacheReloadException("could not read links to types", e);
      } finally  {
        if (stmt != null)  {
          try  {
            stmt.close();
          } catch (final SQLException e)  {
          }
        }
      }
    }
    }
}
