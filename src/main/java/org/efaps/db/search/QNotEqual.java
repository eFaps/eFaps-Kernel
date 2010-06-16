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


package org.efaps.db.search;

import java.util.ArrayList;
import java.util.List;

import org.efaps.db.AbstractObjectQuery;
import org.efaps.util.EFapsException;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class QNotEqual
    extends QAbstractAttrCompare
{

   /**
    * The values the given attribute must be equal to.
    */
    private final List<QAbstractValue> values = new ArrayList<QAbstractValue>();

    /**
     * Constructor setting attribute and value.
     * @param _attribute Attribute to be checked for equal
     * @param _values    values as criteria
     */
    public QNotEqual(final QAttribute _attribute,
                  final QAbstractValue... _values)
    {
        super(_attribute, null);
        for (final QAbstractValue value : _values) {
            this.values.add(value);
        }
    }

    /**
     * Get the first value from the value list.
     *
     * @return null if list is empty else first value
     */
    @Override
    public QAbstractValue getValue()
    {
        return this.values.isEmpty() ? null : this.values.get(0);
    }

    /**
     * Getter method for the instance variable {@link #values}.
     *
     * @return value of instance variable {@link #values}
     */
    public List<QAbstractValue> getValues()
    {
        return this.values;
    }

    /**
     * Add a value to be included in the equal.
     * @param _value value to be include
     * @return this
     */
    public QAbstractPart addValue(final QAbstractValue _value)
    {
        this.values.add(_value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QNotEqual appendSQL(final StringBuilder _sql)
        throws EFapsException
    {
        getAttribute().appendSQL(_sql);
        if (this.values.size() > 1) {
            _sql.append(" NOT IN ( ");
            boolean first = true;
            for (final QAbstractValue value : this.values) {
                if (first) {
                    first = false;
                } else {
                    _sql.append(",");
                }
                value.appendSQL(_sql);
            }
            _sql.append(" )");
        } else {
            _sql.append(" != ");
            getValue().appendSQL(_sql);
        }
        return this;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public QNotEqual prepare(final AbstractObjectQuery<?> _query,
                                 final QAbstractPart _part)
        throws EFapsException
    {
        getAttribute().prepare(_query, this);
        for (final QAbstractValue value : this.values) {
            value.prepare(_query, this);
        }
        return this;
    }
}
