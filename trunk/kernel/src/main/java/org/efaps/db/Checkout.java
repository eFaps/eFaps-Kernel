/*
 * Copyright 2003 - 2009 The eFaps Team
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
import static org.efaps.db.store.Store.PROPERTY_ATTR_FILE_LENGTH;
import static org.efaps.db.store.Store.PROPERTY_ATTR_FILE_NAME;

import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.efaps.admin.access.AccessTypeEnums;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.event.EventType;
import org.efaps.db.store.Resource;
import org.efaps.util.EFapsException;

/**
 * The class is used to checkout a file from a given attribute of an object.
 *
 * @author tmo
 * @version $Id$
 */
public class Checkout extends AbstractAction {

  // ///////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used in this class.
   */
  private static final Logger LOG = LoggerFactory.getLogger(Checkout.class);

  /**
   * Stores the file name after pre processing.
   *
   * @see #preprocess
   * @see #getFileName
   */
  private String fileName = null;

  /**
   * length of the file in byte.
   */
  private long fileLength;

  /**
   * Constructor with object id as string.
   *
   * @param _oid        oid of object on which the checkout is made
   * @todo rewrite to thrown EFapsException
   */
  public Checkout(final String _oid) {
    this(Instance.get(_oid));
  }

  /**
   * Constructor with instance object.
   *
   * @param _instance  instance on which the checkout is made
   */
  public Checkout(final Instance _instance) {
    super.setInstance(_instance);
  }

  /**
   * The method is only a dummy method and closes the checkout action. The
   * method should be called, if in the future the checkout class needs a call
   * to this method.
   */
  public void close() {
  }

  /**
   * @throws Exception on error
   */
  public void preprocess() throws Exception {

    final Type type = getInstance().getType();
    final String fileNameTmp = type.getProperty(PROPERTY_ATTR_FILE_NAME);
    final String size = type.getProperty(PROPERTY_ATTR_FILE_LENGTH);

    final SearchQuery query = new SearchQuery();
    query.setObject(getInstance());
    query.addSelect(fileNameTmp);
    query.addSelect(size);
    // try {
    query.executeWithoutAccessCheck();
    if (query.next()) {
      final Object value = query.get(fileNameTmp);
      if (value != null) {
        this.fileName = value.toString();
        final Long filelength = (Long) query.get(size);
        this.fileLength = filelength;
      }
    }
    // } finally {
    query.close();
    // }
  }

  /**
   * Executes the checkout with an output stream.
   *
   * @param _out
   *                output stream where to write the file
   * @throws EFapsException
   *                 if the current context user has now access to checkout the
   *                 file out of the eFaps object
   */
  public void execute(final OutputStream _out) throws EFapsException {
    final boolean hasAccess =
        super.getInstance().getType().hasAccess(super.getInstance(),
            AccessTypeEnums.CHECKOUT.getAccessType());
    if (!hasAccess) {
      throw new EFapsException(getClass(), "execute.NoAccess");
    }
    executeWithoutAccessCheck(_out);
  }

  /**
   * Executes the checkout for output streams without checking the access rights
   * (but with triggers).
   * <ol>
   * <li>executes the pre checkout trigger (if exists)</li>
   * <li>executes the checkout trigger (if exists)</li>
   * <li>executes if no checkout trigger exists or the checkout trigger is not
   * executed the update ({@see #executeWithoutTrigger})</li>
   * <li>executes the post checkout trigger (if exists)</li>
   * </ol>
   *
   * @param _out output stream where to write the file
   * @throws EFapsException if checkout action fails
   */
  public void executeWithoutAccessCheck(final OutputStream _out)
      throws EFapsException {
    executeEvents(EventType.CHECKOUT_PRE);
    if (!executeEvents(EventType.CHECKOUT_OVERRIDE)) {
      executeWithoutTrigger(_out);
    }
    executeEvents(EventType.CHECKOUT_POST);
  }

  /**
   * Executes the checkout for output streams without checking the access rights
   * and without triggers.
   *
   * @param _out  output stream where to write the file
   * @throws EFapsException if checkout action fails
   */
  public void executeWithoutTrigger(final OutputStream _out)
      throws EFapsException {
    Resource storeRsrc = null;
    try {
      storeRsrc = Context.getThreadContext().getStoreResource(getInstance());
      storeRsrc.read(_out);
      storeRsrc.commit();

    } catch (final EFapsException e) {
      LOG.error("could not checkout " + super.getInstance(), e);
      throw e;
    } catch (final Throwable e) {
      LOG.error("could not checkout " + super.getInstance(), e);
      throw new EFapsException(getClass(),
          "executeWithoutAccessCheck.Throwable", e);
    } finally {
      if ((storeRsrc != null) && storeRsrc.isOpened()) {
        storeRsrc.abort();
      }
    }
  }

  /**
   * Executes the checkout and returns an input stream by calling method
   * {@link #executeWithoutAccessCheck()}.
   *
   * @return input stream of the checked in file
   * @throws EFapsException
   *                 if the current context user has now access to checkout the
   *                 file out of the eFaps object
   * @see #executeWithoutAccessCheck()
   */
  public InputStream execute() throws EFapsException {
    final boolean hasAccess =
        super.getInstance().getType().hasAccess(super.getInstance(),
            AccessTypeEnums.CHECKOUT.getAccessType());
    if (!hasAccess) {
      throw new EFapsException(getClass(), "execute.NoAccess");
    }
    return executeWithoutAccessCheck();
  }

  /**
   * Executes the checkout without an access check (but with triggers) and
   * returns an input streams of the checked in file. The returned input stream
   * must be closed, because the returned inputs stream also commit the store
   * resource. Otherwise the transaction is rolled back!
   * <ol>
   * <li>executes the pre checkout trigger (if exists)</li>
   * <li>executes the checkout trigger (if exists)</li>
   * <li>executes if no checkout trigger exists or the checkout trigger is not
   * executed the update ({@see #executeWithoutTrigger})</li>
   * <li>executes the post checkout trigger (if exists)</li>
   * </ol>
   *
   * @throws EFapsException if checkout action fails
   * @return inputstream containing the file
   */
  public InputStream executeWithoutAccessCheck() throws EFapsException {
    InputStream ret = null;
    executeEvents(EventType.CHECKOUT_PRE);
    if (!executeEvents(EventType.CHECKOUT_OVERRIDE)) {
      ret = executeWithoutTrigger();
    }
    executeEvents(EventType.CHECKOUT_POST);
    return ret;
  }

  /**
   * Executes the checkout without an access check and without triggers and
   * returns an input streams of the checked in file. The returned input stream
   * must be closed, because the returned inputs stream also commit the store
   * resource. Otherwise the transaction is rolled back!
   *
   * @throws EFapsException if checkout action fails
   * @return inputstream containing the file
   */
  public InputStream executeWithoutTrigger() throws EFapsException {
    Resource storeRsrc = null;
    InputStream in = null;
    try {
      storeRsrc = Context.getThreadContext().getStoreResource(getInstance());
      in = storeRsrc.read();

    } catch (final EFapsException e) {
      LOG.error("could not checkout " + super.getInstance(), e);
      throw e;
    } catch (final Throwable e) {
      LOG.error("could not checkout " + super.getInstance(), e);
      throw new EFapsException(getClass(),
          "executeWithoutAccessCheck.Throwable", e);
    } finally {
      if ((in == null) && (storeRsrc != null) && storeRsrc.isOpened()) {
        storeRsrc.abort();
      }
    }
    return in;
  }

  /**
   * This is the getter method for instance variable {@link #fileName}.
   *
   * @return the fileName of the instance variable {@link #fileName}.
   * @see #fileName
   * @see #setFileName
   */
  public String getFileName() {
    return this.fileName;
  }

  /**
   * This is the getter method for instance variable {@link #fileLength}.
   *
   * @return the fileName of the instance variable {@link #fileLength}.
   * @see #fileLength
   */
  public long getFileLength() {
    return this.fileLength;
  }
}
