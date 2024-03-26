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
package org.efaps.ci;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 *
 */
// CHECKSTYLE:OFF
public class CIAdminAccess
{

    public static final _Access4Object Access4Object = new _Access4Object("98d9b606-b1aa-4ae1-9f30-2cba0d99453b");
    public static class _Access4Object
        extends CIType
    {

        protected _Access4Object(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute TypeId = new CIAttribute(this, "TypeId");
        public final CIAttribute ObjectId = new CIAttribute(this, "ObjectId");
        public final CIAttribute PersonLink = new CIAttribute(this, "PersonLink");
        public final CIAttribute AccessSetLink = new CIAttribute(this, "AccessSetLink");
        public final CIAttribute Creator = new CIAttribute(this, "Creator");
        public final CIAttribute Created = new CIAttribute(this, "Created");
        public final CIAttribute Modifier = new CIAttribute(this, "Modifier");
        public final CIAttribute Modified = new CIAttribute(this, "Modified");
    }

    public static final _AccessSet AccessSet = new _AccessSet("40aa4ff1-4786-4169-9a34-b6fd9d8a75f1");
    public static class _AccessSet
        extends CIType
    {

        protected _AccessSet(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute Name = new CIAttribute(this, "Name");
        public final CIAttribute UUID = new CIAttribute(this, "UUID");
        public final CIAttribute Revision = new CIAttribute(this, "Revision");
        public final CIAttribute Creator = new CIAttribute(this, "Creator");
        public final CIAttribute Created = new CIAttribute(this, "Created");
        public final CIAttribute Modifier = new CIAttribute(this, "Modifier");
        public final CIAttribute Modified = new CIAttribute(this, "Modified");
    }

    public static final _AccessSet2Type AccessSet2Type = new _AccessSet2Type("e018708e-5213-4fe9-9b98-27f646b0882f");
    public static class _AccessSet2Type
        extends CIType
    {

        protected _AccessSet2Type(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute AccessSetLink = new CIAttribute(this, "AccessSetLink");
        public final CIAttribute AccessTypeLink = new CIAttribute(this, "AccessTypeLink");
        public final CIAttribute Creator = new CIAttribute(this, "Creator");
        public final CIAttribute Created = new CIAttribute(this, "Created");
        public final CIAttribute Modifier = new CIAttribute(this, "Modifier");
        public final CIAttribute Modified = new CIAttribute(this, "Modified");
    }

    public static final _AccessSet2UserAbstract AccessSet2UserAbstract = new _AccessSet2UserAbstract("92975695-7a98-4221-bc9a-75c3dcb0e152");
    public static class _AccessSet2UserAbstract
        extends CIType
    {

        protected _AccessSet2UserAbstract(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute AccessSetLink = new CIAttribute(this, "AccessSetLink");
        public final CIAttribute UserAbstractLink = new CIAttribute(this, "UserAbstractLink");
        public final CIAttribute Creator = new CIAttribute(this, "Creator");
        public final CIAttribute Created = new CIAttribute(this, "Created");
        public final CIAttribute Modifier = new CIAttribute(this, "Modifier");
        public final CIAttribute Modified = new CIAttribute(this, "Modified");
    }

    public static final _AccessSet2DataModelType AccessSet2DataModelType = new _AccessSet2DataModelType("2f05dbd6-1798-46b9-a13a-f279bff26219");
    public static class _AccessSet2DataModelType
        extends CIType
    {
        protected _AccessSet2DataModelType(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute AccessSetLink = new CIAttribute(this, "AccessSetLink");
        public final CIAttribute DataModelTypeLink = new CIAttribute(this, "DataModelTypeLink");
        public final CIAttribute Creator = new CIAttribute(this, "Creator");
        public final CIAttribute Created = new CIAttribute(this, "Created");
        public final CIAttribute Modifier = new CIAttribute(this, "Modifier");
        public final CIAttribute Modified = new CIAttribute(this, "Modified");
    }

    public static final _AccessSet2Status AccessSet2Status = new _AccessSet2Status("425db05e-1405-4524-ba8e-dce42b2820ad");
    public static class _AccessSet2Status
        extends CIType
    {
        protected _AccessSet2Status(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute AccessSetLink = new CIAttribute(this, "AccessSetLink");
        public final CIAttribute SatusLink = new CIAttribute(this, "SatusLink");
        public final CIAttribute Creator = new CIAttribute(this, "Creator");
        public final CIAttribute Created = new CIAttribute(this, "Created");
        public final CIAttribute Modifier = new CIAttribute(this, "Modifier");
        public final CIAttribute Modified = new CIAttribute(this, "Modified");
    }
}
