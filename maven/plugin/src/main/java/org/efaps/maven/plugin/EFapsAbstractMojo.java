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

package org.efaps.maven.plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.naming.spi.ObjectFactory;
import javax.sql.DataSource;

import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.logging.Log;
import org.apache.slide.transaction.SlideTransactionManager;
import org.jfrog.maven.annomojo.annotations.MojoParameter;
import org.mortbay.naming.NamingUtil;

import org.efaps.admin.runlevel.RunLevel;
import org.efaps.db.Context;
import org.efaps.db.databases.AbstractDatabase;
import org.efaps.maven.logger.SLF4JOverMavenLog;
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
  @MojoParameter(required = true,
                 expression = "${org.efaps.db.factory}")
  private String factory;

  /**
   * Holds all properties of the connection to the database. The properties
   * are separated by a comma.
   */
  @MojoParameter(expression = "${org.efaps.db.connection}",
                 required = true)
  private String connection;

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
    try  {
      Class.forName("org.efaps.maven.logger.SLF4JOverMavenLog");
      SLF4JOverMavenLog.LOGGER = getLog();
    } catch (ClassNotFoundException e)  {
    }
    
    initDatabase();
  }

  /**
   * Initialize the database information set from maven in the parameter
   * instance variables.
   * <ul>
   * <li>configure the database type</li>
   * <li>initialize the sql datasource (JDBC connection to the database)</li>
   * <li>initialize transaction manager</li>
   * </ul>
   *
   * @return <i>true</i> if database connection is initialized
   * @see #convertToMap
   * @see #type       database class
   * @see #factory    factory class name
   * @see #connection connection properties
   */
  protected boolean initDatabase() {

    boolean initialised = false;

    getLog().info("Initialise Database Connection");

    final javax.naming.Context compCtx;
    try {
      final InitialContext context = new InitialContext();
      compCtx = (javax.naming.Context)context.lookup ("java:comp");
    } catch (NamingException e) {
      throw new Error("Could not initialize JNDI", e);
    }

    // configure database type
    try {
      AbstractDatabase dbType
              = (AbstractDatabase)(Class.forName(this.type)).newInstance();
      if (dbType == null) {
        getLog().error("could not initaliase database type");
      } else  {
        NamingUtil.bind(compCtx, "env/eFaps/dbType", dbType);
        initialised = true;
      }
    } catch (ClassNotFoundException e) {
      getLog().error(
          "could not found database description class " + "'" + this.type + "'",
          e);
    } catch (InstantiationException e) {
      getLog().error(
          "could not initialise database description class " + "'" + this.type
              + "'", e);
    } catch (IllegalAccessException e) {
      getLog().error(
          "could not access database description class " + "'" + this.type + "'",
          e);
    } catch (NamingException e) {
      getLog().error(
          "could not bind database description class " + "'" + this.type + "'",
          e);
    }

    // buildup reference and initialize data source object
    Reference ref = new Reference(DataSource.class.getName(), 
                                  this.factory,
                                  null);
    for (Map.Entry<String, String> entry : convertToMap(this.connection).entrySet()) {
      ref.add(new StringRefAddr(entry.getKey(), entry.getValue()));
    }
    ObjectFactory of = null;
    try {
      Class<?> factClass = Class.forName(ref.getFactoryClassName());
      of = (ObjectFactory) factClass.newInstance();
    } catch (ClassNotFoundException e) {
      getLog().error(
          "could not found data source class " + "'"
              + this.factory + "'", e);
    } catch (InstantiationException e) {
      getLog().error(
          "could not initialise data source class " + "'"
              + this.factory + "'", e);
    } catch (IllegalAccessException e) {
      getLog().error(
          "could not access data source class " + "'"
              + this.factory + "'", e);
    }
    if (of != null) {
      try {
        final DataSource ds = (DataSource) of.getObjectInstance(ref, null, null, null);
        if (ds != null) {
          NamingUtil.bind(compCtx, "env/eFaps/jdbc", ds);
          initialised = initialised && true;
        }
// TODO: must be referenced by class frmo outside
NamingUtil.bind(compCtx, "env/eFaps/transactionManager", new SlideTransactionManager());
      } catch (NamingException e)  {
        getLog().error(
            "could not bind JDBC pooling class " + "'" 
                + this.factory + "'",
            e);
      } catch (Exception e) {
        getLog().error(
            "coud not get object instance of factory " + "'"
                + this.factory + "'", e);
      }
    }

    return initialised;
  }

  /**
   * Reloads the internal eFaps cache.
   */
  protected void reloadCache() throws EFapsException  {
    startTransaction();
    RunLevel.init("shell");
    RunLevel.execute();
    abortTransaction();
  }

  /**
   * 
   * 
   * @todo description
   */
  protected void startTransaction() throws EFapsException {
    Context.begin(this.userName);
  }

  /**
   * @todo description
   */
  protected void abortTransaction() throws EFapsException  {
    Context.rollback();
  }

  /**
   * @todo description
   */
  protected void commitTransaction() throws EFapsException {
    Context.commit();
  }
  

  /**
   * Separates all key / value pairs of given text string.<br/>
   * Evaluation algorithm:<br/>
   * Separates the text by all found commas (only if in front of the comma is
   * no back slash). This are the key / value pairs. A key / value pair is
   * separated by the first equal ('=') sign.
   *
   * @param _text   text string to convert to a key / value map
   * @return Map of strings with all found key / value pairs
   */
  protected Map<String, String> convertToMap(final String _text)  {
    final Map<String, String> properties = new HashMap<String, String>();

    // separated all key / value pairs
    final Pattern pattern = Pattern.compile("(([^\\\\,])|(\\\\,)|(\\\\))*");
    final Matcher matcher = pattern.matcher(_text);

    while (matcher.find())  {
      final String group = matcher.group().trim();
      if (group.length() > 0)  {
        // separated key from value
        final int index = group.indexOf('=');
        final String key = (index > 0) 
                           ? group.substring(0, index).trim()
                           : group.trim();
        final String value = (index > 0)
                             ? group.substring(index + 1).trim()
                             : "";
        properties.put(key, value);
      }
    }

    return properties;
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
