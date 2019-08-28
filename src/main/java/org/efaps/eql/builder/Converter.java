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

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

import org.efaps.db.Instance;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class Converter.
 */
public final class Converter
{
    private static final Logger LOG = LoggerFactory.getLogger(Converter.class);

    /**
     * Convert.
     *
     * @param _value the value
     * @return the string
     * @throws EFapsException
     */
    public static String convert(final Object _value) throws EFapsException {
        String ret = null;
        if (_value instanceof String) {
            ret = (String) _value;
        } else if (_value instanceof Instance) {
            ret = ((Instance) _value).getOid();
        } else if (_value instanceof Number) {
            ret = ((Number) _value).toString();
        } else if (_value instanceof LocalDate) {
            ret = ((LocalDate) _value).toString();
        } else if (_value instanceof LocalTime) {
            ret = ((LocalTime) _value).toString();
        } else if (_value instanceof OffsetDateTime) {
            ret = ((OffsetDateTime) _value).toString();
        } else {
            LOG.warn("No specific converter defined for: {}", _value);
            ret = String.valueOf(_value);
        }
        return ret;
    }
}
