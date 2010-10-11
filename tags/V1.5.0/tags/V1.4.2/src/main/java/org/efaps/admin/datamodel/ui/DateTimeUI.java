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

package org.efaps.admin.datamodel.ui;

import org.efaps.db.Context;
import org.efaps.util.EFapsException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Class to represent a DateTime for the user interface.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class DateTimeUI
    extends AbstractUI
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     *  {@inheritDoc}
     */
    @Override
    public String getReadOnlyHtml(final FieldValue _fieldValue)
        throws EFapsException
    {
        String ret = null;

        if (_fieldValue.getValue() instanceof DateTime) {
            final DateTime datetime = (DateTime) _fieldValue.getValue();
            if (datetime != null) {
                final DateTimeFormatter formatter = DateTimeFormat.mediumDateTime();
                // format the Date with the Locale and Chronology from the user
                // context
                ret = datetime.withChronology(Context.getThreadContext().getChronology()).toString(
                                formatter.withLocale(Context.getThreadContext().getLocale()));
            }
        } else if (_fieldValue.getValue() != null) {
            throw new EFapsException(this.getClass(), "getViewHtml.noDateTime", (Object[]) null);
        }
        return ret;
    }

    /**
     *  {@inheritDoc}
     */
    @Override
    public Object getObject4Compare(final FieldValue _fieldValue)
        throws EFapsException
    {
        return _fieldValue.getValue();
    }

    /**
     *  {@inheritDoc}
     */
    @Override
    public int compare(final FieldValue _fieldValue,
                       final FieldValue _fieldValue2)
    {
        int ret = 0;
        if (_fieldValue.getValue() instanceof DateTime && _fieldValue2.getValue() instanceof DateTime) {
            ret = DateTimeComparator.getInstance().compare(_fieldValue.getValue(), _fieldValue2.getValue());
        }
        return ret;
    }
}
