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

import org.efaps.db.Context;

/**
 * The class is the attribute type representation for the modifier person of a
 * business object.
 */
public class ModifierLinkType extends PersonLinkType  {

  /////////////////////////////////////////////////////////////////////////////
  // interface to the data base

  /**
   * The value of the modifier is added via the prepared statement setter
   * method. So only  a question mark ('?') is added to the statement.
   * The value is set with method {@link #update}.
   *
   * @param _stmt string buffer with the statement
   * @see #update
   */
  public boolean prepareUpdate(StringBuilder _stmt)  {
    _stmt.append("?");
    return false;
  }

  /**
   * The instance method sets the value in the prepared statement to the
   * id of the current context user.
   *
   * @param _context  context for this request
   * @param _stmt     sql prepared statement where to set the value
   * @param _index    index in the prepared statement to set the value
   * @see #prepareUpdate
   */
  public void update(Context _context, PreparedStatement _stmt, int _index)  throws SQLException  {
    _stmt.setLong(_index, _context.getPerson().getId());
  }
  
  public String toString(){
    return "" + getValue();
  }
}
