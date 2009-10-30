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

/**
 * The interface is used to define the names of the bindings.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public interface INamingBinds
{
    /**
     * The static variable holds the resource name for the JDBC database
     * connection.
     */
    String RESOURCE_DATASOURCE = "eFaps/jdbc";

    /**
     * The static variable holds the resource name for the database type.
     */
    String RESOURCE_DBTYPE = "eFaps/dbType";

    /**
     * Resource name of the transaction manager.
     */
    String RESOURCE_TRANSMANAG = "eFaps/transactionManager";

    /**
     * Resource name of the transaction manager.
     */
    String RESOURCE_TRANSMANAGTIMEOUT = "eFaps/transactionManagerTimeout";
}
