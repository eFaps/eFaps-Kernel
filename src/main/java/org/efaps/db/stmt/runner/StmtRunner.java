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

import org.efaps.db.stmt.print.ObjectPrint;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class PrintRunner.
 */
public final class StmtRunner
{

    /** The instance. */
    private static StmtRunner RUNNER;

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(StmtRunner.class);

    /**
     * Instantiates a new stmt runner.
     */
    private StmtRunner()
    {
    }

    /**
     * Gets the.
     *
     * @return the stmt runner
     */
    public static StmtRunner get()
    {
        if (StmtRunner.RUNNER == null) {
            StmtRunner.RUNNER = new StmtRunner();
        }
        return StmtRunner.RUNNER;
    }

    /**
     * Execute.
     *
     * @param _print the print
     * @throws EFapsException the e faps exception
     */
    public void execute(final ObjectPrint _print)
        throws EFapsException
    {
        final SQLRunner runner = new SQLRunner();
        runner.execute(_print);
    }

    /**
     * public void execute(final DataPrint _print)
     * throws EFapsException
     * {
     * this.executeSQL(_print);
     * this.executeNOSQL(_print);
     * }
     *
     * protected void executeSQL(final DataPrint _print)
     * throws EFapsException
     * {
     * new SQLRunner().execute(_print);
     * }
     *
     * protected void executeNOSQL(final DataPrint _print)
     * {
     *
     * }
     *
     * private static class SQLRunner
     * {
     *
     * public void execute(final DataPrint _print)
     * throws EFapsException
     * {
     * final String sql = this.createSQLStatement(_print);
     * this.executeOneCompleteStmt(_print, sql);
     * }
     *
     * protected String createSQLStatement(final DataPrint _print)
     * throws EFapsException
     * {
     * final SQLSelect sqlSelect = new SQLSelect();
     * for (final Select select : _print.getSelection().getSelects()) {
     * for (final AbstractElement<?> element : select.getElements()) {
     * element.append2SQLSelect(sqlSelect);
     * }
     * }
     * return sqlSelect.getSQL();
     * }
     *
     * protected boolean executeOneCompleteStmt(final DataPrint _print,
     * final String _complStmt)
     * throws EFapsException
     * {
     * boolean ret = false;
     * ConnectionResource con = null;
     * try {
     * StmtRunner.LOG.debug("Executing SQL: {}", _complStmt);
     *
     * con = Context.getThreadContext().getConnectionResource();
     * final Statement stmt = con.createStatement();
     * final ResultSet rs = stmt.executeQuery(_complStmt);
     * final ArrayListHandler handler = new
     * ArrayListHandler(Context.getDbType().getRowProcessor());
     * final List<Object[]> rows = handler.handle(rs);
     * rs.close();
     * stmt.close();
     *
     * for (final Object[] row : rows) {
     * for (final Select select : _print.getSelection().getSelects()) {
     * select.addObject(row);
     * }
     * ret = true;
     * }
     * } catch (final SQLException e) {
     * throw new EFapsException(InstanceQuery.class, "executeOneCompleteStmt",
     * e);
     * } finally {
     *
     * }
     * return ret;
     * }
     * }
     */
}
