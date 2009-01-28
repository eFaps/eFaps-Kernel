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
 * Revision:        $Rev:1510 $
 * Last Changed:    $Date:2007-10-18 09:35:40 -0500 (Thu, 18 Oct 2007) $
 * Last Changed By: $Author:jmox $
 */

package org.efaps.ui.webdav.resource;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import org.efaps.db.Instance;
import org.efaps.ui.webdav.WebDAVInterface;

/**
 *
 * @author tmo
 * @version $Id$
 * @todo description
 */
public class SourceResource extends AbstractResource  {

  /////////////////////////////////////////////////////////////////////////////
  // instance variables

  /**
   * File length of the source resource.
   *
   * @see #getLength
   */
  private final long length;

  /////////////////////////////////////////////////////////////////////////////
  // constructor / desctructors

  public SourceResource(final WebDAVInterface _webDAVImpl,
                        final String _name,
                        final Instance _instance,
                        final Date _created,
                        final Date _modified,
                        final String _description,
                        final long _length)  {
    super(_webDAVImpl, _name, _instance, _created, _modified, _description);
    this.length = _length;
  }

  /**
   * Deletes this source resource.
   *
   * @return <i>true</i> if deleted, otherwise <i>false</i>
   */
  @Override
  public boolean delete()  {
    return getWebDAVImpl().deleteSource(this);
  }

  /**
   * Moves this source resource to a new location.
   *
   * @param _collection   new parent collection to move
   * @param _newName      new name of the source in the parent collection
   * @return <i>true</i> if moved, otherwise <i>false</i>
   */
  @Override
  public boolean move(final CollectionResource _collection,
                      final String _newName)  {
    return getWebDAVImpl().moveSource(this, _collection, _newName);
  }

  /**
   * Copies this source resource to a new location.
   *
   * @param _collection   new parent collection to copy on
   * @param _newName      new name of the source in the parent collection
   * @return <i>true</i> if copied, otherwise <i>false</i>
   */
  @Override
  public boolean copy(final CollectionResource _collection,
                      final String _newName)  {
    return getWebDAVImpl().copySource(this, _collection, _newName);
  }

  public boolean checkout(final OutputStream _outputStream)  {
    return getWebDAVImpl().checkoutSource(this, _outputStream);
  }

  public boolean checkin(final InputStream _inputStream)  {
    return getWebDAVImpl().checkinSource(this, _inputStream);
  }

  /////////////////////////////////////////////////////////////////////////////
  // getter / setter methods for instance variables

  /**
   * This is the getter method for instance variable {@table #length}.
   *
   * @return value of instance variable {@table #length}
   * @see #length
   */
  public long getLength()  {
    return this.length;
  }
}
