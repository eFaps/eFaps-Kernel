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

package org.efaps.db.search.compare;

import org.efaps.db.search.QAttribute;
import org.efaps.db.search.value.AbstractQValue;
import org.efaps.db.search.value.QStringValue;
import org.efaps.db.wrapper.SQLPart;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public class QMatch
    extends QEqual
{
    /**
     * Constructor setting attribute and value.
     * @param _attribute Attribute to be checked for greater
     * @param _values    values as criteria
     */
    public QMatch(final QAttribute _attribute,
                  final AbstractQValue... _values)
    {
        super(_attribute, _values);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QMatch appendSQL(final SQLSelect _sql)
        throws EFapsException
    {
        if (getValues().size() > 1) {
            boolean first = true;
            _sql.addPart(SQLPart.PARENTHESIS_OPEN);
            for (final AbstractQValue value :getValues()) {
                if (first) {
                    first = false;
                } else {
                    _sql.addPart(SQLPart.OR);
                }
                getAttribute().appendSQL(_sql);
                if (getValue() instanceof QStringValue) {
                    _sql.addPart(SQLPart.LIKE);
                } else {
                    _sql.addPart(SQLPart.EQUAL);
                }
                value.appendSQL(_sql);
            }
            _sql.addPart(SQLPart.PARENTHESIS_CLOSE);
        } else {
            getAttribute().appendSQL(_sql);
            if (getValue() instanceof QStringValue) {
                _sql.addPart(SQLPart.LIKE);
            } else {
                _sql.addPart(SQLPart.EQUAL);
            }
            getValue().appendSQL(_sql);
        }
        return this;
    }
}
