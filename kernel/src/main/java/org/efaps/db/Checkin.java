/*
 * Copyright 2005 The eFaps Team
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

package org.efaps.db;

import java.io.File;
import java.io.InputStream;

import org.efaps.admin.datamodel.Type;
import org.efaps.util.EFapsException;
import org.efaps.db.transaction.StoreResource;

/**
 * The class is used to checkin a file to a given attribute of an object.
 */
public class Checkin extends AbstractAction  {

  /**
   * Constructor with a string as object id.
   *
   * @param _context  eFaps context for this request
   * @param _oid      oid of object on which the checkin is made
   * @todo rewrite to thrown EFapsException
   */
  public Checkin(final Context _context, final String _oid) throws Exception  {
    this(_context, new Instance(_context, _oid));
  }

  /**
   * Constructor with an instance object as object id.
   *
   * @param _context  eFaps context for this request
   * @param _instance instance on which the checkin is made
   */
  public Checkin(final Context _context, final Instance _instance) throws EFapsException  {
    this.instance = _instance;
  }

  /**
   * Executes the checkin:
   * <ul>
   * <li>the file is checked in</li>
   * <li>the file name and file length is stored in with
   *     {@link org.efaps.db.Update} (complete filename without path)</li>
   * </ul>
   *
   * @param _context  eFaps context for this request
   * @param _fileName file name to checkin (could include also the path)
   * @param _in       input stream with the binary data
   * @param _size     size of file in stream to check in (negative size means
   *                  that all from the stream must be written)
   * @throws EFapsException if checkout action fails
   * @todo testing of access
   * @todo history entries
   */
  public void execute(final Context _context, final String _fileName,
              final InputStream _in, final int _size) throws EFapsException  {

    StoreResource storeRsrc = null;
    try  {
      Type type = getInstance().getType();

      String attrFileName   = type.getProperty(PROPERTY_STORE_ATTR_FILE_NAME);
      String attrFileLength = type.getProperty(PROPERTY_STORE_ATTR_FILE_LENGTH);

      storeRsrc = _context.getStoreResource(type, getInstance().getId());
      int size = storeRsrc.write(_in, _size);
      storeRsrc.commit();
      storeRsrc = null;

      File file = new File(_fileName);
      String fileName = file.getName();

      Update update = new Update(_context, getInstance());
      update.add(_context, attrFileName, fileName);
      update.add(_context, attrFileLength, ""+size);
      update.execute(_context);

    } catch (EFapsException e)  {
      throw e;
    } catch (Throwable e)  {
      throw new EFapsException(Checkin.class, "execute.Throwable", e);
    } finally  {
      if ((storeRsrc != null) && (storeRsrc.isOpened()))  {
        storeRsrc.abort();
      }
    }
  }

  /////////////////////////////////////////////////////////////////////////////

  /**
   * Instance holding the oid of the object which is checked out.
   *
   * @see #getInstance
   */
  private final Instance instance;

  /////////////////////////////////////////////////////////////////////////////

  /**
   * This is the getter method for instance variable {@link #instance}.
   *
   * @return value of instance variable {@link #instance}
   * @see #instance
   */
  protected Instance getInstance()  {
    return this.instance;
  }
}