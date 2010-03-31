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


package org.efaps.db;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.db.search.QAbstractAttrCompare;
import org.efaps.db.search.QAbstractValue;
import org.efaps.db.search.QAnd;
import org.efaps.db.search.QAttribute;
import org.efaps.db.search.QDateTimeValue;
import org.efaps.db.search.QEqual;
import org.efaps.db.search.QGreater;
import org.efaps.db.search.QLess;
import org.efaps.db.search.QNumberValue;
import org.efaps.db.search.QStringValue;
import org.efaps.db.search.QWhere;
import org.efaps.util.EFapsException;
import org.joda.time.DateTime;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class QueryBuilder
{

    /**
     * List of compares that will be included in this query.
     */
    private final List<QAbstractAttrCompare> compares = new ArrayList<QAbstractAttrCompare>();

    /**
     * UUID of th etype used for the instance query.
     */
    private final UUID typeUUID;

    /**
     * Query this QueryBuilder will return.
     */
    private InstanceQuery query;

    /**
     * @param _typeUUID     uuid of the type this query is based on
     */
    public QueryBuilder(final UUID _typeUUID)
    {
        this.typeUUID = _typeUUID;
    }

    /**
     * @param _attrName Name of the attribute
     * @param _values    value to be included in the where
     * @throws EFapsException on error
     */
    public void addWhereAttrEqValue(final String _attrName,
                                    final Object... _values)
        throws EFapsException
    {
        final QEqual equal = new QEqual(new QAttribute(_attrName));
        this.compares.add(equal);
        for (final Object value : _values) {
            equal.addValue(getValue(value));
        }
    }

    /**
     * @param _attr     attribute
     * @param _values   value to be included in the where
     * @throws EFapsException on error
     */
    public void addWhereAttrEqValue(final Attribute _attr,
                                    final Object... _values)
        throws EFapsException
    {
        final QEqual equal = new QEqual(new QAttribute(_attr));
        this.compares.add(equal);
        for (final Object value : _values) {
            equal.addValue(getValue(value));
        }
    }

    /**
     * @param _attrName name of the attribute
     * @param _value    value to be included in the where
     * @throws EFapsException on error
     */
    public void addWhereAttrLessValue(final String _attrName,
                                      final Object _value)
        throws EFapsException
    {
        this.compares.add(new QLess(new QAttribute(_attrName), getValue(_value)));
    }

    /**
     * @param _attr     attribute
     * @param _value    value to be included in the where
     * @throws EFapsException on error
     */
    public void addWhereAttrLessValue(final Attribute _attr,
                                      final Object _value)
        throws EFapsException
    {
        this.compares.add(new QLess(new QAttribute(_attr), getValue(_value)));
    }

    /**
     * @param _attrName name of the attribute
     * @param _value    value to be included in the where
     * @throws EFapsException on error
     */
    public void addWhereAttrGreaterValue(final String _attrName,
                                         final Object _value)
        throws EFapsException
    {
        this.compares.add(new QGreater(new QAttribute(_attrName), getValue(_value)));
    }

    /**
     * @param _attr     attribute
     * @param _value    value to be included in the where
     * @throws EFapsException on error
     */
    public void addWhereAttrGreaterValue(final Attribute _attr,
                                         final Object _value)
        throws EFapsException
    {
        this.compares.add(new QGreater(new QAttribute(_attr), getValue(_value)));
    }


    /**
     * Get the QAbstractValue for a value.
     * @param _value    value the QAbstractValue is wanted for
     * @return  QAbstractValue
     * @throws EFapsException on error
     */
    private QAbstractValue getValue(final Object _value)
        throws EFapsException
    {
        QAbstractValue ret = null;
        if (_value instanceof Number) {
            ret = new QNumberValue((Number) _value);
        } else if (_value instanceof String) {
            ret = new QStringValue((String) _value);
        } else if (_value instanceof DateTime) {
            ret = new QDateTimeValue((DateTime) _value);
        } else {
            throw new EFapsException(QueryBuilder.class, "notsuported");
        }
        return ret;
    }

    /**
     * Get the constructed query.
     * @return the query
     */
    public InstanceQuery getQuery()
    {
        if (this.query == null) {
            this.query = new InstanceQuery(this.typeUUID);
            final QAnd and = new QAnd();
            for (final QAbstractAttrCompare compare : this.compares) {
                and.addPart(compare);
            }
            this.query.setWhere(new QWhere(and));
        }
        return this.query;
    }
}
