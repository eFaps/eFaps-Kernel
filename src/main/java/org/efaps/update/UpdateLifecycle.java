/*
 * Copyright 2003 - 2011 The eFaps Team
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
    SQL_CREATE_TABLE(1),

    /**
     * The ID of the SQL tables are correct defined (auto increment or as
     * foreign key to another SQL table).
     */
    SQL_UPDATE_ID(2),

    /**
     * SQL tables are updated (with columns, foreign keys etc.).
     */
    SQL_UPDATE_TABLE(3),

    /**
     * Embedded SQL scripts are executed.
     */
    SQL_RUN_SCRIPT(4),

    /**
     * The Types for the Status Groups are created.
     */
    STATUSGROUP_CREATE(5),

    /**
     * The Status Groups are updated (e.g. Parent-Child relation).
     */
    STATUSGROUP_UPDATE(6),

    /**
     * The Stati for the Status Groups are created.
     */
    STATUS_CREATE(7),

    /**
     * eFaps data model is created.
     */
    EFAPS_CREATE(8),

    /**
     * eFaps data model is updated.
     */
    EFAPS_UPDATE(9),

    /**
     * DBProperties are updated.
     */
    DBPROPERTIES_UPDATE(10);

    /**
     * Number representing the order of this UpdateLifecycle.
     */
    private final int order;

    /**
     * @param _order value for the order
     */
    UpdateLifecycle(final int _order)
    {
        this.order = _order;
    }

    /**
     * Getter method for instance variable {@link #order}.
     *
     * @return value of instance variable {@link #order}
     */
    public Integer getOrder()
    {
        return this.order;
    }
}
