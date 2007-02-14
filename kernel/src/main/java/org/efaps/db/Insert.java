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

package org.efaps.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.efaps.admin.access.AccessTypeEnums;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.AttributeType;
import org.efaps.admin.datamodel.AttributeTypeInterface;
import org.efaps.admin.datamodel.SQLTable;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.event.TriggerEvent;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.util.EFapsException;

/**
 * @author tmo
 * @version $Id$
 * @todo description
 */
public class Insert extends Update {

  /////////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used in this class.
   */
  private static final Log LOG = LogFactory.getLog(Insert.class);

  /////////////////////////////////////////////////////////////////////////////
  // constructors

  /**
   * @param _type     type of instance to insert
   * @see #addCreateUpdateAttributes
   * @see #addTables
   */
  public Insert(final Type _type) throws EFapsException  {
    super(_type, null);

    addCreateUpdateAttributes();
    addTables();
  }

  /**
   * @deprecated
   */
  public Insert(Context _context, Type _type) throws EFapsException  {
    this(_type);
  }

  /**
   * @param _type type of instance to insert
   * @see #Insert(Type)
   */
  public Insert(final String _type) throws EFapsException  {
    this(Type.get(_type));
  }

  /**
   * @deprecated
   */
  public Insert(Context _context, String _type) throws EFapsException  {
    this(Type.get(_type));
  }

  /////////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * Add all tables of the type to the expressions, because for the type
   * an insert must be made for all tables!!!
   */
  private void addTables()  {
    for (SQLTable table : getType().getTables())  {
      if (getExpr4Tables().get(table) == null)  {
        getExpr4Tables().put(table, new HashMap < String, AttributeTypeInterface >());
      }
    }

  }

  /**
   * Add all attributes of the type which must be always updated.
   *
    * @throws EFapsException from called method
   */
  private void addCreateUpdateAttributes() throws EFapsException  {
    Iterator iter = getType().getAttributes().entrySet().iterator();
    while (iter.hasNext())  {
      Map.Entry entry = (Map.Entry) iter.next();
      Attribute attr = (Attribute) entry.getValue();
      AttributeType attrType = attr.getAttributeType();
      if (attrType.isCreateUpdate())  {
        add(attr, null, false);
      }
    }
  }

  /**
   */
  public void execute() throws EFapsException  {
    boolean hasAccess = getType()
          .hasAccess(new Instance(getType()), 
                     AccessTypeEnums.CREATE.getAccessType());

    if (!hasAccess)  {
      throw new EFapsException(getClass(), "execute.NoAccess", getType());
    }
    executeWithoutAccessCheck();
  }

  /**
   */
  public void executeWithoutAccessCheck() throws EFapsException  {
    Context context = Context.getThreadContext();
    ConnectionResource con = null;
    try  {
      executeTrigger(context, TriggerEvent.INSERT_PRE);

      con = context.getConnectionResource();

      if (test4Unique(context))  {
        throw new EFapsException(getClass(), "executeWithoutAccessCheck.UniqueKeyError");
      }

      SQLTable mainTable = getType().getMainTable();

      long id = executeOneStatement(context, con, mainTable, getExpr4Tables().get(mainTable), 0);

      setInstance(new Instance(context, getInstance().getType(), id));

      for (Map.Entry<SQLTable, Map<String,AttributeTypeInterface>> entry
                                              : getExpr4Tables().entrySet())  {
        SQLTable table = entry.getKey();
        if ((table != mainTable) && !table.isReadOnly())  {
          executeOneStatement(context, con, table, entry.getValue(), id);
        }
      }

      con.commit();

      executeTrigger(context, TriggerEvent.INSERT_POST);
    } catch (EFapsException e)  {
      if (con != null)  {
        con.abort();
      }
      throw e;
    } catch (Throwable e)  {
      if (con != null)  {
        con.abort();
      }
      throw new EFapsException(getClass(), "executeWithoutAccessCheck.Throwable");
    }
  }

  /**
   * A new statement must be created an executed for one table. If the
   * parameter '_id' is set to <code>0</code>, a new id is generated. If the
   * JDBC driver supports method <code>getGeneratedKeys</code>, this method is
   * used, otherwise method {@link org.efaps.db.databases#getNewId} is used
   * to retrieve a new id value.
   *
   * @param _context  context for this request
   * @param _con      connection resource
   * @param _table    sql table used to insert
   * @param _expressions
   * @param _id       new created id
   * @return new created id if parameter <i>_id</i> is set to <code>0</code>
   * @see #createOneStatement
   */
  private long executeOneStatement(final Context _context,
      final ConnectionResource _con, final SQLTable _table,
      final Map _expressions, final long _id) throws EFapsException  {

    long ret = _id;
    PreparedStatement stmt = null;
    try {
      if ((ret == 0)  && !_context.getDbType().supportsGetGeneratedKeys())  {
        ret = _context.getDbType().getNewId(_con.getConnection(),
            _table.getSqlTable(), "ID");
      }

      stmt = createOneStatement(_context, _con, _table, _expressions, ret);

      int rows = stmt.executeUpdate();
      if (rows == 0)  {
        throw new EFapsException(getClass(), "executeOneStatement.NotInserted",
            _table.getName()
        );
      }
      if (ret == 0)  {
        ResultSet rs = stmt.getGeneratedKeys();
        if (rs.next())  {
          ret = rs.getLong(1);
        }
      }
    } catch (EFapsException e)  {
      throw e;
    } catch (Exception e)  {
      throw new EFapsException(getClass(), "executeOneStatement.Exception", e,
          _table.getName()
      );
    } finally  {
      try  {
        stmt.close();
      } catch (Exception e)  {
      }
    }
    return ret;
  }

  /**
   *
   * @param _context  context for this request
   * @param _id       new created id, if null, the table is an autoincrement
   *                  SQL table and the id is not set
   * @return new created prepared statement
   */
  private PreparedStatement createOneStatement(final Context _context,
      final ConnectionResource _con, final SQLTable _table,
      final Map _expressions, final long _id) throws SQLException  {

    List<AttributeTypeInterface> list = new ArrayList<AttributeTypeInterface>();
    StringBuilder cmd = new StringBuilder();
    StringBuilder val = new StringBuilder();
    boolean first = true;
    cmd.append("insert into ").append(_table.getSqlTable()).append("(");
    if (_id != 0)  {
      cmd.append(_table.getSqlColId());
      first = false;
    }
    Iterator iter = _expressions.entrySet().iterator();
    while (iter.hasNext())  {
      Map.Entry entry = (Map.Entry)iter.next();

      if (!first)  {
        cmd.append(",");
        val.append(",");
      } else  {
        first = false;
      }
      cmd.append(entry.getKey());

      AttributeTypeInterface attr = (AttributeTypeInterface) entry.getValue();
      if (!attr.prepareUpdate(val))  {
        list.add(attr);
      }
    }
    if (_table.getSqlColType() != null)  {
      cmd.append(",").append(_table.getSqlColType());
      val.append(",?");
    }
    cmd.append(") values (");
    if (_id != 0)  {
      cmd.append(_id);
    }
    cmd.append("").append(val).append(")");

    if (LOG.isTraceEnabled())  {
      LOG.trace(cmd.toString());
    }

    PreparedStatement stmt;
    if (_id == 0)  {
       stmt = _con.getConnection().prepareStatement(cmd.toString(), new String[]{"ID"});
    } else  {
       stmt = _con.getConnection().prepareStatement(cmd.toString());
    }
    for (int i=0, j=1; i<list.size(); i++, j++)  {
      AttributeTypeInterface attr = (AttributeTypeInterface)list.get(i);
      attr.update(_context, stmt, j);
    }
    if (_table.getSqlColType()!=null)  {
      stmt.setLong(list.size()+1, getType().getId());
    }
    return stmt;
  }
}