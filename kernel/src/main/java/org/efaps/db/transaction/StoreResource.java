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
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

import org.efaps.admin.datamodel.Type;
import org.efaps.db.Context;
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
 */
public abstract class StoreResource extends AbstractResource  {

  /**
   * Store resource could implement the possibility to compress and uncompress
   * data while writing to and reading from files.
   */
  protected enum Compress {NONE, ZIP, GZIP};

  /**
   * Property Name of the name of the key (id) in the sql table defined with
   * property {@link #PROPERY_TABLE}.
   */
  private final static String PROPERTY_COMPRESS = "StoreCompress";

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
  private long fileId;

  /**
   * Each store must be identfied with an url defining where the file and which
   * store resource is used.
   *
   * @see #getType
   */
  private Type type;

  /**
   * The variable stores if the store itself is compressed.
   */
  protected Compress compress = Compress.NONE;

  /**
   *
   * @param _context  eFaps context
   * @param _type     type with the information how to store the file
   * @param _fileId   id of the file to store
   */
  protected StoreResource(final Context _context, final Type _type,
                                                        final long _fileId)  {
    super(_context);
    this.fileId = _fileId;
    this.type = _type;
    String compressStr =  this.type.getProperty(PROPERTY_COMPRESS);
    if (compressStr != null)  {
      Compress compress = Compress.valueOf(compressStr.trim().toUpperCase());
      if (compress != null)  {
        this.compress = compress;
      }
    }
  }

  /**
   * Frees the resource and gives this VFS store resource back to the context
   * object.
   */
  protected void freeResource()  {
//    getContext().returnConnectionResource(this);
  }

  /**
   * Writes the file with the given input stream.
   *
   * @param _in     input stream
   * @param _size   size of the data to write (or negative if the size is not
   *                known)
   * @return length of the file which is stored
   * @throws EFapsException if an error occurs
   */
  public abstract int write(final InputStream _in, final int _size)
                                                        throws EFapsException;

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
      in = read();
      if (in!=null)  {
        int length = 1;
        while (length>0)  {
          length = in.read(this.buffer);
          if (length>0)  {
            _out.write(this.buffer, 0, length);
          }
        }
      }
    } catch (IOException e)  {
e.printStackTrace();
    } catch (EFapsException e)  {
      throw e;
    } finally  {
      if (in != null)  {
        try  {
          in.closeWithoutCommit();
        } catch (IOException e)  {
        }
      }
    }
  }

  /**
   * The input stream with the attached content of the object returned.
   *
   * @return input stream with the content of the file
   * @throws EFapsException if an error occurs
   */
  public abstract StoreResourceInputStream read() throws EFapsException;

  /**
   * Deletes the file defined in {@link #fileId}.
   *
   * @throws EFapsException if an error occurs
   */
  public abstract void delete() throws EFapsException;

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
  
  /////////////////////////////////////////////////////////////////////////////
  // input stream wrapper

  protected class StoreResourceInputStream extends InputStream  {
    
    private final InputStream in;
    
    private final StoreResource store;
    
    StoreResourceInputStream(final StoreResource _store,
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
     */
    protected void beforeClose() throws IOException  {
      this.in.close();
    }

    /**
     * Only a dummy method if something must happend after the commit of the
     * store.
     */
    protected void afterClose() throws IOException  {
    }

    /**
     * The input stream and others are closes without commit of the store
     * resource.
     */
    private void closeWithoutCommit() throws IOException  {
      beforeClose();
      afterClose();
    }
    
    ///////////////////////////////////////////////////////////////////////////
    // methods wrapping methods to the original input stream
    
    /**
     * Calls method {@link #beforeClose}, then commits the store and at least
     * calls method {@link #afterClose}.
     *
     * @see #beforeClose
     * @see #afterClose
     * @todo Java6 change IOException with throwable paramter
     */
    public void close() throws IOException  {
      try  {
        super.close();
        beforeClose();
        this.store.commit();
        afterClose();
      } catch (EFapsException e)  {
        throw new IOException("commit of store not possible" + e.toString());
      } finally  {
        if (this.store.isOpened())  {
          try  {
            this.store.abort();
          } catch (EFapsException e)  {
            throw new IOException("store resource could not be aborted" + e.toString());
          }
        }
      }
    }

    public int available() throws IOException  {
      return this.in.available();
    }

    public void  mark(final int _readlimit)  {
      this.in.mark(_readlimit);
    }

    public boolean 	markSupported()  {
      return this.in.markSupported();
    }

    public int read() throws IOException  {
      return this.in.read();
    }

    public int read(final byte[] _b) throws IOException  {
      return this.in.read(_b);
    }

    public int read(final byte[] _b, 
                    final int _off, 
                    final int _len) throws IOException  {
      return this.in.read(_b, _off, _len);
    }

    public void reset() throws IOException  {
      this.in.reset();
    }

    public long skip(final long _n) throws IOException  {
      return this.in.skip(_n);
    }
  }

}
