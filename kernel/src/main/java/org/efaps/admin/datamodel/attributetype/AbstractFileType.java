/*
 * Copyright 2003-2007 The eFaps Team
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

package org.efaps.admin.datamodel.attributetype;

/**
 * @author tmo
 * @version $Id$
 * @todo description
 */
abstract public class AbstractFileType extends AbstractType {

  // ///////////////////////////////////////////////////////////////////////////
  // user interface

  /**
   * @todo must an exception thrown?
   */
  public void set(final Object _value) {
  }

  // ///////////////////////////////////////////////////////////////////////////
  // instance variables

  /**
   * The value stores the file name of the file.
   * 
   * @see #getFileName
   * @see #setFileName
   */
  private String fileName = null;

  // ///////////////////////////////////////////////////////////////////////////
  // setter and getter methods

  /**
   * This is the setter method for instance variable {@link #fileName}.
   * 
   * @param _fileName
   *          new fileName for instance variable {@link #fileName}
   * @see #fileName
   * @see #getFileName
   */
  public void setFileName(final String _fileName) {
    this.fileName = (_fileName != null ? _fileName.trim() : null);
  }

  /**
   * This is the getter method for instance variable {@link #fileName}.
   * 
   * @return the fileName of the instance variable {@link #fileName}.
   * @see #fileName
   * @see #setFileName
   */
  public String getFileName() {
    return this.fileName;
  }
}
