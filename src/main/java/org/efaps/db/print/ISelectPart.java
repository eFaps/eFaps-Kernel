/*
 * Copyright 2003 - 2017 The eFaps Team
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
 *
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
    int join(OneSelect _oneselect,
             SQLSelect _select,
             int _relIndex)
        throws EFapsException;

    /**
     * Method to get the Type the part belongs to.
     *
     * @return type
     * @throws EFapsException the e faps exception
     */
    Type getType()
        throws EFapsException;

    /**
     * Add an Object.
     * @param _row values
     * @throws SQLException on error
     */
    void addObject(Object[] _row)
        throws SQLException;

    /**
     * Get the Object that was added using {@link #addObject(ResultSet)}.
     * @return Object
     */
    Object getObject();

    /**
     * Add something to the where part of the SQL select.
     *
     * @param _oneselect    oneselect this select part must be joined to
     * @param _select       SQL select statement wrapper
     * @throws EFapsException on error
     */
    void add2Where(OneSelect _oneselect,
                   SQLSelect _select)
        throws EFapsException;

    /**
     * Move the SelectPart one step forward.
     * @throws EFapsException on error
     */
    void next()
        throws EFapsException;
}
