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

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.efaps.admin.datamodel.SQLTable;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.db.transaction.StoreResource;
import org.efaps.util.EFapsException;

/**
 *
 */
public class Delete  {

  private static Logger sqlLogger = Logger.getLogger("org.efaps.sql");

  private static final String CLASSNAME= "org.efaps.db.Delete";

  /**
   *
   */
  public Delete(Context _context, Instance _instance) throws EFapsException  {
    setInstance(_instance);
  }

  /**
   *
   */
  public Delete(Context _context, Type _type, String _id) throws EFapsException  {
    setInstance(new Instance(_context, _type, _id));
  }

  /**
   *
   */
  public Delete(Context _context, String _oid) throws Exception  {
    setInstance(new Instance(_context, _oid));
  }

  /**
   *
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
          if (curTable != mainTable)  {
            StringBuffer buf = new StringBuffer();
            buf.append("delete from ").append(curTable.getSqlTable()).append(" ");
            buf.append("where ").append(curTable.getSqlColId()).append("=").append(getInstance().getId()).append("");
sqlLogger.logp(Level.INFO, CLASSNAME, "execute", buf.toString());
            stmt.addBatch(buf.toString());
          }
        }
        StringBuffer buf = new StringBuffer();
        buf.append("delete from ").append(mainTable.getSqlTable()).append(" ");
        buf.append("where ").append(mainTable.getSqlColId()).append("=").append(getInstance().getId()).append("");
sqlLogger.logp(Level.INFO, CLASSNAME, "execute", buf.toString());
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
      String provider  = getInstance().getType().getProperty("VFSProvider");
System.out.println("...provider="+provider);
System.out.println("...getInstance()="+getInstance());
      if (provider != null)  {
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