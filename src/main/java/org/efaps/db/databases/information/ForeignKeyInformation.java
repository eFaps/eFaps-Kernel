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

package org.efaps.db.databases.information;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * The class is used to store information about foreign keys within SQL tables.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class ForeignKeyInformation
{
    /**
     * Name of the foreign key.
     */
    private final String fkName;

    /**
     * Column name which references the foreign key.
     */
    private final String colName;

    /**
     * Name of the referenced SQL table.
     */
    private final String refTableName;

    /**
     * Name of column in the referenced SQL table.
     */
    private final String refColName;

    /**
     * Is the foreign key a cascade delete, means that if the foreign row is
     * deleted, this row referencing the foreign row must also be deleted?
     */
    private final boolean cascadeDelete;

    /**
     * Constructor to initialize all instance variables.
     *
     * @param _foreignKeyName       name of foreign key
     * @param _columnName           name of column name
     * @param _referencedTableName  name of referenced SQL table
     * @param _referencedColumnName name of column within referenced SQL table
     * @param _cascadeDelete        delete cascade activated
     */
    protected ForeignKeyInformation(final String _foreignKeyName,
                                    final String _columnName,
                                    final String _referencedTableName,
                                    final String _referencedColumnName,
                                    final boolean _cascadeDelete)
    {
        this.fkName = _foreignKeyName.toUpperCase();
        this.colName = _columnName.toUpperCase();
        this.refTableName = _referencedTableName.toUpperCase();
        this.refColName = _referencedColumnName.toUpperCase();
        this.cascadeDelete = _cascadeDelete;
    }

    /**
     * Returns string representation of this class instance. The information
     * includes {@link #fkName}, {@link #colName}, {@link #refTableName},
     * {@link #refColName} and {@link #cascadeDelete}.
     *
     * @return string representation for the foreign key information
     */
    @Override()
    public String toString()
    {
        return new ToStringBuilder(this)
            .append("foreign key name", this.fkName)
            .append("column name", this.colName)
            .append("referenced table name", this.refTableName)
            .append("referenced column name", this.refColName)
            .append("cascade delete", this.cascadeDelete)
            .toString();
    }
}
