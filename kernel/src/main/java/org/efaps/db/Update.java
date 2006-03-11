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

package org.efaps.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Map;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.AttributeType;
import org.efaps.admin.datamodel.AttributeTypeInterface;
import org.efaps.admin.datamodel.SQLTable;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.ui.Field;
import org.efaps.db.Context;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.util.EFapsException;

/**
 *
 */
public class Update  {

  private static Logger sqlLogger = Logger.getLogger("org.efaps.sql");

  private static final String CLASSNAME= "org.efaps.db.Update";

  /////////////////////////////////////////////////////////////////////////////

  /**
   *
   */
  public Update(Context _context, Instance _instance) throws EFapsException  {
    setInstance(_instance);
    addAlwaysUpdateAttributes(_context);
  }

  /**
   *
   */
  public Update(Context _context, Type _type, String _id) throws EFapsException  {
    setInstance(new Instance(_context, _type, _id));
    addAlwaysUpdateAttributes(_context);
  }

  /**
   *
   */
  public Update(Context _context, String _oid) throws Exception  {
    setInstance(new Instance(_context, _oid));
    addAlwaysUpdateAttributes(_context);
  }

  /**
   * Add all attributes of the type which must be always updated.
   *
   * @param _context  context for this request
   * @param _type     data model type
   */
  protected void addAlwaysUpdateAttributes(Context _context) throws EFapsException  {
    Iterator iter = getInstance().getType().getAttributes().entrySet().iterator();
    while (iter.hasNext())  {
      Map.Entry entry = (Map.Entry)iter.next();
      Attribute attr = (Attribute)entry.getValue();
      AttributeType attrType = attr.getAttributeType();
      if (attrType.isAlwaysUpdate())  {
        add(_context, attr, null);
      }
    }
  }

  /**
   * The method closes the SQL statement.
   *
   * @see #statement
   */
  public void close() throws Exception  {
/*    if (getStatement()!=null)  {
      try  {
        getStatement().close();
      } catch (Exception e)  {
        throw e;
      } finally  {
        setStatement(null);
      }
    }
*/
  }


Map<String,AttributeTypeInterface> mapAttr2Value = new HashMap<String,AttributeTypeInterface>();

  /**
   * @todo exception if attribute does not exists on type!
   */
  public void add(Context _context, String _attr, String _value) throws EFapsException  {
    Attribute attr = getInstance().getType().getAttribute(_attr);
if (attr==null)  {
  throw new EFapsException(getClass(), "add.UnknownAttributeName");
}
    add(_context, attr, _value);
  }

  /**
   * @todo what happens if an attribute owns more than one SQL column? see TODO comment
   */
  public void add(Context _context, Attribute _attr, String _value) throws EFapsException  {
    Map<String,AttributeTypeInterface> expressions = getExpr4Tables().get(_attr.getTable());

    if (expressions==null)  {
      expressions = new HashMap<String,AttributeTypeInterface>();
      getExpr4Tables().put(_attr.getTable(), expressions);
    }

    AttributeTypeInterface attrType = _attr.newInstance();
    attrType.setAttribute(_attr);
    attrType.set(_context, _value);
// TODO: was, wenn ein attribute mehr als ein SQL Column hat?
//    expressions.put(_attr.getSqlColName(), attrType);
expressions.put(_attr.getSqlColNames().get(0), attrType);

mapAttr2Value.put(_attr.getName(), attrType);
  }


protected boolean test4Unique(Context _context)  throws Exception  {
  return test4Unique(_context, getType());
}

private boolean test4Unique(Context _context, Type _type)  throws Exception  {
  boolean ret = false;

  if (_type.getUniqueKeys()!=null)  {
    for (org.efaps.admin.datamodel.UniqueKey uk : _type.getUniqueKeys())  {

      SearchQuery query = new SearchQuery();
      query.setQueryTypes(_context, _type.getName());
      query.setExpandChildTypes(true);

boolean testNeeded = false;
      for (Attribute attr : uk.getAttributes())  {
        AttributeTypeInterface value = mapAttr2Value.get(attr.getName());
        if (value!=null)  {
          query.addWhereAttrEqValue(_context, attr, value.getViewableString(null));
testNeeded = true;
        }
      }
if (testNeeded)  {
      query.addSelect(_context, "ID");
      query.execute(_context);

      while (query.next())  {
        long id = (Long)query.get(_context, "ID");
        if (id!=getInstance().getId())  {
          ret = true;
          break;
        }
      }
}
    }
  }
  if (_type.getParentType()!=null && ret==false)  {
    ret = test4Unique(_context, _type.getParentType());
  }
  return ret;
}

  /**
   *
   */
  public void execute(Context _context) throws Exception  {
    ConnectionResource con = null;
    try  {
      con = _context.getConnectionResource();

      if (test4Unique(_context))  {
        throw new EFapsException(getClass(), "execute.UniqueKeyError");
      }

      for (Map.Entry<SQLTable,Map<String,AttributeTypeInterface>> entry : getExpr4Tables().entrySet())  {
        SQLTable table = entry.getKey();
        Map expressions = (Map)entry.getValue();

        PreparedStatement stmt = null;
        try {
          stmt = createOneStatement(_context, con, table, expressions);
          int rows = stmt.executeUpdate();
          if (rows==0)  {
throw new Exception("Can not update! It exists not!");
          }
        } catch (Exception e)  {
          throw e;
        } finally  {
          stmt.close();
        }
      }
      con.commit();
    } catch (Exception e)  {
e.printStackTrace();
      throw e;
    } finally  {
      if ((con != null) && con.isOpened())  {
        con.abort();
      }
    }
  }

  private PreparedStatement createOneStatement(Context _context, ConnectionResource _con, SQLTable _table, Map _expressions) throws SQLException, EFapsException  {
    List<AttributeTypeInterface> list = new ArrayList<AttributeTypeInterface>();
    StringBuffer cmd = new StringBuffer();
    cmd.append("update ").append(_table.getSqlTable()).append(" set ");
    Iterator iter = _expressions.entrySet().iterator();
    boolean command = false;
    while (iter.hasNext())  {
      Map.Entry entry = (Map.Entry)iter.next();

      if (command)  {
        cmd.append(",");
      } else  {
        command = true;
      }
      cmd.append(entry.getKey()).append("=");

      AttributeTypeInterface attr = (AttributeTypeInterface)entry.getValue();
      if (!attr.prepareUpdate(cmd))  {
        list.add(attr);
      }
    }
    cmd.append(" where ").append(_table.getSqlColId()).append("=").append(getId()).append("");

    sqlLogger.logp(Level.INFO, CLASSNAME, "createOneStatement", cmd.toString());

    PreparedStatement stmt = _con.getConnection().prepareStatement(cmd.toString());
    for (int i=0, j=1; i<list.size(); i++, j++)  {
      AttributeTypeInterface attr = (AttributeTypeInterface)list.get(i);
sqlLogger.logp(Level.INFO, CLASSNAME, "createOneStatement", attr.toString());
      attr.update(_context, stmt, j);
    }
    return stmt;
  }

  /**
   * The instance method returns the Type instance of {@link #instance}.
   *
   * @return type of {@link #instance}
   * @see #instance
   */
  protected Type getType()   {
    return getInstance().getType();
  }

  /**
   * The instance method returns the id of {@link #instance}.
   *
   * @return id of {@link #instance}
   * @see #instance
   */
  public String getId()   {
    return ""+getInstance().getId();
  }

  /////////////////////////////////////////////////////////////////////////////

  /**
   * The instance variable stores the instance for which this update is made.
   *
   * @see #getInstance
   * @see #setInstance
   */
  private Instance instance = null;

  /**
   * The hash table instance variable stores depending on the field as key the
   * index in the select statement.
   *
   * @see #getFields
   */
  private Hashtable fields = new Hashtable();

  /**
   * The string instance variable stores the table names of the select
   * statement.
   *
   * @see #getExpr4Tables
   */
  private Map<SQLTable,Map<String,AttributeTypeInterface>> expr4Tables = new Hashtable<SQLTable,Map<String,AttributeTypeInterface>>();

  /////////////////////////////////////////////////////////////////////////////

  /**
   * This is the getter method for instance variable {@link #instance}.
   *
   * @return value of instance variable {@link #instance}
   * @see #instance
   * @see #setInstance
   */
  public Instance getInstance()  {
    return this.instance;
  }

  /**
   * This is the setter method for instance variable {@link #instance}.
   *
   * @param _out new value for instance variable {@link #instance}
   * @see #instance
   * @see #getInstance
   */
  protected void setInstance(Instance _instance)  {
    this.instance = _instance;
  }

  /**
   * This is the getter method for instance variable {@link #fields}.
   *
   * @return value of instance variable {@link #fields}
   * @see #fields
   */
  private Hashtable getFields()   {
    return this.fields;
  }

  /**
   * This is the getter method for instance variable {@link #tableNames}.
   *
   * @return value of instance variable {@link #tableNames}
   * @see #tableNames
   * @see #setTableNames
   */
  protected Map<SQLTable,Map<String,AttributeTypeInterface>> getExpr4Tables()   {
    return this.expr4Tables;
  }

}
