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
    //TODO UUID einbauen
    return UUID.fromString("4108effb-988f-42c0-a143-9b15ae57d4d9");
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




  public EditorCommandSet getStatus(final Long _revision, final String _path,
                                    final Depth _depth,
                                    final ReportList _report) {
    return null;
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
      final boolean fileSize, final boolean hasProps, final boolean createdRev, final boolean modified,
      final boolean author) {
    final DirEntryList ret = new DirEntryList();
    final String path = this.rootPath + "/" + _path.toString();
    final Node node = this.path2Node.get(_revision + path);
    try {
      final List<Node> children = node.getChildren();
      for (final Node child: children) {
        if (child.getType().getName().equals(INames.TYPE_FILE)) {

        } else {
          ret.addDirectory(child.getName(), child.getRevision(), new Date(),"halle");
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
   * @param revision
   * @param _path
   * @param _revisions
   * @return
   */
  public LocationEntries getLocations(final long revision, final String _path,
      final long... _revisions) {
    // TODO Auto-generated method stub
    return null;
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
      if (this.path2Node.containsKey(path)) {
        node = this.path2Node.get(path);
      } else {
        node = Node.getNodeFromDB(this.repository, _revision, path);
        this.path2Node.put(path, node);
      }
      this.path2Node.put(_revision + path, node);
      ret = DirEntry.createDirectory(node.getName(),
                                     node.getRevision(), new Date(),"jan");

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