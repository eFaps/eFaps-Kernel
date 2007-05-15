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
 * Author:          tmo
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.db;

import java.io.File;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.efaps.admin.access.AccessTypeEnums;
import org.efaps.admin.datamodel.Type;
import org.efaps.util.EFapsException;
import org.efaps.db.transaction.StoreResource;

/**
 * The class is used to checkin a file to a given attribute of an object.
 * 
 * @author tmo
 * @version $Id: Checkin.java 416 2006-09-16 18:31:00 +0000 (Sat, 16 Sep 2006)
 *          tmo $
 */
public class Checkin extends AbstractAction {

  // ///////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used in this class.
   */
  private static final Log LOG = LogFactory.getLog(Checkin.class);

  // ///////////////////////////////////////////////////////////////////////////
  // instance variables

  /**
   * Instance holding the oid of the object which is checked out.
   */
  private final Instance   instance;

  // ///////////////////////////////////////////////////////////////////////////
  // constructors

  /**
   * Constructor with a string as object id.
   * 
   * @param _oid
   *          oid of object on which the checkin is made
   */
  public Checkin(final String _oid) {
    this(new Instance(_oid));
  }

  /**
   * Constructor with an instance object as object id.
   * 
   * @param _instance
   *          instance on which the checkin is made
   */
  public Checkin(final Instance _instance) {
    this.instance = _instance;
  }

  /**
   * 
   * @param _fileName
   *          file name to checkin (could include also the path)
   * @param _in
   *          input stream with the binary data
   * @param _size
   *          size of file in stream to check in (negative size means that all
   *          from the stream must be written)
   * @see #executeWithoutAccessCheck
   * @todo description
   */
  public void execute(final String _fileName, final InputStream _in,
                      final int _size) throws EFapsException {
    boolean hasAccess = this.instance.getType().hasAccess(this.instance,
        AccessTypeEnums.CHECKIN.getAccessType());
    if (!hasAccess) {
      throw new EFapsException(getClass(), "execute.NoAccess");
    }
    executeWithoutAccessCheck(_fileName, _in, _size);
  }

  /**
   * Executes the checkin:
   * <ul>
   * <li>the file is checked in</li>
   * <li>the file name and file length is stored in with
   * {@link org.efaps.db.Update} (complete filename without path)</li>
   * </ul>
   * If this method is used, the checkin access is not tested!
   * 
   * @param _fileName
   *          file name to checkin (could include also the path)
   * @param _in
   *          input stream with the binary data
   * @param _size
   *          size of file in stream to check in (negative size means that all
   *          from the stream must be written)
   * @throws EFapsException
   *           if checkout action fails
   * @todo history entries
   */
  public void executeWithoutAccessCheck(final String _fileName,
                                        final InputStream _in, final int _size)
                                                                               throws EFapsException {

    Context context = Context.getThreadContext();
    StoreResource storeRsrc = null;
    boolean ok = false;
    try {
      Type type = this.instance.getType();

      String attrFileName = type.getProperty(PROPERTY_STORE_ATTR_FILE_NAME);
      String attrFileLength = type.getProperty(PROPERTY_STORE_ATTR_FILE_LENGTH);

      storeRsrc = context.getStoreResource(type, this.instance.getId());
      int size = storeRsrc.write(_in, _size);
      storeRsrc.commit();
      storeRsrc = null;

      File file = new File(_fileName);
      String fileName = file.getName();

      // remove the path from the filename
      int lastSeperatorPosX = fileName.lastIndexOf("/");
      int lastSeperatorPosWin = fileName.lastIndexOf("\\");
      int lastSeperatorPosMac = fileName.lastIndexOf(":");

      int lastSeperatorPos = lastSeperatorPosX;
      if (lastSeperatorPos < lastSeperatorPosWin) {
        lastSeperatorPos = lastSeperatorPosWin;
      }
      if (lastSeperatorPos < lastSeperatorPosMac) {
        lastSeperatorPos = lastSeperatorPosMac;
      }

      if (lastSeperatorPos > -1 && lastSeperatorPos < fileName.length() - 1) {
        fileName = fileName.substring(lastSeperatorPos + 1);
      }

      // set file name and length in the eFaps object
      Update update = new Update(this.instance);
      update.add(attrFileName, fileName);
      update.add(attrFileLength, "" + size);
      update.executeWithoutAccessCheck();
      ok = true;
    } catch (EFapsException e) {
      LOG.error("could not checkin " + this.instance, e);
      throw e;
    } catch (Throwable e) {
      LOG.error("could not checkin " + this.instance, e);
      throw new EFapsException(Checkin.class,
          "executeWithoutAccessCheck.Throwable", e);
    }
    finally {
      if (!ok) {
        context.abort();
      }
      if ((storeRsrc != null) && (storeRsrc.isOpened())) {
        storeRsrc.abort();
      }
    }
  }
}