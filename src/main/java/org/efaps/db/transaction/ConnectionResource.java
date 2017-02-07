/*
 * Copyright 2003 - 2016 The eFaps Team
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;

import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The class implements the XAResource interface for SQL connections.
 *
 * @author The eFaps Team
 *
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
     * Instantiates a new connection resource.
     *
     * @param _connection   SQL connection
     * @throws SQLException if auto commit could not be set to <i>false</i>
     * @throws EFapsException on error
     */
    public ConnectionResource(final Connection _connection)
        throws SQLException, EFapsException
    {
        this.connection = _connection;
        this.connection.setAutoCommit(false);
        open();
    }

    /**
     * This is the getter method for instance variable {@link #connection}.
     *
     * @return value of instance variable {@link #connection}
     * @see #connection
     */
    protected final Connection getConnection()
    {
        return this.connection;
    }

    /**
     * Frees the resource and gives this connection resource back to the
     * context object.
     *
     * @throws EFapsException on error
     */
    @Override
    protected void freeResource()
        throws EFapsException
    {
        try {
            if (!getConnection().isClosed()) {
                getConnection().close();
            }
        } catch (final SQLException e) {
            throw new EFapsException("Could not close", e);
        }
    }

    /**
     * Creates the statement.
     *
     * @return the statement
     * @throws SQLException the SQL exception
     */
    public Statement createStatement()
        throws SQLException
    {
        return getConnection().createStatement();
    }

    /**
     * Prepare statement.
     *
     * @param _sql the sql
     * @return the prepared statement
     * @throws SQLException the SQL exception
     */
    public PreparedStatement prepareStatement(final String _sql)
        throws SQLException
    {
        return getConnection().prepareStatement(_sql);
    }

    /**
     * Prepare statement.
     *
     * @param _sql the sql
     * @param _strings the strings
     * @return the prepared statement
     * @throws SQLException the SQL exception
     */
    public PreparedStatement prepareStatement(final String _sql,
                                              final String[] _strings)
        throws SQLException
    {
        return getConnection().prepareStatement(_sql, _strings);
    }

    /**
     * Prepare statement.
     *
     * @param _string the string
     * @param _returnGeneratedKeys the return generated keys
     * @return the prepared statement
     * @throws SQLException the SQL exception
     */
    public PreparedStatement prepareStatement(final String _string,
                                              final int _returnGeneratedKeys)
        throws SQLException
    {

        return getConnection().prepareStatement(_string, _returnGeneratedKeys);
    }


    /**
     * Informs the resource manager to roll back work done on behalf of a
     * transaction branch.
     * @param _xid Xid
     * @throws XAException on error
     */
    @Override
    public void rollback(final Xid _xid)
        throws XAException
    {
        ConnectionResource.LOG.trace("rollback (xid = {})", _xid);
        try  {
            if (this.connection != null && !this.connection.isClosed())  {
                this.connection.rollback();
            }
        } catch (final SQLException e)  {
            final XAException xa = new XAException(XAException.XA_RBCOMMFAIL);
            xa.initCause(e);
            throw xa;
        } finally  {
            try {
                if (this.connection != null && !this.connection.isClosed())  {
                    this.connection.close();
                }
                this.connection = null;
            } catch (final SQLException e) {
                ConnectionResource.LOG.error("SQLException", e);
            }
        }
    }

    /**
     * Commits the global transaction specified by xid.
     * @param _xid      Xid
     * @param _onePhase one phase
     * @throws XAException on error
     */
    @Override
    public void commit(final Xid _xid,
                       final boolean _onePhase)
        throws XAException
    {
        ConnectionResource.LOG.trace("commit (xid = {}, one phase = {})", _xid, _onePhase);
        try  {
            if (this.connection != null && !this.connection.isClosed())  {
                this.connection.commit();
            }
        } catch (final SQLException e)  {
            final XAException xa = new XAException(XAException.XA_RBCOMMFAIL);
            xa.initCause(e);
            throw xa;
        } finally  {
            try {
                if (this.connection != null && !this.connection.isClosed())  {
                    this.connection.close();
                }
                this.connection = null;
            } catch (final SQLException e) {
                ConnectionResource.LOG.error("SQLException", e);
            }
        }
    }

    /**
     * Ask the resource manager to prepare for a transaction commit of the
     * transaction specified in xid (used for 2-phase commits).
     * @param _xid Xid
     * @return 0
     */
    @Override
    public int prepare(final Xid _xid)
    {
        ConnectionResource.LOG.trace("prepare (xid = {})", _xid);
        return 0;
    }

    /**
     * Tells the resource manager to forget about a heuristically completed
     * transaction branch.
     * @param _xid Xid
     */
    @Override
    public void forget(final Xid _xid)
    {
        ConnectionResource.LOG.trace("forget (xid = {})", _xid);
    }

    /**
     * Obtains the current transaction timeout value set for this XAResource
     * instance.
     * @return 0
     */
    @Override
    public int getTransactionTimeout()
    {
        ConnectionResource.LOG.trace("getTransactionTimeout");
        return 0;
    }

    /**
     * Obtains a list of prepared transaction branches from a resource manager.
     * @param _flag flag
     * @return null
     */
    @Override
    public Xid[] recover(final int _flag)
    {
        ConnectionResource.LOG.trace("recover (flag = {})", _flag);
        return null;
    }

    /**
     * Sets the current transaction timeout value for this XAResource instance.
     * From this implementation ignored.
     *
     * @param _seconds  time out in seconds
     * @return always <i>true</i>
     */
    @Override
    public boolean setTransactionTimeout(final int _seconds)
    {
        ConnectionResource.LOG.debug("setTransactionTimout");
        return true;
    }
}
