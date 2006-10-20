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

package org.efaps.webdav;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 *
 * @author tmo
 * @version $Id$
 * @todo description
 */
public interface WebDAVInterface  {
  
  public List < AbstractResource > getSubs(final CollectionResource _collection);

  /**
   * @param _collection collection resource representing the folder (if null,
   *                    means root folder)
   * @param _name       name of the searched collection resource
   * @return found collection resource for given instance or null if not found.
   */
  public CollectionResource getCollection(final CollectionResource _collection,
                                          final String _name);

  /**
   * @param _collection collection resource representing the folder (if null,
   *                    means root folder)
   * @param _name       name of the searched source resource
   * @return found source resource for given instance or null if not found.
   */
  public SourceResource getSource(final CollectionResource _collection,
                                  final String _name);

  public boolean deleteCollection(final CollectionResource _collection);
  
  public boolean deleteSource(final SourceResource _source);

  public boolean createCollection(final CollectionResource _collection, 
                                  final String _name);

  public boolean createSource(final CollectionResource _collection, 
                              final String _name);
  
  public boolean checkinSource(final SourceResource _source, 
                               final InputStream _inputStream);

  public boolean checkoutSource(final SourceResource _source, 
                                final OutputStream _outputStream);
}
