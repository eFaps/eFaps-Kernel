/*
 * Copyright 2003 - 2018 The eFaps Team
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

package org.efaps.eql.builder;

import org.efaps.db.Instance;

/**
 * The Class Converter.
 */
public final class Converter
{

    /**
     * Convert.
     *
     * @param _value the value
     * @return the string
     */
    public static String convert(final Object _value) {
        String ret = null;
        if (_value instanceof String) {
            ret = (String) _value;
        } else if (_value instanceof Instance) {
            ret = ((Instance) _value).getOid();
        }
        return ret;
    }
}
