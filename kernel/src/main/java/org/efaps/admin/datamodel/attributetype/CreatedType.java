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

package org.efaps.admin.datamodel.attributetype;

import org.efaps.db.Context;

/**
 * The class is the attribute type representation for the created date time of a
 * business object.
 * 
 * @author tmo
 * @version $Id$
 */
public class CreatedType extends DateTimeType {

  // ///////////////////////////////////////////////////////////////////////////
  // interface to the data base

  /**
   * The instance method appends <i>SYSDATE</i> to the sql statement. Because
   * This is not a value, the method returns a <i>true</i>, that the value is
   * hard coded and must not updated via a prepared sql statement.
   * 
   * @param _stmt
   *          string buffer with the statement
   * @return always <i>true</i>
   */
  public boolean prepareUpdate(final StringBuilder _stmt) {
    _stmt.append(Context.getDbType().getCurrentTimeStamp());
    return true;
  }

  public String toString() {
    return "" + getValue();
  }
}
