/*
 * Copyright 2003 - 2007 The eFaps Team
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

package org.efaps.importer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.naming.spi.ObjectFactory;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import org.apache.commons.logging.Log;
import org.apache.slide.transaction.SlideTransactionManager;
import org.efaps.admin.runlevel.RunLevel;
import org.efaps.db.Context;
import org.efaps.db.databases.AbstractDatabase;
import org.efaps.util.EFapsException;

import org.efaps.util.cache.Cache;

/**
 * This class is going to be replaced with a version inside eFaps
 * 
 * @author jmo
 * 
 */
public class AbstractTransaction {

  // ///////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Theoretically all efaps contexts object instances must include a
   * transaction manager.
   */
  final public static TransactionManager transactionManager = new SlideTransactionManager();

  // ///////////////////////////////////////////////////////////////////////////
  // instance variables

  /**
   * The apache maven logger is stored in this instance variable.
   * 
   * @see #getLog
   * @see #setLog
   */
  private Log                            LOG                = null;

  /**
   * Stores the name of the logged in user.
   * 
   * @see #login
   */
  private String                         userName;

  private static String                  BOOTSTRAP;

  protected void loadRunLevel() {
    initDatabase();
    try {
      startTransaction();
    } catch (EFapsException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    reloadCache();
    try {
      abortTransaction();
    } catch (EFapsException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  protected boolean initDatabase() {
    boolean initialised = false;

    Properties props = new Properties();
    try {
      // read bootstrap properties
      FileInputStream fstr = new FileInputStream(getBootstrap());
      props.loadFromXML(fstr);
      fstr.close();

    } catch (FileNotFoundException e) {
      LOG.error("could not open file '" + getBootstrap() + "'", e);
    } catch (IOException e) {
      LOG.error("could not read file '" + getBootstrap() + "'", e);
    }

    // configure database type
    String dbClass = null;
    try {
      Object dbTypeObj = props.get("dbType");

      if ((dbTypeObj == null) || (dbTypeObj.toString().length() == 0)) {
        LOG.error("could not initaliase database type");
      } else {
        dbClass = dbTypeObj.toString();

        AbstractDatabase dbType = (AbstractDatabase) Class.forName(dbClass)
            .newInstance();

        if (dbType == null) {
          LOG.error("could not initaliase database type");
        }

        Context.setDbType(dbType);
        initialised = true;
      }
    } catch (ClassNotFoundException e) {
      LOG.error("could not found database description class " + "'" + dbClass
          + "'", e);
    } catch (InstantiationException e) {
      LOG.error("could not initialise database description class " + "'"
          + dbClass + "'", e);
    } catch (IllegalAccessException e) {
      LOG.error("could not access database description class " + "'" + dbClass
          + "'", e);
    }

    // buildup reference and initialise datasource object
    String factory = props.get("factory").toString();
    Reference ref = new Reference(DataSource.class.getName(), factory, null);
    for (Object key : props.keySet()) {
      Object value = props.get(key);
      ref.add(new StringRefAddr(key.toString(), (value == null) ? null : value
          .toString()));
    }
    ObjectFactory of = null;
    try {
      Class factClass = Class.forName(ref.getFactoryClassName());
      of = (ObjectFactory) factClass.newInstance();
    } catch (ClassNotFoundException e) {
      LOG.error("could not found data source class " + "'"
          + ref.getFactoryClassName() + "'", e);
    } catch (InstantiationException e) {
      LOG.error("could not initialise data source class " + "'"
          + ref.getFactoryClassName() + "'", e);
    } catch (IllegalAccessException e) {
      LOG.error("could not access data source class " + "'"
          + ref.getFactoryClassName() + "'", e);
    }
    if (of != null) {
      DataSource ds = null;
      try {
        ds = (DataSource) of.getObjectInstance(ref, null, null, null);
      } catch (Exception e) {
        LOG.error("coud not get object instance of factory " + "'"
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
   * Initiliase the database information read from the bootstrap file:
   * <ul>
   * <li>read boostrap properties from bootstrap file</li>
   * <li>configure the database type</li>
   * <li>initiliase the sql datasource (JDBC connection to the database)</li>
   * </ul>
   */

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
  protected void reloadCache() {

    try {
      RunLevel.init("shell");
      RunLevel.execute();

    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

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

  // ///////////////////////////////////////////////////////////////////////////
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
    this.LOG = _log;
  }

  /**
   * This is the getter method for instance variable {@link #log}.
   * 
   * @return value of instance variable {@link #log}
   * @see #log
   * @see #setLog
   */
  public Log getLog() {
    return this.LOG;
  }

  public void setBootstrap(String _Bootstrap) {
    BOOTSTRAP = _Bootstrap;
  }

  public String getBootstrap() {
    return BOOTSTRAP;
  }
}
