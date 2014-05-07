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


package org.efaps.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Classification;
import org.efaps.admin.datamodel.Dimension.UoM;
import org.efaps.admin.datamodel.IBitEnum;
import org.efaps.admin.datamodel.IEnum;
import org.efaps.admin.datamodel.Status;
import org.efaps.admin.datamodel.Type;
import org.efaps.ci.CIAttribute;
import org.efaps.ci.CIType;
import org.efaps.db.search.AbstractQPart;
import org.efaps.db.search.QAnd;
import org.efaps.db.search.QAttribute;
import org.efaps.db.search.QOr;
import org.efaps.db.search.QOrderAsc;
import org.efaps.db.search.QOrderDesc;
import org.efaps.db.search.compare.AbstractQAttrCompare;
import org.efaps.db.search.compare.QClassEqual;
import org.efaps.db.search.compare.QEqual;
import org.efaps.db.search.compare.QGreater;
import org.efaps.db.search.compare.QIn;
import org.efaps.db.search.compare.QIs;
import org.efaps.db.search.compare.QIsNot;
import org.efaps.db.search.compare.QLess;
import org.efaps.db.search.compare.QMatch;
import org.efaps.db.search.compare.QNotEqual;
import org.efaps.db.search.compare.QNotIn;
import org.efaps.db.search.section.QOrderBySection;
import org.efaps.db.search.section.QWhereSection;
import org.efaps.db.search.value.AbstractQValue;
import org.efaps.db.search.value.QBitValue;
import org.efaps.db.search.value.QBooleanValue;
import org.efaps.db.search.value.QClassValue;
import org.efaps.db.search.value.QDateTimeValue;
import org.efaps.db.search.value.QNullValue;
import org.efaps.db.search.value.QNumberValue;
import org.efaps.db.search.value.QSQLValue;
import org.efaps.db.search.value.QStringValue;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class QueryBuilder
{
    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(QueryBuilder.class);

    /**
     * List of compares that will be included in this query.
     */
    private final List<AbstractQAttrCompare> compares = new ArrayList<AbstractQAttrCompare>();

    /**
     * List of parts that will be included in this order of this query.
     */
    private final List<AbstractQPart> orders = new ArrayList<AbstractQPart>();

    /**
     * UUID of the type used for generated instance query.
     */
    private UUID typeUUID;

    /**
     * List of type that should be included.
     */
    private final Set<UUID> types = new LinkedHashSet<UUID>();

    /**
     * Query this QueryBuilder will return.
     */
    private AbstractObjectQuery<?> query;

    /**
     * Is this QueryBuilder using or instead of and.
     */
    private boolean or = false;

    /**
     * List for the query to be executed, defaults to none.
     */
    private int limit = -1;


    /**
     * Should the child types be also be included in this search?
     */
    private boolean includeChildTypes = true;

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
        this(_type.getUUID());
    }

    /**
     * @param _ciType  ciType this query is based on
     */
    public QueryBuilder(final CIType _ciType)
    {
        this(_ciType.getType());
    }

    /**
     * Add a type to the QueryBuilder. Search for the common parent
     * and use it as baseType.
     * @param _uuid uudi of the types to be added to the QueryBuilder
     * @throws EFapsException on error
     */
    public void addType(final UUID... _uuid)
        throws EFapsException
    {
        final Set<Type> typesTmp = new HashSet<Type>();
        for (final UUID uuid : _uuid) {
            typesTmp.add(Type.get(uuid));
        }
        addType(typesTmp.toArray(new Type[typesTmp.size()]));
    }

    /**
     * Add a type to the QueryBuilder. Search for the common parent
     * and use it as baseType.
     * @param _ciType CIType of the types to be added to the QueryBuilder
     * @throws EFapsException on error
     */
    public void addType(final CIType... _ciType)
        throws EFapsException
    {
        final Set<Type> typesTmp = new HashSet<Type>();
        for (final CIType ciType : _ciType) {
            typesTmp.add(ciType.getType());
        }
        addType(typesTmp.toArray(new Type[typesTmp.size()]));
    }

    /**
     * Add a type to the QueryBuilder. Search for the common parent
     * and use it as baseType.
     * @param _type types to be added to the QueryBuilder
     * @throws EFapsException on error
     */
    public void addType(final Type... _type)
        throws EFapsException
    {
        final List<Type> allType = new ArrayList<Type>();
        if (this.types.isEmpty()) {
            allType.add(Type.get(this.typeUUID));
        }
        for (final UUID type : this.types) {
            allType.add(Type.get(type));
        }
        Collections.addAll(allType, _type);

        //make for every type a list of types up to the parent
        final List<List<Type>> typeLists = new ArrayList<List<Type>>();
        for (final Type type : allType) {
            final List<Type> typesTmp = new ArrayList<Type>();
            typeLists.add(typesTmp);
            Type tmpType = type;
            while (tmpType != null) {
                typesTmp.add(tmpType);
                tmpType = tmpType.getParentType();
            }
        }

        final Set<Type> common = new LinkedHashSet<Type>();
        if (!typeLists.isEmpty()) {
            final Iterator<List<Type>> iterator = typeLists.iterator();
            common.addAll(iterator.next());
            while (iterator.hasNext()) {
                common.retainAll(iterator.next());
            }
        }
        if (common.isEmpty()) {
            throw new EFapsException(QueryBuilder.class, "noCommon", allType);
        } else {
            // first common type
            this.typeUUID = common.iterator().next().getUUID();
            for (final Type type : allType) {
                this.types.add(type.getUUID());
            }
        }
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
     * @param _ciAttr Name of the attribute
     * @param _query    the query
     * @return QEqual
     * @throws EFapsException on error
     */
    public QIn addWhereAttrInQuery(final CIAttribute _ciAttr,
                                   final AttributeQuery _query)
        throws EFapsException
    {
        return addWhereAttrInQuery(_ciAttr.name, _query);
    }

    /**
     * @param _attrName Name of the attribute
     * @param _query    the query
     * @return QEqual
     * @throws EFapsException on error
     */
    public QIn addWhereAttrInQuery(final String _attrName,
                                   final AttributeQuery _query)
        throws EFapsException
    {
        final QIn in = new QIn(new QAttribute(_attrName), new QSQLValue(_query.getSQLStatement()));
        this.compares.add(in);
        return in;
    }

    /**
     * @param _attr     attribute
     * @param _query    the query
     * @return QEqual
     * @throws EFapsException on error
     */
    public QIn addWhereAttrEqValue(final Attribute _attr,
                                      final AttributeQuery _query)
        throws EFapsException
    {
        final QIn in = new QIn(new QAttribute(_attr), new QSQLValue(_query.getSQLStatement()));
        this.compares.add(in);
        return in;
    }

    /**
     * @param _ciAttr Name of the attribute
     * @param _query    the query
     * @return QEqual
     * @throws EFapsException on error
     */
    public QNotIn addWhereAttrNotInQuery(final CIAttribute _ciAttr,
                                         final AttributeQuery _query)
        throws EFapsException
    {
        return addWhereAttrNotInQuery(_ciAttr.name, _query);
    }


    /**
     * @param _attrName Name of the attribute
     * @param _query    the query
     * @return QEqual
     * @throws EFapsException on error
     */
    public QNotIn addWhereAttrNotInQuery(final String _attrName,
                                      final AttributeQuery _query)
        throws EFapsException
    {
        final QNotIn in = new QNotIn(new QAttribute(_attrName), new QSQLValue(_query.getSQLStatement()));
        this.compares.add(in);
        return in;
    }

    /**
     * @param _attr     attribute
     * @param _query    the query
     * @return QEqual
     * @throws EFapsException on error
     */
    public QNotIn addWhereAttrNotInQuery(final Attribute _attr,
                                         final AttributeQuery _query)
        throws EFapsException
    {
        final QNotIn in = new QNotIn(new QAttribute(_attr), new QSQLValue(_query.getSQLStatement()));
        this.compares.add(in);
        return in;
    }

    /**
     * @param _ciAttr Name of the attribute
     * @return QEqual
     * @throws EFapsException on error
     */
    public QIs addWhereAttrIsNull(final CIAttribute _ciAttr)
        throws EFapsException
    {
        return addWhereAttrIsNull(_ciAttr.name);
    }


    /**
     * @param _attrName Name of the attribute
     * @return QEqual
     * @throws EFapsException on error
     */
    public QIs addWhereAttrIsNull(final String _attrName)
        throws EFapsException
    {
        final QIs in = new QIs(new QAttribute(_attrName), new QNullValue());
        this.compares.add(in);
        return in;
    }

    /**
     * @param _attr     attribute
     * @return QEqual
     * @throws EFapsException on error
     */
    public QIs addWhereAttrIsNull(final Attribute _attr)
        throws EFapsException
    {
        final QIs in = new QIs(new QAttribute(_attr), new QNullValue());
        this.compares.add(in);
        return in;
    }

    /**
     * @param _ciAttr Name of the attribute
     * @return QEqual
     * @throws EFapsException on error
     */
    public QIsNot addWhereAttrNotIsNull(final CIAttribute _ciAttr)
        throws EFapsException
    {
        return addWhereAttrNotIsNull(_ciAttr.name);
    }

    /**
     * @param _attrName Name of the attribute
     * @return QEqual
     * @throws EFapsException on error
     */
    public QIsNot addWhereAttrNotIsNull(final String _attrName)
        throws EFapsException
    {
        final QIsNot in = new QIsNot(new QAttribute(_attrName), new QNullValue());
        this.compares.add(in);
        return in;
    }

    /**
     * @param _attr     attribute
     * @return QEqual
     * @throws EFapsException on error
     */
    public QIsNot addWhereAttrNotIsNull(final Attribute _attr)
        throws EFapsException
    {
        final QIsNot in = new QIsNot(new QAttribute(_attr), new QNullValue());
        this.compares.add(in);
        return in;
    }

    /**
     * @param _ciAttr CIAttribute to be orderd by.
     * @return QOrderBySection
     */
    public QOrderAsc addOrderByAttributeAsc(final CIAttribute _ciAttr)
    {
        return addOrderByAttributeAsc(_ciAttr.name);
    }

    /**
     * @param _attrName Name of the Attributes to be orderd by.
     * @return QOrderBySection
     */
    public QOrderAsc addOrderByAttributeAsc(final String _attrName)
    {
        final QOrderAsc asc = new QOrderAsc(new QAttribute(_attrName));
        this.orders.add(asc);
        return asc;
    }

    /**
     * @param _attr Attribute to be orderd by.
     * @return QOrderBySection
     */
    public QOrderAsc addOrderByAttributeAsc(final Attribute _attr)
    {
        final QOrderAsc asc = new QOrderAsc(new QAttribute(_attr));
        this.orders.add(asc);
        return asc;
    }

    /**
     * @param _ciAttr CIAttribute to be orderd by.
     * @return QOrderBySection
     */
    public QOrderDesc addOrderByAttributeDesc(final CIAttribute _ciAttr)
    {
        return addOrderByAttributeDesc(_ciAttr.name);
    }

    /**
     * @param _attrName Name of the Attributes to be orderd by.
     * @return QOrderBySection
     */
    public QOrderDesc addOrderByAttributeDesc(final String _attrName)
    {
        final QOrderDesc desc = new QOrderDesc(new QAttribute(_attrName));
        this.orders.add(desc);
        return desc;
    }

    /**
     * @param _attr Attribute to be orderd by.
     * @return QOrderBySection
     */
    public QOrderDesc addOrderByAttributeDesc(final Attribute _attr)
    {
        final QOrderDesc desc = new QOrderDesc(new QAttribute(_attr));
        this.orders.add(desc);
        return desc;
    }




    /**
     * Get the QAbstractValue for a value.
     * @param _value    value the QAbstractValue is wanted for
     * @return  QAbstractValue
     * @throws EFapsException on error
     */
    private AbstractQValue getValue(final Object _value)
        throws EFapsException
    {
        AbstractQValue ret = null;
        if (_value == null) {
            ret = new QNullValue();
        } else if (_value instanceof Number) {
            ret = new QNumberValue((Number) _value);
        } else if (_value instanceof String) {
            ret = new QStringValue((String) _value);
        } else if (_value instanceof DateTime) {
            ret = new QDateTimeValue((DateTime) _value);
        } else if (_value instanceof Boolean) {
            ret = new QBooleanValue((Boolean) _value);
        } else if (_value instanceof Status) {
            ret = new QNumberValue(((Status) _value).getId());
        } else if (_value instanceof Instance) {
            if (!((Instance) _value).isValid()) {
                QueryBuilder.LOG.error("the given Instance was not valid and cannot be used as filter criteria",
                                _value);
                throw new EFapsException(QueryBuilder.class, "invalid Instance given");
            }
            ret = new QNumberValue(((Instance) _value).getId());
        } else if (_value instanceof UoM) {
            ret = new QNumberValue(((UoM) _value).getId());
        } else if (_value instanceof IBitEnum) {
            ret = new QBitValue((IBitEnum) _value);
        } else if (_value instanceof IEnum) {
            ret = new QNumberValue(((IEnum) _value).getInt());
        } else if (_value instanceof AbstractQValue) {
            ret = (AbstractQValue) _value;
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
     * Getter method for the instance variable {@link #limit}.
     *
     * @return value of instance variable {@link #limit}
     */
    public int getLimit()
    {
        return this.limit;
    }

    /**
     * Setter method for instance variable {@link #limit}.
     *
     * @param _limit value for instance variable {@link #limit}
     */
    public void setLimit(final int _limit)
    {
        this.limit = _limit;
    }

    /**
     * Getter method for the instance variable {@link #typeUUID}.
     *
     * @return value of instance variable {@link #typeUUID}
     */
    public final UUID getTypeUUID()
    {
        return this.typeUUID;
    }

    /**
     * Get the base type.
     *
     * @return the basetype of this QueryBuilder
     * @throws CacheReloadException on error
     */
    public final Type getType()
        throws CacheReloadException
    {
        return Type.get(this.typeUUID);
    }

    /**
     * Get the constructed query.
     * @return the query
     */
    public InstanceQuery getQuery()
    {
        if (this.query == null) {
            try {
                this.query = new InstanceQuery(this.typeUUID);
                this.query.setIncludeChildTypes(isIncludeChildTypes());
                prepareQuery();
            } catch (final CacheReloadException e) {
                QueryBuilder.LOG.error("Could not open InstanceQuery for uuid: {}", this.typeUUID);
            }
        }
        return (InstanceQuery) this.query;
    }

    /**
     * Get the constructed query.
     * @param _key key to the Query Cache
     * @return the query
     */
    public CachedInstanceQuery getCachedQuery(final String _key)
    {
        if (this.query == null) {
            try {
                this.query = new CachedInstanceQuery(_key, this.typeUUID);
                this.query.setIncludeChildTypes(isIncludeChildTypes());
                prepareQuery();
            } catch (final CacheReloadException e) {
                QueryBuilder.LOG.error("Could not open InstanceQuery for uuid: {}", this.typeUUID);
            }
        }
        return (CachedInstanceQuery) this.query;
    }

    /**
     * Method to get an Attribute Query.
     * @param _attribute attribute the value is wanted for,
     *        if null the id attribute will be used automatically
     * @return Attribute Query
     */
    public AttributeQuery getAttributeQuery(final CIAttribute _attribute)
    {
        return this.getAttributeQuery(_attribute.name);
    }

    /**
     * Method to get an Attribute Query.
     * @param _attributeName name of the attribute the value is wanted for,
     *        if null the id attribute will be used automatically
     * @return Attribute Query
     */
    public AttributeQuery getAttributeQuery(final String _attributeName)
    {
        if (this.query == null) {
            try {
                this.query = new AttributeQuery(this.typeUUID, _attributeName);
                prepareQuery();
            } catch (final CacheReloadException e) {
                QueryBuilder.LOG.error("Could not open AttributeQuery for uuid: {}", this.typeUUID);
            }
        }
        return (AttributeQuery) this.query;
    }

    /**
     * Prepare the Query.
     * @throws CacheReloadException on error
     */
    private void prepareQuery()
        throws CacheReloadException
    {
        if (!this.types.isEmpty()) {
            // force the include
            this.query.setIncludeChildTypes(true);
            final Type baseType = Type.get(this.typeUUID);
            final QEqual eqPart = new QEqual(new QAttribute(baseType.getTypeAttribute()));

            for (final UUID type : this.types) {
                eqPart.addValue(new QNumberValue(Type.get(type).getId()));
            }
            this.compares.add(eqPart);
        }

        if (!this.compares.isEmpty()) {
            final QAnd and = this.or ? new QOr() : new QAnd();
            for (final AbstractQAttrCompare compare : this.compares) {
                and.addPart(compare);
            }
            this.query.setWhere(new QWhereSection(and));
        }
        if (!this.orders.isEmpty()) {
            final QOrderBySection orderBy = new QOrderBySection(
                            this.orders.toArray(new AbstractQPart[this.orders.size()]));
            this.query.setOrderBy(orderBy);
        }
        if (this.limit > 0) {
            this.query.setLimit(this.limit);
        }
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

    /**
     * Method to get a CachedMultiPrintQuery.
     * @param _key key used for Caching
     * @return MultiPrintQuery based on the InstanceQuery
     * @throws EFapsException on error
     */
    public CachedMultiPrintQuery getCachedPrint(final String _key)
        throws EFapsException
    {
        return new CachedMultiPrintQuery(getCachedQuery(_key).execute(), _key);
    }

    /**
     * Getter method for the instance variable {@link #includeChildTypes}.
     *
     * @return value of instance variable {@link #includeChildTypes}
     */
    public boolean isIncludeChildTypes()
    {
        return this.includeChildTypes;
    }

    /**
     * Setter method for instance variable {@link #includeChildTypes}.
     *
     * @param _includeChildTypes value for instance variable {@link #includeChildTypes}
     */
    public void setIncludeChildTypes(final boolean _includeChildTypes)
    {
        this.includeChildTypes = _includeChildTypes;
    }
}
