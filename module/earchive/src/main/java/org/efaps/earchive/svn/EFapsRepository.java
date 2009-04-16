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

package org.efaps.earchive.svn;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SimpleTimeZone;
import java.util.UUID;

import org.tmatesoft.svn.core.SVNException;

import org.efaps.admin.program.esjp.EFapsClassLoader;
import org.efaps.db.Context;
import org.efaps.esjp.earchive.INames;
import org.efaps.esjp.earchive.node.Node;
import org.efaps.esjp.earchive.repository.Repository;
import org.efaps.util.EFapsException;

import com.googlecode.jsvnserve.api.CommitInfo;
import com.googlecode.jsvnserve.api.Depth;
import com.googlecode.jsvnserve.api.DirEntry;
import com.googlecode.jsvnserve.api.DirEntryList;
import com.googlecode.jsvnserve.api.IRepository;
import com.googlecode.jsvnserve.api.LocationEntries;
import com.googlecode.jsvnserve.api.LockDescriptionList;
import com.googlecode.jsvnserve.api.LogEntryList;
import com.googlecode.jsvnserve.api.ReportList;
import com.googlecode.jsvnserve.api.ServerException;
import com.googlecode.jsvnserve.api.LockDescriptionList.LockDescription;
import com.googlecode.jsvnserve.api.ReportList.AbstractCommand;
import com.googlecode.jsvnserve.api.ReportList.SetPath;
import com.googlecode.jsvnserve.api.editorcommands.EditorCommandSet;
import com.googlecode.jsvnserve.api.properties.Properties;
import com.googlecode.jsvnserve.api.properties.Revision0PropertyValues;
import com.googlecode.jsvnserve.api.properties.RevisionPropertyValues;

/**
 * TODO comment!
 *
 * @author Jan Moxter
 * @version $Id$
 */
public class EFapsRepository implements IRepository {

  /**
   * Date time format from SVN used to format date instance to strings.<br/>
   * Example:<br/>
   * <code>2009-03-14T18:49:06.097886Z</code>
   *
   * @see #StringElement(Date)
   */
  private static final DateFormat DATETIMEFORMAT
                            = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
  static {
    DATETIMEFORMAT.setTimeZone(new SimpleTimeZone(0, "GMT"));
  }

  /**
   * Repository path.
   *
   * @see #getRepositoryPath()
   */
  final String repositoryPath;

  /**
   * Root path of the repository.
   *
   * @see #getLocationPath()
   */
  final String rootPath;

  private final String user;

  ClassLoader loader = new EFapsClassLoader(this.getClass().getClassLoader());

  private Repository repository;

  final Map<String, Node> path2Node = new HashMap<String, Node>();

  public EFapsRepository(final String _user, final String _path)
      throws SVNException {

    this.user = _user;
    this.repositoryPath = "/" + _path.split("/")[1];
    this.rootPath = _path.substring(this.repositoryPath.length());
    try {
      Context.begin(_user);
      Thread.currentThread().setContextClassLoader(this.loader);
      this.repository = Repository.getByName(_path.split("/")[1]);
    } catch (final EFapsException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public UUID getUUID() {
    return this.repository.getUuid();
  }

  /**
   * @see #repositoryPath
   */
  public CharSequence getRepositoryPath() {
    return this.repositoryPath;
  }

  /**
   * Returns the root path of the repository.
   *
   * @returns repository root path
   * @see #rootPath
   */
  public CharSequence getLocationPath() {
    return this.rootPath;
  }

  /**
   * @see com.googlecode.jsvnserve.api.IRepository#getStatus(java.lang.Long, java.lang.String, com.googlecode.jsvnserve.api.Depth, com.googlecode.jsvnserve.api.ReportList)
   * @param _revision             update revision, if not specified the value
   *                              is <code>null</code> and means the HEAD
   *                              revision
   * @param _path                 udate path
   * @param _depth                depth for update, determines the scope
   * @param _report               report of the current directory structure of
   *                              the client
   * @return set of commands to be executed in the client
   */
  public EditorCommandSet getStatus(final Long _revision, final String _path,
                                    final Depth _depth,
                                    final ReportList _report) {
    final EditorCommandSet deltaEditor = new EditorCommandSet(_revision);
    deltaEditor.updateRoot("getStatucUpdateRoot", _revision, new Date());
    final List<AbstractCommand> values = _report.values();
    final long clientRevision = ((SetPath) values.get(0)).getRevision();
    final boolean firstCheckOut = ((SetPath) values.get(0)).isStartEmpty();
    final Map<String, AbstractCommand> path2cmds = new HashMap<String, AbstractCommand>();
    for (final AbstractCommand cmd : values) {
      path2cmds.put(cmd.getPath(),cmd);
    }
    final String path = this.rootPath
        + (_path.length() > 1 ? "/" + _path.toString() : "");
    try {

      if (firstCheckOut) {
        final Node root = Node.getNodeFromDB(this.repository,
                                             deltaEditor.getTargetRevision(),
                                             path);
        createTree(deltaEditor, root);
        //a smaller version than existing is required by the client
      } else if (deltaEditor.getTargetRevision() < clientRevision) {
        final Iterator<AbstractCommand> iter = values.iterator();
        //the first must be ignored
        iter.next();
        while (iter.hasNext()) {
          final AbstractCommand cmd = iter.next();
          if (cmd instanceof SetPath) {
            //TODO file muss geloescht werden
          }
        }
      } else {
        final Node targetNode = Node.getNodeFromDB(this.repository,
                                                deltaEditor.getTargetRevision(),
                                                path);
        updateTree(deltaEditor, targetNode, clientRevision);
      }
    } catch (final EFapsException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }

    return deltaEditor;
  }

  private void updateTree(final EditorCommandSet _deltaEditor,
                          final Node _parent, final long _clientRevision)
      throws EFapsException {
    final List<Node> targetChildren = _parent.getChildren();
    for (final Node targetChild : targetChildren) {
      //if targetChild has a higher Revision than the client
      if (targetChild.getRevision() > _clientRevision) {
        final Node clientChild = targetChild.getNodeInRevision(_clientRevision);
        // if the child is not existing in the revision of the client it is
        // a new node, else it must be updated
        if (clientChild == null) {
          _deltaEditor.createDir(targetChild.getPath().substring(this.repositoryPath.length()), "jan",
                                targetChild.getRevision(),
                                new Date());
        } else {
          _deltaEditor.updateDir(targetChild.getPath().substring(this.repositoryPath.length()), "updateJan",
                                targetChild.getRevision(), new Date());
        }
      }
      updateTree(_deltaEditor, targetChild, _clientRevision);
    }
  }

  private void createTree(final EditorCommandSet _deltaEditor,
                          final Node _parent) throws EFapsException {
    for (final Node child : _parent.getChildren()) {
      _deltaEditor.createDir(child.getPath().substring(this.repositoryPath.length()),
                             "jmox", child.getRevision(), new Date());
      createTree(_deltaEditor, child);
    }
  }

  /**
   * @see com.googlecode.jsvnserve.api.IRepository#close()
   */
  public void close() {
    try {
      if (!Context.isTMNoTransaction()) {
        if (Context.isTMActive()) {
          Context.commit();
        } else {
          Context.rollback();
        }
      }
    } catch (final EFapsException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }


  /**
   * @see com.googlecode.jsvnserve.api.IRepository#getDir(java.lang.Long, java.lang.CharSequence, boolean, boolean, boolean, boolean, boolean)
   * @param _revision
   * @param _path
   * @param fileSize
   * @param hasProps
   * @param createdRev
   * @param modified
   * @param author
   * @return
   */
  public DirEntryList getDir(final Long _revision, final CharSequence _path,
                             final boolean _fileSize, final boolean _hasProps,
                             final boolean _createdRev, final boolean _modified,
                             final boolean _author) {
    final DirEntryList ret = new DirEntryList();
    final String path = this.rootPath + "/" + _path.toString();
    final Node node = this.path2Node.get(_revision + path);
    try {
      final List<Node> children = node.getChildren();
      for (final Node child: children) {
        if (child.getType().getName().equals(INames.TYPE_NODEFILE)) {
          ret.addFile(child.getName(), child.getRevision(), null, "halle", 1);
        } else {
          ret.addDirectory(child.getName(), child.getRevision(), null,"halle");
        }
      }
    } catch (final EFapsException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return ret;
  }

  /**
   * @see com.googlecode.jsvnserve.api.IRepository#getFile(java.lang.Long, java.lang.CharSequence)
   * @param _revision
   * @param _path
   * @return
   */
  public InputStream getFile(final Long _revision, final CharSequence _path) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see com.googlecode.jsvnserve.api.IRepository#getFileLock(java.lang.CharSequence)
   * @param path
   * @return
   */
  public LockDescription getFileLock(final CharSequence path) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see com.googlecode.jsvnserve.api.IRepository#getLatestRevision()
   * @return
   */
  public long getLatestRevision() {
    return this.repository.getLatestRevision();
  }

  /**
   * @see com.googlecode.jsvnserve.api.IRepository#getLocations(long, java.lang.String, long[])
   * @param _revision
   * @param _path
   * @param _revisions
   * @return
   */
  public LocationEntries getLocations(final long _revision, final String _path,
                                      final long... _revisions) {
    final LocationEntries entries = new LocationEntries();
    try {
      final String path = this.rootPath + "/" + _path.toString();
      final Node node = Node.getNodeFromDB(this.repository, _revision, path);
      //TODO was passiert wenn kein node gefunden wird?
      for (final long revision : _revisions) {
        final Node revnode = node.getRevisionNode(revision);
        if (revnode != null) {
          entries.add(revision,
             ("/" + revnode.getPath()).substring(this.repositoryPath.length()));
        }
      }
    } catch (final EFapsException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }



    return entries;
  }

  /**
   * @see com.googlecode.jsvnserve.api.IRepository#getLocks(java.lang.CharSequence)
   * @param _path
   * @return
   */
  public LockDescriptionList getLocks(final CharSequence _path) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see com.googlecode.jsvnserve.api.IRepository#getLog(long, long, boolean, java.lang.CharSequence[])
   * @param revision
   * @param revision2
   * @param changedPaths
   * @param _paths
   * @return
   */
  public LogEntryList getLog(final long revision, final long revision2,
      final boolean changedPaths, final CharSequence... _paths) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see com.googlecode.jsvnserve.api.IRepository#lock(java.lang.String, boolean, java.util.Map)
   * @param _comment
   * @param lock
   * @param withRevision
   * @return
   * @throws ServerException
   */
  public LockDescriptionList lock(final String _comment, final boolean lock,
      final Map<String, Long> withRevision) throws ServerException {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see com.googlecode.jsvnserve.api.IRepository#stat(java.lang.Long, java.lang.CharSequence, boolean)
   * @param _revision
   * @param _path
   * @param properties
   * @return
   */
  public DirEntry stat(final Long _revision, final CharSequence _path, final boolean properties) {
    DirEntry ret = null;
    try {
      final String path = this.rootPath + "/" + _path.toString();
      final Node node;
      if (this.path2Node.containsKey(_revision + path)) {
        node = this.path2Node.get(_revision + path);
      } else {
        node = Node.getNodeFromDB(this.repository, _revision, path);
        this.path2Node.put(_revision + path, node);
      }
      if (node.getType().getName().equals(INames.TYPE_NODEFILE)) {
        ret = DirEntry.createFile(node.getName(),  node.getRevision(),
                                 null, "jan", 0, "asd");
      } else {
        ret = DirEntry.createDirectory(node.getName(), node.getRevision(),
                                       null,"jan");
      }
    } catch (final EFapsException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return ret;
  }

  /**
   * @see com.googlecode.jsvnserve.api.IRepository#unlock(boolean, java.util.Map)
   * @param lock
   * @param withTokens
   * @return
   */
  public LockDescriptionList unlock(final boolean lock, final Map<String, String> withTokens) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see com.googlecode.jsvnserve.api.IRepository#commit(java.lang.String, java.util.Map, boolean, com.googlecode.jsvnserve.api.properties.Properties, com.googlecode.jsvnserve.api.editorcommands.EditorCommandSet)
   * @param message
   * @param _locks
   * @param locks
   * @param props
   * @param _editor
   * @return
   * @throws ServerException
   */
  public CommitInfo commit(final String message, final Map<String, String> _locks,
      final boolean locks, final Properties props, final EditorCommandSet _editor)
      throws ServerException {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see com.googlecode.jsvnserve.api.IRepository#getRevision0Properties()
   * @return
   * @throws ServerException
   */
  public Revision0PropertyValues getRevision0Properties()
      throws ServerException {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see com.googlecode.jsvnserve.api.IRepository#getRevisionProperties(long)
   * @param _revision
   * @return
   * @throws ServerException
   */
  public RevisionPropertyValues getRevisionProperties(final long _revision)
      throws ServerException {
    // TODO Auto-generated method stub
    return null;
  }
}