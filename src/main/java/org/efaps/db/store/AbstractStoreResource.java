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

package org.efaps.db.store;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

import org.efaps.db.Context;
import org.efaps.db.GeneralInstance;
import org.efaps.db.Instance;
import org.efaps.db.InstanceQuery;
import org.efaps.db.databases.AbstractDatabase;
import org.efaps.db.transaction.AbstractResource;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.db.wrapper.SQLPart;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author The eFaps Team
 * @version $Id$
 */
public abstract class AbstractStoreResource
    extends AbstractResource
    implements Resource
{
    /**
     * Name of the main store table.
     */
    public static final String TABLENAME_STORE = "T_CMGENSTORE";

    /**
     * Name of the column for the filename.
     */
    public static final String COLNAME_FILENAME = "FILENAME";

    /**
     * Name of the column for the file length.
     */
    public static final String COLNAME_FILELENGTH = "FILELENGTH";

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractStoreResource.class);

    /**
     * Basic SQL Select for getting the Resource.
     */
    private static final SQLSelect SQL_SELECT = new SQLSelect()
                                                    .column(0, "ID")
                                                    .column(1, AbstractStoreResource.COLNAME_FILENAME)
                                                    .column(1, AbstractStoreResource.COLNAME_FILELENGTH)
                                                    .column(1, "ID")
                                                    .from(GeneralInstance.TABLENAME, 0)
                                                    .leftJoin(AbstractStoreResource.TABLENAME_STORE, 1, "ID", 0, "ID");

    /**
     * @see #StoreEvent
     */
    private StoreEvent storeEvent = StoreEvent.UNKNOWN;

    /**
     * Buffer used to copy from the input stream to the output stream.
     *
     * @see #read()
     */
    private final byte[] buffer = new byte[1024];

    /**
     * Instance this resource belongs to.
     */
    private Instance instance;

    /**
     * GeneralID of this Store Resource.
     */
    private Long generalID;

    /**
     * Do the related objects exist.
     */
    private boolean[] exist;

    /**
     * File Name of the Source.
     */
    private String fileName = "DEFAULT";

    /**
     * Length of the file in byte.
     */
    private Long fileLength = new Long(0);

    /**
     * Store this Resource belongs to.
     */
    private Store store;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(final Instance _instance,
                           final Store _store)
        throws EFapsException
    {
        this.instance = _instance;
        this.store = _store;
        final SQLSelect select = AbstractStoreResource.SQL_SELECT.getCopy()
                        .addPart(SQLPart.WHERE)
                        .addColumnPart(0, "INSTTYPEID").addPart(SQLPart.EQUAL)
                        .addValuePart(_instance.getType().getId())
                        .addPart(SQLPart.AND)
                        .addColumnPart(0, "INSTID").addPart(SQLPart.EQUAL).addValuePart(_instance.getId());
        this.exist = new boolean[1 + add2Select(select)];
        getGeneralID(select.getSQL());

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void open(final StoreEvent _event)
        throws EFapsException
    {
        this.storeEvent = _event;
        super.open();
        if (getStoreEvent().equals(StoreEvent.READ) || getStoreEvent().equals(StoreEvent.WRITE)) {
            insertDefaults();
        }
    }

    /**
     * The output stream is written with the content of the file. From method {@link #read()} the input stream is used
     * and copied into the output stream.
     *
     * @param _out output stream where the file content must be written
     * @throws EFapsException if an error occurs
     * @see #read()
     */
    public void read(final OutputStream _out)
        throws EFapsException
    {
        StoreResourceInputStream in = null;
        try {
            in = (StoreResourceInputStream) read();
            if (in != null) {
                int length = 1;
                while (length > 0) {
                    length = in.read(this.buffer);
                    if (length > 0) {
                        _out.write(this.buffer, 0, length);
                    }
                }
            }
        } catch (final IOException e) {
            throw new EFapsException(AbstractStoreResource.class, "read.IOException", e);
        } finally {
            if (in != null) {
                try {
                    in.closeWithoutCommit();
                } catch (final IOException e) {
                    AbstractStoreResource.LOG.warn("Catched IOException in class: " + this.getClass());
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFileName()
        throws EFapsException
    {
        return this.fileName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getFileLength()
        throws EFapsException
    {
        return this.fileLength;
    }

    /**
     * Insert default values in the table. (if necessary).
     * @throws EFapsException on error
     */
    protected void insertDefaults()
        throws EFapsException
    {
        if (!getExist()[0] && getGeneralID() != null) {
            try {
                final ConnectionResource res = Context.getThreadContext().getConnectionResource();
                final Connection con = res.getConnection();
                Context.getDbType().newInsert(AbstractStoreResource.TABLENAME_STORE, "ID", false)
                                .column("ID", getGeneralID())
                                .column(AbstractStoreResource.COLNAME_FILENAME, "TMP")
                                .column(AbstractStoreResource.COLNAME_FILELENGTH, 0)
                                .execute(con);
                res.commit();
                this.fileName = "TMP";
                this.fileLength = new Long(0);
            } catch (final SQLException e) {
                throw new EFapsException(AbstractStoreResource.class, "insertDefaults", e);
            }
        }
    }


    /**
     * Set the info for the file in this store reosurce.
     * @param _filename     name of the file
     * @param _fileLength   length of the file
     * @throws EFapsException on error
     */
    protected void setFileInfo(final String _filename,
                               final long _fileLength)
        throws EFapsException
    {
        if (!_filename.equals(this.fileName) || _fileLength != this.fileLength) {
            ConnectionResource res = null;
            try {
                res = Context.getThreadContext().getConnectionResource();
                final AbstractDatabase<?> db = Context.getDbType();
                final StringBuilder cmd = new StringBuilder().append(db.getSQLPart(SQLPart.UPDATE)).append(" ")
                        .append(db.getTableQuote()).append(AbstractStoreResource.TABLENAME_STORE)
                        .append(db.getTableQuote())
                        .append(" ").append(db.getSQLPart(SQLPart.SET)).append(" ")
                        .append(db.getColumnQuote())
                        .append(AbstractStoreResource.COLNAME_FILENAME)
                        .append(db.getColumnQuote()).append(db.getSQLPart(SQLPart.EQUAL)).append("? ")
                        .append(db.getSQLPart(SQLPart.COMMA))
                        .append(db.getColumnQuote())
                        .append(AbstractStoreResource.COLNAME_FILELENGTH)
                        .append(db.getColumnQuote()).append(db.getSQLPart(SQLPart.EQUAL)).append("? ")
                        .append(db.getSQLPart(SQLPart.WHERE)).append(" ")
                        .append(db.getColumnQuote()).append("ID").append(db.getColumnQuote())
                        .append(db.getSQLPart(SQLPart.EQUAL)).append(getGeneralID());

                final PreparedStatement stmt = res.getConnection().prepareStatement(cmd.toString());
                try {
                    stmt.setString(1, _filename);
                    stmt.setLong(2, _fileLength);
                    stmt.execute();
                } finally {
                    stmt.close();
                }
                res.commit();
            } catch (final EFapsException e) {
                res.abort();
                throw e;
            } catch (final SQLException e) {
                res.abort();
                throw new EFapsException(JDBCStoreResource.class, "write.SQLException", e);
            }
        }
    }

    /**
     * Add to the select for the existence check.
     * @param _select select to add to
     * @return number of added columns
     */
    protected abstract int add2Select(final SQLSelect _select);

    /**
     * Get the generalID etc. from the eFasp DataBase.
     * @param _complStmt Statement to be executed
     * @throws EFapsException on error
     */
    private void getGeneralID(final String _complStmt)
        throws EFapsException
    {
        ConnectionResource con = null;
        try {
            con = Context.getThreadContext().getConnectionResource();

            final Statement stmt = con.getConnection().createStatement();

            final ResultSet rs = stmt.executeQuery(_complStmt.toString());

            while (rs.next()) {
                this.generalID = rs.getLong(1);
                this.fileName = rs.getString(2);
                if (this.fileName != null && !this.fileName.isEmpty()) {
                    this.fileName = this.fileName.trim();
                }
                this.fileLength = rs.getLong(3);
                for (int i = 0; i < this.exist.length; i++) {
                    this.exist[i] = rs.getLong(4 + i) > 1;
                }
                getAdditionalInfo(rs);
            }
            rs.close();
            stmt.close();
            con.commit();
        } catch (final SQLException e) {
            throw new EFapsException(InstanceQuery.class, "executeOneCompleteStmt", e);
        } finally {
            if (con != null && con.isOpened()) {
                con.abort();
            }
        }
    }

    /**
     * Can be used by implementation to get additionla information form the database.
     * @param _rs   ResultSet
     * @throws SQLException on error
     */
    protected void getAdditionalInfo(final ResultSet _rs)
        throws SQLException
    {
    }

    /**
     * Frees this resource. Only a dummy implementation because nothing must be freed for this store.
     */
    @Override
    protected void freeResource()
    {
    }

    /**
     * Getter method for instance variable {@link #instance}.
     *
     * @return value of instance variable {@link #instance}
     */
    protected Instance getInstance()
    {
        return this.instance;
    }

    /**
     * Setter method for instance variable {@link #instance}.
     *
     * @param _instance value for instance variable {@link #instance}
     */
    protected void setInstance(final Instance _instance)
    {
        this.instance = _instance;
    }

    /**
     * Is this Store resource compressed.
     *
     * @return Is this Store resource compressed
     */
    protected Compress getCompress()
    {
        Compress compress;
        if (this.store.getResourceProperties().containsKey(Store.PROPERTY_COMPRESS)) {
            compress = Compress.valueOf(this.store.getResourceProperties().get(Store.PROPERTY_COMPRESS).toUpperCase());
        } else {
            compress = Compress.NONE;
        }
        return compress;
    }

    /**
     * Getter method for instance variable {@link #store}.
     *
     * @return value of instance variable {@link #store}
     */
    protected Store getStore()
    {
        return this.store;
    }

    /**
     * Get the properties for this resource.
     *
     * @return properties for this resource
     */
    protected Map<String, String> getProperties()
    {
        return this.store.getResourceProperties();
    }

    /**
     * Getter method for instance variable {@link #exist}.
     *
     * @return value of instance variable {@link #exist}
     */
    protected boolean[] getExist()
    {
        return this.exist;
    }

    /**
     * Getter method for instance variable {@link #generalID}.
     *
     * @return value of instance variable {@link #generalID}
     */
    protected Long getGeneralID()
    {
        return this.generalID;
    }

    /**
     * Getter method for instance variable {@link #storeEvent}.
     *
     * @return value of instance variable {@link #storeEvent}
     */
    protected StoreEvent getStoreEvent()
    {
        return this.storeEvent;
    }

    /**
     * Wraps the standard {@link InputStream} to get an input stream for the needs of eFaps.
     */
    protected class StoreResourceInputStream
        extends InputStream
    {
        /**
         * InputStream.
         */
        private final InputStream in;

        /**
         * StoreResource for this InputStream.
         */
        private final AbstractStoreResource store;

        /**
         * @param _resource StoreResource this InputStream belong to
         * @param _in       inputstream
         * @throws IOException on error
         */
        protected StoreResourceInputStream(final AbstractStoreResource _resource,
                                           final InputStream _in)
            throws IOException
        {
            this.store = _resource;
            if (_resource.getCompress().equals(Compress.GZIP)) {
                this.in = new GZIPInputStream(_in);
            } else if (_resource.getCompress().equals(Compress.ZIP)) {
                this.in = new ZipInputStream(_in);
            } else {
                this.in = _in;
            }
        }

        /**
         * The input stream itself is closed.
         *
         * @throws IOException on error
         */
        protected void beforeClose()
            throws IOException
        {
            this.in.close();
        }

        /**
         * Only a dummy method if something must happened after the commit of the store.
         *
         * @throws IOException on error
         */
        protected void afterClose()
            throws IOException
        {
        }

        /**
         * The input stream and others are closes without commit of the store resource.
         *
         * @throws IOException on error
         */
        private void closeWithoutCommit()
            throws IOException
        {
            beforeClose();
            afterClose();
        }

        /**
         * Calls method {@link #beforeClose()}, then commits the store and at least calls method {@link #afterClose()}.
         *
         * @see #beforeClose()
         * @see #afterClose()
         * @throws IOException on error
         */
        @Override
        public void close()
            throws IOException
        {
            try {
                super.close();
                beforeClose();
                if (this.store.isOpened()) {
                    this.store.commit();
                }
                afterClose();
            } catch (final EFapsException e) {
                throw new IOException("commit of store not possible", e);
            } finally {
                if (this.store.isOpened()) {
                    try {
                        this.store.abort();
                    } catch (final EFapsException e) {
                        throw new IOException("store resource could not be aborted", e);
                    }
                }
            }
        }

        /**
         * @return 0 if available, else 1
         * @throws IOException on error
         * @see InputStream#available()
         */
        @Override
        public int available()
            throws IOException
        {
            return this.in.available();
        }

        /**
         * @param _readlimit limit to read
         * @see InputStream#mark(int)
         */
        @Override
        public void mark(final int _readlimit)
        {
            this.in.mark(_readlimit);
        }

        /**
         * @return mark suported
         * @see InputStream#markSupported()
         */
        @Override
        public boolean markSupported()
        {
            return this.in.markSupported();
        }

        /**
         * @return readed
         * @throws IOException on error
         * @see InputStream#read()
         */
        @Override
        public int read()
            throws IOException
        {
            return this.in.read();
        }

        /**
         * @param _b byte to read
         * @return d
         * @throws IOException on error
         * @see InputStream#read(byte[])
         */
        @Override
        public int read(final byte[] _b)
            throws IOException
        {
            return this.in.read(_b);
        }

        /**
         * @param _b byte
         * @param _off offset
         * @param _len length
         * @return int
         * @throws IOException on error
         * @see InputStream#read(byte[], int, int)
         */
        @Override
        public int read(final byte[] _b,
                        final int _off,
                        final int _len)
            throws IOException
        {
            return this.in.read(_b, _off, _len);
        }

        /**
         * @throws IOException on error
         * @see InputStream#reset()
         */
        @Override
        public void reset()
            throws IOException
        {
            this.in.reset();
        }

        /**
         * @param _n n to skip
         * @return long
         * @throws IOException on error
         * @see InputStream#skip(long)
         */
        @Override
        public long skip(final long _n)
            throws IOException
        {
            return this.in.skip(_n);
        }
    }
}
