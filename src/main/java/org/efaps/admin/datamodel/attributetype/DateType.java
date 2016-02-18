/*
 * Copyright 2003 - 2016 The eFaps Team
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

import java.sql.Timestamp;
import java.util.Date;

import org.efaps.util.EFapsException;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;


/**
 * Attribute Type for Date. It uses DateTime by cutting of the Time information.
 *
 * @author The eFaps Team
 *
 */
public class DateType
    extends DateTimeType
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The value that can be set is a Date, a DateTime or a String
     * yyyy-MM-dd'T'HH:mm:ss.SSSZZ. It will be normalized to ISO Calender with
     * TimeZone from SystemAttribute Admin_Common_DataBaseTimeZone. In case that
     * the SystemAttribute is missing UTC will be used.
     * For storing the value in the database the time is set to 00:00;
     *
     * @param _value value to evaluate
     * @return evaluated value
     * @throws EFapsException on error
     */
    @Override
    protected Timestamp eval(final Object[] _value)
        throws EFapsException
    {
        final Timestamp ret;
        if ((_value == null) || (_value.length == 0) || (_value[0] == null)) {
            ret = null;
        } else  {
            DateTime dateTime = new DateTime();
            if (_value[0] instanceof Date) {
                dateTime = new DateTime(_value[0]);
            } else if (_value[0] instanceof DateTime) {
                dateTime = (DateTime) _value[0];
            } else if (_value[0] instanceof String) {
                dateTime = ISODateTimeFormat.dateTime().withOffsetParsed().parseDateTime((String) _value[0]);
            }
            // until now we have a time that depends on the timezone of the application server
            // to convert it in a timestamp for the efaps database the timezone information (mainly the offset)
            // must be removed. This is done by creating a local date with the same, date and time.
            // this guarantees that the datetime inserted into the database depends on the setting
            // in the configuration and not on the timezone for the application server.
            final DateTime localized = new DateTime(dateTime.getYear(),
                                                    dateTime.getMonthOfYear(),
                                                    dateTime.getDayOfMonth(),
                                                    0,
                                                    0,
                                                    0,
                                                    0);
            ret = (localized != null) ? new Timestamp(localized.getMillis()) : null;
        }
        return ret;
    }
}
