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

package org.efaps.admin.datamodel.attributetype;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.efaps.admin.EFapsSystemConfiguration;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.db.query.CachedResult;
import org.efaps.db.wrapper.AbstractSQLInsertUpdate;
import org.efaps.util.DateTimeUtil;
import org.efaps.util.EFapsException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.ReadableDateTime;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.format.ISODateTimeFormat;

/**
 * @author The eFaps Team
 * @version $Id$
 */
public class DateTimeType
    extends AbstractType
{
    /**
     * {@inheritDoc}
     */
    @Override
    public Object readValue(final Attribute _attribute,
                            final CachedResult _rs,
                            final List<Integer> _indexes)
    {
        return _rs.getDateTime(_indexes.get(0).intValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object readValue(final Attribute _attribute,
                            final List<Object> _objectList)
    {
        // reads the Value from "Admin_Common_DataBaseTimeZone"
        final String timezoneID = EFapsSystemConfiguration.KERNEL.get().getAttributeValue("DataBaseTimeZone");
        final ISOChronology chron;
        if (timezoneID != null) {
            final DateTimeZone timezone = DateTimeZone.forID(timezoneID);
            chron = ISOChronology.getInstance(timezone);
        } else {
            chron = ISOChronology.getInstanceUTC();
        }

        final List<DateTime> ret = new ArrayList<DateTime>();
        for (final Object object : _objectList) {
            if (object instanceof Timestamp || object instanceof Date) {
                ret.add(new DateTime(object, chron));
            } else if (ret != null) {
                ret.add(new DateTime());
            }
        }
        return _objectList.size() > 0 ? (ret.size() > 1 ? ret : ret.get(0)) : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void prepare(final AbstractSQLInsertUpdate<?> _insertUpdate,
                           final Attribute _attribute,
                           final Object... _values)
        throws SQLException
    {
        checkSQLColumnSize(_attribute, 1);
        _insertUpdate.column(_attribute.getSqlColNames().get(0), eval(_values));
    }


    /**
     * The value that can be set is a Date, a DateTime or a String
     * yyyy-MM-dd'T'HH:mm:ss.SSSZZ. It will be normalized to ISO Calender with
     * TimeZone from SystemAttribute Admin_Common_DataBaseTimeZone. In case that
     * the SystemAttribute is missing UTC will be used.
     *
     * @param _value value to evaluate
     * @return evaluated value
     */
    protected Timestamp eval(final Object[] _value)
    {
        final Timestamp ret;
        if ((_value == null) || (_value.length == 0) || (_value[0] == null)) {
            ret = null;
        } else  {
            final DateTime dateTime = DateTimeUtil.translateFromUI(_value[0]);
            ret = (dateTime != null) ? new Timestamp(dateTime.getMillis()) : null;
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString4Where(final Object _value)
        throws EFapsException
    {
        String ret = "";
        if (_value instanceof ReadableDateTime) {
            ret = ((ReadableDateTime) _value).toDateTime().toString(ISODateTimeFormat.dateTime());
        } else if (_value instanceof String) {
            ret = (String) _value;
        }
        return ret;
    }
}
