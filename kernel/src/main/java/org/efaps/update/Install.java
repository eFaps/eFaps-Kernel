/*
 * Copyright 2003-2007 The eFaps Team
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

import org.efaps.update.access.AccessSetUpdate;
import org.efaps.update.access.AccessTypeUpdate;
import org.efaps.update.common.SystemAttributeUpdate;
import org.efaps.update.datamodel.SQLTableUpdate;
import org.efaps.update.datamodel.TypeUpdate;
import org.efaps.update.integration.WebDAVUpdate;
import org.efaps.update.program.JavaUpdate;
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
 * @author tmo
 * @author jmox
 * @version $Id$
 * @todo description
 */
public class Install {

  public enum FileType {
    JAVA("source-java", "readXMLFile"),
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

  // ///////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * List of all update classes. The order is also used for the install order.
   *
   * @see #install
   */
  private final Map<Class<? extends AbstractUpdate>, FileType> updateClasses =
      new LinkedHashMap<Class<? extends AbstractUpdate>, FileType>();
  {
    if (this.updateClasses.size() == 0) {
      this.updateClasses.put(RoleUpdate.class, FileType.XML);
      this.updateClasses.put(SQLTableUpdate.class, FileType.XML);
      this.updateClasses.put(TypeUpdate.class, FileType.XML);
      this.updateClasses.put(JAASSystemUpdate.class, FileType.XML);
      this.updateClasses.put(AccessTypeUpdate.class, FileType.XML);
      this.updateClasses.put(AccessSetUpdate.class, FileType.XML);
      this.updateClasses.put(ImageUpdate.class, FileType.XML);
      this.updateClasses.put(FormUpdate.class, FileType.XML);
      this.updateClasses.put(TableUpdate.class, FileType.XML);
      this.updateClasses.put(SearchUpdate.class, FileType.XML);
      this.updateClasses.put(MenuUpdate.class, FileType.XML);
      this.updateClasses.put(CommandUpdate.class, FileType.XML);
      this.updateClasses.put(WebDAVUpdate.class, FileType.XML);
      this.updateClasses.put(JavaUpdate.class, FileType.XML);
      this.updateClasses.put(SystemAttributeUpdate.class, FileType.XML);
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

  // ///////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * Installs the xml update scripts of the schema definitions for this version
   * defined in {@link #number}.
   */
  @SuppressWarnings("unchecked")
  public void install(final Long _number) throws EFapsException, Exception {

    // initialse cache
    initialise();

    // initiliase JexlContext (used to evalute version)
    final JexlContext jexlContext = JexlHelper.createContext();
    if (_number != null) {
      jexlContext.getVars().put("version", _number);
    }

    // make update
    for (final Entry<Class<? extends AbstractUpdate>, FileType> entry : this.updateClasses
        .entrySet()) {
      final Class<? extends AbstractUpdate> updateClass = entry.getKey();
      for (final AbstractUpdate update : this.cache.get(updateClass)) {
        update.updateInDB(jexlContext);
      }
    }
  }

  /**
   * @see #initialised
   */
  public void initialise() throws Exception {
    if (!this.initialised) {
      this.initialised = true;
      this.cache.clear();

      for (final Entry<Class<? extends AbstractUpdate>, FileType> entry : this.updateClasses
          .entrySet()) {
        final List<AbstractUpdate> list = new ArrayList<AbstractUpdate>();

        final Class<? extends AbstractUpdate> updateClass = entry.getKey();
        this.cache.put(updateClass, list);

        final Method method =
            updateClass.getMethod(entry.getValue().method, URL.class);
        for (final InstallFile file : this.files) {
          if (file.getType().equals(entry.getValue().type)) {
            final Object obj = method.invoke(null, file.getUrl());
            if (obj != null) {
              list.add((AbstractUpdate) obj);
            }
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

}
