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
import java.util.ArrayList;
import java.util.List;

import org.efaps.admin.datamodel.Dimension;
import org.efaps.admin.datamodel.Dimension.UoM;
import org.efaps.db.query.CachedResult;


/**
 * Abstract class for an attribute type with an UoM.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public abstract class AbstractWithUoMType extends AbstractType
{
    /**
     * Uom of this attribute type.
     */
    private UoM uoM = null;

    /**
     * Getter method for instance variable {@link #uoM}.
     *
     * @return value of instance variable {@link #uoM}
     */
    public UoM getUoM()
    {
        return this.uoM;
    }

    /**
     * Setter method for instance variable {@link #uoM}.
     *
     * @param _uoM value for instance variable {@link #uoM}
     */
    public void setUoM(final UoM _uoM)
    {
        this.uoM = _uoM;
    }

    /**
     * The method prepares the statement for insert the object in the database.
     * It must be overwritten, because this type has at least two columns.
     *
     * @param _stmt string buffer to append the statement
     * @return <i>true</i> if only a preparation is needed, otherwise
     *         <i>false</i> if the value must be set
     */
    @Override
    public boolean prepareInsert(final StringBuilder _stmt)
    {
        _stmt.append("?,?");
        if (getAttribute().getSqlColNames().size() == 3) {
            _stmt.append(",?");
        }
        return false;
    }

    /**
     * @see org.efaps.admin.datamodel.attributetype.AbstractLinkType#update(java.lang.Object, java.sql.PreparedStatement, int)
     * @see #setValueStmt(PreparedStatement, int)
     * @param _object   object
     * @param _stmt     SQL statement to update the value
     * @param _index    index in the SQL statement to update the value
     * @return number of indexes used in the method, if the return value is null an error should be thrown
     * @throws SQLException on error
     */
    public int update(final Object _object, final PreparedStatement _stmt, final int _index) throws SQLException
    {
        int ret = 2;
        setValueStmt(_stmt, _index);
        _stmt.setLong(_index + 1, getUoM().getId());
        if (getAttribute().getSqlColNames().size() > 2) {
            _stmt.setDouble(_index + 2, getUoM().getBaseDouble(getValue()));
            ret++;
        }
        return ret;
    }

    /**
     * Read the value from the resultset. Works with
     * {@link #readValue(CachedResult, int)} to return an array with the
     * values:
     * <ul>
     * <li>object, UoM</li>
     * <li>or object, UoM, Base as Double</li>
     * </ul>
     *
     * @param _rs       cached result from the JDBC select statement
     * @param _indexes  indexes in the result set
     * @return Object Array
     */
    public Object readValue(final CachedResult _rs, final List<Integer> _indexes)
    {
        final Object object = readValue(_rs.getObject(_indexes.get(0)));
        setUoM(Dimension.getUoM(_rs.getLong(_indexes.get(1))));
        final Object[] ret;
        if (getAttribute().getSqlColNames().size() > 2) {
            ret = new Object[]{object, getUoM(), _rs.getDouble(_indexes.get(2))};
        } else {
            ret = new Object[]{object, getUoM()};
        }
        return ret;
    }

    /**
     * @see org.efaps.admin.datamodel.IAttributeType#readValue(java.util.List)
     * @param _objectList List of Objects
     * @return DateTime
     * TODO throw error if more than one value is given
     */
    public Object readValue(final List<Object> _objectList)
    {
        final List<Object[]> ret = new ArrayList<Object[]>();
        for (final Object object : _objectList) {
            final Object[] temp = (Object[]) object;
            final Object value = readValue(temp[0]);
            final UoM uom = Dimension.getUoM((Long) temp[1]);
            if (temp.length > 2) {
                final Object obj = temp[2];
                double dbl = 0;
                if (obj instanceof Number) {
                    dbl = ((Number) obj).doubleValue();
                } else if (obj != null) {
                    dbl = Double.parseDouble(obj.toString());
                }
                ret.add(new Object[]{value, uom, dbl});
            } else {
                ret.add(new Object[]{value, uom});
            }

        }
        return _objectList.size() > 0 ? (ret.size() > 1 ? ret : ret.get(0)) : null;
    }

    /**
     * Method to read the value from the cached result.
     * @see #readValue(CachedResult, List)
     *
     * @param _object   Object to read
     * @return value as object
     */
    protected abstract Object readValue(final Object _object);

    /**
     * Method to set the statement belonging to the value.
     *
     * @param _stmt     PreparedStatement
     * @param _index    index
     * @throws SQLException on error
     */
    protected abstract void setValueStmt(final PreparedStatement _stmt, final int _index) throws SQLException;

    /**
     * Get the Value as a double to calculate the base value.
     * @return value as double
     */
    protected abstract Double getValue();
}
