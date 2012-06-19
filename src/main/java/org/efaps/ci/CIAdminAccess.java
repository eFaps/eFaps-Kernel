/*
 * Copyright 2003 - 2012 The eFaps Team
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
}
