/*
 * Copyright 2003 - 2013 The eFaps Team
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
public class CIAdmin
{

    public static final _Abstract Abstract = new _Abstract("2a869f46-0ec7-4afb-98e7-8b1125e1c43c");

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
        public final CIAttribute Purpose = new CIAttribute(this, "Purpose");
        public final CIAttribute Creator = new CIAttribute(this, "Creator");
        public final CIAttribute Created = new CIAttribute(this, "Created");
        public final CIAttribute Modifier = new CIAttribute(this, "Modifier");
        public final CIAttribute Modified = new CIAttribute(this, "Modified");
    }

    public static final _Langauge Language = new _Langauge("56851b99-c5fe-41f5-9e95-d7d94137acc1");

    public static class _Langauge
        extends CIType
    {

        protected _Langauge(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute Language = new CIAttribute(this, "Language");

    }

}
