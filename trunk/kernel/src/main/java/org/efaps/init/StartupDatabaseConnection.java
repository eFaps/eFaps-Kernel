/*
 * Copyright 2003 - 2009 The eFaps Team
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

package org.efaps.init;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.naming.spi.ObjectFactory;
import javax.sql.DataSource;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.efaps.db.databases.AbstractDatabase;
import org.efaps.util.DateTimeUtil;

/**
 * Initialize the database connection for an eFaps instance.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public final class StartupDatabaseConnection
    implements INamingBinds
{
    /**
     * Logging instance used to give logging information of this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(DateTimeUtil.class);

    /**
     * Constructor is hidden to prevent instantiation.
     */
    private StartupDatabaseConnection()
    {
    }

    /**
     * Initialize the database information set for given parameter values.
     *
     * @param _classDBType      class name of the database type
     * @param _classDSFactory   class name of the SQL data source factory
     * @param _propConnection   string with properties for the JDBC connection
     * @param _classTM          class name of the transaction manager
     * @param _timeout          timeout for the transaction manager in seconds,
     *                          if null the default from the transaction manager
     *                          will be used
     * @throws StartupException if the database connection or transaction
     *             manager could not be initialized
     * @see #startup(String, String, Map, String)
     * @see #convertToMap(String)
     */
    public static void startup(final String _classDBType,
                               final String _classDSFactory,
                               final String _propConnection,
                               final String _classTM,
                               final Integer _timeout)
        throws StartupException
    {
        StartupDatabaseConnection.startup(_classDBType,
                                          _classDSFactory,
                                          StartupDatabaseConnection.convertToMap(_propConnection),
                                          _classTM,
                                          _timeout);
    }

    /**
     * Initialize the database information set for given parameter values.
     * <ul>
     * <li>configure the database type</li>
     * <li>initialize the SQL data source (JDBC connection to the database)
     *     </li>
     * <li>initialize transaction manager</li>
     * </ul>
     *
     * @param _classDBType      class name of the database type
     * @param _classDSFactory   class name of the SQL data source factory
     * @param _propConnection   map of properties for the JDBC connection
     * @param _classTM          class name of the transaction manager
     * @param _timeout          timeout for the transaction manager in seconds,
     *                          if null the default from the transaction manager
     *                          will be used
     * @throws StartupException if the database connection or transaction
     *                          manager could not be initialized
     * @see #configureDBType(Context, String)
     * @see #configureDataSource(Context, String, Map)
     * @see #configureTransactionManager(Context, String)
     */
    public static void startup(final String _classDBType,
                               final String _classDSFactory,
                               final Map<String, String> _propConnection,
                               final String _classTM,
                               final Integer _timeout)
        throws StartupException
    {
        if (StartupDatabaseConnection.LOG.isInfoEnabled()) {
            StartupDatabaseConnection.LOG.info("Initialise Database Connection");
        }

        final Context compCtx;
        try {
            final InitialContext context = new InitialContext();
            compCtx = (javax.naming.Context) context.lookup("java:comp");
        } catch (final NamingException e) {
            throw new StartupException("Could not initialize JNDI", e);
        }

        configureDBType(compCtx, _classDBType);
        configureDataSource(compCtx, _classDSFactory, _propConnection);
        configureTransactionManager(compCtx, _classTM, _timeout);
    }

    /**
     * The class defined with parameter _classDSFactory initialized and bind to
     * {@link #RESOURCE_DATASOURCE}. The initialized class must implement
     * interface {@link DataSource}. As JDBC connection properties the map
     * _propConneciton is used.
     *
     * @param _compCtx Java root naming context
     * @param _classDSFactory class name of the SQL data source factory
     * @param _propConnection map of properties for the JDBC connection
     * @throws StartupException on error
     */
    protected static void configureDataSource(final Context _compCtx,
                                              final String _classDSFactory,
                                              final Map<String, String> _propConnection)
        throws StartupException
    {
        final Reference ref = new Reference(DataSource.class.getName(), _classDSFactory, null);
        for (final Entry<String, String> entry : _propConnection.entrySet()) {
            ref.add(new StringRefAddr(entry.getKey(), entry.getValue()));
        }
        ObjectFactory of = null;
        try {
            final Class<?> factClass = Class.forName(ref.getFactoryClassName());
            of = (ObjectFactory) factClass.newInstance();
        } catch (final ClassNotFoundException e) {
            throw new StartupException("could not found data source class '" + _classDSFactory + "'", e);
        } catch (final InstantiationException e) {
            throw new StartupException("could not initialise data source class '" + _classDSFactory + "'", e);
        } catch (final IllegalAccessException e) {
            throw new StartupException("could not access data source class '" + _classDSFactory + "'", e);
        }
        if (of != null) {
            try {
                final DataSource ds = (DataSource) of.getObjectInstance(ref, null, null, null);
                if (ds != null) {
                    Util.bind(_compCtx, "env/" + RESOURCE_DATASOURCE, ds);
                }
            } catch (final NamingException e) {
                throw new StartupException("could not bind JDBC pooling class '" + _classDSFactory + "'", e);
            } catch (final Exception e) {
                throw new StartupException("coud not get object instance of factory '" + _classDSFactory + "'", e);
            }
        }
    }

    /**
     * The class defined with parameter _classDBType initialized and bind to
     * {@link #RESOURCE_DBTYPE}. The initialized class must be extended from
     * class {@link AbstractDatabse}.
     *
     * @param _compCtx      Java root naming context
     * @param _classDBType  class name of the database type
     * @throws StartupException if the database type class could not be found,
     *             initialized, accessed or bind to the context
     */
    protected static void configureDBType(final Context _compCtx,
                                          final String _classDBType)
        throws StartupException
    {
        try {
            final AbstractDatabase<?> dbType = (AbstractDatabase<?>) (Class.forName(_classDBType)).newInstance();
            if (dbType == null) {
                throw new StartupException("could not initaliase database type '" + _classDBType + "'");
            } else {
                Util.bind(_compCtx, "env/" + RESOURCE_DBTYPE, dbType);
            }
        } catch (final ClassNotFoundException e) {
            throw new StartupException("could not found database description class '" + _classDBType + "'", e);
        } catch (final InstantiationException e) {
            throw new StartupException("could not initialise database description class '" + _classDBType + "'", e);
        } catch (final IllegalAccessException e) {
            throw new StartupException("could not access database description class '" + _classDBType + "'", e);
        } catch (final NamingException e) {
            throw new StartupException("could not bind database description class '" + _classDBType + "'", e);
        }
    }

    /**
     * The class defined with parameter _classTM initialized and bind to
     * {@link #RESOURCE_TRANSMANAG}. The initialized class must implement
     * interface {@link TransactionManager}.
     *
     * @param _compCtx  Java root naming context
     * @param _classTM  class name of the transaction manager
     * @param _timeout  timeout for the transaction manager in seconds, if null the
     *                  default from the transaction manager will be used
     * @throws StartupException if the transaction manager class could not be
     *             found, initialized, accessed or bind to the context
     */
    protected static void configureTransactionManager(final Context _compCtx,
                                                      final String _classTM,
                                                      final Integer _timeout)
        throws StartupException
    {
        try {
            final TransactionManager tm = (TransactionManager) (Class.forName(_classTM)).newInstance();
            if (tm == null) {
                throw new StartupException("could not initaliase database type");
            } else {
                if (_timeout != null) {
                    tm.setTransactionTimeout(900);
                }
                Util.bind(_compCtx, "env/" + RESOURCE_TRANSMANAG, tm);
            }
        } catch (final ClassNotFoundException e) {
            throw new StartupException("could not found transaction manager class '" + _classTM + "'", e);
        } catch (final InstantiationException e) {
            throw new StartupException("could not initialise transaction manager class '" + _classTM + "'", e);
        } catch (final IllegalAccessException e) {
            throw new StartupException("could not access transaction manager class '" + _classTM + "'", e);
        } catch (final NamingException e) {
            throw new StartupException("could not bind transaction manager class '" + _classTM + "'", e);
        } catch (final SystemException e) {
            throw new StartupException("could not set transaction timeout for class '" + _classTM + "'", e);
        }
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
    protected static Map<String, String> convertToMap(final String _text)
    {
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
}
