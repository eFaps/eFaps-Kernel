/*
 * Copyright 2003-2007 The eFaps Team
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

/**
 * @author tmo
 * @version $Id$
 * @todo description
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
   * The map stores the mapping between the column types used in eFaps the
   * database specific column types.
   */
  protected final Map < ColumnType, String > columnMap = new HashMap < ColumnType, String >();

  protected AbstractDatabase()  {
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
   * The method tests, if the given view exists.
   *
   * @param _con      sql connection
   * @param _viewName name of view to test
   * @return <i>true</i> if view exists, otherwise <i>false</i>
   */
  public abstract boolean existsView(final Connection _con,
          final String _viewName) throws SQLException;

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

  /**
   * A new id for given column of a sql table is returned (e.g. with
   * sequences). This abstract class always throws a SQLException, because for
   * default, it is not needed to implemente (only if the JDBC drive does not
   * implement method 'getGeneratedKeys' for java.sql.Statements).
   *
   * @param _con          sql connection
   * @param _table        sql table for which a new id must returned
   * @param _column       sql table column for which a new id must returned
   * @throws SQLException always, because method itself is not implemented not
   *         not allowed to call
   */
  public long getNewId(final Connection _con, final String _table,
          final String _column)  throws SQLException  {
    throw new SQLException("method 'getNewId' not imlemented");
  }

  /**
   * The method returns if a database implementation supports to get generated
   * keys while inserting a new line in a SQL table.
   *
   * @return always <i>false</i> because not implemented in this class
   */
  public boolean supportsGetGeneratedKeys()  {
    return false;
  }

  /**
   * The method returns if a database implementation support to get multiple
   * auto generated keys. If defined to <i>true</i>, the insert is done with
   * defined column names for the auto generated columns. Otherwise only
   * {@link java.sql.Statement#RETURN_GENERATED_KEYS} is given for the insert.
   *
   * @return always <i>false</i> because not implemented in this class
   * @see #supportsGetGeneratedKeys
   */
  public boolean supportsMultiGeneratedKeys()  {
    return false;
  }

  /**
   * The method returns if a database implementation supports for blobs binary
   * input stream supports the available method or not.
   *
   * @return always <i>false</i> because not implemented in this class
   * @see #supportsBinaryInputStream
   */
  public boolean supportsBlobInputStreamAvailable()  {
    return false;
  }

  /**
   * The method returns if a database implementation supports directly binary
   * stream for result sets (instead of using first blobs).
   *
   * @return always <i>false</i> because not implemented in this class
   * @see #supportsBlobInputStreamAvailable
   */
  public boolean supportsBinaryInputStream()  {
    return false;
  }

  /////////////////////////////////////////////////////////////////////////////
  // static methods
  
  /**
   * Instanciate the given db classname and returns them.
   *
   * @param _dbClassName    name of the class to instanciate
   * @return new database definition instance
   */
  public static AbstractDatabase findByClassName(final String _dbClassName) 
                                            throws ClassNotFoundException,
                                                   InstantiationException,
                                                   IllegalAccessException  {
    return (AbstractDatabase) Class.forName(_dbClassName).newInstance();
  }
}

