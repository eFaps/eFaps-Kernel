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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
import org.efaps.update.access.AccessSetUpdate;
import org.efaps.update.access.AccessTypeUpdate;
import org.efaps.update.common.SystemAttributeUpdate;
import org.efaps.update.datamodel.SQLTableUpdate;
import org.efaps.update.datamodel.TypeUpdate;
import org.efaps.update.dbproperty.DBPropertiesUpdate;
import org.efaps.update.integration.WebDAVUpdate;
import org.efaps.update.program.CSSUpdate;
import org.efaps.update.program.JavaScriptUpdate;
import org.efaps.update.program.JavaUpdate;
import org.efaps.update.program.XSLUpdate;
import org.efaps.update.ui.CommandUpdate;
import org.efaps.update.ui.FormUpdate;
import org.efaps.update.ui.ImageUpdate;
import org.efaps.update.ui.MenuUpdate;
import org.efaps.update.ui.SearchUpdate;
import org.efaps.update.ui.TableUpdate;
import org.efaps.update.user.JAASSystemUpdate;
import org.efaps.update.user.RoleUpdate;
import org.efaps.util.EFapsException;

/**
 * TODO description
 *
 * @author tmo
 * @author jmox
 * @version $Id$
 */
public class Install {

  public enum FileType {
    JAVA("source-java", "readXMLFile"),
    JS("source-js", "readFile"),
    CSS("source-css", "readFile"),
    XML("install-xml", "readXMLFile"),
    XSL("source-xsl", "readFile");

    public String type;

    public String method;

    private FileType(final String _type, final String _method) {
      this.type = _type;
      this.method = _method;
    }

  }

  /**
   * List of all update classes. The order is also used for the install order.
   *
   * @see #install(Long)
   */
  private final static Map<Class<? extends AbstractUpdate>, FileType> updateClasses =
      new LinkedHashMap<Class<? extends AbstractUpdate>, FileType>();
  static  {
    if (updateClasses.isEmpty()) {
      updateClasses.put(RoleUpdate.class, FileType.XML);
      updateClasses.put(SQLTableUpdate.class, FileType.XML);
      updateClasses.put(TypeUpdate.class, FileType.XML);
      updateClasses.put(JAASSystemUpdate.class, FileType.XML);
      updateClasses.put(AccessTypeUpdate.class, FileType.XML);
      updateClasses.put(AccessSetUpdate.class, FileType.XML);
      updateClasses.put(ImageUpdate.class, FileType.XML);
      updateClasses.put(FormUpdate.class, FileType.XML);
      updateClasses.put(TableUpdate.class, FileType.XML);
      updateClasses.put(SearchUpdate.class, FileType.XML);
      updateClasses.put(MenuUpdate.class, FileType.XML);
      updateClasses.put(CommandUpdate.class, FileType.XML);
      updateClasses.put(WebDAVUpdate.class, FileType.XML);
      updateClasses.put(JavaUpdate.class, FileType.JAVA);
      updateClasses.put(SystemAttributeUpdate.class, FileType.XML);
      updateClasses.put(CSSUpdate.class, FileType.CSS);
      updateClasses.put(XSLUpdate.class, FileType.XSL);
      updateClasses.put(JavaScriptUpdate.class, FileType.JS);
    }
  }

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

  private Long maxVersion;

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
    for (final Entry<Class<? extends AbstractUpdate>, FileType> entry : updateClasses.entrySet()) {
      final Class<? extends AbstractUpdate> updateClass = entry.getKey();
      for (final AbstractUpdate update : this.cache.get(updateClass)) {
        update.createInDB(jexlContext);
        if (!bigTrans)  {
          Context.commit();
          Context.begin(user);
        }
      }
    }

    // and update them
    for (final Entry<Class<? extends AbstractUpdate>, FileType> entry : updateClasses.entrySet()) {
      final Class<? extends AbstractUpdate> updateClass = entry.getKey();
      for (final AbstractUpdate update : this.cache.get(updateClass)) {
        update.updateInDB(jexlContext);
        if (!bigTrans)  {
          Context.commit();
          Context.begin(user);
        }
      }
    }
  }

  /**
   * @see #initialised
   */
  public void initialise()
      throws EFapsException
  {
    if (!this.initialised) {
      this.initialised = true;
      this.cache.clear();

      for (final Entry<Class<? extends AbstractUpdate>, FileType> entry : updateClasses.entrySet()) {
        final List<AbstractUpdate> list = new ArrayList<AbstractUpdate>();

        final Class<? extends AbstractUpdate> updateClass = entry.getKey();
        this.cache.put(updateClass, list);

        Method method = null;
        try {
          method = updateClass.getMethod(entry.getValue().method, URL.class, URL.class);
        } catch (SecurityException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (NoSuchMethodException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        for (final InstallFile file : this.files) {
          if (file.getType().equals(entry.getValue().type)) {
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
              final AbstractUpdate update = (AbstractUpdate) obj;
              update.setApplication(this.application);
              update.setMaxVersion(this.maxVersion);
              list.add(update);
            }
          }
        }
      }
    }
  }

  public void importData() throws Exception {

    for (final Entry<Class<? extends ImportInterface>, FileType> entry : this.importClasses
        .entrySet()) {
      final Method method =
          entry.getKey().getMethod(entry.getValue().method, URL.class);

      for (final InstallFile file : this.files) {
        if (file.getType().equals(entry.getValue().type)) {
          final Object obj = method.invoke(null, file.getUrl());
          if (obj != null) {
            ((ImportInterface) obj).updateInDB();
          }
        }
      }
    }
  }

  /**
   * Appends a new file defined through an url. The initialised flag is
   * automatically reseted.
   *
   * @param _url
   *                file to append
   * @see #urls
   * @see #initialised
   */
  public void addFile(final URL _url, final String _type) {
    this.files.add(new InstallFile(_url, _type));
    this.initialised = false;
  }

  // ///////////////////////////////////////////////////////////////////////////
  // instance getter and setter methods

  /**
   * This is the getter method for the instance variable {@link #files}.
   *
   * @return value of instance variable {@link #files}
   */
  public List<InstallFile> getFiles() {
    return this.files;
  }

  /**
   * This is the setter method for the instance variable {@link #application}.
   *
   * @param application
   *                the application to set
   */
  public void setApplication(String application) {
    this.application = application;
  }

  /**
   * This is the setter method for the instance variable {@link #maxVersion}.
   *
   * @param maxVersion
   *                the maxVersion to set
   */
  public void setMaxVersion(Long maxVersion) {
    this.maxVersion = maxVersion;
  }

  /**
   * This is the setter method for the instance variable {@link #rootDir}.
   *
   * @param _rootDir  the rootDir to set
   */
  public void setRootDir(final URL _rootDir) {
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

  public class InstallFile {

    private final URL url;

    private final String type;

    public InstallFile(final URL _url, final String _type) {
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
    public String getType() {
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
