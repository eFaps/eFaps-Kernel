/*
 * Copyright 2003 - 2019 The eFaps Team
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

package org.efaps.admin.datamodel.attributetype;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.db.Instance;
import org.efaps.db.wrapper.SQLInsert;
import org.efaps.db.wrapper.SQLUpdate;
import org.efaps.util.EFapsException;

/**
 * Classes which implements this interface are used to handle the mapping of
 * attributes between eFaps and the database.
 *
 * @author The eFaps Team
 *
 */
public interface IAttributeType
    extends Serializable
{
    /**
     * The method prepares the statement for update the object in the database.
     *
     * @param _update       SQL update statement
     * @param _attribute    attribute which is updated
     * @param _values       new object value to set; values are localized and
     *                      are coming from the user interface
     * @throws SQLException if preparation for the update failed
     */
    void prepareUpdate(SQLUpdate _update,
                       Attribute _attribute,
                       Object... _values)
        throws SQLException;

    /**
     * The method prepares the statement for insert the object in the database.
     *
     * @param _insert       SQL insert statement
     * @param _attribute    attribute which is inserted
     * @param _values       new object value to set; values are localized and
     *                      are coming from the user interface
     * @throws SQLException if preparation for the insert failed
     */
    void prepareInsert(SQLInsert _insert,
                       Attribute _attribute,
                       Object... _values)
        throws SQLException;

    /**
     * Method is used to read the values, retrieved from an JDBC result set and
     * put into the given parameter <code>_objectList</code>. This is
     * necessary, because only in the different instances of this interface
     * it can be determined what to do with the objects from the database. e.g.
     * in case of DateTimeType the values will be cased into a DateTime Object
     * and in case of a PersonLinkType a Person or Role instance will be
     * returned. This method is called from the {@link org.efaps.db.PrintQuery}.
     *
     * @param _attribute    related attribute which is read
     * @param _objectList   list of objects from the eFaps Database
     * @return Object as needed for eFaps
     * @throws EFapsException on error
     */
    Object readValue(Attribute _attribute,
                     List<Object> _objectList)
        throws EFapsException;

    /**
     * Method is called when the value of the related attribute is used
     * as argument in a where clause for a query. This is necessary because
     * for the different attribute types the string representation must be
     * generated different, e.g  a datetime must be converted in an iso string
     * etc.
     *
     * @param _value    value to be returned as a string
     * @return  string representation of the value
     * @throws EFapsException on error
     */
    String toString4Where(Object _value)
        throws EFapsException;

    /**
     * Method is executed on addition of an Attribute/Value pair for an Update
     * on an Instance to validate if the value is permitted. Must throw the
     * exception if not valid.
     *
     * @param _attribute    the Attribute that will be updated with the _value
     * @param _instance     Instance that will be updated
     * @param _value        value that will be used for the update
     * @throws EFapsException if not valid
     */
    default void validate4Update(final Attribute _attribute,
                                 final Instance _instance,
                                 final Object[] _value)
        throws EFapsException
    {
    }

    /**
     * Method is executed on addition of an Attribute/Value pair for an Insert
     * on an Instance to validate if the value is permitted. Must throw the
     * exception if not valid.<br/>
     * The given instance is expected to be invalid, but must contain the type.
     *
     * @param _attribute    the Attribute that will be updated with the _value
     * @param _instance     Instance that will be updated
     * @param _value        value that will be used for the update
     * @throws EFapsException if not valid
     */
    default void validate4Insert(final Attribute _attribute,
                        final Instance _instance,
                        final Object[] _value)
        throws EFapsException
    {
    }
}
