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

package org.efaps.admin.datamodel;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.efaps.db.query.CachedResult;

/**
 * @author tmo
 * @version $Id$
 */
public interface AttributeTypeInterface {

  // ///////////////////////////////////////////////////////////////////////////
  // methods for the interface to the database

  /**
   * The method prepares the statement for update the object in the database.
   * 
   * @param _stmt
   *          string buffer to append the statement
   * @return <i>true</i> if only a preparation is needed, otherwise <i>false</i>
   *         if the value must be set
   */
  public boolean prepareUpdate(final StringBuilder _stmt);

  /**
   * The method updates in the statment the value.
   * 
   * @param _stmt
   *          SQL statement to update the value
   * @param _index
   *          index in the SQL statement to update the value
   */
  public void update(final Object _object, final PreparedStatement _stmt,
      final List<Integer> _indexes) throws SQLException;

  /**
   * @param _rs
   *          cached result from the JDBC select statement
   * @param _index
   *          index in the result set
   */
  public Object readValue(final CachedResult _rs, final List<Integer> _indexes)
      throws Exception;

  // ///////////////////////////////////////////////////////////////////////////
  // methods for the interface to the UI

  /**
   * This methods sets the internal value with a string coming from the user
   * interface. The string is a localised value!
   * 
   * @param _context
   *          context for this request
   * @param _value
   *          new object value to set
   */
  public void set(final Object _value);

  // ///////////////////////////////////////////////////////////////////////////

  /**
   * The instance method gets the attribute for this attribute type interface.
   * 
   * @return attribute for this attribute value representing
   */
  public Attribute getAttribute();

  /**
   * The instance method sets the field for this attribute type interface.
   * 
   * @param _field
   *          field to set for this attribute type value
   */
  public void setAttribute(final Attribute _attribute);
}
