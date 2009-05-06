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

import org.efaps.db.query.CachedResult;

/**
 * @author tmo
 * @version $Id$
 */
public class OIDType extends StringType {

  @Override
  public void update(final Object _object, final PreparedStatement _stmt,
      final List<Integer> _indexes) throws SQLException {
    throw new SQLException("Update value for OID not allowed!!!");
  }

  /**
   * The oid (object id) is the type id, than a point and the id itself. If in
   * the attribute the attribute has no defined type id SQL column name, the
   * type from the attribute is used (this means, the type itself is not derived
   * and has no childs).
   *
   * @param _context
   *          eFaps context for this request
   */
  @Override
  public Object readValue(final CachedResult _rs, final List<Integer> _indexes) {
    if (getAttribute().getSqlColNames().size() > 1) {
      final long typeId = _rs.getLong(_indexes.get(0).intValue());
      final long id = _rs.getLong(_indexes.get(1).intValue());
      setValue(typeId + "." + id);
    } else {
      final long id = _rs.getLong(_indexes.get(0).intValue());
      setValue(getAttribute().getParent().getId() + "." + id);
    }
    return getValue();
  }

  @Override
  public String toString() {
    return "" + getValue();
  }
}
