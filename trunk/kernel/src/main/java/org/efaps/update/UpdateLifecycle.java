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

package org.efaps.update;

/**
 * All steps used within update life cycle.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public enum UpdateLifecycle
{
    /**
     * SQL tables are created.
     */
    SQL_CREATE_TABLE,

    /**
     * The ID of the SQL tables are correct defined (auto increment or as
     * foreign key to another SQL table).
     */
    SQL_UPDATE_ID,

    /**
     * SQL tables are updated (with columns, foreign keys etc.).
     */
    SQL_UPDATE_TABLE,

    /**
     * Embedded SQL scripts are executed.
     */
    SQL_RUN_SCRIPT,

    /**
     * eFaps data model is created.
     */
    EFAPS_CREATE,

    /**
     * eFaps data model is updated.
     */
    EFAPS_UPDATE;
}
