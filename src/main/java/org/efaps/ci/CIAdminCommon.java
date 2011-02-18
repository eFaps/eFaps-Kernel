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

package org.efaps.ci;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
// CHECKSTYLE:OFF
public class CIAdminCommon
{

    public static final _Abstract2Abstract Abstract2Abstract = new _Abstract2Abstract("86eafabf-e20e-4107-9cd1-8a28a17f6f3d");

    public static class _Abstract2Abstract
        extends CIType
    {

        protected _Abstract2Abstract(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute Creator = new CIAttribute(this, "Creator");
        public final CIAttribute Created = new CIAttribute(this, "Created");
        public final CIAttribute Modifier = new CIAttribute(this, "Modifier");
        public final CIAttribute Modified = new CIAttribute(this, "Modified");
    }

    public static final _Property Property = new _Property("f3d54a86-c323-43d8-9c78-284d61d955b3");

    public static class _Property
        extends CIType
    {

        protected _Property(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute Name = new CIAttribute(this, "Name");
        public final CIAttribute Value = new CIAttribute(this, "Value");
        public final CIAttribute Abstract = new CIAttribute(this, "Abstract");
        public final CIAttribute Modified = new CIAttribute(this, "Modified");
    }

    public static final _SystemConfiguration SystemConfiguration = new _SystemConfiguration("5fecab1b-f4a8-447d-ad64-cf5965fe5d3b");

    public static class _SystemConfiguration
        extends CIAdmin._Abstract
    {

        protected _SystemConfiguration(final String _uuid)
        {
            super(_uuid);
        }
    }

    public static final _SystemConfigurationAbstract SystemConfigurationAbstract = new _SystemConfigurationAbstract(
                    "24653ad8-4568-41e1-bfd8-1331d2a74beb");

    public static class _SystemConfigurationAbstract
        extends CIType

    {

        protected _SystemConfigurationAbstract(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute AbstractLink = new CIAttribute(this, "AbstractLink");
        public final CIAttribute Key = new CIAttribute(this, "Key");
        public final CIAttribute Value = new CIAttribute(this, "Value");
        public final CIAttribute Description = new CIAttribute(this, "Description");
        public final CIAttribute Creator = new CIAttribute(this, "Creator");
        public final CIAttribute Created = new CIAttribute(this, "Created");
        public final CIAttribute Modifier = new CIAttribute(this, "Modifier");
        public final CIAttribute Modified = new CIAttribute(this, "Modified");
    }

    public static final _SystemConfigurationAttribute SystemConfigurationAttribute = new _SystemConfigurationAttribute(
                    "21c731b4-e717-47dd-92a3-9a6cf731b164");

    public static class _SystemConfigurationAttribute
        extends _SystemConfigurationAbstract

    {

        protected _SystemConfigurationAttribute(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute Link = new CIAttribute(this, "Link");

    }

    public static final _SystemConfigurationLink SystemConfigurationLink = new _SystemConfigurationLink(
                    "227048fb-f7a5-4bf5-b620-c88e6c87eed7");

    public static class _SystemConfigurationLink
        extends _SystemConfigurationAbstract

    {

        protected _SystemConfigurationLink(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute Link = new CIAttribute(this, "Link");

    }

    public static final _SystemConfigurationLink SystemConfigurationObjectAttribute = new _SystemConfigurationLink(
                    "ccf64cd1-ac38-4194-b44e-5706f12ae150");

    public static class SystemConfigurationObjectAttribute
        extends _SystemConfigurationAbstract

    {

        protected SystemConfigurationObjectAttribute(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute Link = new CIAttribute(this, "Link");

    }

    public static final _Version Version = new _Version("1bb051f3-b664-43db-b409-c0c4009f5972");

    public static class _Version
        extends CIType

    {

        protected _Version(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute Name = new CIAttribute(this, "Name");
        public final CIAttribute Revision = new CIAttribute(this, "Revision");
        public final CIAttribute Creator = new CIAttribute(this, "Creator");
        public final CIAttribute Created = new CIAttribute(this, "Created");
        public final CIAttribute Modifier = new CIAttribute(this, "Modifier");
        public final CIAttribute Modified = new CIAttribute(this, "Modified");
    }

    public static final _QuartzTriggerAbstract QuartzTriggerAbstract = new _QuartzTriggerAbstract("b1267287-1532-4a33-9197-144f06f4944c");

    public static class _QuartzTriggerAbstract
        extends CIType

    {

        protected _QuartzTriggerAbstract(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute Name = new CIAttribute(this, "Name");
        public final CIAttribute ESJPLink = new CIAttribute(this, "ESJPLink");
        public final CIAttribute Parameter1 = new CIAttribute(this, "Parameter1");
        public final CIAttribute Parameter2 = new CIAttribute(this, "Parameter2");
        public final CIAttribute Parameter3 = new CIAttribute(this, "Parameter3");
        public final CIAttribute Creator = new CIAttribute(this, "Creator");
        public final CIAttribute Created = new CIAttribute(this, "Created");
        public final CIAttribute Modifier = new CIAttribute(this, "Modifier");
        public final CIAttribute Modified = new CIAttribute(this, "Modified");
    }

    public static final _QuartzTriggerDaily QuartzTriggerDaily = new _QuartzTriggerDaily("d93c4682-f06d-43b0-8503-700f7ec3f8a7");

    public static class _QuartzTriggerDaily
        extends _QuartzTriggerAbstract

    {

        protected _QuartzTriggerDaily(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute Hour = new CIAttribute(this, "Hour");
        public final CIAttribute Minute = new CIAttribute(this, "Minute");

    }

    public static final _QuartzTriggerHourly QuartzTriggerHourly = new _QuartzTriggerHourly("128f67b6-d227-4064-8e2c-0b73f0b57e10");

    public static class _QuartzTriggerHourly
        extends _QuartzTriggerAbstract

    {

        protected _QuartzTriggerHourly(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute IntervalInHours = new CIAttribute(this, "IntervalInHours");
        public final CIAttribute RepeatCount = new CIAttribute(this, "RepeatCount");

    }

    public static final _QuartzTriggerMinutely QuartzTriggerMinutely = new _QuartzTriggerMinutely("70fac71c-b2d2-49ec-aecb-bc0c7f77565e");

    public static class _QuartzTriggerMinutely
        extends _QuartzTriggerAbstract

    {

        protected _QuartzTriggerMinutely(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute IntervalInMinutes = new CIAttribute(this, "IntervalInMinutes");
        public final CIAttribute RepeatCount = new CIAttribute(this, "RepeatCount");

    }

    public static final _QuartzTriggerMonthly QuartzTriggerMonthly = new _QuartzTriggerMonthly("009982f5-2bca-4ca6-90ba-78c13d5e6fac");

    public static class _QuartzTriggerMonthly
        extends _QuartzTriggerAbstract

    {

        protected _QuartzTriggerMonthly(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute DayOfMonth = new CIAttribute(this, "DayOfMonth");
        public final CIAttribute Hour = new CIAttribute(this, "Hour");
        public final CIAttribute Minute = new CIAttribute(this, "Minute");

    }

    public static final _QuartzTriggerSecondly QuartzTriggerSecondly = new _QuartzTriggerSecondly("891e3f87-7403-494a-b333-6c955335aa93");

    public static class _QuartzTriggerSecondly
        extends _QuartzTriggerAbstract

    {

        protected _QuartzTriggerSecondly(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute IntervalInSeconds = new CIAttribute(this, "IntervalInSeconds");
        public final CIAttribute RepeatCount = new CIAttribute(this, "RepeatCount");

    }

    public static final _QuartzTriggerWeekly QuartzTriggerWeekly = new _QuartzTriggerWeekly("74ca424f-f787-47a8-80ac-59f7a136bd43");

    public static class _QuartzTriggerWeekly
        extends _QuartzTriggerAbstract

    {

        protected _QuartzTriggerWeekly(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute DayOfWeek = new CIAttribute(this, "DayOfWeek");
        public final CIAttribute Hour = new CIAttribute(this, "Hour");
        public final CIAttribute Minute = new CIAttribute(this, "Minute");

    }

}
