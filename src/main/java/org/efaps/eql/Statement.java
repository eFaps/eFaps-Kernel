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
        addSelect(_select, new Integer(this.alias2select.size() + 1).toString());
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
        } catch (final EFapsException e) {
            LOG.error("Catched error", e);
        }
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
     * @param _stmtStr Statement
     * @return StatementObject
     */
    public static Statement getStatement(final String _stmtStr)
    {
        final Statement ret = new Statement();
        final EQLParser parser = new EQLParser(new StringReader(_stmtStr));
        try {
            parser.parseStatement(ret);
        } catch (final ParseException e) {
            LOG.error("Catched error", e);
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setObject(final String _oid)
    {
        this.instance = Instance.get(_oid);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setStmtType(final StmtType _stmtType)
    {
        this.stmtType = _stmtType;
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

    public boolean isQuery()
    {
        return IStatement.StmtType.QUERY.equals(getStmtType());
    }
}
