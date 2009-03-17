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

package org.efaps.esjp.earchive.revision;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Context;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.esjp.earchive.node.Node;
import org.efaps.esjp.earchive.repository.Repository;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author jmox
 * @version $Id$
 */
@EFapsUUID("28f7fa7e-0687-499d-9355-783038019331")
@EFapsRevision("$Rev$")
public class Revision {

  private static final String TYPE_REVISION = "eArchive_Revision";
  private static final String TABLE_REVISION = "T_EAREVISION";
  private static final String TABLE_REPOSITORY = "T_EAREPOSITORY";


  private Long revision;


  /**
   * Getter method for instance variable {@link #revision}.
   *
   * @return value of instance variable {@link #revision}
   */
  public Long getRevision() {
    return this.revision;
  }

  public static Revision getNewRevision(final Repository _repository,
                                        final Node _node)
      throws EFapsException {
    final StringBuilder cmd = new StringBuilder();

    cmd.append("insert into ").append(TABLE_REVISION)
      .append("(id, repositoryid, revision, nodeid, creator, created)")
      .append(" values (?,?,?,?,?,").append(Context.getDbType().getCurrentTimeStamp())
      .append(")");

    final Context context = Context.getThreadContext();
    final ConnectionResource con = context.getConnectionResource();

    Long id = null;
    try {
     id = Context.getDbType().getNewId(con.getConnection(),
                                       TABLE_REVISION,
                                       "ID");

     final Revision revision = new Revision();
     revision.newRevision(_repository, con);

     PreparedStatement stmt = null;
     try {
       stmt = con.getConnection().prepareStatement(cmd.toString());

       stmt.setLong(1, id);
       stmt.setLong(2, _repository.getId());
       stmt.setLong(3, revision.getRevision());
       stmt.setLong(4, _node.getId());
       stmt.setLong(5, context.getPersonId());
       final int rows = stmt.executeUpdate();
       final ResultSet resultset = stmt.getGeneratedKeys();

       if (resultset.next()) {
           id = resultset.getLong(1);
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
    return new Revision();
  }


  private void newRevision(final Repository _repository,
                           final ConnectionResource _con) {

    final StringBuilder cmd = new StringBuilder();
    cmd.append("select lastrevision from ").append(TABLE_REPOSITORY)
      .append(" where id=").append(_repository.getId())
      .append(" for update");
    try {
      final Statement stmt = _con.getConnection().createStatement();
      final ResultSet rec = stmt.executeQuery(cmd.toString());

      if (rec.next()) {
        this.revision = rec.getLong(1) + 1;
        final StringBuilder upd = new StringBuilder();
        upd.append("Update ").append(TABLE_REPOSITORY)
          .append(" set lastrevision = ").append(this.revision).append(",")
          .append(" modified=")
            .append(Context.getDbType().getCurrentTimeStamp())
          .append(" where id=").append(_repository.getId());
        final Statement stmt2 = _con.getConnection().createStatement();
        stmt2.executeUpdate(upd.toString());
      } else {
        this.revision = new Long(1);
      }
    }  catch (final SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
