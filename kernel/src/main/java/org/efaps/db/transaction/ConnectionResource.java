/*
 * Copyright 2003-2008 The eFaps Team
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

import java.sql.Connection;
import java.sql.SQLException;

import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.efaps.db.Context;

/**
 * The class implements the {@link javax.transaction.XAResource} interface for
 * SQL connections.
 *
 * @author tmo
 * @version $Id$
 */
public class ConnectionResource extends AbstractResource {

  /////////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used in this class.
   */
  private final static Logger LOG = LoggerFactory.getLogger(ConnectionResource.class);

  /////////////////////////////////////////////////////////////////////////////
  // instance variables

  /**
   * Stores the SQL connection of this connection resource.
   */
  private Connection connection = null;

  /**
   *
   */
  public ConnectionResource(final Context _context,
                            final Connection _connection) throws SQLException  {
    super(_context);
    this.connection = _connection;
    this.connection.setAutoCommit(false);
  }

  /////////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * This is the getter method for instance variable {@link #connection}.
   *
   * @return value of instance variable {@link #connection}
   * @see #connection
   * @see #setConnection
   */
  public final Connection getConnection()  {
    return this.connection;
  }

  /**
   * Frees the resource and gives this connection resource back to the context
   * object.
   */
  protected void freeResource()  {
    getContext().returnConnectionResource(this);
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
          Commits the global transaction specified by xid.
   */
  public void commit(final Xid _xid,
                     final boolean _onePhase) throws XAException  {
    if (LOG.isDebugEnabled())  {
      LOG.debug("commit (xid = " + _xid + ", one phase = " + _onePhase + ")");
    }
    try  {
if (this.connection != null)  {
      this.connection.commit();
}
    } catch (SQLException e)  {
      XAException xa = new XAException(XAException.XA_RBCOMMFAIL);
      xa.initCause(e);
      throw xa;
    } finally  {
      try {
if (this.connection != null)  {
        this.connection.close();
        this.connection = null;
}
      } catch (SQLException e) {
//        getLogger().log(e, LOG_CHANNEL, Logger.WARNING);
      }
    }
  }

  /**
          Informs the resource manager to roll back work done on behalf of a transaction branch.
   */
  public void rollback(Xid _xid) throws XAException  {
    if (LOG.isDebugEnabled())  {
      LOG.debug("rollback (xid = " + _xid + ")");
    }
    try  {
if (this.connection != null)  {
      this.connection.rollback();
}
    } catch (SQLException e)  {
      XAException xa = new XAException(XAException.XA_RBCOMMFAIL);
      xa.initCause(e);
      throw xa;
    } finally  {
      try {
if (this.connection != null)  {
        this.connection.close();
        this.connection = null;
}
      } catch (SQLException e) {
//        getLogger().log(e, LOG_CHANNEL, Logger.WARNING);
      }
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
  public boolean  setTransactionTimeout(final int seconds)  {
    if (LOG.isDebugEnabled())  {
      LOG.debug("setTransactionTimout");
    }
    return true;
  }
}

