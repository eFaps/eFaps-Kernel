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

import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.efaps.db.Context;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class used to given an easy interface to implemented XA resources
 * within the eFaps application.<br/>
 * To use the implementation of such resource, it must be called first method
 * {@link #open}. To free a resource of using, methods {@link #commit} (if all
 * work was OK) or {@link #abort} (if the transaction must be rolled back) must
 * be called.
 *
 * @author The eFaps Team
 *
 */
public abstract class AbstractResource
    implements XAResource
{
    /**
     * Logging instance used in this class.
     */
    private static Logger LOG = LoggerFactory.getLogger(AbstractResource.class);

    /**
     * Is set to <i>true</i> if the connection resource is already enlisted in
     * the transaction. Otherwise the value is <i>false</i>.
     */
    private boolean opened = false;

    /**
     * Opens this connection resource and enlisted this resource in the
     * transaction.
     *
     * @throws EFapsException if the resource is already opened or this
     *                        resource could not be enlisted
     */
    public void open() throws EFapsException
    {
        AbstractResource.LOG.debug("open resource:{}", this);
        if (this.opened)  {
            AbstractResource.LOG.error("resource already opened");
            throw new EFapsException(AbstractResource.class, "open.AlreadyOpened");
        }
        try  {
            final Context context = Context.getThreadContext();
            context.getTransaction().enlistResource(this);
        } catch (final RollbackException e)  {
            AbstractResource.LOG.error("exception occurs while delisting in transaction, "
                                                + "commit not possible", e);
            throw new EFapsException(AbstractResource.class,
                                     "open.RollbackException", e);
        } catch (final SystemException e)  {
            AbstractResource.LOG.error("exception occurs while delisting in transaction, "
                                                + "commit not possible", e);
            throw new EFapsException(AbstractResource.class,
                                     "open.SystemException", e);
        }
        this.opened = true;
    }

    /**
     * Closes this connection resource and delisted this resource in the
     * transaction. The method must be called if the transaction should be
     * commited.
     *
     * @throws EFapsException if the resource is not opened or this resource
     *                        could not delisted
     */
    public void commit() throws EFapsException
    {
        AbstractResource.LOG.debug("commit resource:{}", this);
        if (!this.opened)  {
            AbstractResource.LOG.error("resource not opened, commit not possible");
            throw new EFapsException(AbstractResource.class, "commit.NotOpened");
        }
        try  {
            final Context context = Context.getThreadContext();
            context.getTransaction().delistResource(this, XAResource.TMSUCCESS);
        } catch (final SystemException e)  {
            AbstractResource.LOG.error("exception occurs while delisting in transaction, "
                                                + "commit not possible", e);
            throw new EFapsException(AbstractResource.class, "commit.SystemException", e);
        }
        freeResource();
        this.opened = false;
    }

    /**
     * Closes this XA resource and delisted this resource in the transaction.
     * <br/>
     * The method must be called if the transaction should be aborted (rolled
     * back).
     *
     * @throws EFapsException if the resource is not opened or this resource
     *                        could not delisted
     */
    public void abort()
        throws EFapsException
    {
        AbstractResource.LOG.debug("abort resource:{}", this);
        if (!this.opened)  {
            throw new EFapsException(AbstractResource.class, "abort.NotOpened");
        }
        try  {
            final Context context = Context.getThreadContext();
            context.getTransaction().delistResource(this, XAResource.TMFAIL);
            context.abort();
        } catch (final SystemException e)  {
            throw new EFapsException(AbstractResource.class, "abort.SystemException", e);
        }
        freeResource();
        this.opened = false;
    }

    /**
     * Method used to free this resource in the eFaps context object (so that
     * the resource instance could be reused).
     */
    protected abstract void freeResource();

    /**
     * This is the getter method for instance variable {@link #opened}.
     *
     * @return <code>true</code> if this resource is open, otherwise
     *         <code>false</code> is returned.
     * @see #opened
     */
    public final boolean isOpened()
    {
        return this.opened;
    }

    /**
     * The method starts work on behalf of a transaction branch specified in
     * parameter <code>_xid</code>. Normally nothing must be done, because the
     * pre-work is already done in the contructor (and an instance of a
     * resource is only defined for one transaction).
     *
     * @param _xid      global transaction identifier
     * @param _flags    flags
     */
    public void start(final Xid _xid,
                      final int _flags)
    {
        AbstractResource.LOG.trace("start resource {}, flags {}" + _flags, _xid, _flags);
    }

    /**
     * The method ends the work performed on behalf of a transaction branch.
     * Normally nothing must be done, because an instance of a resource is only
     * defined for one transaction.
     *
     * @param _xid      global transaction identifier
     * @param _flags    flags
     */
    public void end(final Xid _xid,
                    final int _flags)
    {
        AbstractResource.LOG.trace("end resource {}, flags {}" + _flags, _xid, _flags);
    }

    /**
     * The method is called from the transaction (manager) to check if the XA
     * resource is the same as the given XA resource in the parameter
     * <code>_xaras</code>. This is done with a string compare
     * (method {@link String#equals(Object)}) off the
     * {@link Object#toString()} methods of this and the XA resource in
     * <code>_xaras</code>.
     *
     * @param _xares    XA resource used to test if same resource
     * @return <code>true</code> if the string compare returns that they are
     *         equal, otherwise <code>false</code> is returned
     * @see XAResource#isSameRM(XAResource)
     */
    public boolean isSameRM(final XAResource _xares)
    {
        final boolean ret = _xares.toString().equals(toString());
        AbstractResource.LOG.trace("is Same RM: {}", ret);
        return ret;
    }
}
