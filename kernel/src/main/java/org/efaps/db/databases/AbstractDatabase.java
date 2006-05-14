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

package org.efaps.db.databases;

import java.util.Map;
import java.util.HashMap;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.JoinRowSet;

/**
 */
public abstract class AbstractDatabase  {

  /**
   * The enumeration defines the known column types in the database.
   */
  public enum ColumnType  {

    /** integer number */
    INTEGER,

    /** real number */
    REAL,

    /** short string */
    STRING_SHORT,

    /** long string */
    STRING_LONG,

    /** data and time */
    DATETIME,

    /** binary large object */
    BLOB,

    /** character large object */
    CLOB,

    /** boolean */
    BOOLEAN
  }

  /**
   *
   */
//  public abstract void createTable();

  /**
   * The class is used to create a new instance of {@link CachedRowSet}.
   *
   * @see #createCachedRowSetInstance
   */
  private Class < CachedRowSet > cachedRowSetImplClass = null;

  /**
   * The class is used to create a new instance of {@link JoinRowSet}.
   *
   * @see #createJoinRowSetInstance
   */
  private Class < JoinRowSet > joinRowSetImplClass = null;

  /**
   * The map stores the mapping between the column types used in eFaps the
   * database specific column types.
   */
  protected final Map < ColumnType, String > columnMap = new HashMap < ColumnType, String >();

  protected AbstractDatabase() throws ClassNotFoundException, IllegalAccessException  {
    this((Class < CachedRowSet >)Class.forName("com.sun.rowset.CachedRowSetImpl"),
         (Class < JoinRowSet >)Class.forName("com.sun.rowset.JoinRowSetImpl"));
  }

  protected AbstractDatabase(
      final Class < CachedRowSet > _cachedRowSetImplClass,
      final Class < JoinRowSet > _joinRowSetImplClass)  {

    this.cachedRowSetImplClass  = _cachedRowSetImplClass;
    this.joinRowSetImplClass    = _joinRowSetImplClass;
  }

  /**
   * The instance method returns a new cached row set instance.
   *
   * @return cached row set instance
   */
  public CachedRowSet createCachedRowSetInstance() throws InstantiationException, IllegalAccessException  {
    return this.cachedRowSetImplClass.newInstance();
  }

  /**
   * The instance method returns a new join row set instance.
   *
   * @return join row set instance
   */
  public JoinRowSet createJoinRowSetInstance() throws InstantiationException, IllegalAccessException  {
    return this.joinRowSetImplClass.newInstance();
  }

  /**
   * @param _columnType column type for which the vendor specific column type
   *                    should be returned
   */
  public String getColumnType(final ColumnType _columnType)  {
    return this.columnMap.get(_columnType);
  }

  /**
   * The method returns the database vendor specific value for the current time
   * stamp.
   *
   * @return vendor specific string of the current time stamp
   */
  public abstract String getCurrentTimeStamp();

  /**
   * The method implements a delete all of database user specific objects (e.g.
   * tables, views etc...). The method is called before a complete rebuild is
   * done.
   *
   * @param _con  sql connection
   * @throws SQLException
   */
  public abstract void deleteAll(final Connection _con) throws SQLException;

  /**
   * A new sql table with column <code>ID</code> is created. If no parent table
   * is given (set to <i>null</i>), the column <code>ID</code> of the table is
   * automatically incremented, otherwise a foreign key to the parent table on
   * column <code>ID</code> is set.
   *
   * @param _con          sql connection
   * @param _table        name of the table to create
   * @param _parentTable  name of the parent table (could be null to define a
   *                      table without parent, but with autoincrement)
   */
  public abstract void createTable(final Connection _con, final String _table,
          final String _parentTable) throws SQLException;
}
