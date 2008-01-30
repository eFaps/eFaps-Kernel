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

package org.efaps.admin.datamodel;

import java.util.Collection;
import java.util.HashSet;
import java.util.StringTokenizer;

/**
 * The class stores the unique keys of a type instance object. One unique
 * key can have one or more attribute.
 *
 * @author tmo
 * @version $Id$
 */
public class UniqueKey  {

  /**
   * The constructor creates a new list of attributes which are an unique
   * key for this type.<br/>
   * Each attribute in the  string list is added to this unique key instance.
   *
   * @param _attrList string with comma separated list of attribute names
   */
  UniqueKey(Type _type, String _attrList)  {
    StringTokenizer tokens = new StringTokenizer(_attrList, ",");
    while (tokens.hasMoreTokens())  {
      Attribute attr = _type.getAttribute(tokens.nextToken());
      attr.addUniqueKey(this);
      getAttributes().add(attr);
    }
  }

  ///////////////////////////////////////////////////////////////////////////

  /**
   * The collection instance variables holds all attributes for this unique
   * key.
   *
   * @see #getAttributes
   */
  private Collection<Attribute> attributes = new HashSet<Attribute>();

  ///////////////////////////////////////////////////////////////////////////

  /**
   * This is the getter method for instance variable {@link #attributes}.
   *
   * @return value of instance variable {@link #attributes}
   * @see #attributes
   */
  public Collection<Attribute> getAttributes()  {
    return this.attributes;
  }
}
