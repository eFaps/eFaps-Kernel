/*
 * Copyright 2006 The eFaps Team
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

package org.efaps.db;

import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.efaps.admin.datamodel.Type;
import org.efaps.db.transaction.StoreResource;
import org.efaps.util.EFapsException;

/**
 * The class is used to checkout a file from a given attribute of an object.
 */
public class Checkout extends AbstractAction  {

  /**
   * Constructor with object id as string.
   *
   * @param _context  eFaps context for this request
   * @param _oid      oid of object on which the checkout is made
   * @param _attrName name of the attribute where the blob is in
   * @todo rewrite to thrown EFapsException
   */
  public Checkout(final Context _context, final String _oid) throws Exception  {
    this(_context, new Instance(_context, _oid));
  }

  /**
   * Constructor with instance object.
   *
   * @param _context  eFaps context for this request
   * @param _instance instance on which the checkout is made
   * @param _attrName name of the attribute where the blob is in
   */
  public Checkout(final Context _context, final Instance _instance) throws EFapsException  {
    this.instance = _instance;
  }

  /**
   * The method is only a dummy method and closes the checkout action. The
   * method should be called, if in the future the checkout class needs a
   * call to this method.
   */
  public void close()  {
  }

  /**
   *
   * @param _context  eFaps context for this request
   */
  public void preprocess(final Context _context) throws Exception  {
    Type type = getInstance().getType();
    String fileName = type.getProperty(PROPERTY_STORE_ATTR_FILE_NAME);

    SearchQuery query = new SearchQuery();
    query.setObject(_context, getInstance());
    query.addSelect(_context, fileName);
//    try  {
      query.execute(_context);
      if (query.next())  {
        Object value = query.get(_context, fileName);
        setFileName(value.toString());
      }
//    } finally  {
      query.close();
//    }
  }

  /**
   * Executes the checkout.
   *
   * @param _context  eFaps context for this request
   * @param _out      output stream where to write the file
   * @throws EFapsException if checkout action fails
   */
  public void process(final Context _context, final OutputStream _out) throws EFapsException  {
    StoreResource storeRsrc = null;
    try  {
      storeRsrc = _context.getStoreResource(getInstance().getType(), getInstance().getId());
      storeRsrc.read(_out);
      storeRsrc.commit();
    } catch (EFapsException e)  {
      if (storeRsrc != null)  {
        storeRsrc.abort();
      }
      throw e;
    } catch (Throwable e)  {
      if (storeRsrc != null)  {
        storeRsrc.abort();
      }
      throw new EFapsException(Checkout.class, "execute.Throwable", e);
    }


//FileSystem fileSystem = new org.efaps.db.vfs.provider.sqldatabase.SQLDataBaseFileSystem();

/*    FileObject fileObject = null;
    try  {

        Type type = getInstance().getType();

org.apache.commons.vfs.impl.DefaultFileSystemManager defaultFSManager = new org.apache.commons.vfs.impl.DefaultFileSystemManager();

String providerClassName = null;
Type parent = type;
while (providerClassName==null && parent!=null)  {
  providerClassName=parent.getProperty("VFSProvider");
  parent=parent.getParentType();
}

String prefix = null;
parent = type;
while (prefix==null && parent!=null)  {
  prefix=parent.getProperty("VFSPrefix");
  parent=parent.getParentType();
}

org.apache.commons.vfs.provider.FileProvider fileProvider = (org.apache.commons.vfs.provider.FileProvider)Class.forName(providerClassName).newInstance();

defaultFSManager.addProvider("test", fileProvider);
//defaultFSManager.setDefaultProvider(fileProvider);
defaultFSManager.init();

fileObject = defaultFSManager.resolveFile("test://"+prefix+"/"+getInstance().getId());

  if (!fileObject.isReadable())  {
    throw new EFapsException(Checkout.class, "#####file no readable");
  }


  InputStream in = fileObject.getContent().getInputStream();
  if (in!=null)  {
    int length = 1;
    while (length>0)  {
      length = in.read(this.buffer);
      if (length>0)  {
        _out.write(this.buffer, 0, length);
      }
    }
  }

    } catch (Throwable e)  {
e.printStackTrace();
      throw new EFapsException(Checkout.class, "execute.Throwable", e);
    } finally  {
      try {fileObject.close();} catch (FileSystemException e) {}
    }
*/

  }


  /**
   * Executes the checkout.
   *
   * @param _context  eFaps context for this request
   * @throws EFapsException if checkout action fails
   */
/*
  public InputStream getInputStream(final Context _context) throws EFapsException  {
//FileSystem fileSystem = new org.efaps.db.vfs.provider.sqldatabase.SQLDataBaseFileSystem();

InputStream ret = null;
    FileObject fileObject = null;
    try  {
        Type type = getInstance().getType();

org.apache.commons.vfs.impl.DefaultFileSystemManager defaultFSManager = new org.apache.commons.vfs.impl.DefaultFileSystemManager();

String providerClassName = null;
Type parent = type;
while (providerClassName==null && parent!=null)  {
  providerClassName=parent.getProperty("VFSProvider");
  parent=parent.getParentType();
}

String prefix = null;
parent = type;
while (prefix==null && parent!=null)  {
  prefix=parent.getProperty("VFSPrefix");
  parent=parent.getParentType();
}

org.apache.commons.vfs.provider.FileProvider fileProvider = (org.apache.commons.vfs.provider.FileProvider)Class.forName(providerClassName).newInstance();

defaultFSManager.addProvider("test", fileProvider);
//defaultFSManager.setDefaultProvider(fileProvider);
defaultFSManager.init();

fileObject = defaultFSManager.resolveFile("test://"+prefix+"/"+getInstance().getId());

  if (!fileObject.isReadable())  {
    throw new EFapsException(Checkout.class, "#####file no readable");
  }


  InputStream in = fileObject.getContent().getInputStream();

ret = new VFSInputStream(in, fileObject);

    } catch (Throwable e)  {
e.printStackTrace();
      throw new EFapsException(Checkout.class, "execute.Throwable", e);
    }
return ret;
  }


private class VFSInputStream extends FilterInputStream  {

FileObject object = null;

  private VFSInputStream(InputStream _in, FileObject _object)  {
    super(_in);
this.object = _object;
  }

  public void close() throws IOException  {
    super.close();
this.object.close();
  }

}
*/

  /////////////////////////////////////////////////////////////////////////////

  /**
   * Instance holding the oid of the object which is checked out.
   *
   * @see #getInstance
   */
  private final Instance instance;

  /**
   * Buffer used to copy from the input stream to the output stream.
   *
   * @see #execute
   */
  private byte[] buffer = new byte[1024];

  /**
   * Stores the file name after pre processing.
   *
   * @see #preprocess
   * @see #setFileName
   * @see #getFileName
   */
  private String fileName = null;

  /////////////////////////////////////////////////////////////////////////////

  /**
   * This is the getter method for instance variable {@link #instance}.
   *
   * @return value of instance variable {@link #instance}
   * @see #instance
   */
  protected Instance getInstance()  {
    return this.instance;
  }

  /**
   * This is the setter method for instance variable {@link #fileName}.
   *
   * @param _fileName new fileName for instance variable {@link #fileName}
   * @see #fileName
   * @see #getFileName
   */
  private void setFileName(String _fileName)  {
    this.fileName = _fileName;
  }

  /**
   * This is the getter method for instance variable {@link #fileName}.
   *
   * @return the fileName of the instance variable {@link #fileName}.
   * @see #fileName
   * @see #setFileName
   */
  public String getFileName()  {
    return this.fileName;
  }
}