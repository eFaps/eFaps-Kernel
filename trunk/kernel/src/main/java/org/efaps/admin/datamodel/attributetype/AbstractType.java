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

package org.efaps.admin.datamodel.attributetype;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.AttributeTypeInterface;
import org.efaps.db.query.CachedResult;

/**
 * @author tmo
 * @version $Id$
 */
abstract public class AbstractType implements AttributeTypeInterface {

  public boolean prepareUpdate(final StringBuilder _stmt) {
    _stmt.append("?");
    return false;
  }

  public void update(final Object _object, final PreparedStatement _stmt,
      final List<Integer> _index) throws SQLException {
  }

  /**
   *
   *
   */
  abstract public Object readValue(final CachedResult _rs,
      final List<Integer> _indexes) throws Exception;

  // ///////////////////////////////////////////////////////////////////////////
  // methods for the user interface

  abstract public void set(final Object _value);

  // ///////////////////////////////////////////////////////////////////////////

  /**
   *
   */
  private Attribute attribute = null;

  // ///////////////////////////////////////////////////////////////////////////

  /**
   * This is the getter method for the field variable {@link #attribute}.
   * 
   * @return value of field variable {@link #attribute}
   * @see #attribute
   * @see #setAttribute
   */
  public Attribute getAttribute() {
    return this.attribute;
  }

  /**
   * This is the setter method for the field variable {@link #attribute}.
   * 
   * @param _field
   *          new value for field variable {@link #attribute}
   * @see #attribute
   * @see #getAttribute
   */
  public void setAttribute(final Attribute _attribute) {
    this.attribute = _attribute;
  }

}
