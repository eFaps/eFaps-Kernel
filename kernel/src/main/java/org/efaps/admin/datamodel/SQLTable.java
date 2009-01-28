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

package org.efaps.admin.datamodel;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.efaps.db.Context;
import org.efaps.db.databases.information.TableInformation;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.Cache;
import org.efaps.util.cache.CacheReloadInterface;
import org.efaps.util.cache.CacheReloadException;

/**
 * This is the class for the table description. The table description holds
 * information in which table attributes are stored.
 *
 * @author tmo
 * @version $Id$
 */
public class SQLTable extends AbstractDataModelObject {

  /**
   * This is the sql select statement to select all SQL tables from the
   * database.
   */
  private final static String SQL_SELECT
      = "select ID,"
             + "UUID,"
             + "NAME,"
             + "SQLTABLE,"
             + "SQLCOLUMNID,"
             + "SQLCOLUMNTYPE,"
             + "DMTABLEMAIN "
       + "from V_ADMINSQLTABLE";

  /**
   * Stores all instances of SQLTable.
   *
   * @see #getCache
   */
  private final static Cache<SQLTable> tableCache = new Cache<SQLTable>(
                                                      new CacheReloadInterface() {
                                                        public int priority() {
                                                          return CacheReloadInterface.Priority.SQLTable.number;
                                                        };

                                                        public void reloadCache()
                                                                                 throws CacheReloadException {
                                                          SQLTable.initialise();
                                                        };
                                                      });

  /**
   * Instance variable for the name of the SQL table.
   *
   * @see #getSqlTable
   */
  private final String sqlTable;

  /**
   * This instance variable stores the SQL column name of the id of a table.
   *
   * @see #getSqlColId
   */
  private final String sqlColId;

  /**
   * The instance variable stores the SQL column name of the type id.
   *
   * @see #getSqlColType
   */
  private final String sqlColType;

  /**
   * Stores the information about the SQL table within the database.
   *
   * @see #getTableInformation
   */
  private final TableInformation tableInformation;

  /**
   * The instance variable stores the main table for this table instance. The
   * main table is the table, which holds the information about the SQL select
   * statement to get a new id. Also the main table must be inserted as first
   * insert (e.g. the id in the table with a main table has a foreign key to the
   * id of the main table).
   *
   * @see #getMainTable
   * @see #setMainTable
   */
  private SQLTable mainTable = null;

  /**
   * The instance variable stores all types which stores information in this
   * table.
   */
  private final Set<Type> types = new HashSet<Type>();

  /**
   * The instance variables is set to <i>true</i> if this table is only a read
   * only sql table. This means, that no insert and no update on this table is
   * allowed and made.
   *
   * @see #isReadOnly
   * @see #setReadOnly
   */
  private boolean readOnly = false;

  /**
   * This is the constructor for class {@link Attribute}. Every instance of
   * class {@link Attribute} must have a name (parameter <i>_name</i>) and an
   * identifier (parameter <i>_id</i>).
   *
   * @param _id           eFaps id of the SQL table
   * @param _uuid         unique identifier
   * @param _name         eFaps name of the SQL table
   * @param _sqlTable     name of the SQL Table in the database
   * @param _sqlColId     name of column for the id within SQL table
   * @param _sqlColType   name of column for the type within SQL table
   * @throws SQLException
   */
  private SQLTable(final Connection _con,
                   final long _id,
                   final String _uuid,
                   final String _name,
                   final String _sqlTable,
                   final String _sqlColId,
                   final String _sqlColType)
      throws SQLException
  {
    super(_id, _uuid, _name);
    this.sqlTable = _sqlTable.trim();
    this.sqlColId = _sqlColId.trim();
    this.sqlColType = (_sqlColType != null)
                      ? _sqlColType.trim()
                      : null;
    this.tableInformation = Context.getDbType().getTableInformation(_con, this.sqlTable);
  }

  /**
   * The instance method adds a new type to the type list.
   *
   * @see #types
   */
  protected void add(final Type _type)
  {
    getTypes().add(_type);
  }

  // ///////////////////////////////////////////////////////////////////////////

  /**
   * The instance method sets a new property value.
   *
   * @param _name   name of the property
   * @param _value  value of the property
   */
  @Override
  protected void setProperty(final String _name,
                             final String _value)
      throws CacheReloadException
  {
    if (_name.equals("ReadOnly")) {
      this.readOnly = "true".equalsIgnoreCase("true");
    }
  }

  /////////////////////////////////////////////////////////////////////////////

  /**
   * This is the getter method for instance variable {@link #sqlTable}.
   *
   * @return value of instance variable {@link #sqlTable}
   * @see #sqlTable
   */
  public String getSqlTable()
  {
    return this.sqlTable;
  }

  /**
   * This is the getter method for instance variable {@link #sqlColId}.
   *
   * @return value of instance variable {@link #sqlColId}
   * @see #sqlColId
   */
  public String getSqlColId()
  {
    return this.sqlColId;
  }

  /**
   * This is the getter method for instance variable {@link #sqlColType}.
   *
   * @return value of instance variable {@link #sqlColType}
   * @see #sqlColType
   */
  public String getSqlColType()
  {
    return this.sqlColType;
  }


  /**
   * This is the getter method for instance variable {@link #tableInformation}.
   *
   * @return value of instance variable {@link #tableInformation}
   * @see #tableInformation
   */
  public TableInformation getTableInformation()
  {
    return this.tableInformation;
  }

  /**
   * This is the getter method for instance variable {@link #mainTable}.
   *
   * @return value of instance variable {@link #mainTable}
   * @see #mainTable
   */
  public SQLTable getMainTable()
  {
    return this.mainTable;
  }

  /**
   * This is the getter method for instance variable {@link #types}.
   *
   * @return value of instance variable {@link #types}
   * @see #types
   */
  public Set<Type> getTypes()
  {
    return this.types;
  }

  /**
   * This is the getter method for instance variable {@link #readOnly}.
   *
   * @return value of instance variable {@link #readOnly}
   * @see #readOnly
   */
  public boolean isReadOnly()
  {
    return this.readOnly;
  }

  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Initialise the cache of types.
   */
  protected static void initialise() throws CacheReloadException
  {
    ConnectionResource con = null;
    try {
      con = Context.getThreadContext().getConnectionResource();

      Statement stmt = null;
      try {
        final Map<Long, Long> mainTables = new HashMap<Long, Long>();

        stmt = con.getConnection().createStatement();

        ResultSet rs = stmt.executeQuery(SQL_SELECT);
        while (rs.next()) {
          final long id = rs.getLong(1);
          final SQLTable table = new SQLTable(
              con.getConnection(),
              id,
              rs.getString(2),
              rs.getString(3),
              rs.getString(4),
              rs.getString(5),
              rs.getString(6));
          getCache().add(table);
          final long tableMainId = rs.getLong(7);
          if (tableMainId > 0) {
            mainTables.put(id, tableMainId);
          }
          table.readFromDB4Properties();
        }
        rs.close();

        // initialize main tables
        for (final Map.Entry<Long, Long> entry : mainTables.entrySet()) {
          final SQLTable table = SQLTable.get(entry.getKey());
          final SQLTable mainTable = SQLTable.get(entry.getValue());
          table.mainTable = mainTable;
        }

      } finally  {
        if (stmt != null) {
          stmt.close();
        }
      }
      con.commit();
    } catch (SQLException e) {
      throw new CacheReloadException("could not read sql tables", e);
    } catch (EFapsException e) {
      throw new CacheReloadException("could not read sql tables", e);
    }
    finally {
      if ((con != null) && con.isOpened()) {
        try {
          con.abort();
        } catch (EFapsException e) {
          throw new CacheReloadException("could not read sql tables", e);
        }
      }
    }
  }

  /**
   * Returns for given parameter <i>_id</i> the instance of class {@link Table}.
   *
   * @param _id   id to search in the cache
   * @return instance of class {@link SQLTable}
   * @see #getCache
   */
  static public SQLTable get(final long _id)
  {
    return getCache().get(_id);
  }

  /**
   * Returns for given parameter <i>_name</i> the instance of class
   * {@link Table}.
   *
   * @param _name   name to search in the cache
   * @return instance of class {@link SQLTable}
   * @see #getCache
   */
  static public SQLTable get(final String _name)
  {
    return getCache().get(_name);
  }

  /**
   * Returns for given parameter <i>_uuid</i> the instance of class
   * {@link SQLTable}.
   *
   * @return instance of class {@link Type}
   */
  static public SQLTable get(final UUID _uuid)
  {
    return getCache().get(_uuid);
  }

  /**
   * Static getter method for the attribute {@link #cache}.
   *
   * @return value of static variable {@link #cache}
   */
  static Cache<SQLTable> getCache()
  {
    return tableCache;
  }
}
