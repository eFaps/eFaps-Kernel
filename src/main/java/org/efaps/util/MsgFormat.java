/*
 * Copyright 2003 - 2014 The eFaps Team
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

package org.efaps.util;

import java.util.Locale;

import org.apache.commons.lang3.text.ExtendedMessageFormat;
import org.efaps.db.Context;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class MsgFormat
{

    public static ExtendedMessageFormat getFormat(final String _pattern)
        throws EFapsException
    {
        return getFormat(_pattern, Context.getThreadContext().getLocale());
    }

    public static ExtendedMessageFormat getFormat(final String _pattern,
                                                  final Locale _locale)
    {
        return new ExtendedMessageFormat(_pattern, _locale);
    }
}
