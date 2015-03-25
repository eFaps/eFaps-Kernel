/*
 * Copyright 2003 - 2013 The eFaps Team
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

import org.efaps.admin.datamodel.Status;
import org.efaps.db.AbstractObjectQuery;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.search.AbstractQPart;
import org.efaps.db.search.compare.AbstractQAttrCompare;
import org.efaps.db.search.compare.QEqual;
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
     * Activate / deactivate the escape for the given string.
     */
    private boolean noEscape = false;

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
            if (((AbstractQAttrCompare) _part).isIgnoreCase()) {
                this.value = this.value.toUpperCase(Context.getThreadContext().getLocale());
            }
            if (_part instanceof QMatch) {
                this.value = Context.getDbType().prepare4Match(this.value);
            }
            if (_part instanceof QEqual && ((QEqual) _part).getAttribute().getAttribute() != null) {
                // check if the string is an status key and must be converted in
                // a long
                if (((QEqual) _part).getAttribute().getAttribute().getParent().isCheckStatus()
                                && ((QEqual) _part).getAttribute().getAttribute().equals(
                                   ((QEqual) _part).getAttribute().getAttribute().getParent().getStatusAttribute())) {
                    final Status status = Status.find(
                                    ((QEqual) _part).getAttribute().getAttribute().getLink().getUUID(), this.value);
                    if (status != null) {
                        this.value = Long.valueOf(status.getId()).toString();
                        this.noEscape = true;
                    }
                 // check if the string is an oid and must be converted in a long
                } else if (((QEqual) _part).getAttribute().getAttribute().hasLink()) {
                    final Instance insTmp = Instance.get(this.value);
                    if (insTmp.isValid()) {
                        this.value = Long.valueOf(insTmp.getId()).toString();
                        this.noEscape = true;
                    }
                }
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
        if (this.noEscape) {
            _sql.addValuePart(this.value);
        } else {
            _sql.addEscapedValuePart(this.value);
        }
        return this;
    }
}
