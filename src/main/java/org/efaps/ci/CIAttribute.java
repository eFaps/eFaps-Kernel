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

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 *
 */
public class CIAttribute
{
    //CHECKSTYLE:OFF
    /**
     * Name of the attribute.
     */
    public final String name;

    /**
     * Type this attribute belongs to.
     */
    public final CIType ciType;

    /**
     * Profiles this attribute is related to.
     */
    public final String[] profiles;
    //CHECKSTYLE:ON

    /**
     * @param _type         type this attribute belongs to
     * @param _name         name of this attribute
     */
    public CIAttribute(final CIType _type,
                       final String _name)
    {
        this(_type, _name, (String[]) null);
    }

    /**
     * @param _type         type this attribute belongs to
     * @param _name         name of this attribute
     * @param _profiles     Profiles this attribute is related to.
     */
    public CIAttribute(final CIType _type,
                       final String _name,
                       final String... _profiles)
    {
        this.ciType = _type;
        this.name = _name;
        this.profiles = _profiles;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this).append("name", this.name).append("ciType", this.ciType).toString();
    }
}
