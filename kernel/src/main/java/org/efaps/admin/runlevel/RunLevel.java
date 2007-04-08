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

package org.efaps.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.naming.spi.ObjectFactory;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.slide.transaction.SlideTransactionManager;
import org.efaps.db.Context;
import org.efaps.db.databases.AbstractDatabase;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.util.cache.Cache;

/**
 * This Class is the Runlevel for eFaps. It provides the possibilty to load only
 * the specified or needed parts into the Cache. It can be defined within the
 * database.
 * 
 * @author jmo
 * 
 */
public class RunLevel {
  /**
   * Logger for this class
   */
  private static final Log         LOG          = LogFactory
                                                    .getLog(RunLevel.class);

  /**
   * This is the sql select statement to select all RunLevel from the database.
   */
  private final static String      SQL_SELECT   = "select ID, RUNLEVEL, UUID, PARENT "
                                                    + "from T_ADRUNLEVEL ";

  private static List<CacheMethod> CACHEMETHODS = new ArrayList<CacheMethod>();

  private static String            ID           = null;

  private static String            RUNLEVEL     = null;

  private static String            UUID         = null;

  private static List<String>      PARENTS      = new ArrayList<String>();

  public static void main(String[] args) {
    new RunLevel("initDB");
  }

  /**
   * get the List of the CachedMethods
   * 
   * @return List
   */
  public static List getCacheMethods() {
    return CACHEMETHODS;

  }

  public RunLevel(String _RunLevel) {
    initDatabase();
    setRunLevel(_RunLevel);
    initialise();
    try {
      Cache.reloadCacheRunLevel();
      this.abortTransaction();
    } catch (Exception e) {

      LOG.error("RunLevel(String)", e);
    }

  }

  private void setRunLevel(String _RunLevel) {
    RUNLEVEL = _RunLevel;
  }

  private static String getSelectStmt() {

    return SQL_SELECT + " WHERE RUNLEVEL = '" + RUNLEVEL + "'";

  }

  private static String getMethodSelectStmt() {

    StringBuilder stmt = new StringBuilder();
    stmt
        .append("select CLASS, METHOD, PARAMETER from T_ADRUNLEVELDEF where RUNLEVELID  in (");
    stmt.append(getId());
    for (Iterator iter = PARENTS.iterator(); iter.hasNext();) {
      String element = (String) iter.next();
      stmt.append(",");
      stmt.append(element);
    }
    stmt.append(") order by PRIORITY");
    return stmt.toString();

  }

  private void initialise() {

    Statement stmt = null;
    String parentID = null;
    ConnectionResource con = null;
    try {
      startTransaction();

      con = Context.getThreadContext().getConnectionResource();

      stmt = con.getConnection().createStatement();

      ResultSet rs = stmt.executeQuery(getSelectStmt());
      if (rs.next()) {
        setId(rs.getString(1));
        setUUID(rs.getString(3));
        parentID = (rs.getString(4));
      } else {
        LOG.error("RunLevel not found");
      }
      rs.close();
      while (parentID != null) {
        PARENTS.add(parentID);

        rs = stmt.executeQuery("select PARENT from T_ADRUNLEVEL where ID= "
            + parentID);

        if (rs.next()) {
          parentID = rs.getString(1);
        } else {
          parentID = null;
        }

      }
      rs.close();

      rs = stmt.executeQuery(getMethodSelectStmt());
      while (rs.next()) {
        if (rs.getString(3) != null) {
          CACHEMETHODS.add(this.new CacheMethod(rs.getString(1).trim(), rs
              .getString(2).trim(), rs.getString(3).trim()));
        } else {
          CACHEMETHODS.add(this.new CacheMethod(rs.getString(1).trim(), rs
              .getString(2).trim()));
        }
      }

    } catch (EFapsException e) {

      LOG.error("initialise()", e);
    } catch (SQLException e) {

      LOG.error("initialise()", e);
    } catch (Exception e) {

      LOG.error("initialise()", e);
    }

    finally {
      if (stmt != null) {
        try {
          stmt.close();
          con.commit();
        } catch (SQLException e) {

          LOG.error("initialise()", e);
        } catch (EFapsException e) {

          LOG.error("initialise()", e);
        }
      }
    }

  }

  public static String getId() {
    return ID;
  }

  private static void setId(String _ID) {
    ID = _ID;

  }

  public static String getUUID() {
    return UUID;
  }

  public static void setUUID(String _UUID) {
    UUID = _UUID;
  }

  protected boolean initDatabase() {
    boolean initialised = false;
    String bootstrap = "/Users/janmoxter/Documents/workspace/eFaps/bootstrap.xml";

    Properties props = new Properties();
    try {
      // read bootstrap properties
      FileInputStream fstr = new FileInputStream(bootstrap);
      props.loadFromXML(fstr);
      fstr.close();
    } catch (FileNotFoundException e) {
      LOG.error("could not open file '" + bootstrap + "'", e);
    } catch (IOException e) {
      LOG.error("could not read file '" + bootstrap + "'", e);
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

  protected void startTransaction() throws EFapsException, Exception {
    getTransactionManager().begin();
    Context.newThreadContext(getTransactionManager().getTransaction(), "Admin");
  }

  protected TransactionManager getTransactionManager() {
    return transactionManager;
  }

  final public static TransactionManager transactionManager = new SlideTransactionManager();

  protected void abortTransaction() throws EFapsException, Exception {
    getTransactionManager().rollback();
    Context.getThreadContext().close();
  }

  /**
   * Cache for the Methods, wich are defined for the Runlevel. The stored
   * String-Values can be used to invoke the Methods. Therefor the Cache is
   * seperated in three Fields: <br>
   * <li> CLASSNAME: Name of the Class as written in a java Class </li>
   * <li> METHODNAME: Name of the a static Method, it can optional used with on
   * String-Parameter</li>
   * <li><i>optional PARAMETER: the String-Value coresponding with the Method</i></li>
   * <br>
   * 
   * @author jmo
   * 
   */
  public class CacheMethod {

    private String CLASSNAME  = null;

    private String METHODNAME = null;

    private String PARAMETER  = null;

    /**
     * Constructor for the ChacheMethod in the Case that there are only
     * ClassName an MethodName
     * 
     * @see CacheMethod(String _ClassName, String _MethodName, String
     *      _Parameter)
     * @param _ClassName
     *          Name of the Clasee
     * @param _MethodName
     *          Name of the Method
     */
    public CacheMethod(String _ClassName, String _MethodName) {
      CLASSNAME = _ClassName;

      METHODNAME = _MethodName;

    }

    /**
     * Constructor for the Cache with ClassName, MethodName and Parameter
     * 
     * @see CacheMethod(String _ClassName, String _MethodName)
     * @param _ClassName
     *          Name of the Class
     * @param _MethodName
     *          Name of the Method
     * @param _Parameter
     *          Value of the Parameter
     */
    public CacheMethod(String _ClassName, String _MethodName, String _Parameter) {
      CLASSNAME = _ClassName;

      METHODNAME = _MethodName;

      PARAMETER = _Parameter;
    }

    /**
     * get the Name of the Class
     * 
     * @return Name of the Class
     */
    public String getClassName() {
      return CLASSNAME;
    }

    /**
     * get the Name of the Method
     * 
     * @return Name of the Method
     */
    public String getMethodName() {
      return METHODNAME;
    }

    /**
     * get the value of a String Parameter
     * 
     * @return Value of the Parameter, null if not initialised
     */
    public String getParameter() {
      return PARAMETER;
    }

    /**
     * has the Method a Parameter
     * 
     * @return true if a Parameter is given, else false
     */
    public boolean hasParameter() {
      if (PARAMETER != null) {
        return true;
      }

      return false;

    }

  }

}
