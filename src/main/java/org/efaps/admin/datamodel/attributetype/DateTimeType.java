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

package org.efaps.admin.datamodel.attributetype;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.efaps.admin.EFapsSystemConfiguration;
import org.efaps.admin.KernelSettings;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.db.Context;
import org.efaps.db.wrapper.AbstractSQLInsertUpdate;
import org.efaps.util.DateTimeUtil;
import org.efaps.util.EFapsException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.ReadableDateTime;
import org.joda.time.chrono.ISOChronology;

/**
 * @author The eFaps Team
 *
 */
public class DateTimeType
    extends AbstractType
    implements IFormattableType
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * {@inheritDoc}
     * @throws EFapsException
     */
    @Override
    public Object readValue(final Attribute _attribute,
                            final List<Object> _objectList)
        throws EFapsException
    {
        // reads the Value from "Admin_Common_DataBaseTimeZone"
        final String timezoneID = EFapsSystemConfiguration.get() == null ? DateTimeZone.getDefault().getID()
                : EFapsSystemConfiguration.get().getAttributeValue(KernelSettings.DBTIMEZONE);
        final ISOChronology chron;
        if (timezoneID != null) {
            final DateTimeZone timezone = DateTimeZone.forID(timezoneID);
            chron = ISOChronology.getInstance(timezone);
        } else {
            chron = ISOChronology.getInstanceUTC();
        }

        final List<DateTime> ret = new ArrayList<>();
        for (final Object object : _objectList) {
            if (object instanceof Timestamp || object instanceof Date) {
                // to avoid the automatic "correction" of the timezone first a local date must be made
                // and than a new Date with the correct timezone must be created
                final DateTime dateTime = new DateTime(object);
                final DateTime unlocalized = new DateTime(dateTime.getYear(),
                                dateTime.getMonthOfYear(),
                                dateTime.getDayOfMonth(),
                                dateTime.getHourOfDay(),
                                dateTime.getMinuteOfHour(),
                                dateTime.getSecondOfMinute(),
                                dateTime.getMillisOfSecond(),
                                chron);
                ret.add(unlocalized);
            } else if (object != null) {
                ret.add(new DateTime());
            } else {
                ret.add(null);
            }
        }
        return _objectList.size() > 0 ? ret.size() > 1 ? ret : ret.get(0) : null;
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
        try {
            _insertUpdate.column(_attribute.getSqlColNames().get(0), eval(_values));
        } catch (final EFapsException e) {
            throw new SQLException(e);
        }
    }


    /**
     * The value that can be set is a Date, a DateTime or a String
     * yyyy-MM-dd'T'HH:mm:ss.SSSZZ. It will be normalized to ISO Calender with
     * TimeZone from SystemAttribute Admin_Common_DataBaseTimeZone. In case that
     * the SystemAttribute is missing UTC will be used.
     *
     * @param _value value to evaluate
     * @return evaluated value
     * @throws EFapsException on error
     */
    protected Timestamp eval(final Object[] _value)
        throws EFapsException
    {
        final Timestamp ret;
        if (_value == null || _value.length == 0 || _value[0] == null) {
            ret = null;
        } else  {
            final DateTime dateTime = DateTimeUtil.translateFromUI(_value[0]);
            // until now we have a time that depends on the timezone of the application server
            // to convert it in a timestamp for the efaps database the timezone information (mainly the offset)
            // must be removed. This is done by creating a local date with the same, date and time.
            // this guarantees that the datetime inserted into the database depends on the setting
            // in the configuration and not on the timezone for the application server.
            final DateTime localized = new DateTime(dateTime.getYear(),
                                                    dateTime.getMonthOfYear(),
                                                    dateTime.getDayOfMonth(),
                                                    dateTime.getHourOfDay(),
                                                    dateTime.getMinuteOfHour(),
                                                    dateTime.getSecondOfMinute(),
                                                    dateTime.getMillisOfSecond());
            ret = localized != null ? new Timestamp(localized.getMillis()) : null;
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
            ret = Context.getDbType().getStr4DateTime((ReadableDateTime) _value);
        } else if (_value instanceof OffsetDateTime) {
            ret = Context.getDbType().getStr4DateTime((OffsetDateTime) _value);
        } else if (_value instanceof String) {
            ret = (String) _value;
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object format(final Object _object,
                         final String _pattern)
        throws EFapsException
    {
        final Object ret;
        if (_object instanceof DateTime) {
            ret = ((DateTime) _object).toString(_pattern, Context.getThreadContext().getLocale());
        } else {
            ret = _object;
        }
        return ret;
    }
}
