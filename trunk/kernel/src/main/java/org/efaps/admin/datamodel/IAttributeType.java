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

package org.efaps.admin.datamodel;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.efaps.db.query.CachedResult;
import org.efaps.util.EFapsException;

/**
 * @author The eFaps Team
 * @version $Id$
 */
public interface IAttributeType
{
    /**
     * The method prepares the statement for update the object in the database.
     *
     * @param _stmt string buffer to append the statement
     * @return <i>true</i> if only a preparation is needed, otherwise
     *         <i>false</i> if the value must be set
     */
    boolean prepareUpdate(final StringBuilder _stmt);

    /**
     * The method prepares the statement for insert the object in the database.
     *
     * @param _stmt string buffer to append the statement
     * @return <i>true</i> if only a preparation is needed, otherwise
     *         <i>false</i> if the value must be set
     */
    boolean prepareInsert(final StringBuilder _stmt);

    /**
     * The method updates in the statement the value.
     *
     * @param _object object
     * @param _stmt SQL statement to update the value
     * @param _index index in the SQL statement to update the value
     * @return number of indexes used in the method, if the return value is null
     *         an error should be thrown
     * @throws SQLException on error
     */
    int update(final Object _object, final PreparedStatement _stmt, final int _index) throws SQLException;

    /**
     * Method is used to read the values, retrieved from an jdbc resultset and
     * put into the given parameter <code>_objectList</code>. This is
     * necessary, because only in the different instances of this interface
     * it can be determined what to do with the objects from the database. e.g.
     * in case of DateTimeType the values will be cased into a DateTime Object
     * and in case of a PersonLinkType a Person or Role instance will be returned.
     *
     * @param _objectList list of objects from the eFaps Database
     * @return Object as needed for eFaps
     * @throws EFapsException on error
     */
    Object readValue(final List<Object> _objectList) throws EFapsException;

    /**
     * @param _rs cached result from the JDBC select statement
     * @param _indexes index in the result set
     * @throws Exception on error
     * @return Object
     */
    Object readValue(final CachedResult _rs, final List<Integer> _indexes) throws Exception;

    /**
     * This methods sets the internal value with a string coming from the user
     * interface. The string is a localised value!
     *
     * @param _values new object value to set
     */
    void set(final Object[] _values);

    /**
     * The instance method gets the attribute for this attribute type interface.
     *
     * @return attribute for this attribute value representing
     */
    Attribute getAttribute();

    /**
     * The instance method sets the field for this attribute type interface.
     *
     * @param _attribute attribute
     */
    void setAttribute(final Attribute _attribute);
}
