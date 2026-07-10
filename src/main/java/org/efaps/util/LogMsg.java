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
package org.efaps.util;

public class LogMsg
{

    private final StringBuilder bldr = new StringBuilder();
    private boolean suffix = false;
    private boolean first = true;

    public static LogMsg builder(final String type)
    {
        final var logMsg = new LogMsg();
        logMsg.bldr.append(type).append("[");
        return logMsg;
    }

    public LogMsg info(final String key,
                       final Object object)
    {
        if (first) {
            first = false;
        } else {
            bldr.append(", ");
        }
        Object value;
        if (object instanceof final IFormatedLog formatedLog) {
            value = formatedLog.logInfo();
        } else {
            value = object;
        }
        bldr.append(key).append("=").append(value);
        return this;
    }

    public String build()
    {
        if (!suffix) {
            bldr.append("]");
            suffix = true;
        }
        return bldr.toString();
    }
}
