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
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections4.MultiMapUtils;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.dbutils.handlers.ArrayListHandler;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.Context;
import org.efaps.db.stmt.print.AbstractPrint;
import org.efaps.db.stmt.print.ObjectPrint;
import org.efaps.db.stmt.print.QueryPrint;
import org.efaps.db.stmt.selection.ISelectionProvider;
import org.efaps.db.stmt.selection.Select;
import org.efaps.db.stmt.selection.elements.AbstractElement;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.db.wrapper.SQLPart;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.db.wrapper.SQLSelect.SQLSelectPart;
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
    private AbstractPrint print;

    /** The sql select. */
    private SQLSelect sqlSelect;

    @Override
    public void prepare(final AbstractPrint _print)
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
            addTypeCriteria();
            if (this.print instanceof ObjectPrint) {
                addWhere4ObjectPrint();
            } else {
                addWhere4QueryPrint();
            }
        }
    }

    /**
     * Adds the type criteria.
     */
    private void addTypeCriteria()
    {
        final MultiValuedMap<Tableidx, TypeCriteria> typeCriterias = MultiMapUtils.newListValuedHashMap();
        for (final Type type : this.print.getTypes()) {
            final String tableName = type.getMainTable().getSqlTable();
            final Tableidx tableidx = this.sqlSelect.getIndexer().getTableIdx(tableName, tableName);
            if (tableidx.isCreated()) {
                this.sqlSelect.from(tableidx.getTable(), tableidx.getIdx());
            }
            if (type.getMainTable().getSqlColType() != null) {
                typeCriterias.put(tableidx, new TypeCriteria(type.getMainTable().getSqlColType(), type.getId()));
            }
        }
        if (!typeCriterias.isEmpty()) {
            final SQLSelectPart currentPart = this.sqlSelect.getCurrentPart();
            if (currentPart == null) {
                this.sqlSelect.addPart(SQLPart.WHERE);
            }
            for (final Tableidx tableidx : typeCriterias.keys()) {
                final Collection<TypeCriteria> criterias = typeCriterias.get(tableidx);
                if (criterias.size() == 1) {
                    final TypeCriteria criteria = criterias.iterator().next();
                    this.sqlSelect.addColumnPart(tableidx.getIdx(), criteria.sqlColType)
                        .addPart(SQLPart.EQUAL).addValuePart(criteria.id);
                }
            }
        }
    }

    /**
     * Adds the where.
     */
    private void addWhere4QueryPrint()
    {
        final QueryPrint queryPrint = (QueryPrint) this.print;

    }


    /**
     * Adds the where.
     */
    private void addWhere4ObjectPrint()
    {
        final ObjectPrint objectPrint = (ObjectPrint) this.print;
        final SQLSelectPart currentPart = this.sqlSelect.getCurrentPart();
        if (currentPart == null) {
            this.sqlSelect.addPart(SQLPart.WHERE);
        } else {
            this.sqlSelect.addPart(SQLPart.AND);
        }
        this.sqlSelect.addColumnPart(0, "ID").addPart(SQLPart.EQUAL)
            .addValuePart(objectPrint.getInstance().getId());
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

    private static class TypeCriteria {

        private final String sqlColType;
        private final long id;

        public TypeCriteria(final String _sqlColType,
                            final long _id)
        {
            this.sqlColType = _sqlColType;
            this.id = _id;
        }

        @Override
        public boolean equals(final Object _obj)
        {
            boolean ret;
            if (_obj instanceof TypeCriteria) {
                final TypeCriteria obj = (TypeCriteria) _obj;
                ret = this.sqlColType.equals(obj.sqlColType) && this.id == obj.id;
            } else {
                ret = super.equals(_obj);
            }
            return ret;
        }

        @Override
        public int hashCode()
        {
            return this.sqlColType.hashCode() + Long.valueOf(this.id).hashCode();
        }
    }
}
