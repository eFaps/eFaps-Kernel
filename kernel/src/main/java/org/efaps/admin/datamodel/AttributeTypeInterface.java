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

package org.efaps.admin.datamodel;

import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Locale;

import org.efaps.admin.ui.Field;
import org.efaps.db.Context;
import org.efaps.db.query.CachedResult;

/**
 * @author tmo
 * @version $Rev$
 */
public interface AttributeTypeInterface  {

  /////////////////////////////////////////////////////////////////////////////
  // methods for the interface to the database

  /**
   * The method prepares the statement for update the object in the database.
   *
   * @param _stmt string buffer to append the statement
   * @return  <i>true</i> if only a preparation is needed, otherwise
   *          <i>false</i> if the value must be set
   */
  public boolean prepareUpdate(StringBuilder _stmt);

  /**
   * The method updates in the statment the value.
   *
   * @param __context eFaps context for this request
   * @param _stmt     SQL statement to update the value
   * @param _index    index in the SQL statement to update the value
   */
  public void update(Context _context, PreparedStatement _stmt, int _index)  throws SQLException;

  /**
   *
   * @param __context eFaps context for this request
   * @param _rs       cached result from the JDBC select statement
   * @param _index    index in the result set
   */
  public Object readValue(Context _context, CachedResult _rs, ArrayList<Integer> _index) throws Exception;

  /////////////////////////////////////////////////////////////////////////////
  // methods for the interface to the UI

  /**
   * This methods sets the internal value with a string coming from the user
   * interface. The string is a localised value!
   *
   * @param _context  context for this request
   * @param _value    new string value to set
   */
  public void set(Context _context, String _value);

  /**
   * The method returns a string as the viewable value of the attribute type.
   *
   * @param _locale   locale object
   */
  public String getViewableString(Locale _locale);

  /////////////////////////////////////////////////////////////////////////////

  /**
   * The instance method gets the attribute for this attribute type interface.
   *
   * @return attribute for this attribute value representing
   */
  public Attribute getAttribute();

  /**
   * The instance method sets the field for this attribute type interface.
   *
   * @param _field  field to set for this attribute type value
   */
  public void setAttribute(Attribute _attribute);

  /////////////////////////////////////////////////////////////////////////////
  // methods to compare

  /**
   * Compares this object with the specified object for order. Returns a
   * negative integer, zero, or a positive integer as this object is less than,
   * equal to, or greater than the specified object.<br/>
   * The implementor must ensure sgn(x.compareTo(y)) == -sgn(y.compareTo(x))
   * for all x and y. (This implies that x.compareTo(y) must throw an
   * exception if y.compareTo(x) throws an exception.) <br/>
   * The implementor must also ensure that the relation is transitive:
   * (x.compareTo(y)>0 && y.compareTo(z)>0) implies x.compareTo(z)>0.<br/>
   * Finally, the implementer must ensure that x.compareTo(y)==0 implies that
   * sgn(x.compareTo(z)) == sgn(y.compareTo(z)), for all z.<br/>
   * It is strongly recommended, but not strictly required that
   * (x.compareTo(y)==0) == (x.equals(y)). Generally speaking, any class that
   * implements the "compareTo" method and violates this condition should
   * clearly indicate this fact. The recommended language is "Note: this
   * class has a natural ordering that is inconsistent with equals."
   *
   * @param _locale   locale object
   * @param _object   the Object to be compared.
   * @return a negative integer, zero, or a positive integer as this object
   *         is less than, equal to, or greater than the specified object.
   * @throws ClassCastException if the specified object's type prevents it
   *                            from being compared to this Object.
   */
  public int compareTo(Locale _locale, AttributeTypeInterface _object);
}
