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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SimpleTimeZone;
import java.util.UUID;

import org.tmatesoft.svn.core.SVNException;

import org.efaps.admin.program.esjp.EFapsClassLoader;
import org.efaps.db.Context;
import org.efaps.esjp.earchive.node.EFapsFile;
import org.efaps.esjp.earchive.node.Node;
import org.efaps.esjp.earchive.repository.Repository;
import org.efaps.esjp.earchive.revision.Revision;
import org.efaps.util.EFapsException;

import com.googlecode.jsvnserve.api.CommitInfo;
import com.googlecode.jsvnserve.api.Depth;
import com.googlecode.jsvnserve.api.DirEntry;
import com.googlecode.jsvnserve.api.DirEntryList;
import com.googlecode.jsvnserve.api.IRepository;
import com.googlecode.jsvnserve.api.LocationEntries;
import com.googlecode.jsvnserve.api.LockDescriptionList;
import com.googlecode.jsvnserve.api.LogEntryList;
import com.googlecode.jsvnserve.api.OtherServerException;
import com.googlecode.jsvnserve.api.ReportList;
import com.googlecode.jsvnserve.api.ServerException;
import com.googlecode.jsvnserve.api.LockDescriptionList.LockDescription;
import com.googlecode.jsvnserve.api.ReportList.AbstractCommand;
import com.googlecode.jsvnserve.api.ReportList.SetPath;
import com.googlecode.jsvnserve.api.editorcommands.AbstractDelta;
import com.googlecode.jsvnserve.api.editorcommands.DeltaDirectoryCreate;
import com.googlecode.jsvnserve.api.editorcommands.DeltaFileCreate;
import com.googlecode.jsvnserve.api.editorcommands.DeltaRootOpen;
import com.googlecode.jsvnserve.api.editorcommands.DirectoryNotExistsException;
import com.googlecode.jsvnserve.api.editorcommands.EditorCommandSet;
import com.googlecode.jsvnserve.api.editorcommands.FileNotExistsException;
import com.googlecode.jsvnserve.api.filerevisions.FileRevisionsList;
import com.googlecode.jsvnserve.api.properties.Properties;
import com.googlecode.jsvnserve.api.properties.Revision0PropertyValues;
import com.googlecode.jsvnserve.api.properties.RevisionPropertyValues;
import com.googlecode.jsvnserve.util.Timestamp;

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

  private final Map<String, Node> path2Node = new HashMap<String, Node>();

  private final Map<Long, Revision> revisionId2Revision = new HashMap<Long, Revision>();


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
    try {
      final Revision rev = getRevision(_revision);
      final AbstractDelta delta = deltaEditor.updateRoot(_revision);
      delta.setLastAuthor(rev.getCreatorName());
      delta.setCommittedDate(Timestamp.valueOf(rev.getCreated()));
      delta.setCommittedRevision(rev.getRevision());

      final List<AbstractCommand> values = _report.values();
      final long clientRevision = ((SetPath) values.get(0)).getRevision();
      final boolean firstCheckOut = ((SetPath) values.get(0)).isStartEmpty();
      final Map<String, AbstractCommand> path2cmds = new HashMap<String, AbstractCommand>();
      for (final AbstractCommand cmd : values) {
        path2cmds.put(cmd.getPath(),cmd);
      }
      final String path = this.rootPath
          + (_path.length() > 1 ? "/" + _path.toString() : "");

      // the client makes the first check out
      if (firstCheckOut) {
        final Node targetNode = Node.getNodeFromDB(this.repository,
                                                deltaEditor.getTargetRevision(),
                                                path);
        createTree(deltaEditor, targetNode);
      // a smaller version than existing is required by the client, that means
      // the client must be "reversed"
      } else if (deltaEditor.getTargetRevision() < clientRevision) {
        final Node clientNode = Node.getNodeFromDB(this.repository,
                                                   clientRevision,
                                                   path);
        reverseTree(deltaEditor, clientNode, clientRevision);
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

  /**
   * Method is called in the case that the client has got a bigger revision
   * as wanted by the target and the Tree must be reversed.
   *
   * @param _deltaEditor      editor for the deltas
   * @param _targetNode       node of the client
   * @param _clientRevision   revion of the client
   * @throws EFapsException
   */
  private void reverseTree(final EditorCommandSet _deltaEditor,
                           final Node _clientNode,final long _clientRevision)
      throws EFapsException {
    final List<Node> clientChildren = _clientNode.getChildren();
    for (final Node clientChild : clientChildren) {
      // only if the client revision is bigger than the target revision
      // something must be done
      if (clientChild.getComittedRevision() > _deltaEditor.getTargetRevision()) {
        final Node targetChild = clientChild.getNodeInRevision(_deltaEditor.getTargetRevision());
        // if the child is not exiting in the target it must be deleted in the
        // client
        if (targetChild == null) {
          final Revision rev = getRevision(clientChild.getComittedRevision());
          final AbstractDelta delta = _deltaEditor.delete(clientChild.getPath().substring(this.repositoryPath.length()),
                              rev.getRevision());
          delta.setLastAuthor(rev.getCreatorName());
          delta.setCommittedDate(Timestamp.valueOf(rev.getCreated()));
          delta.setCommittedRevision(rev.getRevision());

        // if the child has a different name than the target child it must be
        // renamed
        } else if (!targetChild.getName().equals(clientChild.getName())) {
          final Revision rev = getRevision(targetChild.getComittedRevision());
          _deltaEditor.delete(clientChild.getPath().substring(this.repositoryPath.length()), _clientRevision);
          if (clientChild.isFile()) {
            final AbstractDelta delta = _deltaEditor.createFile(targetChild.getPath().substring(this.repositoryPath.length()));
            delta.setLastAuthor(rev.getCreatorName());
            delta.setCommittedDate(Timestamp.valueOf(rev.getCreated()));
            delta.setCommittedRevision(rev.getRevision());
          } else {
            final AbstractDelta delta =_deltaEditor.createDir(targetChild.getPath().substring(this.repositoryPath.length()));
            delta.setLastAuthor(rev.getCreatorName());
            delta.setCommittedDate(Timestamp.valueOf(rev.getCreated()));
            delta.setCommittedRevision(rev.getRevision());
            createTree(_deltaEditor, targetChild);
          }
        } else {
          if (targetChild.isFile()) {
            //TODO update of file
          } else {
            final Revision rev = getRevision(targetChild.getComittedRevision());
            final AbstractDelta delta = _deltaEditor.updateDir(targetChild.getPath().substring(this.repositoryPath.length()),
                                   rev.getRevision());
            delta.setLastAuthor(rev.getCreatorName());
            delta.setCommittedDate(Timestamp.valueOf(rev.getCreated()));
            delta.setCommittedRevision(rev.getRevision());
            reverseTree(_deltaEditor, clientChild, _clientRevision);
          }
        }
      }
    }
  }

  /**
   * Method is called in the case that the client has got a smaller revision
   * as wanted by the target and the Tree must be updated.
   *
   * @param _deltaEditor      editor for the deltas
   * @param _targetNode       target node
   * @param _clientRevision   revion of the client
   * @throws EFapsException
   */
  private void updateTree(final EditorCommandSet _deltaEditor,
                          final Node _targetNode, final long _clientRevision)
      throws EFapsException {
    final List<Node> targetChildren = _targetNode.getChildren();
    for (final Node targetChild : targetChildren) {
      // if targetChild has a higher Revision than the client
      if (targetChild.getComittedRevision() > _clientRevision) {
        final Node clientChild = targetChild.getNodeInRevision(_clientRevision);
        final Revision rev = getRevision(targetChild.getComittedRevision());
        // if the child is not existing for the revision of the client it is
        // a new node for the client, else it must be updated in the client
        if (clientChild == null) {
          if (targetChild.isFile()) {
            final AbstractDelta delta = _deltaEditor.createFile(targetChild.getPath().substring(this.repositoryPath.length()));
            delta.setLastAuthor(rev.getCreatorName());
            delta.setCommittedDate(Timestamp.valueOf(rev.getCreated()));
            delta.setCommittedRevision(rev.getRevision());
          } else {
            final AbstractDelta delta = _deltaEditor.createDir(targetChild.getPath().substring(this.repositoryPath.length()));
            delta.setLastAuthor(rev.getCreatorName());
            delta.setCommittedDate(Timestamp.valueOf(rev.getCreated()));
            delta.setCommittedRevision(rev.getRevision());
            updateTree(_deltaEditor, targetChild, _clientRevision);
          }
        // if the child is existing for the revision of the client, but it has
        // a different name, it means that the node was renamed
        } else if (!targetChild.getName().equals(clientChild.getName())) {
          _deltaEditor.delete(clientChild.getPath().substring(this.repositoryPath.length()), rev.getRevision());
          if (targetChild.isFile()) {
            final AbstractDelta delta = _deltaEditor.createFile(targetChild.getPath().substring(this.repositoryPath.length()));
            delta.setLastAuthor(rev.getCreatorName());
            delta.setCommittedDate(Timestamp.valueOf(rev.getCreated()));
            delta.setCommittedRevision(rev.getRevision());
          } else {
            final AbstractDelta delta =_deltaEditor.createDir(targetChild.getPath().substring(this.repositoryPath.length()));
            delta.setLastAuthor(rev.getCreatorName());
            delta.setCommittedDate(Timestamp.valueOf(rev.getCreated()));
            delta.setCommittedRevision(rev.getRevision());
            createTree(_deltaEditor, targetChild);
          }
        } else {
          if (targetChild.isFile()) {
            //TODO update of a file
          } else {
            final AbstractDelta delta = _deltaEditor.updateDir(targetChild.getPath().substring(this.repositoryPath.length()),
                rev.getRevision());
            delta.setLastAuthor(rev.getCreatorName());
            delta.setCommittedDate(Timestamp.valueOf(rev.getCreated()));
            delta.setCommittedRevision(rev.getRevision());
            updateTree(_deltaEditor, targetChild, _clientRevision);
          }
        }
      }
    }
  }

  /**
   * Method is called for the fist checkout of the repository by a client.
   * Because it is a first checkout now checking at all must be done.
   *
   * @param _deltaEditor      editor for the deltas
   * @param _targetNode       target node
   * @throws EFapsException
   */
  private void createTree(final EditorCommandSet _deltaEditor,
                          final Node _targetNode) throws EFapsException {
    for (final Node child : _targetNode.getChildren()) {
      final Revision rev = getRevision(child.getComittedRevision());
      if (child.isFile()) {
        final AbstractDelta delta = _deltaEditor.createFile(child.getPath().substring(this.repositoryPath.length()));
        delta.setLastAuthor(rev.getCreatorName());
        delta.setCommittedDate(Timestamp.valueOf(rev.getCreated()));
        delta.setCommittedRevision(rev.getRevision());
      } else {
        final AbstractDelta delta = _deltaEditor.createDir(child.getPath().substring(this.repositoryPath.length()));
        delta.setLastAuthor(rev.getCreatorName());
        delta.setCommittedDate(Timestamp.valueOf(rev.getCreated()));
        delta.setCommittedRevision(rev.getRevision());
        // for a directory the recursive must be started
        createTree(_deltaEditor, child);
      }
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
   * @param _fileSize
   * @param _hasProps
   * @param _createdRev
   * @param _modified
   * @param _author
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
        final Revision rev = getRevision(child.getComittedRevision());
        if (child.isFile()) {
          ret.addFile(child.getName(), child.getComittedRevision(),
                      Timestamp.valueOf(rev.getCreated()),
                      rev.getCreatorName(), 1);
        } else {
          ret.addDirectory(child.getName(), child.getComittedRevision(),
                           Timestamp.valueOf(rev.getCreated()),
                           rev.getCreatorName());
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
    InputStream ret = null;
    try {
      final Node node = Node.getNodeFromDB(this.repository, _revision, _path);
      ret = node.getFile();
    } catch (final EFapsException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (final Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return ret;
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
        final Node revnode = node.getNodeInRevision(revision);
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
   * @param _fromRevision
   * @param _toRevision
   * @param _changedPaths
   * @param _paths
   * @return
   */
  public LogEntryList getLog(final long _startRevision, final long _endRevision,
                             final boolean _changedPaths,
                             final CharSequence... _paths) {
    //TODO changedPaths wir nicht beruecksichtigt
    final LogEntryList entryList = new LogEntryList();
    try {
      for (final CharSequence pathSeq : _paths) {
        final String path = this.rootPath
                        + (pathSeq.length() > 1 ? "/" + pathSeq.toString() : "");
        Node current = Node.getNodeFromDB(this.repository, _startRevision, path);
        Revision rev = Revision.getRevisionFromDB(this.repository, current.getComittedRevision());
        entryList.addLogEntry(current.getComittedRevision(),
                              rev.getCreatorName(), rev.getCreated(),
                              rev.getMessage());

        Long curRev = rev.getRevision() - 1;
        while (curRev > 0 && curRev > _endRevision) {
          current = current.getNodeInRevision(curRev);
          if (current != null) {
            rev = Revision.getRevisionFromDB(this.repository, current.getComittedRevision());
            entryList.addLogEntry(current.getComittedRevision(),
                                  rev.getCreatorName(), rev.getCreated(),
                                  rev.getMessage());
            curRev = rev.getRevision() - 1;
          } else {
            curRev = new Long(0);
          }
        }
      }
    } catch (final EFapsException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return entryList;
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
  public DirEntry stat(final Long _revision, final CharSequence _path,
                       final boolean properties) {
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
      final Revision rev = getRevision(node.getComittedRevision());
      if (node.isFile()) {
        ret = DirEntry.createFile(node.getName(), rev.getRevision(),
                                  Timestamp.valueOf(rev.getCreated()),
                                  rev.getCreatorName(), 0, "asd");
      } else {
        ret = DirEntry.createDirectory(node.getName(), rev.getRevision(),
                                       Timestamp.valueOf(rev.getCreated()),
                                       rev.getCreatorName());
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
   * @param _message
   * @param _locks
   * @param locks
   * @param props
   * @param _editor
   * @return
   */
  public CommitInfo commit(final String _message,
                           final Map<String, String> _locks,
                           final boolean locks, final Properties props,
                           final EditorCommandSet _editor)
      throws DirectoryNotExistsException, FileNotExistsException,
      OtherServerException {
    CommitInfo commitInfo = null;
    final Collection<AbstractDelta> deltas = _editor.getDeltas();
    try {
      final Map <DeltaFileCreate,EFapsFile> delta2File = new HashMap<DeltaFileCreate,EFapsFile>();
      //first check all the files in
      for (final AbstractDelta delta : deltas) {
        if (delta instanceof DeltaFileCreate) {
          final DeltaFileCreate fileDelta = (DeltaFileCreate) delta;
          final int pos = delta.getPath().lastIndexOf(Node.SEPERATOR_PATH);
          final String name = delta.getPath().substring(pos + 1);
          final EFapsFile file = EFapsFile.createFile(fileDelta.getInputStream(), name, fileDelta.getFileSize());
          delta2File.put(fileDelta, file);
        }
      }

      //build the structur
      final List<Node> newnodes = new ArrayList<Node>();
      for (final AbstractDelta delta : deltas) {
        if (!(delta instanceof DeltaRootOpen)) {
          final int pos = delta.getPath().lastIndexOf(Node.SEPERATOR_PATH);
          final String parentPath;
          final String name;
          if (pos < 0) {
            parentPath = this.rootPath;
            name = delta.getPath();
          } else {
            parentPath = delta.getPath().substring(0, pos);
            name = delta.getPath().substring(pos + 1);
          }
          final Node parentNode = Node.getNodeFromDB(this.repository, getLatestRevision(), parentPath);

          if (delta instanceof DeltaDirectoryCreate) {
            final Node newDir = Node.createNewNode(this.repository, name,
                                                   Node.TYPE_NODEDIRECTORY, null, null);
            newDir.setConnectTarget(parentNode);
            newnodes.add(newDir);
          } else if (delta instanceof DeltaFileCreate) {
            final EFapsFile file = delta2File.get(delta);
            final Node newFile = Node.createNewNode(this.repository, name,
                Node.TYPE_NODEFILE, null, file.getId());
            newFile.setConnectTarget(parentNode);
            newnodes.add(newFile);
          }
        }
      }
      final Revision rev = Node.multiBubbleUp(newnodes, _message);
      commitInfo = new CommitInfo(rev.getRevision(), this.user, new Date());
    } catch (final EFapsException e) {

    }

    return commitInfo;
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


  private Revision getRevision(final Long _revision) throws EFapsException {
    final Revision ret;
    if (this.revisionId2Revision.containsKey(_revision)) {
      ret = this.revisionId2Revision.get(_revision);
    } else {
      ret = Revision.getRevisionFromDB(this.repository, _revision);
      this.revisionId2Revision.put(ret.getRevision(), ret);
    }
    return ret;
  }

  /**
   * @see com.googlecode.jsvnserve.api.IRepository#getFileRevs(java.lang.String, long, long, boolean)
   * @param _path
   * @param rev
   * @param rev2
   * @param info
   * @return
   * @throws ServerException
   */
  public FileRevisionsList getFileRevs(final String _path, final long rev, final long rev2,
      final boolean info) throws ServerException {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see com.googlecode.jsvnserve.api.IRepository#setLocationPath(java.lang.CharSequence)
   * @param path
   */
  public void setLocationPath(final CharSequence path) {
    // TODO Auto-generated method stub

  }
}