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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.efaps.db.Context;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.Cache;

/**
 * This Class is the Runlevel for eFaps. It provides the possibilty to load only
 * the specified or needed parts into the Cache. It can be defined within the
 * database.
 *
 * @author jmox
 * @author tmo
 * @version $Id$
 */
public class RunLevel {

  /**
   * Logger for this class
   */
  private static final Logger LOG = LoggerFactory.getLogger(RunLevel.class);

  /**
   * Name of SQL table used to test if the runlevel is already installed in the
   * database.
   *
   * @see #isInitialisable
   */
  private final static String TABLE_TESTS = "T_RUNLEVEL";

  /**
   * This is the SQL select statement to select all RunLevel from the database.
   */
  private final static String SQL_RUNLEVEL  = "select ID,PARENT "
                                              + "from T_RUNLEVEL "
                                              + "WHERE ";

  private final static String SQL_DEF_PRE   = "select CLASS, METHOD, PARAMETER "
                                              + "from T_RUNLEVELDEF "
                                              + "where RUNLEVELID=";

  private final static String SQL_DEF_POST  = " order by PRIORITY";

  private static RunLevel     RUNLEVEL      = null;

  private final static Map<Long, RunLevel> ALL_RUNLEVELS = new HashMap<Long, RunLevel>();

  /**
   * All cache initialise methods for this runlevel are stored in this instance
   * variable. They are ordered by the priority.
   */
  private final List<CacheMethod>                cacheMethods  = new ArrayList<CacheMethod>();

  /**
   * The id in the eFaps database of this runlevel.
   */
  private long                             id            = 0;

  private RunLevel                         parent        = null;

  /**
   * The static method first removes all values in the caches. Then the cache is
   * initialized automatically depending on the desired RunLevel
   *
   * @param _runLevel   name of run level to initialise
   * @todo exception handling
   */
  public static void init(final String _runLevel) throws EFapsException {
    ALL_RUNLEVELS.clear();
    RUNLEVEL = new RunLevel(_runLevel);
  }

  /**
   * Tests, if the SQL table {@link #TABLE_TESTS} exists (= <i>true</i>). This
   * means the run level could be initialized.
   *
   * @return <i>true</i> if a run level is initialisable (and the SQL table
   *         exists in the database); otherwise <i>false</i>
   * @throws EFapsException if the test for the table fails
   * @see #TABLE_TESTS
   */
  public static boolean isInitialisable() throws EFapsException  {
    try {
      return Context.getDbType().existsTable(Context.getThreadContext().getConnection(),
                                             TABLE_TESTS);
    } catch (final SQLException e) {
      throw new EFapsException(RunLevel.class,
                               "isInitialisable.SQLException", e);
    }
  }

  private void clearCache() {
    Cache.clearCaches();
  }

  public static void execute() throws EFapsException {
    RUNLEVEL.clearCache();
    RUNLEVEL.executeMethods();
  }

  private RunLevel(final String _name) throws EFapsException {
    initialise(SQL_RUNLEVEL + " RUNLEVEL='" + _name + "'");
  }

  private RunLevel(final long _id) throws EFapsException {
    initialise(SQL_RUNLEVEL + " ID=" + _id);
  }

  /**
   * All cache initialise methods stored in {@link #cacheMethods} are called.
   *
   * @see #cacheMethods
   */
  protected void executeMethods() throws EFapsException {
    if (this.parent != null) {
      this.parent.executeMethods();
    }
    for (final CacheMethod cacheMethod : this.cacheMethods) {
      cacheMethod.callMethod();
    }
  }

  /**
   * Reads the id and the parent id of this runlevel. All defined methods for
   * this runlevel are loaded. If a parent id is defined, the parent is
   * initialised.
   *
   * @param _sql
   *          sql statement to get the id and parent id for this runlevel
   * @see #parent
   * @see #cacheMethods
   */
  protected void initialise(final String _sql) throws EFapsException {

    ConnectionResource con = null;
    try {

      con = Context.getThreadContext().getConnectionResource();

      Statement stmt = null;

      long parentId = 0;

      try {
        stmt = con.getConnection().createStatement();

        // read runlevel itself
        ResultSet rs = stmt.executeQuery(_sql);
        if (rs.next()) {
          this.id = rs.getLong(1);
          parentId = rs.getLong(2);
        } else {
          LOG.error("RunLevel not found");
        }
        rs.close();

        // read all methods for one runlevel
        rs = stmt.executeQuery(SQL_DEF_PRE + this.id + SQL_DEF_POST);
        while (rs.next()) {
          if (rs.getString(3) != null) {
            this.cacheMethods.add(new CacheMethod(rs.getString(1).trim(), rs
                .getString(2).trim(), rs.getString(3).trim()));
          } else {
            this.cacheMethods.add(new CacheMethod(rs.getString(1).trim(), rs
                .getString(2).trim()));
          }
        }

      }
      finally {
        if (stmt != null) {
          stmt.close();
        }
      }

      con.commit();

      ALL_RUNLEVELS.put(this.id, this);

      if (parentId != 0) {
        this.parent = ALL_RUNLEVELS.get(parentId);

        if (this.parent == null) {
          this.parent = new RunLevel(parentId);
        }
      }

    } catch (final EFapsException e) {
      LOG.error("initialise()", e);
    } catch (final SQLException e) {
      LOG.error("initialise()", e);
    }
    finally {
      if ((con != null) && con.isOpened()) {
        con.abort();
      }
    }
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

    /**
     * Name of the class which must be initiliased.
     */
    final private String className;

    /**
     * Name of the static method used to initiliase the cache.
     */
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
    public CacheMethod(final String _className, final String _methodName) {
      this(_className, _methodName, null);
    }

    /**
     * Constructor for the Cache with ClassName, MethodName and Parameter
     *
     * @see CacheMethod(String _ClassName, String _MethodName)
     * @param _className
     *          Name of the Class
     * @param _methodName
     *          Name of the Method
     * @param _parameter
     *          Value of the Parameter
     */
    public CacheMethod(final String _className, final String _methodName,
        final String _parameter) {
      this.className = _className;
      this.methodName = _methodName;
      this.parameter = _parameter;
    }

    /**
     * Calls the static cache initialise method defined by this instance.
     */
    public void callMethod() throws EFapsException {
      try {
        final Class<?> cls = Class.forName(this.className);
        if (this.parameter != null) {
          final Method m = cls.getMethod(this.methodName, String.class);
          m.invoke(cls, this.parameter);
        } else {
          final Method m = cls.getMethod(this.methodName, new Class[] {});
          m.invoke(cls);
        }
      } catch (final ClassNotFoundException e) {
        LOG.error("class '" + this.className + "' not found", e);
        throw new EFapsException(getClass(),
            "callMethod.ClassNotFoundException", null, e, this.className);
      } catch (final NoSuchMethodException e) {
        LOG.error("class '" + this.className + "' does not own method '"
            + this.methodName + "'", e);
        throw new EFapsException(getClass(),
            "callMethod.NoSuchMethodException", null, e, this.className,
            this.methodName);
      } catch (final IllegalAccessException e) {
        LOG.error("could not access class '" + this.className + "' method '"
            + this.methodName + "'", e);
        throw new EFapsException(getClass(),
            "callMethod.IllegalAccessException", null, e, this.className,
            this.methodName);
      } catch (final InvocationTargetException e) {
        LOG.error("could not execute class '" + this.className + "' method '"
            + this.methodName + "' because an exception was thrown.", e);
        if (e.getCause() != null) {
          throw new EFapsException(getClass(),
              "callMethod.InvocationTargetException", null, e.getCause(),
              this.className, this.methodName);
        } else {
          throw new EFapsException(getClass(),
              "callMethod.InvocationTargetException", null, e, this.className,
              this.methodName);
        }
      }
    }
  }

}
