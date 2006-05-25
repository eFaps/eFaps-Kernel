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

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.efaps.admin.datamodel.SQLTable;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.db.transaction.StoreResource;
import org.efaps.util.EFapsException;

/**
 * The class is used as interface to the eFaps kernel to delete one object.
 *
 * @author tmo
 * @version $Rev$
 */
public class Delete  {

  /**
   * Logging instance used in this class.
   */
  private static final Log LOG = LogFactory.getLog(Delete.class);

  /**
   *
   * @param _context    eFaps context for this request
   * @param _instance
   */
  public Delete(Context _context, Instance _instance) throws EFapsException  {
    setInstance(_instance);
  }

  /**
   *
   * @param _context    eFaps context for this request
   * @param _type
   * @param _id
   */
  public Delete(Context _context, Type _type, String _id) throws EFapsException  {
    setInstance(new Instance(_context, _type, _id));
  }

  /**
   * @param _context    eFaps context for this request
   * @param _oid
   */
  public Delete(Context _context, String _oid) throws Exception  {
    setInstance(new Instance(_context, _oid));
  }

  /**
   * The method executes the delete. For the object, a delete is made in all
   * SQL tables from the type (if the SQL table is not read only!). If a store
   * is defined for the type, the checked in file is also deleted (with the
   * help of the store resource implementation; if the store resource
   * implementation has not implemented the delete, the file is not deleted!).
   *
   * @param _context    eFaps context for this request
   * @see SQLTable#readOnly
   */
  public void execute(Context _context) throws Exception  {
    ConnectionResource con = null;

    try  {
      con = _context.getConnectionResource();

      Statement stmt = null;
      try  {
        stmt = con.getConnection().createStatement();

        SQLTable mainTable = getInstance().getType().getMainTable();
        for (SQLTable curTable : getInstance().getType().getTables())  {
          if ((curTable != mainTable) && !curTable.isReadOnly())  {
            StringBuilder buf = new StringBuilder();
            buf.append("delete from ").append(curTable.getSqlTable()).append(" ");
            buf.append("where ").append(curTable.getSqlColId()).append("=").append(getInstance().getId()).append("");
            if (LOG.isTraceEnabled())  {
              LOG.trace(buf.toString());
            }
            stmt.addBatch(buf.toString());
          }
        }
        StringBuilder buf = new StringBuilder();
        buf.append("delete from ").append(mainTable.getSqlTable()).append(" ");
        buf.append("where ").append(mainTable.getSqlColId()).append("=").append(getInstance().getId()).append("");
        if (LOG.isTraceEnabled())  {
          LOG.trace(buf.toString());
        }
        stmt.addBatch(buf.toString());

        stmt.executeBatch();
      } catch (Exception e)  {
        throw e;
      } finally  {
        try  {
          stmt.close();
        } catch (java.sql.SQLException e)  {
        }
      }

      con.commit();
    } catch (Exception e)  {
      if (con != null)  {
        con.abort();
      }
      throw e;
    }

    StoreResource store = null;
    try  {
      if (getInstance().getType().hasStoreResource())  {
        store = _context.getStoreResource(getInstance());
        store.delete();
        store.commit();
      }
    } catch (Exception e)  {
      if (store != null)  {
        store.abort();
      }
      throw e;
    }
  }

  /////////////////////////////////////////////////////////////////////////////

  /**
   * The instance variable stores the instance for which this update is made.
   *
   * @see #getInstance
   * @see #setInstance
   */
  private Instance instance = null;

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
}