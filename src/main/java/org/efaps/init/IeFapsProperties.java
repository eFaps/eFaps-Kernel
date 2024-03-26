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
package org.efaps.init;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 *
 */
public interface IeFapsProperties
{
    /**
     * Key to the Transaction Timeout value.
     */
    String TRANSACTIONTIMEOUT = "org.efaps.transaction.timeout";

    /**
     * Key to the DB Schema Pattern used in Database connection.
     */
    String DBSCHEMAPATTERN = "org.efaps.db.schemaPattern";

    /**
     *  Key to the DB Catalog used in Database connection.
     */
    String DBCATALOG = "org.efaps.db.catalog";
}
