/*
 * Copyright © 2003 - 2024 The eFaps Team (-)
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
 */
package org.efaps.db.store;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipOutputStream;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FilesCache;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.cache.LRUFilesCache;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.FileProvider;
import org.efaps.db.Instance;
import org.efaps.db.store.Resource.Compress;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>The class implements the {@link Resource} interface for Apache Jakarta
 * Commons Virtual File System.<p/>
 * <p>
 * All different virtual file systems could be used. The algorithm is:
 * <ol>
 *   <li>check if the file already exists</li>
 *   <li></li>
 *   <li></li>
 * </ol>
 * The store implements the compress property setting on the type for
 * <code>ZIP</code> and <code>GZIP</code>.</p>
 *
 * For each file id a new VFS store resource must be created.
 *
 * @author The eFaps Team
 */
public class VFSStoreResource
    extends AbstractStoreResource
{
    /**
     * Extension of the temporary file in the store used in the transaction
     * that the original file is not overwritten.
     */
    private static final String EXTENSION_TEMP = ".tmp";

    /**
     * Extension of a file in the store.
     */
    private static final String EXTENSION_NORMAL = "";

    /**
     * Extension of a bakup file in the store.
     */
    private static final String EXTENSION_BACKUP = ".bak";

    /**
     * Property Name of the number of sub directories.
     */
    private static final String PROPERTY_NUMBER_SUBDIRS = "VFSNumberSubDirectories";

    /**
     * Property Name to define if the type if is used to define a sub
     * directory.
     */
    private static final String PROPERTY_USE_TYPE = "VFSUseTypeIdInPath";

    /**
     * Property Name to define if the type if is used to define a sub
     * directory.
     */
    private static final String PROPERTY_NUMBER_BACKUP = "VFSNumberBackups";

    /**
     * Property Name to define the base name.
     */
    private static final String PROPERTY_BASENAME = "VFSBaseName";

    /**
     * Property Name for the class name of the Provider.
     */
    private static final String PROPERTY_PROVIDER = "VFSProvider";

    /**
     * Property Name for the class name of the Provider.
     */
    private static final String PROPERTY_FILESCACHE = "VFSFilesCache";

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG  = LoggerFactory.getLogger(VFSStoreResource.class);

    /**
     * Buffer used to copy from the input stream to the output stream.
     *
     * @see #write(InputStream, int)
     */
    private final byte[] buffer = new byte[1024];

    /**
     * Stores the name of the file including the correct directory.
     */
    private String storeFileName = null;

    /**
     * FilesystemManager for this VFSStoreResource.
     */
    private DefaultFileSystemManager manager;

    /**
     * NUmber of backup files to be kept.
     */
    private int numberBackup = 1;


    /**
     * Method called to initialize this StoreResource.
     * @param _instance     Instance of the object this StoreResource is wanted
     *                      for
     * @param _store        Store this resource belongs to
     * @throws EFapsException on error
     * @see Resource#initialize(Instance, Map, Compress)
     */
    @Override
    public void initialize(final Instance _instance,
                           final Store _store)
        throws EFapsException
    {
        super.initialize(_instance, _store);

        final StringBuilder fileNameTmp = new StringBuilder();

        final String useTypeIdStr = getStore().getResourceProperties().get(VFSStoreResource.PROPERTY_USE_TYPE);
        if ("true".equalsIgnoreCase(useTypeIdStr))  {
            fileNameTmp.append(getInstance().getType().getId()).append("/");
        }

        final String numberSubDirsStr =  getStore().getResourceProperties().get(
                        VFSStoreResource.PROPERTY_NUMBER_SUBDIRS);
        if (numberSubDirsStr != null)  {
            final long numberSubDirs = Long.parseLong(numberSubDirsStr);
            final String pathFormat = "%0"
                          + Math.round(Math.log10(numberSubDirs) + 0.5d)
                          + "d";
            fileNameTmp.append(String.format(pathFormat,
                            getInstance().getId() % numberSubDirs))
                   .append("/");
        }
        fileNameTmp.append(getInstance().getType().getId()).append(".").append(getInstance().getId());
        this.storeFileName = fileNameTmp.toString();

        final String numberBackupStr = getStore().getResourceProperties().get(VFSStoreResource.PROPERTY_NUMBER_BACKUP);
        if (numberBackupStr != null) {
            this.numberBackup  = Integer.parseInt(numberBackupStr);
        }

        if (this.manager == null) {
            try {
                DefaultFileSystemManager tmpMan = null;
                if (getStore().getResourceProperties().containsKey(Store.PROPERTY_JNDINAME)) {
                    final InitialContext initialContext = new InitialContext();
                    final Context context = (Context) initialContext.lookup("java:comp/env");
                    final NamingEnumeration<NameClassPair> nameEnum = context.list("");
                    while (nameEnum.hasMoreElements()) {
                        final NameClassPair namePair = nameEnum.next();
                        if (namePair.getName().equals(getStore().getResourceProperties().get(
                                        Store.PROPERTY_JNDINAME))) {
                            tmpMan = (DefaultFileSystemManager) context.lookup(
                                            getStore().getResourceProperties().get(Store.PROPERTY_JNDINAME));
                            break;
                        }
                    }
                }
                if (tmpMan == null && this.manager == null) {
                    this.manager = evaluateFileSystemManager();
                }
            } catch (final NamingException e) {
                throw new EFapsException(VFSStoreResource.class, "initialize.NamingException", e);
            }
        }
    }

    /**
     * @return DefaultFileSystemManager
     * @throws EFapsException on error
     */
    private DefaultFileSystemManager evaluateFileSystemManager()
        throws EFapsException
    {
        final DefaultFileSystemManager ret = new DefaultFileSystemManager();

        final String baseName =  getProperties().get(VFSStoreResource.PROPERTY_BASENAME);
        final String provider =  getProperties().get(VFSStoreResource.PROPERTY_PROVIDER);
        final String filesCacheName;
        if (getProperties().containsKey(VFSStoreResource.PROPERTY_FILESCACHE)) {
            filesCacheName = getProperties().get(VFSStoreResource.PROPERTY_FILESCACHE);
        } else {
            filesCacheName = LRUFilesCache.class.getName();
        }
        try {
            final FilesCache filesCache = (FilesCache) Class.forName(filesCacheName).newInstance();
            ret.setFilesCache(filesCache);
            final FileProvider fileProvider = (FileProvider) Class.forName(provider).newInstance();
            ret.addProvider(baseName, fileProvider);
            final FileObject baseFile = VFS.getManager().resolveFile(baseName);
            ret.setBaseFile(baseFile);
            ret.init();
        } catch (final FileSystemException e) {
            throw new EFapsException(VFSStoreResource.class,
                                     "evaluateFileSystemManager.FileSystemException",
                                     e, provider, baseName);
        } catch (final InstantiationException e) {
            throw new EFapsException(VFSStoreResource.class,
                                     "evaluateFileSystemManager.InstantiationException",
                                     e, baseName, provider);
        } catch (final IllegalAccessException e) {
            throw new EFapsException(VFSStoreResource.class,
                                     "evaluateFileSystemManager.IllegalAccessException",
                                     e, provider);
        } catch (final ClassNotFoundException e) {
            throw new EFapsException(VFSStoreResource.class,
                                     "evaluateFileSystemManager.ClassNotFoundException",
                                     e, provider);
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int add2Select(final SQLSelect _select)
    {
        return 0;
    }

    /**
     * The method writes the context (from the input stream) to a temporary file
     * (same file URL, but with extension {@link #EXTENSION_TEMP}).
     *
     * @param _in   input stream defined the content of the file
     * @param _size length of the content (or negative meaning that the length
     *              is not known; then the content gets the length of readable
     *              bytes from the input stream)
     * @param _fileName name of the file
     * @return size of the created temporary file object
     * @throws EFapsException on error
     */
    @Override
    public long write(final InputStream _in,
                      final long _size,
                      final String _fileName)
        throws EFapsException
    {
        try  {
            long size = _size;
            final FileObject tmpFile = this.manager.resolveFile(this.manager.getBaseFile(),
                                            this.storeFileName + VFSStoreResource.EXTENSION_TEMP);
            if (!tmpFile.exists()) {
                tmpFile.createFile();
            }
            final FileContent content = tmpFile.getContent();
            OutputStream out = content.getOutputStream(false);
            if (getCompress().equals(Compress.GZIP))  {
                out = new GZIPOutputStream(out);
            } else if (getCompress().equals(Compress.ZIP))  {
                out = new ZipOutputStream(out);
            }

            // if size is unkown!
            if (_size < 0)  {
                int length = 1;
                size = 0;
                while (length > 0)  {
                    length = _in.read(this.buffer);
                    if (length > 0)  {
                        out.write(this.buffer, 0, length);
                        size += length;
                    }
                }
            } else  {
                Long length = _size;
                while (length > 0) {
                    final int readLength = length.intValue() < this.buffer.length
                                    ? length.intValue()  : this.buffer.length;
                    _in.read(this.buffer, 0, readLength);
                    out.write(this.buffer, 0, readLength);
                    length -= readLength;
                }
            }
            if (getCompress().equals(Compress.GZIP) || getCompress().equals(Compress.ZIP))  {
                out.close();
            }
            tmpFile.close();
            setFileInfo(_fileName, size);
            return size;
        } catch (final IOException e)  {
            VFSStoreResource.LOG.error("write of content failed", e);
            throw new EFapsException(VFSStoreResource.class, "write.IOException", e);
        }

    }

    /**
     * Deletes the file defined in {@link #fileId}.
     */
    @Override
    public void delete()
    {
        //Deletion is done on commit
    }

    /**
     * Returns for the file the input stream.
     *
     * @return input stream of the file with the content
     * @throws EFapsException on error
     */
    @Override
    public InputStream read()
        throws EFapsException
    {
        StoreResourceInputStream in = null;
        try  {
            final FileObject file = this.manager.resolveFile(this.storeFileName + VFSStoreResource.EXTENSION_NORMAL);
            if (!file.isReadable())  {
                VFSStoreResource.LOG.error("file for " + this.storeFileName + " not readable");
                throw new EFapsException(VFSStoreResource.class, "#####file not readable");
            }
            in = new VFSStoreResourceInputStream(this, file);
        } catch (final FileSystemException e)  {
            VFSStoreResource.LOG.error("read of " + this.storeFileName + " failed", e);
            throw new EFapsException(VFSStoreResource.class, "read.Throwable", e);
        } catch (final IOException e) {
            VFSStoreResource.LOG.error("read of " + this.storeFileName + " failed", e);
            throw new EFapsException(VFSStoreResource.class, "read.Throwable", e);
        }
        return in;
    }

    /**
     * Ask the resource manager to prepare for a transaction commit of the
     * transaction specified in xid (used for 2-phase commits).
     *
     * @param _xid Xid
     * @return always 0
     */
    @Override
    public int prepare(final Xid _xid)
    {
        if (VFSStoreResource.LOG.isDebugEnabled())  {
            VFSStoreResource.LOG.debug("prepare (xid=" + _xid + ")");
        }
        return 0;
    }

    /**
     * Method that deletes the oldest backup and moves the others one up.
     *
     * @param _backup   file to backup
     * @param _number         number of backup
     * @throws FileSystemException on error
     */
    private void backup(final FileObject _backup,
                        final int _number)
        throws FileSystemException
    {
        if (_number < this.numberBackup) {
            final FileObject backFile = this.manager.resolveFile(this.manager.getBaseFile(),
                    this.storeFileName + VFSStoreResource.EXTENSION_BACKUP + _number);
            if (backFile.exists()) {
                backup(backFile, _number + 1);
            }
            _backup.moveTo(backFile);
        } else {
            _backup.delete();
        }
    }

    /**
     * The method is called from the transaction manager if the complete
     * transaction is completed.<br/>
     * A file in the virtual file system is committed with the algorithms:
     * <ol>
     * <li>any existing backup fill will be moved to an older backup file. The
     *     maximum number of backups can be defined by setting the property
     *     {@link #PROPERTY_NUMBER_BACKUP}. Default is one. To disable the
     *     property must be set to 0.</li>
     * <li>the current file is moved to the backup file (or deleted if property
     *     {@link #PROPERTY_NUMBER_BACKUP} is 0)</li>
     * <li>the new file is moved to the original name</li>
     * </ol>
     *
     * @param _xid      global transaction identifier (not used, because each
     *                  file with the file id gets a new VFS store resource
     *                  instance)
     * @param _onePhase <i>true</i> if it is a one phase commitment transaction
     *                  (not used)
     * @throws XAException if any exception occurs (catch on
     *         {@link java.lang.Throwable})
     */
    @Override
    public void commit(final Xid _xid,
                       final boolean _onePhase)
        throws XAException
    {
        if (VFSStoreResource.LOG.isDebugEnabled())  {
            VFSStoreResource.LOG.debug("transaction commit");
        }
        if (getStoreEvent() == VFSStoreResource.StoreEvent.WRITE) {
            try {
                final FileObject tmpFile = this.manager.resolveFile(this.manager.getBaseFile(),
                        this.storeFileName + VFSStoreResource.EXTENSION_TEMP);
                final FileObject currentFile = this.manager.resolveFile(this.manager.getBaseFile(),
                        this.storeFileName + VFSStoreResource.EXTENSION_NORMAL);
                final FileObject bakFile = this.manager.resolveFile(this.manager.getBaseFile(),
                        this.storeFileName + VFSStoreResource.EXTENSION_BACKUP);
                if (bakFile.exists() && this.numberBackup > 0) {
                    backup(bakFile, 0);
                }
                if (currentFile.exists()) {
                    if (this.numberBackup > 0) {
                        currentFile.moveTo(bakFile);
                    } else {
                        currentFile.delete();
                    }
                }
                tmpFile.moveTo(currentFile);
                tmpFile.close();
                currentFile.close();
                bakFile.close();
            } catch (final FileSystemException e)  {
                VFSStoreResource.LOG.error("transaction commit fails for " + _xid
                        + " (one phase = " + _onePhase + ")", e);
                final XAException xa = new XAException(XAException.XA_RBCOMMFAIL);
                xa.initCause(e);
                throw xa;
            }
        } else if (getStoreEvent() == VFSStoreResource.StoreEvent.DELETE) {
            try {
                final FileObject curFile = this.manager.resolveFile(this.manager.getBaseFile(),
                            this.storeFileName + VFSStoreResource.EXTENSION_NORMAL);
                final FileObject bakFile = this.manager.resolveFile(this.manager.getBaseFile(),
                        this.storeFileName + VFSStoreResource.EXTENSION_BACKUP);
                if (bakFile.exists()) {
                    bakFile.delete();
                }
                if (curFile.exists())  {
                    curFile.moveTo(bakFile);
                }
                bakFile.close();
                curFile.close();
            } catch (final FileSystemException e) {
                VFSStoreResource.LOG.error("transaction commit fails for " + _xid
                                + " (one phase = " + _onePhase + ")", e);
                final XAException xa = new XAException(XAException.XA_RBCOMMFAIL);
                xa.initCause(e);
                throw xa;
            }
        }
    }

    /**
     * If the file written in the virtual file system must be rolled back, only
     * the created temporary file (created from method {@link #write}) is
     * deleted.
     *
     * @param _xid      global transaction identifier (not used, because each
     *                  file with the file id gets a new VFS store resource
     *                  instance)
     * @throws XAException if any exception occurs (catch on
     *         {@link java.lang.Throwable})
     */
    @Override
    public void rollback(final Xid _xid)
        throws XAException
    {
        if (VFSStoreResource.LOG.isDebugEnabled())  {
            VFSStoreResource.LOG.debug("rollback (xid = " + _xid + ")");
        }
        try {
            final FileObject tmpFile = this.manager.resolveFile(this.manager.getBaseFile(),
                    this.storeFileName + VFSStoreResource.EXTENSION_TEMP);
            if (tmpFile.exists()) {
                tmpFile.delete();
            }
        } catch (final FileSystemException e)  {
            VFSStoreResource.LOG.error("transaction rollback fails for " + _xid, e);
            final XAException xa = new XAException(XAException.XA_RBCOMMFAIL);
            xa.initCause(e);
            throw xa;
        }
    }

    /**
     * Tells the resource manager to forget about a heuristically completed
     * transaction branch.
     *
     * @param _xid  global transaction identifier (not used, because each file
     *              with the file id gets a new VFS store resource instance)
     */
    @Override
    public void forget(final Xid _xid)
    {
        if (VFSStoreResource.LOG.isDebugEnabled()) {
            VFSStoreResource.LOG.debug("forget (xid = " + _xid + ")");
        }
    }

    /**
     * Obtains the current transaction timeout value set for this XAResource
     * instance.
     *
     * @return always 0
     */
    @Override
    public int getTransactionTimeout()
    {
        if (VFSStoreResource.LOG.isDebugEnabled()) {
            VFSStoreResource.LOG.debug("getTransactionTimeout");
        }
        return 0;
    }

    /**
     * Obtains a list of prepared transaction branches from a resource manager.
     *
     * @param _flag flag
     * @return always <code>null</code>
     */
    @Override
    public Xid[] recover(final int _flag)
    {
        if (VFSStoreResource.LOG.isDebugEnabled()) {
            VFSStoreResource.LOG.debug("recover (flag = " + _flag + ")");
        }
        return null;
    }

    /**
     * Sets the current transaction timeout value for this XAResource instance.
     *
     * @param _seconds number of seconds
     * @return always <i>true</i>
     */
    @Override
    public boolean setTransactionTimeout(final int _seconds)
    {
        if (VFSStoreResource.LOG.isDebugEnabled()) {
            VFSStoreResource.LOG.debug("setTransactionTimeout (seconds = " + _seconds + ")");
        }
        return true;
    }

    /**
     * Input stream wrapper class.
     */
    private class VFSStoreResourceInputStream
        extends StoreResourceInputStream
    {
        /**
         * File to be stored.
         */
        private final FileObject file;

        /**
         * @param _storeRes   storeresource
         * @param _file       file to store
         * @throws IOException on error
         */
        protected VFSStoreResourceInputStream(final AbstractStoreResource _storeRes,
                                              final FileObject _file)
            throws IOException
        {
            super(_storeRes, _file.getContent().getInputStream());
            this.file = _file;
        }

        /**
         * The file object {@link #file} is closed. The method overwrites the
         * method to close the input stream, because if the file is closed, the
         * input stream is also closed.
         *
         * @throws IOException on error
         */
        @Override
        protected void beforeClose() throws IOException
        {
            this.file.close();
        }
    }
}
