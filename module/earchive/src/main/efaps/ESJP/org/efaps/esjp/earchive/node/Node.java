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

import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.efaps.admin.datamodel.Type;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Checkout;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.esjp.earchive.INames;
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
public class Node implements INames {

  /**
   * Id of this Node.
   */
  private final Long id;

  /**
   * HistoryId of the Node.<br>
   * The historyid is the id that is always the same
   * for all revisions of this node, "it stays the same during history".
   */
  private final Long historyId;

  /**
   * CopyId of the Node. The copyId is changed if a node is created by copying
   * it from another node.
   */
  private final Long copyId;

  private final List<Node> children = new ArrayList<Node>();

  /**
   * Revision the Node was committed with.
   */
  private Long comittedRevision;

  /**
   * Type of the node. There are file nodes and directory nodes.
   */
  private Type type;

  /**
   * Name of the Node.
   */
  private String name;

  /**
   * Id of the Repository this node belongs to. This id is necessary because in
   * eFaps it is possible to store various Repositories in the same
   * eFaps-Database.
   */
  private Long repositoryId;

  /**
   * In the case that this node is a file node, the id of the related file is
   * stored here.
   */
  private Long fileId;

  /**
   * Id of the set of properties belonging to this node.
   */
  private Long propSetId;

  /**
   * Node that this node was cloned from.
   */
  private Node ancestor;

  /**
   * Id Path of this Node. The id path is a path made out of the ids from the
   * parent - child relation. In comparison to the path it does not change when
   * a node is renamed. <br>
   * The idpath is constructed like this: <br><code>
   * [parent history id] SEPERATOR_IDS [parent copy id] SEPERATOR_INSTANCE
   * [child history id] SEPERATOR_IDS [child copy id] ... </code><br>
   * e.g.:<br>
   * 23:0-25:1-27:0
   */
  private String idPath;

  /**
   * Parent node of this node.
   */
  private Node parent;

  /**
   * This boolean stores if the children of this node where already resolved, or
   * if the database must be queried still.
   */
  private boolean childrenResolved = false;

  /**
   * Is this node the root node of the repository.
   */
  private boolean root;

  /**
   * Path of this node. The path is constructed like a normal path in a file
   * system, using a seperator.<br><code>
   * [name of the parent node] SEPERATOR_PATH [name of the child node]</code>
   */
  private String path;

  /**
   * Node that a new Node will be connect to on bubble up.
   */
  private Node connectTarget;

  /**
   * Constructor is needed to be able to use this class in eFpas as an esjp.
   */
  public Node() {
    this.id = null;
    this.historyId = null;
    this.copyId = null;
    this.comittedRevision = null;
  }



  /**
   * Constructor.
   *
   * @param _id           id of this node
   * @param _type         type of this node
   * @param _historyId    history id of this node
   * @param _copyId       copy id of this node
   * @param _revision     revision of this node
   * @param _name         name of this node
   * @param _repositoryId id of the repository this node belongs to
   * @param _fileId       id of the file if this node is a file node., else null
   * @param _propSetId    id of the property set
   */
  private Node (final Long _id, final Type _type, final Long _historyId,
                final Long _copyId, final Long _revision, final String _name,
                final Long _repositoryId, final Long _fileId,
                final Long _propSetId) {
    this.id = _id;
    this.type = _type;
    this.historyId = _historyId != null ? _historyId : new Long(0);
    this.copyId = _copyId != null ? _copyId : new Long(0);
    this.comittedRevision = _revision != null ? _revision : new Long(0);
    this.name = _name != null ? _name.trim() : null;
    this.repositoryId = _repositoryId;
    this.fileId = _fileId;
    this.propSetId = _propSetId;
  }

  /**
   * Setter method for instance variable {@link #root}.
   *
   * @param _root value for instance variable {@link #root}
   */
  public void setRoot(final boolean _root) {
    this.root = _root;
  }

  /**
   * Getter method for instance variable {@link #root}.
   *
   * @return value of instance variable {@link #root}
   */
  public boolean isRoot() {
    return this.root;
  }


  /**
   * Setter method for instance variable {@link #parent}.
   *
   * @param _parent value for instance variable {@link #parent}
   */
  public void setParent(final Node _parent) {
    this.parent = _parent;
  }


  /**
   * Getter method for instance variable {@link #parent}.
   *
   * @return value of instance variable {@link #parent}
   */
  public Node getParent() {
    return this.parent;
  }


  /**
   * Getter method for instance variable {@link #connectTarget}.
   *
   * @return value of instance variable {@link #connectTarget}
   */
  public Node getConnectTarget() {
    return this.connectTarget;
  }


  /**
   * @param parentNode
   */
  public void setConnectTarget(final Node parentNode) {
    this.connectTarget = parentNode;
  }



  /**
   * Getter method for instance variable {@link #children}.
   *
   * @return value of instance variable {@link #children}
   * @throws EFapsException
   */
  public List<Node> getChildren() throws EFapsException {
    if (!this.childrenResolved) {
      this.children.addAll(getChildNodes(null, null, false));
      this.childrenResolved = true;
    }
    return this.children;
  }


  /**
   * Getter method for instance variable {@link #comittedRevision}.
   *
   * @return value of instance variable {@link #comittedRevision}
   */
  public Long getComittedRevision() {
    return this.comittedRevision;
  }


  /**
   * Setter method for instance variable {@link #comittedRevision}.
   *
   * @param comittedRevision value for instance variable {@link #comittedRevision}
   */
  public void setComittedRevision(final Long comittedRevision) {
    this.comittedRevision = comittedRevision;
  }


  /**
   * Getter method for instance variable {@link #propSetId}.
   *
   * @return value of instance variable {@link #propSetId}
   */
  public Long getPropSetId() {
    return this.propSetId;
  }

  public boolean hasProperties() {
    return this.propSetId != null && this.propSetId > 0;
  }

  /**
   * Getter method for instance variable {@link #fileId}.
   *
   * @return value of instance variable {@link #fileId}
   */
  public Long getFileId() {
    return this.fileId;
  }


  /**
   * Getter method for instance variable {@link #type}.
   *
   * @return value of instance variable {@link #type}
   */
  public Type getType() {
    return this.type;
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
   * Getter method for instance variable {@link #name}.
   *
   * @return value of instance variable {@link #name}
   */
  public String getName() {
    return this.name;
  }


  /**
   * Getter method for instance variable {@link #idPath}.
   *
   * @return value of instance variable {@link #idPath}
   */
  public String getIdPath() {
    return this.idPath;
  }


  /**
   * Setter method for instance variable {@link #idPath}.
   *
   * @param _idPath value for instance variable {@link #idPath}
   */
  public void setIdPath(final String _idPath) {
    this.idPath = _idPath;
  }


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

  /**
   * @return
   */
  public Repository getRepository() {
    final Repository ret;
    if (this.repositoryId != null) {
      ret = new Repository(this.repositoryId);
    } else {
      ret = null;
    }
    return ret;
  }

  /**
   * Getter method for instance variable {@link #historyId}.
   *
   * @return value of instance variable {@link #historyId}
   */
  public Long getHistoryId() {
    return this.historyId;
  }

  /**
   * Getter method for instance variable {@link #copyId}.
   *
   * @return value of instance variable {@link #copyId}
   */
  public Long getCopyId() {
    return this.copyId;
  }

  /**
   * Getter method for instance variable {@link #path}.
   *
   * @return value of instance variable {@link #path}
   */
  public String getPath() {
    return this.path;
  }

  /**
   * Setter method for instance variable {@link #path}.
   *
   * @param path value for instance variable {@link #path}
   */
  public void setPath(final String path) {
    this.path = path;
  }

  public boolean isFile () {
    return this.fileId != null && this.fileId > 0;
  }

  public InputStream getFile() throws Exception {
    final Checkout checkout = new Checkout(Instance.get(Type.get(TYPE_FILE),
                                                        this.fileId));
    checkout.preprocess();
    return checkout.execute();
  }

  /**
   * Method creates a new node in the eFaps database and returns an instance of
   * this node.
   *
   * @param _repository   Repository the the newly created node belongs to.
   * @param _name         name of the newly created node
   * @param _type         type of the node to be created
   * @param _properties   properties for the the newly created node
   * @param _fileId       file id for the newly created node. If null, but the
   *                      type of the node is a file node a new file will be
   *                      created automatically
   * @return  new Node
   * @throws EFapsException
   */
  public static Node createNewNode(final Repository _repository,
                                   final String _name, final String _type,
                                   final Map<String, String> _properties,
                                   final Long _fileId)
      throws EFapsException {

    Long fileIdTmp = null;
    if (_type.equals(TYPE_NODEFILE)) {
     fileIdTmp = (_fileId != null && _fileId > 0)
                 ? _fileId
                 : EFapsFile.createFile().getId();
    }
    Long propSetId = null;
    if (_properties != null) {
      propSetId = insertProperties(_properties);
    }

    final StringBuilder cmd = new StringBuilder();

    final Long typeId = Type.get(_type).getId();

    cmd.append("insert into ").append(TABLE_NODE)
      .append("(id, typeid, historyid, copyid, revision, name, repositoryid");

    if (fileIdTmp != null) {
      cmd.append(",").append(TABLE_NODE_C_FILEID);
    }
    if (propSetId != null) {
      cmd.append(",").append(TABLE_NODE_C_PROPSETID);
    }
    cmd.append(")")
      .append(" values (?,?,?,?,?,?,?").append(fileIdTmp != null ? ",?" : "")
      .append(propSetId != null ? ",?" : "").append(")");

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
        stmt.setLong(7, _repository.getId());
        int propset = 8;
        if (fileIdTmp != null) {
          stmt.setLong(8, fileIdTmp);
          propset = 9;
        }
        if (propSetId != null) {
            stmt.setLong(propset, propSetId);
        }
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
   return new Node(id, Type.get(typeId), histId, null, null, _name,
                   _repository.getId(), fileIdTmp, propSetId);
  }

  private static long insertProperties(final Map<String, String> _properties)
      throws EFapsException {
    long ret = 0;
    final StringBuilder cmd = new StringBuilder();
    cmd.append("insert into ").append(TABLE_PROPERTYSET)
      .append("(")
      .append(TABLE_PROPERTYSET_C_ID)
      .append(") values (?)");

    final StringBuilder cmd2 = new StringBuilder();
    cmd2.append("insert into ").append(TABLE_PROPERTY)
      .append("(")
      .append(TABLE_PROPERTY_C_ID).append(",")
      .append(TABLE_PROPERTY_C_SETID).append(",")
      .append(TABLE_PROPERTY_C_KEY).append(",")
      .append(TABLE_PROPERTY_C_VALUE)
      .append(") values (?,?,?,?)");


    final Context context = Context.getThreadContext();
    ConnectionResource con = null;
    try {
      con = context.getConnectionResource();
      ret = Context.getDbType().getNewId(con.getConnection(), TABLE_PROPERTYSET,
                                         "ID");

      PreparedStatement stmt = null;
      PreparedStatement stmt2 = null;

      try {
        stmt = con.getConnection().prepareStatement(cmd.toString());
        stmt.setLong(1, ret);
        final int row = stmt.executeUpdate();

        stmt2 = con.getConnection().prepareStatement(cmd2.toString());

        for (final Entry<String, String> entry : _properties.entrySet()) {
          final long idTmp = Context.getDbType().getNewId(con.getConnection(),
                                               TABLE_PROPERTY,
                                               "ID");
          stmt2.setLong(1, idTmp);
          stmt2.setLong(2, ret);
          stmt2.setString(3, entry.getKey());
          stmt2.setString(4, entry.getValue());
          stmt2.addBatch();
        }
        final int[] rows = stmt2.executeBatch();

        if (row == 0) {
  //         TODO fehler schmeissen
        }
      } finally {
        stmt.close();
        stmt2.close();
      }
      con.commit();
    } catch (final SQLException e) {
    //  TODO fehler schmeissen
      e.printStackTrace();
    } finally {
      if ((con != null) && con.isOpened()) {
        con.abort();
      }
    }
    return ret;
  }




  public static List<Node> getNodeHirachy(final String _instanceKey)
      throws EFapsException {
    final List<Node> ret = new ArrayList<Node>();
    String revision = null;
    final String instanceKey;
    if (_instanceKey.contains(SEPERATOR_REVISION)) {
      final int pos = _instanceKey.indexOf(SEPERATOR_REVISION);
      revision = _instanceKey.substring(pos + 1);
      instanceKey = _instanceKey.substring(0, pos);
    } else {
      instanceKey = _instanceKey;
    }
    final String[] pairs;
    if (instanceKey.contains(SEPERATOR_INSTANCE)) {
      pairs = instanceKey.split(SEPERATOR_INSTANCE_RE);
    } else {
      pairs = new String[]{instanceKey};
    }

    final StringBuilder cmd = new StringBuilder();
    cmd.append("select")
      .append(" t_eanode.id").append(",")
      .append(" t_eanode.typeid").append(",")
      .append(" t_eanode.historyid").append(",")
      .append(" t_eanode.copyid").append(",")
      .append(" t_earevision.revision").append(",")
      .append(" t_eanode.name").append(",")
      .append(" t_eanode.repositoryid").append(",")
      .append(TABLE_NODE_C_PROPSETID);
   if (revision == null) {
      cmd.append(" from").append(" t_earepository")
         .append(" join").append(" t_earevision").append(" on ")
        .append(" t_earevision.repositoryid")
        .append("=").append(" t_earepository.id")
      .append(" and").append(" t_earevision.revision").append("=")
      .append(" t_earepository.lastrevision");
   } else {
     cmd.append(" from ").append(TABLE_REVISION);
   }
     cmd.append(" join ").append(TABLE_NODE)
      .append(" on ").append(TABLE_NODE_T_C_ID)
      .append("=").append(" t_earevision.nodeid")
      .append(" where ").append(" t_eanode.historyid").append("=?")
      .append(" and ").append(" t_eanode.copyid").append("=? ");
    if (revision != null) {
      cmd.append(" and ").append(TABLE_NODE_T_C_REVISION).append("=?");
    }
    final ConnectionResource con
                          = Context.getThreadContext().getConnectionResource();
    try {

      PreparedStatement stmt = null;
      try {
        stmt = con.getConnection().prepareStatement(cmd.toString());
        final String[] pair = pairs[0].split(SEPERATOR_IDS_RE);
        stmt.setLong(1, Long.parseLong(pair[0]));
        stmt.setLong(2, Long.parseLong(pair[1]));
        if (revision != null) {
          stmt.setLong(3, Long.parseLong(revision));
        }
        final ResultSet resultset = stmt.executeQuery();
        Node current = null;
        if (resultset.next()) {
          final Node root = new Node(resultset.getLong(1),
                               Type.get(resultset.getLong(2)),
                               resultset.getLong(3),
                               resultset.getLong(4), resultset.getLong(5),
                               resultset.getString(6), resultset.getLong(7),
                               null, resultset.getLong(8));
          root.setRoot(true);
          ret.add(root);
          current = root;
        }
        resultset.close();

        for (int i = 1; i < pairs.length; i++) {
          final String[] childPair = pairs[i].split(SEPERATOR_IDS_RE);
          final Node child = current.getChildNode(Long.parseLong(childPair[0]),
                                          Long.parseLong(childPair[1]));
          ret.add(child);
          current = child;
        }

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
   * Method returns from the given Repository and revision the node for a path.
   * The node contains the whole parent/child relation like a tree.
   * The path must be a valid path in the given revision, because the tree is
   * climbed down following the given path.
   *
   * @param _repository
   * @param _revision
   * @param _path
   * @return
   * @throws EFapsException
   */
  public static Node getNodeFromDB(final Repository _repository,
                                   final long _revision,
                                   final CharSequence _path)
      throws EFapsException {
    final StringBuilder cmd = new StringBuilder();
    //statement for the rootnode
    cmd.append(" select ")
      .append(TABLE_NODE_T_C_ID).append(",")
      .append(TABLE_NODE_C_TYPEID).append(",")
      .append(TABLE_NODE_C_HISTORYID).append(",")
      .append(TABLE_NODE_C_COPYID).append(",")
      .append(TABLE_NODE_T_C_REVISION).append(",")
      .append(TABLE_NODE_C_NAME).append(",")
      .append(TABLE_NODE_T_C_REPOSITORYID).append(",")
      .append(TABLE_NODE_C_FILEID).append(",")
      .append(TABLE_NODE_C_PROPSETID)
      .append(" from ").append(TABLE_NODE)
      .append(" join ").append(TABLE_REVISION).append(" on ")
        .append(TABLE_REVISION_T_C_NODEID)
        .append(" = ").append(TABLE_NODE_T_C_ID)
      .append(" where ").append(TABLE_NODE_T_C_REPOSITORYID).append(" = ?")
      .append(" and ").append(TABLE_REVISION_T_C_REVISION).append(" = ?");

    final ConnectionResource con
                          = Context.getThreadContext().getConnectionResource();
    Node ret = null;
    try {

      PreparedStatement stmt = null;
      try {
        stmt = con.getConnection().prepareStatement(cmd.toString());

        stmt.setLong(1, _repository.getId());
        stmt.setLong(2, _revision);
        final ResultSet resultset = stmt.executeQuery();

        if (resultset.next()) {
          ret = new Node(resultset.getLong(1), Type.get(resultset.getLong(2)),
                         resultset.getLong(3), resultset.getLong(4),
                         resultset.getLong(5), resultset.getString(6),
                         resultset.getLong(7), resultset.getLong(8),
                         resultset.getLong(9));
          ret.setRoot(true);
          ret.setIdPath(ret.getHistoryId() + SEPERATOR_IDS + ret.getCopyId());
          ret.setPath(ret.getName());
        }
        resultset.close();
      } finally {
        stmt.close();
      }
      final String[] childNames = _path.toString().split("/");
      for (final String childName :  childNames) {
        if (childName.length() > 0) {
          ret = ret.getChildNode(childName);
        }
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


  public static Node getNodeFromDB(final Long _nodeId, final String _idPath)
      throws EFapsException {
    final StringBuilder cmd = new StringBuilder();
    cmd.append(" select ")
      .append(TABLE_NODE_T_C_ID).append(",")
      .append(TABLE_NODE_C_TYPEID).append(",")
      .append(TABLE_NODE_C_HISTORYID).append(",")
      .append(TABLE_NODE_C_COPYID).append(",")
      .append(TABLE_NODE_T_C_REVISION).append(",")
      .append(TABLE_NODE_C_NAME).append(",")
      .append(TABLE_NODE_T_C_REPOSITORYID).append(",")
      .append(TABLE_NODE_C_FILEID).append(",")
      .append(TABLE_REVISION_T_C_REPOSITORYID).append(",")
      .append(TABLE_NODE_T_C_PROPSETID)
      .append(" from ").append(TABLE_NODE)
      .append(" left join ").append(TABLE_REVISION).append(" on ")
        .append(TABLE_REVISION).append(".").append(TABLE_REVISION_C_NODEID)
        .append(" = ").append(TABLE_NODE_T_C_ID)
      .append(" where ").append(TABLE_NODE_T_C_ID).append(" = ?");

    final ConnectionResource con
                          = Context.getThreadContext().getConnectionResource();
    Node ret = null;
    try {

     PreparedStatement stmt = null;
     try {
       stmt = con.getConnection().prepareStatement(cmd.toString());

       stmt.setLong(1, _nodeId);
       final ResultSet resultset = stmt.executeQuery();

       if (resultset.next()) {
          ret = new Node(resultset.getLong(1), Type.get(resultset.getLong(2)),
                         resultset.getLong(3), resultset.getLong(4),
                         resultset.getLong(5), resultset.getString(6),
                         resultset.getLong(7), resultset.getLong(8),
                         resultset.getLong(10));
         ret.setIdPath(_idPath);
         ret.setRoot(resultset.getLong(9) > 0);
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


  public static Node getRootNodeFromDB(final Repository _repository)
      throws EFapsException {
    final StringBuilder cmd = new StringBuilder();
    cmd.append("select ")
      .append(TABLE_NODE_T_C_ID).append(",")
      .append(TABLE_NODE_T_C_TYPEID).append(",")
      .append(TABLE_NODE_C_HISTORYID).append(",")
      .append(TABLE_NODE_C_COPYID).append(",")
      .append(TABLE_REVISION_T_C_REVISION).append(",")
      .append(TABLE_NODE_T_C_NAME).append(",")
      .append(TABLE_NODE_T_C_REPOSITORYID).append(",")
      .append(TABLE_NODE_T_C_PROPSETID)
      .append(" from ").append(TABLE_REVISION)
      .append(" join ").append(TABLE_REPOSITORY).append(" on ")
        .append(TABLE_REVISION_T_C_REPOSITORYID).append(" = ")
        .append(TABLE_REPOSITORY_T_C_ID).append(" and ")
        .append(TABLE_REVISION_T_C_REVISION).append(" = ")
        .append(TABLE_REPOSITORY_T_C_LASTREVISION)
      .append(" join ").append(TABLE_NODE).append(" on ")
        .append(TABLE_NODE_T_C_ID).append(" = ")
        .append(TABLE_REVISION_T_C_NODEID)
      .append(" where ").append(TABLE_REVISION_T_C_REPOSITORYID).append(" = ?");

    final ConnectionResource con
                          = Context.getThreadContext().getConnectionResource();
    Node ret = null;
    try {

     PreparedStatement stmt = null;
     try {
       stmt = con.getConnection().prepareStatement(cmd.toString());

       stmt.setLong(1, _repository.getId());
       final ResultSet resultset = stmt.executeQuery();

       if (resultset.next()) {
         final Long historyIdTmp = resultset.getLong(3);
         final Long copyIdTmp = resultset.getLong(4);
         ret = new Node(resultset.getLong(1), Type.get(resultset.getLong(2)),
                        historyIdTmp, copyIdTmp , resultset.getLong(5),
                        resultset.getString(6), resultset.getLong(7), null,
                        resultset.getLong(8));
         ret.setIdPath(historyIdTmp + SEPERATOR_IDS + copyIdTmp);
         ret.setRoot(true);
         ret.setPath(ret.getName());
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

  public static Revision multiBubbleUp(final List<Node> _nodes, final String _msg) throws EFapsException {
    Node current = null;
    final Map <String, Node> idPath2Node = new HashMap<String, Node>();

    for (final Node node : _nodes) {

      final Node targetParent = node.getConnectTarget();
      final List<Node> childrenTmp;

      if (idPath2Node.containsKey(targetParent.getIdPath())) {
        current = idPath2Node.get(targetParent.getIdPath());
        childrenTmp= new ArrayList<Node>();
      } else {
        current = targetParent.getNodeClone();
        idPath2Node.put(current.getIdPath(), current);
        final List<Node> exclude = new ArrayList<Node>();
        exclude.add(current);
        childrenTmp = targetParent.getChildNodes(exclude, null, false);
      }
      childrenTmp.add(node);
      Node2Node.connect(current, childrenTmp);

      while (!current.isRoot()) {
        final Node parentTmp = current.getParent();
        final Node rev;
        final List<Node> newchildren;
        if (idPath2Node.containsKey(parentTmp.getIdPath())) {
          rev = idPath2Node.get(parentTmp.getIdPath());
          newchildren= new ArrayList<Node>();
        } else {
          rev = parentTmp.getNodeClone();
          idPath2Node.put(rev.getIdPath(), rev);
          final List<Node> exclude = new ArrayList<Node>();
          exclude.add(current);
          newchildren = parentTmp.getChildNodes(exclude, null, false);
        }
        newchildren.add(current);
        Node2Node.connect(rev, newchildren);
        current = rev;
      }
    }
    return Revision.getNewRevision(new Repository(current.getRepositoryId()),
        current, _msg);
  }




  private Node getChildNode(final long _historyId,
                            final long _copyId) throws EFapsException {
    Node ret = null;
    final StringBuilder cmd = new StringBuilder();
    cmd.append(" select ")
      .append(VIEW_NODE2NODE_C_CHILDID).append(",")
      .append(VIEW_NODE2NODE_C_NODETYPE).append(",")
      .append(VIEW_NODE2NODE_C_HISTORYID).append(",")
      .append(VIEW_NODE2NODE_C_COPYID).append(",")
      .append(VIEW_NODE2NODE_C_REVISION).append(",")
      .append(VIEW_NODE2NODE_C_NAME).append(",")
      .append(VIEW_NODE2NODE_C_PROPSETID)
      .append(" from ").append(VIEW_NODE2NODE)
      .append(" where ").append(VIEW_NODE2NODE_C_PARENTID).append(" = ?")
      .append(" and ").append(VIEW_NODE2NODE_C_HISTORYID).append(" = ?")
      .append(" and ").append(VIEW_NODE2NODE_C_COPYID).append(" = ?");

    final ConnectionResource con
                          = Context.getThreadContext().getConnectionResource();
    try {

      PreparedStatement stmt = null;
      try {
        stmt = con.getConnection().prepareStatement(cmd.toString());
        stmt.setLong(1, this.id);
        stmt.setLong(2, _historyId);
        stmt.setLong(3, _copyId);

        final ResultSet resultset = stmt.executeQuery();

        if (resultset.next()) {
          ret = new Node(resultset.getLong(1), Type.get(resultset.getLong(2)),
                         resultset.getLong(3), resultset.getLong(4),
                         resultset.getLong(5), resultset.getString(6),
                         this.repositoryId, null, resultset.getLong(7));
          ret.setPath(this.path + SEPERATOR_PATH + ret.getName());
          ret.setIdPath(this.idPath + SEPERATOR_INSTANCE + ret.getHistoryId() + SEPERATOR_IDS + ret.getCopyId());
          ret.setParent(this);
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

  private Node getChildNode(final String _name) throws EFapsException {
    Node ret = null;
    final StringBuilder cmd = new StringBuilder();
    cmd.append(" select ")
      .append(VIEW_NODE2NODE_C_CHILDID).append(",")
      .append(VIEW_NODE2NODE_C_NODETYPE).append(",")
      .append(VIEW_NODE2NODE_C_HISTORYID).append(",")
      .append(VIEW_NODE2NODE_C_COPYID).append(",")
      .append(VIEW_NODE2NODE_C_REVISION).append(",")
      .append(VIEW_NODE2NODE_C_NAME).append(",")
      .append(VIEW_NODE2NODE_C_FILEID).append(",")
      .append(VIEW_NODE2NODE_C_PROPSETID)
      .append(" from ").append(VIEW_NODE2NODE)
    .append(" where ").append(VIEW_NODE2NODE_C_PARENTID).append(" = ?")
    .append(" and ").append(VIEW_NODE2NODE_C_NAME).append(" = ?");

    final ConnectionResource con = Context.getThreadContext()
                                                      .getConnectionResource();
    try {
      PreparedStatement stmt = null;
      try {
        stmt = con.getConnection().prepareStatement(cmd.toString());
        stmt.setLong(1, this.id);
        stmt.setString(2, _name);
        final ResultSet resultset = stmt.executeQuery();

        if (resultset.next()) {
          ret = new Node(resultset.getLong(1), Type.get(resultset.getLong(2)),
                         resultset.getLong(3), resultset.getLong(4),
                         resultset.getLong(5), resultset.getString(6),
                         this.repositoryId, resultset.getLong(7),
                         resultset.getLong(8));
          ret.setPath(this.path + SEPERATOR_PATH + ret.getName());
          ret.setIdPath(this.idPath + SEPERATOR_INSTANCE + ret.getHistoryId() + SEPERATOR_IDS + ret.getCopyId());
          ret.setParent(this);
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
   * @return
   * @throws EFapsException
   */
  public List<Node> connect2Parent(final Node _parentNode,
                                   final String _commitMsg)
      throws EFapsException {
    final List<Node> nodes = getNodeHirachy(_parentNode.getIdPath());
    return bubbleUp(nodes, _commitMsg);
  }

  public List<Node> bubbleUp(final List<Node> _nodes, final String _commitMsg)
      throws EFapsException{
    final List<Node> ret = new ArrayList<Node>();
    Collections.reverse(_nodes);
    final Iterator<Node> iter = _nodes.iterator();
    Node current = this;
    while (iter.hasNext()) {
      final Node node = iter.next();
      final Node reviseNode = node.getNodeClone();
      ret.add(reviseNode);
      final List<Node> list = new ArrayList<Node>();
      if (current.getAncestor() != null) {
        list.add(current.getAncestor());
      }
      final List<Node> childrenTmp = node.getChildNodes(list, null, false);
      childrenTmp.add(current);
      Node2Node.connect(reviseNode, childrenTmp);
      current = reviseNode;
    }
    final Revision rev = Revision.getNewRevision(new Repository(current.getRepositoryId()),
                                                      current, _commitMsg);
    for (final Node node: ret) {
      node.setComittedRevision(rev.getRevision());
    }
    return ret;
  }

  /**
   * Get this node as it was in <code>_revision</code>. It is necessary to
   * always start at the root node and then climb down the tree, because it is
   * possible that the node as it was in revision 300 is wanted, but the actual
   * revision of this node was 200.
   *
   * @param _revision the revision the revision of the node is wanted for
   * @return Node as it was in the given revision
   * @throws EFapsException
   */
  public Node getNodeInRevision(final long _revision) throws EFapsException {
    final StringBuilder cmd = new StringBuilder();
    //statement for the rootnode
    cmd.append(" select ")
      .append(TABLE_NODE_T_C_ID).append(",")
      .append(TABLE_NODE_C_TYPEID).append(",")
      .append(TABLE_NODE_C_HISTORYID).append(",")
      .append(TABLE_NODE_C_COPYID).append(",")
      .append(TABLE_NODE_T_C_REVISION).append(",")
      .append(TABLE_NODE_C_NAME).append(",")
      .append(TABLE_NODE_T_C_REPOSITORYID).append(",")
      .append(TABLE_NODE_C_FILEID).append(",")
      .append(TABLE_NODE_C_PROPSETID)
      .append(" from ").append(TABLE_NODE)
      .append(" join ").append(TABLE_REVISION).append(" on ")
        .append(TABLE_REVISION_T_C_NODEID)
        .append(" = ").append(TABLE_NODE_T_C_ID)
      .append(" where ").append(TABLE_NODE_T_C_REPOSITORYID).append(" = ?")
      .append(" and ").append(TABLE_REVISION_T_C_REVISION).append(" = ?");
    final ConnectionResource con
                          = Context.getThreadContext().getConnectionResource();
    Node ret = null;
    try {

      PreparedStatement stmt = null;
      try {
        stmt = con.getConnection().prepareStatement(cmd.toString());

        stmt.setLong(1, this.repositoryId);
        stmt.setLong(2, _revision);
        final ResultSet resultset = stmt.executeQuery();

        if (resultset.next()) {
          ret = new Node(resultset.getLong(1), Type.get(resultset.getLong(2)),
                         resultset.getLong(3), resultset.getLong(4),
                         resultset.getLong(5), resultset.getString(6),
                         resultset.getLong(7), resultset.getLong(8),
                         resultset.getLong(9));
          ret.setRoot(true);
          ret.setPath(ret.getName());
        }
        resultset.close();
      } finally {
        stmt.close();
      }
      //if this is not the root, it must be climbed down the tree
      if (!this.root) {
        final String [] childIdPaths = this.idPath.split(SEPERATOR_INSTANCE_RE);
        boolean first = true;
        for (final String childIdPath :  childIdPaths) {
          if (childIdPath.length() > 0 && !first && ret != null) {
            System.out.println("-----------------" + childIdPath);
            final String[] ids = childIdPath.split(SEPERATOR_IDS_RE);
            ret = ret.getChildNode(Long.parseLong(ids[0]),
                                   Long.parseLong(ids[1]));
          }
          first = false;
        }
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
   * Method to get all child nodes for this node.
   *
   * @param _excludeNodes  Nodes to be excluded from the list of child nodes,
   *                       <code>null</code> if all children must be returned
   * @param _revision      comiited revision that the child node must have,
   *                       <code>null</code> if this filter is not necessary
   * @return
   * @throws EFapsException
   */
  private List<Node> getChildNodes(final List<Node> _excludeNodes,
                                   final Long _revision,
                                   final boolean _compareById)
      throws EFapsException {

    final StringBuilder cmd = new StringBuilder();
    cmd.append(" select ")
      .append(VIEW_NODE2NODE_C_ID).append(",")
      .append(VIEW_NODE2NODE_C_NODETYPE).append(",")
      .append(VIEW_NODE2NODE_C_CHILDID).append(",")
      .append(VIEW_NODE2NODE_C_HISTORYID).append(",")
      .append(VIEW_NODE2NODE_C_COPYID).append(",")
      .append(VIEW_NODE2NODE_C_REVISION).append(",")
      .append(VIEW_NODE2NODE_C_NAME).append(",")
      .append(VIEW_NODE2NODE_C_FILEID).append(",")
      .append(VIEW_NODE2NODE_C_PROPSETID)
      .append(" from ")
      .append(VIEW_NODE2NODE)
      .append(" where ").append(VIEW_NODE2NODE_C_PARENTID).append(" = ?");
    if (_revision != null) {
      cmd.append(" and ").append(VIEW_NODE2NODE_C_REVISION).append(" = ?");
    }
    final ConnectionResource con
                          = Context.getThreadContext().getConnectionResource();
    final List<Node> ret = new ArrayList<Node>();
    try {

     PreparedStatement stmt = null;
     try {
       stmt = con.getConnection().prepareStatement(cmd.toString());

       stmt.setLong(1, this.id);
       if (_revision != null) {
         stmt.setLong(2, _revision);
       }
       final ResultSet resultset = stmt.executeQuery();

       while (resultset.next()) {
         final Long childid = resultset.getLong(3);
         final Long historyIdTmp = resultset.getLong(4);
         final Long copyIdTmp = resultset.getLong(5);
         boolean add = true;
         if (_excludeNodes != null) {
           for (final Node excludeNode : _excludeNodes) {
             if (_compareById) {
               if (childid == excludeNode.getId()) {
                 add = false;
                 break;
               }
             } else {
               if (historyIdTmp == excludeNode.getHistoryId()
                   && copyIdTmp == excludeNode.getCopyId()) {
                 add = false;
                 break;
               }
             }
           }

         }
         if (add) {
           final Node child = new Node(childid, Type.get(resultset.getLong(2)),
                                       historyIdTmp, copyIdTmp,
                                       resultset.getLong(6),
                                       resultset.getString(7),
                                       this.repositoryId, resultset.getLong(8),
                                       resultset.getLong(9));
           child.setParent(this);
           child.setPath(this.path + SEPERATOR_PATH + child.name);
           child.setIdPath(this.idPath + SEPERATOR_INSTANCE + child.getHistoryId() + SEPERATOR_IDS + child.getCopyId());
           ret.add(child);
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
      .append("(id, typeid, historyid, copyid, revision, name, repositoryid");
    if (this.fileId != null) {
      cmd.append(",").append(TABLE_NODE_C_FILEID);
    }
    if (this.propSetId != null) {
      cmd.append(",").append(TABLE_NODE_C_PROPSETID);
    }
    cmd.append(")")
      .append(" values (?,?,?,?,?,?,?")
      .append(this.fileId == null ? "" : ",?")
      .append(this.propSetId == null ? "" : ",?").append(")");

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
        stmt.setLong(7, this.repositoryId);
        int propset = 8;
        if (this.fileId != null) {
          stmt.setLong(8, this.fileId);
          propset = 9;
        }
        if (this.propSetId != null) {
            stmt.setLong(propset, this.propSetId);
        }

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
    e.printStackTrace();
  } finally {
    if ((con != null) && con.isOpened()) {
      con.abort();
    }
  }

  final Node ret = new Node(idTmp, this.type, this.historyId, this.copyId, null,
                            this.name, this.repositoryId, this.fileId,
                            this.propSetId);
  ret.setAncestor(this);
  ret.setIdPath(this.idPath);
  ret.setPath(this.path);
  ret.setParent(this.parent);
  ret.setRoot(isRoot());
  return ret;
  }


  /**
   * @param ret
   */
  private void setAncestor(final Node _ancestor) {
    this.ancestor = _ancestor;
  }

  /**
   * @param _name
   * @return
   * @throws EFapsException
   */
  public List<Node> rename(final String _name, final String _commitMsg)
      throws EFapsException {
    final List<Node> ret = new ArrayList<Node>();
    this.name = _name;
    //make a clone
    final Node clone = getNodeClone();
    ret.add(clone);
    //connect existing children to clone
    final List<Node> childrenTmp = getChildNodes(null, null, true);
    Node2Node.connect(clone, childrenTmp);
    final List<Node> nodes = getNodeHirachy(this.idPath);
    // remove last node from the hirachy because it is the node that was renamed
    nodes.remove(nodes.size() - 1);
    clone.bubbleUp(nodes, _commitMsg);
    return ret;
  }

  public List<Node> updateFile(final String _commitMsg) throws EFapsException {
    final List<Node> ret = new ArrayList<Node>();
    //make a clone
    this.fileId = EFapsFile.createFile().getId();
    final Node clone = getNodeClone();
    ret.add(clone);
    final List<Node> nodes = getNodeHirachy(this.idPath);
    // remove last node from the hirachy because it is this node
    nodes.remove(nodes.size() - 1);
    clone.bubbleUp(nodes, _commitMsg);
    return ret;
  }



  public List<Node> deleteChildren(final String[] _instanceKeys,
                                   final String _commitMsg)
      throws EFapsException{
    final List<Node> ret = new ArrayList<Node>();
    final List<Node> remove = new ArrayList<Node>();
    for (final String key : _instanceKeys) {
      final String inst = key.substring(key.lastIndexOf(SEPERATOR_INSTANCE) + 1);
      final String[] ids = inst.split(SEPERATOR_IDS_RE);
      remove.add(new Node(null, null, Long.parseLong(ids[0]),
                          Long.parseLong(ids[1]), null, null, null, null, null));
    }
    final List<Node> childrenTmp = getChildNodes(remove, null, false);
    final Node clone = getNodeClone();
    Node2Node.connect(clone, childrenTmp);
    final List<Node> nodes = getNodeHirachy(this.idPath);
    // remove last node from the hirachy because it is this node
    nodes.remove(nodes.size() - 1);
    clone.bubbleUp(nodes, _commitMsg);
    ret.add(clone);
    return ret;
  }


  public static void updateRevisions() throws EFapsException {
    final StringBuilder cmd = new StringBuilder();
    cmd.append("select ")
      .append(TABLE_NODE_T_C_ID).append(",")
      .append(TABLE_NODE_C_TYPEID).append(",")
      .append(TABLE_NODE_C_HISTORYID).append(",")
      .append(TABLE_NODE_C_COPYID).append(",")
      .append(TABLE_REVISION_T_C_REVISION).append(",")
      .append(TABLE_NODE_C_NAME).append(",")
      .append(TABLE_NODE_T_C_REPOSITORYID)
      .append(" from ").append(TABLE_NODE)
      .append(" join ").append(TABLE_REVISION).append(" on ")
        .append(TABLE_NODE_T_C_ID).append(" = ")
        .append(TABLE_REVISION_C_NODEID)
      .append(" order by ").append(TABLE_REVISION_T_C_REVISION);

    final ConnectionResource con
                          = Context.getThreadContext().getConnectionResource();
    final List<Node> nodes = new ArrayList<Node>();
    try {

     Statement stmt = null;
     try {
       stmt = con.getConnection().createStatement();
       final ResultSet resultset = stmt.executeQuery(cmd.toString());

       while (resultset.next()) {
        nodes.add(new Node(resultset.getLong(1), Type.get(resultset.getLong(2)),
                        resultset.getLong(3), resultset.getLong(4),
                        resultset.getLong(5), resultset.getString(6),
                        resultset.getLong(7), null, null));

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

    for (final Node node : nodes) {
      node.updateRevision(node.getComittedRevision());
    }
  }


  private void updateRevision(final Long _revision) throws EFapsException {
    this.children.addAll(getChildNodes(null, new Long (0), false));
    for (final Node child : this.children) {
      child.updateRevision(_revision);
    }
    final StringBuilder cmd = new StringBuilder();
    cmd.append("update ")
      .append(TABLE_NODE)
      .append(" set ").append(TABLE_NODE_C_REVISION).append("=?")
      .append(" where ").append(TABLE_NODE_C_ID).append("=?");

    final ConnectionResource con
                = Context.getThreadContext().getConnectionResource();
    try {

      PreparedStatement stmt = null;
      try {
        con.getConnection().setAutoCommit(true);
        stmt = con.getConnection().prepareStatement(cmd.toString());
        stmt.setLong(1, _revision);
        stmt.setLong(2, this.id);
        final int rows = stmt.executeUpdate();
        if (rows == 0) {
//           TODO fehler schmeissen
        }
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
  }

  @Override
  public String toString() {
    return "[id=" + this.id
          + "; historyId=" + this.historyId
          + "; copyId=" + this.copyId
          + "; revision=" + this.comittedRevision
          + "; name=" + this.name
          + "; repositoryId=" + this.repositoryId
          + "; fileId=" + this.fileId
          + "; propSetIs=" + this.propSetId
          + "; idPath=" + this.idPath
          + "; path=" + this.path
          + "; root=" + this.root
          + "]";
  }
}
