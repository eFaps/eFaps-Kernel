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

package org.efaps.db.transaction;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
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

  /**
   * Logging instance used in this class.
   */
  private static Log log = LogFactory.getLog(JDBCStoreResource.class);

  /**
   * Property Name of the virtual file system prefix.
   */
  public final static String PROPERY_PREFIX     = "VFSPrefix";

  /**
   *
   */
  private enum StoreEvent  {DELETE, WRITE, READ, UNKNOWN};

  /**
   *
   */
  private StoreEvent storeEvent = StoreEvent.UNKNOWN;

  /**
   * The string stores the sql table name where the blob and key is located,
   */
  private String table = null;

  /**
   * The string stores the name of key column to select the row in the table
   * {@link #table} (used to create the where clause).
   */
  private String keyColumn = null;

  /**
   * The string stores the name of the blob column in the table {@link #table}.
   */
  private String blobColumn = null;

  /**
   * Buffer used to copy from the input stream to the output stream.
   *
   * @see #read
   */
  private byte[] buffer = new byte[1024];

  /**
   *
   */
  public JDBCStoreResource(final Context _context, final Type _type, final long _fileId)  {
    super(_context, _type, _fileId);

    String prefix = getType().getProperty("VFSPrefix");
    String[] options = prefix.split(":");
    this.table      = options[0];
    this.keyColumn  = options[1];
    this.blobColumn = options[2];
  }

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
      log.error("write of content failed", e);
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
   *
   *
   * @param _out
   * @throws EFapsException
   * @todo exception throwing needed
   */
  public void read(final OutputStream _out) throws EFapsException  {
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
System.out.println("cmd.toString()="+cmd.toString());
      ResultSet resultSet = stmt.executeQuery(cmd.toString());
      if (!resultSet.next()) {
//              @todo exception throwing
//throw new Exception("could not found file");
      }

      InputStream in = resultSet.getBinaryStream(1);
      int length = 1;
      while (length > 0)  {
        length = in.read(this.buffer);
        if (length > 0)  {
          _out.write(this.buffer, 0, length);
        }
      }
      res.commit();
    } catch (IOException e)  {
      log.error("read of content failed", e);
      throw new EFapsException(JDBCStoreResource.class, "write.SQLException", e);
    } catch (SQLException e)  {
      log.error("read of content failed", e);
      throw new EFapsException(JDBCStoreResource.class, "write.SQLException", e);
    } finally  {
      if ((res != null) && (res.isOpened()))  {
        res.abort();
      }
    }
  }

  /////////////////////////////////////////////////////////////////////////////
  // all further methods are implementing javax.transaction.xa.XAResource

  /**
          Ask the resource manager to prepare for a transaction commit of the transaction specified in xid.
   * (used for 2-phase commits)
   */
  public int prepare(final Xid _xid)  {
System.out.println("VFSStore.öööööööööööööööööööööööööö.prepare="+_xid);
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
    if (log.isDebugEnabled())  {
      log.debug("transaction commit");
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
  }

  /**
          Tells the resource manager to forget about a heuristically completed transaction branch.
   */
  public void forget(final Xid _xid)   {
System.out.println("VFSStore.öööööööööööööööööööööööööö.forget="+_xid);
  }

  /**
          Obtains the current transaction timeout value set for this XAResource instance.
   */
  public int getTransactionTimeout()  {
System.out.println("VFSStore.öööööööööööööööööööööööööö.getTransactionTimeout");
    return 0;
  }

  /**
          Obtains a list of prepared transaction branches from a resource manager.
   */
  public Xid[] recover(final int _flag)  {
System.out.println("VFSStore.öööööööööööööööööööööööööö.recover");
    return null;
  }


  /**
          Sets the current transaction timeout value for this XAResource instance.
   */
  public boolean  setTransactionTimeout(final int _seconds)  {
System.out.println("VFSStore.öööööööööööööööööööööööööö.setTransactionTimout");
    return true;
  }
}
