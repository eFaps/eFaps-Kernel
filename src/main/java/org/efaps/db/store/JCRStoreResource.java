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

import java.io.InputStream;
import java.util.Map;

import javax.jcr.Repository;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;

import org.apache.jackrabbit.rmi.repository.RMIRemoteRepository;
import org.efaps.db.Instance;
import org.efaps.util.EFapsException;

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

            final Context context = (Context) ctx.lookup("java:comp/env");
            final NamingEnumeration<NameClassPair> nameEnum = context.list("");
            while (nameEnum.hasMoreElements()) {
                final NameClassPair namePair = nameEnum.next();
                if (namePair.getName().equals(_properties.get(Store.PROPERTY_JNDINAME))) {
                    this.repository = (Repository) context.lookup(_properties.get(Store.PROPERTY_JNDINAME));
                    break;
                }
            }
            this.repository = new RMIRemoteRepository("//localhost/jackrabbit.repository");
        } catch (final NamingException e) {
            throw new EFapsException(JCRStoreResource.class, "initialize.NamingException", e);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.efaps.db.store.Resource#write(java.io.InputStream, int)
     */
    @Override
    public int write(final InputStream _in,
                     final int _size)
        throws EFapsException
    {

        return 0;
    }

    /*
     * (non-Javadoc)
     * @see org.efaps.db.store.Resource#read()
     */
    @Override
    public InputStream read()
        throws EFapsException
    {

        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.efaps.db.store.Resource#delete()
     */
    @Override
    public void delete()
        throws EFapsException
    {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * @see javax.transaction.xa.XAResource#commit(javax.transaction.xa.Xid, boolean)
     */
    @Override
    public void commit(final Xid _xid,
                       final boolean _onePhase)
        throws XAException
    {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * @see javax.transaction.xa.XAResource#forget(javax.transaction.xa.Xid)
     */
    @Override
    public void forget(final Xid _xid)
        throws XAException
    {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * @see javax.transaction.xa.XAResource#getTransactionTimeout()
     */
    @Override
    public int getTransactionTimeout()
        throws XAException
    {
        // TODO Auto-generated method stub
        return 0;
    }

    /*
     * (non-Javadoc)
     * @see javax.transaction.xa.XAResource#prepare(javax.transaction.xa.Xid)
     */
    @Override
    public int prepare(final Xid _xid)
        throws XAException
    {
        // TODO Auto-generated method stub
        return 0;
    }

    /*
     * (non-Javadoc)
     * @see javax.transaction.xa.XAResource#recover(int)
     */
    @Override
    public Xid[] recover(final int _flag)
        throws XAException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see javax.transaction.xa.XAResource#rollback(javax.transaction.xa.Xid)
     */
    @Override
    public void rollback(final Xid _xid)
        throws XAException
    {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * @see javax.transaction.xa.XAResource#setTransactionTimeout(int)
     */
    @Override
    public boolean setTransactionTimeout(final int _seconds)
        throws XAException
    {
        // TODO Auto-generated method stub
        return false;
    }

}
