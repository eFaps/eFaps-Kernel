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

package org.efaps.jaas;

import java.io.Serializable;
import java.security.Principal;

/**
 * The abstract class is used to implement the user and role principal
 * instances used by class {@link UserLoginModule}.
 */
abstract class AbstractPrincipal implements Principal, Serializable  {

  /**
   * The instance variable stores the name of the principal.
   *
   * @see #getName
   */
  private final String name;

  /**
   * Constructor used to create a new abstract principal instance. The instance
   * variable {@link #name} is initiated.
   *
   * @param _name name of the user
   */
  protected AbstractPrincipal(final String _name)  {
    this.name = _name;
  }

  /**
   * Compares this principal to the specified object.
   *
   * @param _another object to compare to this principle
   * @return returns <i>true</i> if the other object is from this class and
   *         has the same name (method equals is used), otherwise <i>false</i>
   */
  public boolean equals(final Object _another)  {
    boolean ret = false;
    if (_another instanceof UserPrincipal
        && ((UserPrincipal) _another).getName().equals(this.name))  {

      ret = true;
    }
    return ret;
  }

  /**
   * Returns the name of this principal stored in instance variable
   * {@link #name}.
   *
   * @return name of the principal
   * @see #name
   */
  public String getName()  {
    return this.name;
  }


  /**
   * Returns a string representation of this principal.
   *
   * @return value of {@link #name}
   * @see #name
   */
  public String toString()  {
    return this.name;
  }

  /**
   * Returns the hash code of the instance variable {@link #name}, because
   * if two user principals are the same, the hash code must have also the
   * same value (otherwise a same hash code does not mean they are equal!).
   *
   * @return hashcode of the name string
   * @see #name
   */
  public int hashCode()  {
    return this.name.hashCode();
  }
}
