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
import java.sql.Timestamp;

import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Context;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.esjp.earchive.INames;
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
public class Revision implements INames {

  /**
   * Revision number of this Revision.
   */
  private Long revision;
  private Long id;
  private Long repositoryId;
  private Long nodeId;
  private String message;
  private Long creatorId;
  private String creatorName;
  private Timestamp created;


  /**
   * Constructor is needed so it can be used as esjp.
   */
  public Revision() {
  }

  public Revision(final Long _repositoryId, final Long _id,
                  final Long _revision, final Long _nodeId,
                  final String _message, final Long _creatorId,
                  final Timestamp _created, final String _creatorName) {
    this.id = _id;
    this.repositoryId = _repositoryId;
    this.revision = _revision;
    this.nodeId = _nodeId;
    this.message = _message.trim();
    this.creatorId = _creatorId;
    this.created = _created;
    this.creatorName = _creatorName.trim();
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
   * Getter method for instance variable {@link #repositoryId}.
   *
   * @return value of instance variable {@link #repositoryId}
   */
  public Long getRepositoryId() {
    return this.repositoryId;
  }

  /**
   * Getter method for instance variable {@link #nodeId}.
   *
   * @return value of instance variable {@link #nodeId}
   */
  public Long getNodeId() {
    return this.nodeId;
  }

  /**
   * Getter method for instance variable {@link #message}.
   *
   * @return value of instance variable {@link #message}
   */
  public String getMessage() {
    return this.message;
  }

  /**
   * Getter method for instance variable {@link #creatorId}.
   *
   * @return value of instance variable {@link #creatorId}
   */
  public Long getCreatorId() {
    return this.creatorId;
  }

  /**
   * Getter method for instance variable {@link #creatorName}.
   *
   * @return value of instance variable {@link #creatorName}
   */
  public String getCreatorName() {
    return this.creatorName;
  }

  /**
   * Getter method for instance variable {@link #created}.
   *
   * @return value of instance variable {@link #created}
   */
  public Timestamp getCreated() {
    return this.created;
  }

  /**
   * Getter method for instance variable {@link #revision}.
   *
   * @return value of instance variable {@link #revision}
   */
  public Long getRevision() {
    return this.revision;
  }

  public static Revision getNewRevision(final Repository _repository,
                                        final Node _rootNode,
                                        final String _msg)
      throws EFapsException {
    final Revision ret = new Revision();
    final StringBuilder cmd = new StringBuilder();

    cmd.append("insert into ").append(TABLE_REVISION)
      .append("(")
      .append(TABLE_REVISION_C_ID).append(",")
      .append(TABLE_REVISION_C_REPOSITORYID).append(",")
      .append(TABLE_REVISION_C_REVISION).append(",")
      .append(TABLE_REVISION_C_NODEID).append(",")
      .append(TABLE_REVISION_C_MESSAGE).append(",")
      .append(TABLE_REVISION_C_CREATOR).append(",")
      .append(TABLE_REVISION_C_CREATED)
      .append(") values (?,?,?,?,?,?,")
      .append(Context.getDbType().getCurrentTimeStamp())
      .append(")");

    final Context context = Context.getThreadContext();
    final ConnectionResource con = context.getConnectionResource();

    Long id = null;
    try {
     id = Context.getDbType().getNewId(con.getConnection(),
                                       TABLE_REVISION,
                                       "ID");

     ret.insertRevision(_repository, con);

     PreparedStatement stmt = null;
     try {
       stmt = con.getConnection().prepareStatement(cmd.toString());

       stmt.setLong(1, id);
       stmt.setLong(2, _repository.getId());
       stmt.setLong(3, ret.getRevision());
       stmt.setLong(4, _rootNode.getId());
       stmt.setString(5, _msg);
       stmt.setLong(6, context.getPersonId());
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
    return ret;
  }


  private void insertRevision(final Repository _repository,
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

  public static Revision getRevisionFromDB(final Repository _repository,
                                    final long _revision)
      throws EFapsException {
    final StringBuilder cmd = new StringBuilder();
    cmd.append(" select ")
      .append(TABLE_REVISION_T_C_ID).append(",")
      .append(TABLE_REVISION_T_C_REVISION).append(",")
      .append(TABLE_REVISION_C_NODEID).append(",")
      .append(TABLE_REVISION_C_MESSAGE).append(",")
      .append(TABLE_REVISION_T_C_CREATOR).append(",")
      .append(TABLE_REVISION_T_C_CREATED).append(",")
      .append(" NAME ")
      .append(" from ").append(TABLE_REVISION)
      .append(" join  t_userabstract on t_userabstract.id =")
         .append(TABLE_REVISION_T_C_CREATOR)
      .append(" where ").append(TABLE_REVISION_T_C_REVISION).append(" = ?")
      .append(" and ").append(TABLE_REVISION_T_C_REPOSITORYID).append(" = ?");

    final ConnectionResource con
                          = Context.getThreadContext().getConnectionResource();
    Revision ret = null;
    try {

     PreparedStatement stmt = null;
     try {
       stmt = con.getConnection().prepareStatement(cmd.toString());

       stmt.setLong(1, _revision);
       stmt.setLong(2, _repository.getId());
       final ResultSet resultset = stmt.executeQuery();

       if (resultset.next()) {
         ret = new Revision(_repository.getId(), resultset.getLong(1),
                            resultset.getLong(2), resultset.getLong(3),
                            resultset.getString(4), resultset.getLong(5),
                            resultset.getTimestamp(6), resultset.getString(7));
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
