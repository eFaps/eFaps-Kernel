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

import org.efaps.db.Context;
import org.efaps.db.query.CachedResult;

/**
 *
 */
public class BlobType extends AbstractFileType  {

  /////////////////////////////////////////////////////////////////////////////
  // db interface

  public boolean prepareUpdate(StringBuffer _stmt)  {
    _stmt.append("''");
    return true;
  }

  public void update(Context _context, PreparedStatement _stmt, int _index)  throws SQLException  {
    throw new SQLException("Update value for file not allowed!!!");
  }

  /**
   *
   * @todo test that only one value is given for indexes
   */
  public Object readValue(Context _context, CachedResult _rs, ArrayList<Integer> _indexes) throws Exception  {
setFileName(_rs.getString(_indexes.get(0).intValue()));
//    throw new Exception("setValue value for file not allowed!!!");
return getFileName();
  }

}