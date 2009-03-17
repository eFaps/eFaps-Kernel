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
import java.util.ArrayList;
import java.util.List;

import org.efaps.admin.datamodel.Type;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Context;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.esjp.earchive.repository.Repository;
import org.efaps.esjp.earchive.revision.Revision;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author jmox
 * @version $Id$
 */
@EFapsUUID("4108effb-988f-42c0-a143-9b15ae57d4d9")
@EFapsRevision("$Rev$")
public class Node {

  public static String TABLE_NODE ="T_EANODE";

  public static String SEQ_NODE_HISTORYID = "T_EANODE_HISTORYID_SEQ";

  public static String TYPE_DIRECTORY = "eArchive_NodeDirectory";

  private final Long id;

  /**
   * Getter method for instance variable {@link #id}.
   *
   * @return value of instance variable {@link #id}
   */
  public Long getId() {
    return this.id;
  }

  private final Long historyId;

  private final Long copyId;

  private final Long revision;

  private Type type;

  private String name;

  private Long repositoryId;

  private Node ancestor;

  /**
   * Getter method for instance variable {@link #ancestor}.
   *
   * @return value of instance variable {@link #ancestor}
   */
  public Node getAncestor() {
    return this.ancestor;
  }


  /**
   * Getter method for instance variable {@link #repositoryId}.
   *
   * @return value of instance variable {@link #repositoryId}
   */
  public Long getRepositoryId() {
    return this.repositoryId;
  }


  public Node() {
    this.id = null;
    this.historyId = null;
    this.copyId =null;
    this.revision = null;
  }


  /**
   * @param _id
   * @param i
   * @param j
   * @param k
   * @param _name
   */
  public Node(final Long _id, final Type _type, final Long _historyId,
              final Long _copyId, final Long _revision, final String _name,
              final Long _repositoryId) {
    this.id = _id;
    this.type = _type;
    this.historyId = _historyId != null ? _historyId : new Long(0);
    this.copyId = _copyId != null ? _copyId : new Long(0);
    this.revision = _revision != null ? _revision : new Long(0);
    this.name = _name;
    this.repositoryId = _repositoryId;
  }


  public static Node createNewNode(final String _name) throws EFapsException {

    final StringBuilder cmd = new StringBuilder();

    final Long typeId = Type.get(TYPE_DIRECTORY).getId();

    cmd.append("insert into ").append(TABLE_NODE)
      .append("(id, typeid, historyid, copyid, revision, name)")
      .append(" values (?,?,?,?,?,?)");

    Long id = null;
    Long histId = null;

    final Context context = Context.getThreadContext();
    ConnectionResource con = null;

    try {
      con = context.getConnectionResource();
      id = Context.getDbType().getNewId(con.getConnection(), TABLE_NODE, "ID");
      histId = Context.getDbType().getNewId(con.getConnection(), TABLE_NODE,
             "historyid");

      PreparedStatement stmt = null;
      try {
        stmt = con.getConnection().prepareStatement(cmd.toString());
        stmt.setLong(1, id);
        stmt.setLong(2, typeId);
        stmt.setLong(3, histId);
        stmt.setInt(4, 0);
        stmt.setInt(5, 0);
        stmt.setString(6, _name);

        final int rows = stmt.executeUpdate();
        if (rows == 0) {
//           TODO fehler schmeissen
        }
      } finally {
        stmt.close();
      }
    con.commit();
  } catch (final SQLException e) {
//    TODO fehler schmeissen
  } finally {
    if ((con != null) && con.isOpened()) {
      con.abort();
    }
  }
    return new Node(id, Type.get(typeId), histId, null, null, _name, null);
  }

  public static Node getNodeFromDB(final Long _nodeId) throws EFapsException {
    final StringBuilder cmd = new StringBuilder();
    cmd.append(" select t_eanode.id, typeid, historyid, copyid, t_eanode.revision, name, t_earevision.repositoryid from ")
      .append(TABLE_NODE)
      .append(" left join t_earevision on t_earevision.nodeid = t_eanode.id")
      .append(" where t_eanode.id = ?");
    final ConnectionResource con = Context.getThreadContext().getConnectionResource();
    Node ret = null;
    try {

     PreparedStatement stmt = null;
     try {
       stmt = con.getConnection().prepareStatement(cmd.toString());

       stmt.setLong(1, _nodeId);
       final ResultSet resultset = stmt.executeQuery();

       if (resultset.next()) {
         ret = new Node(resultset.getLong(1), Type.get(resultset.getLong(2)),
             resultset.getLong(3), resultset.getLong(4), resultset.getLong(5),
             resultset.getString(6), resultset.getLong(7));
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


  public static Node getRootNodeFromDB(final Repository _repository) throws EFapsException {

    final StringBuilder cmd = new StringBuilder();
    cmd.append("  SELECT t_eanode.id,t_eanode.typeid, t_eanode.historyid,  copyid ,t_earevision.revision,t_eanode.name, repositoryid from ")
      .append("t_earevision")
      .append(" join t_earepository on t_earevision.repositoryid = t_earepository.id and t_earevision.revision = t_earepository.lastrevision")
      .append(" join t_eanode on t_eanode.id = t_earevision.nodeid")
      .append(" where repositoryid = ?");
    final ConnectionResource con = Context.getThreadContext().getConnectionResource();
    Node ret = null;
    try {

     PreparedStatement stmt = null;
     try {
       stmt = con.getConnection().prepareStatement(cmd.toString());

       stmt.setLong(1, _repository.getId());
       final ResultSet resultset = stmt.executeQuery();

       if (resultset.next()) {
         ret = new Node(resultset.getLong(1), Type.get(resultset.getLong(2)),
             resultset.getLong(3), resultset.getLong(4), resultset.getLong(5),
             resultset.getString(6), resultset.getLong(7));
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
   * @param nodeid
   * @throws EFapsException
   */
  public void connectRevise(final Node _parentNode) throws EFapsException {
    final Node reviseNode = _parentNode.getNodeClone();
    final List<Node> children = _parentNode.getChildNodes(getAncestor());
    children.add(this);
    Node2Node.connect(reviseNode, children);
    if (reviseNode.isRoot()) {
      Revision.getNewRevision(new Repository(reviseNode.getRepositoryId()),
                                             reviseNode);
    } else {
      final Node parent = _parentNode.getParentNode();
      reviseNode.connectRevise(parent);
    }
  }

  public Node getParentNode() throws EFapsException{

  final StringBuilder cmd = new StringBuilder();
  cmd.append("SELECT id, typeid, parentid, childid,HISTORYID,COPYID, REVISION,NAME,nodetype,repositoryid from ")
    .append(" V_EANODE2NODEHIGHREV ")
    .append(" where childid = ?");
  final ConnectionResource con = Context.getThreadContext().getConnectionResource();
  Node ret = null;
  try {

   PreparedStatement stmt = null;
   try {
     stmt = con.getConnection().prepareStatement(cmd.toString());

     stmt.setLong(1, this.id);
     final ResultSet resultset = stmt.executeQuery();

     if (resultset.next()) {
       ret = new Node(resultset.getLong(3), Type.get(resultset.getLong(9)),
           resultset.getLong(5), resultset.getLong(6), resultset.getLong(7),
           resultset.getString(8), resultset.getLong(10));
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
   * @return
   * @throws EFapsException
   *
   */
  private List<Node> getChildNodes(final Node _excludeNode) throws EFapsException {
    // TODO Auto-generated method stub

    final StringBuilder cmd = new StringBuilder();
    cmd.append(" SELECT id, typeid, childid, name, revision, nodetype from ")
      .append(" v_eanode2node")
      .append(" where parentid = ?");
    final ConnectionResource con = Context.getThreadContext().getConnectionResource();
    final List<Node> ret = new ArrayList<Node>();
    try {

     PreparedStatement stmt = null;
     try {
       stmt = con.getConnection().prepareStatement(cmd.toString());

       stmt.setLong(1, this.id);
       final ResultSet resultset = stmt.executeQuery();

       while (resultset.next()) {
         final Long childid = resultset.getLong(3);
         boolean add = true;
         if (_excludeNode != null) {
           if (childid == _excludeNode.getId()) {
             add = false;
           }
         }
         if (add) {
           ret.add(new Node(childid, Type.get(resultset.getLong(2)),
                 null, null, resultset.getLong(5), resultset.getString(3), null));
         }
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


  public Node getNodeClone() throws EFapsException {
    final StringBuilder cmd = new StringBuilder();

    cmd.append("insert into ").append(TABLE_NODE)
      .append("(id, typeid, historyid, copyid, revision, name)")
      .append(" values (?,?,?,?,?,?)");

    Long idTmp = null;

    final Context context = Context.getThreadContext();
    ConnectionResource con = null;

    try {
      con = context.getConnectionResource();
      idTmp = Context.getDbType().getNewId(con.getConnection(),
                                           TABLE_NODE,
                                           "ID");

      PreparedStatement stmt = null;
      try {
        stmt = con.getConnection().prepareStatement(cmd.toString());
        stmt.setLong(1, idTmp);
        stmt.setLong(2, this.type.getId());
        stmt.setLong(3, this.historyId);
        stmt.setLong(4, this.copyId);
        stmt.setInt(5, 0);
        stmt.setString(6, this.name);

        final int rows = stmt.executeUpdate();
        if (rows == 0) {
//           TODO fehler schmeissen
        }
      } finally {
        stmt.close();
      }
    con.commit();
  } catch (final SQLException e) {
//    TODO fehler schmeissen
  } finally {
    if ((con != null) && con.isOpened()) {
      con.abort();
    }
  }

  final Node ret = new Node(idTmp, this.type, this.historyId, this.copyId, null, this.name, this.repositoryId);
  ret.setAncestor(this);
  return ret;
  }


  /**
   * @param ret
   */
  private void setAncestor(final Node _ancestor) {
    this.ancestor = _ancestor;
  }


  public boolean isRoot() {
    return this.repositoryId != null && this.repositoryId > 0;
  }


  /**
   * @param _name
   * @throws EFapsException
   */
  public void rename(final String _name) throws EFapsException {
    this.name = _name;
    //make a clone
    final Node clone = getNodeClone();
    //connect existing children to clone
    final List<Node> children = getChildNodes(null);
    Node2Node.connect(clone, children);
    final Node parent = getParentNode();
    clone.connectRevise(parent);
  }
}
