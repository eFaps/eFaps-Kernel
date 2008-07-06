/*
 * Copyright 2003-2008 The eFaps Team
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

package org.efaps.update;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jexl.JexlHelper;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.efaps.db.Context;
import org.efaps.db.databases.AbstractDatabase;
import org.efaps.importer.DataImport;
import org.efaps.update.dbproperty.DBPropertiesUpdate;
import org.efaps.util.EFapsException;
import org.xml.sax.SAXException;

/**
 * TODO description
 *
 * @author tmo
 * @author jmox
 * @version $Id$
 */
public class Install
{
  /**
   * List of all import classes. The order is also used for the import order.
   *
   * @see #importData()
   */
  private final Map<Class<? extends ImportInterface>, FileType> importClasses =
      new LinkedHashMap<Class<? extends ImportInterface>, FileType>();
  {
    if (this.importClasses.size() == 0) {
      this.importClasses.put(DataImport.class, FileType.XML);
      this.importClasses.put(DBPropertiesUpdate.class, FileType.XML);
    }
  }

  // ///////////////////////////////////////////////////////////////////////////
  // instance variables

  /**
   * All defined file urls which are updated.
   *
   * @see #addFile(URL, String)
   */
  private final List<InstallFile> files = new ArrayList<InstallFile>();

  /**
   * Flag to store that the cache is initialised.
   *
   * @see #initialise
   * @see #addURL
   */
  private boolean initialised = false;

  /**
   * Cache with all update instances (loaded from the list of {@link #urls}).
   *
   * @see #initialise
   * @see #install
   */
  private final Map<Class<? extends AbstractUpdate>, List<AbstractUpdate>> cache =
      new HashMap<Class<? extends AbstractUpdate>, List<AbstractUpdate>>();

  private String application;

  private URL rootDir;

  // ///////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * Installs the XML update scripts of the schema definitions for this version
   * defined in {@link #number}. The install itself is done for given version
   * normally in one big transaction. If the database does not support to big
   * transactions (method {@link AbstractDatabase#supportsBigTransactions},
   * each modification of one update is commited within small single
   * transactions.
   *
   * @param _number       number to install
   * @param _latestNumber latest version number to install (e..g. defined in
   *                      the version.xml file)
   * @see AbstractDatabase#supportsBigTransactions  is used to get information
   *                                                about the support of very
   *                                                big transactions from the
   *                                                database
   */
  @SuppressWarnings("unchecked")
  public void install(final Long _number,
                      final Long _latestNumber)
      throws EFapsException
  {
    final boolean bigTrans = Context.getDbType().supportsBigTransactions();
    final String user = (org.efaps.db.Context.getThreadContext().getPerson() != null)
                        ? org.efaps.db.Context.getThreadContext().getPerson().getName()
                        : null;

    // initialize cache
    initialise();

    // initialize JexlContext (used to evaluate version)
    final JexlContext jexlContext = JexlHelper.createContext();
    if (_number != null) {
      jexlContext.getVars().put("version", _number);
    }
    if (_latestNumber != null)  {
      jexlContext.getVars().put("latest", _latestNumber);
    }

    // create all objects
    for (final Map.Entry<Class<? extends AbstractUpdate>, List<AbstractUpdate>> entry : this.cache.entrySet())  {
      for (final AbstractUpdate update : entry.getValue())  {
        update.createInDB(jexlContext);
        if (!bigTrans)  {
          Context.commit();
          Context.begin(user);
        }
      }
    }
/*    for (final FileType fileType : FileType.values()) {
      for (final Class<? extends AbstractUpdate> updateClass : fileType.clazzes)  {
        for (final AbstractUpdate update : this.cache.get(updateClass)) {
          update.createInDB(jexlContext);
          if (!bigTrans)  {
            Context.commit();
            Context.begin(user);
          }
        }
      }
    }
*/

    // and update them
    for (final Map.Entry<Class<? extends AbstractUpdate>, List<AbstractUpdate>> entry : this.cache.entrySet())  {
      for (final AbstractUpdate update : entry.getValue())  {
        update.updateInDB(jexlContext);
        if (!bigTrans)  {
          Context.commit();
          Context.begin(user);
        }
      }
    }
/*    for (final FileType fileType : FileType.values()) {
      for (final Class<? extends AbstractUpdate> updateClass : fileType.clazzes)  {
        for (final AbstractUpdate update : this.cache.get(updateClass)) {
          update.updateInDB(jexlContext);
          if (!bigTrans)  {
            Context.commit();
            Context.begin(user);
          }
        }
      }
    }*/
  }

  /**
   * @throws SAXException
   * @throws IOException
   * @throws IOException
   * @throws FileNotFoundException
   * @see #initialised
   */
  public void initialise()
      throws EFapsException
  {
    if (!this.initialised) {
      this.initialised = true;
      this.cache.clear();

      for (final FileType fileType : FileType.values()) {

        if (fileType == FileType.XML)  {
          for (final InstallFile file : this.files) {
            if (file.getType() == fileType) {
try  {
  SaxHandler handler = new SaxHandler();
  AbstractUpdate elem = handler.parse(file.getUrl());

  List<AbstractUpdate> list = this.cache.get(elem.getClass());
  if (list == null)  {
    list = new ArrayList<AbstractUpdate>();
    this.cache.put(elem.getClass(), list);
  }
  list.add(handler.elem);
} catch (Exception e)  {
  System.out.println("Error in File "+file);
  e.printStackTrace();
  throw new Error(e);
}
            }
          }
System.out.println(""+this.cache);
        } else
        for (final Class<? extends AbstractUpdate> updateClass : fileType.clazzes)  {

          final List<AbstractUpdate> list = new ArrayList<AbstractUpdate>();
          this.cache.put(updateClass, list);

          Method method = null;
          try  {
            method = updateClass.getMethod("readFile", URL.class, URL.class);
          } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
          for (final InstallFile file : this.files) {
            if (file.getType() == fileType) {
              Object obj = null;
              try {
                obj = method.invoke(null, this.rootDir, file.getUrl());
              } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
              } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
              } catch (InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
              }
              if (obj != null) {
                list.add((AbstractUpdate) obj);
              }
            }
          }
        }
      }
    }
  }

  public void importData() throws Exception {

    for (final Entry<Class<? extends ImportInterface>, FileType> entry : this.importClasses.entrySet()) {
      final Method method = entry.getKey().getMethod("readFile", URL.class);

      for (final InstallFile file : this.files) {
        if (file.getType() == entry.getValue()) {
          final Object obj = method.invoke(null, file.getUrl());
          if (obj != null) {
            ((ImportInterface) obj).updateInDB();
          }
        }
      }
    }
  }

  /**
   * Appends a new file defined through an URL and the string representation of
   * the file type.
   *
   * @param _url    URL of the file to append
   * @param _type   type of the file
   * @see #files
   * @see #initialised
   * @see #addFile(URL, FileType) method called to add the URL after convert
   *                              the string representation of the type to a
   *                              file type instance
   */
  public void addFile(final URL _url,
                      final String _type)
  {
    addFile(_url, FileType.getFileTypeByType(_type));
  }

  /**
   * Appends a new file.
   *
   * @param _file   file to add
   * @throws MalformedURLException if the path cannot be parsed as a URL
   * @see #addFile(URL, FileType) method called to add the file after convert
   *                              to an URL and evaluate of the file type
   */
  public void addFile(final File _file)
      throws MalformedURLException
  {
/*    addFile(_file.toURL(),
            FileType.getFileTypeByExensione(_file.getExtension()));
*/  }

  /**
   * Appends a new file defined through an URL. The initialized flag
   * {@link #initialised} is automatically reseted.
   *
   * @param _url        URL of the file to add
   * @param _fileType   file type of the file to add
   */
  public void addFile(final URL _url, FileType _fileType)
  {
    this.files.add(new InstallFile(_url, _fileType));
    this.initialised = false;
  }

  /////////////////////////////////////////////////////////////////////////////
  // instance getter and setter methods

  /**
   * This is the getter method for the instance variable {@link #files}.
   *
   * @return value of instance variable {@link #files}
   */
  public List<InstallFile> getFiles()
  {
    return this.files;
  }

  /**
   * This is the setter method for the instance variable {@link #application}.
   *
   * @param _application
   *                the application to set
   */
  public void setApplication(String _application)
  {
    this.application = _application;
  }

  /**
   * This is the setter method for the instance variable {@link #rootDir}.
   *
   * @param _rootDir  the rootDir to set
   */
  public void setRootDir(final URL _rootDir)
  {
    this.rootDir = _rootDir;
  }

  /**
   * Returns a string representation with values of all instance variables.
   *
   * @return string representation of this Application
   */
  @Override
  public String toString() {
    return new ToStringBuilder(this).append("urls", this.files).toString();
  }

  private class InstallFile {

    private final URL url;

    private final FileType type;

    public InstallFile(final URL _url, final FileType _type)
    {
      this.url = _url;
      this.type = _type;
    }

    /**
     * This is the getter method for the instance variable {@link #url}.
     *
     * @return value of instance variable {@link #url}
     */
    public URL getUrl() {
      return this.url;
    }

    /**
     * This is the getter method for the instance variable {@link #type}.
     *
     * @return value of instance variable {@link #type}
     */
    public FileType getType() {
      return this.type;
    }

  }

  /**
   * This interface is used in {@link #org.efaps.update.Install.importData()}.
   *
   * @see #importClasses
   */
  public interface ImportInterface {

    public void updateInDB() throws EFapsException;

  }

}
