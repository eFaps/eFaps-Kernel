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

package org.efaps.db.transaction;

import java.io.InputStream;
import java.io.OutputStream;

import org.efaps.admin.datamodel.Type;
import org.efaps.db.Context;
import org.efaps.util.EFapsException;

/**
 * The algorithm is:
 * <ol>
 *   <li></li>
 *   <li></li>
 *   <li></li>
 * </ol>
 *
 * For each file id a new VFS store resource must be created.
 */
public abstract class StoreResource extends AbstractResource  {

  /**
   * The variable stores the identifier of the file. This store is representing
   * this file.
   *
   * @see #getFileId
   */
  private long fileId;

  /**
   * Each store must be identfied with an url defining where the file and which
   * store resource is used.
   *
   * @see #getType
   */
  private Type type;

  /**
   *
   * @param _context  eFaps context
   * @param _type     type with the information how to store the file
   * @param _fileId   id of the file to store
   */
  protected StoreResource(final Context _context, final Type _type,
                                                        final long _fileId)  {
    super(_context);
    this.fileId = _fileId;
    this.type = _type;
  }

  /**
   * Frees the resource and gives this VFS store resource back to the context
   * object.
   */
  protected void freeResource()  {
//    getContext().returnConnectionResource(this);
  }

  /**
   * Writes the file with the given input stream.
   *
   * @param _in     input stream
   * @param _size   size of the data to write (or negative if the size is not
   *                known)
   * @return length of the file which is stored
   * @throws EFapsException if an error occurs
   */
  public abstract int write(final InputStream _in, final int _size)
                                                        throws EFapsException;

  /**
   * The output stream is written with the content of the file.
   *
   * @param _out    output stream where the file content must be written
   * @throws EFapsException if an error occurs
   */
  public abstract void read(final OutputStream _out) throws EFapsException;

  /**
   * Deletes the file defined in {@link #fileId}.
   *
   * @throws EFapsException if an error occurs
   */
  public abstract void delete() throws EFapsException;

  /**
   * This is the getter method for instance variable {@link #person}.
   *
   * @return value of instance variable {@link #fileId}
   * @see #fileId
   */
  protected final long getFileId()  {
    return this.fileId;
  }

  /**
   * This is the getter method for instance variable {@link #type}.
   *
   * @return value of instance variable {@link #type}
   * @see #type
   */
  protected final Type getType()  {
    return this.type;
  }
}
