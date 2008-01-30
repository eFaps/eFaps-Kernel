/*
 * Copyright 2003 - 2008 The eFaps Team
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

package org.efaps.jaas;

import javax.security.auth.callback.Callback;

/**
 * The class defines with which action the login is made. It defines e.g. the
 * difference between a login and an import of all persons.
 *
 * @author tmo
 * @version $Id$
 */
public class ActionCallback implements Callback {

  public enum Mode {
    /** A 'normal' login is done. Name and Password must be checked. */
    Login,
    /** List of all persons is needed. */
    AllPersons,
    /** Information about a person is needed. Password is not to check */
    PersonInformation,
    /** Mode is undefined. An exception should be thrown. */
    Undefined
  }

  // ///////////////////////////////////////////////////////////////////////////
  // instance variables

  /**
   * @see #setMode
   * @see #getMode
   */
  private Mode mode = Mode.Undefined;

  // ///////////////////////////////////////////////////////////////////////////
  // getter and setter methods

  /**
   * This is the setter method for instance variable {@link #mode}.
   *
   * @param _id
   *                new value for instance variable {@link #mode}
   * @see #mode
   * @see #getMode
   */
  public void setMode(final Mode _mode) {
    this.mode = _mode;
  }

  /**
   * This is the getter method for instance variable {@link #mode}.
   *
   * @return the value of the instance variable {@link #mode}.
   * @see #mode
   * @see #setMode
   */
  public Mode getMode() {
    return this.mode;
  }
}
