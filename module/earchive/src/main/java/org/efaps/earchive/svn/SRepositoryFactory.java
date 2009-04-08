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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SimpleTimeZone;
import java.util.Stack;
import java.util.UUID;

import org.tmatesoft.svn.core.ISVNDirEntryHandler;
import org.tmatesoft.svn.core.ISVNLogEntryHandler;
import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNErrorMessage;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLock;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.SVNPropertyValue;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.BasicAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.io.ISVNEditor;
import org.tmatesoft.svn.core.io.ISVNLocationEntryHandler;
import org.tmatesoft.svn.core.io.ISVNLockHandler;
import org.tmatesoft.svn.core.io.ISVNReporter;
import org.tmatesoft.svn.core.io.ISVNReporterBaton;
import org.tmatesoft.svn.core.io.SVNLocationEntry;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.diff.SVNDiffWindow;
import org.tmatesoft.svn.core.wc.SVNClientManager;

import com.googlecode.jsvnserve.api.CommitInfo;
import com.googlecode.jsvnserve.api.Depth;
import com.googlecode.jsvnserve.api.DirEntry;
import com.googlecode.jsvnserve.api.DirEntryList;
import com.googlecode.jsvnserve.api.IRepository;
import com.googlecode.jsvnserve.api.IRepositoryFactory;
import com.googlecode.jsvnserve.api.LocationEntries;
import com.googlecode.jsvnserve.api.LockDescriptionList;
import com.googlecode.jsvnserve.api.LogEntryList;
import com.googlecode.jsvnserve.api.Properties;
import com.googlecode.jsvnserve.api.ReportList;
import com.googlecode.jsvnserve.api.ServerException;
import com.googlecode.jsvnserve.api.LockDescriptionList.LockDescription;
import com.googlecode.jsvnserve.api.LogEntryList.LogEntry;
import com.googlecode.jsvnserve.api.ReportList.AbstractCommand;
import com.googlecode.jsvnserve.api.ReportList.DeletePath;
import com.googlecode.jsvnserve.api.ReportList.SetPath;
import com.googlecode.jsvnserve.api.editorcommands.AbstractDelta;
import com.googlecode.jsvnserve.api.editorcommands.AbstractDeltaDirectory;
import com.googlecode.jsvnserve.api.editorcommands.DeltaDirectoryCopy;
import com.googlecode.jsvnserve.api.editorcommands.DeltaDirectoryCreate;
import com.googlecode.jsvnserve.api.editorcommands.DeltaDirectoryOpen;
import com.googlecode.jsvnserve.api.editorcommands.DeltaRootOpen;
import com.googlecode.jsvnserve.api.editorcommands.EditorCommandSet;

/**
 *
 * @author jSVNServe Team
 * @version $Id$
 */
public class SRepositoryFactory
        implements IRepositoryFactory
{
    /**
     * Date time format from SVN used to format date instance to strings.<br/>
     * Example:<br/>
     * <code>2009-03-14T18:49:06.097886Z</code>
     *
     * @see #StringElement(Date)
     */
    private static final DateFormat DATETIMEFORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
    static
    {
        DATETIMEFORMAT.setTimeZone(new SimpleTimeZone(0, "GMT"));
    }

    /**
     * SVN URL to the original property.
     */
    private final SVNURL svnURL;

    public SRepositoryFactory(final String _svnURI) throws SVNException
    {
        this.svnURL = SVNURL.parseURIDecoded(_svnURI);
        SVNRepositoryFactoryImpl.setup();
        FSRepositoryFactory.setup();
    }

    public IRepository createRepository(final String _user, final String _path)
    {
        try {
            return new Repository(_user, "/proxy", _path.substring("/proxy".length()));
        } catch (final SVNException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public class Repository
            implements IRepository
    {
        final SVNRepository svnRepository;
        final SVNClientManager clientManager;

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

        Repository(final String _user,
                   final String _repositoryPath,
                   final String _rootPath)
                throws SVNException
        {
            this.repositoryPath = _repositoryPath;
            this.rootPath = _rootPath;
            this.clientManager = SVNClientManager.newInstance(null, new BasicAuthenticationManager(_user , _user));
            this.svnRepository = this.clientManager.createRepository(SRepositoryFactory.this.svnURL.appendPath(_rootPath, false), false);
        }

        public UUID getUUID()
        {
            try {
                return UUID.fromString(this.svnRepository.getRepositoryUUID(true));
            } catch (final SVNException e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * @see #repositoryPath
         */
        public CharSequence getRepositoryPath()
        {
            return this.repositoryPath;
        }

        /**
         * Returns the root path of the repository.
         *
         * @returns repository root path
         * @see #rootPath
         */
        public CharSequence getLocationPath()
        {
            return this.rootPath;
        }

        public CommitInfo commit(final String _logMessage,
                                 final Map<String,String> _locks,
                                 final boolean _keepLocks,
                                 final Properties _revisionProps,
                                 final EditorCommandSet _editor)
                throws ServerException
        {
            CommitInfo commitInfo = null;

            try {
                final ISVNEditor editor = this.svnRepository.getCommitEditor(_logMessage,
                                                                             _locks,
                                                                             _keepLocks,
                                                                             SVNProperties.wrap(_revisionProps),
                                                                             null);
                final Stack<AbstractDelta> stack = new Stack<AbstractDelta>();
                for (final AbstractDelta delta : _editor.getDeltas())  {
                    if (!stack.isEmpty())  {
                        final File file = new File(delta.getPath());
                        final String parentDir = (file.getParent() == null) ? "" : file.getParent();

                        while (!stack.peek().getPath().equals(parentDir))  {
                            if (stack.pop() instanceof AbstractDeltaDirectory)  {
                                editor.closeDir();
                            } else  {
                                // file close
                            }
                        }
                    }

                    stack.add(delta);

                    if (delta instanceof DeltaRootOpen)  {
                        editor.openRoot(-1);
                    } else if (delta instanceof DeltaDirectoryCopy)  {
                        editor.addDir(delta.getPath(), delta.getCopiedPath(), delta.getCopiedRevision());
                    } else if (delta instanceof DeltaDirectoryCreate)  {
                        editor.addDir(delta.getPath(), null, -1);
                    } else if (delta instanceof DeltaDirectoryOpen)  {
                        editor.openDir(delta.getPath(), -1);
                    }
                }
                while (!stack.empty())  {
                    if (stack.pop() instanceof AbstractDeltaDirectory)  {
                        editor.closeDir();
                    } else  {
                        // file close
                    }
                }

                final SVNCommitInfo svnCommitInfo = editor.closeEdit();

                commitInfo = new CommitInfo(svnCommitInfo.getNewRevision(),
                                            svnCommitInfo.getAuthor(),
                                            svnCommitInfo.getDate());
            } catch (final SVNException e) {
                throw new ServerException(e.getMessage(), e);
            }

            return commitInfo;
        }

        public LogEntryList getLog(final long _startRevision,
                                   final long _endRevision,
                                   final boolean _includeChangedPaths,
                                   final CharSequence... _paths)
        {
            final LogEntryList ret = new LogEntryList();

            try {
                final List<SVNLogEntry> logEntries = new ArrayList<SVNLogEntry>();

                final ISVNLogEntryHandler logHandler = new ISVNLogEntryHandler()  {
                    public void handleLogEntry(final SVNLogEntry _logEntry)
                            throws SVNException
                    {
                        logEntries.add(_logEntry);
                    }
                };

                final String[] paths = new String[_paths.length];
                for (int idx = 0; idx < _paths.length; idx++)
                {
                    paths[idx] = _paths[idx].toString();
                }

                this.svnRepository.log(paths,
                                       _startRevision,
                                       _endRevision,
                                       _includeChangedPaths,
                                       false, /* if true then copy history (if any) is not to be traversed*/
                                       logHandler);

                for (final SVNLogEntry logEntry : logEntries)  {
                    final LogEntry geha = ret.addLogEntry(logEntry.getRevision(), logEntry.getAuthor(), logEntry.getDate(), logEntry.getMessage());
                    if (_includeChangedPaths)  {
                        for (final Object logEntryPathObj : logEntry.getChangedPaths().values())  {
                            final SVNLogEntryPath logEntryPath = (SVNLogEntryPath) logEntryPathObj;
                            if (SVNLogEntryPath.TYPE_ADDED == logEntryPath.getType())  {
                                if (logEntryPath.getCopyPath() != null)  {
                                    geha.addCopied(logEntryPath.getPath(), logEntryPath.getCopyPath(), logEntryPath.getCopyRevision());
                                } else  {
                                    geha.addLogCreated(logEntryPath.getPath());
                                }
                            } else if (SVNLogEntryPath.TYPE_DELETED == logEntryPath.getType())  {
                                geha.addDeleted(logEntryPath.getPath());
                            } else if (SVNLogEntryPath.TYPE_MODIFIED == logEntryPath.getType())  {
                                geha.addModified(logEntryPath.getPath());
                            } else if (SVNLogEntryPath.TYPE_REPLACED == logEntryPath.getType())  {
                                geha.addRead(logEntryPath.getPath());
                            }
                        }
                    }
                }
            } catch (final SVNException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return ret;
        }

        public DirEntryList getDir(final Long _revision,
                                 final CharSequence _path,
                                 final boolean _retFileSize,
                                 final boolean _retHasProps,
                                 final boolean _retCreatedRev,
                                 final boolean _retModified,
                                 final boolean _retAuthor)
        {
            final DirEntryList ret = new DirEntryList();

            final SVNProperties props = new SVNProperties();

            try {
                final Set<SVNDirEntry> dirEntries = new HashSet<SVNDirEntry>();
                final ISVNDirEntryHandler handler = new ISVNDirEntryHandler() {
                    public void handleDirEntry(final SVNDirEntry _dirEntry)
                    {
                        dirEntries.add(_dirEntry);
                    }
                };
                this.svnRepository.getDir(_path.toString(), _revision, props, 63, handler);
                for (final SVNDirEntry dirEntry : dirEntries)  {
                    if (dirEntry.getKind().equals(SVNNodeKind.DIR))  {
                        ret.addDirectory(dirEntry.getName(),
                                         dirEntry.getRevision(),
                                         dirEntry.getDate(),
                                         dirEntry.getAuthor());
                    } else if (dirEntry.getKind().equals(SVNNodeKind.FILE))  {
                        ret.addFile(dirEntry.getName(),
                                    dirEntry.getRevision(),
                                    dirEntry.getDate(),
                                    dirEntry.getAuthor(),
                                    (int) dirEntry.getSize());
                    } else  {
                        System.err.println("wrong directory entry " + dirEntry);
                    }
                }
            } catch (final SVNException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return ret;
        }

        public InputStream getFile(final Long _revision,
                                   final CharSequence _path)
        {
            try {
                final File temp = File.createTempFile("SVN", ".temp");
System.out.println("temp="+temp);
                temp.deleteOnExit();
                final OutputStream out = new FileOutputStream(temp);
                try  {
                    this.svnRepository.getFile(_path.toString(),
                                               _revision,
                                               null,
                                               out);
                } finally  {
                    out.flush();
                    out.close();
                }
                final InputStream in = new FileInputStream(temp);
                return in;
            } catch (final SVNException e)  {
                e.printStackTrace();
            } catch (final IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        public DirEntry stat(final Long _revision,
                             final CharSequence _path,
                             final boolean _includeProperties)
        {
//            this.svnRepository.getRepositoryPath(relativePath)
//            SVNReplicationEditor editor = new SVNReplicationEditor();
//            this.svnRepository.status(_revision, _path.toString(), false, reporter, editor)
            //our editor only stores properties of files and directories

            try {
                final SVNNodeKind nodeKind = this.svnRepository.checkPath(_path.toString(),
                                                                          (_revision == null) ? -1 : _revision);
                if (SVNNodeKind.DIR.equals(nodeKind))  {
                    final SVNProperties props = new SVNProperties();
                    this.svnRepository.getDir(_path.toString(),
                                              (_revision == null) ? -1 : _revision,
                                              props,
                                              0,
                                              (Collection<?>) null);
//System.out.println("props="+props.asMap());
                    return DirEntry.createDirectory(_path.toString(),
                                                    Long.parseLong(props.getStringValue("svn:entry:committed-rev")),
// irgendwie wird da falsch geparst
                                                    DATETIMEFORMAT.parse(props.getStringValue("svn:entry:committed-date")),
                                                    props.getStringValue("svn:entry:last-author"));
                } else  {
                    final SVNProperties props = new SVNProperties();
                    this.svnRepository.getFile(_path.toString(),
                                               (_revision == null) ? -1 : _revision,
                                               props,
                                               null);
//System.out.println("props="+props.asMap());
                    return DirEntry.createFile(_path.toString(),
                                               Long.parseLong(props.getStringValue("svn:entry:committed-rev")),
                                               DATETIMEFORMAT.parse(props.getStringValue("svn:entry:committed-date")),
                                               props.getStringValue("svn:entry:last-author"),
                                               0,
                                               props.getStringValue("svn:entry:checksum"));
                }
            } catch (final SVNException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (final NumberFormatException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (final ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }

        public long getLatestRevision()
        {
            try {
                return this.svnRepository.getLatestRevision();
            } catch (final SVNException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return 0;
        }

        public LockDescriptionList lock(final String _comment,
                                        final boolean _stealLock,
                                        final Map<String,Long> _pathsWithRevision)
                throws ServerException
        {
            final LockDescriptionList locks = new LockDescriptionList();

            try {
                final ISVNLockHandler lockHandler = new ISVNLockHandler()  {
                    public void handleLock(final String _path,
                                           final SVNLock _svnLock,
                                           final SVNErrorMessage _svnErrorMessage)
                            throws SVNException
                    {
                        if (_svnLock != null)  {
                            locks.addLockedSuccessfully(_path,
                                                  _svnLock.getID(),
                                                  _svnLock.getOwner(),
                                                  _svnLock.getComment(),
                                                  _svnLock.getCreationDate(),
                                                  _svnLock.getExpirationDate());
                        } else  {
                            locks.addFailed(_path, _svnErrorMessage.getFullMessage());
                        }
                    }

                    /** Not needed. */
                    public void handleUnlock(final String _path,
                                             final SVNLock _svnLock,
                                             final SVNErrorMessage _svnerrormessage)
                            throws SVNException
                    {
                    }
                };

                this.svnRepository.lock(_pathsWithRevision, _comment, _stealLock, lockHandler);
            } catch (final SVNException e) {
                e.printStackTrace();
            }
            return locks;
        }

        public LockDescriptionList unlock(final boolean _breakLock,
                           final Map<String,String> _pathsWithTokens)
        {
            final LockDescriptionList locks = new LockDescriptionList();

            try {
                final ISVNLockHandler lockHandler = new ISVNLockHandler()  {
                    /** Not needed. */
                    public void handleLock(final String _path,
                                           final SVNLock _svnLock,
                                           final SVNErrorMessage _svnErrorMessage)
                            throws SVNException
                    {
                    }
                    public void handleUnlock(final String _path,
                                             final SVNLock _svnLock,
                                             final SVNErrorMessage _svnErrorMessage)
                            throws SVNException
                    {
                        if (_svnErrorMessage != null)  {
                            locks.addFailed(_path, _svnErrorMessage.getFullMessage());
                        } else  {
                            locks.addUnlockedSuccessfully(_path);
                        }
                    }
                };

                this.svnRepository.unlock(_pathsWithTokens, _breakLock, lockHandler);
            } catch (final SVNException e) {
                e.printStackTrace();
            }
            return locks;

        }

        public LockDescription getFileLock(final CharSequence _filePath)
        {
            LockDescription lockDesc = null;

            try {
                final SVNLock svnLock = this.svnRepository.getLock(_filePath.toString());
                if (svnLock != null)  {
                    lockDesc = LockDescription.create(svnLock.getPath(),
                                                      svnLock.getID(),
                                                      svnLock.getOwner(),
                                                      svnLock.getComment(),
                                                      svnLock.getCreationDate(),
                                                      svnLock.getExpirationDate());
                }
            } catch (final SVNException e) {
                e.printStackTrace();
            }

            return lockDesc;
        }

        public LockDescriptionList getLocks(final CharSequence _path)
        {
            final LockDescriptionList lockList = new LockDescriptionList();
            try {
                final SVNLock[] svnLocks = this.svnRepository.getLocks(_path.toString());
                if (svnLocks != null)  {
                    for (final SVNLock svnLock : svnLocks)  {
                        lockList.addLockedSuccessfully(svnLock.getPath(),
                                                 svnLock.getID(),
                                                 svnLock.getOwner(),
                                                 svnLock.getComment(),
                                                 svnLock.getCreationDate(),
                                                 svnLock.getExpirationDate());
                    }
                }
            } catch (final SVNException e) {
                e.printStackTrace();
            }
            return lockList;
        }

        public LocationEntries getLocations(final long _pegRevision,
                                            final String _path,
                                            final long... _revisions)
        {
            final LocationEntries entries = new LocationEntries();
            try {
                this.svnRepository.getLocations(_path, _pegRevision, _revisions,
                        new ISVNLocationEntryHandler()  {

                            public void handleLocationEntry(final SVNLocationEntry _svnlocationentry)
                                    throws SVNException
                            {
                                entries.add(_svnlocationentry.getRevision(),
                                            _svnlocationentry.getPath());
                            }
                        });
            } catch (final SVNException e) {
                e.printStackTrace();
            }

            return entries;
        }

        public EditorCommandSet getStatus(final Long _revision,
                                  final String _path,
                                  final Depth _depth,
                                  final ReportList _report)
        {
            final SVNEditor editor = new SVNEditor();

            SVNDepth svnDepth = SVNDepth.UNKNOWN;
            switch (_depth)  {
                case EXCLUDE:       svnDepth = SVNDepth.EXCLUDE;break;
                case EMPTY:         svnDepth = SVNDepth.EMPTY;break;
                case FILES:         svnDepth = SVNDepth.FILES;break;
                case IMMEDIATES:    svnDepth = SVNDepth.IMMEDIATES;break;
                case INFINITY:      svnDepth = SVNDepth.INFINITY;break;
            }

try {
    // TODO: path could not start with / (why?)
    this.svnRepository.status((_revision != null) ? _revision : -1,
                              _path.substring(1),
                              svnDepth,
                              new ISVNReporterBaton()  {

        public void report(final ISVNReporter isvnreporter) throws SVNException
        {
            for (final AbstractCommand command : _report.values())  {
                if (command instanceof SetPath)  {
                    final SetPath setPath = (SetPath) command;
                    isvnreporter.setPath(setPath.getPath(),
                                         setPath.getLockToken(),
                                         setPath.getRevision(),
                                         SVNDepth.INFINITY,
                                         setPath.isStartEmpty());
                } else if (command instanceof DeletePath)  {
                    isvnreporter.deletePath(command.getPath());
                }
            }
            isvnreporter.finishReport();
            System.out.println("isvnreporter="+isvnreporter);
        }

    }, editor);
} catch (final SVNException e) {
    // TODO Auto-generated catch block
    e.printStackTrace();
}

System.out.println("editor="+editor.list);
final EditorCommandSet deltaEditor = new EditorCommandSet(editor.targetRevision);
try  {
            for (final AbstractDir dir : editor.list)  {
                dir.createDelta(deltaEditor);
            }
} catch (final Exception e)  {
    e.printStackTrace();
}
            return deltaEditor;
        }
    }

    abstract class AbstractDir
            extends HashMap<String,String>
    {
        final String path;

        final long revision;

        final String copiedPath;

        final long copiedRevision;

        AbstractDir(final String _path,
                    final long _revision,
                    final String _copiedPath,
                    final long _copiedRevision)
        {
            this.path = _path;
            this.revision = _revision;
            this.copiedPath = _copiedPath;
            this.copiedRevision = _copiedRevision;
        }

        public abstract void createDelta(final EditorCommandSet _deltaEditor)
                throws NumberFormatException, ParseException;
    }

    class OpenRoot
        extends AbstractDir
    {
        OpenRoot(final long _revision)
        {
            super(null, _revision, null, -1);
        }


        @Override
        public void createDelta(final EditorCommandSet _deltaEditor)
                throws NumberFormatException, ParseException
        {
            final AbstractDelta delta = _deltaEditor.updateRoot(
                    "tim",
                    containsKey("svn:entry:committed-rev")
                            ? Long.parseLong(get("svn:entry:committed-rev"))
                            : null,
                    containsKey("svn:entry:committed-date")
                            ? DATETIMEFORMAT.parse(get("svn:entry:committed-date"))
                            : null);
     for (final Map.Entry<String,String> entry : entrySet())  {
         if (!"svn:entry:committed-rev".equals(entry.getKey())
                 && !"svn:entry:committed-date".equals(entry.getKey())
                 && !"svn:entry:last-author".equals(entry.getKey())
                 && !"svn:entry:uuid".equals(entry.getKey()))  {
             delta.addProperty(entry.getKey(), entry.getValue());
         }
     }
        }
    }

    class UpdateDir
            extends AbstractDir
    {
        UpdateDir(final String _path,
                  final long _revision)
        {
            super(_path, _revision, null, -1);
        }

        @Override
        public void createDelta(final EditorCommandSet _deltaEditor)
                throws NumberFormatException, ParseException
        {
            final AbstractDelta delta = _deltaEditor.updateDir(this.path,
                                    "tim",
                                   Long.parseLong(get("svn:entry:committed-rev")),
                                   DATETIMEFORMAT.parse(get("svn:entry:committed-date")));
            for (final Map.Entry<String,String> entry : entrySet())  {
                if (!"svn:entry:committed-rev".equals(entry.getKey())
                        && !"svn:entry:committed-date".equals(entry.getKey())
                        && !"svn:entry:last-author".equals(entry.getKey())
                        && !"svn:entry:uuid".equals(entry.getKey()))  {
                    delta.addProperty(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    class CreateDir
            extends AbstractDir
    {
        CreateDir(final String _path,
                  final String _copiedPath,
                  final long _copiedRevision)
        {
            super(_path, -1, _copiedPath, _copiedRevision);
        }

        @Override
        public void createDelta(final EditorCommandSet _deltaEditor)
                throws NumberFormatException, ParseException
        {
            final AbstractDelta delta = _deltaEditor.createDir(this.path,
                                   "tim",
                                   Long.parseLong(get("svn:entry:committed-rev")),
                                   DATETIMEFORMAT.parse(get("svn:entry:committed-date")));
            for (final Map.Entry<String,String> entry : entrySet())  {
                if (!"svn:entry:committed-rev".equals(entry.getKey())
                        && !"svn:entry:committed-date".equals(entry.getKey())
                        && !"svn:entry:last-author".equals(entry.getKey())
                        && !"svn:entry:uuid".equals(entry.getKey()))  {
                    delta.addProperty(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    class CreateFile
            extends AbstractDir
    {
        CreateFile(final String _path,
                   final String _copiedPath,
                   final long _copiedRevision)
        {
            super(_path, -1, _copiedPath, _copiedRevision);
        }

        @Override
        public void createDelta(final EditorCommandSet _deltaEditor)
                throws NumberFormatException, ParseException
        {
            final AbstractDelta delta = _deltaEditor.createFile(this.path, "tim",
                                                        Long.parseLong(get("svn:entry:committed-rev")),
                                                        DATETIMEFORMAT.parse(get("svn:entry:committed-date")));
            for (final Map.Entry<String,String> entry : entrySet())  {
                if (!"svn:entry:committed-rev".equals(entry.getKey())
                        && !"svn:entry:committed-date".equals(entry.getKey())
                        && !"svn:entry:last-author".equals(entry.getKey())
                        && !"svn:entry:uuid".equals(entry.getKey()))  {
                    delta.addProperty(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    public class SVNEditor
            implements ISVNEditor
    {
        private long targetRevision;

        private final Stack<AbstractDir> stack = new Stack<AbstractDir>();

        private final List<AbstractDir> list = new ArrayList<AbstractDir>();

        public void abortEdit() throws SVNException
        {
            System.out.println("abortEdit()");
        }

        public void absentDir(final String s) throws SVNException
        {
            System.out.println("absentDir("+s+")");
        }

        public void addDir(final String _path,
                           final String _copiedPath,
                           final long _copiedRevision)
                throws SVNException
        {
            System.out.println("addDir("+_path+","+_copiedPath+","+_copiedRevision+")");
            final AbstractDir path = new CreateDir(_path, _copiedPath, _copiedRevision);
            this.stack.add(path);
            this.list.add(path);
        }

        public void changeDirProperty(final String _propKey,
                                      final SVNPropertyValue _propValue)
                throws SVNException
        {
            System.out.println("changeDirProperty("+_propKey+","+_propValue+")");
            this.stack.peek().put(_propKey, (_propValue != null) ? _propValue.toString() : null);
        }

        public void closeDir() throws SVNException
        {
            System.out.println("closeDir()");
            this.stack.pop();
        }

        public void openDir(final String _path, final long _revision)
        {
            System.out.println("openDir("+_path+","+_revision+")");
            final AbstractDir path = new UpdateDir(_path, _revision);
            this.stack.add(path);
            this.list.add(path);
        }

        public void absentFile(final String s)
        {
            System.out.println("absentFile("+s+")");
        }

        public void addFile(final String _path,
                            final String _copiedPath,
                            final long _copiedRevision)
        {
            System.out.println("addFile("+_path+","+_copiedPath+","+_copiedRevision+")");
            final AbstractDir path = new CreateFile(_path, _copiedPath, _copiedRevision);
            this.stack.add(path);
            this.list.add(path);
        }

        public void changeFileProperty(final String _path,
                                       final String _propKey,
                                       final SVNPropertyValue _propValue)
        {
            System.out.println("changeFileProperty("+_propKey+","+_propValue+")");
            this.stack.peek().put(_propKey, (_propValue != null) ? _propValue.toString() : null);
        }

        public void closeFile(final String s, final String s1)
        {
            System.out.println("closeFile("+s+","+s1+")");
            this.stack.pop();
        }

        public void openFile(final String s, final long l)
        {
            System.out.println("openFile("+s+","+l+")");

        }

        public SVNCommitInfo closeEdit()
        {
            System.out.println("closeEdit()");
            return null;
        }

        public void deleteEntry(final String s, final long l)
        {
            System.out.println("deleteEntry("+s+","+l+")");
        }

        public void openRoot(final long _revision) throws SVNException
        {
            System.out.println("openRoot("+_revision+")");
            final AbstractDir rootPath = new OpenRoot(_revision);
            this.stack.add(rootPath);
            this.list.add(rootPath);
        }

        public void targetRevision(final long _targetRevision)
        {
            this.targetRevision = _targetRevision;
        }

        public void applyTextDelta(final String s, final String s1) throws SVNException
        {
            System.out.println("applyTextDelta("+s+","+s1+")");
        }

        public OutputStream textDeltaChunk(final String s, final SVNDiffWindow svndiffwindow)
                throws SVNException
        {
            System.out.println("textDeltaChunk("+s+","+svndiffwindow.toString()+")");
            return null;
        }

        public void textDeltaEnd(final String s) throws SVNException
        {
            System.out.println("textDeltaEnd("+s+")");
        }
    }
}
