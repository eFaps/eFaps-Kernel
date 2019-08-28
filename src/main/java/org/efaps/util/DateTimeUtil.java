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

package org.efaps.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Date;

import org.efaps.admin.EFapsSystemConfiguration;
import org.efaps.admin.KernelSettings;
import org.efaps.db.Context;
import org.efaps.db.transaction.ConnectionResource;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Date and time utility class to handle the time zone from the eFaps
 * correctly.
 *
 * @author The eFaps Team
 *
 */
public final class DateTimeUtil
{
    /**
     * Logging instance used to give logging information of this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(DateTimeUtil.class);

    /**
     * Private constructor so that no instance of this utility class could be
     * created.
     */
    private DateTimeUtil()
    {
    }

    public static DateTimeFormatter FORMATTER = new DateTimeFormatterBuilder()
                    // date/time
                    .append(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    // offset (hh:mm - "+00:00" when it's zero)
                    .optionalStart().appendOffset("+HH:MM", "+00:00").optionalEnd()
                    // offset (hhmm - "+0000" when it's zero)
                    .optionalStart().appendOffset("+HHMM", "+0000").optionalEnd()
                    // offset (hh - "Z" when it's zero)
                    .optionalStart().appendOffset("+HH", "Z").optionalEnd()
                    .toFormatter();

    /**
     * Static method to get the current time stamp from the eFaps database.
     *
     * @return time stamp containing the current time of the eFaps database
     * @throws EFapsException on error
     */
    public static Timestamp getCurrentTimeFromDB() throws EFapsException
    {
        Timestamp now = null;
        final ConnectionResource rsrc = Context.getThreadContext().getConnectionResource();
        final Statement stmt;
        try {
            stmt = rsrc.createStatement();
            final ResultSet resultset = stmt.executeQuery("SELECT " + Context.getDbType().getCurrentTimeStamp());
            resultset.next();
            now = resultset.getTimestamp(1);
            resultset.close();
            stmt.close();
        } catch (final SQLException e) {
            DateTimeUtil.LOG.error("could not execute SQL-Statement", e);
        }
        return now;
    }

    public static ZoneId getDBZoneId()
        throws EFapsException
    {
        ZoneId ret;
        if (EFapsSystemConfiguration.get() == null) {
            ret = ZoneId.systemDefault();
        } else {
            final String zoneId = EFapsSystemConfiguration.get().getAttributeValue(KernelSettings.DBTIMEZONE);
            ret = zoneId == null ? ZoneId.of("Z") : ZoneId.of(zoneId);
        }
        return ret;
    }


    /**
     * The given DateTime will be normalized to ISO calendar with time zone
     * from {@link EFapsSystemConfiguration#KERNEL kernel system configuration}
     * &quot;Admin_Common_DataBaseTimeZone&quot;. In case that the
     * system configuration is missing &quot;UTC&quot; will be used.
     *
     * @param _date     date to normalize
     * @return DateTime normalized for the database
     * @throws EFapsException on error
     */
    public static DateTime normalize(final DateTime _date)
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
        return _date.withChronology(chron);
    }

    /**
     * The value that can be set is a Date, a DateTime or a String
     * yyyy-MM-dd'T'HH:mm:ss.SSSZZ. It will be normalized to ISO Calender with
     * TimeZone from SystemAttribute Admin_Common_DataBaseTimeZone. In case
     * that the SystemAttribute is missing UTC will be used.
     *
     *
     * @param _value    value from user interface to translate
     * @return translated date time
     * @throws EFapsException on error
     */
    public static DateTime translateFromUI(final Object _value)
        throws EFapsException
    {
        final DateTime ret;
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
        if (_value instanceof Date) {
            ret = new DateTime(_value).withChronology(chron);
        } else if (_value instanceof DateTime) {
            ret = ((DateTime) _value).withChronology(chron);
        } else if (_value instanceof String) {
            ret = ISODateTimeFormat.dateTime().parseDateTime((String) _value).withChronology(chron);
        } else if (_value instanceof java.time.LocalDate) {
            final java.time.LocalDate localDateTime = (java.time.LocalDate) _value;
            ret = new DateTime(localDateTime.getYear(), localDateTime.getMonthValue(),
                            localDateTime.getDayOfMonth(), 0, 0).withChronology(chron);
        } else if (_value instanceof java.time.LocalDateTime) {
            final java.time.LocalDateTime localDateTime = (java.time.LocalDateTime) _value;
            ret = new DateTime(localDateTime.getYear(), localDateTime.getMonthValue(), localDateTime.getDayOfMonth(),
                            localDateTime.getHour(), localDateTime.getMinute(), localDateTime.getSecond(),
                            localDateTime.getSecond()).withChronology(chron);
        } else  {
            ret = null;
        }
        return ret;
    }

    public static OffsetDateTime toDateTime(final Object _value)
        throws EFapsException
    {
        OffsetDateTime ret;
        if (_value == null) {
            ret = null;
        } else if (_value instanceof Date) {
            ret = OffsetDateTime.ofInstant(Instant.ofEpochMilli(((Date) _value).getTime()), getDBZoneId());
        } else if (_value instanceof DateTime) {
            ret = OffsetDateTime.parse(((DateTime) _value).toString(), FORMATTER);
        } else if (_value instanceof String) {
            ret = OffsetDateTime.parse((String) _value, FORMATTER);
        } else if (_value instanceof LocalDate) {
            final LocalDateTime localDateTime = LocalDateTime.of((LocalDate) _value, LocalTime.MIN);
            ret = OffsetDateTime.of(localDateTime, getDBZoneId().getRules().getOffset(localDateTime));
        } else if (_value instanceof LocalDateTime) {
            final LocalDateTime localDateTime = (LocalDateTime) _value;
            ret = OffsetDateTime.of(localDateTime, getDBZoneId().getRules().getOffset(localDateTime));
        } else if (_value instanceof OffsetDateTime) {
            ret = (OffsetDateTime) _value;
        } else {
            LOG.warn("Cannot convert value {} to OffsetDateTime", _value);
            ret = null;
        }
        return ret;
    }

    public static LocalDate toDate(final Object _value)
        throws EFapsException
    {
        LocalDate ret;
        if (_value == null) {
            ret = null;
        } else if (_value instanceof Date) {
            final Instant instant = ((Date) _value).toInstant();
            ret = instant.atZone(DateTimeUtil.getDBZoneId()).toLocalDate();
        } else if (_value instanceof DateTime) {
            final DateTime dateTime = (DateTime)_value;
            ret = LocalDate.of(dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth());
        } else if (_value instanceof String) {
            ret = LocalDate.parse((String) _value);
        } else if (_value instanceof LocalDateTime) {
            final LocalDateTime localDateTime = (LocalDateTime) _value;
            ret = LocalDate.of(localDateTime.getYear(), localDateTime.getMonthValue(), localDateTime.getDayOfMonth());
        } else if (_value instanceof LocalDate) {
            ret = (LocalDate) _value;
        } else {
            LOG.warn("Cannot convert value {} to OffsetDateTime", _value);
            ret = null;
        }
        return ret;
    }

    public static LocalTime toTime(final Object _value)
        throws EFapsException
    {
        LocalTime ret;
        if (_value == null) {
            ret = null;
        } else if (_value instanceof Date) {
            final Instant instant = ((Date) _value).toInstant();
            ret = instant.atZone(DateTimeUtil.getDBZoneId()).toLocalTime();
        } else if (_value instanceof DateTime) {
            final DateTime dateTime = (DateTime) _value;
            ret = LocalTime.of(dateTime.getHourOfDay(), dateTime.getMinuteOfHour(), dateTime.getSecondOfMinute());
        } else if (_value instanceof String) {
            ret = LocalTime.parse((String) _value);
        } else if (_value instanceof LocalDateTime) {
            final LocalDateTime localDateTime = (LocalDateTime) _value;
            ret = LocalTime.of(localDateTime.getHour(), localDateTime.getMinute(), localDateTime.getSecond());
        } else if (_value instanceof LocalTime) {
            ret = (LocalTime) _value;
        } else {
            LOG.warn("Cannot convert value {} to OffsetDateTime", _value);
            ret = null;
        }
        return ret;
    }
}
