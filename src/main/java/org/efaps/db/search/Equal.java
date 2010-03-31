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

import org.efaps.db.Query;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class Equal
    extends AbstractPart
{
    /**
     * The attribute used for this equal.
     */
    private final QueryAttribute attribute;

   /**
    * The values the given attribute must be equal to.
    */
    private final List<AbstractValue> values = new ArrayList<AbstractValue>();

    /**
     * Constructor setting attribute and value.
     * @param _attribute Attribute to be checked for equal
     * @param _values    values as criteria
     */
    public Equal(final QueryAttribute _attribute,
                 final AbstractValue... _values)
    {
        this.attribute = _attribute;
        for (final AbstractValue value : _values) {
            this.values.add(value);
        }
    }

    /**
     * Getter method for the instance variable {@link #attribute}.
     *
     * @return value of instance variable {@link #attribute}
     */
    public QueryAttribute getQueryAttribute()
    {
        return this.attribute;
    }

    /**
     * Get the first valeu from th evalue list.
     *
     * @return null if list is empty else first value
     */
    public AbstractValue getValue()
    {
        return this.values.isEmpty() ? null : this.values.get(0);
    }

    /**
     * Getter method for the instance variable {@link #values}.
     *
     * @return value of instance variable {@link #values}
     */
    public List<AbstractValue> getValues()
    {
        return this.values;
    }

    /**
     * Add a value to be included in the equal.
     * @param _value value to be include
     * @return this
     */
    public AbstractPart addValue(final AbstractValue _value)
    {
        this.values.add(_value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractPart appendSQL(final StringBuilder _sql)
    {
        this.attribute.appendSQL(_sql);
        if (this.values.size() > 1) {
            _sql.append(" IN ( ");
            boolean first = true;
            for (final AbstractValue value : this.values) {
                if (first) {
                    first = false;
                } else {
                    _sql.append(",");
                }
                value.appendSQL(_sql);
            }
            _sql.append(" )");
        } else {
            _sql.append(" = ");
            getValue().appendSQL(_sql);
        }
        return this;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractPart prepare(final Query _query)
    {
        this.attribute.prepare(_query);
        for (final AbstractValue value : this.values) {
            value.prepare(_query);
        }
        return this;
    }
}
