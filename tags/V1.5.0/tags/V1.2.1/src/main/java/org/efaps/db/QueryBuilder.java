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
import org.efaps.admin.datamodel.Classification;
import org.efaps.admin.datamodel.Type;
import org.efaps.ci.CIAttribute;
import org.efaps.ci.CIType;
import org.efaps.db.search.QAbstractAttrCompare;
import org.efaps.db.search.QAbstractValue;
import org.efaps.db.search.QAnd;
import org.efaps.db.search.QAttribute;
import org.efaps.db.search.QBooleanValue;
import org.efaps.db.search.QClassEqual;
import org.efaps.db.search.QClassValue;
import org.efaps.db.search.QDateTimeValue;
import org.efaps.db.search.QEqual;
import org.efaps.db.search.QGreater;
import org.efaps.db.search.QLess;
import org.efaps.db.search.QMatch;
import org.efaps.db.search.QNotEqual;
import org.efaps.db.search.QNumberValue;
import org.efaps.db.search.QOr;
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
     * Is this QueryBuilder using or instead of and.
     */
    private boolean or = false;


    /**
     * @param _typeUUID     uuid of the type this query is based on
     */
    public QueryBuilder(final UUID _typeUUID)
    {
        this.typeUUID = _typeUUID;
    }

    /**
     * @param _type  type this query is based on
     */
    public QueryBuilder(final Type _type)
    {
        this.typeUUID = _type.getUUID();
    }

    /**
     * @param _ciType  ciType this query is based on
     */
    public QueryBuilder(final CIType _ciType)
    {
        this(_ciType.getType());
    }

    /**
     * @param _ciAttr Name of the attribute
     * @param _values    value to be included in the where
     * @return QEqual
     * @throws EFapsException on error
     */
    public QEqual addWhereAttrEqValue(final CIAttribute _ciAttr,
                                      final Object... _values)
        throws EFapsException
    {
        return addWhereAttrEqValue(_ciAttr.name, _values);
    }


    /**
     * @param _attrName Name of the attribute
     * @param _values    value to be included in the where
     * @return QEqual
     * @throws EFapsException on error
     */
    public QEqual addWhereAttrEqValue(final String _attrName,
                                      final Object... _values)
        throws EFapsException
    {
        final QEqual equal = new QEqual(new QAttribute(_attrName));
        this.compares.add(equal);
        for (final Object value : _values) {
            equal.addValue(getValue(value));
        }
        return equal;
    }

    /**
     * @param _attr     attribute
     * @param _values   value to be included in the where
     * @return QEqual
     * @throws EFapsException on error
     */
    public QEqual addWhereAttrEqValue(final Attribute _attr,
                                      final Object... _values)
        throws EFapsException
    {
        final QEqual equal = new QEqual(new QAttribute(_attr));
        this.compares.add(equal);
        for (final Object value : _values) {
            equal.addValue(getValue(value));
        }
        return equal;
    }

    /**
     * @param _ciAttr Name of the attribute
     * @param _values    value to be included in the where
     * @return QEqual
     * @throws EFapsException on error
     */
    public QNotEqual addWhereAttrNotEqValue(final CIAttribute _ciAttr,
                                            final Object... _values)
        throws EFapsException
    {
        return addWhereAttrNotEqValue(_ciAttr.name, _values);
    }


    /**
     * @param _attrName Name of the attribute
     * @param _values    value to be included in the where
     * @return QEqual
     * @throws EFapsException on error
     */
    public QNotEqual addWhereAttrNotEqValue(final String _attrName,
                                            final Object... _values)
        throws EFapsException
    {
        final QNotEqual equal = new QNotEqual(new QAttribute(_attrName));
        this.compares.add(equal);
        for (final Object value : _values) {
            equal.addValue(getValue(value));
        }
        return equal;
    }

    /**
     * @param _attr     attribute
     * @param _values   value to be included in the where
     * @return QEqual
     * @throws EFapsException on error
     */
    public QNotEqual addWhereAttrNotEqValue(final Attribute _attr,
                                            final Object... _values)
        throws EFapsException
    {
        final QNotEqual equal = new QNotEqual(new QAttribute(_attr));
        this.compares.add(equal);
        for (final Object value : _values) {
            equal.addValue(getValue(value));
        }
        return equal;
    }

    /**
     * @param _ciAttr   CIAttribute of the attribute
     * @param _value    value to be included in the where
     * @return QLess
     * @throws EFapsException on error
     */
    public QLess addWhereAttrLessValue(final CIAttribute _ciAttr,
                                       final Object _value)
        throws EFapsException
    {
        return addWhereAttrLessValue(_ciAttr.name, _value);
    }


    /**
     * @param _attrName name of the attribute
     * @param _value    value to be included in the where
     * @return QLess
     * @throws EFapsException on error
     */
    public QLess addWhereAttrLessValue(final String _attrName,
                                       final Object _value)
        throws EFapsException
    {
        final QLess ret = new QLess(new QAttribute(_attrName), getValue(_value));
        this.compares.add(ret);
        return ret;
    }

    /**
     * @param _attr     attribute
     * @param _value    value to be included in the where
     * @return QLess
     * @throws EFapsException on error
     */
    public QLess addWhereAttrLessValue(final Attribute _attr,
                                       final Object _value)
        throws EFapsException
    {
        final QLess ret = new QLess(new QAttribute(_attr), getValue(_value));
        this.compares.add(ret);
        return ret;
    }


    /**
     * @param _ciAttr   CIAttribute of the attribute
     * @param _value    value to be included in the where
     * @return QLess
     * @throws EFapsException on error
     */
    public QGreater addWhereAttrGreaterValue(final CIAttribute _ciAttr,
                                             final Object _value)
        throws EFapsException
    {
        return addWhereAttrGreaterValue(_ciAttr.name, _value);
    }


    /**
     * @param _attrName name of the attribute
     * @param _value    value to be included in the where
     * @return QGreater
     * @throws EFapsException on error
     */
    public QGreater addWhereAttrGreaterValue(final String _attrName,
                                             final Object _value)
        throws EFapsException
    {
        final QGreater ret = new QGreater(new QAttribute(_attrName), getValue(_value));
        this.compares.add(ret);
        return ret;
    }

    /**
     * @param _attr     attribute
     * @param _value    value to be included in the where
     * @return QGreater
     * @throws EFapsException on error
     */
    public QGreater addWhereAttrGreaterValue(final Attribute _attr,
                                             final Object _value)
        throws EFapsException
    {
        final QGreater ret = new QGreater(new QAttribute(_attr), getValue(_value));
        this.compares.add(ret);
        return ret;
    }

    /**
     * @param _ciAttr   CIAttribute of the attribute
     * @param _value    value to be included in the where
     * @return QLess
     * @throws EFapsException on error
     */
    public QMatch addWhereAttrMatchValue(final CIAttribute _ciAttr,
                                             final Object _value)
        throws EFapsException
    {
        return addWhereAttrMatchValue(_ciAttr.name, _value);
    }

    /**
     * @param _attr     attribute
     * @param _value    value to be included in the where
     * @return QMatch
     * @throws EFapsException on error
     */
    public QMatch addWhereAttrMatchValue(final Attribute _attr,
                                         final Object _value)
        throws EFapsException
    {
        final QMatch ret = new QMatch(new QAttribute(_attr), getValue(_value));
        this.compares.add(ret);
        return ret;
    }

    /**
     * @param _attrName name of the attribute
     * @param _value    value to be included in the where
     * @return QMatch
     * @throws EFapsException on error
     */
    public QMatch addWhereAttrMatchValue(final String _attrName,
                                         final Object _value)
        throws EFapsException
    {
        final QMatch ret = new QMatch(new QAttribute(_attrName), getValue(_value));
        this.compares.add(ret);
        return ret;
    }

    /**
     * @param _classes    value to be included in the where
     * @return QClassEqual
     * @throws EFapsException on error
     */
    public QClassEqual addWhereClassification(final Classification... _classes)
        throws EFapsException
    {
        final QClassEqual ret = new QClassEqual();
        for (final Classification clazz : _classes) {
            ret.addValue(new QClassValue(clazz));
        }
        this.compares.add(ret);
        return ret;
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
        } else if (_value instanceof Boolean) {
            ret = new QBooleanValue((Boolean) _value);
        } else {
            throw new EFapsException(QueryBuilder.class, "notsuported");
        }
        return ret;
    }

    /**
     * Getter method for the instance variable {@link #or}.
     *
     * @return value of instance variable {@link #or}
     */
    public boolean isOr()
    {
        return this.or;
    }

    /**
     * Setter method for instance variable {@link #or}.
     *
     * @param _or value for instance variable {@link #or}
     * @return this
     */
    public QueryBuilder setOr(final boolean _or)
    {
        this.or = _or;
        return this;
    }

    /**
     * Get the constructed query.
     * @return the query
     */
    public InstanceQuery getQuery()
    {
        if (this.query == null) {
            this.query = new InstanceQuery(this.typeUUID);
            if (!this.compares.isEmpty()) {
                final QAnd and = this.or ? new QOr() : new QAnd();
                for (final QAbstractAttrCompare compare : this.compares) {
                    and.addPart(compare);
                }
                this.query.setWhere(new QWhere(and));
            }
        }
        return this.query;
    }

    /**
     * Method to get a MultiPrintQuery.
     * @return MultiPrintQuery based on the InstanceQuery
     * @throws EFapsException on error
     */
    public MultiPrintQuery getPrint()
        throws EFapsException
    {
        return new MultiPrintQuery(getQuery().execute());
    }
}
