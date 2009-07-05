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

package org.efaps.admin.datamodel.attributetype;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.efaps.util.EFapsException;

/**
 * @author The eFaps Team
 * @version $Id$
 */
public abstract class AbstractFileType extends AbstractType
{
    /**
     * The value stores the file name of the file.
     *
     * @see #getFileName
     * @see #setFileName
     */
    private String fileName = null;

    /**
     * This is the setter method for instance variable {@link #fileName}.
     *
     * @param _fileName new fileName for instance variable {@link #fileName}
     * @see #fileName
     * @see #getFileName
     */
    public void setFileName(final String _fileName)
    {
        this.fileName = (_fileName != null ? _fileName.trim() : null);
    }

    /**
     * This is the getter method for instance variable {@link #fileName}.
     *
     * @return the fileName of the instance variable {@link #fileName}.
     * @see #fileName
     * @see #setFileName
     */
    public String getFileName()
    {
        return this.fileName;
    }

    /**
     * @see org.efaps.admin.datamodel.IAttributeType#set(java.lang.Object[])
     * @param _values values to set
     */
    public void set(final Object[] _values)
    {
        // set can not be used in file types. it must be done with a checkin
    }

    /**
     * The method updates in the statement the value.
     *
     * @param _object   object
     * @param _stmt     SQL statement to update the value
     * @param _index    index in the SQL statement to update the value
     * @return number of indexes used in the method, if the return value is null an error should be thrown
     * @throws SQLException on error
     */
    public int update(final Object _object, final PreparedStatement _stmt, final int _index)
            throws SQLException
    {
        throw new SQLException("Update value for Type not allowed!!!");
    }

    /**
     * @see org.efaps.admin.datamodel.IAttributeType#readValue(java.util.List)
     * @param _objectList List of Objects
     * @return nothing, because error will be thrown
     * @throws EFapsException allways
     *
     */
    public Object readValue(final List<Object> _objectList) throws EFapsException
    {
        throw new EFapsException(AbstractFileType.class, "readValue.notAllowed");
    }
}
