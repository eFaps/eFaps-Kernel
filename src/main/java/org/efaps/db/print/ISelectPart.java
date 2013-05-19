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

package org.efaps.db.print;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.efaps.admin.datamodel.Type;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.util.EFapsException;

/**
 * Interface used for the different Select parts.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public interface ISelectPart
{
    /**
     * Method to join a table to the given from select statement.
     *
     * @param _oneselect    oneselect this select part must be joined to
     * @param _select       SQL select statement wrapper
     * @param _relIndex     relation index
     * @return table index of the joint table
     * @throws EFapsException on error
     */
    int join(final OneSelect _oneselect,
             final SQLSelect _select,
             final int _relIndex)
        throws EFapsException;

    /**
     * Method to get the Type the part belongs to.
     * @return type
     */
    Type getType();

    /**
     * Add an Object.
     * @param _rs ResultSet
     * @throws SQLException on error
     */
    void addObject(final ResultSet _rs)
        throws SQLException;

    /**
     * Get the Object that was added using {@link #addObject(ResultSet)}.
     * @return Object
     */
    Object getObject();


    /**
     * Add something to the where part of the SQL select.
     * @param _oneselect    oneselect this select part must be joined to
     * @param _select       SQL select statement wrapper
     */
    void add2Where(final OneSelect _oneselect,
                   final SQLSelect _select);
}
