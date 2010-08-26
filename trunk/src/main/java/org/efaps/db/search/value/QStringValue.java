/*
 * Copyright 2003 - 2010 The eFaps Team
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


package org.efaps.db.search.value;

import org.efaps.db.AbstractObjectQuery;
import org.efaps.db.Context;
import org.efaps.db.search.AbstractQPart;
import org.efaps.db.search.compare.AbstractQAttrCompare;
import org.efaps.db.search.compare.QMatch;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class QStringValue
    extends AbstractQValue
{
    /**
     * Value for this StringValue.
     */
    private String value;

    /**
     * @param _value Value
     */
    public QStringValue(final String _value)
    {
        this.value = _value;
    }

    /**
     * {@inheritDoc}
     *
     */
    @Override
    public QStringValue prepare(final AbstractObjectQuery<?> _query,
                                 final AbstractQPart _part)
        throws EFapsException
    {
        if (_part instanceof AbstractQAttrCompare) {
            if (_part instanceof QMatch) {
                this.value = this.value.replace("*", "%");
            }
            if (((AbstractQAttrCompare) _part).isIgnoreCase()) {
                this.value = this.value.toUpperCase(Context.getThreadContext().getLocale());
            }
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QStringValue appendSQL(final SQLSelect _sql)
    {
        _sql.addEscapedValuePart(this.value);
        return this;
    }
}
