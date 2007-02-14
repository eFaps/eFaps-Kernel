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

package org.efaps.db.transaction;

import java.io.InputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.efaps.admin.datamodel.Type;
import org.efaps.db.Context;
import org.efaps.util.EFapsException;

/**
 * The class implements the {@link javax.transaction.XAResource} interface for
 * Blobs.<br/>
 * For each file id a new JDBC store resource must be created.
 */
public class JDBCStoreResource extends StoreResource  {

  /////////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used in this class.
   */
  private static Log LOG = LogFactory.getLog(JDBCStoreResource.class);

  /**
   * Property Name of the name of the blob in the sql table defined with
   * property {@link #PROPERY_TABLE}.
   */
  public final static String PROPERY_BLOB       = "StoreJDBCBlob";

  /**
   * Property Name of the name of the key (id) in the sql table defined with
   * property {@link #PROPERY_TABLE}.
   */
  public final static String PROPERY_KEY        = "StoreJDBCKey";

  /**
   * Property Name of the name of the sql table.
   */
  public final static String PROPERY_TABLE      = "StoreJDBCTable";

  /////////////////////////////////////////////////////////////////////////////
  // enums

  /**
   *
   */
  private enum StoreEvent  {DELETE, WRITE, READ, UNKNOWN};

  /////////////////////////////////////////////////////////////////////////////
  // instance variables

  /**
   *
   */
  private StoreEvent storeEvent = StoreEvent.UNKNOWN;

  /**
   * The string stores the sql table name where the blob and key is located,
   */
  private final String table;

  /**
   * The string stores the name of key column to select the row in the table
   * {@link #table} (used to create the where clause).
   */
  private final String keyColumn;

  /**
   * The string stores the name of the blob column in the table {@link #table}.
   */
  private final String blobColumn;

  /**
   * Buffer used to copy from the input stream to the output stream.
   *
   * @see #read
   */
  private byte[] buffer = new byte[1024];

  /////////////////////////////////////////////////////////////////////////////
  // constructors / destructors

  /**
   *
   */
  public JDBCStoreResource(final Context _context, 
                           final Type _type, 
                           final long _fileId)  {
    super(_context, _type, _fileId);

    this.table      = getType().getProperty(PROPERY_TABLE);
    this.keyColumn  = getType().getProperty(PROPERY_KEY);
    this.blobColumn = getType().getProperty(PROPERY_BLOB);
  }

  /////////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
  /**
   * The method writes the context (from the input stream) to a temporary file
   * (same file URL, but with extension {@link #EXTENSION_TEMP}).
   *
   * @param _in   input stream defined the content of the file
   * @param _size length of the content (or negative meaning that the length
   *              is not known; then the content gets the length of readable
   *              bytes from the input stream)
   * return size of the created temporary file object
   */
  public int write(final InputStream _in, final int _size)
                                                      throws EFapsException  {

    int size = 0;
    ConnectionResource res = null;
    try  {
      res = getContext().getConnectionResource();

      StringBuffer cmd = new StringBuffer()
          .append("update ").append(this.table).append(" ")
              .append("set ").append(this.blobColumn).append("=? ")
              .append("where ").append(this.keyColumn).append("=").append(getFileId())
      ;
//AbstractQuery.sqlLogger.logp(Level.INFO, CLASSNAME, "execute", cmd.toString());

      PreparedStatement stmt = res.getConnection().prepareStatement(cmd.toString());
      try  {
        stmt.setBinaryStream(1, _in, _size);
        stmt.execute();
      } finally  {
        stmt.close();
      }

size = _size;
      res.commit();
    } catch (EFapsException e)  {
      res.abort();
      throw e;
    } catch (SQLException e)  {
      res.abort();
      LOG.error("write of content failed", e);
      throw new EFapsException(JDBCStoreResource.class, "write.SQLException", e);
    }
    return size;
  }

  /**
   * Deletes the file defined in {@link #fileId}.
   */
  public void delete() throws EFapsException  {
  }

  /**
   * Returns for the file the input stream.
   *
   * @return input stream of the file with the content
   * @todo throw exception
   */
  public StoreResourceInputStream read() throws EFapsException  {
    StoreResourceInputStream in = null;
    ConnectionResource res = null;
    try  {
      res = getContext().getConnectionResource();

      Statement stmt = res.getConnection().createStatement();
      StringBuffer cmd = new StringBuffer().
          append("select ").
          append(this.blobColumn).append(" ").
          append("from ").append(this.table).append(" ").
          append("where ").append(this.keyColumn).append("=").append(getFileId())
    ;
//System.out.println("cmd.toString()="+cmd.toString());
      ResultSet resultSet = stmt.executeQuery(cmd.toString());
      if (!resultSet.next()) {
//              @todo exception throwing
//throw new Exception("could not found file");
      }

      in = new JDBCStoreResourceInputStream(this, res,
                                            resultSet.getBinaryStream(1));
    } catch (IOException e)  {
      LOG.error("read of content failed", e);
      throw new EFapsException(JDBCStoreResource.class, "read.SQLException", e);
    } catch (SQLException e)  {
      LOG.error("read of content failed", e);
      throw new EFapsException(JDBCStoreResource.class, "read.SQLException", e);
    } finally  {
      if (in == null)  {
        res.abort();
      }
    }

    return in;
  }

  /////////////////////////////////////////////////////////////////////////////
  // all further methods are implementing javax.transaction.xa.XAResource

  /**
          Ask the resource manager to prepare for a transaction commit of the transaction specified in xid.
   * (used for 2-phase commits)
   */
  public int prepare(final Xid _xid)  {
    if (LOG.isDebugEnabled())  {
      LOG.debug("prepare (xid = " + _xid + ")");
    }
    return 0;
  }

  /**
   * The method is called from the transaction manager if the complete
   * transaction is completed.<br/>
   * Nothing is to do here, because the commitment is done by the
   * {@link ConnectionResource} instance.
   *
   * @param _xid      global transaction identifier (not used, because each
   *                  file with the file id gets a new VFS store resource
   *                  instance)
   * @param _onePhase <i>true</i> if it is a one phase commitment transaction
   *                  (not used)
   * @throws XAException if any exception occurs (catch on
   *         {@link java.lang.Throwable})
   */
  public void commit(final Xid _xid, final boolean _onePhase) throws XAException  {
    if (LOG.isDebugEnabled())  {
      LOG.debug("commit (xid = " + _xid + ", one phase = " + _onePhase + ")");
    }
  }

  /**
   * If the file written in the virtual file system must be rolled back, only
   * the created temporary file (created from method {@link #write}) is
   * deleted.
   *
   * @param _xid      global transaction identifier (not used, because each
   *                  file with the file id gets a new VFS store resource
   *                  instance)
   * @throws XAException if any exception occurs (catch on
   *         {@link java.lang.Throwable})
   */
  public void rollback(final Xid _xid) throws XAException  {
    if (LOG.isDebugEnabled())  {
      LOG.debug("rollback (xid = " + _xid + ")");
    }
  }

  /**
          Tells the resource manager to forget about a heuristically completed transaction branch.
   */
  public void forget(final Xid _xid)   {
    if (LOG.isDebugEnabled())  {
      LOG.debug("forget (xid = " + _xid + ")");
    }
  }

  /**
          Obtains the current transaction timeout value set for this XAResource instance.
   */
  public int getTransactionTimeout()  {
    if (LOG.isDebugEnabled())  {
      LOG.debug("getTransactionTimeout");
    }
    return 0;
  }

  /**
          Obtains a list of prepared transaction branches from a resource manager.
   */
  public Xid[] recover(final int _flag)  {
    if (LOG.isDebugEnabled())  {
      LOG.debug("recover (flag = " + _flag + ")");
    }
    return null;
  }


  /**
          Sets the current transaction timeout value for this XAResource instance.
   */
  public boolean  setTransactionTimeout(final int _seconds)  {
    if (LOG.isDebugEnabled())  {
      LOG.debug("setTransactionTimeout (seconds = " + _seconds + ")");
    }
    return true;
  }
  
  /////////////////////////////////////////////////////////////////////////////
  // input stream wrapper class
  
  private class JDBCStoreResourceInputStream extends StoreResourceInputStream  {
    
    private final ConnectionResource res;
    
    JDBCStoreResourceInputStream(final StoreResource _storeRes,
                                 final ConnectionResource _res,
                                 final InputStream _in)
    throws IOException  {
      super(_storeRes, _in);
      this.res = _res;
    }

    /**
     * @todo Java6 change IOException with throwable parameter
     */
    protected void beforeClose() throws IOException  {
      super.beforeClose();
      try  {
        this.res.commit();
      } catch (EFapsException e)  {
        throw new IOException("commit of connection resource not possible" + e.toString());
      }
    }
  }
}
