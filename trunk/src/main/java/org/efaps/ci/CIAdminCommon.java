/*
 * Copyright 2003 - 2010 The eFaps Team
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

        public final CIAttribute AbstractLink = new CIAttribute(this, "AbstractLink");

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

        public final CIAttribute AbstractLink = new CIAttribute(this, "AbstractLink");

    }

}
