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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.SQLTable;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;


/**
 * Query is used as a nested query for an attribute.
 *
 * @author The eFaps Team
 *
 */
public class AttributeQuery
    extends AbstractObjectQuery<Object>
{

    /**
     * Attribute this query will return the value for.
     */
    private final Attribute attribute;

    /**
     * Constructor setting the type by his UUID.
     * @param _typeUUI UUID of the Type the query is based on
     * @throws CacheReloadException on error
     */
    public AttributeQuery(final UUID _typeUUI)
        throws CacheReloadException
    {
        this(Type.get(_typeUUI));
    }

    /**
     * Constructor setting the type.
     *
     * @param _type TYpe the query is based on
     */
    public AttributeQuery(final Type _type)
    {
        this(_type, _type.getAttribute("ID"));
    }

    /**
     * Constructor setting the type by his UUID.
     *
     * @param _typeUUI UUID of the Type the query is based on
     * @param _attribute attribute the value is wanted for
     * @throws CacheReloadException on error
     */
    public AttributeQuery(final UUID _typeUUI,
                          final Attribute _attribute)
        throws CacheReloadException
    {
        this(Type.get(_typeUUI), _attribute);
    }

    /**
     * Constructor setting the type by his UUID.
     *
     * @param _typeUUI UUID of the Type the query is based on
     * @param _attributeName name of the attribute the value is wanted for
     * @throws CacheReloadException on error
     */
    public AttributeQuery(final UUID _typeUUI,
                          final String _attributeName)
        throws CacheReloadException
    {
        this(Type.get(_typeUUI), Type.get(_typeUUI).getAttribute(_attributeName));
    }

    /**
     * Constructor setting the type.
     * @param _type         TYpe the query is based on
     * @param _attribute    attribute the value is wanted for
     */
    public AttributeQuery(final Type _type,
                          final Attribute _attribute)
    {
        super(_type);
        this.attribute = _attribute == null ?  _type.getAttribute("ID") : _attribute;
    }

    /**
     * Getter method for the instance variable {@link #attribute}.
     *
     * @return value of instance variable {@link #attribute}
     */
    public Attribute getAttribute()
    {
        return this.attribute;
    }

    /**
     * Execute the Query.
     * @return this
     * @throws EFapsException
     * TODO Accesscheck
     */
    @Override
    public List<Object> execute()
        throws EFapsException
    {
        return executeWithoutAccessCheck();
    }

    /**
     * The instance method executes the query without an access check.
     *
     * @return true if the query contains values, else false
     * @throws EFapsException on error
     */
    @Override
    public List<Object> executeWithoutAccessCheck()
        throws EFapsException
    {
        executeOneCompleteStmt(getSQLStatement(0));
        return getValues();
    }

    /**
     * Create the SQL statement.
     *
     * @param _idx the _idx
     * @return StringBuilder containing the statement
     * @throws EFapsException on error
     */
    public String getSQLStatement(final Integer _idx)
        throws EFapsException
    {
        prepareQuery();
        final SQLSelect select = new SQLSelect("S" + _idx + "T")
            .column(getSqlTable2Index().get(this.attribute.getTable()), this.attribute.getSqlColNames().get(0))
            .from(getBaseType().getMainTable().getSqlTable(), 0);

        // add child tables
        if (getSqlTable2Index().size() > 0) {
            for (final Entry<SQLTable, Integer> entry : getSqlTable2Index().entrySet()) {
                if (entry.getValue() > 0) {
                    select.leftJoin(entry.getKey().getSqlTable(), entry.getValue(), "ID", 0, "ID");
                }
            }
        }
        select.addSection(getWhere());
        select.addSection(getOrderBy());
        select.addSection(getLimit());

        if (AbstractObjectQuery.LOG.isDebugEnabled()) {
            AbstractObjectQuery.LOG.debug(select.getSQL());
        }
        return select.getSQL();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void prepareQuery()
        throws EFapsException
    {
        super.prepareQuery();
        // to be sure that the table the attribute is in, is part of the indexed tables
        getIndex4SqlTable(this.attribute.getTable());
    }

    /**
     * Execute the actual statement against the database.
     * @param _complStmt        Statment to be executed
     * @return true if executed with success
     * @throws EFapsException on error
     */
    protected boolean executeOneCompleteStmt(final String _complStmt)
        throws EFapsException
    {
        final boolean ret = false;
        ConnectionResource con = null;
        try {
            con = Context.getThreadContext().getConnectionResource();
            if (AbstractObjectQuery.LOG.isDebugEnabled()) {
                AbstractObjectQuery.LOG.debug(_complStmt.toString());
            }

            final Statement stmt = con.createStatement();

            final ResultSet rs = stmt.executeQuery(_complStmt.toString());
            new ArrayList<Instance>();
            while (rs.next()) {
                getValues().add(rs.getObject(1));
            }
            rs.close();
            stmt.close();
        } catch (final SQLException e) {
            throw new EFapsException(AttributeQuery.class, "executeOneCompleteStmt", e);
        }
        return ret;
    }
}
