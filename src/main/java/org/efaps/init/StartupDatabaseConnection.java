/*
 * Copyright 2003 - 2016 The eFaps Team
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
 */

package org.efaps.init;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.naming.spi.ObjectFactory;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;

import org.apache.commons.lang3.StringUtils;
import org.efaps.db.databases.AbstractDatabase;
import org.efaps.db.transaction.DelegatingUserTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Initialize the database connection for an eFaps instance.
 *
 * @author The eFaps Team
 *
 */
public final class StartupDatabaseConnection
    implements INamingBinds
{

    /**
     * Logging instance used to give logging information of this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(StartupDatabaseConnection.class);

    /**
     * Name of the environment variable to define the bootstrap path.
     */
    private static final String ENV_PATH = "EFAPS_BOOTSTRAP_PATH";

    /**
     * Name of the environment variable to define the name of the bootstrap file.
     */
    private static final String ENV_FILE = "EFAPS_BOOTSTRAP_FILE";

    /**
     * Name of the property for the database type class.
     */
    private static final String PROP_DBTYPE_CLASS = "org.efaps.db.type";

    /**
     * Name of the property for the database factory class.
     */
    private static final String PROP_DBFACTORY_CLASS = "org.efaps.db.factory";

    /**
     * Name of the property for the database connection.
     */
    private static final String PROP_DBCONNECTION = "org.efaps.db.connection";

    /**
     * Name of the property for the transaction manager class.
     */
    private static final String PROP_TM_CLASS = "org.efaps.transaction.manager";

    /**
     * Name of the property for the timeout of the transaction manager.
     */
    private static final String PROP_CONFIGPROP = "org.efaps.configuration.properties";

    /**
     * Name of the default bootstrap path in the user home directory.
     */
    private static final String DEFAULT_BOOTSTRAP_PATH = ".efaps/bootstrap";

    /**
     * Name of the default bootstrap file.
     */
    private static final String DEFAULT_BOOTSTRAP_FILE = "default.efaps.xml";

    /**
     * Constructor is hidden to prevent instantiation.
     */
    private StartupDatabaseConnection()
    {
    }

    /**
     * Startups the eFaps kernel with the bootstrap configuration defined as shell variable. If the bootstrap
     * configuration is not defined as shell variables, the default bootstrap definition is used.
     *
     * @throws StartupException if startup failed
     * @see #startup(String, String)
     */
    public static void startup()
        throws StartupException
    {
        StartupDatabaseConnection.startup(null, null);
    }

    /**
     * Startups the eFaps kernel with the bootstrap path defined as shell variable (or if not defined the default
     * bootstrap path is used).
     *
     * @param _bootstrapFile name of the bootstrap file (without file extension); <code>null</code> means the the name
     *            of the bootstrap is not predefined
     * @throws StartupException if startup failed
     * @see #startup(String, String)
     */
    public static void startup(final String _bootstrapFile)
        throws StartupException
    {
        StartupDatabaseConnection.startup(null, _bootstrapFile);
    }

    /**
     * <p>
     * Startups he kernel depending on a bootstrap definition.
     * </p>
     *
     * <p>
     * Following rules applies for the bootstrap path:
     * <ul>
     * <li>if <code>_bootstrapPath</code> is not <code>null</code>, <code>_bootstrapPath</code> is used as bootstrap
     * path</li>
     * <li>if in the system environment the shell variable {@link #ENV_PATH} is defined, this value of this shell
     * variable is used</li>
     * <li>in all other cases {@link #DEFAULT_BOOTSTRAP_PATH} is used</li>
     * </ul>
     * </p>
     *
     * <p>
     * Following rules applies for the bootstrap name:
     * <ul>
     * <li>if <code>_bootstrapFile</code> is not <code>null</code>, <code>_bootstrapFile</code> is used as bootstrap
     * name</li>
     * <li>if in the system environment the shell variable {@link #ENV_FILE} is defined, this value of this shell
     * variable is used</li>
     * <li>in all other cases {@link #DEFAULT_BOOTSTRAP_FILE} is used</li>
     * </ul>
     * </p>
     *
     * <p>
     * After the used bootstrap file is identified, this file is opened and the keys are read and evaluated.
     * </p>
     *
     * @param _bootstrapPath path where the bootstrap files are located; <code>null</code> means that the path is not
     *            predefined
     * @param _bootstrapFile name of the bootstrap file (without file extension); <code>null</code> means the the name
     *            of the bootstrap is not predefined
     * @throws StartupException if startup failed
     */
    public static void startup(final String _bootstrapPath,
                               final String _bootstrapFile)
        throws StartupException
    {
        // evaluate bootstrap path
        final File bsPath;
        if (_bootstrapPath != null) {
            bsPath = new File(_bootstrapPath);
        } else {
            final String envPath = System.getenv(StartupDatabaseConnection.ENV_PATH);
            if (envPath != null) {
                bsPath = new File(envPath);
            } else {
                bsPath = new File(System.getProperty("user.home"), StartupDatabaseConnection.DEFAULT_BOOTSTRAP_PATH);
            }
        }
        // evaluate bootstrap file
        final String bsFile;
        File bootstrap = null;
        if (_bootstrapFile != null) {
            bsFile = _bootstrapFile;
        } else {
            final String envFile = System.getenv(StartupDatabaseConnection.ENV_FILE);
            if (envFile != null) {
                bsFile = envFile;
            } else {
                bsFile = StartupDatabaseConnection.DEFAULT_BOOTSTRAP_FILE;
            }
        }
        bootstrap = new File(bsFile);
        if (bootstrap == null || !bootstrap.exists()) {
            bootstrap = new File(bsPath, bsFile);
        }

        // read bootstrap file
        final Properties props = new Properties();
        try {
            props.loadFromXML(new FileInputStream(bootstrap));
        } catch (final FileNotFoundException e) {
            throw new StartupException("bootstrap file " + bootstrap + " not found", e);
        } catch (final IOException e) {
            throw new StartupException("bootstrap file " + bootstrap + " could not be read", e);
        }

        // and startup
        final Map<String, String> eFapsProps;
        if (props.containsKey(StartupDatabaseConnection.PROP_CONFIGPROP)) {
            eFapsProps = StartupDatabaseConnection.convertToMap(props
                            .getProperty(StartupDatabaseConnection.PROP_CONFIGPROP));
        } else {
            eFapsProps = new HashMap<>();
        }
        StartupDatabaseConnection.startup(props.getProperty(StartupDatabaseConnection.PROP_DBTYPE_CLASS),
                        props.getProperty(StartupDatabaseConnection.PROP_DBFACTORY_CLASS),
                        props.getProperty(StartupDatabaseConnection.PROP_DBCONNECTION),
                        props.getProperty(StartupDatabaseConnection.PROP_TM_CLASS),
                        null,
                        eFapsProps);
    }

    /**
     * Initialize the database information set for given parameter values.
     *
     * @param _classDBType class name of the database type
     * @param _classDSFactory class name of the SQL data source factory
     * @param _propConnection string with properties for the JDBC connection
     * @param _classTM class name of the transaction manager
     * @param _classTSR class name of TransactionSynchronizationRegistry
     * @param _eFapsProps tring with properties
     * @throws StartupException if the database connection or transaction manager could not be initialized
     * @see StartupDatabaseConnection#startup(String, String, Map, String, Integer)
     * @see StartupDatabaseConnection#convertToMap(String)
     */
    public static void startup(final String _classDBType,
                               final String _classDSFactory,
                               final String _propConnection,
                               final String _classTM,
                               final String _classTSR,
                               final String _eFapsProps)
        throws StartupException
    {
        StartupDatabaseConnection.startup(_classDBType,
                        _classDSFactory,
                        StartupDatabaseConnection.convertToMap(_propConnection),
                        _classTM,
                        _classTSR,
                        StartupDatabaseConnection.convertToMap(_eFapsProps));
    }

    /**
     * Initialize the database information set for given parameter values.
     *
     * @param _classDBType class name of the database type
     * @param _classDSFactory class name of the SQL data source factory
     * @param _propConnection string with properties for the JDBC connection
     * @param _classTM class name of the transaction manager
     * @param _classTSR class name of TransactionSynchronizationRegistry
     * @param _eFapsProps Map or properties
     * @throws StartupException if the database connection or transaction manager could not be initialized
     * @see StartupDatabaseConnection#startup(String, String, Map, String, Integer)
     * @see StartupDatabaseConnection#convertToMap(String)
     */
    public static void startup(final String _classDBType,
                               final String _classDSFactory,
                               final String _propConnection,
                               final String _classTM,
                               final String _classTSR,
                               final Map<String, String> _eFapsProps)
        throws StartupException
    {
        StartupDatabaseConnection.startup(_classDBType,
                        _classDSFactory,
                        StartupDatabaseConnection.convertToMap(_propConnection),
                        _classTM,
                        _classTSR,
                        _eFapsProps);
    }

    /**
     * Initialize the database information set for given parameter values.
     * <ul>
     * <li>configure the database type</li>
     * <li>initialize the SQL data source (JDBC connection to the database)</li>
     * <li>initialize transaction manager</li>
     * </ul>
     *
     * @param _classDBType class name of the database type
     * @param _classDSFactory class name of the SQL data source factory
     * @param _propConnection map of properties for the JDBC connection
     * @param _classTM class name of the transaction manager
     * @param _classTSR class name of TransactionSynchronizationRegistry
     * @param _eFapsProps Map or properties
     * @throws StartupException if the database connection or transaction manager could not be initialized
     * @see #configureDBType(Context, String)
     * @see #configureDataSource(Context, String, Map)
     * @see #configureTransactionManager(Context, String, Integer)
     */
    public static void startup(final String _classDBType,
                               final String _classDSFactory,
                               final Map<String, String> _propConnection,
                               final String _classTM,
                               final String _classTSR,
                               final Map<String, String> _eFapsProps)
        throws StartupException
    {
        if (StartupDatabaseConnection.LOG.isInfoEnabled()) {
            StartupDatabaseConnection.LOG.info("Initialise Database Connection");
        }

        final Context compCtx;
        try {
            final InitialContext context = new InitialContext();
            compCtx = (javax.naming.Context) context.lookup("java:/comp");

            StartupDatabaseConnection.configureEFapsProperties(compCtx, _eFapsProps);
            StartupDatabaseConnection.configureDBType(compCtx, _classDBType);
            StartupDatabaseConnection.configureDataSource(compCtx, _classDSFactory, _propConnection);
            StartupDatabaseConnection.configureTransactionManager(compCtx, _classTM);
            StartupDatabaseConnection.configureTransactionSynchronizationRegistry(compCtx, _classTSR);
        } catch (final NamingException e) {
            throw new StartupException("Could not initialize JNDI", e);
        }
        // and reset eFaps context (to be sure..)
        org.efaps.db.Context.reset();
    }

    /**
     * Add the eFaps Properties to the JNDI binding.
     * @param _compCtx      Java root naming context
     * @param _eFapsProps   Properties to bind
     * @throws StartupException on error
     */
    protected static void configureEFapsProperties(final Context _compCtx,
                                                   final Map<String, String> _eFapsProps)
        throws StartupException
    {
        try {
            Util.bind(_compCtx, "env/" + INamingBinds.RESOURCE_CONFIGPROPERTIES, _eFapsProps);
        } catch (final NamingException e) {
            throw new StartupException("could not bind eFaps Properties '" + _eFapsProps + "'", e);
            // CHECKSTYLE:OFF
        } catch (final Exception e) {
            // CHECKSTYLE:ON
            throw new StartupException("could not bind eFaps Properties '" + _eFapsProps + "'", e);
        }
    }

    /**
     * The class defined with parameter <code>_classDSFactory</code> initialized and bind to
     * {@link #RESOURCE_DATASOURCE}. The initialized class must implement interface {@link DataSource}. As JDBC
     * connection properties the map <code>_propConneciton</code> is used.
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
        try {
            Util.bind(_compCtx, "env/" + INamingBinds.RESOURCE_DATASOURCE, ref);
            Util.bind(_compCtx, "test", ref);
        } catch (final NamingException e) {
            throw new StartupException("could not bind JDBC pooling class '" + _classDSFactory + "'", e);
            // CHECKSTYLE:OFF
        } catch (final Exception e) {
            // CHECKSTYLE:ON
            throw new StartupException("coud not get object instance of factory '" + _classDSFactory + "'", e);
        }
    }

    /**
     * The class defined with parameter _classDBType initialized and bind to {@link #RESOURCE_DBTYPE}. The initialized
     * class must be extended from class {@link AbstractDatabase}.
     *
     * @param _compCtx Java root naming context
     * @param _classDBType class name of the database type
     * @throws StartupException if the database type class could not be found, initialized, accessed or bind to the
     *             context
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
                Util.bind(_compCtx, "env/" + INamingBinds.RESOURCE_DBTYPE, dbType);
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
     * The class defined with parameter _classTM initialized and bind to {@link #RESOURCE_TRANSMANAG}. The initialized
     * class must implement interface {@link TransactionManager}.
     *
     * @param _compCtx Java root naming context
     * @param _classTM class name of the transaction manager
     * @throws StartupException if the transaction manager class could not be found, initialized, accessed or bind to
     *             the context
     */
    protected static void configureTransactionManager(final Context _compCtx,
                                                      final String _classTM)
        throws StartupException
    {
        try {
            final Object tm = (Class.forName(_classTM)).newInstance();
            if (tm == null) {
                throw new StartupException("could not initialise TransactionManager ");
            } else {
                if (tm instanceof TransactionManager) {
                    Util.bind(_compCtx, "env/" + INamingBinds.RESOURCE_TRANSMANAG, tm);
                    Util.bind(_compCtx, "env/" + INamingBinds.RESOURCE_USERTRANSACTION,
                                    new DelegatingUserTransaction((TransactionManager) tm));
                } else {
                    throw new StartupException("could not initialise TransactionManager with object:" + tm);
                }
            }
        } catch (final ClassNotFoundException e) {
            throw new StartupException("could not find transaction manager class '" + _classTM + "'", e);
        } catch (final InstantiationException e) {
            throw new StartupException("could not initialise transaction manager class '" + _classTM + "'", e);
        } catch (final IllegalAccessException e) {
            throw new StartupException("could not access transaction manager class '" + _classTM + "'", e);
        } catch (final NamingException e) {
            throw new StartupException("could not bind transaction manager class '" + _classTM + "'", e);
        }
    }


    /**
     * The class defined with parameter _classTM initialized and bind to {@link #RESOURCE_TRANSSYNREG}. The initialized
     * class must implement interface {@link TransactionSynchronizationRegistry}.
     *
     * @param _compCtx Java root naming context
     * @param _classTSR class name of the transaction SynchronizationRegistry
     * @throws StartupException if the transaction manager class could not be found, initialized, accessed or bind to
     *             the context
     */
    protected static void configureTransactionSynchronizationRegistry(final Context _compCtx,
                                                                      final String _classTSR)
        throws StartupException
    {
        try {

            final Object clzz = (Class.forName(_classTSR)).newInstance();
            if (clzz == null) {
                throw new StartupException("could not initaliase database type");
            } else {
                if (clzz instanceof TransactionSynchronizationRegistry) {
                    Util.bind(_compCtx, "env/" + INamingBinds.RESOURCE_TRANSSYNREG, clzz);
                    Util.bind(_compCtx, "TransactionSynchronizationRegistry", clzz);
                } else if (clzz instanceof ObjectFactory) {
                    final Reference ref = new Reference(TransactionSynchronizationRegistry.class.getName(), clzz
                                    .getClass().getName(), null);
                    Util.bind(_compCtx, "env/" + INamingBinds.RESOURCE_TRANSSYNREG, ref);
                    Util.bind(_compCtx, "TransactionSynchronizationRegistry", ref);
                }
            }

        } catch (final ClassNotFoundException e) {
            throw new StartupException("could not found TransactionSynchronizationRegistry '" + _classTSR + "'", e);
        } catch (final InstantiationException e) {
            throw new StartupException("could not initialise TransactionSynchronizationRegistry class '" + _classTSR
                            + "'", e);
        } catch (final IllegalAccessException e) {
            throw new StartupException("could not access TransactionSynchronizationRegistry class '" + _classTSR + "'",
                            e);
        } catch (final NamingException e) {
            throw new StartupException("could not bind Transaction Synchronization Registry class '"
                            + _classTSR + "'", e);
        }
    }

    /**
     * <p>
     * Separates all key / value pairs of given text string.
     * </p>
     * <p>
     * <b>Evaluation algorithm:</b><br/>
     * Separates the text by all found commas (only if in front of the comma is no back slash). This are the key / value
     * pairs. A key / value pair is separated by the first equal ('=') sign.
     * </p>
     *
     * @param _text text string to convert to a key / value map
     * @return Map of strings with all found key / value pairs
     */
    public static Map<String, String> convertToMap(final String _text)
    {
        final Map<String, String> properties = new HashMap<>();
        if (StringUtils.isNotEmpty(_text)) {
            // separated all key / value pairs
            final Pattern pattern = Pattern.compile("(([^\\\\,])|(\\\\,)|(\\\\))*");
            final Matcher matcher = pattern.matcher(_text);

            while (matcher.find()) {
                final String group = matcher.group().trim();
                if (group.length() > 0) {
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
        }
        return properties;
    }

    /**
     * Shutdowns the connection to the database.
     *
     * @throws StartupException if shutdown failed
     */
    public static void shutdown()
        throws StartupException
    {
        final Context compCtx;
        try {
            final InitialContext context = new InitialContext();
            compCtx = (javax.naming.Context) context.lookup("java:comp");
        } catch (final NamingException e) {
            throw new StartupException("Could not initialize JNDI", e);
        }
        try {
            Util.unbind(compCtx, "env/" + INamingBinds.RESOURCE_DATASOURCE);
            Util.unbind(compCtx, "env/" + INamingBinds.RESOURCE_DBTYPE);
            Util.unbind(compCtx, "env/" + INamingBinds.RESOURCE_CONFIGPROPERTIES);
            Util.unbind(compCtx, "env/" + INamingBinds.RESOURCE_TRANSMANAG);
        } catch (final NamingException e) {
            throw new StartupException("unbind of the database connection failed", e);
        }
    }
}
