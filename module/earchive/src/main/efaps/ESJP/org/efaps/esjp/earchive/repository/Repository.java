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

package org.efaps.esjp.earchive.repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.efaps.admin.datamodel.Type;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.esjp.earchive.INames;
import org.efaps.util.EFapsException;


/**
 * TODO comment!
 *
 * @author jmox
 * @version $Id$
 */
@EFapsUUID("de8eaa7f-154a-40c3-ae6a-6ad061f3cffa")
@EFapsRevision("$Rev$")
public class Repository implements INames {

  private Long id;

  private Type type;

  private Long latestRevision;

  private UUID uuid;

  public Repository(){

  }

  /**
   * @param id
   */
  public Repository(final long _id) {
    this.id = _id;
  }

  /**
   * @param instance
   */
  public Repository(final Instance _instance) {
    this.type = _instance.getType();
    this.id = _instance.getId();
  }

  /**
   * Getter method for instance variable {@link #latestRevision}.
   *
   * @return value of instance variable {@link #latestRevision}
   */
  public Long getLatestRevision() {
    return this.latestRevision;
  }

  /**
   * Setter method for instance variable {@link #latestRevision}.
   *
   * @param latestRevision value for instance variable {@link #latestRevision}
   */
  public void setLatestRevision(final Long latestRevision) {
    this.latestRevision = latestRevision;
  }

  /**
   * Getter method for instance variable {@link #id}.
   *
   * @return value of instance variable {@link #id}
   */
  public Long getId() {
    return this.id;
  }

  /**
   * Getter method for instance variable {@link #uuid}.
   *
   * @return value of instance variable {@link #uuid}
   */
  public UUID getUuid() {
    return this.uuid;
  }

  /**
   * Setter method for instance variable {@link #uuid}.
   *
   * @param uuid value for instance variable {@link #uuid}
   */
  public void setUuid(final UUID uuid) {
    this.uuid = uuid;
  }

  public static Repository getByName(final String _name) throws EFapsException {
    Repository ret = null;
    final StringBuilder cmd = new StringBuilder();
    cmd.append(" select ")
      .append(TABLE_REPOSITORY_C_ID).append(",")
      .append(TABLE_REPOSITORY_C_UUID).append(",")
      .append(TABLE_REPOSITORY_C_LASTREVISION)
      .append(" from ")
      .append(TABLE_REPOSITORY)
      .append(" where ")
      .append(TABLE_REPOSITORY_C_NAME).append("=?");

    final ConnectionResource con
                          = Context.getThreadContext().getConnectionResource();
    try {

     PreparedStatement stmt = null;
     try {
       stmt = con.getConnection().prepareStatement(cmd.toString());

       stmt.setString(1, _name);
       final ResultSet resultset = stmt.executeQuery();

       if (resultset.next()) {
         ret = new Repository(resultset.getLong(1));
         ret.setUuid(UUID.fromString(resultset.getString(2).trim()));
         ret.setLatestRevision(resultset.getLong(3));
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

}

