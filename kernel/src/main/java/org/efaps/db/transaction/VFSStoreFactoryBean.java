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

package org.efaps.db.transaction;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.apache.commons.vfs.provider.FileProvider;

import org.efaps.admin.datamodel.Type;
import org.efaps.util.EFapsException;

/**
 *
 * @author tmo
 * @version $Rev$
 */
public class VFSStoreFactoryBean extends DefaultFileSystemManager  {

  /**
   * Logging instance used in this class.
   */
  private final static Log LOG = LogFactory.getLog(VFSStoreFactoryBean.class);

  /**
   * Property Name defined on the type for the virtual file system name.
   */
  private final static String PROPERY_STORENAME  = "StoreVFSName";

  /**
   * The base name of this VFS store is stored in this instance string.
   */
  private String baseName = null;

  /**
   * The base file is stored from which all uris depends.
   */
  private FileObject baseFile = null;

  /**
   * The instance variable hold the file provider (e.g. local file, ftp or
   * sftp).
   */
  private FileProvider fileProvider = null;

  /**
   * The virtual file system manger is initialised.
   *
   * @see DefaultFileSystemManager#init
   */
  public VFSStoreFactoryBean()  {
    try  {
      init();
    } catch (FileSystemException e)  {
      LOG.error("could not initialise the VFS file manager", e);
    }
  }

  /**
   * The base name in which all the files of the store are located, is set. If
   * the file provider was already set with {@link #setProvider}, the
   * {@link #baseFile} is initialised to the base name.
   *
   * @param _baseName base path including schema to set
   * @see #baseName
   * @see #baseFile
   */
  public void setBaseName(final String _baseName)  {
    this.baseName = _baseName;
    if (this.fileProvider != null)  {
      try  {
        addProvider(this.baseName, this.fileProvider);
        this.baseFile = this.fileProvider.findFile(null, this.baseName, null);
      } catch (FileSystemException e)  {
        LOG.error("could not get base file for '" + _baseName + "'", e);
      }
    }
  }

  /**
   * The provider is set to the given class name. If the {@link #baseName} is
   * already set, the {@link #baseFile} is also set.
   *
   * @param _providerName file provider class name to set
   * @see #setBaseName
   * @see #fileProvider
   */
  public void setProvider(final String _providerName)  {
    try  {
      this.fileProvider = (FileProvider) Class.forName(_providerName).newInstance();
    } catch (ClassNotFoundException e)  {
        LOG.error("could not found class '" + _providerName + "'", e);
    } catch (InstantiationException e)  {
        LOG.error("could not instantiate class '" + _providerName + "'", e);
    } catch (IllegalAccessException e)  {
        LOG.error("could not access class '" + _providerName + "'", e);
    }
    if ((this.fileProvider != null) && (this.baseName != null))  {
      try  {
        addProvider(this.baseName, this.fileProvider);
        this.baseFile = this.fileProvider.findFile(null, this.baseName, null);
      } catch (FileSystemException e)  {
        LOG.error("could not get base file for '" + this.baseName + "'", e);
      }
    }
  }

  /**
   * Returns the file object linking to the given uri by using the file
   * provider in {@link #fileProvider}. The base directory is automatically
   * added in front of the uri.
   *
   * @param _uri uri to the file / path
   * @return file object linking the given uri parameter
   * @throws FileSystemException if file is not found
   */
  public FileObject findFile(final String _uri) throws FileSystemException  {
    return this.fileProvider.findFile(this.baseFile, this.baseName + "/" + _uri, null);
  }

  //////////////////////////////////////////////////////////////////////////////

  /**
   * Returns the instance of this class needed to store files for the given
   * type.
   *
   * @param _type type for which the file provider is searched
   * @return file provider for given type
   * @throws EFapsException if the file provider is not accessable or does not
   *         exists
   */
  public static VFSStoreFactoryBean getFileProvider(final Type _type) throws EFapsException  {
    VFSStoreFactoryBean ret = null;

    try  {
      InitialContext initialContext = new InitialContext();

      Context context = (Context) initialContext.lookup("java:comp/env");

      ret = (VFSStoreFactoryBean) context.lookup(_type.getProperty(PROPERY_STORENAME));

    } catch (Throwable e)  {
      e.printStackTrace();
      LOG.error("vfs store could not be initialised! resource not accessable", e);
      throw new EFapsException(VFSStoreFactoryBean.class,
          "getFileProvider.throwable", e);
    }

    if (ret == null)  {
      LOG.error("vfs store could not be initialised! Not found for type "
          + "'" + _type.getName() + "'");
      throw new EFapsException(VFSStoreFactoryBean.class,
          "getFileProvider.doesNotExists",
          _type.getName(), _type.getProperty(PROPERY_STORENAME));
    }

    return ret;
  }
}
