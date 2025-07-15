/*
 * Copyright © 2003 - 2024 The eFaps Team (-)
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
 */
package org.efaps.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoUnit;
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
 * Date and time utility class to handle the time zone from the eFaps correctly.
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
    public static Timestamp getCurrentTimeFromDB()
        throws EFapsException
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
            ret = ZoneId.of("UTC");
        } else {
            final String zoneId = EFapsSystemConfiguration.get().getAttributeValue(KernelSettings.DBTIMEZONE);
            ret = zoneId == null ? ZoneId.of("UTC") : ZoneId.of(zoneId);
        }
        return ret;
    }

    /**
     * The given DateTime will be normalized to ISO calendar with time zone from
     * {@link EFapsSystemConfiguration#KERNEL kernel system configuration}
     * &quot;Admin_Common_DataBaseTimeZone&quot;. In case that the system
     * configuration is missing &quot;UTC&quot; will be used.
     *
     * @param _date date to normalize
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
     * TimeZone from SystemAttribute Admin_Common_DataBaseTimeZone. In case that
     * the SystemAttribute is missing UTC will be used.
     *
     *
     * @param _value value from user interface to translate
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
        } else if (_value instanceof final java.time.LocalDate localDateTime) {
            ret = new DateTime(localDateTime.getYear(), localDateTime.getMonthValue(),
                            localDateTime.getDayOfMonth(), 0, 0).withChronology(chron);
        } else if (_value instanceof final java.time.LocalDateTime localDateTime) {
            ret = new DateTime(localDateTime.getYear(), localDateTime.getMonthValue(), localDateTime.getDayOfMonth(),
                            localDateTime.getHour(), localDateTime.getMinute(), localDateTime.getSecond(),
                            localDateTime.getSecond()).withChronology(chron);
        } else {
            ret = null;
        }
        return ret;
    }

    /**
     * Convert a context value to db value
     *
     * @param value db time value
     * @return context time value
     * @throws EFapsException
     */
    public static OffsetDateTime toDBDateTime(final Object value)
        throws EFapsException
    {
        LOG.debug("Converting {} to DBDateTime", value);
        final var dateTime = toDateTime(value, Context.getThreadContext().getZoneId());
        LOG.debug("Result: {} ", dateTime);
        if (dateTime != null) {
            final var offset = getDBZoneId().getRules().getOffset(LocalDateTime.now());
            final var withOffset = dateTime.withOffsetSameInstant(offset);
            LOG.debug("with Offset: {} ", withOffset);
            return withOffset;
        } else {
            return null;
        }
    }

    /**
     * Convert a db value to context value
     *
     * @param value db time value
     * @return context time value
     * @throws EFapsException
     */
    public static OffsetDateTime toContextDateTime(final Object value)
        throws EFapsException
    {
        LOG.debug("Converting {} to ContextDateTime", value);
        final OffsetDateTime ret = toDateTime(value, getDBZoneId());
        LOG.debug("Result: {} ", ret);

        if (ret != null) {
            final var offset = Context.getThreadContext().getZoneId().getRules().getOffset(LocalDateTime.now());
            final var withOffset = ret.withOffsetSameInstant(offset);
            LOG.debug("with Offset: {} ", withOffset);
            return withOffset;
        } else {
            return null;
        }
    }

    static OffsetDateTime toDateTime(final Object value,
                                     final ZoneId valueZoneId)
        throws EFapsException
    {
        OffsetDateTime ret;
        if (value == null) {
            ret = null;
        } else if (value instanceof final Timestamp date) {
            final var localDateTime = date.toLocalDateTime();
            ret = OffsetDateTime.of(localDateTime, valueZoneId.getRules().getOffset(localDateTime));
        } else if (value instanceof final Date date) {
            ret = OffsetDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()), valueZoneId);
        } else if (value instanceof DateTime) {
            ret = OffsetDateTime.parse(((DateTime) value).toString(), FORMATTER);
        } else if (value instanceof final String valueStr) {
            ret = OffsetDateTime.parse(valueStr, FORMATTER);
        } else if (value instanceof LocalDate) {
            final LocalDateTime localDateTime = LocalDateTime.of((LocalDate) value, LocalTime.MIN);
            ret = OffsetDateTime.of(localDateTime,
                            valueZoneId.getRules().getOffset(localDateTime));
        } else if (value instanceof final LocalDateTime localDateTime) {
            ret = OffsetDateTime.of(localDateTime, valueZoneId.getRules().getOffset(localDateTime));
        } else if (value instanceof OffsetDateTime) {
            ret = (OffsetDateTime) value;
        } else {
            LOG.error("Cannot convert value {} to OffsetDateTime for DB", value);
            ret = null;
        }
        return ret;
    }

    public static LocalDate toDBDate(final Object value)
        throws EFapsException
    {
        final var ret = toDateInternal(value);
        return ret;
    }

    public static LocalDate toContextDate(final Object value)
        throws EFapsException
    {
        final var ret = toDateInternal(value);
        return ret;
    }

    static LocalDate toDateInternal(final Object value)
        throws EFapsException
    {
        LocalDate ret;
        if (value == null) {
            ret = null;
        } else if (value instanceof Date) {
            final var dateStr = new SimpleDateFormat("yyyy-MM-dd").format(value);
            ret = LocalDate.parse(dateStr);
        } else if (value instanceof final DateTime dateTime) {
            ret = LocalDate.of(dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth());
        } else if (value instanceof final String str) {
            // an empty or not long enough value -> null
            if (str.length() < 10) {
                ret = null;
            } else {
                ret = LocalDate.parse(str.substring(0, 10));
            }
        } else if (value instanceof final LocalDateTime localDateTime) {
            ret = LocalDate.of(localDateTime.getYear(), localDateTime.getMonthValue(),
                            localDateTime.getDayOfMonth());
        } else if (value instanceof final OffsetDateTime dateTime) {
            ret = LocalDate.of(dateTime.getYear(), dateTime.getMonth(), dateTime.getDayOfMonth());
        } else if (value instanceof LocalDate) {
            ret = (LocalDate) value;
        } else {
            LOG.warn("Cannot convert value {} to LocalDate", value);
            ret = null;
        }
        return ret;
    }

    public static LocalTime toContextTime(final Object value)
        throws EFapsException
    {
        LocalTime ret;
        if (value == null) {
            ret = null;
        } else if (value instanceof Timestamp) {
            // value from database -> do not change the timezone
            final LocalTime dbLocal = ((Timestamp) value).toLocalDateTime().toLocalTime();
            ret = dbLocal.minusHours(getOffset()).withNano(0);
        } else {
            LOG.error("Cannot convert value {} to LocalTime fro context", value);
            ret = null;
        }
        return ret;
    }

    public static LocalTime toDBTime(final Object value)
        throws EFapsException
    {
        LocalTime ret;
        if (value == null) {
            ret = null;
        } else if (value instanceof final String strValue) {
            DateTimeFormatter frmt;
            if (strValue.matches("\\d\\d:\\d\\d:\\d\\d")) {
                frmt = DateTimeFormatter.ISO_LOCAL_TIME;
            } else if (strValue.matches("\\d:\\d\\d:\\d\\d")) {
                frmt = DateTimeFormatter.ofPattern("H:mm:ss");
            } else if (strValue.matches("\\d\\d:\\d\\d")) {
                frmt = DateTimeFormatter.ofPattern("HH:mm");
            } else if (strValue.matches("\\d\\d:\\d")) {
                frmt = DateTimeFormatter.ofPattern("HH:m");
            } else if (strValue.matches("\\d:\\d\\d")) {
                frmt = DateTimeFormatter.ofPattern("H:mm");
            } else if (strValue.matches("\\d:\\d")) {
                frmt = DateTimeFormatter.ofPattern("H:m");
            } else {
                frmt = null;
            }
            if (frmt == null) {
                LOG.error("Cannot parse string value {} to LocalTime for DB", value);
                ret = null;
            } else {
                ret = LocalTime.parse((String) value, frmt).plusHours(getOffset());
            }
        } else if (value instanceof final LocalDateTime localDateTime) {
            ret = LocalTime.of(localDateTime.getHour(), localDateTime.getMinute(), localDateTime.getSecond())
                            .plusHours(getOffset());
        } else if (value instanceof LocalTime) {
            ret = ((LocalTime) value).plusHours(getOffset());
        } else {
            LOG.error("Cannot convert value {} to LocalTime for DB", value);
            ret = null;
        }
        return ret;
    }

    public static long getOffset()
        throws EFapsException
    {
        return ChronoUnit.HOURS.between(LocalTime.now(Context.getThreadContext().getZoneId()),
                        LocalTime.now(DateTimeUtil.getDBZoneId()));
    }

    public static OffsetDateTime asContextDateTime(final LocalDate localdate)
        throws EFapsException
    {
        final var systemZone = Context.getThreadContext().getZoneId();
        final var zoneOffset = systemZone.getRules().getOffset(Instant.now());
        return OffsetDateTime.of(localdate.getYear(), localdate.getMonthValue(), localdate.getDayOfMonth(), 0, 0, 0, 0,
                        zoneOffset);
    }

    @Deprecated
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
        } else if (_value instanceof final LocalDateTime localDateTime) {
            ret = OffsetDateTime.of(localDateTime, getDBZoneId().getRules().getOffset(localDateTime));
        } else if (_value instanceof OffsetDateTime) {
            ret = (OffsetDateTime) _value;
        } else {
            LOG.warn("Cannot convert value {} to OffsetDateTime", _value);
            ret = null;
        }
        return ret;
    }

    @Deprecated
    public static LocalDate toDate(final Object _value)
        throws EFapsException
    {
        LocalDate ret;
        if (_value == null) {
            ret = null;
        } else if (_value instanceof Date) {
            final Instant instant = ((Date) _value).toInstant();
            ret = instant.atZone(DateTimeUtil.getDBZoneId()).toLocalDate();
        } else if (_value instanceof final DateTime dateTime) {
            ret = LocalDate.of(dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth());
        } else if (_value instanceof final String str) {
            // an empty or not long enough value -> null
            if (str.length() < 10) {
                ret = null;
            } else {
                ret = LocalDate.parse(str.substring(0, 10));
            }
        } else if (_value instanceof final LocalDateTime localDateTime) {
            ret = LocalDate.of(localDateTime.getYear(), localDateTime.getMonthValue(), localDateTime.getDayOfMonth());
        } else if (_value instanceof final OffsetDateTime dateTime) {
            ret = LocalDate.of(dateTime.getYear(), dateTime.getMonth(), dateTime.getDayOfMonth());
        } else if (_value instanceof LocalDate) {
            ret = (LocalDate) _value;
        } else {
            LOG.warn("Cannot convert value {} to LocalDate", _value);
            ret = null;
        }
        return ret;
    }
}
