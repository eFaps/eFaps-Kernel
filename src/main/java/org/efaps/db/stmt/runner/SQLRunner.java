/*
 * Copyright 2003 - 2017 The eFaps Team
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
 */

package org.efaps.db.stmt.runner;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.apache.commons.dbutils.handlers.ArrayListHandler;
import org.efaps.db.Context;
import org.efaps.db.stmt.selection.ISelectionProvider;
import org.efaps.db.stmt.selection.Select;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class SQLRunner.
 *
 * @author The eFaps Team
 */
public class SQLRunner
{

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(SQLRunner.class);

    /**
     * Creates the SQL statement.
     *
     * @param _sqlProvider the sql provider
     * @return the string
     * @throws EFapsException the e faps exception
     */
    protected String createSQLStatement(final ISQLProvider _sqlProvider)
        throws EFapsException
    {
        _sqlProvider.prepare();
        final SQLSelect sqlSelect = new SQLSelect();
        _sqlProvider.append2SQLSelect(sqlSelect);
        return sqlSelect.getSQL();
    }

    /**
     * Execute.
     *
     * @param _sqlProvider the sql provider
     * @throws EFapsException the e faps exception
     */
    protected void execute(final ISQLProvider _sqlProvider)
        throws EFapsException
    {
        final String sql = createSQLStatement(_sqlProvider);
        SQLRunner.LOG.debug("SQL-Statement: {}", sql);
        executeSQLStmt((ISelectionProvider) _sqlProvider, sql);
    }

    /**
     * Execute SQL stmt.
     *
     * @param _sqlProvider the sql provider
     * @param _complStmt the compl stmt
     * @return true, if successful
     * @throws EFapsException the e faps exception
     */
    protected boolean executeSQLStmt(final ISelectionProvider _sqlProvider,
                                     final String _complStmt)
        throws EFapsException
    {
        boolean ret = false;
        ConnectionResource con = null;
        try {

            con = Context.getThreadContext().getConnectionResource();
            final Statement stmt = con.createStatement();
            final ResultSet rs = stmt.executeQuery(_complStmt);
            final ArrayListHandler handler = new ArrayListHandler(Context.getDbType().getRowProcessor());
            final List<Object[]> rows = handler.handle(rs);
            rs.close();
            stmt.close();

            for (final Object[] row : rows) {
                for (final Select select : _sqlProvider.getSelection().getSelects()) {
                    select.addObject(row);
                }
                ret = true;
            }
        } catch (final SQLException e) {
            throw new EFapsException(SQLRunner.class, "executeOneCompleteStmt", e);
        } finally {

        }
        return ret;
    }
}
