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

import org.efaps.admin.datamodel.Classification;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.AbstractObjectQuery;
import org.efaps.db.Context;
import org.efaps.util.EFapsException;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class QClassEqual
    extends QAbstractAttrCompare
{

   /**
    * The values the given attribute must be equal to.
    */
    private final List<QClassValue> values = new ArrayList<QClassValue>();

    /**
     * Constructor setting attribute and value.
     * @param _values    values as criteria
     */
    public QClassEqual(final QClassValue... _values)
    {
        super(new QAttribute("ID"), null);
        for (final QClassValue value : _values) {
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
    public List<QClassValue> getValues()
    {
        return this.values;
    }

    /**
     * Add a value to be included in the equal.
     * @param _value value to be include
     * @return this
     */
    public QAbstractPart addValue(final QClassValue _value)
    {
        this.values.add(_value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QClassEqual prepare(final AbstractObjectQuery<?> _query,
                                 final QAbstractPart _part)
        throws EFapsException
    {
        getAttribute().prepare(_query, this);
        for (final QClassValue value : this.values) {
            value.prepare(_query, this);
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QClassEqual appendSQL(final StringBuilder _sql)
        throws EFapsException
    {
        getAttribute().appendSQL(_sql);
        final Classification clazz = this.values.get(0).getClassification();
        final Type relType = clazz.getClassifyRelationType();
        _sql.append(" in ( select ")
            .append(Context.getDbType().getColumnQuote())
            .append(relType.getAttribute(clazz.getRelLinkAttributeName()).getSqlColNames().get(0))
            .append(Context.getDbType().getColumnQuote())
            .append(" from ")
            .append(Context.getDbType().getTableQuote())
            .append(relType.getMainTable().getSqlTable())
            .append(Context.getDbType().getTableQuote())
            .append(" where ")
            .append(Context.getDbType().getTableQuote())
            .append(relType.getAttribute(clazz.getRelTypeAttributeName()).getSqlColNames().get(0))
            .append(Context.getDbType().getTableQuote());
        if (this.values.size() > 1) {
            _sql.append(" in ( ");
            boolean first = true;
            for (final QClassValue value : this.values) {
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
        _sql.append(" )");
        return this;
    }
}
