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

package org.efaps.admin.runlevel;

import java.lang.reflect.Method;
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
import org.efaps.db.Context;
import org.efaps.db.databases.AbstractDatabase;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.util.cache.Cache;
import org.efaps.util.EFapsException;

/**
 * This Class is the Runlevel for eFaps. It provides the possibilty to load only
 * the specified or needed parts into the Cache. It can be defined within the
 * database.
 * 
 * @author jmo
 */
public class RunLevel {
  /**
   * Logger for this class
   */
  private static final Log LOG = LogFactory.getLog(RunLevel.class);

  /**
   * This is the sql select statement to select all RunLevel from the database.
   */
  private final static String      SQL_SELECT   = "select ID, RUNLEVEL, UUID, PARENT "
                                                    + "from T_RUNLEVEL ";

  private static List<CacheMethod> CACHEMETHODS = new ArrayList<CacheMethod>();

  private static String            ID           = null;

  private static String            RUNLEVEL     = null;

  private static String            UUID         = null;

  private static List<String>      PARENTS      = new ArrayList<String>();

  /**
   * The static method first removes all values in the caches. Then the cache is
   * initialised automatically debending on the desired RunLevel
   *
   * @param _runLevel   name of run level to initialise
   * @todo exception handling
   */
  public static void init(final String _runLevel) throws Exception {
    new RunLevel(_runLevel);
    Cache.cleanCache();

    for (CacheMethod method : CACHEMETHODS)  {
        Class<?> cls = Class.forName(method.getClassName());
        if (method.hasParameter()) {
          Method m = cls.getMethod(method.getMethodName(),
              new Class[] { String.class });
          m.invoke(cls, (String) method.getParameter());
        } else {

          Method m = cls.getMethod(method.getMethodName(), new Class[] {});
          m.invoke(cls, (Object[]) null);

        }
    }
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
    setRunLevel(_RunLevel);
    initialise();
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
        .append("select CLASS, METHOD, PARAMETER from T_RUNLEVELDEF where RUNLEVELID  in (");
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

        rs = stmt.executeQuery("select PARENT from T_RUNLEVEL where ID= "
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
    } finally {
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

  /**
   * Cache for the Methods, wich are defined for the Runlevel. The stored
   * String-Values can be used to invoke the Methods. Therefor the Cache is
   * seperated in three Fields: <br>
   * <li> CLASSNAME: Name of the Class as written in a java Class </li>
   * <li> METHODNAME: Name of the a static Method, it can optional used with on
   * String-Parameter</li>
   * <li><i>optional PARAMETER: the String-Value coresponding with the Method</i></li>
   * <br>
   */
  public class CacheMethod {

    final private String className;

    final private String methodName;

    final private String parameter;

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
    public CacheMethod(final String _className,
                       final String _methodName) {
      this(_className, _methodName, null);
    }

    /**
     * Constructor for the Cache with ClassName, MethodName and Parameter
     * 
     * @see CacheMethod(String _ClassName, String _MethodName)
     * @param _className    Name of the Class
     * @param _methodName   Name of the Method
     * @param _parameter    Value of the Parameter
     */
    public CacheMethod(final String _className,
                       final String _methodName,
                       final String _parameter) {
      this.className = _className;
      this.methodName = _methodName;
      this.parameter = _parameter;
    }

    /**
     * get the Name of the Class
     * 
     * @return Name of the Class
     */
    public String getClassName() {
      return this.className;
    }

    /**
     * get the Name of the Method
     * 
     * @return Name of the Method
     */
    public String getMethodName() {
      return this.methodName;
    }

    /**
     * get the value of a String Parameter
     * 
     * @return Value of the Parameter, null if not initialised
     */
    public String getParameter() {
      return this.parameter;
    }

    /**
     * has the Method a Parameter
     * 
     * @return true if a Parameter is given, else false
     */
    public boolean hasParameter() {
      return this.parameter != null;
    }

  }

}
