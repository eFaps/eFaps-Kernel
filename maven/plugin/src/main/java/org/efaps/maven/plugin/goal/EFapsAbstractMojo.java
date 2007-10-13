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

package org.efaps.maven.plugin.goal;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.naming.spi.ObjectFactory;
import javax.sql.DataSource;

import org.apache.commons.digester.Digester;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.logging.Log;
import org.apache.slide.transaction.SlideTransactionManager;
import org.jfrog.maven.annomojo.annotations.MojoParameter;

import org.efaps.admin.runlevel.RunLevel;
import org.efaps.db.Context;
import org.efaps.db.databases.AbstractDatabase;
import org.efaps.maven.logger.SLF4JOverMavenLog;
import org.efaps.maven.plugin.goal.efaps.install.Application;
import org.efaps.maven.plugin.goal.efaps.install.ApplicationVersion;
import org.efaps.maven.plugin.goal.efaps.install.FileSet;
import org.efaps.util.EFapsException;

/**
 * 
 * @author tmo
 * @version $Id$
 */
public abstract class EFapsAbstractMojo implements Mojo {

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
   */
  @MojoParameter(expression = "${org.efaps.db.factory}")
  private String factory;

  /**
   * Holds all properties of the connection to the database.
   */
  @MojoParameter
  private Properties connection;

  /**
   * Stores the name of the logged in user.
   * 
   * @see #login
   */
  @MojoParameter(required = true)
  private String userName;

  /**
   * Stores the name of the logged in user.
   * 
   * @see #login
   */
  @MojoParameter(required = true)
  private String passWord;

  /**
   */
  @MojoParameter(expression = "${org.efaps.db.type}",
                 required = true)
  private String type;

  /**
   * Project classpath.
   */
  @MojoParameter(expression = "${project.compileClasspathElements}",
                 required = true,
                 readonly = true)
  private List<String> classpathElements;

  /**
   */
  @MojoParameter(expression = "${basedir}/src/main/efaps/versions.xml")
  private File versionFile;

  /**
   */
  @MojoParameter(expression = "${basedir}/src/main/efaps")
  private File eFapsDir;

  /////////////////////////////////////////////////////////////////////////////
  // constructors / destructors

  protected EFapsAbstractMojo()  {
  }

  /////////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * @todo better way instead of catching class not found exception (needed for
   *       the shell!)
   */
  protected void init()  {
/*    System.getProperties().setProperty(
            org.apache.commons.logging.Log.class.getName(),
            Maven2SLF4JLog.class.getName());*/
    try  {
      Class.forName("org.efaps.maven.logger.SLF4JOverMavenLog");
      SLF4JOverMavenLog.LOGGER = getLog();
    } catch (ClassNotFoundException e)  {
    }
    
    Context.setTransactionManager(new SlideTransactionManager());
    initDatabase();
  }

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
      for (File file : fileSet.getFiles())  {
        appl.addURL(file.toURL());
      }
      for (ApplicationVersion applVers : appl.getVersions()) {
        applVers.setClasspathElements(this.classpathElements);
      }
    } catch (IOException e) {
      getLog().error(
          "Could not open / read version file " + "'" + this.versionFile + "'");
    } catch (Exception e) {
      getLog().error(e);
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

    getLog().info("Initialise Database Connection");

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
    Reference ref = new Reference(DataSource.class.getName(), 
                                  this.factory,
                                  null);
    for (Object key : this.connection.keySet()) {
      Object value = this.connection.get(key);
      ref.add(new StringRefAddr(key.toString(),
                                (value == null) ? null : value.toString()));
    }
    ObjectFactory of = null;
    try {
      Class<?> factClass = Class.forName(ref.getFactoryClassName());
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
    Context.begin(this.userName);
  }

  /**
   * @todo remove Exception
   * @todo description
   */
  protected void abortTransaction() throws EFapsException, Exception {
    Context.rollback();
  }

  /**
   * @todo remove Exception
   * @todo description
   */
  protected void commitTransaction() throws EFapsException, Exception {
    Context.commit();
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
   * This is the getter method for instance variable {@link #userName}.
   * 
   * @return value of instance variable {@link #userName}
   * @see #userName
   */
  protected String getUserName() {
    return this.userName;
  }

  /**
   * This is the getter method for instance variable {@link #passWord}.
   * 
   * @return value of instance variable {@link #passWord}
   * @see #passWord
   */
  protected String getPassWord() {
    return this.passWord;
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
