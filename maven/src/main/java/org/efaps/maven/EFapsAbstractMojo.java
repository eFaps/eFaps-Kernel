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

package org.efaps.maven;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.naming.spi.ObjectFactory;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import org.apache.commons.digester.Digester;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.logging.Log;
import org.apache.slide.transaction.SlideTransactionManager;

import org.efaps.admin.runlevel.RunLevel;
import org.efaps.db.Context;
import org.efaps.db.databases.AbstractDatabase;
import org.efaps.maven.install.Application;
import org.efaps.maven.install.ApplicationVersion;
import org.efaps.maven.install.FileSet;
import org.efaps.util.EFapsException;

/**
 * 
 * @author tmo
 * @version $Id$
 */
abstract class EFapsAbstractMojo implements Mojo {

  /////////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Theoretically all efaps contexts object instances must include a
   * transaction manager.
   */
  final public static TransactionManager transactionManager = new SlideTransactionManager();

  /////////////////////////////////////////////////////////////////////////////
  // instance variables

  /**
   * The apache maven logger is stored in this instance variable.
   * 
   * @see #getLog
   * @see #setLog
   */
  private Log log = null;

  /**
   * @parameter expression="${org.efaps.db.factory}"
   */
  private String factory;

  /**
   * Holds all properties of the connection to the database.
   * 
   * @parameter
   */
  private Properties connection;

  /**
   * Stores the name of the logged in user.
   * 
   * @see #login
   */
  private String userName;

  /**
   * @parameter expression="${org.efaps.db.type}"
   */
  private String type;

  /**
   * Project classpath.
   *
   * @parameter expression="${project.compileClasspathElements}"
   * @required
   * @readonly
   */
  private List<String> classpathElements;

  /**
   * @parameter expression="${basedir}/src/main/efaps/versions.xml"
   */
  private File versionFile;

  /**
   * @parameter expression="${basedir}/src/main/efaps"
   */
  private File eFapsDir;

  /////////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * <code>null</code> is returned, of the version file could not be opened
   * and read.
   * 
   * @return application instance with all version information
   * @todo description
   * @todo better definition of include dir / file
   */
  protected Application getApplication() {
    Application appl = null;
    try {
      Digester digester = new Digester();
      digester.setValidating(false);
      digester.addObjectCreate("install", Application.class);

      digester.addCallMethod("install/application", "setApplication", 1);
      digester.addCallParam("install/application", 0);

      digester.addObjectCreate("install/version", ApplicationVersion.class);
      digester.addSetNext("install/version", "addVersion");

      digester.addCallMethod("install/version", "setNumber", 1, new Class[]{Long.class});
      digester.addCallParam("install/version", 0, "number");

      digester.addCallMethod("install/version", "setCompile", 1, new Class[]{Boolean.class});
      digester.addCallParam("install/version", 0, "compile");

      appl = (Application) digester.parse(this.versionFile);

      FileSet fileSet = new FileSet();
      fileSet.setDirectory(null, eFapsDir.toString());
      fileSet.addIncludeDir(".*");
      fileSet.addIncludeFile(".*xml$");
      for (ApplicationVersion applVers : appl.getVersions()) {
        applVers.addFileSet(fileSet);
        applVers.setClasspathElements(this.classpathElements);
      }
    } catch (IOException e) {
      getLog().error(
          "Could not open / read version file " + "'" + this.versionFile + "'");
    } catch (Exception e) {
    }
    return appl;
  }

  /**
   * Initiliase the database information read from the bootstrap file:
   * <ul>
   * <li>read boostrap properties from bootstrap file</li>
   * <li>configure the database type</li>
   * <li>initiliase the sql datasource (JDBC connection to the database)</li>
   * </ul>
   */
  protected boolean initDatabase() {
    boolean initialised = false;

    // configure database type
    String dbClass = null;
    try {
      AbstractDatabase dbType = (AbstractDatabase) (Class.forName(this.type))
          .newInstance();
      if (dbType == null) {
        getLog().error("could not initaliase database type");
      }
      Context.setDbType(dbType);
      initialised = true;
    } catch (ClassNotFoundException e) {
      getLog().error(
          "could not found database description class " + "'" + dbClass + "'",
          e);
    } catch (InstantiationException e) {
      getLog().error(
          "could not initialise database description class " + "'" + dbClass
              + "'", e);
    } catch (IllegalAccessException e) {
      getLog().error(
          "could not access database description class " + "'" + dbClass + "'",
          e);
    }

    // buildup reference and initialise datasource object
    Reference ref = new Reference(DataSource.class.getName(), this.factory,
        null);
    for (Object key : this.connection.keySet()) {
      Object value = this.connection.get(key);
      ref.add(new StringRefAddr(key.toString(), (value == null) ? null : value
          .toString()));
    }
    ObjectFactory of = null;
    try {
      Class factClass = Class.forName(ref.getFactoryClassName());
      of = (ObjectFactory) factClass.newInstance();
    } catch (ClassNotFoundException e) {
      getLog().error(
          "could not found data source class " + "'"
              + ref.getFactoryClassName() + "'", e);
    } catch (InstantiationException e) {
      getLog().error(
          "could not initialise data source class " + "'"
              + ref.getFactoryClassName() + "'", e);
    } catch (IllegalAccessException e) {
      getLog().error(
          "could not access data source class " + "'"
              + ref.getFactoryClassName() + "'", e);
    }
    if (of != null) {
      DataSource ds = null;
      try {
        ds = (DataSource) of.getObjectInstance(ref, null, null, null);
      } catch (Exception e) {
        getLog().error(
            "coud not get object instance of factory " + "'"
                + ref.getFactoryClassName() + "'", e);
      }
      if (ds != null) {
        Context.setDataSource(ds);
        initialised = initialised && true;
      }
    }
    return initialised;
  }

  /**
   * The user with given user name and password makes a login.
   * 
   * @param _userName
   *          name of user who wants to make a login
   * @param _password
   *          password of the user used to check
   * @throws EFapsException
   *           if the user could not login
   * @see #userName
   * @todo real login with check of password
   */
  protected void login(final String _userName, final String _password)
                                                                      throws EFapsException {
    this.userName = _userName;
  }

  /**
   * Reloads the internal eFaps cache.
   * 
   * @todo remove Exception
   */
  protected void reloadCache() throws EFapsException, Exception {
    startTransaction();
    RunLevel.init("shell");
    RunLevel.execute();
    abortTransaction();
  }

  /**
   * 
   * 
   * @todo remove Exception
   * @todo description
   */
  protected void startTransaction() throws EFapsException, Exception {
    getTransactionManager().begin();
    Context.newThreadContext(getTransactionManager().getTransaction(),
        this.userName);
  }

  /**
   * @todo remove Exception
   * @todo description
   */
  protected void abortTransaction() throws EFapsException, Exception {
    getTransactionManager().rollback();
    Context.getThreadContext().close();
  }

  /**
   * @todo remove Exception
   * @todo description
   */
  protected void commitTransaction() throws EFapsException, Exception {
    getTransactionManager().commit();
    Context.getThreadContext().close();
  }

  /**
   * @todo description
   */
  protected TransactionManager getTransactionManager() {
    return transactionManager;
  }

  /////////////////////////////////////////////////////////////////////////////
  // instance getter and setter methods

  /**
   * This is the setter method for instance variable {@link #log}.
   * 
   * @param _log
   *          new value for instance variable {@link #log}
   * @see #log
   * @see #getLog
   */
  public void setLog(final Log _log) {
    this.log = _log;
  }

  /**
   * This is the getter method for instance variable {@link #log}.
   * 
   * @return value of instance variable {@link #log}
   * @see #log
   * @see #setLog
   */
  public Log getLog() {
    return this.log;
  }

  /**
   * This is the getter method for instance variable {@link #classpathElements}.
   * 
   * @return value of instance variable {@link #classpathElements}
   * @see #classpathElements
   */
  protected List<String> getClasspathElements() {
    return this.classpathElements;
  }
}
