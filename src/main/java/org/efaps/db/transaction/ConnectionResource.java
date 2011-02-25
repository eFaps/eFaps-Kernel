/*
 * Copyright 2003 - 2011 The eFaps Team
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

import org.efaps.db.Context;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The class implements the XAResource interface for SQL connections.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class ConnectionResource
    extends AbstractResource
{
    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ConnectionResource.class);

    /**
     * Stores the SQL connection of this connection resource.
     */
    private Connection connection = null;

    /**
     * @param _connection   SQL connection
     * @throws SQLException if auto commit could not be set to <i>false</i>
     */
    public ConnectionResource(final Connection _connection)
        throws SQLException
    {
        this.connection = _connection;
        this.connection.setAutoCommit(false);
    }

    /**
     * This is the getter method for instance variable {@link #connection}.
     *
     * @return value of instance variable {@link #connection}
     * @see #connection
     */
    public final Connection getConnection()
    {
        return this.connection;
    }

    /**
     * Frees the resource and gives this connection resource back to the
     * context object.
     */
    @Override
    protected void freeResource()
    {
        try {
            Context.getThreadContext().returnConnectionResource(this);
        } catch (final EFapsException e) {
            ConnectionResource.LOG.error("EFapsException", e);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // all further methods are implementing javax.transaction.xa.XAResource

    /**
     * Ask the resource manager to prepare for a transaction commit of the
     * transaction specified in xid (used for 2-phase commits).
     * @param _xid Xid
     * @return 0
     */
    public int prepare(final Xid _xid)
    {
        if (ConnectionResource.LOG.isDebugEnabled())  {
            ConnectionResource.LOG.debug("prepare (xid = " + _xid + ")");
        }
        return 0;
    }

    /**
     * Commits the global transaction specified by xid.
     * @param _xid      Xid
     * @param _onePhase one phase
     * @throws XAException on error
     */
    public void commit(final Xid _xid,
                       final boolean _onePhase)
        throws XAException
    {
        if (ConnectionResource.LOG.isDebugEnabled())  {
            ConnectionResource.LOG.debug("commit (xid = " + _xid + ", one phase = " + _onePhase + ")");
        }
        try  {
            if (this.connection != null)  {
                this.connection.commit();
            }
        } catch (final SQLException e)  {
            final XAException xa = new XAException(XAException.XA_RBCOMMFAIL);
            xa.initCause(e);
            throw xa;
        } finally  {
            try {
                if (this.connection != null)  {
                    this.connection.close();
                    this.connection = null;
                }
            } catch (final SQLException e) {
                ConnectionResource.LOG.error("SQLException", e);
            }
        }
    }

    /**
     * Informs the resource manager to roll back work done on behalf of a
     * transaction branch.
     * @param _xid Xid
     * @throws XAException on error
     */
    public void rollback(final Xid _xid)
        throws XAException
    {
        if (ConnectionResource.LOG.isDebugEnabled())  {
            ConnectionResource.LOG.debug("rollback (xid = " + _xid + ")");
        }
        try  {
            if (this.connection != null)  {
                this.connection.rollback();
            }
        } catch (final SQLException e)  {
            final XAException xa = new XAException(XAException.XA_RBCOMMFAIL);
            xa.initCause(e);
            throw xa;
        } finally  {
            try {
                if (this.connection != null)  {
                    this.connection.close();
                    this.connection = null;
                }
            } catch (final SQLException e) {
                ConnectionResource.LOG.error("SQLException", e);
            }
        }
    }

    /**
     * Tells the resource manager to forget about a heuristically completed
     * transaction branch.
     * @param _xid Xid
     */
    public void forget(final Xid _xid)
    {
        if (ConnectionResource.LOG.isDebugEnabled())  {
            ConnectionResource.LOG.debug("forget (xid = " + _xid + ")");
        }
    }

    /**
     * Obtains the current transaction timeout value set for this XAResource
     * instance.
     * @return 0
     */
    public int getTransactionTimeout()
    {
        if (ConnectionResource.LOG.isDebugEnabled())  {
            ConnectionResource.LOG.debug("getTransactionTimeout");
        }
        return 0;
    }

    /**
     * Obtains a list of prepared transaction branches from a resource manager.
     * @param _flag flag
     * @return null
     */
    public Xid[] recover(final int _flag)
    {
        if (ConnectionResource.LOG.isDebugEnabled())  {
            ConnectionResource.LOG.debug("recover (flag = " + _flag + ")");
        }
        return null;
    }

    /**
     * Sets the current transaction timeout value for this XAResource instance.
     * From this implementation ignored.
     *
     * @param _seconds  time out in seconds
     * @return always <i>true</i>
     */
    public boolean setTransactionTimeout(final int _seconds)
    {
        if (ConnectionResource.LOG.isDebugEnabled())  {
            ConnectionResource.LOG.debug("setTransactionTimout");
        }
        return true;
    }
}
