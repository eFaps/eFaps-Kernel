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

package org.efaps.ui.webdav;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.efaps.ui.webdav.resource.AbstractResource;
import org.efaps.ui.webdav.resource.CollectionResource;
import org.efaps.ui.webdav.resource.SourceResource;

/**
 *
 * @author tmo
 * @version $Id$
 * @todo description
 */
public interface WebDAVInterface  {

  /////////////////////////////////////////////////////////////////////////////
  // collection resource methods

  public List < AbstractResource > getSubs(final CollectionResource _collection);

  /**
   * @param _collection collection resource representing the folder (if null,
   *                    means root folder)
   * @param _name       name of the searched collection resource
   * @return found collection resource for given instance or null if not found.
   */
  public CollectionResource getCollection(final CollectionResource _collection,
                                          final String _name);

  public boolean createCollection(final CollectionResource _collection,
                                  final String _name);

  /**
   * A collection is moved to a new collection with a new name. Attention! The
   * new location (new parent) could be the same parent as currently specified!
   *
   * @param _collection collection to move
   * @param _newParent  new parent collection
   * @param _newName    new name of the collection to move in the new parent
   *                    collection
   * @return <i>true</i> if the move of the collection is allowed, otherwise
   *         <i>false</i>
   */
  public boolean moveCollection(final CollectionResource _collection,
                                final CollectionResource _newParent,
                                final String _newName);

  /**
   * A collection is copied to a new collection with a new name. Attention! The
   * new location (new parent) could be the same parent as currently specified!
   *
   * @param _collection collection to copy
   * @param _newParent  new parent collection
   * @param _newName    new name of the collection to copy in the new parent
   *                    collection
   * @return <i>true</i> if the copy of the collection is allowed, otherwise
   *         <i>false</i>
   */
  public boolean copyCollection(final CollectionResource _collection,
                                final CollectionResource _newParent,
                                final String _newName);

  public boolean deleteCollection(final CollectionResource _collection);

  /////////////////////////////////////////////////////////////////////////////
  // source resource methods

  /**
   * @param _collection collection resource representing the folder (if null,
   *                    means root folder)
   * @param _name       name of the searched source resource
   * @return found source resource for given instance or null if not found.
   */
  public SourceResource getSource(final CollectionResource _collection,
                                  final String _name);

  public boolean createSource(final CollectionResource _collection,
                              final String _name);

  /**
   * A source resource is moved to a new collection with a new name. Attention!
   * The new location (new parent) could be the same parent as currently
   * specified!
   *
   * @param _source     source to move
   * @param _newParent  new parent collection
   * @param _newName    new name of the collection to move in the new parent
   *                    collection
   * @return <i>true</i> if the move of the collection is allowed, otherwise
   *         <i>false</i>
   */
  public boolean moveSource(final SourceResource _source,
                            final CollectionResource _newParent,
                            final String _name);

  /**
   * A source resource is copied to a new collection with a new name.
   * Attention! The new location (new parent) could be the same parent as
   * currently specified!
   *
   * @param _source     source to copy
   * @param _newParent  new parent collection
   * @param _newName    new name of the collection to copy in the new parent
   *                    collection
   * @return <i>true</i> if the copy of the collection is allowed, otherwise
   *         <i>false</i>
   */
  public boolean copySource(final SourceResource _source,
                            final CollectionResource _newParent,
                            final String _name);

  public boolean deleteSource(final SourceResource _source);

  public boolean checkinSource(final SourceResource _source,
                               final InputStream _inputStream);

  public boolean checkoutSource(final SourceResource _source,
                                final OutputStream _outputStream);
}
