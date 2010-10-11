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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.efaps.admin.datamodel.SQLTable;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.search.QAnd;
import org.efaps.db.search.QAttribute;
import org.efaps.db.search.QEqual;
import org.efaps.db.search.QNumberValue;
import org.efaps.db.search.QWhere;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 * @param <T> type the query returns
 */
public abstract class AbstractObjectQuery<T>
{
    /**
     * Logging instance used in this class.
     */
    protected static final Logger LOG = LoggerFactory.getLogger(AbstractObjectQuery.class);


    /**
     * Must this query be executed company depended.
     * (if the type is company dependend)
     */
    private boolean companyDepended = true;


    /**
     * Base type this query is searching on.
     */
    private final Type baseType;


    /**
     * The where criteria for this search.
     */
    private QWhere where;


    /**
     * Should the child types be also be included in this search?
     */
    private boolean includeChildTypes = true;


    /**
     * Map to store the table to index relation.
     */
    private final Map<SQLTable, Integer> sqlTable2Index = new HashMap<SQLTable, Integer>();


    /**
     * List contains the values returned from the query.
     */
    private final List<T> values = new ArrayList<T>();


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
        this.baseType = _type;
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
     * Getter method for the instance variable {@link #companyDepended}.
     *
     * @return value of instance variable {@link #companyDepended}
     */
    public boolean isCompanyDepended()
    {
        return this.companyDepended;
    }

    /**
     * Setter method for instance variable {@link #companyDepended}.
     *
     * @param _companyDepended value for instance variable {@link #companyDepended}
     * @return this
     */

    public AbstractObjectQuery<T> setCompanyDepended(final boolean _companyDepended)
    {
        this.companyDepended = _companyDepended;
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
     * Getter method for the instance variable {@link #baseType}.
     *
     * @return value of instance variable {@link #baseType}
     */
    public Type getBaseType()
    {
        return this.baseType;
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
    public Integer getIndex4SqlTable(final SQLTable _sqlTable)
    {
        Integer ret;
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
    public AbstractObjectQuery<T> setWhere(final QWhere _where)
    {
        this.where = _where;
        return this;
    }


    /**
     * Getter method for the instance variable {@link #where}.
     *
     * @return value of instance variable {@link #where}
     */
    public QWhere getWhere()
    {
        return this.where;
    }


    /**
     * Move the current instance to the next instance in the list.
     * @return true if the instance was set to the next value, else false
     */
    public boolean next()
    {
        if (this.iter == null) {
            this.iter = new ArrayList<T>(this.values).iterator();
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
        this.sqlTable2Index.put(this.baseType.getMainTable(), 0);
        if (this.baseType.getMainTable().getSqlColType() != null) {
            final QEqual eqPart = new QEqual(new QAttribute(this.baseType.getTypeAttribute()),
                                           new QNumberValue(this.baseType.getId()));
            if (this.includeChildTypes && !this.baseType.getChildTypes().isEmpty()) {
                for (final Type type : this.baseType.getChildTypes()) {
                    eqPart.addValue(new QNumberValue(type.getId()));
                }
            }
            if (this.where == null) {
                this.where = new QWhere(eqPart);
            } else {
                this.where.setPart(new QAnd(this.where.getPart(), eqPart));
            }
        }
        if (this.companyDepended && this.baseType.isCompanyDepended()) {
            if (Context.getThreadContext().getCompany() == null) {
                throw new EFapsException(InstanceQuery.class, "noCompany");
            }
            final QEqual eqPart = new QEqual(new QAttribute(this.baseType.getCompanyAttribute()),
                                           new QNumberValue(Context.getThreadContext().getCompany().getId()));
            if (this.where == null) {
                this.where = new QWhere(eqPart);
            } else {
                this.where.setPart(new QAnd(this.where.getPart(), eqPart));
            }
        }
        if (this.where != null) {
            this.where.prepare(this);
        }
    }
}
