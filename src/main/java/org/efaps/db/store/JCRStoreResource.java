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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Map;

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

import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Store Resource that uses the Content Repository for Java Technology API (JCR).
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class JCRStoreResource
    extends AbstractStoreResource
{
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
                           final Map<String, String> _properties,
                           final Compress _compress)
        throws EFapsException
    {
        super.initialize(_instance, _properties, _compress);
        this.identifier = _instance.getOid();
        try {
            final InitialContext ctx = new InitialContext();
            this.repository = (Repository) ctx.lookup(_properties.get(Store.PROPERTY_JNDINAME));
            if (JCRStoreResource.LOG.isDebugEnabled()) {
                final String name = this.repository.getDescriptor(Repository.REP_NAME_DESC);
                JCRStoreResource.LOG.debug("Successfully retrieved '%s' repository from JNDI", new Object[]{ name });
            }
            this.session = this.repository.login(new SimpleCredentials("username", "password".toCharArray()),
                            getProperties().get(JCRStoreResource.PROPERTY_WORKSPACENAME));
        } catch (final NamingException e) {
            throw new EFapsException(JCRStoreResource.class, "initialize.NamingException", e);
        } catch (final LoginException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final NoSuchWorkspaceException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final RepositoryException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /* (non-Javadoc)
     * @see org.efaps.db.store.AbstractStoreResource#add2Select(org.efaps.db.wrapper.SQLSelect)
     */
    @Override
    protected int add2Select(final SQLSelect _select)
    {
        return 0;
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
        final JCRBinary bin = new JCRBinary(_in);
        long size = _size;
        try {
            final Node rootNode = this.session.getRootNode();
            final Node fileNode = rootNode.addNode(this.identifier, NodeType.NT_FILE);
            final Node resNode = fileNode.addNode(Property.JCR_CONTENT, NodeType.NT_RESOURCE);
            resNode.setProperty(Property.JCR_DATA, bin);
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
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return size;
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
            //this.session.getNodeByIdentifier(id)
            final Node rootNode = this.session.getRootNode();
            final Node fileNode = rootNode.getNode(this.identifier);
            final Node resNode = fileNode.getNode(Property.JCR_CONTENT);
            final Property data = resNode.getProperty(Property.JCR_DATA);
            final Binary bin = data.getBinary();
            input = new JCRStoreResourceInputStream(this, bin);
        } catch (final RepositoryException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final IOException e) {
             // TODO Auto-generated catch block
            e.printStackTrace();
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
        try {
            final Node rootNode = this.session.getRootNode();
            final Node fileNode = rootNode.getNode(this.identifier);
            fileNode.remove();
        } catch (final RepositoryException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
            if (this.session.hasPendingChanges()) {
                this.session.save();
            }
            this.session.logout();
        } catch (final AccessDeniedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final ItemExistsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final ReferentialIntegrityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final ConstraintViolationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final InvalidItemStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final VersionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final LockException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final NoSuchNodeTypeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final RepositoryException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
        this.session.logout();
    }

    /**
     * Sets the current transaction timeout value for this XAResource instance.
     *
     * @param _seconds number of seconds
     * @return always <i>true</i>
     */
    public boolean setTransactionTimeout(final int _seconds)
    {
        if (JCRStoreResource.LOG.isDebugEnabled()) {
            JCRStoreResource.LOG.debug("setTransactionTimeout (seconds = " + _seconds + ")");
        }
        return true;
    }


    /**
     * Implementation of Binary from JCR.
     */
    private static class JCRBinary
        implements Binary
    {

        /**
         * The InpuStrema this Binary belongs to.
         */
        private InputStream stream;

        /**
         * @param _in InputStream
         */
        public JCRBinary(final InputStream _in)
        {
            this.stream = _in;
        }


        /* (non-Javadoc)
         * @see javax.jcr.Binary#getStream()
         */
        @Override
        public InputStream getStream()
            throws RepositoryException
        {
            return this.stream;
        }

        /* (non-Javadoc)
         * @see javax.jcr.Binary#read(byte[], long)
         */
        @Override
        public int read(final byte[] _b,
                        final long _position)
            throws IOException, RepositoryException
        {
            return this.stream.read(_b);
        }

        /* (non-Javadoc)
         * @see javax.jcr.Binary#getSize()
         */
        @Override
        public long getSize()
            throws RepositoryException
        {
            return 0;
        }

        /* (non-Javadoc)
         * @see javax.jcr.Binary#dispose()
         */
        @Override
        public void dispose()
        {
            // TODO Auto-generated method stub
            try {
                this.stream.close();
            } catch (final IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

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
