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
package org.efaps.db.search.compare;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.efaps.db.AbstractTypeQuery;
import org.efaps.db.search.AbstractQPart;
import org.efaps.db.search.QAttribute;
import org.efaps.db.search.value.AbstractQValue;
import org.efaps.db.search.value.QBitValue;
import org.efaps.db.wrapper.SQLPart;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.util.EFapsException;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 *
 */
public class QEqual
    extends AbstractQAttrCompare
{

   /**
    * The values the given attribute must be equal to.
    */
    private final List<AbstractQValue> values = new ArrayList<>();

    /**
     * Constructor setting attribute and value.
     * @param _attribute Attribute to be checked for equal
     * @param _values    values as criteria
     */
    public QEqual(final QAttribute _attribute,
                  final AbstractQValue... _values)
    {
        super(_attribute, null);
        for (final AbstractQValue value : _values) {
            values.add(value);
        }
    }

    /**
     * Get the first value from the value list.
     *
     * @return null if list is empty else first value
     */
    @Override
    public AbstractQValue getValue()
    {
        return values.isEmpty() ? null : values.get(0);
    }

    /**
     * Getter method for the instance variable {@link #values}.
     *
     * @return value of instance variable {@link #values}
     */
    public List<AbstractQValue> getValues()
    {
        return values;
    }

    /**
     * Add a value to be included in the equal.
     * @param _value value to be include
     * @return this
     */
    public AbstractQPart addValue(final AbstractQValue _value)
    {
        values.add(_value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QEqual appendSQL(final SQLSelect _sql)
        throws EFapsException
    {
        getAttribute().appendSQL(_sql);
        if (values.size() > 1
                        || values.size() > 0 && values.get(0) instanceof QBitValue) {
            _sql.addPart(SQLPart.IN).addPart(SQLPart.PARENTHESIS_OPEN);
            boolean first = true;
            for (final AbstractQValue value : values) {
                if (first) {
                    first = false;
                } else {
                    _sql.addPart(SQLPart.COMMA);
                }
                value.appendSQL(_sql);
            }
            _sql.addPart(SQLPart.PARENTHESIS_CLOSE);
        } else if (getValue() != null){
            _sql.addPart(SQLPart.EQUAL);
            getValue().appendSQL(_sql);
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QEqual prepare(final AbstractTypeQuery _query,
                          final AbstractQPart _part)
        throws EFapsException
    {
        getAttribute().prepare(_query, this);
        for (final AbstractQValue value : values) {
            value.prepare(_query, this);
        }
        return this;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this).append("values", values).toString();
    }
}
