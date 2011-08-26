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

package org.efaps.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;

import org.efaps.admin.EFapsSystemConfiguration;
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
 * @version $Id$
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
        Statement stmt;
        try {
            stmt = rsrc.getConnection().createStatement();
            final ResultSet resultset = stmt.executeQuery("SELECT " + Context.getDbType().getCurrentTimeStamp());
            resultset.next();
            now = resultset.getTimestamp(1);
            resultset.close();
            stmt.close();
            rsrc.commit();
        } catch (final SQLException e) {
            DateTimeUtil.LOG.error("could not execute SQL-Statement", e);
        }
        return now;
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
        final String timezoneID = EFapsSystemConfiguration.KERNEL.get().getAttributeValue("DataBaseTimeZone");
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
        final String timezoneID = EFapsSystemConfiguration.KERNEL.get().getAttributeValue("DataBaseTimeZone");
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
        } else  {
            ret = null;
        }
        return ret;
    }
}
