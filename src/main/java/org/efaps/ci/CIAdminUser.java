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

package org.efaps.ci;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 *
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
        public final CIAttribute Note = new CIAttribute(this, "Note");
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

    public static final _Association Association = new _Association("f941d6f2-2554-421a-8c6e-c109067b1c22");

    public static class _Association
        extends CIType
    {

        protected _Association(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute RoleLink = new CIAttribute(this, "RoleLink");
        public final CIAttribute GroupLink = new CIAttribute(this, "GroupLink");
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

    public static final _Group Group = new _Group("f5e1e2ff-bfa9-40d9-8340-a259f48d5ad9");

    public static class _Group
        extends _Abstract
    {
        protected _Group(final String _uuid)
        {
            super(_uuid);
        }
    }

    public static final _Company Company = new _Company("6a5388e9-7f7f-4bc0-b7a0-3245302faad5");

    public static class _Company
        extends _Abstract
    {
        protected _Company(final String _uuid)
        {
            super(_uuid);
        }
    }

    public static final _RoleAbstract RoleAbstract = new _RoleGlobal("2f970c5c-41be-444f-b129-e6779e24c3fa");

    public static class _RoleAbstract
        extends _Abstract
    {
        protected _RoleAbstract(final String _uuid)
        {
            super(_uuid);
        }
    }

    public static final _RoleGlobal RoleGlobal = new _RoleGlobal("fe9d94fd-2ed8-4c44-b1f0-00e150555888");

    public static class _RoleGlobal
        extends _RoleAbstract
    {
        protected _RoleGlobal(final String _uuid)
        {
            super(_uuid);
        }
    }

    public static final _RoleLocal RoleLocal = new _RoleLocal("ae62fa23-6f5d-40e7-b1aa-d977fa4f188d");

    public static class _RoleLocal
        extends _RoleAbstract
    {
        protected _RoleLocal(final String _uuid)
        {
            super(_uuid);
        }
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

    public static final _Person2Group Person2Group = new _Person2Group("fec64148-a39b-4f69-bedd-c3bcfe8e1602");

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

    public static final _Person2Company Person2Company = new _Person2Company("a79898fb-966a-44ee-a338-d034e2aad83a");

    public static class _Person2Company
        extends _Abstract2Abstract
    {

        protected _Person2Company(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute UserLink = new CIAttribute(this, "UserLink");
        public final CIAttribute CompanyLink = new CIAttribute(this, "CompanyLink");

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
