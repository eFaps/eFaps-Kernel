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
 */

package org.efaps.db.databases;

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

    /* integer number */
    INTEGER,

    /* real number */
    REAL,

    /* short string */
    STRING_SHORT,

    /* long string */
    STRING_LONG,

    /* data and time */
    DATETIME,

    /* binary large object */
    BLOB,

    /* character large object */
    CLOB
  }

  /**
   *
   */
//  public abstract void createTable();


  private Class < CachedRowSet > cachedRowSetImplClass = null;

  private Class < JoinRowSet > joinRowSetImplClass = null;


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
  public CachedRowSet createCachedRowSetInstance() throws ClassNotFoundException, InstantiationException, IllegalAccessException  {
    return this.cachedRowSetImplClass.newInstance();
  }

  /**
   * The instance method returns a nw join row set instance.
   *
   * @return join row set instance
   */
  public JoinRowSet createJoinRowSetInstance() throws ClassNotFoundException, InstantiationException, IllegalAccessException  {
    return this.joinRowSetImplClass.newInstance();
  }
}
