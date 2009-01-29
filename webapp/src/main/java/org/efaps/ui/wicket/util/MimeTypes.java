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

package org.efaps.ui.wicket.util;

import java.util.HashMap;
import java.util.Map;

/**
 * This enum contains the MimesTypes.
 *
 * @author jmox
 * @version $Id$
 */
public enum MimeTypes {
  /** text format. */
  TXT("txt", "text/plain"),
  /** pdf format. */
  PDF("pdf", "application/pdf"),
  /** xml format. */
  XML("xml", "text/xml");

  /**
   * Stores the type of the content.
   */
  private final String contentType;

  /**
   * Stores the ending.
   */
  private final String ending;

  /**
   * Private Constructor.
   *
   * @param _ending         ending
   * @param _contentType    content type
   */
  private MimeTypes(final String _ending, final String _contentType) {
    this.ending = _ending;
    this.contentType = _contentType;
    Mapper.ENDING2MIMETYPE.put(_ending, this);
  }

  /**
   * Method to get a MimeType by its ending.
   *
   * @param _ending   ending the Mimetye is returned
   * @return  MimeType
   */
  public static MimeTypes getMimeTypeByEnding(final String _ending) {
    return Mapper.ENDING2MIMETYPE.get(_ending);
  }

  /**
   * Getter method for instance variable {@link #ending}.
   *
   * @return value of instance variable {@link #ending}
   */
  public String getEnding() {
    return this.ending;
  }

  /**
   * Getter method for instance variable {@link #contentType}.
   *
   * @return value of instance variable {@link #contentType}
   */
  public String getContentType() {
    return this.contentType;
  }

  /**
   * Mapper Class. Needed because an enum can not contain static maps.
   *
   */
  private static class Mapper {

    /**
     * Mapping ending to MimeType.
     */
    private static final Map<String, MimeTypes> ENDING2MIMETYPE
                                            = new HashMap<String, MimeTypes>();
  }
}
