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
package org.efaps.admin.index;

import org.efaps.admin.datamodel.IEnum;

/**
 * The Enum FieldType.
 *
 * @author The eFaps Team
 */
public enum FieldType
    implements IEnum
{

    /** {@link org.apache.lucene.document.StringField} not Stored. */
    SEARCHLONG,

    /** {@link org.apache.lucene.document.StringField}. */
    LONG,

    /** {@link org.apache.lucene.document.StringField} not Stored. */
    SEARCHSTRING,

    /** {@link org.apache.lucene.document.StringField}. */
    STRING,

    /** {@link org.apache.lucene.document.TextField} not Stored. */
    SEARCHTEXT,

    /** {@link org.apache.lucene.document.TextField}. */
    TEXT,

    /** {@link org.apache.lucene.document.StoredField}. */
    STORED;

    /**
     * {@inheritDoc}
     */
    @Override
    public int getInt()
    {
        return ordinal();
    }
}
