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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.efaps.db.Context;
import org.efaps.util.EFapsException;

/**
 * The class is the attribute type representation for the owner person of a
 * business object.
 * 
 * @author tmo
 * @version $Id$
 */
public class OwnerLinkType extends PersonLinkType {
  /**
   * Logger for this class
   */
  private static final Logger LOG = LoggerFactory.getLogger(OwnerLinkType.class);

  // ///////////////////////////////////////////////////////////////////////////
  // interface to the data base

  /**
   * The value of the modifier is added via the prepared statement setter
   * method. So only a question mark ('?') is added to the statement. The value
   * is set with method {@link #update}.
   * 
   * @param _stmt
   *          string buffer with the statement
   * @see #update
   */
  public boolean prepareUpdate(final StringBuilder _stmt) {
    _stmt.append("?");
    return false;
  }

  /**
   * The instance method sets the value in the prepared statement to the id of
   * the current context user.
   * 
   * @param _context
   *          context for this request
   * @param _stmt
   *          sql prepared statement where to set the value
   * @param _index
   *          index in the prepared statement to set the value
   * @see #prepareUpdate
   */
  public void update(final Object _object, final PreparedStatement _stmt,
      final List<Integer> _indexes) throws SQLException {
    try {
      _stmt.setLong(_indexes.get(0), Context.getThreadContext().getPerson()
          .getId());
    } catch (EFapsException e) {
      LOG.error("update(Object, PreparedStatement, List<Integer>)", e);
    }
  }

  public String toString() {
    return "" + getValue();
  }
}
