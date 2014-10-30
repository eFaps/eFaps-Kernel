/*
 * Copyright 2003 - 2014 The eFaps Team
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

package org.efaps.eql;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.efaps.admin.datamodel.Type;
import org.efaps.db.Instance;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public final class Statement
    implements IStatement
{

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Statement.class);

    /**
     * Mapping between alias and select.
     */
    private final Map<String, String> alias2select = new LinkedHashMap<>();

    /**
     * Mapping between alias and esjp className.
     */
    private final Map<String, String> alias2esjp = new LinkedHashMap<>();

    /**
     * Statementtype.
     */
    private StmtType stmtType;

    /**
     * QueryBuilder.
     */
    private QueryBuilder queryBdr;

    /**
     * MultiPrint.
     */
    private MultiPrintQuery multiPrint;

    /**
     * Instance for the statement.
     */
    private Instance instance;

    /**
     * Class Name of the esjp to be executed as query.
     */
    private String esjpClassName;

    /**
     * No public constructor is wanted.
     */
    private Statement()
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addType(final String _type)
    {
        try {
            if (this.queryBdr == null) {
                this.queryBdr = new QueryBuilder(Type.get(_type));
            } else {
                this.queryBdr.addType(Type.get(_type));
            }
            LOG.debug("adding Type: '{}'", _type);
        } catch (final EFapsException e) {
            LOG.error("Catched error", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addSelect(final String _select)
    {
        addSelect(_select, new Integer(this.alias2select.size() + this.alias2esjp.size() + 1).toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addSelect(final String _select,
                          final String _alias)
    {
        try {
            if (this.multiPrint == null) {
                switch (getStmtType()) {
                    case QUERY:
                        this.multiPrint = this.queryBdr.getPrint();
                        break;
                    case PRINT:
                        final List<Instance> list = new ArrayList<Instance>();
                        list.add(getInstance());
                        this.multiPrint = new MultiPrintQuery(list);
                        break;
                    default:
                        break;
                }
            }
            this.multiPrint.addSelect(_select);
            this.alias2select.put(_alias, _select);
            LOG.debug("adding Select: '{}' alias '{}'", _select, _alias);
        } catch (final EFapsException e) {
            LOG.error("Catched error", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addWhereAttrEq(final String _attr,
                               final String _value)
    {
        try {
            this.queryBdr.addWhereAttrEqValue(_attr, _value);
            LOG.debug("adding WhereAttrEq: '{}' '{}'", _attr, _value);
        } catch (final EFapsException e) {
            LOG.error("Catched error", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addWhereAttrIn(final String _attr,
                               final Collection<String> _values)
    {
        try {
            this.queryBdr.addWhereAttrEqValue(_attr, _values.toArray());
            LOG.debug("adding WhereAttrIn: '{}' '{}'", _attr, _values);
        } catch (final EFapsException e) {
            LOG.error("Catched error", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addWhereAttrGreater(final String _attr,
                                    final String _value)
    {
        try {
            this.queryBdr.addWhereAttrGreaterValue(_attr, _value);
            LOG.debug("adding WhereAttrGreater: '{}' '{}'", _attr, _value);
        } catch (final EFapsException e) {
            LOG.error("Catched error", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addWhereAttrLess(final String _attr,
                                 final String _value)
    {
        try {
            this.queryBdr.addWhereAttrLessValue(_attr, _value);
            LOG.debug("adding WhereAttrLess: '{}' '{}'", _attr, _value);
        } catch (final EFapsException e) {
            LOG.error("Catched error", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setObject(final String _oid)
    {
        this.instance = Instance.get(_oid);
        LOG.debug("setting Object: '{}'", _oid);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setStmtType(final StmtType _stmtType)
    {
        this.stmtType = _stmtType;
        LOG.debug("setting StmtType: '{}'", _stmtType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEsjp(final String _className)
    {
        this.esjpClassName = _className;
        LOG.debug("setting Esjp: '{}'", _className);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addEsjpSelect(final String _className)
    {
        addEsjpSelect(_className, new Integer(this.alias2select.size() + this.alias2esjp.size() + 1).toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addEsjpSelect(final String _className,
                              final String _alias)
    {
        this.alias2esjp.put(_alias, _className);
        LOG.debug("setting Esjp Select: '{}' '{}'", _className, _alias);
    }

    /**
     * Getter method for the instance variable {@link #multiPrint}.
     *
     * @return value of instance variable {@link #multiPrint}
     */
    public MultiPrintQuery getMultiPrint()
    {
        return this.multiPrint;
    }

    /**
     * Getter method for the instance variable {@link #alias2select}.
     *
     * @return value of instance variable {@link #alias2select}
     */
    public Map<String, String> getAlias2Selects()
    {
        return this.alias2select;
    }

    /**
     * Getter method for the instance variable {@link #alias2esjp}.
     *
     * @return value of instance variable {@link #alias2esjp}
     */
    public Map<String, String> getAlias2Esjp()
    {
        return this.alias2esjp;
    }

    /**
     * Getter method for the instance variable {@link #stmtType}.
     *
     * @return value of instance variable {@link #stmtType}
     */
    protected StmtType getStmtType()
    {
        return this.stmtType;
    }

    /**
     * Getter method for the instance variable {@link #instance}.
     *
     * @return value of instance variable {@link #instance}
     */
    protected Instance getInstance()
    {
        return this.instance;
    }

    /**
     * @return true if it is a query
     */
    public boolean isQuery()
    {
        return IStatement.StmtType.QUERY.equals(getStmtType());
    }

    /**
     * @return true if it is a query
     */
    public boolean isEsjp()
    {
        return this.esjpClassName != null;
    }

    /**
     * Get the name of the esjp class.
     *
     * @return name of the esjp class
     */
    public String getEsjp()
    {
        return this.esjpClassName;
    }

    /**
     * @param _stmtStr Statement
     * @return StatementObject
     */
    public static Statement getStatement(final String _stmtStr)
    {
        final Statement ret = new Statement();
        LOG.debug("parsing Statement: '{}'", _stmtStr);
        final EQLParser parser = new EQLParser(new StringReader(_stmtStr));
        try {
            parser.parseStatement(ret);
        } catch (final ParseException e) {
            LOG.error("Catched error", e);
        }
        return ret;
    }
}
