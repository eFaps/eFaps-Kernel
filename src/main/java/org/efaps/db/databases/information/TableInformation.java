/*
 * Copyright 2003 - 2012 The eFaps Team
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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.efaps.db.databases.AbstractDatabase;
import org.efaps.util.cache.CacheObjectInterface;

/**
 * Class used to return information about a SQL table.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class TableInformation
    implements CacheObjectInterface
{
    /**
     * Stores the map between a column name and the column information itself.
     *
     * @see #getColInfo(String)
     * @see #addColInfo(String, Set, int, int, boolean)
     */
    private final Map<String, ColumnInformation> colMap = new HashMap<String, ColumnInformation>();

    /**
     * Stores the map between a unique key name and the unique key information
     * itself.
     *
     * @see #getUKInfo(String)
     * @see #addUniqueKeyColumn(String, String)
     */
    private final Map<String, UniqueKeyInformation> ukMap = new HashMap<String, UniqueKeyInformation>();

    /**
     * Stores the map between comma separated string of column names of a unique
     * key and the unique key itself.
     *
     * @see #getUKInfoByColNames(String)
     * @see #addUniqueKeyColumn(String, String)
     */
    private final Map<String, UniqueKeyInformation> ukColMap = new HashMap<String, UniqueKeyInformation>();

    /**
     * Stores the map between a name of a foreign key and the information about
     * the foreign key itself. The name of all foreign keys are in upper case.
     *
     * @see #getFKInfo(String)
     * @see #addForeignKey(String, String, String, String, boolean)
     */
    private final Map<String, ForeignKeyInformation> fkMap = new HashMap<String, ForeignKeyInformation>();

    /**
     * SQL table name.
     *
     * @see #TableInformation(String)
     * @see #getName()
     */
    private final String name;

    /**
     * Initialize this table information instance for given
     * <code>_tableName</code>.
     *
     * @param _tableName    name of SQL table for which the information
     *                      is searched
     */
    public TableInformation(final String _tableName)
    {
        this.name = _tableName.toUpperCase();
    }

    /**
     * Appends the column information for given values for column
     * <code>_colName</code>.
     *
     * @param _colName      name of the column
     * @param _colTypes     eFaps types of the column
     * @param _size         size (for character)
     * @param _scale        scale (for number)
     * @param _isNullable   is the column nullable?
     * @see #colMap
     */
    public void addColInfo(final String _colName,
                              final Set<AbstractDatabase.ColumnType> _colTypes,
                              final int _size,
                              final int _scale,
                              final boolean _isNullable)
    {
        this.colMap.put(_colName.toUpperCase(),
                        new ColumnInformation(_colName, _colTypes, _size, _scale, _isNullable));
    }

    /**
     * Adds an unique key information to this table information.
     *
     * @param _ukName   name of unique key
     * @param _colIndex index of the column for given unique key
     * @param _colName  name of the column for given unique key
     * @see #ukMap
     * @see #ukColMap
     */
    public void addUniqueKeyColumn(final String _ukName,
                                   final int _colIndex,
                                   final String _colName)
    {
        final String ukName = _ukName.toUpperCase();
        final UniqueKeyInformation uki;
        if (!this.ukMap.containsKey(ukName))  {
            uki = new UniqueKeyInformation(_ukName);
            this.ukMap.put(ukName, uki);
        } else  {
            uki = this.ukMap.get(ukName);
        }
        uki.appendColumnName(_colIndex, _colName);

//        this.ukColMap.put(uki.getColumnNames(), uki);
    }

    /**
     * Fetches all foreign keys for this table.
     *
     * @param _fkName       name of foreign key
     * @param _colName      name of column name
     * @param _refTableName name of referenced SQL table
     * @param _refColName   name of column within referenced SQL table
     * @param _cascade      delete cascade activated
     * @see #fkMap
     */
    public void addForeignKey(final String _fkName,
                              final String _colName,
                              final String _refTableName,
                              final String _refColName,
                              final boolean _cascade)
    {
        this.fkMap.put(_fkName.toUpperCase(),
                       new ForeignKeyInformation(_fkName, _colName, _refTableName, _refColName, _cascade));
    }

    /**
     * <p>Returns for given column name (of the SQL table) the column
     * information. If the column does not exists in the table, a
     * <code>null</code> is returned.</p>
     * <p>The name of given column is searched independently of upper and / or
     * lower case.</p>
     *
     * @param _columnName   name of column for which the column information is
     *                      searched
     * @return column information for given column name; or <code>null</code>
     *         if column does not exists in the table
     * @see #colMap
     */
    public ColumnInformation getColInfo(final String _columnName)
    {
        return (_columnName != null)
               ? this.colMap.get(_columnName.toUpperCase())
               : null;
    }

    /**
     * <p>Returns for given name of unique key (of the SQL table) the
     * information about the unique key, or if the given name of unique key is
     * not defined, a <code>null</code> is returned.</p>
     * <p>The name of the given unique key is searched independently of upper
     * and / or lower case.</p>
     *
     * @param _ukName   name of unique key which is searched
     * @return unique key information
     * @see #ukMap
     */
    public UniqueKeyInformation getUKInfo(final String _ukName)
    {
        return (_ukName != null)
               ? this.ukMap.get(_ukName.toUpperCase())
               : null;
    }

    /**
     * <p>Returns for given name of unique key <code>_ukColName</code> (of the
     * SQL table) the information about the unique key, or if the given name of
     * unique key is not defined, a <code>null</code> is returned.</p>
     * <p>The name of the given unique key is searched independently of upper
     * and / or lower case.</p>
     *
     * @param _ukColName  name of column names for which the unique key is
     *                    searched
     * @return unique key information
     * @see #ukColMap
     */
    public UniqueKeyInformation getUKInfoByColNames(final String _ukColName)
    {
        return (_ukColName != null)
               ? this.ukColMap.get(_ukColName.toUpperCase())
               : null;
    }

    /**
     * <p>Returns for given name of foreign key <code>_fkName</code> (of the
     * SQL table) the information about the foreign key, or if the given name
     * of foreign key is not defined, a <code>null</code> is returned.</p>
     * <p>The name of the given foreign key is searched independently of upper
     * and / or lower case.</p>
     *
     * @param _fkName   name of foreign key which is searched
     * @return foreign key information
     * @see #fkMap
     */
    public ForeignKeyInformation getFKInfo(final String _fkName)
    {
        return (_fkName != null)
               ? this.fkMap.get(_fkName.toUpperCase())
               : null;
    }

    /**
     * Returns the string representation of this class instance. The
     * information includes {@link #name}, {@link #colMap}, {@link #ukMap} and
     * {@link #fkMap}.
     *
     * @return string representation of this table information
     */
    @Override
    public String toString()
    {
        return new ToStringBuilder(this)
            .append("name", this.name)
            .append("columns", this.colMap.values())
            .append("unique keys", this.ukMap.values())
            .append("foreign keys", this.fkMap.values())
            .toString();
    }

    /**
     * Returns the {@link #name table name}.
     *
     * @return table name
     * @see #name
     */
    @Override
    public String getName()
    {
        return this.name;
    }

    /**
     * An id does not exists for the table information and so an error will
     * be always thrown if this method is called. The method is only required
     * to implement interface {@link CacheObjectInterface},
     * @return nothing because an error will be always thrown
     */
    @Override
    public long getId()
    {
        throw new Error("id does not exists for the table information");
    }

    /**
     * An UUID does not exists for the table information and so an error will
     * be always thrown if this method is called. The method is only required
     * to implement interface {@link CacheObjectInterface},
     *
     * @return nothing because an error will be always thrown
     */
    @Override
    public UUID getUUID()
    {
        throw new Error("UUID does not exists for the table information");
    }
}
