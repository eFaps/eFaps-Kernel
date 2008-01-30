/*
 * Copyright 2003-2008 The eFaps Team
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.efaps.admin.access.AccessTypeEnums;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.event.EventType;
import org.efaps.db.transaction.StoreResource;
import org.efaps.util.EFapsException;

/**
 * The class is used to checkin a file to a given attribute of an object.
 *
 * @author tmo
 * @version $Id$
 */
public class Checkin extends AbstractAction {

  // ///////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used in this class.
   */
  private static final Logger LOG = LoggerFactory.getLogger(Checkin.class);

  // ///////////////////////////////////////////////////////////////////////////
  // instance variables

  // ///////////////////////////////////////////////////////////////////////////
  // constructors

  /**
   * Constructor with a string as object id.
   *
   * @param _oid
   *                oid of object on which the checkin is made
   */
  public Checkin(final String _oid) {
    this(new Instance(_oid));
  }

  /**
   * Constructor with an instance object as object id.
   *
   * @param _instance
   *                instance on which the checkin is made
   */
  public Checkin(final Instance _instance) {
    super.setInstance(_instance);
  }

  /**
   * @param _fileName
   *                file name to checkin (could include also the path)
   * @param _in
   *                input stream with the binary data
   * @param _size
   *                size of file in stream to check in (negative size means that
   *                all from the stream must be written)
   * @see #executeWithoutAccessCheck
   * @todo description
   */
  public void execute(final String _fileName, final InputStream _in,
                      final int _size) throws EFapsException {
    final boolean hasAccess =
        super.getInstance().getType().hasAccess(super.getInstance(),
            AccessTypeEnums.CHECKIN.getAccessType());
    if (!hasAccess) {
      throw new EFapsException(getClass(), "execute.NoAccess");
    }
    executeWithoutAccessCheck(_fileName, _in, _size);
  }

  /**
   * Executes the checkin without checking the access rights (but with
   * triggers):
   * <ol>
   * <li>executes the pre checkin trigger (if exists)</li>
   * <li>executes the checkin trigger (if exists)</li>
   * <li>executes if no checkin trigger exists or the checkin trigger is not
   * executed the update ({@see #executeWithoutTrigger})</li>
   * <li>executes the post checkin trigger (if exists)</li>
   * </ol>
   *
   * @param _fileName
   *                file name to checkin (could include also the path)
   * @param _in
   *                input stream with the binary data
   * @param _size
   *                size of file in stream to check in (negative size means that
   *                all from the stream must be written)
   * @throws EFapsException
   *                 if checkout action fails
   * @todo history entries
   */
  public void executeWithoutAccessCheck(final String _fileName,
                                        final InputStream _in, final int _size)
                                                                               throws EFapsException {
    executeEvents(EventType.CHECKIN_PRE);
    if (!executeEvents(EventType.CHECKIN_OVERRIDE)) {
      executeWithoutTrigger(_fileName, _in, _size);
    }
    executeEvents(EventType.CHECKIN_POST);
  }

  /**
   * The checkin is done without calling triggers and check of access rights.
   * Executes the checkin:
   * <ul>
   * <li>the file is checked in</li>
   * <li>the file name and file length is stored in with
   * {@link org.efaps.db.Update} (complete filename without path)</li>
   * </ul>
   *
   * @param _fileName
   *                file name to checkin (could include also the path)
   * @param _in
   *                input stream with the binary data
   * @param _size
   *                size of file in stream to check in (negative size means that
   *                all from the stream must be written)
   * @throws EFapsException
   *                 if checkout action fails
   */

  public void executeWithoutTrigger(final String _fileName,
                                    final InputStream _in, final int _size)
                                                                           throws EFapsException {
    final Context context = Context.getThreadContext();
    StoreResource storeRsrc = null;
    boolean ok = false;
    try {

      final Type type = super.getInstance().getType();

      final String attrFileName =
          type.getProperty(PROPERTY_STORE_ATTR_FILE_NAME);
      final String attrFileLength =
          type.getProperty(PROPERTY_STORE_ATTR_FILE_LENGTH);

      storeRsrc = context.getStoreResource(type, super.getInstance().getId());
      final int size = storeRsrc.write(_in, _size);
      storeRsrc.commit();
      storeRsrc = null;

      final File file = new File(_fileName);
      String fileName = file.getName();

      // remove the path from the filename
      final int lastSeperatorPosX = fileName.lastIndexOf("/");
      final int lastSeperatorPosWin = fileName.lastIndexOf("\\");
      final int lastSeperatorPosMac = fileName.lastIndexOf(":");

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
      final Update update = new Update(super.getInstance());
      update.add(attrFileName, fileName);
      update.add(attrFileLength, "" + size);
      update.executeWithoutAccessCheck();
      ok = true;

    } catch (EFapsException e) {
      LOG.error("could not checkin " + super.getInstance(), e);
      throw e;
    } catch (Throwable e) {
      LOG.error("could not checkin " + super.getInstance(), e);
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
