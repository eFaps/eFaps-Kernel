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
public class CIAdminUser
{

    public static final _Abstract Abstract = new _Abstract("4c3e33a2-a024-4bb7-b857-69886bce7132");

    public static class _Abstract
        extends CIType
    {

        protected _Abstract(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute Name = new CIAttribute(this, "Name");
        public final CIAttribute UUID = new CIAttribute(this, "UUID");
        public final CIAttribute Revision = new CIAttribute(this, "Revision");
        public final CIAttribute Status = new CIAttribute(this, "Status");
        public final CIAttribute Creator = new CIAttribute(this, "Creator");
        public final CIAttribute Created = new CIAttribute(this, "Created");
        public final CIAttribute Modifier = new CIAttribute(this, "Modifier");
        public final CIAttribute Modified = new CIAttribute(this, "Modified");
    }

    public static final _Abstract2Abstract _Abstract2Abstract = new _Abstract2Abstract("1ded9229-3daa-4c27-8e2a-175e5760470b");

    public static class _Abstract2Abstract
        extends CIType
    {

        protected _Abstract2Abstract(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute UserJAASSystem = new CIAttribute(this, "UserJAASSystem");
        public final CIAttribute UserFromAbstractLink = new CIAttribute(this, "UserFromAbstractLink");
        public final CIAttribute UserToAbstractLink = new CIAttribute(this, "UserToAbstractLink");
        public final CIAttribute Creator = new CIAttribute(this, "Creator");
        public final CIAttribute Created = new CIAttribute(this, "Created");
        public final CIAttribute Modifier = new CIAttribute(this, "Modifier");
        public final CIAttribute Modified = new CIAttribute(this, "Modified");
    }


    public static final _JAASKey JAASKey = new _JAASKey("0e7650c6-8ec3-4c63-b377-f3eb5fb85f16");

    public static class _JAASKey
        extends CIType
    {

        protected _JAASKey(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute Key = new CIAttribute(this, "Key");
        public final CIAttribute JAASSystemLink = new CIAttribute(this, "JAASSystemLink");
        public final CIAttribute UserLink = new CIAttribute(this, "UserLink");
        public final CIAttribute Creator = new CIAttribute(this, "Creator");
        public final CIAttribute Created = new CIAttribute(this, "Created");
        public final CIAttribute Modifier = new CIAttribute(this, "Modifier");
        public final CIAttribute Modified = new CIAttribute(this, "Modified");
    }

    public static final _Person Person = new _Person("fe9d94fd-2ed8-4c44-b1f0-00e150555888");

    public static class _Person
        extends _Abstract
    {

        protected _Person(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute FirstName = new CIAttribute(this, "FirstName");
        public final CIAttribute LastName = new CIAttribute(this, "LastName");
        public final CIAttribute TimeZone = new CIAttribute(this, "TimeZone");
        public final CIAttribute Chronology = new CIAttribute(this, "Chronology");
        public final CIAttribute Language = new CIAttribute(this, "Language");
        public final CIAttribute Locale = new CIAttribute(this, "Locale");
        public final CIAttribute EmailSet = new CIAttribute(this, "EmailSet");
        public final CIAttribute Password = new CIAttribute(this, "Password");
        public final CIAttribute LastLogin = new CIAttribute(this, "LastLogin");
        public final CIAttribute LoginTry = new CIAttribute(this, "LoginTry");
        public final CIAttribute LoginTriesCounter = new CIAttribute(this, "LoginTriesCounter");
    }

    public static final _Person2Role Person2Role = new _Person2Role("37deb6ae-3e1c-4642-8823-715120386fc3");

    public static class _Person2Role
        extends _Abstract2Abstract
    {

        protected _Person2Role(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute UserFromLink = new CIAttribute(this, "UserFromLink");
        public final CIAttribute UserToLink = new CIAttribute(this, "UserToLink");

    }

    public static final _Person2Group Person2Group = new _Person2Group("fec64148-a39b-4f69-bedd-9c3bcfe8e1602");

    public static class _Person2Group
        extends _Abstract2Abstract
    {

        protected _Person2Group(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute UserFromLink = new CIAttribute(this, "UserFromLink");
        public final CIAttribute UserToLink = new CIAttribute(this, "UserToLink");

    }

    public static final _AttributeAbstract AttributeAbstract = new _AttributeAbstract("d9dd0971-0bb9-4ac1-ba46-8aefd5e8badb");

    public static class _AttributeAbstract
        extends CIType
    {

        protected _AttributeAbstract(final String _uuid)
        {
            super(_uuid);
        }
        public final CIAttribute Creator = new CIAttribute(this, "Creator");
        public final CIAttribute Created = new CIAttribute(this, "Created");
        public final CIAttribute Modifier = new CIAttribute(this, "Modifier");
        public final CIAttribute Modified = new CIAttribute(this, "Modified");
    }

}
