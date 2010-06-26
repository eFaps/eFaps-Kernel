/*
 * Copyright 2003 - 2010 The eFaps Team
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

package org.efaps.db.store;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import javax.transaction.xa.Xid;

import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The class implements the {@link Resource} interface for SQL blobs. For each
 * file id a new JDBC store resource must be created.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class JDBCStoreResource
    extends AbstractStoreResource
{
    /**
     * Logging instance used in this class.
     */
    private static Logger LOG = LoggerFactory.getLogger(JDBCStoreResource.class);

    /**
     * Property Name of the name of the blob in the SQL table defined with
     * property {@link #PROPERY_TABLE}.
     */
    private static final String PROPERY_BLOB  = "JDBCBlob";

    /**
     * Property Name of the name of the key (id) in the SQL table defined with
     * property {@link #PROPERY_TABLE}.
     */
    private static final String PROPERY_KEY   = "JDBCKey";

    /**
     * Property Name of the name of the SQL table.
     */
    private static final String PROPERY_TABLE = "JDBCTable";

    /**
     * The string stores the SQL table name where the blob and key is located.
     */
    private String table;

    /**
     * The string stores the name of key column to select the row in the table
     * {@link #table} (used to create the where clause).
     */
    private String keyColumn;

    /**
     * The string stores the name of the blob column in the table
     * {@link #table}.
     */
    private String blobColumn;

    /**
     * Method called to initialize this StoreResource.
     *
     * @param _instance     instance of the object this StoreResource is wanted
     *                      for
     * @param _properties   properties for this StoreResource
     * @param _compress     compress type for this StoreResource
     * @throws EFapsException on error
     * @see org.efaps.db.store.Resource#initialize(Instance, Map, Compress)
     */
    @Override()
    public void initialize(final Instance _instance,
                           final Map<String, String> _properties,
                           final Compress _compress)
        throws EFapsException
    {
        super.initialize(_instance, _properties, _compress);
        this.table = _properties.get(JDBCStoreResource.PROPERY_TABLE);
        this.keyColumn = _properties.get(JDBCStoreResource.PROPERY_KEY);
        this.blobColumn = _properties.get(JDBCStoreResource.PROPERY_BLOB);
    }

    /**
     * The method writes the context (from the input stream) into a SQL blob.
     *
     * @param _in   input stream defined the content of the file
     * @param _size length of the content (or negative meaning that the length
     *              is not known; then the content gets the length of readable
     *              bytes from the input stream) return size of the created
     *              temporary file object
     * @return size of the file
     * @throws EFapsException on error
     */
    public int write(final InputStream _in,
                     final int _size)
        throws EFapsException
    {
        int size = 0;
        ConnectionResource res = null;
        try {
            res = Context.getThreadContext().getConnectionResource();

            final StringBuffer cmd = new StringBuffer().append("update ")
                .append(this.table).append(" set ")
                .append(this.blobColumn).append("=? ")
                .append("where ").append(this.keyColumn).append("=").append(getFileId());

            final PreparedStatement stmt = res.getConnection().prepareStatement(cmd.toString());
            try {
                stmt.setBinaryStream(1, _in, _size);
                stmt.execute();
            } finally {
                stmt.close();
            }
            size = _size;
            res.commit();
        } catch (final EFapsException e) {
            res.abort();
            throw e;
        } catch (final SQLException e) {
            res.abort();
            JDBCStoreResource.LOG.error("write of content failed", e);
            throw new EFapsException(JDBCStoreResource.class, "write.SQLException", e);
        }
        return size;
    }

    /**
     * Deletes the file defined in {@link #fileId}.
     */
    public void delete()
    {
    }

    /**
     * Returns for the file the input stream.
     *
     * @return input stream of the file with the content
     * @throws EFapsException on error
     */
    public InputStream read()
        throws EFapsException
    {
        StoreResourceInputStream in = null;
        ConnectionResource res = null;
        try {
            res = Context.getThreadContext().getConnectionResource();

            final Statement stmt = res.getConnection().createStatement();
            final StringBuffer cmd = new StringBuffer()
                .append("select ").append(this.blobColumn).append(" ")
                .append("from ").append(this.table).append(" ")
                .append("where ").append(this.keyColumn).append("=").append(getFileId());
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
        } catch (final IOException e) {
            JDBCStoreResource.LOG.error("read of content failed", e);
            throw new EFapsException(JDBCStoreResource.class, "read.SQLException", e);
        } catch (final SQLException e) {
            JDBCStoreResource.LOG.error("read of content failed", e);
            throw new EFapsException(JDBCStoreResource.class, "read.SQLException", e);
        } finally {
            if (in == null) {
                res.abort();
            }
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
    public boolean setTransactionTimeout(final int _seconds)
    {
        if (JDBCStoreResource.LOG.isDebugEnabled()) {
            JDBCStoreResource.LOG.debug("setTransactionTimeout (seconds = " + _seconds + ")");
        }
        return true;
    }

    /**
     * @see Resource#isVFS()
     * @return always <i>false</i>
     */
    public boolean isVFS()
    {
        return false;
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
        @Override()
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
        @Override()
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
        @Override()
        public int available()
            throws IOException
        {
            return this.available;
        }
    }


    /**
     * Extdns super class.
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

        /**
         * @throws IOException on error
         */
        @Override
        protected void beforeClose()
            throws IOException
        {
            super.beforeClose();
            try {
                if (this.res.isOpened()) {
                    this.res.commit();
                }
            } catch (final EFapsException e) {
                throw new IOException("commit of connection resource not possible", e);
            }
        }
    }
}
