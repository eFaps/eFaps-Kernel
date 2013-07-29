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


package org.efaps.admin.datamodel.attributetype;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.util.EFapsException;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.ISODateTimeFormat;


/**
 * Attribute Type for Date. It uses DateTime by cutting of the Time information.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class TimeType
    extends DateTimeType
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;


    @Override
    public Object readValue(final Attribute _attribute,
                            final List<Object> _objectList)
        throws EFapsException
    {
        final List<LocalTime> ret = new ArrayList<LocalTime>();
        for (final Object object : _objectList) {
            if (object instanceof Timestamp || object instanceof Date) {
                ret.add(new DateTime(object).toLocalTime());
            } else if (ret != null) {
                ret.add(new LocalTime());
            }
        }
        return _objectList.size() > 0 ? (ret.size() > 1 ? ret : ret.get(0)) : null;
    }


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
            LocalTime time = new LocalTime();
            if (_value[0] instanceof Date) {
                time = new DateTime(_value[0]).toLocalTime();
            } else if (_value[0] instanceof DateTime) {
                time = ((DateTime) _value[0]).toLocalTime();
            } else if (_value[0] instanceof String) {
                time = ISODateTimeFormat.localTimeParser().parseLocalTime((String) _value[0]);
            }
            ret = new Timestamp(time.toDateTimeToday().getMillis());
        }
        return ret;
    }
}
