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
 * Revision:        $Rev: 2142 $
 * Last Changed:    $Date: 2009-01-28 16:51:50 -0500 (Wed, 28 Jan 2009) $
 * Last Changed By: $Author: jmox $
 */

package org.efaps.db.store;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.mortbay.log.Log;

import org.efaps.admin.datamodel.Type;
import org.efaps.db.Instance;
import org.efaps.db.transaction.AbstractResource;
import org.efaps.util.EFapsException;

/**
 * The algorithm is:
 * <ol>
 *   <li></li>
 *   <li></li>
 *   <li></li>
 * </ol>
 *
 * For each file id a new VFS store resource must be created.
 *
 * @author tmo
 * @version $Id: StoreResource.java 2142 2009-01-28 21:51:50Z jmox $
 */
public abstract class AbstractStoreResource extends AbstractResource
    implements Resource {

  /**
   * The variable stores if the files inside the store itself are compressed.
   */
  private Compress compress = Compress.NONE;

  /**
   * Buffer used to copy from the input stream to the output stream.
   *
   * @see #read
   */
  private final byte[] buffer = new byte[1024];

  /**
   * The variable stores the identifier of the file. This store is representing
   * this file.
   *
   * @see #getFileId
   */
  private  long fileId;

  /**
   * Each store must be identfied with an url defining where the file and which
   * store resource is used.
   *
   * @see #getType
   */
  private  Type type;

  /**
   * Properties of this StoreResource.
   */
  private Map<String, String> properties;

  /**
   * Method called to initialize this StoreResource.
   * @see org.efaps.db.store.Resource#initialize(org.efaps.db.Instance,
   *  java.util.Map, org.efaps.db.store.Resource.Compress)
   * @param _instance     Instance of the object this StoreResource is wanted
   *                      for
   * @param _properties   properties for this StoreResource
   * @param _compress     compress type for this StoreResource
   * @throws EFapsException on error
   */
  public void initialize(final Instance _instance,
                         final Map<String, String> _properties,
                         final Compress _compress) throws EFapsException {
    this.type = _instance.getType();
    this.properties = _properties;
    this.fileId = _instance.getId();
    this.compress = _compress;
  }

  /**
   * Frees the resource and gives this VFS store resource back to the context
   * object.
   */
  @Override
  protected void freeResource()  {
//    getContext().returnConnectionResource(this);
  }

  /**
   * The output stream is written with the content of the file. From method
   * {@link #read()} the input stream is used and copied into the output
   * stream.
   *
   * @param _out    output stream where the file content must be written
   * @throws EFapsException if an error occurs
   * @see #read()
   */
  public void read(final OutputStream _out) throws EFapsException  {
    StoreResourceInputStream in = null;
    try  {
      in = (StoreResourceInputStream) read();
      if (in != null)  {
        int length = 1;
        while (length > 0)  {
          length = in.read(this.buffer);
          if (length > 0)  {
            _out.write(this.buffer, 0, length);
          }
        }
      }
    } catch (final IOException e)  {
e.printStackTrace();
    } catch (final EFapsException e)  {
      throw e;
    } finally  {
      if (in != null)  {
        try  {
          in.closeWithoutCommit();
        } catch (final IOException e) {
          Log.warn("Catched IOException in class: " + this.getClass());
        }
      }
    }
  }

  /**
   * This is the getter method for instance variable {@link #person}.
   *
   * @return value of instance variable {@link #fileId}
   * @see #fileId
   */
  protected final long getFileId()  {
    return this.fileId;
  }

  /**
   * This is the getter method for instance variable {@link #type}.
   *
   * @return value of instance variable {@link #type}
   * @see #type
   */
  protected final Type getType()  {
    return this.type;
  }


  /**
   * Getter method for instance variable {@link #compress}.
   *
   * @return value of instance variable {@link #compress}
   */
  public Compress getCompress() {
    return this.compress;
  }
  /**
   * Setter method for instance variable {@link #compress}.
   *
   * @param _compress value for instance variable {@link #compress}
   */
  public void setCompress(final Compress _compress) {
    this.compress = _compress;
  }

  /**
   * set the properties for this class.
   * @param _properties properties to set
   */
  public void setProperties(final Map<String, String> _properties) {
    this.properties = _properties;
  }

  /**
   * Getter method for instance variable {@link #properties}.
   *
   * @return value of instance variable {@link #properties}
   */
  public Map<String, String> getProperties() {
    return this.properties;
  }

  /**
   * Method is used to set the FileSytemManager for a Virtual File System.
   *
   * @param _manager DefaultFileSystemManager to set
   */
  public void setFileSystemManager(final DefaultFileSystemManager _manager) {
    //not used here.
  }

  /**
   * @return DefaultFileSystemManager
   * @throws EFapsException on error
   */
  public DefaultFileSystemManager evaluateFileSystemManager()
      throws EFapsException {
    //not used here.
    return null;
  }

  /**
   * Wraps the standard InputStream to get a InputStream for the needs of eFaps.
   *
   */
  protected class StoreResourceInputStream extends InputStream  {

    /**
     * InputStream.
     */
    private final InputStream in;

    /**
     * StoreResource for this InputStream.
     */
    private final AbstractStoreResource store;

    /**
     * @param _store    StoreResource this InputStream belong to
     * @param _in       inputstream
     * @throws IOException on error
     */
    protected StoreResourceInputStream(final AbstractStoreResource _store,
                                       final InputStream _in)
    throws IOException  {
      this.store = _store;
      if (_store.compress.equals(Compress.GZIP))  {
        this.in = new GZIPInputStream(_in);
      } else if (_store.compress.equals(Compress.ZIP))  {
        this.in = new ZipInputStream(_in);
      } else  {
        this.in = _in;
      }
    }

    /**
     * The input stream itself is closed.
     * @throws IOException on error
     */
    protected void beforeClose() throws IOException  {
      this.in.close();
    }

    /**
     * Only a dummy method if something must happend after the commit of the
     * store.
     * @throws IOException on error
     */
    protected void afterClose() throws IOException  {
    }

    /**
     * The input stream and others are closes without commit of the store
     * resource.
     * @throws IOException on error
     */
    private void closeWithoutCommit() throws IOException  {
      beforeClose();
      afterClose();
    }
    /**
     * Calls method {@link #beforeClose}, then commits the store and at least
     * calls method {@link #afterClose}.
     *
     * @see #beforeClose
     * @see #afterClose
     * @throws IOException on error
     * @todo Java6 change IOException with throwable paramter
     */
    @Override
    public void close() throws IOException  {
      try  {
        super.close();
        beforeClose();
        this.store.commit();
        afterClose();
      } catch (final EFapsException e)  {
        throw new IOException("commit of store not possible" + e.toString());
      } finally  {
        if (this.store.isOpened())  {
          try  {
            this.store.abort();
          } catch (final EFapsException e)  {
            throw new IOException("store resource could not be aborted"
                                    + e.toString());
          }
        }
      }
    }

    /**
     * @see java.io.InputStream#available()
     * @return 0 if available, else 1
     * @throws IOException on error
     */
    @Override
    public int available() throws IOException  {
      return this.in.available();
    }

    /**
     * @see java.io.InputStream#mark(int)
     * @param _readlimit limit to read
     */
    @Override
    public void  mark(final int _readlimit)  {
      this.in.mark(_readlimit);
    }

    /**
     * @see java.io.InputStream#markSupported()
     * @return marksuported
     */
    @Override
    public boolean markSupported()  {
      return this.in.markSupported();
    }

    /**
     * @see java.io.InputStream#read()
     * @return readed
     * @throws IOException on error
     */
    @Override
    public int read() throws IOException  {
      return this.in.read();
    }

    /**
     * @see java.io.InputStream#read(byte[])
     * @param _b byte to read
     * @return d
     * @throws IOException on error
     */
    @Override
    public int read(final byte[] _b) throws IOException  {
      return this.in.read(_b);
    }

    /**
     * @see java.io.InputStream#read(byte[], int, int)
     * @param _b byte
     * @param _off offset
     * @param _len length
     * @return int
     * @throws IOException on error
     */
    @Override
    public int read(final byte[] _b,
                    final int _off,
                    final int _len) throws IOException  {
      return this.in.read(_b, _off, _len);
    }

    /**
     * @see java.io.InputStream#reset()
     * @throws IOException on error
     */
    @Override
    public void reset() throws IOException  {
      this.in.reset();
    }

    /**
     * @see java.io.InputStream#skip(long)
     * @param _n n to skip
     * @return long
     * @throws IOException on error
     */
    @Override
    public long skip(final long _n) throws IOException  {
      return this.in.skip(_n);
    }
  }
}
