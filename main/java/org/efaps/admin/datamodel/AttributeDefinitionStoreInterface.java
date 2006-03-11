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

package org.efaps.admin.datamodel;

import java.io.InputStream;
import java.io.OutputStream;

import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.util.EFapsException;

/**
 * The interface describes the needed methods for using a file store.
 */
public interface AttributeDefinitionStoreInterface  {

  /**
   * @param _context  eFaps context for this request
   * @oaram _instance eFaps instance itself for which the file is checked out
   * @param _fileName name of the file to checkin
   * @param _in       file represented by input stream
   */
  public void checkin(final Context _context, Instance _instance, final String fileName, final InputStream _in) throws EFapsException;

  /**
   * Returns the file name for the file representing
   *
   * @param _context  eFaps context for this request
   * @oaram _instance eFaps instance itself for which the file is checked out
   */
  public String getFileName(final Context _context, Instance _instance) throws EFapsException;

  /**
   * @param _context  eFaps context for this request
   * @oaram _instance eFaps instance itself for which the file is checked out
   * @param _out      output stream to write the file in
   */
  public void checkout(final Context _context, Instance _instance, final OutputStream _out) throws EFapsException;
}