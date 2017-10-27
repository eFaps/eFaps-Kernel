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

package org.efaps.db.store;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.transaction.xa.Xid;

import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.databases.AbstractDatabase;
import org.efaps.db.store.Resource.Compress;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.db.wrapper.SQLPart;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The class implements the {@link Resource} interface for SQL blobs. For each
 * file id a new JDBC store resource must be created.
 *
 * @author The eFaps Team
 */
public class JDBCStoreResource
    extends AbstractStoreResource
{
    /**
     * Name of the table the content is stored in.
     */
    public static final String TABLENAME_STORE = "T_CMGENSTOREJDBC";

    /**
     * Name of the column the content is stored in.
     */
    public static final String COLNAME_FILECONTENT = "FILECONTENT";

    /**
     * Logging instance used in this class.
     */
    private static Logger LOG = LoggerFactory.getLogger(JDBCStoreResource.class);

    /**
     * Method called to initialize this StoreResource.
     *
     * @param _instance     instance of the object this StoreResource is wanted
     *                      for
     * @param _store        Store this resource belongs to
     * @throws EFapsException on error
     * @see org.efaps.db.store.Resource#initialize(Instance, Map, Compress)
     */
    @Override
    public void initialize(final Instance _instance,
                           final Store _store)
        throws EFapsException
    {
        super.initialize(_instance, _store);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int add2Select(final SQLSelect _select)
    {
        _select.column(2, "ID").leftJoin(JDBCStoreResource.TABLENAME_STORE, 2, "ID", 0, "ID");
        return 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void insertDefaults()
        throws EFapsException
    {
        super.insertDefaults();
        if (!getExist()[1] && getGeneralID() != null) {
            try {
                final ConnectionResource res = Context.getThreadContext().getConnectionResource();
                Context.getDbType().newInsert(JDBCStoreResource.TABLENAME_STORE, "ID", false)
                                .column("ID", getGeneralID())
                                .execute(res);
            } catch (final SQLException e) {
                throw new EFapsException(JDBCStoreResource.class, "insertDefaults", e);
            }
        }
    }

    /**
     * The method writes the context (from the input stream) into a SQL blob.
     *
     * @param _in   input stream defined the content of the file
     * @param _size length of the content (or negative meaning that the length
     *              is not known; then the content gets the length of readable
     *              bytes from the input stream) return size of the created
     *              temporary file object
     * @param _fileName name of the file
     * @return size of the file
     * @throws EFapsException on error
     */
    @Override
    public long write(final InputStream _in,
                      final long _size,
                      final String _fileName)
        throws EFapsException
    {
        long size = 0;
        ConnectionResource res = null;
        try {
            res = Context.getThreadContext().getConnectionResource();

            final AbstractDatabase<?> db = Context.getDbType();
            final StringBuilder cmd = new StringBuilder().append(db.getSQLPart(SQLPart.UPDATE)).append(" ")
                    .append(db.getTableQuote()).append(JDBCStoreResource.TABLENAME_STORE)
                    .append(db.getTableQuote())
                    .append(" ").append(db.getSQLPart(SQLPart.SET)).append(" ")
                    .append(db.getColumnQuote())
                    .append(JDBCStoreResource.COLNAME_FILECONTENT)
                    .append(db.getColumnQuote()).append(db.getSQLPart(SQLPart.EQUAL)).append("? ")
                    .append(db.getSQLPart(SQLPart.WHERE)).append(" ")
                    .append(db.getColumnQuote()).append("ID").append(db.getColumnQuote())
                    .append(db.getSQLPart(SQLPart.EQUAL)).append(getGeneralID());

            final PreparedStatement stmt = res.prepareStatement(cmd.toString());
            try {
                stmt.setBinaryStream(1, _in, ((Long) _size).intValue());
                stmt.execute();
            } finally {
                stmt.close();
            }
            size = _size;
        } catch (final EFapsException e) {
            throw e;
        } catch (final SQLException e) {
            JDBCStoreResource.LOG.error("write of content failed", e);
            throw new EFapsException(JDBCStoreResource.class, "write.SQLException", e);
        }
        setFileInfo(_fileName, size);
        return size;
    }

    /**
     * Deletes the file defined in {@link #fileId}.
     */
    @Override
    public void delete()
    {
        // not needed here
    }

    /**
     * Returns for the file the input stream.
     *
     * @return input stream of the file with the content
     * @throws EFapsException on error
     */
    @Override
    public InputStream read()
        throws EFapsException
    {
        StoreResourceInputStream in = null;
        ConnectionResource res = null;
        try {
            res = Context.getThreadContext().getConnectionResource();

            final Statement stmt = res.createStatement();
            final StringBuffer cmd = new StringBuffer()
                .append("select ").append(JDBCStoreResource.COLNAME_FILECONTENT).append(" ")
                .append("from ").append(JDBCStoreResource.TABLENAME_STORE).append(" ")
                .append("where ID =").append(getGeneralID());
            final ResultSet resultSet = stmt.executeQuery(cmd.toString());
            if (resultSet.next()) {
                if (Context.getDbType().supportsBinaryInputStream())  {
                    in = new JDBCStoreResourceInputStream(this,
                                                          res,
                                                          resultSet.getBinaryStream(1));
                } else  {
                    in = new JDBCStoreResourceInputStream(this,
                                                          res,
                                                          resultSet.getBlob(1));
                }
            }
            resultSet.close();
            stmt.close();
        } catch (final IOException e) {
            JDBCStoreResource.LOG.error("read of content failed", e);
            throw new EFapsException(JDBCStoreResource.class, "read.SQLException", e);
        } catch (final SQLException e) {
            JDBCStoreResource.LOG.error("read of content failed", e);
            throw new EFapsException(JDBCStoreResource.class, "read.SQLException", e);
        }
        return in;
    }

    /**
     * Ask the resource manager to prepare for a transaction commit of the
     * transaction specified in xid. (used for 2-phase commits).
     * @param _xid  global transaction identifier (not used, because each file
     *              with the file id gets a new VFS store resource instance)
     * @return always 0
     */
    @Override
    public int prepare(final Xid _xid)
    {
        if (JDBCStoreResource.LOG.isDebugEnabled()) {
            JDBCStoreResource.LOG.debug("prepare (xid = " + _xid + ")");
        }
        return 0;
    }

    /**
     * The method is called from the transaction manager if the complete
     * transaction is completed.<br/> Nothing is to do here, because the
     * commitment is done by the {@link ConnectionResource} instance.
     *
     * @param _xid  global transaction identifier (not used, because each file
     *              with the file id gets a new VFS store resource instance)
     * @param _onePhase <i>true</i> if it is a one phase commitment transaction
     *                  (not used)
     */
    @Override
    public void commit(final Xid _xid,
                       final boolean _onePhase)
    {
        if (JDBCStoreResource.LOG.isDebugEnabled()) {
            JDBCStoreResource.LOG.debug("commit (xid = " + _xid + ", one phase = " + _onePhase + ")");
        }
    }

    /**
     * If the file written in the virtual file system must be rolled back, only
     * the created temporary file (created from method {@link #write}) is
     * deleted.
     *
     * @param _xid  global transaction identifier (not used, because each file
     *              with the file id gets a new VFS store resource instance)
     */
    @Override
    public void rollback(final Xid _xid)
    {
        if (JDBCStoreResource.LOG.isDebugEnabled()) {
            JDBCStoreResource.LOG.debug("rollback (xid = " + _xid + ")");
        }
    }

    /**
     * Tells the resource manager to forget about a heuristically completed
     * transaction branch.
     *
     * @param _xid  global transaction identifier (not used, because each file
     *              with the file id gets a new VFS store resource instance)
     */
    @Override
    public void forget(final Xid _xid)
    {
        if (JDBCStoreResource.LOG.isDebugEnabled()) {
            JDBCStoreResource.LOG.debug("forget (xid = " + _xid + ")");
        }
    }

    /**
     * Obtains the current transaction timeout value set for this XAResource
     * instance.
     *
     * @return always 0
     */
    @Override
    public int getTransactionTimeout()
    {
        if (JDBCStoreResource.LOG.isDebugEnabled()) {
            JDBCStoreResource.LOG.debug("getTransactionTimeout");
        }
        return 0;
    }

    /**
     * Obtains a list of prepared transaction branches from a resource manager.
     *
     * @param _flag flag
     * @return always <code>null</code>
     */
    @Override
    public Xid[] recover(final int _flag)
    {
        if (JDBCStoreResource.LOG.isDebugEnabled()) {
            JDBCStoreResource.LOG.debug("recover (flag = " + _flag + ")");
        }
        return null;
    }

    /**
     * Sets the current transaction timeout value for this XAResource instance.
     *
     * @param _seconds number of seconds
     * @return always <i>true</i>
     */
    @Override
    public boolean setTransactionTimeout(final int _seconds)
    {
        if (JDBCStoreResource.LOG.isDebugEnabled()) {
            JDBCStoreResource.LOG.debug("setTransactionTimeout (seconds = " + _seconds + ")");
        }
        return true;
    }

    /**
     * This class implements an InputStream to read bytes from a
     * {@link java.sql.Blob} if the get binary stream of the blob does not
     * support the available method (and returns e.g. always 0 like the Oracle
     * JDBC driver).
     *
     * TODO:  avaible must be long! (because of max integer value!)
     */
    private class BlobInputStream
        extends InputStream
    {
        /**
         * Stores the blob for this input stream.
         */
        private final Blob blob;

        /**
         * The actual InputStream.
         */
        private final InputStream in;

        /**
         * Hold the available bytes in the input stream.
         */
        private int available;

        /**
         * @param _blob blob to be read
         * @throws SQLException on error with blob
         */
        protected BlobInputStream(final Blob _blob)
            throws SQLException
        {
            this.blob = _blob;
            this.in = _blob.getBinaryStream();
            this.available = (int) this.blob.length();
        }

        /**
         * @see java.io.InputStream#read()
         * @return length of the stream
         * @throws IOException on error
         */
        @Override
        public int read()
            throws IOException
        {
            this.available--;
            return this.in.read();
        }

        /**
         * @see java.io.InputStream#read(byte[])
         * @param _bytes bytes to read
         * @return length of the stream
         * @throws IOException on error
         */
        @Override
        public int read(final byte[] _bytes)
            throws IOException
        {
            int length = _bytes.length;
            if (this.available > 0)  {
                if (this.available < length)  {
                    length = this.available;
                }
                this.available = this.available - length;
                this.in.read(_bytes);
            } else  {
                length = -1;
            }
            return length;
        }

        /**
         * @see java.io.InputStream#available()
         * @return <i>true</i> if available
         * @throws IOException on error
         */
        @Override
        public int available()
            throws IOException
        {
            return this.available;
        }
    }


    /**
     * Extdens super class.
     *
     */
    private class JDBCStoreResourceInputStream
        extends StoreResourceInputStream
    {
        /**
         * The connection resource.
         */
        private final ConnectionResource res;

        /**
         * @param _storeRe  store resource itself
         * @param _res      connection resource
         * @param _blob     blob with the input stream
         * @throws SQLException on error with blob
         * @throws IOException on error with inputstream
         */
        protected JDBCStoreResourceInputStream(final AbstractStoreResource _storeRe,
                                               final ConnectionResource _res,
                                               final Blob _blob)
            throws IOException, SQLException
        {
            super(_storeRe,
                  Context.getDbType().supportsBlobInputStreamAvailable()
                          ? _blob.getBinaryStream()
                          : new BlobInputStream(_blob));
            this.res = _res;
        }

        /**
         * @param _storeRes store resource itself
         * @param _res      connection resource
         * @param _in       binary input stream (from the blob)
         * @throws IOException on error
         */
        protected JDBCStoreResourceInputStream(final AbstractStoreResource _storeRes,
                                               final ConnectionResource _res,
                                               final InputStream _in)
            throws IOException
        {
            super(_storeRes, _in);
            this.res = _res;
        }
    }
}
