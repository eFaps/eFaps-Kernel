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

package org.efaps.jaas.xml;

import java.security.Principal;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * The abstract class is used to implement the person, role and group principal
 * instances used by class {@link XMLUserLoginModule}.
 *
 * @author tmo
 * @version $Id$
 */
public abstract class XMLAbstractPrincipal implements Principal  {

  /////////////////////////////////////////////////////////////////////////////
  // instance variables

  /**
   * The instance variable stores the name of the principal.
   *
   * @see #getName
   * @see #setName
   */
  private String name = null;

  /////////////////////////////////////////////////////////////////////////////
  // getter and setter methods

  /**
   * Returns the name of this principal stored in instance variable
   * {@link #name}.
   *
   * @return name of this principal
   * @see #name
   * @see #setName
   */
  public String getName()  {
    return this.name;
  }

  /**
   * Sets the name of this principal stored in instance variable
   * {@link #name}. The method must be public, because it is set from the XML
   * to bean converter in {@link XMLUserLoginModule}.
   *
   * @param _name new name to set for this principal
   * @see #name
   * @see #getName
   */
  public void setName(final String _name)  {
    this.name = _name;
  }

  /**
   * Returns a string representation of this principal.
   *
   * @return string representation of this principal
   */
  public String toString()  {
    return new ToStringBuilder(this)
        .append("name", getName())
        .toString();
  }
}
