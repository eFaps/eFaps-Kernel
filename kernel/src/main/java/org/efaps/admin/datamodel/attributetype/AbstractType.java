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

package org.efaps.admin.datamodel.attributetype;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Locale;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.AttributeTypeInterface;
import org.efaps.db.Context;
import org.efaps.db.query.CachedResult;

/**
 *
 */
abstract public class AbstractType implements AttributeTypeInterface  {

  public boolean prepareUpdate(StringBuilder _stmt)  {
    _stmt.append("?");
    return false;
  }

  public void update(Context _context, PreparedStatement _stmt, int _index)  throws SQLException  {
  }

  /**
   *
   *
   */
  abstract public Object readValue(Context _context, CachedResult _rs, ArrayList<Integer> _indexes) throws Exception;

  /////////////////////////////////////////////////////////////////////////////
  // methods for the user interface

  abstract public void set(Context _context, String _value);

  /**
   * The method returns a string as the viewable value of the attribute type.
   *
   * @param _locale locale object
   */
  abstract public String getViewableString(Locale _locale);

  /////////////////////////////////////////////////////////////////////////////

  /**
   * The instance variable stores the field for which the attribute value.
   *
   * @see #getField
   * @see #setField
   */
//  private Field field = null;

  /**
   *
   */
  private Attribute attribute = null;

  /////////////////////////////////////////////////////////////////////////////

  /**
   * This is the getter method for the field variable {@link #field}.
   *
   * @return value of field variable {@link #field}
   * @see #field
   * @see #setField
   */
//  public Field getField()  {
//    return this.field;
//  }

  /**
   * This is the setter method for the field variable {@link #field}.
   *
   * @param _field  new value for field variable {@link #field}
   * @see #field
   * @see #getField
   */
//  public void setField(Field _field)  {
//    this.field = _field;
//  }

  /**
   * This is the getter method for the field variable {@link #attribute}.
   *
   * @return value of field variable {@link #attribute}
   * @see #attribute
   * @see #setAttribute
   */
  public Attribute getAttribute()  {
    return this.attribute;
  }

  /**
   * This is the setter method for the field variable {@link #attribute}.
   *
   * @param _field  new value for field variable {@link #attribute}
   * @see #attribute
   * @see #getAttribute
   */
  public void setAttribute(Attribute _attribute)  {
    this.attribute = _attribute;
  }

  /////////////////////////////////////////////////////////////////////////////
  // methods of interface Comparable

  /**
   * Compares this object with the specified object for order. Returns a
   * negative integer, zero, or a positive integer as this object is less than,
   * equal to, or greater than the specified object.<br/>
   * This is the default implementation. The method makes a string compare.
   *
   * @param _locale   locale object
   * @param _object   the Object to be compared.
   * @return a negative integer, zero, or a positive integer as this object is
   *         less than, equal to, or greater than the specified object.
   */
  public int compareTo(Locale _locale, AttributeTypeInterface _object)  {
    String s1 = getViewableString(_locale);
    String s2 = _object.getViewableString(_locale);
    return s1.compareTo(s2);
  }
}