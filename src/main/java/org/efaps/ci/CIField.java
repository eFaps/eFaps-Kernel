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
public class CIField
{
    //CHECKSTYLE:OFF
    /**
     * Name of the field.
     */
    public final String name;

    /**
     * Type this attribute belongs to.
     */
    public final CICollection ciCollection;

    /**
     * Profiles this field is related to.
     */
    public final String[] profiles;
    //CHECKSTYLE:ON

    /**
     * @param _ciCollection     Collection this field belongs to
     * @param _name             name of this field
     * @param _profiles         Profiles this field is related to.
     */
    public CIField(final CICollection _ciCollection,
                   final String _name,
                   final String... _profiles)
    {
        this.ciCollection = _ciCollection;
        this.name = _name;
        this.profiles = _profiles;
    }
}
