/*
 * Copyright 2006 The eFaps Team
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

package org.efaps.db;

import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.efaps.admin.access.AccessTypeEnums;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.transaction.StoreResource;
import org.efaps.util.EFapsException;

/**
 * The class is used to checkout a file from a given attribute of an object.
 *
 * @author tmo
 * @version $Id$
 */
public class Checkout extends AbstractAction  {

  /////////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used in this class.
   */
  private static final Log LOG = LogFactory.getLog(Checkout.class);

  /////////////////////////////////////////////////////////////////////////////
  // instance variables

  /**
   * Instance holding the oid of the object which is checked out.
   *
   * @see #getInstance
   */
  private final Instance instance;

  /**
   * Buffer used to copy from the input stream to the output stream.
   *
   * @see #execute
   */
  private byte[] buffer = new byte[1024];

  /**
   * Stores the file name after pre processing.
   *
   * @see #preprocess
   * @see #getFileName
   */
  private String fileName = null;

  /////////////////////////////////////////////////////////////////////////////
  // constructors

  /**
   * Constructor with object id as string.
   *
   * @param _context  eFaps context for this request
   * @param _oid      oid of object on which the checkout is made
   * @param _attrName name of the attribute where the blob is in
   * @todo rewrite to thrown EFapsException
   */
  public Checkout(final String _oid)  {
    this(new Instance(_oid));
  }

  /**
   * Constructor with instance object.
   *
   * @param _context  eFaps context for this request
   * @param _instance instance on which the checkout is made
   * @param _attrName name of the attribute where the blob is in
   */
  public Checkout(final Instance _instance)  {
    this.instance = _instance;
  }

  /////////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * The method is only a dummy method and closes the checkout action. The
   * method should be called, if in the future the checkout class needs a
   * call to this method.
   */
  public void close()  {
  }

  /**
   *
   */
  public void preprocess() throws Exception  {
    Context context = Context.getThreadContext();

    Type type = getInstance().getType();
    String fileName = type.getProperty(PROPERTY_STORE_ATTR_FILE_NAME);

    SearchQuery query = new SearchQuery();
    query.setObject(context, getInstance());
    query.addSelect(context, fileName);
//    try  {
      query.executeWithoutAccessCheck();
      if (query.next())  {
        Object value = query.get(context, fileName);
        this.fileName = value.toString();
      }
//    } finally  {
      query.close();
//    }
  }

  /////////////////////////////////////////////////////////////////////////////
  // output stream methods

  /**
   * Executes the checkout with an output stream.
   *
   * @param _out      output stream where to write the file
   * @throws EFapsException if the current context user has now access to 
   *         checkout the file out of the eFaps object
   */
  public void execute(final OutputStream _out) throws EFapsException  {
    boolean hasAccess = this.instance.getType()
          .hasAccess(this.instance, 
                     AccessTypeEnums.CHECKOUT.getAccessType());
    if (!hasAccess)  {
      throw new EFapsException(getClass(), "execute.NoAccess");
    }
    executeWithoutAccessCheck(_out);
  }

  /**
   * Executes the checkout without an access check for output streams.
   *
   * @param _out      output stream where to write the file
   * @throws EFapsException if checkout action fails
   */
  public void executeWithoutAccessCheck(final OutputStream _out) 
                                                      throws EFapsException  {
    Context context = Context.getThreadContext();
    StoreResource store = null;
    try  {
      store = context.getStoreResource(getInstance().getType(), 
                                       getInstance().getId());
      store.read(_out);
      store.commit();
    } catch (EFapsException e)  {
      LOG.error("could not checkout " + this.instance, e);
      throw e;
    } catch (Throwable e)  {
      LOG.error("could not checkout " + this.instance, e);
      throw new EFapsException(getClass(), 
                               "executeWithoutAccessCheck.Throwable", e);
    } finally  {
      if ((store != null) && store.isOpened())  {
        store.abort();
      }
    }
  }

  /////////////////////////////////////////////////////////////////////////////
  // input stream methods

  /**
   * Executes the checkout and returns an input stream by calling method
   * {@link #executeWithoutAccessCheck()}.
   *
   * @return input stream of the checked in file
   * @throws EFapsException if the current context user has now access to 
   *         checkout the file out of the eFaps object
   * @see #executeWithoutAccessCheck()
   */
  public InputStream execute() throws EFapsException  {
    boolean hasAccess = this.instance.getType()
          .hasAccess(this.instance, 
                     AccessTypeEnums.CHECKOUT.getAccessType());
    if (!hasAccess)  {
      throw new EFapsException(getClass(), "execute.NoAccess");
    }
    return executeWithoutAccessCheck();
  }

  /**
   * Executes the checkout without an access check and returns an input streams
   * of the checked in file. The returned input stream must be closed, because
   * the returned inputs stream also commit the store resource. Otherwise the
   * transaction is rolled back!
   *
   * @param _out      output stream where to write the file
   * @throws EFapsException if checkout action fails
   */
  public InputStream executeWithoutAccessCheck()  throws EFapsException  {
    Context context = Context.getThreadContext();
    StoreResource store = null;
    InputStream in = null;
    try  {
      store = context.getStoreResource(getInstance().getType(), 
                                       getInstance().getId());
      in = store.read();
    } catch (EFapsException e)  {
      LOG.error("could not checkout " + this.instance, e);
      throw e;
    } catch (Throwable e)  {
      LOG.error("could not checkout " + this.instance, e);
      throw new EFapsException(getClass(), 
                               "executeWithoutAccessCheck.Throwable", e);
    } finally  {
      if ((in == null) && (store != null) && store.isOpened())  {
        store.abort();
      }
    }
    return in;
  }

  /////////////////////////////////////////////////////////////////////////////
  // instance getter and setter methods

  /**
   * This is the getter method for instance variable {@link #instance}.
   *
   * @return value of instance variable {@link #instance}
   * @see #instance
   */
  protected Instance getInstance()  {
    return this.instance;
  }

  /**
   * This is the getter method for instance variable {@link #fileName}.
   *
   * @return the fileName of the instance variable {@link #fileName}.
   * @see #fileName
   * @see #setFileName
   */
  public String getFileName()  {
    return this.fileName;
  }
}