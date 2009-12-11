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

/**
 * Class from this package should be only used if no J2EE server is used to
 * initialize the database connection and store binding. E.g. if a shell is
 * implemented, the shell does not have a J2EE server...
 *
 * To define the database connection, three naming bindings must exists:
 * <dl>
 *   <dt>{@link org.efaps.init.INamingBinds#RESOURCE_DBTYPE}</dt>
 *   <dd>Holds the instance of the Database Type. The implementing class must
 *       be derived from {@link org.efaps.db.databases.AbstractDatabase}.</dd>
 *   <dt>{@link org.efaps.init.INamingBinds#RESOURCE_DATASOURCE}<dt>
 *   <dd>Holds the instance of the SQL data source. The class must implement
 *       interface {@link javax.sql.DataSource}.</dd>
 *   <dt>{@link org.efaps.init.INamingBinds#RESOURCE_TRANSMANAG}</dt>
 *   <dd>Holds the instance of the Transaction Manager. The class must
 *       implement interface {@link javax.transaction.TransactionManager}.</dd>
 * </dl>
 */
package org.efaps.init;

