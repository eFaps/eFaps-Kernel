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

package org.efaps.db.stmt.selection.elements;

import org.efaps.db.Context;
import org.efaps.util.EFapsException;
import org.joda.time.base.AbstractDateTime;

public class FormatElement
    extends AbstractElement<FormatElement>
    implements IAuxillary
{
    private String pattern;

    @Override
    public FormatElement getThis()
    {
        return this;
    }

    @Override
    public Object getObject(final Object[] _row)
        throws EFapsException
    {
        Object object = _row == null ? null : _row[0];
        if (object != null && object instanceof AbstractDateTime) {
            object = ((AbstractDateTime) object).toString(this.pattern, Context.getThreadContext().getLocale());
        }
        return object;
    }

    public FormatElement setPattern(final String _value)
    {
        this.pattern = _value;
        return this;
    }
}
