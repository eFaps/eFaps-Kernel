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

import java.util.Map;
import java.util.Map.Entry;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.naming.spi.ObjectFactory;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import org.efaps.db.databases.AbstractDatabase;
import org.efaps.util.DateTimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Initialize the database connection for an eFaps instance.
 *
 * @author tmo
 * @version $Id$
 */
public class StartupDatabaseConnection implements INamingBinds
{
  /**
   * Logging instance used to give logging information of this class.
   */
  private final static Logger LOG = LoggerFactory.getLogger(DateTimeUtil.class);

  /**
   * Initialize the database information set for given parameter values:
   * <ul>
   * <li>configure the database type</li>
   * <li>initialize the SQL datasource (JDBC connection to the database)</li>
   * <li>initialize transaction manager</li>
   * </ul>
   *
   * @param _classDBType      class name of the database type
   * @param _classDSFactory   class name of the SQL data source factory
   * @param _propConnection   map of properties for the JDBC connection
   * @param _classTM          class name of the transaction manager
   * @throws StartupException if the database connection or transaction manager
   *                          could not be initialized
   * @see #configureDBType(Context, String)
   * @see #configureDataSource(Context, String, Map)
   * @see #configureTransactionManager(Context, String)
   */
  public static void startup(final String _classDBType,
                             final String _classDSFactory,
                             final Map<String,String> _propConnection,
                             final String _classTM)
      throws StartupException
  {
    if (LOG.isInfoEnabled())  {
      LOG.info("Initialise Database Connection");
    }

    final Context compCtx;
    try {
      final InitialContext context = new InitialContext();
      compCtx = (javax.naming.Context)context.lookup ("java:comp");
    } catch (NamingException e) {
      throw new StartupException("Could not initialize JNDI", e);
    }

    configureDBType(compCtx, _classDBType);
    configureDataSource(compCtx, _classDSFactory, _propConnection);
    configureTransactionManager(compCtx, _classTM);
  }

  /**
   * The class defined with parameter _classDSFactory initialized and bind to
   * {@link #RESOURCE_DATASOURCE}. The initialized class must implement
   * interface {@link DataSource}. As JDBC connection properties the map
   * _propConneciton is used.
   *
   * @param _compCtx          Java root naming context
   * @param _classDSFactory   class name of the SQL data source factory
   * @param _propConnection   map of properties for the JDBC connection
   * @throws StartupException
   */
  protected static void configureDataSource(final Context _compCtx,
                                            final String _classDSFactory,
                                            final Map<String,String> _propConnection)
      throws StartupException
  {
    final Reference ref = new Reference(DataSource.class.getName(),
                                        _classDSFactory,
                                        null);
    for (final Entry<String, String> entry : _propConnection.entrySet()) {
      ref.add(new StringRefAddr(entry.getKey(), entry.getValue()));
    }
    ObjectFactory of = null;
    try {
      final Class<?> factClass = Class.forName(ref.getFactoryClassName());
      of = (ObjectFactory) factClass.newInstance();
    } catch (ClassNotFoundException e) {
      throw new StartupException("could not found data source class '" + _classDSFactory + "'", e);
    } catch (InstantiationException e) {
      throw new StartupException("could not initialise data source class '" + _classDSFactory + "'", e);
    } catch (IllegalAccessException e) {
      throw new StartupException("could not access data source class '" + _classDSFactory + "'", e);
    }
    if (of != null) {
      try {
        final DataSource ds = (DataSource) of.getObjectInstance(ref, null, null, null);
        if (ds != null) {
          Util.bind(_compCtx, "env/" + RESOURCE_DATASOURCE, ds);
        }
      } catch (NamingException e)  {
        throw new StartupException("could not bind JDBC pooling class '" + _classDSFactory + "'", e);
      } catch (Exception e) {
        throw new StartupException("coud not get object instance of factory '" + _classDSFactory + "'", e);
      }
    }
  }

  /**
   * The class defined with parameter _classDBType initialized and bind to
   * {@link #RESOURCE_DBTYPE}. The initialized class must be extended from
   * class {@link AbstractDatabse}.
   *
   * @param _compCtx          Java root naming context
   * @param _classDBType      class name of the database type
   * @throws StartupException if the database type class could not be
   *                          found, initialized, accessed or bind to the
   *                          context
   */
  protected static void configureDBType(final Context _compCtx,
                                        final String _classDBType)
      throws StartupException
  {
    try {
      final AbstractDatabase dbType = (AbstractDatabase)(Class.forName(_classDBType)).newInstance();
      if (dbType == null) {
        throw new StartupException("could not initaliase database type '" + _classDBType + "'");
      } else  {
        Util.bind(_compCtx, "env/" + RESOURCE_DBTYPE, dbType);
      }
    } catch (ClassNotFoundException e) {
      throw new StartupException("could not found database description class '" + _classDBType + "'", e);
    } catch (InstantiationException e) {
      throw new StartupException("could not initialise database description class '" + _classDBType + "'", e);
    } catch (IllegalAccessException e) {
      throw new StartupException("could not access database description class '" + _classDBType + "'", e);
    } catch (NamingException e) {
      throw new StartupException("could not bind database description class '" + _classDBType + "'", e);
    }
  }

  /**
   * The class defined with parameter _classTM initialized and bind to
   * {@link #RESOURCE_TRANSMANAG}. The initialized class must implement
   * interface {@link TransactionManager}.
   *
   * @param _compCtx          Java root naming context
   * @param _classTM          class name of the transaction manager
   * @throws StartupException if the transaction manager class could not be
   *                          found, initialized, accessed or bind to the
   *                          context
   */
  protected static void configureTransactionManager(final Context _compCtx,
                                                    final String _classTM)
      throws StartupException
  {
    try {
      final TransactionManager tm = (TransactionManager)(Class.forName(_classTM)).newInstance();
      if (tm == null) {
        throw new StartupException("could not initaliase database type");
      } else  {
        Util.bind(_compCtx, "env/" + RESOURCE_TRANSMANAG, tm);
      }
    } catch (ClassNotFoundException e) {
      throw new StartupException("could not found transaction manager class '" + _classTM + "'", e);
    } catch (InstantiationException e) {
      throw new StartupException("could not initialise transaction manager class '" + _classTM + "'", e);
    } catch (IllegalAccessException e) {
      throw new StartupException("could not access transaction manager class '" + _classTM + "'", e);
    } catch (NamingException e) {
      throw new StartupException("could not bind transaction manager class '" + _classTM + "'", e);
    }
  }
}
