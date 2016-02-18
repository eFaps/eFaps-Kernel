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


package org.efaps.util;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public final class UUIDUtil
{
    /**
     * Regex for testing a UUID for valid.
     */
    //CHECKSTYLE:OFF
    public static final String UUID_REGEX = "[0-9A-Fa-f]{8}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{12}";
    //CHECKSTYLE:ON

    /**
     * Singelton.
     */
    private UUIDUtil()
    {
    }

    /**
     * @param _string string to validate
     * @return true if valid UUID else false.
     */
    public static boolean isUUID(final String _string)
    {
        return _string.matches(UUID_REGEX);
    }
}
