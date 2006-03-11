/*
 * Copyright 2005 The eFaps Team
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

package org.efaps.admin.datamodel;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.efaps.admin.datamodel.AttributeType;
import org.efaps.db.Cache;
import org.efaps.db.CacheInterface;
import org.efaps.db.Context;
import org.efaps.db.transaction.ConnectionResource;

/**
 * This is the class for the table description. The table description holds
 * information in which table attributes are stored.
 */
public class SQLTable extends DataModelObject  {

  /**
   * Logging instance used in this class.
   */
  private final static Log log = LogFactory.getLog(SQLTable.class);

  /**
   * This is the sql select statement to select all SQL tables from the
   * database.
   */
  private final static String SQL_SELECT  = "select "+
                                                "ID,"+
                                                "NAME,"+
                                                "SQLTABLE,"+
                                                "SQLCOLUMNID,"+
                                                "SQLCOLUMNTYPE,"+
                                                "SQLNEWIDSELECT,"+
                                                "DMTABLEMAIN "+
                                              "from ADMINSQLTABLE";

  /**
   * Stores all instances of SQLTable.
   *
   * @see #getCache
   */
  private final static Cache<SQLTable> tableCache = new Cache<SQLTable>();

  /**
   * This is the constructor for class {@link Attribute}. Every instance of
   * class {@link Attribute} must have a name (parameter <i>_name</i>) and
   * an identifier (parameter <i>_id</i>).
   *
   * @param _id         id of the attribute
   * @param _name       name of the instance
   */
  private SQLTable(long _id, String _name)  {
    super(_id, _name);
  }

  /**
   * Returns the name of the table.
   *
   * @param _context
   * @see #getName
   */
  public String getViewableName(Context _context)  {
    return getName();
  }

  /**
   * The instance method adds a new type to the type list.
   *
   * @see #types
   */
  protected void add(Type _type)  {
    getTypes().add(_type);
  }

  /////////////////////////////////////////////////////////////////////////////

  /**
   * The instance method sets a new property value.
   *
   * @param _name   name of the property
   * @param _value  value of the property
   * @param _toId   id of the to object
   */
  protected void setProperty(Context _context, String _name, String _value) throws Exception  {
    if (_name.equals("ReadOnly"))  {
      if (_value.equals("true"))  {
        setReadOnly(true);
      }
    }
  }

  /////////////////////////////////////////////////////////////////////////////

  /**
   * Instance variable for the name of the sql table.
   *
   * @see #getSqlTable
   * @see #setSqlTable
   */
  private String sqlTable = null;

  /**
   * This instance variable stores the sql column name of the id of a table.
   *
   * @see #getSqlColId
   * @see #setSqlColId
   */
  private String sqlColId = null;

  /**
   * The instance variable stores the sql column name of the type.
   *
   * @see #getSqlColType
   * @see #setSqlColType
   */
  private String sqlColType = null;

  /**
   * The instance variable stores the sql select statement to get a new ID.
   *
   * @see #getSqlNewIdSelect
   * @see #setSqlNewIdSelect
   */
  private String sqlNewIdSelect = null;

  /**
   * The instance variable stores the main table for this table instance. The
   * main table is the table, which holds the information about the sql
   * select statement to get a new id. Also the main table must be inserted
   * as first insert (e.g. the id in the table with a main table has a foreign
   * key to the id of the main table).
   *
   * @see #getMainTable
   * @see #setMainTable
   */
  private SQLTable mainTable = null;

  /**
   * The instance variable stores all types which stores information in this
   * table.
   */
  private Set<Type> types = new HashSet<Type>();

  /**
   * The instance variables is set to <i>true</i> if this table is only a
   * read only sql table. This means, that no insert and no update on this
   * table is allowed and made.
   *
   * @see #isReadOnly
   * @see #setReadOnly
   */
  private boolean readOnly = false;

  /////////////////////////////////////////////////////////////////////////////

  /**
   * This is the setter method for instance variable {@link #sqlTable}.
   *
   * @param _sqlTable new instance of class {@link Type} to set for sqlTable
   * @see #sqlTable
   * @see #getSqlTable
   */
  void setSqlTable(String _sqlTable)  {
    this.sqlTable = _sqlTable;
  }

  /**
   * This is the getter method for instance variable {@link #sqlTable}.
   *
   * @return value of instance variable {@link #sqlTable}
   * @see #sqlTable
   * @see #setSqlTable
   */
  public String getSqlTable()  {
    return this.sqlTable;
  }

  /**
   * This is the setter method for instance variable {@link #sqlColId}.
   *
   * @param _sqlColId new value for instance variable {@link #sqlColId}
   * @see #sqlColId
   * @see #getSqlColId
   */
  private void setSqlColId(String _sqlColId)  {
    this.sqlColId = _sqlColId;
  }

  /**
   * This is the getter method for instance variable {@link #sqlColId}.
   *
   * @return value of instance variable {@link #sqlColId}
   * @see #sqlColId
   * @see #setSqlColId
   */
  public String getSqlColId()  {
    return this.sqlColId;
  }

  /**
   * This is the setter method for instance variable {@link #sqlColType}.
   *
   * @param _sqlColType new value for instance variable {@link #sqlColType}
   * @see #sqlColType
   * @see #getSqlColType
   */
  private void setSqlColType(String _sqlColType)  {
    this.sqlColType = (_sqlColType != null ? _sqlColType.trim() : null);
  }

  /**
   * This is the getter method for instance variable {@link #sqlColType}.
   *
   * @return value of instance variable {@link #sqlColType}
   * @see #sqlColType
   * @see #setSqlColType
   */
  public String getSqlColType()  {
    return this.sqlColType;
  }

  /**
   * This is the setter method for instance variable {@link #sqlNewIdSelect}.
   *
   * @param _sqlNewIdSelect new value for instance variable
   *                        {@link #sqlNewIdSelect}
   * @see #sqlNewIdSelect
   * @see #getSqlNewIdSelect
   */
  private void setSqlNewIdSelect(String _sqlNewIdSelect)  {
    this.sqlNewIdSelect = (_sqlNewIdSelect != null ? _sqlNewIdSelect.trim() : null);
  }

  /**
   * This is the getter method for instance variable {@link #sqlNewIdSelect}.
   *
   * @return value of instance variable {@link #sqlNewIdSelect}
   * @see #sqlNewIdSelect
   * @see #setSqlNewIdSelect
   */
  public String getSqlNewIdSelect()  {
    return this.sqlNewIdSelect;
  }

  /**
   * This is the setter method for instance variable {@link #mainTable}.
   *
   * @param _mainTable new value for instance variable {@link #mainTable}
   * @see #mainTable
   * @see #getMainTable
   */
  private void setMainTable(SQLTable _mainTable)  {
    this.mainTable = _mainTable;
  }

  /**
   * This is the getter method for instance variable {@link #mainTable}.
   *
   * @return value of instance variable {@link #mainTable}
   * @see #mainTable
   * @see #setMainTable
   */
  public SQLTable getMainTable()  {
    return this.mainTable;
  }

  /**
   * This is the getter method for instance variable {@link #types}.
   *
   * @return value of instance variable {@link #types}
   * @see #types
   * @see #setTypes
   */
  public Set<Type> getTypes()  {
    return this.types;
  }

  /**
   * This is the setter method for instance variable {@link #readOnly}.
   *
   * @param _readOnly new value for instance variable {@link #readOnly}
   * @see #readOnly
   * @see #isReadOnly
   */
  private void setReadOnly(boolean _readOnly)  {
    this.readOnly = _readOnly;
  }

  /**
   * This is the getter method for instance variable {@link #readOnly}.
   *
   * @return value of instance variable {@link #readOnly}
   * @see #readOnly
   * @see #setReadOnly
   */
  public boolean isReadOnly()  {
    return this.readOnly;
  }

  /////////////////////////////////////////////////////////////////////////////

  /**
   * Initialise the cache of types.
   *
   * @param _context  eFaps context for this request
   */
  public static void initialise(final Context _context) throws Exception  {
    ConnectionResource con = null;
    try  {
      con = _context.getConnectionResource();

      Statement stmt = null;
      try  {
        Map<Long,Long> mainTables = new HashMap<Long,Long>();

        stmt = con.getConnection().createStatement();

        ResultSet rs = stmt.executeQuery(SQL_SELECT);
        while (rs.next())  {
          long id =     rs.getLong(1);
          String name = rs.getString(2).trim();

          SQLTable table = new SQLTable(id, name);
          table.setSqlTable(rs.getString(3).trim());
          table.setSqlColId(rs.getString(4).trim());
          table.setSqlColType(rs.getString(5));
          table.setSqlNewIdSelect(rs.getString(6));
          getCache().add(table);
          long tableMainId = rs.getLong(7);
          if (tableMainId>0)  {
            mainTables.put(id, tableMainId);
          }
          table.readFromDB4Properties(_context);
        }
        rs.close();

        // initialise main tables
        for (Map.Entry<Long,Long> entry: mainTables.entrySet())  {
          SQLTable table      = SQLTable.get(entry.getKey());
          SQLTable mainTable  = SQLTable.get(entry.getValue());
          table.setMainTable(mainTable);
        }

      } finally  {
        if (stmt != null)  {
          stmt.close();
        }
      }
      con.commit();
    } finally  {
      if ((con != null) && con.isOpened())  {
        con.abort();
      }
    }
  }

  /**
   * Returns for given parameter <i>_id</i> the instance of class
   * {@link Table}.
   *
   * @param _id id to search in the cache
   * @return instance of class {@link Table}
   * @see #getCache
   */
  static public SQLTable get(final long _id) throws Exception  {
    return getCache().get(_id);
  }

  /**
   * Returns for given parameter <i>_name</i> the instance of class
   * {@link Table}.
   *
   * @param _name name to search in the cache
   * @return instance of class {@link Table}
   * @see #getCache
   */
  static public SQLTable get(final String _name) throws Exception  {
    return getCache().get(_name);
  }

  /**
   * Static getter method for the attribute {@link #cache}.
   *
   * @return value of static variable {@link #cache}
   */
  static Cache<SQLTable> getCache()  {
    return tableCache;
  }
}
