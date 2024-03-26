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
package org.efaps.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.efaps.admin.common.Association;
import org.efaps.admin.datamodel.SQLTable;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.datamodel.attributetype.ConsortiumLinkType;
import org.efaps.admin.user.Company;
import org.efaps.db.search.QAnd;
import org.efaps.db.search.QAttribute;
import org.efaps.db.search.compare.QEqual;
import org.efaps.db.search.section.QLimitSection;
import org.efaps.db.search.section.QOffsetSection;
import org.efaps.db.search.section.QOrderBySection;
import org.efaps.db.search.section.QWhereSection;
import org.efaps.db.search.value.QNumberValue;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author The eFaps Team
 *
 * @param <T> type the query returns
 */
public abstract class AbstractObjectQuery<T>
    extends AbstractTypeQuery
{
    /**
     * Logging instance used in this class.
     */
    protected static final Logger LOG = LoggerFactory.getLogger(AbstractObjectQuery.class);

    /**
     * Must this query be executed company dependent.
     * (if the type is company dependent)
     */
    private boolean companyDependent = true;

    /**
     * The where criteria for this search.
     */
    private QWhereSection where;

    /**
     * Order by clause for this query.
     */
    private QOrderBySection orderBy;

    /**
     * Limit for this Query.
     */
    private QLimitSection limit;

    private QOffsetSection offset;

    /**
     * Should the child types be also be included in this search?
     */
    private boolean includeChildTypes = true;

    /**
     * Map to store the table to index relation.
     */
    private final Map<SQLTable, Integer> sqlTable2Index = new HashMap<>();

    /**
     * List contains the values returned from the query.
     */
    private final List<T> values = new ArrayList<>();

    /**
     * Iterator for the instances.
     */
    private Iterator<T> iter;

    /**
     * Variable holds the current instance.
     */
    private T current;

    /**
     * Constructor setting the type.
     * @param _type TYpe the query is based on
     */
    public AbstractObjectQuery(final Type _type)
    {
        setBaseType(_type);
    }

    /**
     * @return List of objects
     * @throws EFapsException on error
     */
    public abstract List<T> execute()
        throws EFapsException;

    /**
     * @return List of objects
     * @throws EFapsException on error
     */
    public abstract List<T> executeWithoutAccessCheck()
        throws EFapsException;

    /**
     * Getter method for the instance variable {@link #companyDependent}.
     *
     * @return value of instance variable {@link #companyDependent}
     */
    public boolean isCompanyDependent()
    {
        return this.companyDependent;
    }

    /**
     * Setter method for instance variable {@link #companyDependent}.
     *
     * @param _companyDependent the companyDependent
     * @return this
     */

    public AbstractObjectQuery<T> setCompanyDependent(final boolean _companyDependent)
    {
        this.companyDependent = _companyDependent;
        return this;
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
     * @return this
     */

    public AbstractObjectQuery<T> setIncludeChildTypes(final boolean _includeChildTypes)
    {
        this.includeChildTypes = _includeChildTypes;
        return this;
    }

    /**
     * Getter method for the instance variable {@link #sqlTable2Index}.
     *
     * @return value of instance variable {@link #sqlTable2Index}
     */
    public Map<SQLTable, Integer> getSqlTable2Index()
    {
        return this.sqlTable2Index;
    }

    /**
     * Get the index for a SQLTable if the table is not existing the table is
     * added and a new index given.
     *
     * @param _sqlTable SQLTable the index is wanted for
     * @return index of the SQLTable
     */
    @Override
    public Integer getIndex4SqlTable(final SQLTable _sqlTable)
    {
        final Integer ret;
        if (this.sqlTable2Index.containsKey(_sqlTable)) {
            ret = this.sqlTable2Index.get(_sqlTable);
        } else {
            Integer max = 0;
            for (final Integer index : this.sqlTable2Index.values()) {
                if (index > max) {
                    max = index;
                }
            }
            ret = max + 1;
            this.sqlTable2Index.put(_sqlTable, ret);
        }
        return ret;
    }

    /**
     * setter method for the instance variable {@link #where}.
     * @param _where value for instance variable {@link #where}
     * @return this
     */
    public AbstractObjectQuery<T> setWhere(final QWhereSection _where)
    {
        this.where = _where;
        return this;
    }

    /**
     * Getter method for the instance variable {@link #where}.
     *
     * @return value of instance variable {@link #where}
     */
    public QWhereSection getWhere()
    {
        return this.where;
    }

    /**
     * Getter method for the instance variable {@link #orderBy}.
     *
     * @return value of instance variable {@link #orderBy}
     */
    public QOrderBySection getOrderBy()
    {
        return this.orderBy;
    }


    /**
     * Setter method for instance variable {@link #orderBy}.
     *
     * @param _orderBy value for instance variable {@link #orderBy}
     */

    public void setOrderBy(final QOrderBySection _orderBy)
    {
        this.orderBy = _orderBy;
    }

    /**
     * Getter method for the instance variable {@link #limit}.
     *
     * @return value of instance variable {@link #limit}
     */
    public QLimitSection getLimit()
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
        this.limit = new QLimitSection(_limit);
    }

    /**
     * Setter method for instance variable {@link #limit}.
     *
     * @param _limit value for instance variable {@link #limit}
     */

    public void setLimit(final QLimitSection _limit)
    {
        this.limit = _limit;
    }

    public QOffsetSection getOffset()
    {
        return this.offset;
    }

    public void setOffset(final int Offset)
    {
        this.offset = new QOffsetSection(Offset);
    }

    public void setOffset(final QOffsetSection offset)
    {
        this.offset = offset;
    }

    /**
     * Move the current instance to the next instance in the list.
     * @return true if the instance was set to the next value, else false
     */
    public boolean next()
    {
        if (this.iter == null) {
            this.iter = new ArrayList<>(this.values).iterator();
        }
        final boolean ret = this.iter.hasNext();
        if (ret) {
            this.current = this.iter.next();
        }
        return ret;
    }

    /**
     * Get the current instance.
     * @return value of instance variable {@link #current}
     */
    public T getCurrentValue()
    {
        return this.current;
    }

    /**
     * Getter method for the instance variable {@link #instances}.
     *
     * @return value of instance variable {@link #instances}
     */
    public List<T> getValues()
    {
        return this.values;
    }

    /**
     * Prepare the Query for execution.
     * @throws EFapsException on error
     */
    protected void prepareQuery()
        throws EFapsException
    {
        this.sqlTable2Index.put(getBaseType().getMainTable(), 0);
        if (getBaseType().getMainTable() == null) {
            throw new EFapsException(AbstractObjectQuery.class, "BaseType", getBaseType());
        }
        if (getBaseType().getMainTable().getSqlColType() != null) {
            final QEqual eqPart = new QEqual(new QAttribute(getBaseType().getTypeAttribute()),
                                           new QNumberValue(getBaseType().getId()));
            if (this.includeChildTypes && !getBaseType().getChildTypes().isEmpty()) {
                for (final Type type : getBaseType().getChildTypes()) {
                    eqPart.addValue(new QNumberValue(type.getId()));
                }
            }
            if (this.where == null) {
                this.where = new QWhereSection(eqPart);
            } else {
                this.where.setPart(new QAnd(this.where.getPart(), eqPart));
            }
        }
        if (getBaseType().isCompanyDependent()) {
            final QEqual eqPart = new QEqual(new QAttribute(getBaseType().getCompanyAttribute()));
            if (this.isCompanyDependent()) {
                if (Context.getThreadContext().getCompany() == null) {
                    throw new EFapsException(InstanceQuery.class, "noCompany");
                }
                if (getBaseType().getCompanyAttribute().getAttributeType().getClassRepr().equals(
                                ConsortiumLinkType.class)) {
                    for (final Long consortium : Context.getThreadContext().getCompany().getConsortiums()) {
                        eqPart.addValue(new QNumberValue(consortium));
                    }
                } else {
                    eqPart.addValue(new QNumberValue(Context.getThreadContext().getCompany().getId()));
                }
            } else {
                for (final Long compId : Context.getThreadContext().getPerson().getCompanies()) {
                    if (getBaseType().getCompanyAttribute().getAttributeType().getClassRepr().equals(
                                    ConsortiumLinkType.class)) {
                        for (final Long consortium : Company.get(compId).getConsortiums()) {
                            eqPart.addValue(new QNumberValue(consortium));
                        }
                    } else {
                        eqPart.addValue(new QNumberValue(compId));
                    }
                }
            }
            // only add if an actual filter was set. A background process might not use Companies at all.
            if (CollectionUtils.isNotEmpty(eqPart.getValues())) {
                if (this.where == null) {
                    this.where = new QWhereSection(eqPart);
                } else {
                    this.where.setPart(new QAnd(this.where.getPart(), eqPart));
                }
            }
        }

        if (getBaseType().hasAssociation()) {
            final QEqual eqPart = new QEqual(new QAttribute(getBaseType().getAssociationAttribute()));
            if (this.isCompanyDependent()) {
                if (Context.getThreadContext().getCompany() == null) {
                    throw new EFapsException(InstanceQuery.class, "noCompany");
                }
                eqPart.addValue(new QNumberValue(Association.evaluate(getBaseType()).getId()));
            } else {
                for (final Long compId : Context.getThreadContext().getPerson().getCompanies()) {
                    eqPart.addValue(new QNumberValue(Association.evaluate(getBaseType(), compId).getId()));
                }
            }
            // only add if an actual filter was set. A background process might not use Companies at all.
            if (CollectionUtils.isNotEmpty(eqPart.getValues())) {
                if (this.where == null) {
                    this.where = new QWhereSection(eqPart);
                } else {
                    this.where.setPart(new QAnd(this.where.getPart(), eqPart));
                }
            }
        }

        if (this.where != null) {
            this.where.prepare(this);
        }
        if (this.orderBy != null) {
            this.orderBy.prepare(this);
        }

        if (this.limit != null) {
            this.limit.prepare(this);
        }
        if (this.offset != null) {
            this.offset.prepare(this);
        }
    }
}
