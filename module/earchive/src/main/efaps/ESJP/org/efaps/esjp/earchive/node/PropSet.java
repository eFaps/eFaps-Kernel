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

package org.efaps.esjp.earchive.node;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Context;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.esjp.earchive.INames;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author Jan Moxter
 * @version $Id$
 */
@EFapsUUID("37347a2c-33cf-4d7e-9230-e8348bb01383")
@EFapsRevision("$Rev$")
public class PropSet implements INames {


  private Long id;

  private final Map <String, String> properties = new HashMap<String, String>();

  /**
   * Getter method for instance variable {@link #id}.
   *
   * @return value of instance variable {@link #id}
   */
  public Long getId() {
    return this.id;
  }

  /**
   * Getter method for instance variable {@link #properties}.
   *
   * @return value of instance variable {@link #properties}
   */
  public Map<String, String> getProperties() {
    return this.properties;
  }

  public static PropSet get(final Long _propSetId) throws EFapsException {
    final PropSet ret = new PropSet();

    final StringBuilder cmd = new StringBuilder();
    //statement for the rootnode
    cmd.append(" select ")
      .append(TABLE_PROPERTY_C_KEY).append(",")
      .append(TABLE_PROPERTY_C_VALUE)
      .append(" from ").append(TABLE_PROPERTY)
      .append(" where ").append(TABLE_PROPERTY_C_SETID).append(" = ?");

    final ConnectionResource con
                          = Context.getThreadContext().getConnectionResource();
    try {

      PreparedStatement stmt = null;
      try {
        stmt = con.getConnection().prepareStatement(cmd.toString());
        stmt.setLong(1, _propSetId);
        final ResultSet resultset = stmt.executeQuery();
        ret.setId(_propSetId);
        while (resultset.next()) {
          ret.properties.put(resultset.getString(1).trim(),
                             resultset.getString(2).trim());
        }
        resultset.close();
      } finally {
        stmt.close();
      }
      con.commit();
    } catch (final SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } finally {
      if ((con != null) && con.isOpened()) {
        con.abort();
      }
    }

    return ret;
  }

  /**
   * @param setId
   */
  private void setId(final Long _setId) {
    this.id = _setId;
  }
}
