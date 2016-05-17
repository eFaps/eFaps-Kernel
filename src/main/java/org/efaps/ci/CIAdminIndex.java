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
public class CIAdminIndex
{

    public static final _IndexDefinition IndexDefinition = new _IndexDefinition("d9cc4301-7b29-4946-8d1a-4b1305c8aa5c");

    public static class _IndexDefinition
        extends CIType
    {

        protected _IndexDefinition(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute TypeLink = new CIAttribute(this, "TypeLink");
        public final CIAttribute MsgPhraseLink = new CIAttribute(this, "MsgPhraseLink");
        public final CIAttribute Active = new CIAttribute(this, "Active");
        public final CIAttribute Creator = new CIAttribute(this, "Creator");
        public final CIAttribute Created = new CIAttribute(this, "Created");
        public final CIAttribute Modifier = new CIAttribute(this, "Modifier");
        public final CIAttribute Modified = new CIAttribute(this, "Modified");
    }

    public static final _IndexField IndexField = new _IndexField("ab470ba9-29dc-40b1-beb4-cd54f0f7133a");

    public static class _IndexField
        extends CIType
    {

        protected _IndexField(final String _uuid)
        {
            super(_uuid);
        }

        public final CIAttribute DefinitionLink = new CIAttribute(this, "DefinitionLink");
        public final CIAttribute Identifier = new CIAttribute(this, "Identifier");
        public final CIAttribute Key = new CIAttribute(this, "Key");
        public final CIAttribute Select = new CIAttribute(this, "Select");
        public final CIAttribute FieldType = new CIAttribute(this, "FieldType");
        public final CIAttribute TransformerLink = new CIAttribute(this, "TransformerLink");
        public final CIAttribute Creator = new CIAttribute(this, "Creator");
        public final CIAttribute Created = new CIAttribute(this, "Created");
        public final CIAttribute Modifier = new CIAttribute(this, "Modifier");
        public final CIAttribute Modified = new CIAttribute(this, "Modified");
    }
}
