/*
 * Copyright 2003-2007 The eFaps Team
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

import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.efaps.db.Context;
import org.efaps.util.EFapsException;

/**
 * Abstract class used to given an easy interface to implemented XA resources
 * within the eFaps application.<br/>
 * To use the implementation of such resource, it must be called first method
 * {@link #open}. To free a resource of using, methods {@link #commit} (if all
 * work was OK) or {@link #abort} (if the transaction must be rolled back) must
 * be called.
 *
 * @author tmo
 * @version $Id$
 */
abstract class AbstractResource implements XAResource {

  /////////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used in this class.
   */
  private static Logger LOG = LoggerFactory.getLogger(AbstractResource.class);

  /////////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Stores the eFaps context which uses this connection resource.
   */
  private Context context = null;

  /**
   * Is set to <i>true</i> if the connection resource is already enlisted in
   * the transaction. Otherwise the value is <i>false</i>.
   */
  private boolean opened = false;

  /////////////////////////////////////////////////////////////////////////////
  // constructors / destructors

  /**
   * Constructor used to set the instance variable {@link #context}.
   *
   * @param _context  eFaps context
   * @see #context
   */
  protected AbstractResource(final Context _context)  {
    this.context = _context;
  }

  /////////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * Opens this connection resource and enlisted this resource in the
   * transaction.
   *
   * @throws EFapsException if the resource is already opened or this
   *         resource could not be enlisted
   */
  public void open() throws EFapsException  {
    if (LOG.isDebugEnabled())  {
      LOG.debug("open");
    }
    if (this.opened)  {
      LOG.error("resource already opened");
      throw new EFapsException(AbstractResource.class, "open.AlreadyOpened");
    }
    try  {
      getContext().getTransaction().enlistResource(this);
    } catch (RollbackException e)  {
      LOG.error("exception occurs while delisting in transaction, "
                                                + "commit not possible", e);
      throw new EFapsException(AbstractResource.class,
                                                "open.RollbackException", e);
    } catch (SystemException e)  {
      LOG.error("exception occurs while delisting in transaction, "
                                                + "commit not possible", e);
      throw new EFapsException(AbstractResource.class,
                                                "open.SystemException", e);
    }
    this.opened = true;
  }

  /**
   * Closes this connection resource and delisted this resource in the
   * transaction.
   * The method must be called if the transaction should be commited.
   *
   * @throws EFapsException if the resource is not opened or this resource
   *         could not delisted
   */
  public void commit() throws EFapsException  {
    if (LOG.isDebugEnabled())  {
      LOG.debug("commit");
    }
    if (!this.opened)  {
      LOG.error("resource not opened, commit not possible");
      throw new EFapsException(AbstractResource.class, "commit.NotOpened");
    }
    try  {
      getContext().getTransaction().delistResource(this, TMSUCCESS);
    } catch (SystemException e)  {
      LOG.error("exception occurs while delisting in transaction, "
                                                + "commit not possible", e);
      throw new EFapsException(AbstractResource.class,
                                              "commit.SystemException", e);
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
   *         could not delisted
   */
  public void abort() throws EFapsException  {
    if (LOG.isDebugEnabled())  {
      LOG.debug("abort");
    }
    if (!this.opened)  {
      throw new EFapsException(AbstractResource.class, "abort.NotOpened");
    }
    try  {
      getContext().getTransaction().delistResource(this, TMFAIL);
      getContext().abort();
    } catch (SystemException e)  {
      throw new EFapsException(AbstractResource.class,
                                                  "abort.SystemException", e);
    }
    freeResource();
    this.opened = false;
  }

  /**
   * Method used to free this resource in the eFaps context object (so that the
   * resource instance could be reused).
   */
  protected abstract void freeResource();

  /**
   * This is the getter method for instance variable {@link #context}.
   *
   * @return value of instance variable {@link #context}
   * @see #context
   */
  public final Context getContext()  {
    return this.context;
  }

  /**
   * This is the getter method for instance variable {@link #opened}.
   *
   * @return <code>true</code> if this resource is open, otherwise
   *         <code>false</code> is returned.
   * @see #opened
   */
  public final boolean isOpened()  {
    return this.opened;
  }

  /////////////////////////////////////////////////////////////////////////////
  // all further methods are implementing javax.transaction.xa.XAResource

  /**
   * The method starts work on behalf of a transaction branch specified in
   * parameter <code>_xid</code>. Normally nothing must be done, because the
   * pre-work is already done in the contructor (and an instance of a resource
   * is only defined for one transaction).
   *
   * @param _xid      global transaction identifier
   */
  public void start(final Xid _xid, final int _flags) throws XAException  {
    if (LOG.isDebugEnabled())  {
      LOG.debug("start resource " + _xid + ", flags = " + _flags);
    }
  }

  /**
   * The method ends the work performed on behalf of a transaction branch.
   * Normally nothing must be done, because an instance of a resource is only
   * defined for one transaction.
   *
   * @param _xid      global transaction identifier
   */
  public void end(final Xid _xid, final int _flags) throws XAException  {
    if (LOG.isDebugEnabled())  {
      LOG.debug("end resource " + _xid + ", flags = " + _flags);
    }
  }

  /**
   * The method is called from the transaction (manager) to check if the XA
   * resource is the same as the given XA resource in the parameter
   * <code>_xaras</code>. This is done with a string compare
   * (method {@link java.lang.String#equals}) off the
   * {@link #java.lang.Object#toString} methods of this and the XA resource in
   * <code>_xaras</code>.
   *
   * @param _xares    XA resource used to test if same resource
   * @return <code>true</code> if the string compare returns that they are
   *         equal, otherwise <code>false</code> is returned
   * @see {@link javax.transaction.xa.XAResource#isSameRM}
   */
  public boolean isSameRM(final XAResource _xares)  {
    if (LOG.isDebugEnabled())  {
      LOG.debug("is Same RM " + _xares.toString().equals(this.toString()));
    }
    return _xares.toString().equals(this.toString());
  }
}
