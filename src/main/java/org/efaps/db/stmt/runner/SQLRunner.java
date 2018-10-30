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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.collections4.MultiMapUtils;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.dbutils.handlers.ArrayListHandler;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.AttributeType;
import org.efaps.admin.datamodel.SQLTable;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.datamodel.attributetype.ConsortiumLinkType;
import org.efaps.db.Context;
import org.efaps.db.stmt.filter.Filter;
import org.efaps.db.stmt.print.AbstractPrint;
import org.efaps.db.stmt.print.ListPrint;
import org.efaps.db.stmt.print.ObjectPrint;
import org.efaps.db.stmt.print.QueryPrint;
import org.efaps.db.stmt.selection.ISelectionProvider;
import org.efaps.db.stmt.selection.Select;
import org.efaps.db.stmt.selection.elements.AbstractDataElement;
import org.efaps.db.stmt.selection.elements.AbstractElement;
import org.efaps.db.stmt.update.AbstractObjectUpdate;
import org.efaps.db.stmt.update.AbstractUpdate;
import org.efaps.db.stmt.update.Insert;
import org.efaps.db.stmt.update.ObjectUpdate;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.db.wrapper.AbstractSQLInsertUpdate;
import org.efaps.db.wrapper.SQLInsert;
import org.efaps.db.wrapper.SQLPart;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.db.wrapper.SQLSelect.SQLSelectPart;
import org.efaps.db.wrapper.SQLUpdate;
import org.efaps.db.wrapper.TableIndexer.TableIdx;
import org.efaps.eql2.IUpdateElement;
import org.efaps.eql2.IUpdateElementsStmt;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;
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
    private IRunnable runnable;

    /** The sql select. */
    private SQLSelect sqlSelect;

    /** The updatemap. */
    private final Map<SQLTable, AbstractSQLInsertUpdate<?>> updatemap = new LinkedHashMap<>();

    @Override
    public void prepare(final IRunnable _runnable)
        throws EFapsException
    {
        this.runnable = _runnable;
        this.sqlSelect = new SQLSelect();
        if (isPrint()) {
            preparePrint((AbstractPrint) _runnable);
        } else if (isInsert()) {
            prepareInsert();
        } else {
            prepareUpdate();
        }
    }

    private void prepareUpdate()
        throws EFapsException
    {
        final ObjectUpdate update = (ObjectUpdate) this.runnable;
        prepareUpdate(update.getInstance().getType(), false);
    }

    private void prepareInsert()
        throws EFapsException
    {
        final Insert insert = (Insert) this.runnable;
        final Type type = insert.getType();
        final SQLTable mainTable = type.getMainTable();
        getSQLInsert(mainTable);
        prepareUpdate(type, true);
    }

    private void prepareUpdate(final Type _type, final boolean _create)
        throws EFapsException
    {
        final Iterator<?> iter = _type.getAttributes().entrySet().iterator();
        while (iter.hasNext()) {
            final Map.Entry<?, ?> entry = (Map.Entry<?, ?>) iter.next();
            final Attribute attr = (Attribute) entry.getValue();
            final AttributeType attrType = attr.getAttributeType();
            if (_create && attrType.isCreateUpdate() || attrType.isAlwaysUpdate()) {
                final SQLTable sqlTable = attr.getTable();
                final SQLInsert sqlInsert = getSQLInsert(sqlTable);
                try {
                    attr.prepareDBInsert(sqlInsert);
                } catch (final SQLException e) {
                    throw new EFapsException(SQLRunner.class, "prepareInsert", e);
                }
            }
        }

        final IUpdateElementsStmt<?> eqlStmt = ((AbstractUpdate) this.runnable).getEqlStmt();
        for (final IUpdateElement element : eqlStmt.getUpdateElements()) {
            final Attribute attr = _type.getAttribute(element.getAttribute());
            final SQLTable sqlTable = attr.getTable();
            try {
                if (_create) {
                    final SQLInsert sqlInsert = getSQLInsert(sqlTable);
                    attr.prepareDBInsert(sqlInsert, element.getValue());
                } else {
                    final SQLUpdate sqlUpdate = getSQLUpdate(sqlTable);
                    attr.prepareDBUpdate(sqlUpdate, element.getValue());
                }
            } catch (final SQLException e) {
                throw new EFapsException(SQLRunner.class, "prepareUpdate", e);
            }
        }
    }

    private SQLInsert getSQLInsert(final SQLTable _sqlTable)
    {
        SQLInsert ret;
        if (this.updatemap.containsKey(_sqlTable)) {
            ret = (SQLInsert) this.updatemap.get(_sqlTable);
        } else {
            ret = Context.getDbType().newInsert(_sqlTable.getSqlTable(), _sqlTable.getSqlColId(), this.updatemap
                            .isEmpty());
            this.updatemap.put(_sqlTable, ret);
        }
        return ret;
    }

    private SQLUpdate getSQLUpdate(final SQLTable _sqlTable)
    {
        SQLUpdate ret;
        if (this.updatemap.containsKey(_sqlTable)) {
            ret = (SQLUpdate) this.updatemap.get(_sqlTable);
        } else {
            final AbstractUpdate update = (AbstractUpdate) this.runnable;
            long id = 0;
            if (update instanceof AbstractObjectUpdate) {
                id = ((AbstractObjectUpdate) update).getInstance().getId();
            }
            ret = Context.getDbType().newUpdate(_sqlTable.getSqlTable(), _sqlTable.getSqlColId(), id);
            this.updatemap.put(_sqlTable, ret);
        }
        return ret;
    }

    /**
     * Prepare print.
     *
     * @param _print the print
     * @throws EFapsException the e faps exception
     */
    private void preparePrint(final AbstractPrint _print) throws EFapsException {
        for (final Select select : _print.getSelection().getAllSelects()) {
            for (final AbstractElement<?> element : select.getElements()) {
                if (element instanceof AbstractDataElement) {
                    ((AbstractDataElement<?>) element).append2SQLSelect(this.sqlSelect);
                }
            }
        }
        if (this.sqlSelect.getColumns().size() > 0) {
            if (_print instanceof ObjectPrint) {
                addWhere4ObjectPrint((ObjectPrint) _print);
            } else if (_print instanceof ListPrint) {
                addWhere4ListPrint((ListPrint) _print);
            } else {
                addTypeCriteria((QueryPrint) _print);
                addWhere4QueryPrint((QueryPrint) _print);
            }
            addCompanyCriteria(_print);
        }
    }

    /**
     * Checks if is prints the.
     *
     * @return true, if is prints the
     */
    private boolean isPrint() {
        return this.runnable instanceof AbstractPrint;
    }

    private boolean isInsert() {
        return this.runnable instanceof Insert;
    }

    /**
     * Adds the company criteria.
     *
     * @param _print the print
     * @throws EFapsException the e faps exception
     */
    private void addCompanyCriteria(final AbstractPrint _print)
        throws EFapsException
    {
        final Map<TableIdx, CompanyCriteria> companyCriterias = new HashMap<>();
        final List<Type> types = _print.getTypes().stream().sorted((type1, type2) -> Long.compare(type1.getId(), type2
                        .getId())).collect(Collectors.toList());
        for (final Type type : types) {
            final String tableName = type.getMainTable().getSqlTable();
            final TableIdx tableIdx = this.sqlSelect.getIndexer().getTableIdx(tableName);
            if (tableIdx.isCreated()) {
                this.sqlSelect.from(tableIdx.getTable(), tableIdx.getIdx());
            }
            if (type.isCompanyDependent()) {
                final String columnName = type.getCompanyAttribute().getSqlColNames().get(0);
                companyCriterias.put(tableIdx, new CompanyCriteria(columnName, type.getId()));
            }
        }
        if (!companyCriterias.isEmpty()) {
            if (Context.getThreadContext().getCompany() == null) {
                throw new EFapsException(SQLRunner.class, "noCompany");
            }
            final SQLSelectPart currentPart = this.sqlSelect.getCurrentPart();
            if (currentPart == null) {
                this.sqlSelect.addPart(SQLPart.WHERE);
            } else {
                this.sqlSelect.addPart(SQLPart.AND);
            }
            for (final Entry<TableIdx, CompanyCriteria> entry : companyCriterias.entrySet()) {
                this.sqlSelect.addColumnPart(entry.getKey().getIdx(), entry.getValue().sqlColCompany);
                if (Type.get(entry.getValue().id).getCompanyAttribute().getAttributeType().getClassRepr().equals(
                                ConsortiumLinkType.class)) {
                    this.sqlSelect.addPart(SQLPart.IN).addPart(SQLPart.PARENTHESIS_OPEN);
                    boolean first = true;
                    for (final Long consortium : Context.getThreadContext().getCompany().getConsortiums()) {
                        if (first) {
                            first = false;
                        } else {
                            this.sqlSelect.addPart(SQLPart.COMMA);
                        }
                        this.sqlSelect.addValuePart(consortium);
                    }
                    this.sqlSelect.addPart(SQLPart.PARENTHESIS_CLOSE);
                } else {
                    this.sqlSelect.addPart(SQLPart.EQUAL).addValuePart(Context.getThreadContext().getCompany().getId());
                }
            }
        }
    }

    /**
     * Adds the type criteria.
     *
     * @param _print the print
     */
    private void addTypeCriteria(final QueryPrint _print)
    {
        final MultiValuedMap<TableIdx, TypeCriteria> typeCriterias = MultiMapUtils.newListValuedHashMap();
        final List<Type> types = _print.getTypes().stream()
                        .sorted((type1, type2) -> Long.compare(type1.getId(), type2.getId()))
                        .collect(Collectors.toList());
        for (final Type type : types) {
            final String tableName = type.getMainTable().getSqlTable();
            final TableIdx tableIdx = this.sqlSelect.getIndexer().getTableIdx(tableName);
            if (tableIdx.isCreated()) {
                this.sqlSelect.from(tableIdx.getTable(), tableIdx.getIdx());
            }
            if (type.getMainTable().getSqlColType() != null) {
                typeCriterias.put(tableIdx, new TypeCriteria(type.getMainTable().getSqlColType(), type.getId()));
            }
        }
        if (!typeCriterias.isEmpty()) {
            final SQLSelectPart currentPart = this.sqlSelect.getCurrentPart();
            if (currentPart == null) {
                this.sqlSelect.addPart(SQLPart.WHERE);
            } else {
                this.sqlSelect.addPart(SQLPart.AND);
            }
            for (final TableIdx tableIdx : typeCriterias.keySet()) {
                final Collection<TypeCriteria> criterias = typeCriterias.get(tableIdx);
                final Iterator<TypeCriteria> iter = criterias.iterator();
                final TypeCriteria criteria = iter.next();
                this.sqlSelect.addColumnPart(tableIdx.getIdx(), criteria.sqlColType);
                if (criterias.size() == 1) {
                    this.sqlSelect .addPart(SQLPart.EQUAL).addValuePart(criteria.id);
                } else {
                    this.sqlSelect.addPart(SQLPart.IN).addPart(SQLPart.PARENTHESIS_OPEN);
                    this.sqlSelect.addValuePart(criteria.id);
                    while (iter.hasNext()) {
                        this.sqlSelect.addPart(SQLPart.COMMA).addValuePart(iter.next().id);
                    }
                    this.sqlSelect.addPart(SQLPart.PARENTHESIS_CLOSE);
                }
            }
        }
    }

    /**
     * Adds the where.
     * @throws CacheReloadException on error
     */
    private void addWhere4QueryPrint(final QueryPrint _print)
        throws CacheReloadException
    {
        final Filter filter = _print.getFilter();
        filter.append2SQLSelect(this.sqlSelect);
    }

    /**
     * Adds the where.
     */
    private void addWhere4ObjectPrint(final ObjectPrint _print)
    {
        final SQLSelectPart currentPart = this.sqlSelect.getCurrentPart();
        if (currentPart == null) {
            this.sqlSelect.addPart(SQLPart.WHERE);
        } else {
            this.sqlSelect.addPart(SQLPart.AND);
        }
        this.sqlSelect.addColumnPart(0, "ID").addPart(SQLPart.EQUAL)
            .addValuePart(_print.getInstance().getId());
    }

    /**
     * Adds the where.
     */
    private void addWhere4ListPrint(final ListPrint _print)
    {
        final SQLSelectPart currentPart = this.sqlSelect.getCurrentPart();
        if (currentPart == null) {
            this.sqlSelect.addPart(SQLPart.WHERE);
        } else {
            this.sqlSelect.addPart(SQLPart.AND);
        }
        this.sqlSelect.addColumnPart(0, "ID")
            .addPart(SQLPart.IN)
            .addPart(SQLPart.PARENTHESIS_OPEN)
            .addValuePart(_print.getInstances().stream()
                        .map(instance -> String.valueOf(instance.getId()))
                        .collect(Collectors.joining(SQLPart.COMMA.getDefaultValue())))
            .addPart(SQLPart.PARENTHESIS_CLOSE);
    }

    @Override
    public void execute()
        throws EFapsException
    {
        if (isPrint()) {
            executeSQLStmt((ISelectionProvider) this.runnable, this.sqlSelect.getSQL());
        } else if (isInsert()) {
            executeInserts();
        } else {
            executeUpdates();
        }
    }

    /**
     * Execute the inserts.
     *
     * @throws EFapsException the e faps exception
     */
    private void executeUpdates()
        throws EFapsException
    {
        ConnectionResource con = null;
        try {
            con = Context.getThreadContext().getConnectionResource();
            for (final Entry<SQLTable, AbstractSQLInsertUpdate<?>> entry : this.updatemap.entrySet()) {
                ((SQLUpdate) entry.getValue()).execute(con);
            }
        } catch (final SQLException e) {
            throw new EFapsException(SQLRunner.class, "executeOneCompleteStmt", e);
        }
    }

    /**
     * Execute the inserts.
     *
     * @throws EFapsException the e faps exception
     */
    private void executeInserts() throws EFapsException
    {
        ConnectionResource con = null;
        try {
            final Insert insert = (Insert) this.runnable;
            con = Context.getThreadContext().getConnectionResource();
            long id = 0;
            for (final Entry<SQLTable, AbstractSQLInsertUpdate<?>> entry : this.updatemap.entrySet()) {
                if (id != 0) {
                    entry.getValue().column(entry.getKey().getSqlColId(), id);
                }
                if (entry.getKey().getSqlColType() != null) {
                    entry.getValue().column(entry.getKey().getSqlColType(), insert.getType().getId());
                }
                final Long created = ((SQLInsert) entry.getValue()).execute(con);
                if (created != null) {
                    id = created;
                    insert.evaluateInstance(created);
                }
            }
        } catch (final SQLException e) {
            throw new EFapsException(SQLRunner.class, "executeOneCompleteStmt", e);
        }
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
                for (final Select select : _sqlProvider.getSelection().getAllSelects()) {
                    select.addObject(row);
                }
                ret = true;
            }
        } catch (final SQLException e) {
            throw new EFapsException(SQLRunner.class, "executeOneCompleteStmt", e);
        }
        return ret;
    }

    /**
     * The Class TypeCriteria.
     */
    private static class TypeCriteria
    {

        /** The sql col type. */
        private final String sqlColType;

        /** The id. */
        private final long id;

        /**
         * Instantiates a new type criteria.
         *
         * @param _sqlColType the sql col type
         * @param _id the id
         */
        TypeCriteria(final String _sqlColType,
                    final long _id)
        {
            this.sqlColType = _sqlColType;
            this.id = _id;
        }

        @Override
        public boolean equals(final Object _obj)
        {
            final boolean ret;
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

    /**
     * The Class TypeCriteria.
     */
    private static class CompanyCriteria
    {

        /** The sql column for company. */
        private final String sqlColCompany;

        /** The id. */
        private final long id;

        /**
         * Instantiates a new type criteria.
         *
         * @param _sqlColCopmany the sql column for company id
         * @param _id the id
         */
        CompanyCriteria(final String _sqlColCompany,
                        final long _id)
        {
            this.sqlColCompany = _sqlColCompany;
            this.id = _id;
        }

        @Override
        public boolean equals(final Object _obj)
        {
            final boolean ret;
            if (_obj instanceof CompanyCriteria) {
                final CompanyCriteria obj = (CompanyCriteria) _obj;
                ret = this.sqlColCompany.equals(obj.sqlColCompany) && this.id == obj.id;
            } else {
                ret = super.equals(_obj);
            }
            return ret;
        }

        @Override
        public int hashCode()
        {
            return this.sqlColCompany.hashCode() + Long.valueOf(this.id).hashCode();
        }
    }
}
