/*
 * Copyright 2003 - 2011 The eFaps Team
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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Map.Entry;

import org.efaps.admin.datamodel.SQLTable;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.util.EFapsException;


/**
 * Query that returns a list of instances.
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class InstanceQuery
    extends AbstractObjectQuery<Instance>
{
    /**
     * Constructor setting the type by his UUID.
     * @param _typeUUI UUID of the Type the query is based on
     */
    public InstanceQuery(final UUID _typeUUI)
    {
        this(Type.get(_typeUUI));
    }

    /**
     * Constructor setting the type.
     * @param _type TYpe the query is based on
     */
    public InstanceQuery(final Type _type)
    {
        super(_type);
    }

    /**
     * Execute the Query.
     * @return this
     * @throws EFapsException
     * TODO Accesscheck
     */
    @Override
    public List<Instance> execute()
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
    public List<Instance> executeWithoutAccessCheck()
        throws EFapsException
    {
        prepareQuery();
        executeOneCompleteStmt(createSQLStatement());
        return getValues();
    }

    /**
     * Create the SQL statement.
     * @return StringBuilder containing the statement
     * @throws EFapsException on error
     */
    private String createSQLStatement()
        throws EFapsException
    {
        final SQLSelect select = new SQLSelect()
            .column(0, "ID")
            .from(getBaseType().getMainTable().getSqlTable(), 0);

        // if the main table has a column for the type it is selected also
        int colIndex = 2;
        if (getBaseType().getMainTable().getSqlColType() != null) {
            select.column(0, getBaseType().getMainTable().getSqlColType());
            colIndex++;
        }
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

            final Statement stmt = con.getConnection().createStatement();

            final ResultSet rs = stmt.executeQuery(_complStmt.toString());

            new ArrayList<Instance>();
            while (rs.next()) {
                final long id = rs.getLong(1);
                Long typeId = null;
                if (getBaseType().getMainTable().getSqlColType() != null) {
                    typeId = rs.getLong(2);
                }
                getValues().add(Instance.get(typeId == null ? getBaseType() : Type.get(typeId), id));
            }
            rs.close();
            stmt.close();
            con.commit();
        } catch (final SQLException e) {
            throw new EFapsException(InstanceQuery.class, "executeOneCompleteStmt", e);
        } finally {
            if (con != null && con.isOpened()) {
                con.abort();
            }
        }
        return ret;
    }
}
