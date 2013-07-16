/*
 * Copyright 2003 - 2013 The eFaps Team
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

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * The class is used to store information about unique keys within SQL tables.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class UniqueKeyInformation
    implements Serializable
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Name of the unique key in upper case.
     */
    private final String ukName;

    /**
     * Comma separated string of column names in upper case for which this unique
     * key is defined.
     */
    private final Map<Integer, String> columnNames = new TreeMap<Integer, String>();

    /**
     * Constructor to initialize all instance variables.
     *
     * @param _ukName   name of unique key
     */
    protected UniqueKeyInformation(final String _ukName)
    {
        this.ukName = _ukName.toUpperCase();
    }

    /**
     * Append a new name of column for which this unique key is defined.
     *
     * @param _index    index within the unique key of the column name
     * @param _colName  further column names which are defined in this unique
     *                  key
     */
    protected void appendColumnName(final int _index,
                                    final String _colName)
    {
        this.columnNames.put(_index, _colName);
    }

    /**
     * Getter method for instance variable {@link #columnNames}.
     *
     * @return value of instance variable columnNames
     * @see #columnNames
     */
    public String getColumnNames()
    {
        return this.columnNames.toString();
    }

    /**
     * Getter method for the instance variable {@link #ukName}.
     *
     * @return value of instance variable {@link #ukName}
     */
    public String getUkName()
    {
        return this.ukName;
    }

    /**
     * Returns string representation of this class instance. The information
     * includes {@link #ukName} and {@link #columnNames}.
     *
     * @return string representation of the unique key information
     */
    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }
}
