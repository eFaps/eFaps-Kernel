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
import org.efaps.admin.datamodel.Type;
import org.efaps.db.Context;
import org.efaps.db.stmt.print.ObjectPrint;
import org.efaps.db.stmt.selection.ISelectionProvider;
import org.efaps.db.stmt.selection.Select;
import org.efaps.db.stmt.selection.elements.AbstractElement;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.db.wrapper.SQLPart;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.db.wrapper.TableIndexer.Tableidx;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class SQLRunner.
 *
 * @author The eFaps Team
 */
public class SQLRunner
    implements IEQLRunner
{

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(SQLRunner.class);

    /** The print. */
    private ObjectPrint print;

    /** The sql select. */
    private SQLSelect sqlSelect;

    @Override
    public void prepare(final ObjectPrint _print)
        throws EFapsException
    {
        this.print = _print;
        this.sqlSelect = new SQLSelect();

        for (final Select select : this.print.getSelection().getSelects()) {
            for (final AbstractElement<?> element : select.getElements()) {
                element.append2SQLSelect(this.sqlSelect);
            }
        }
        if (this.sqlSelect.getColumns().size() > 0) {
            addWhere();
        }
    }

    /**
     * Adds the where.
     */
    private void addWhere()
    {
        final Type type = this.print.getInstance().getType();
        final String tableName = type.getMainTable().getSqlTable();
        final Tableidx tableidx = this.sqlSelect.getIndexer().getTableIdx(tableName, tableName);
        if (tableidx.isCreated()) {
            this.sqlSelect.from(tableidx.getTable(), tableidx.getIdx());
        }

        this.sqlSelect.addPart(SQLPart.WHERE).addColumnPart(0, "ID").addPart(SQLPart.EQUAL).addValuePart(this.print
                        .getInstance().getId());

        if (type.getMainTable().getSqlColType() != null) {
            this.sqlSelect.addPart(SQLPart.AND)
                .addColumnPart(0, type.getMainTable().getSqlColType())
                .addPart(SQLPart.EQUAL).addValuePart(type.getId());
        }
    }

    @Override
    public void execute()
        throws EFapsException
    {
        executeSQLStmt(this.print, this.sqlSelect.getSQL());
    }

    /**
     * Execute SQL stmt.
     *
     * @param _sqlProvider the sql provider
     * @param _complStmt the compl stmt
     * @return true, if successful
     * @throws EFapsException the e faps exception
     */
    protected boolean executeSQLStmt(final ISelectionProvider _sqlProvider, final String _complStmt)
        throws EFapsException
    {
        SQLRunner.LOG.debug("SQL-Statement: {}", _complStmt);
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
        }
        return ret;
    }
}
