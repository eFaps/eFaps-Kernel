/*
 * Copyright 2005 The eFaps Team
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
 */

package org.efaps.db.transaction;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.apache.commons.vfs.provider.FileProvider;

import org.efaps.admin.datamodel.Type;
import org.efaps.db.Context;
import org.efaps.util.EFapsException;

/**
 * The class implements the {@link javax.transaction.XAResource} interface for
 * Apache Jakarta Commons Virtual File System.<br/>
 * All different virtual file systems could be used.
 * The algorithm is:
 * <ol>
 *   <li>check if the file already exists</li>
 *   <li></li>
 *   <li></li>
 * </ol>
 *
 * For each file id a new VFS store resource must be created.
 */
public class VFSStoreResource extends StoreResource  {

  /**
   *
   */
  private enum StoreEvent  {DELETE, WRITE, READ, UNKNOWN};

  /**
   *
   */
  private StoreEvent storeEvent = StoreEvent.UNKNOWN;

  /**
   * Logging instance used in this class.
   */
  private static Log log = LogFactory.getLog(VFSStoreResource.class);

  /**
   * Property NAme of the virtual file system provider class name.
   */
  public final static String PROPERTY_PROVIDER  = "VFSProvider";

  /**
   * Property Name of the virtual file system prefix.
   */
  public final static String PROPERY_PREFIX     = "VFSPrefix";

  /**
   *
   */
  public final static String EXTENSION_TEMP     = ".tmp";

  /**
   * Extension of a file in the store.
   */
  public final static String EXTENSION_NORMAL   = "";

  /**
   * Extension of a bakup file in the store.
   */
  public final static String EXTENSION_BAKUP    = ".bak";

  /**
   * Static variable storing the default virtual file system manager used to
   * access different file systems.
   */
  private static DefaultFileSystemManager fsManager;
  static  {
    fsManager = new DefaultFileSystemManager();
    try  {
      fsManager.init();
    } catch (FileSystemException e)  {
e.printStackTrace();
    }
  }

  /**
   * Buffer used to copy from the input stream to the output stream.
   *
   * @see #read
   * @see #write
   */
  private byte[] buffer = new byte[1024];

  /**
   * Stores the mapping between the key (provider and prefix) and the value
   * (schema name of the virtual file system) used to create the url prefix
   * (method {@link #getUrlPrefix}).
   */
  private static Map<String,String> urlMapping = new HashMap<String,String>();

  /**
   *
   */
  public VFSStoreResource(final Context _context, final Type _type, final long _fileId)  {
    super(_context, _type, _fileId);
  }

  /**
   * The method returns the url prefix which maps from the file provider class
   * including path to the internal VFS representation (the url includes the
   * schema and the VFS prefix (location of the starting path).<br/>
   * If no predefined mapping in {@link #urlMapping} is found, a new mapping is
   * made. First a new schema of the VFS file system manager must be defined.
   * As unique schema name the letter 'T' plus the current milliseconds
   * is used (the schema name must be only unique in this Java virtual machine,
   * so the complete method is synchronized). Under this schema, a new file
   * provider defined in type property {@link #PROPERTY_PROVIDER} is registered
   * in the default file system manager {@link #fsManager}.
   * Then the key in the mapping {@link #urlMapping} combines the VFS schema
   * name  and the prefix from the type definition (accessed via the property
   * key name {@link #PROPERY_PREFIX})
   *
   * @return url prefix including schema name for the current type (where the
   *         store is defined)
   * @throws EFapsException if
   *         <ul>
   *           <li>the provider class name is not found,</li>
   *           <li>the provider class name is not accessable,</li>
   *           <li>the provider class name is not instancable,</li>
   *           <li>the provider instance is not registerable with the new
   *               schema</li>
   *           <li>or another exception (catch for {@link java.lang.Throwable})
   *               occurs.
   *         </ul>
   */
  private synchronized String getUrlPrefix() throws EFapsException  {
    String provider = getType().getProperty(PROPERTY_PROVIDER);
    String prefix   = getType().getProperty(PROPERY_PREFIX);
    String key = provider + prefix;
    String value = urlMapping.get(key);
    if (value==null)  {
      try  {
        FileProvider fileProvider =
                      (FileProvider) Class.forName(provider).newInstance();
        String schema = "T" + System.currentTimeMillis();
        value = schema + "://" + prefix + "/";
        fsManager.addProvider(schema, fileProvider);
        urlMapping.put(key, value);
      } catch (ClassNotFoundException e)  {
        log.error("class " + provider + "not found", e);
        throw new EFapsException(VFSStoreResource.class,
                      "getUrlPrefix.ClassNotFoundException", e, provider);
      } catch (IllegalAccessException e)  {
        log.error("class " + provider + " not accessable", e);
        throw new EFapsException(VFSStoreResource.class,
                      "getUrlPrefix.IllegalAccessException", e, provider);
      } catch (InstantiationException e)  {
        log.error("class " + provider + " not instanciable", e);
        throw new EFapsException(VFSStoreResource.class,
                      "getUrlPrefix.InstantiationException", e, provider);
      } catch (FileSystemException e)  {
        log.error("file schema " + value + " for class " + provider
                                                    + " not registerable", e);
        throw new EFapsException(VFSStoreResource.class,
                      "getUrlPrefix.FileSystemException", e, provider, value);
      } catch (Throwable e)  {
        log.error("could not register new schema  for class " + provider
                                              + " with prefix " + prefix, e);
        throw new EFapsException(VFSStoreResource.class,
                      "getUrlPrefix.Throwable", e, provider, prefix);
      }
    }
    return value;
  }

  /**
   * The complete URL file is returned including the URL prefix (from
   * {@link #getUrlPrefix}) and the extension of the file.
   *
   * @return file url including the url prefix
   * #see getUrlPrefix
   */
  private String getFileUrl(final String _extension) throws EFapsException  {
    return getUrlPrefix() + getFileId() + _extension;
  }

  /**
   * The method writes the context (from the input stream) to a temporary file
   * (same file URL, but with extension {@link #EXTENSION_TEMP}).
   *
   * @param _in   input stream defined the content of the file
   * @param _size length of the content (or negative meaning that the length
   *              is not known; then the content gets the length of readable
   *              bytes from the input stream)
   * return size of the created temporary file object
   */
  public int write(final InputStream _in, final int _size)
                                                      throws EFapsException  {
    try  {
      this.storeEvent = StoreEvent.WRITE;
      int size = _size;

      FileObject tmpFile = fsManager.resolveFile(getFileUrl(EXTENSION_TEMP));
      if (!tmpFile.exists())  {
        tmpFile.createFile();
      }
      FileContent content = tmpFile.getContent();
      OutputStream out = content.getOutputStream(false);

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
        int length = _size;
        while (length > 0)  {
          int readLength = (length < this.buffer.length ? length : this.buffer.length);
          _in.read(this.buffer, 0, readLength);
          out.write(this.buffer, 0 , readLength);
          length -= readLength;
        }
      }
      tmpFile.close();
      return size;
    } catch (EFapsException e)  {
      throw e;
    } catch (IOException e)  {
      log.error("write of content failed", e);
      throw new EFapsException(VFSStoreResource.class, "write.IOException", e);
    }
  }

  /**
   *
   *
   */
  public void read(final OutputStream _out) throws EFapsException  {
    FileObject file = null;
    try  {
      this.storeEvent = StoreEvent.READ;

      file = fsManager.resolveFile(getFileUrl(EXTENSION_NORMAL));

      if (!file.isReadable())  {
throw new EFapsException(VFSStoreResource.class, "#####file no readable");
      }

      InputStream in = file.getContent().getInputStream();
      if (in!=null)  {
        int length = 1;
        while (length>0)  {
          length = in.read(this.buffer);
          if (length>0)  {
            _out.write(this.buffer, 0, length);
          }
        }
      }

    } catch (EFapsException e)  {
      throw e;
    } catch (Throwable e)  {
e.printStackTrace();
      throw new EFapsException(VFSStoreResource.class, "read.Throwable", e);
    } finally  {
      if (file != null)  {
        try {file.close();} catch (FileSystemException e) {}
      }
    }
  }

  /**
   * Deletes the file defined in {@link #fileId}.
   */
  public void delete() throws EFapsException  {
    this.storeEvent = StoreEvent.DELETE;
  }

  /////////////////////////////////////////////////////////////////////////////
  // all further methods are implementing javax.transaction.xa.XAResource

  /**
          Ask the resource manager to prepare for a transaction commit of the transaction specified in xid.
   * (used for 2-phase commits)
   */
  public int prepare(final Xid _xid)  {
System.out.println("VFSStore.öööööööööööööööööööööööööö.prepare="+_xid);
    return 0;
  }

  /**
   * The method is called from the transaction manager if the complete
   * transaction is completed.<br/>
   * A file in the virtual file system is commited with the algorithmus:
   * <ol>
   * <li>an existing bakup file is deleted</li>
   * <li>the original file is moved to the bakup file</li>
   * <li>the new file is moded to the original name</li>
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
  public void commit(final Xid _xid, final boolean _onePhase) throws XAException  {
    if (log.isDebugEnabled())  {
      log.debug("transaction commit");
    }
if (this.storeEvent == StoreEvent.WRITE)  {
    try  {
      FileObject tmpFile = fsManager.resolveFile(getFileUrl(EXTENSION_TEMP));
      FileObject newFile = fsManager.resolveFile(getFileUrl(EXTENSION_NORMAL));
      FileObject bakFile = fsManager.resolveFile(getFileUrl(EXTENSION_BAKUP));
      if (bakFile.exists())  {
        bakFile.delete();
      }
      if (newFile.exists())  {
        newFile.moveTo(bakFile);
      }
      tmpFile.moveTo(newFile);
      tmpFile.close();
      newFile.close();
      bakFile.close();
    } catch (Throwable e)  {
      log.error("transaction commit fails for " + _xid + " (one phase = "
                                                        + _onePhase + ")", e);
      XAException xa = new XAException(XAException.XA_RBCOMMFAIL);
      xa.initCause(e);
      throw xa;
    }
} else if (this.storeEvent == StoreEvent.DELETE)  {
    try  {
System.out.println("--1");
      FileObject curFile = fsManager.resolveFile(getFileUrl(EXTENSION_NORMAL));
System.out.println("--2"+curFile);
      FileObject bakFile = fsManager.resolveFile(getFileUrl(EXTENSION_BAKUP));
System.out.println("--3"+bakFile);
      if (bakFile.exists())  {
System.out.println("--4.bakFile.delete");
        bakFile.delete();
      }
      if (curFile.exists())  {
System.out.println("--5.curFile.move");
        curFile.moveTo(bakFile);
      }
System.out.println("--6.bakFile.close");
      bakFile.close();
System.out.println("--6.curFile.close");
      curFile.close();
    } catch (Throwable e)  {
      log.error("transaction commit fails for " + _xid + " (one phase = "
                                                        + _onePhase + ")", e);
      XAException xa = new XAException(XAException.XA_RBCOMMFAIL);
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
  public void rollback(final Xid _xid) throws XAException  {
    if (log.isDebugEnabled())  {
      log.debug("transaction rollback");
    }
    try  {
      FileObject tmpFile = fsManager.resolveFile(getFileUrl(EXTENSION_TEMP));
      if (tmpFile.exists())  {
        tmpFile.delete();
      }
    } catch (Throwable e)  {
      log.error("transaction rollback fails for " + _xid, e);
      XAException xa = new XAException(XAException.XA_RBCOMMFAIL);
      xa.initCause(e);
      throw xa;
    }
  }

  /**
          Tells the resource manager to forget about a heuristically completed transaction branch.
   */
  public void forget(final Xid _xid)   {
System.out.println("VFSStore.öööööööööööööööööööööööööö.forget="+_xid);
  }

  /**
          Obtains the current transaction timeout value set for this XAResource instance.
   */
  public int getTransactionTimeout()  {
System.out.println("VFSStore.öööööööööööööööööööööööööö.getTransactionTimeout");
    return 0;
  }

  /**
          Obtains a list of prepared transaction branches from a resource manager.
   */
  public Xid[] recover(final int _flag)  {
System.out.println("VFSStore.öööööööööööööööööööööööööö.recover");
    return null;
  }


  /**
          Sets the current transaction timeout value for this XAResource instance.
   */
  public boolean  setTransactionTimeout(final int _seconds)  {
System.out.println("VFSStore.öööööööööööööööööööööööööö.setTransactionTimout");
    return true;
  }
}
