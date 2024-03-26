/*
 * Copyright © 2003 - 2024 The eFaps Team (-)
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
 */
package org.efaps.db.store;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;

import javax.jcr.AccessDeniedException;
import javax.jcr.Binary;
import javax.jcr.InvalidItemStateException;
import javax.jcr.ItemExistsException;
import javax.jcr.LoginException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.ReferentialIntegrityException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.version.VersionException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;

import org.apache.jackrabbit.rmi.value.SerialValueFactory;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.util.EFapsException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Store Resource that uses the Content Repository for Java Technology API (JCR).
 *
 * @author The eFaps Team
 */
public class JCRStoreResource
    extends AbstractStoreResource
{
    /**
     * Name of the table the content is stored in.
     */
    public static final String TABLENAME_STORE = "T_CMGENSTOREJCR";

    /**
     * Name of the column the content is stored in.
     */
    public static final String COLNAME_IDENTIFIER = "IDENTIFIER";

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG  = LoggerFactory.getLogger(JCRStoreResource.class);

    /**
     * Property Name to define if the type if is used to define a sub
     * directory.
     */
    private static final String PROPERTY_WORKSPACENAME = "JCRWorkSpaceName";

    /**
     * Property Name to define if the file will be deleted on deletion of the related object.
     */
    private static final String PROPERTY_BASEFOLDER = "JCRBaseFolder";

    /**
     * Property Name to define if the file will be deleted on deletion of the related object.
     */
    private static final String PROPERTY_ENABLEDELETION = "JCREnableDeletion";

    /**
     * Property Name to define if the file will be deleted on deletion of the related object.
     */
    private static final String PROPERTY_USERNAME = "JCRUserName";

    /**
     * Property Name to define if the file will be deleted on deletion of the related object.
     */
    private static final String PROPERTY_PASSWORD = "JCRPassword";


    /**
     * The repository for this JCR Store Resource.
     */
    private Repository repository;

    /**
     * Identifier for the node to be accessed.
     */
    private String identifier;

    /**
     * Session for JCR access.
     */
    private Session session;

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(final Instance _instance,
                           final Store _store)
        throws EFapsException
    {
        super.initialize(_instance, _store);
        try {
            final InitialContext ctx = new InitialContext();
            repository = (Repository) ctx.lookup(getStore().getProperty(Store.PROPERTY_JNDINAME));
            if (JCRStoreResource.LOG.isDebugEnabled()) {
                final String name = repository.getDescriptor(Repository.REP_NAME_DESC);
                JCRStoreResource.LOG.debug("Successfully retrieved '{}' repository from JNDI", new Object[]{ name });
            }

        } catch (final NamingException e) {
            throw new EFapsException(JCRStoreResource.class, "initialize.NamingException", e);
        }
    }

    /**
     * Gets the session for JCR access.
     *
     * @return the session for JCR access
     * @throws EFapsException on error
     */
    protected Session getSession()
        throws EFapsException
    {
        if (session == null) {
            try {
                String username = getProperties().get(JCRStoreResource.PROPERTY_USERNAME);
                if (username == null) {
                    username = Context.getThreadContext().getPerson().getName();
                }
                String passwd = getProperties().get(JCRStoreResource.PROPERTY_PASSWORD);
                if (passwd == null) {
                    passwd = "efaps";
                }
                session = repository.login(new SimpleCredentials(username, passwd.toCharArray()),
                                getProperties().get(JCRStoreResource.PROPERTY_WORKSPACENAME));
            } catch (final LoginException e) {
                throw new EFapsException(JCRStoreResource.class, "initialize.LoginException", e);
            } catch (final NoSuchWorkspaceException e) {
                throw new EFapsException(JCRStoreResource.class, "initialize.NoSuchWorkspaceException", e);
            } catch (final RepositoryException e) {
                throw new EFapsException(JCRStoreResource.class, "initialize.RepositoryException", e);
            }
        }
        return session;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int add2Select(final SQLSelect _select)
    {
        _select.column(2, "ID").column(2, JCRStoreResource.COLNAME_IDENTIFIER)
            .leftJoin(JCRStoreResource.TABLENAME_STORE, 2, "ID", 0, "ID");
        return 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void getAdditionalInfo(final ResultSet _rs)
        throws SQLException
    {
        final String identiferTmp = _rs.getString(6);
        if (identiferTmp != null && !identiferTmp.isEmpty()) {
            identifier = identiferTmp.trim();
        }
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
                Context.getDbType().newInsert(JCRStoreResource.TABLENAME_STORE, "ID", false)
                                .column("ID", getGeneralID())
                                .column(JCRStoreResource.COLNAME_IDENTIFIER, "NEW")
                                .execute(res);
            } catch (final SQLException e) {
                throw new EFapsException(JCRStoreResource.class, "insertDefaults", e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long write(final InputStream _in,
                      final long _size,
                      final String _fileName)
        throws EFapsException
    {
        long size = _size;
        try {
            final Binary bin = getBinary(_in);
            final Node resNode;
            if (identifier == null) {
                final Node fileNode = getFolderNode().addNode(getInstance().getOid(), NodeType.NT_FILE);
                setIdentifer(fileNode.getIdentifier());
                resNode = fileNode.addNode(Property.JCR_CONTENT, NodeType.NT_RESOURCE);
            } else {
                final Node fileNode = getSession().getNodeByIdentifier(identifier);
                resNode = fileNode.getNode(Property.JCR_CONTENT);
            }
            resNode.setProperty(Property.JCR_DATA, bin);
            resNode.setProperty(Property.JCR_ENCODING, "UTF-8");
            resNode.setProperty(Property.JCR_LAST_MODIFIED, Calendar.getInstance());
            resNode.setProperty(Property.JCR_LAST_MODIFIED_BY, Context.getThreadContext().getPerson().getName());
            // if size is unkown!
            if (size < 0)  {
                final byte[] buffer = new byte[1024];
                int length = 1;
                size = 0;
                final OutputStream out = new ByteArrayOutputStream();
                while (length > 0)  {
                    length = _in.read(buffer);
                    if (length > 0)  {
                        out.write(buffer, 0, length);
                        size += length;
                    }
                }
            }
        } catch (final RepositoryException e) {
            throw new EFapsException(JCRStoreResource.class, "write.RepositoryException", e);
        } catch (final IOException e) {
            throw new EFapsException(JCRStoreResource.class, "write.IOException", e);
        }
        setFileInfo(_fileName, size);
        return size;
    }

    /**
     * Gets the folder node.
     *
     * @return the folder node
     * @throws EFapsException on error
     * @throws RepositoryException the repository exception
     */
    protected Node getFolderNode()
        throws EFapsException, RepositoryException
    {
        Node ret = getSession().getRootNode();
        if (getProperties().containsKey(JCRStoreResource.PROPERTY_BASEFOLDER)) {
            if (ret.hasNode(getProperties().get(JCRStoreResource.PROPERTY_BASEFOLDER))) {
                ret = ret.getNode(getProperties().get(JCRStoreResource.PROPERTY_BASEFOLDER));
            } else {
                ret = ret.addNode(getProperties().get(JCRStoreResource.PROPERTY_BASEFOLDER), NodeType.NT_FOLDER);
            }
        }
        final String subFolder = new DateTime().toString("yyyy-MM");
        if (ret.hasNode(subFolder)) {
            ret = ret.getNode(subFolder);
        } else {
            ret = ret.addNode(subFolder, NodeType.NT_FOLDER);
        }
        return ret;
    }

    /**
     * Gets the binary.
     *
     * @param _in the in
     * @return the binary
     * @throws EFapsException on error
     */
    protected Binary getBinary(final InputStream _in)
        throws EFapsException
    {
        Binary ret = null;
        try {
            ret = SerialValueFactory.getInstance().createBinary(_in);
        } catch (final RepositoryException e) {
            throw new EFapsException("RepositoryException", e);
        }
        return ret;
    }


    /**
     * Set the identifier in the eFaps DataBase.
     * @param _identifier   identifer to set
     * @throws EFapsException on error
     */
    protected void setIdentifer(final String _identifier)
        throws EFapsException
    {
        if (!_identifier.equals(identifier)) {

            ConnectionResource res = null;
            try {
                res = Context.getThreadContext().getConnectionResource();

                final StringBuffer cmd = new StringBuffer().append("update ")
                                .append(JCRStoreResource.TABLENAME_STORE).append(" set ")
                                .append(JCRStoreResource.COLNAME_IDENTIFIER).append("=? ")
                                .append("where ID =").append(getGeneralID());

                final PreparedStatement stmt = res.prepareStatement(cmd.toString());
                try {
                    stmt.setString(1, _identifier);
                    stmt.execute();
                } finally {
                    stmt.close();
                }
                identifier = _identifier;
            } catch (final EFapsException e) {
                throw e;
            } catch (final SQLException e) {
                throw new EFapsException(JDBCStoreResource.class, "write.SQLException", e);
            }
        }
    }

    /**
     * A JCR Store resource does not use compression from eFaps Side.
     * @return Compress.NONE
     */
    @Override
    protected Compress getCompress()
    {
        return Compress.NONE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream read()
        throws EFapsException
    {
        InputStream input = null;
        try {
            if (identifier != null) {
                final Node fileNode = getSession().getNodeByIdentifier(identifier);
                final Node resNode = fileNode.getNode(Property.JCR_CONTENT);
                final Property data = resNode.getProperty(Property.JCR_DATA);
                final Binary bin = data.getBinary();
                input = new JCRStoreResourceInputStream(this, bin);
            }
        } catch (final RepositoryException e) {
            throw new EFapsException(JCRStoreResource.class, "read.RepositoryException", e);
        } catch (final IOException e) {
            throw new EFapsException(JCRStoreResource.class, "read.IOException", e);
        }
        return input;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete()
        throws EFapsException
    {
        // only delete if it actually exists and deletion is configured
        if (getExist()[0] && getExist()[1]
                        && "TRUE".equalsIgnoreCase(getProperties().get(JCRStoreResource.PROPERTY_ENABLEDELETION))) {
            try {
                final Node fileNode = getSession().getNodeByIdentifier(identifier);
                fileNode.remove();
            } catch (final RepositoryException e) {
                throw new EFapsException(JCRStoreResource.class, "delete.RepositoryException", e);
            }
        }
    }

    /**
     * The method is called from the transaction manager if the complete
     * transaction is completed.<br/>
     *
     * @param _xid      global transaction identifier (not used, because each
     *                  file with the file id gets a new VFS store resource
     *                  instance)
     * @param _onePhase <i>true</i> if it is a one phase commitment transaction
     *                  (not used)
     * @throws XAException if any exception occurs (catch on
     *         {@link java.lang.Throwable})
     */
    @Override
    public void commit(final Xid _xid,
                       final boolean _onePhase)
        throws XAException
    {
        try {
            if (getSession().hasPendingChanges()) {
                getSession().save();
            }
            getSession().logout();
        } catch (final AccessDeniedException e) {
            throw new XAException("AccessDeniedException");
        } catch (final ItemExistsException e) {
            throw new XAException("ItemExistsException");
        } catch (final ReferentialIntegrityException e) {
            throw new XAException("ReferentialIntegrityException");
        } catch (final ConstraintViolationException e) {
            throw new XAException("AccessDeniedException");
        } catch (final InvalidItemStateException e) {
            throw new XAException("InvalidItemStateException");
        } catch (final VersionException e) {
            throw new XAException("VersionException");
        } catch (final LockException e) {
            throw new XAException(XAException.XA_RBDEADLOCK);
        } catch (final NoSuchNodeTypeException e) {
            throw new XAException("NoSuchNodeTypeException");
        } catch (final RepositoryException e) {
            throw new XAException("RepositoryException");
        } catch (final EFapsException e) {
            throw new XAException("RepositoryException");
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
        if (JCRStoreResource.LOG.isDebugEnabled()) {
            JCRStoreResource.LOG.debug("forget (xid = " + _xid + ")");
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
        if (JCRStoreResource.LOG.isDebugEnabled()) {
            JCRStoreResource.LOG.debug("getTransactionTimeout");
        }
        return 0;
    }

    /**
     * Ask the resource manager to prepare for a transaction commit of the
     * transaction specified in xid (used for 2-phase commits).
     *
     * @param _xid Xid
     * @return always 0, because not 2 phase commit
     */
    @Override
    public int prepare(final Xid _xid)
    {
        if (JCRStoreResource.LOG.isDebugEnabled())  {
            JCRStoreResource.LOG.debug("prepare (xid=" + _xid + ")");
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
        if (JCRStoreResource.LOG.isDebugEnabled()) {
            JCRStoreResource.LOG.debug("recover (flag = " + _flag + ")");
        }
        return null;
    }

    /**
     * On rollback no save is send to the session..
     *
     * @param _xid      global transaction identifier (not used, because each
     *                  file with the file id gets a new VFS store resource
     *                  instance)
     * @throws XAException if any exception occurs (catch on
     *         {@link java.lang.Throwable})
     */
    @Override
    public void rollback(final Xid _xid)
        throws XAException
    {
        try {
            getSession().logout();
        } catch (final EFapsException e) {
            throw new XAException("EFapsException");
        }
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
        if (JCRStoreResource.LOG.isDebugEnabled()) {
            JCRStoreResource.LOG.debug("setTransactionTimeout (seconds = " + _seconds + ")");
        }
        return true;
    }

    /**
     * ResourceInputStream implementation.
     */
    private class JCRStoreResourceInputStream
        extends StoreResourceInputStream
    {

        /**
         * @param _store    Strore this InputStream belongs to
         * @param _bin      Binary
         * @throws IOException on error
         * @throws RepositoryException  on error
         */
        protected JCRStoreResourceInputStream(final AbstractStoreResource _store,
                                              final Binary _bin)
            throws IOException, RepositoryException
        {
            super(_store, _bin.getStream());
        }
    }
}
