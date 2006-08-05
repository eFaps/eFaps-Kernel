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

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 *
 * @author tmo
 * @version $Id$
 */
public class XMLPersonPrincipal extends XMLAbstractPrincipal  {

  /////////////////////////////////////////////////////////////////////////////
  // instance variables

  /***
   * The password of this person is stored in this instance variable.
   *
   * @see #getPassword
   * @see #setPassword
   */
  private String password = null;

  /**
   * All groups assign to this person are stored in this instance variable.
   *
   * @see #getRoles
   */
  private final Set < XMLRolePrincipal > roles
                                      = new HashSet < XMLRolePrincipal > ();

  /**
   * All groups assign to this person are stored in this instance variable.
   *
   * @see #getGroups
   */
  private final Set < XMLGroupPrincipal > groups
                                      = new HashSet < XMLGroupPrincipal > ();

  /////////////////////////////////////////////////////////////////////////////
  // methods

  /**
   * A new role with givevn name is added. The role is added as role principal
   * (instance of {@link XMLRolePrincipal}).
   *
   * @param _role   name of role to add
   * @see #roles
   */
  public void addRole(final String _role)  {
    this.roles.add(new XMLRolePrincipal(_role));
  }

  /**
   * A new group with givevn name is added. The group is added as role principal
   * (instance of {@link XMLGroupPrincipal}).
   *
   * @param _group  name of group to add
   * @see #groups
   */
  public void addGroup(final String _group)  {
    this.groups.add(new XMLGroupPrincipal(_group));
  }

  /////////////////////////////////////////////////////////////////////////////
  // getter and setter methods

  /**
   * Returns the password of this principal stored in instance variable
   * {@link #password}.
   *
   * @return name of this person principal
   * @see #password
   * @see #setPassword
   */
  public String getPassword() {
    return this.password;
  }

  /**
   * Sets the password of this person principal stored in instance variable
   * {@link #password}. The method must be public, because it is set from the
   * XML to bean converter in {@link XMLUserLoginModule}.
   *
   * @param _password new name to set for this principal
   * @see #password
   * @see #getPassword
   */
  public void setPassword(final String _password) {
    this.password = _password;
  }

  /**
   * Returns the name of this principal stored in instance variable
   * {@link #roles}.
   *
   * @return all assigned roles of this person principal
   * @see #roles
   */
  public Set < XMLRolePrincipal > getRoles() {
    return this.roles;
  }

  /**
   * Returns the groups of this principal stored in instance variable
   * {@link #groups}.
   *
   * @return all assigned groups of this person principal
   * @see #groups
   */
  public Set < XMLGroupPrincipal > getGroups() {
    return this.groups;
  }

  /**
   * Returns a string representation of this person principal.
   *
   * @return string representation of this person principal
   */
  public String toString()  {
    return new ToStringBuilder(this)
        .appendSuper(super.toString())
        .append("password", getPassword())
        .append("roles", getRoles())
        .append("groups", getGroups())
        .toString();
  }
}
