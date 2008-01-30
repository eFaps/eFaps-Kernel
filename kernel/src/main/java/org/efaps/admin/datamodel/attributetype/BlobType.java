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

package org.efaps.admin.datamodel.attributetype;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.efaps.db.query.CachedResult;

/**
 *
 * @author tmo
 * @version $Id$
 */
public class BlobType extends AbstractFileType {

  // ///////////////////////////////////////////////////////////////////////////
  // db interface

  public boolean prepareUpdate(final StringBuilder _stmt) {
    _stmt.append("''");
    return true;
  }

  @Override
  public void update(final Object _object, final PreparedStatement _stmt,
      final List<Integer> _index) throws SQLException {
    throw new SQLException("Update value for file not allowed!!!");
  }

  public Object readValue(final CachedResult _rs, final List<Integer> _index)
      throws Exception {
    setFileName(_rs.getString(_index.get(0).intValue()));
    return getFileName();
  }

}
