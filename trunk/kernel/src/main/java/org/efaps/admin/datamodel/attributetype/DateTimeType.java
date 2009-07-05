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
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import org.efaps.admin.common.SystemConfiguration;
import org.efaps.db.query.CachedResult;

/**
 * @author The eFaps Team
 * @version $Id$
 */
public class DateTimeType extends AbstractType
{

    /**
     * @see #getValue
     * @see #setValue
     */
    private DateTime value = null;

    public Object readValue(final CachedResult _rs, final List<Integer> _indexes)
    {
        setValue(_rs.getDateTime(_indexes.get(0).intValue()));
        return getValue();
    }

    /**
     * @see org.efaps.admin.datamodel.IAttributeType#readValue(java.util.List)
     * @param _objectList List of Objects
     * @return DateTime
     * TODO throw error if more than one value is given
     */
    public Object readValue(final List<Object> _objectList)
    {
        DateTime ret = null;
        final Object obj = _objectList.get(0);
        if (obj instanceof Timestamp || obj instanceof Date) {
            // reads the Value from "Admin_Common_DataBaseTimeZone"
            final SystemConfiguration kernelConfig = SystemConfiguration.get(UUID
                            .fromString("acf2b19b-f7c4-4e4a-a724-fb2d9ed30079"));
            final String timezoneID = kernelConfig.getAttributeValue("DataBaseTimeZone");
            final ISOChronology chron;
            if (timezoneID != null) {
                final DateTimeZone timezone = DateTimeZone.forID(timezoneID);
                chron = ISOChronology.getInstance(timezone);
            } else {
                chron = ISOChronology.getInstanceUTC();
            }
            ret = new DateTime(obj, chron);
        }
        this.value = ret;
        return ret;
    }

    /**
     * The value that can be set is a Date, a DateTime or a String
     * yyyy-MM-dd'T'HH:mm:ss.SSSZZ. It will be normalized to ISO Calender with
     * TimeZone from SystemAttribute Admin_Common_DataBaseTimeZone. In case that
     * the SystemAttribute is missing UTC will be used.
     *
     *
     * @param _value new value to set
     */
    public void set(final Object[] _value)
    {
        if (_value[0] != null) {
            // reads the Value from "Admin_Common_DataBaseTimeZone"
            final SystemConfiguration kernelConfig = SystemConfiguration.get(UUID
                            .fromString("acf2b19b-f7c4-4e4a-a724-fb2d9ed30079"));
            final String timezoneID = kernelConfig.getAttributeValue("DataBaseTimeZone");
            final ISOChronology chron;
            if (timezoneID != null) {
                final DateTimeZone timezone = DateTimeZone.forID(timezoneID);
                chron = ISOChronology.getInstance(timezone);
            } else {
                chron = ISOChronology.getInstanceUTC();
            }
            if (_value[0] instanceof Date) {
                final DateTime datetime = new DateTime(_value[0]);
                this.value = datetime.withChronology(chron);
            } else if (_value[0] instanceof DateTime) {
                this.value = ((DateTime) _value[0]).withChronology(chron);
            } else if (_value[0] instanceof String) {
                final DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
                this.value = fmt.parseDateTime((String) _value[0]);
            }
        }
    }

    /**
     * @see org.efaps.admin.datamodel.attributetype.AbstractLinkType#update(java.lang.Object, java.sql.PreparedStatement, int)
     * @param _object   object
     * @param _stmt     SQL statement to update the value
     * @param _index    index in the SQL statement to update the value
     * @return number of indexes used in the method, if the return value is null an error should be thrown
     * @throws SQLException on error
     */
    public int update(final Object _object, final PreparedStatement _stmt, final int _index)
                    throws SQLException
    {
        _stmt.setTimestamp(_index, new Timestamp(this.value.getMillis()));
        return 1;
    }

    /**
     * This is the setter method for instance variable {@link #value}.
     *
     * @param _value new value for instance variable {@link #value}
     * @see #value
     * @see #getValue
     */
    public final void setValue(final DateTime _value)
    {
        this.value = _value;
    }

    /**
     * This is the getter method for instance variable {@link #value}.
     *
     * @return the value of the instance variable {@link #value}.
     * @see #value
     * @see #setValue
     */
    public DateTime getValue()
    {
        return this.value;
    }

    @Override
    public String toString()
    {
        return "" + getValue();
    }
}
