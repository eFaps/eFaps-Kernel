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
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.admin.program.pack;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * TODO description
 *
 * @author jmox
 * @version $Id$
 */
public class OnePackage {

  private final File file;

  private final long created;

  private String contentType = "text/plain";

  public OnePackage(final File _file) {
    this.file = _file;
    this.created = System.currentTimeMillis();
  }

  public InputStream getInputStream() {
    InputStream ret = null;
    try {
      ret = new FileInputStream(this.file);
    } catch (final FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return ret;
  }

  public long getCreationTime() {
    return this.created;
  }

  /**
   * This is the getter method for the instance variable {@link #contentType}.
   *
   * @return value of instance variable {@link #contentType}
   */
  public String getContentType() {
    return this.contentType;
  }

  /**
   * This is the setter method for the instance variable {@link #contentType}.
   *
   * @param contentType
   *                the contentType to set
   */
  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

}
