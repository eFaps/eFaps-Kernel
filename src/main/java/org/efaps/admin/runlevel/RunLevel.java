/*
 * Copyright © 2003 - 2024 The eFaps Team (-)
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
 */
package org.efaps.admin.runlevel;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.efaps.admin.common.Quartz;
import org.efaps.admin.program.esjp.Listener;
import org.efaps.db.Context;
import org.efaps.db.wrapper.SQLPart;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.AbstractCache;
import org.efaps.util.cache.CacheReloadException;
import org.efaps.util.cache.InfinispanCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Class is the run level for eFaps. It provides the possibility to load
 * only the specified or needed parts into the Cache. It can be defined within
 * the database.
 *
 * @author The eFaps Team
 *
 */
public final class RunLevel
{
    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(RunLevel.class);

    /**
     * Name of SQL table used to test if the run level is already installed in
     * the database.
     *
     * @see #isInitialisable()
     */
    private static final String TABLE_TESTS = "T_RUNLEVEL";

    /**
     * This is the SQL select statement to select a RunLevel from the database.
     *
     * @see #RunLevel(long)
     * @see #RunLevel(String)
     */
    private static final SQLSelect SELECT_RUNLEVEL = new SQLSelect()
                                                            .column(0, "ID")
                                                            .column(0, "PARENT")
                                                            .from("T_RUNLEVEL", 0);

    /**
     * SQL select statement to select a RunLevel from the database.
     *
     * @see #initialize(String)
     */
    private static final SQLSelect SELECT_DEF_PRE = new SQLSelect()
                                                            .column(0, "CLASS")
                                                            .column(0, "METHOD")
                                                            .column(0, "PARAMETER")
                                                            .from("T_RUNLEVELDEF", 0);

    /**
     * Current RunLevel.
     */
    private static RunLevel RUNLEVEL = null;

    /**
     * Mapping of all RunLevels to id.
     */
    private static final Map<Long, RunLevel> ALL_RUNLEVELS = new HashMap<>();

    /**
     * All cache initialize methods for this RunLevel are stored in this instance
     * variable. They are ordered by the priority.
     */
    private final List<CacheMethod> cacheMethods = new ArrayList<>();

    /**
     * The id in the eFaps database of this run level.
     */
    private long id = 0;

    /**
     * The parent run level.
     */
    private RunLevel parent;

    /**
     * Name of this RunLevel.
     */
    private String name;

    /**
     * Initializes this run level instance depending on the <code>_name</code>.
     *
     * @param _name   name of the run level
     * @throws EFapsException on error
     */
    private RunLevel(final String _name)
        throws EFapsException
    {
        name = _name;
        initialize(RunLevel.SELECT_RUNLEVEL.getCopy()
                        .addPart(SQLPart.WHERE)
                        .addColumnPart(0, "RUNLEVEL")
                        .addPart(SQLPart.EQUAL).addEscapedValuePart(_name).getSQL());
    }

    /**
     * Initializes this run level instance depending on the <code>_id</code>.
     *
     * @param _id       id of the run level
     * @throws EFapsException on error
     */
    private RunLevel(final long _id)
        throws EFapsException
    {
        initialize(RunLevel.SELECT_RUNLEVEL.getCopy().addPart(SQLPart.WHERE).addColumnPart(0, "ID")
                        .addPart(SQLPart.EQUAL).addValuePart(_id).getSQL());
    }

    /**
     * The static method first removes all values in the caches. Then the cache
     * is initialized automatically depending on the desired RunLevel
     *
     * @param _runLevel   name of run level to initialize
     * @throws EFapsException on error
     */
    public static void init(final String _runLevel)
        throws EFapsException
    {
        RunLevel.ALL_RUNLEVELS.clear();
        RunLevel.RUNLEVEL = new RunLevel(_runLevel);
    }

    /**
     * Stops all RunLevel.
     */
    public static void stop()
    {
        InfinispanCache.stop();
        Quartz.shutDown();
    }

    /**
     * @return Return the name of the currenct active RunLevel.
     */
    public static String getName4Current()
    {
        return RunLevel.RUNLEVEL == null ? null : RunLevel.RUNLEVEL.name;
    }

    /**
     * Tests, if the SQL table {@link #TABLE_TESTS} exists (= <i>true</i>).
     * This means the run level could be initialized.
     *
     * @return <i>true</i> if a run level could be initialized (and the SQL
     *         table exists in the database); otherwise <i>false</i>
     * @throws EFapsException if the test for the table fails
     * @see #TABLE_TESTS
     */
    public static boolean isInitialisable()
        throws EFapsException
    {
        try {
            final Connection con = Context.getConnection();
            final boolean ret = Context.getDbType().existsTable(Context.getConnection(), RunLevel.TABLE_TESTS);
            con.close();
            return ret;
        } catch (final SQLException e) {
            throw new EFapsException(RunLevel.class, "isInitialisable.SQLException", e);
        }
    }

    /**
     * Execute the current RunLevel. (Load all defined Caches).
     *
     * @throws EFapsException on error
     */
    public static void execute()
        throws EFapsException
    {
        RunLevel.RUNLEVEL.executeMethods();
        final List<String> allInitializer = RunLevel.RUNLEVEL.getAllInitializers();
        for (final AbstractCache<?> cache : AbstractCache.getCaches()) {
            final String initiliazer = cache.getInitializer();
            if (!allInitializer.contains(initiliazer)) {
                cache.clear();
            }
        }
        for (final IRunLevelListener listener : Listener.get().<IRunLevelListener>invoke(IRunLevelListener.class)) {
            listener.onExecute(RunLevel.RUNLEVEL.name);
        }
    }

    /**
     * Returns the list of all initializers.
     *
     * @return list of all initializers
     */
    private List<String> getAllInitializers()
    {
        final List<String> ret = new ArrayList<>();
        for (final CacheMethod cacheMethod : cacheMethods) {
            ret.add(cacheMethod.className);
        }
        if (parent != null) {
            ret.addAll(parent.getAllInitializers());
        }
        return ret;
    }

    /**
     * All cache initialize methods stored in {@link #cacheMethods} are called.
     *
     * @see #cacheMethods
     * @throws EFapsException on error
     */
    protected void executeMethods()
        throws EFapsException
    {
        if (parent != null) {
            parent.executeMethods();
        }
        for (final CacheMethod cacheMethod : cacheMethods) {
            cacheMethod.callMethod();
        }
    }

    /**
     * Reads the id and the parent id of this RunLevel. All defined methods for
     * this run level are loaded. If a parent id is defined, the parent is
     * initialized.
     *
     * @param _sql  SQL statement to get the id and parent id for this run
     *              level
     * @see #parent
     * @see #cacheMethods
     * @throws EFapsException on error
     */
    protected void initialize(final String _sql)
        throws EFapsException
    {
        Connection con = null;
        try {
            con = Context.getConnection();
            Statement stmt = null;
            long parentId = 0;

            try {
                stmt = con.createStatement();
                // read run level itself
                ResultSet rs = stmt.executeQuery(_sql);
                if (rs.next()) {
                    id = rs.getLong(1);
                    parentId = rs.getLong(2);
                } else {
                    RunLevel.LOG.warn("RunLevel not found");
                }
                rs.close();

                // read all methods for one run level
                rs = stmt.executeQuery(RunLevel.SELECT_DEF_PRE.getCopy()
                                .addPart(SQLPart.WHERE)
                                .addColumnPart(0, "RUNLEVELID")
                                .addPart(SQLPart.EQUAL)
                                .addValuePart(id)
                                .addPart(SQLPart.ORDERBY)
                                .addColumnPart(0, "PRIORITY").getSQL());

                /**
                 * Order part of the SQL select statement.
                 */
                //private static final String SQL_DEF_POST  = " order by PRIORITY";

                while (rs.next()) {
                    if (rs.getString(3) != null) {
                        cacheMethods.add(new CacheMethod(rs.getString(1).trim(),
                                                              rs.getString(2).trim(),
                                                              rs.getString(3).trim()));
                    } else {
                        cacheMethods.add(new CacheMethod(rs.getString(1).trim(),
                                                              rs.getString(2).trim()));
                    }
                }
                rs.close();
            } finally {
                if (stmt != null) {
                    stmt.close();
                }
            }
            con.commit();
            con.close();
            RunLevel.ALL_RUNLEVELS.put(id, this);
            if (parentId != 0) {
                parent = RunLevel.ALL_RUNLEVELS.get(parentId);
                if (parent == null) {
                    parent = new RunLevel(parentId);
                }
            }
        } catch (final EFapsException e) {
            RunLevel.LOG.error("initialise()", e);
        } catch (final SQLException e) {
            RunLevel.LOG.error("initialise()", e);
        } finally {
            try {
                if (con != null && !con.isClosed()) {
                    con.close();
                }
            } catch (final SQLException e) {
                throw new CacheReloadException("Cannot read a type for an attribute.", e);
            }
        }
    }

    /**
     * Cache for the methods, which are defined for the run level. The stored
     * string values can be used to invoke the methods. Therefore the cache is
     * separated in three fields:
     * <ul>
     * <li><b>CLASSNAME</b>: name of the class as written in a java class </li>
     * <li><b>METHODNAME</b>: name of the a static method, it can optional used
     *     with one string parameter</li>
     * <li><b>PARAMETER</b> <i>(optional)</i>: the string value corresponding
     *     with the method</li>
     * </ul>
     */
    public static final class CacheMethod
    {
        /**
         * Name of the class which must be initialized.
         */
        private final String className;

        /**
         * Name of the static method used to initialized the cache.
         */
        private final String methodName;

        /**
         * Parameter for the static method used to initialized the cache.
         */
        private final String parameter;

        /**
         *  Constructor for the {@link CacheMethod} in the case that there are only
         * {@link #className} and {@link #methodName}.
         *
         * @param _className   name of the class
         * @param _methodName  name of the method
         * @see #CacheMethod(String,String,String)
         */
        private CacheMethod(final String _className,
                            final String _methodName)
        {
            this(_className, _methodName, null);
        }

        /**
         * Constructor for the cache with {@link #className},
         * {@link #methodName} and {@link #parameter}.
         *
         * @param _className    Name of the Class
         * @param _methodName   Name of the Method
         * @param _parameter    Value of the Parameter
         */
        private CacheMethod(final String _className,
                            final String _methodName,
                            final String _parameter)
        {
            className = _className;
            methodName = _methodName;
            parameter = _parameter;
        }

        /**
         * Calls the static cache initialize method defined by this instance.
         *
         * @throws EFapsException on error
         */
        public void callMethod()
            throws EFapsException
        {
            try {
                final Class<?> cls = Class.forName(className);
                if (parameter != null) {
                    final Method m = cls.getMethod(methodName, String.class);
                    m.invoke(cls, parameter);
                } else {
                    final Method m = cls.getMethod(methodName, new Class[] {});
                    m.invoke(cls);
                }
            } catch (final ClassNotFoundException e) {
                RunLevel.LOG.error("class '{}' not found", className);
            } catch (final NoSuchMethodException e) {
                RunLevel.LOG.error("class '" + className + "' does not own method '" + methodName + "'", e);
                throw new EFapsException(getClass(),
                                         "callMethod.NoSuchMethodException",
                                         null,
                                         e,
                                         className,
                                         methodName);
            } catch (final IllegalAccessException e) {
                RunLevel.LOG.error("could not access class '" + className + "' method '"
                        + methodName + "'", e);
                throw new EFapsException(getClass(),
                                         "callMethod.IllegalAccessException",
                                         null,
                                         e,
                                         className,
                                         methodName);
            } catch (final InvocationTargetException e) {
                RunLevel.LOG.error("could not execute class '" + className + "' method '"
                        + methodName + "' because an exception was thrown.", e);
                if (e.getCause() != null) {
                    if (e.getCause() instanceof EFapsException) {
                        throw (EFapsException) e.getCause();
                    } else {
                        throw new EFapsException(getClass(), "callMethod.InvocationTargetException",
                                             null,
                                             e.getCause(),
                                             className,
                                             methodName);
                    }
                } else {
                    throw new EFapsException(getClass(),
                                             "callMethod.InvocationTargetException",
                                             null,
                                             e,
                                             className,
                                             methodName);
                }
            }
        }
    }
}
