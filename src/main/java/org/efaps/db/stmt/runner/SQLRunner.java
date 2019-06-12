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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.collections4.MultiMapUtils;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.dbutils.handlers.ArrayListHandler;
import org.efaps.admin.access.user.AccessCache;
import org.efaps.admin.common.Association;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.AttributeType;
import org.efaps.admin.datamodel.SQLTable;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.datamodel.attributetype.ConsortiumLinkType;
import org.efaps.admin.index.Queue;
import org.efaps.admin.user.Company;
import org.efaps.db.Context;
import org.efaps.db.GeneralInstance;
import org.efaps.db.ICacheDefinition;
import org.efaps.db.Instance;
import org.efaps.db.QueryCache;
import org.efaps.db.QueryKey;
import org.efaps.db.stmt.StmtFlag;
import org.efaps.db.stmt.delete.AbstractDelete;
import org.efaps.db.stmt.filter.Filter;
import org.efaps.db.stmt.print.AbstractPrint;
import org.efaps.db.stmt.print.ListPrint;
import org.efaps.db.stmt.print.ObjectPrint;
import org.efaps.db.stmt.print.QueryPrint;
import org.efaps.db.stmt.selection.ISelectionProvider;
import org.efaps.db.stmt.selection.Select;
import org.efaps.db.stmt.selection.elements.AbstractDataElement;
import org.efaps.db.stmt.selection.elements.AbstractElement;
import org.efaps.db.stmt.selection.elements.IOrderable;
import org.efaps.db.stmt.update.AbstractObjectUpdate;
import org.efaps.db.stmt.update.AbstractUpdate;
import org.efaps.db.stmt.update.Insert;
import org.efaps.db.stmt.update.ListUpdate;
import org.efaps.db.stmt.update.ObjectUpdate;
import org.efaps.db.store.Resource;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.db.wrapper.AbstractSQLInsertUpdate;
import org.efaps.db.wrapper.SQLDelete;
import org.efaps.db.wrapper.SQLDelete.DeleteDefintion;
import org.efaps.db.wrapper.SQLInsert;
import org.efaps.db.wrapper.SQLPart;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.db.wrapper.SQLSelect.SQLSelectPart;
import org.efaps.db.wrapper.SQLUpdate;
import org.efaps.db.wrapper.SQLWhere;
import org.efaps.db.wrapper.SQLWhere.Criteria;
import org.efaps.db.wrapper.TableIndexer.TableIdx;
import org.efaps.eql2.Comparison;
import org.efaps.eql2.Connection;
import org.efaps.eql2.IOrder;
import org.efaps.eql2.IOrderElement;
import org.efaps.eql2.IPrintQueryStatement;
import org.efaps.eql2.IStatement;
import org.efaps.eql2.IUpdateElement;
import org.efaps.eql2.IUpdateElementsStmt;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;
import org.infinispan.Cache;
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
        LOG.trace("Preparing: {}", this);
        runnable = _runnable;
        sqlSelect = new SQLSelect();
        if (isPrint()) {
            preparePrint((AbstractPrint) _runnable);
        } else if (isInsert()) {
            prepareInsert();
        } else if (isDelete()){
            prepareDelete();
        } else {
            prepareUpdate();
        }
    }

    private void prepareDelete()
        throws EFapsException
    {
        // Nothing to do yet
    }

    private void prepareUpdate()
        throws EFapsException
    {
        if (runnable instanceof ObjectUpdate) {
            final ObjectUpdate update = (ObjectUpdate) runnable;
            prepareUpdate(update.getInstance().getType(), false);
        } else if (runnable instanceof ListUpdate) {
            final ListUpdate update = (ListUpdate) runnable;
            final Set<Type> types = update.getInstances()
                            .stream().map(instance -> instance.getType())
                            .collect(Collectors.toSet());
            for (final Type type : types) {
                prepareUpdate(type, false);
            }
        }
    }

    private void prepareInsert()
        throws EFapsException
    {
        final Insert insert = (Insert) runnable;
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
                try {
                    final SQLTable sqlTable = attr.getTable();
                    if (_create) {
                        final SQLInsert sqlInsert = getSQLInsert(sqlTable);
                        attr.prepareDBInsert(sqlInsert);
                    } else {
                        final SQLUpdate sqlUpdate = getSQLUpdate(_type, sqlTable);
                        attr.prepareDBUpdate(sqlUpdate);
                    }
                } catch (final SQLException e) {
                    throw new EFapsException(SQLRunner.class, "prepareInsert", e);
                }
            }
        }

        final IUpdateElementsStmt<?> eqlStmt = ((AbstractUpdate) runnable).getEqlStmt();
        for (final IUpdateElement element : eqlStmt.getUpdateElements()) {
            final Attribute attr = _type.getAttribute(element.getAttribute());
            final SQLTable sqlTable = attr.getTable();
            try {
                if (_create) {
                    final SQLInsert sqlInsert = getSQLInsert(sqlTable);
                    attr.prepareDBInsert(sqlInsert, element.getValue());
                } else {
                    final SQLUpdate sqlUpdate = getSQLUpdate(_type, sqlTable);
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
        if (updatemap.containsKey(_sqlTable)) {
            ret = (SQLInsert) updatemap.get(_sqlTable);
        } else {
            ret = Context.getDbType().newInsert(_sqlTable.getSqlTable(), _sqlTable.getSqlColId(), updatemap
                            .isEmpty());
            updatemap.put(_sqlTable, ret);
        }
        return ret;
    }

    private SQLUpdate getSQLUpdate(final Type _type, final SQLTable _sqlTable)
    {
        SQLUpdate ret;
        if (updatemap.containsKey(_sqlTable)) {
            ret = (SQLUpdate) updatemap.get(_sqlTable);
        } else {
            final AbstractUpdate update = (AbstractUpdate) runnable;
            final Long[] ids;
            if (update instanceof AbstractObjectUpdate) {
                ids = new Long[] { ((AbstractObjectUpdate) update).getInstance().getId()};
            } else if (update instanceof ListUpdate) {
                ids = ((ListUpdate) update).getInstances().stream()
                                .filter(instance -> instance.getType().equals(_type))
                                .map(instance -> instance.getId())
                                .toArray(Long[]::new);
            } else {
                ids = new Long[] { 0L };
            }
            ret = Context.getDbType().newUpdate(_sqlTable.getSqlTable(), _sqlTable.getSqlColId(), ids);
            updatemap.put(_sqlTable, ret);
        }
        return ret;
    }

    /**
     * Prepare print.
     *
     * @param _print the print
     * @throws EFapsException the e faps exception
     */
    private void preparePrint(final AbstractPrint _print)
        throws EFapsException
    {
        final IStatement<?> stmt = _print.getStmt();
        IOrder order = null;
        if (stmt instanceof IPrintQueryStatement) {
            order = ((IPrintQueryStatement) stmt).getOrder();
        }
        int idx = 1;
        for (final Select select : _print.getSelection().getAllSelects()) {
            for (final AbstractElement<?> element : select.getElements()) {
                if (element instanceof AbstractDataElement) {
                    ((AbstractDataElement<?>) element).append2SQLSelect(sqlSelect);
                }
            }
            if (order != null) {
                int orderIdx = 0;
                for (final IOrderElement orderElement: order.getElementsList()) {
                    if (orderElement.getKey().equals(select.getAlias()) || orderElement.getKey().equals(String.valueOf(idx))) {
                        final List<AbstractElement<?>> orderables = select.getElements().stream()
                                        .filter(element -> element instanceof IOrderable)
                                        .collect(Collectors.toList());
                        if (orderables.isEmpty()) {
                            LOG.warn("Cannot add order for Key: {}", orderElement);
                        } else {
                            ((IOrderable) orderables.get(orderables.size() - 1)).append2SQLOrder(orderIdx, sqlSelect.getOrder(),
                                            orderElement.isDesc());
                        }
                        break;
                    }
                    orderIdx++;
                }
            }
            idx++;
        }
        if (sqlSelect.getColumns().size() > 0) {
            for (final Select select : _print.getSelection().getAllSelects()) {
                for (final AbstractElement<?> element : select.getElements()) {
                    if (element instanceof AbstractDataElement) {
                        ((AbstractDataElement<?>) element).append2SQLWhere(sqlSelect.getWhere());
                    }
                }
            }
            if (_print instanceof ObjectPrint) {
                addWhere4ObjectPrint((ObjectPrint) _print);
            } else if (_print instanceof ListPrint) {
                addWhere4ListPrint((ListPrint) _print);
            } else {
                addTypeCriteria((QueryPrint) _print);
                addWhere4QueryPrint((QueryPrint) _print);
            }
            addCompanyCriteria(_print);
            addAssociationCriteria(_print);
        }
    }

    /**
     * Checks if is prints the.
     *
     * @return true, if is prints the
     */
    private boolean isPrint() {
        return runnable instanceof AbstractPrint;
    }

    private boolean isInsert() {
        return runnable instanceof Insert;
    }

    private boolean isDelete() {
        return runnable instanceof AbstractDelete;
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
            final TableIdx tableIdx = sqlSelect.getIndexer().getTableIdx(tableName);
            if (tableIdx.isCreated()) {
                sqlSelect.from(tableIdx.getTable(), tableIdx.getIdx());
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
            final SQLWhere where = sqlSelect.getWhere();
            for (final Entry<TableIdx, CompanyCriteria> entry : companyCriterias.entrySet()) {
                final boolean isConsortium = Type.get(entry.getValue().id).getCompanyAttribute()
                                .getAttributeType().getClassRepr().equals(ConsortiumLinkType.class);
                List<String> ids;
                if (_print.has(StmtFlag.COMPANYINDEPENDENT)) {
                    if (isConsortium) {
                       ids = Context.getThreadContext().getPerson().getCompanies().stream()
                            .map(compId -> {
                                try {
                                    return Company.get(compId).getConsortiums().stream();
                                } catch (final CacheReloadException e) {
                                    return Arrays.asList(compId).stream();
                                }
                            })
                            .map(id -> String.valueOf(id))
                            .collect(Collectors.toList());
                    } else {
                        ids = Context.getThreadContext().getPerson().getCompanies().stream()
                            .map(id -> String.valueOf(id))
                            .collect(Collectors.toList());
                    }
                } else {
                    if (isConsortium) {
                        ids =  Context.getThreadContext().getCompany().getConsortiums().stream()
                                        .map(id -> String.valueOf(id))
                                        .collect(Collectors.toList());
                    } else {
                        ids = new ArrayList<>();
                        ids.add(String.valueOf(Context.getThreadContext().getCompany().getId()));
                    }
                }
                where.addCriteria(entry.getKey().getIdx(),
                                Collections.singletonList(entry.getValue().sqlColCompany),
                                ids.size() > 1 ? Comparison.IN : Comparison.EQUAL, new LinkedHashSet<>(ids),
                                                false, Connection.AND);
            }
        }
    }


    /**
     * Adds the company criteria.
     *
     * @param _print the print
     * @throws EFapsException the e faps exception
     */
    private void addAssociationCriteria(final AbstractPrint _print)
        throws EFapsException
    {
        final Map<TableIdx, AssociationCriteria> associationCriterias = new HashMap<>();
        final List<Type> types = _print.getTypes().stream().sorted((type1, type2) -> Long.compare(type1.getId(), type2
                        .getId())).collect(Collectors.toList());
        for (final Type type : types) {
            final String tableName = type.getMainTable().getSqlTable();
            final TableIdx tableIdx = sqlSelect.getIndexer().getTableIdx(tableName);
            if (tableIdx.isCreated()) {
                sqlSelect.from(tableIdx.getTable(), tableIdx.getIdx());
            }
            if (type.hasAssociation()) {
                final String columnName = type.getAssociationAttribute().getSqlColNames().get(0);
                associationCriterias.put(tableIdx, new AssociationCriteria(columnName, type.getId()));
            }
        }
        if (!associationCriterias.isEmpty()) {
            if (Context.getThreadContext().getCompany() == null) {
                throw new EFapsException(SQLRunner.class, "noCompany");
            }
            final SQLWhere where = sqlSelect.getWhere();
            for (final Entry<TableIdx, AssociationCriteria> entry : associationCriterias.entrySet()) {
                final List<String> ids = new ArrayList<>();
                if (_print.has(StmtFlag.COMPANYINDEPENDENT)) {
                    for (final Long companyId : Context.getThreadContext().getPerson().getCompanies()) {
                        ids.add(String.valueOf(Association.evaluate(Type.get(entry.getValue().typeId), companyId)));
                    }
                } else {
                    final Association association = Association.evaluate(Type.get(entry.getValue().typeId));
                    if (association == null) {
                        LOG.debug("No valid Association was found");
                        ids.add("0");
                    }else {
                        ids.add(String.valueOf(association.getId()));
                    }
                }
                where.addCriteria(entry.getKey().getIdx(),
                                Collections.singletonList(entry.getValue().sqlColAssociation),
                                ids.size() > 1 ? Comparison.IN : Comparison.EQUAL, new LinkedHashSet<>(ids),
                                                false, Connection.AND);
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
            final TableIdx tableIdx = sqlSelect.getIndexer().getTableIdx(tableName);
            if (tableIdx.isCreated()) {
                sqlSelect.from(tableIdx.getTable(), tableIdx.getIdx());
            }
            if (type.getMainTable().getSqlColType() != null) {
                typeCriterias.put(tableIdx, new TypeCriteria(type.getMainTable().getSqlColType(), type.getId()));
            }
        }
        if (!typeCriterias.isEmpty()) {
            final SQLWhere where = sqlSelect.getWhere();

            for (final TableIdx tableIdx : typeCriterias.keySet()) {
                final Collection<TypeCriteria> criterias = typeCriterias.get(tableIdx);
                final Iterator<TypeCriteria> iter = criterias.iterator();
                final TypeCriteria typeCriteria = iter.next();

                final Criteria criteria = where.addCriteria(tableIdx.getIdx(), typeCriteria.sqlColType,
                                iter.hasNext() ? Comparison.IN : Comparison.EQUAL,
                                                String.valueOf(typeCriteria.id), Connection.AND);

                while (iter.hasNext()) {
                    criteria.value(String.valueOf(iter.next().id));
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
        filter.append2SQLSelect(sqlSelect);
    }

    /**
     * Adds the where.
     */
    private void addWhere4ObjectPrint(final ObjectPrint _print)
    {
        final SQLWhere where = sqlSelect.getWhere();
        where.addCriteria(0, "ID", Comparison.EQUAL, String.valueOf(_print.getInstance().getId()), Connection.AND);
    }

    /**
     * Adds the where.
     */
    private void addWhere4ListPrint(final ListPrint _print)
    {
        final SQLSelectPart currentPart = sqlSelect.getCurrentPart();
        if (currentPart == null) {
            sqlSelect.addPart(SQLPart.WHERE);
        } else {
            sqlSelect.addPart(SQLPart.AND);
        }
        sqlSelect.addColumnPart(0, "ID")
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
        LOG.trace("Executing: {}", this);
        if (isPrint()) {
            executeSQLStmt((ISelectionProvider) runnable, sqlSelect.getSQL());
        } else if (isInsert()) {
            executeInserts();
        } else if (isDelete()) {
            executeDeletes();
        } else {
            executeUpdates();
        }
    }

    /**
     * Execute the inserts.
     *
     * @throws EFapsException the e faps exception
     */
    private void executeDeletes()
        throws EFapsException
    {
        final AbstractDelete delete = (AbstractDelete) runnable;
        for (final Instance instance : delete.getInstances()) {
            final Context context = Context.getThreadContext();
            final ConnectionResource con = context.getConnectionResource();
            // first remove the storeresource, because the information needed
            // from the general
            // instance to actually delete will be removed in the second step
            Resource storeRsrc = null;
            try {
                if (instance.getType().hasStore()) {
                    storeRsrc = context.getStoreResource(instance, Resource.StoreEvent.DELETE);
                    storeRsrc.delete();
                }
            } finally {
                if (storeRsrc != null && storeRsrc.isOpened()) {
                }
            }
            try {
                final List<DeleteDefintion> defs = new ArrayList<>();
                defs.addAll(GeneralInstance.getDeleteDefintion(instance, con));
                final SQLTable mainTable = instance.getType().getMainTable();
                for (final SQLTable curTable : instance.getType().getTables()) {
                    if (!curTable.equals(mainTable) && !curTable.isReadOnly()) {
                        defs.add(new DeleteDefintion(curTable.getSqlTable(), curTable.getSqlColId(), instance.getId()));
                    }
                }
                defs.add(new DeleteDefintion(mainTable.getSqlTable(), mainTable.getSqlColId(), instance.getId()));
                final SQLDelete sqlDelete = Context.getDbType().newDelete(defs.toArray(new DeleteDefintion[defs
                                .size()]));
                sqlDelete.execute(con);
                AccessCache.registerUpdate(instance);
                Queue.registerUpdate(instance);
            } catch (final SQLException e) {
                throw new EFapsException(getClass(), "executeWithoutAccessCheck.SQLException", e, instance);
            }
        }
    }

    /**
     * Execute the update.
     *
     * @throws EFapsException the e faps exception
     */
    private void executeUpdates()
        throws EFapsException
    {
        ConnectionResource con = null;
        try {
            con = Context.getThreadContext().getConnectionResource();
            for (final Entry<SQLTable, AbstractSQLInsertUpdate<?>> entry : updatemap.entrySet()) {
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
            final Insert insert = (Insert) runnable;
            con = Context.getThreadContext().getConnectionResource();
            long id = 0;
            for (final Entry<SQLTable, AbstractSQLInsertUpdate<?>> entry : updatemap.entrySet()) {
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
    @SuppressWarnings("unchecked")
    protected boolean executeSQLStmt(final ISelectionProvider _sqlProvider, final String _complStmt)
        throws EFapsException
    {
        SQLRunner.LOG.debug("SQL-Statement: {}", _complStmt);

        boolean ret = false;
        List<Object[]> rows = new ArrayList<>();

        boolean cached = false;
        if (runnable.has(StmtFlag.REQCACHED)) {
            final QueryKey querykey = QueryKey.get(Context.getThreadContext().getRequestId(), _complStmt);
            final Cache<QueryKey, Object> cache = QueryCache.getSqlCache();
            if (cache.containsKey(querykey)) {
                final Object object = cache.get(querykey);
                if (object instanceof List) {
                    rows = (List<Object[]>) object;
                }
                cached = true;
            }
        }
        if (!cached) {
            ConnectionResource con = null;
            try {
                con = Context.getThreadContext().getConnectionResource();
                final Statement stmt = con.createStatement();
                final ResultSet rs = stmt.executeQuery(_complStmt);
                final ArrayListHandler handler = new ArrayListHandler(Context.getDbType().getRowProcessor());
                rows = handler.handle(rs);
                rs.close();
                stmt.close();
            } catch (final SQLException e) {
                throw new EFapsException(SQLRunner.class, "executeOneCompleteStmt", e);
            }
            if (runnable.has(StmtFlag.REQCACHED)) {
                final ICacheDefinition cacheDefinition = new ICacheDefinition() {

                    @Override
                    public long getLifespan()
                    {
                        return 5;
                    }

                    @Override
                    public TimeUnit getLifespanUnit()
                    {
                        return TimeUnit.MINUTES;
                    }
                };
                QueryCache.put(cacheDefinition, QueryKey.get(Context.getThreadContext().getRequestId(), _complStmt), rows);
            }
        }
        for (final Object[] row : rows) {
            for (final Select select : _sqlProvider.getSelection().getAllSelects()) {
                select.addObject(row);
            }
            ret = true;
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
            sqlColType = _sqlColType;
            id = _id;
        }

        @Override
        public boolean equals(final Object _obj)
        {
            final boolean ret;
            if (_obj instanceof TypeCriteria) {
                final TypeCriteria obj = (TypeCriteria) _obj;
                ret = sqlColType.equals(obj.sqlColType) && id == obj.id;
            } else {
                ret = super.equals(_obj);
            }
            return ret;
        }

        @Override
        public int hashCode()
        {
            return sqlColType.hashCode() + Long.valueOf(id).hashCode();
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
            sqlColCompany = _sqlColCompany;
            id = _id;
        }

        @Override
        public boolean equals(final Object _obj)
        {
            final boolean ret;
            if (_obj instanceof CompanyCriteria) {
                final CompanyCriteria obj = (CompanyCriteria) _obj;
                ret = sqlColCompany.equals(obj.sqlColCompany) && id == obj.id;
            } else {
                ret = super.equals(_obj);
            }
            return ret;
        }

        @Override
        public int hashCode()
        {
            return sqlColCompany.hashCode() + Long.valueOf(id).hashCode();
        }
    }

    /**
     * The Class TypeCriteria.
     */
    private static class AssociationCriteria
    {

        /** The sql column for company. */
        private final String sqlColAssociation;

        /** The id. */
        private final long typeId;

        /**
         * Instantiates a new type criteria.
         *
         * @param _sqlColCopmany the sql column for company id
         * @param _id the id
         */
        AssociationCriteria(final String _sqlColAssociation,
                        final long _typeId)
        {
            sqlColAssociation = _sqlColAssociation;
            typeId = _typeId;
        }

        @Override
        public boolean equals(final Object _obj)
        {
            final boolean ret;
            if (_obj instanceof AssociationCriteria) {
                final AssociationCriteria obj = (AssociationCriteria) _obj;
                ret = sqlColAssociation.equals(obj.sqlColAssociation) && typeId == obj.typeId;
            } else {
                ret = super.equals(_obj);
            }
            return ret;
        }

        @Override
        public int hashCode()
        {
            return sqlColAssociation.hashCode() + Long.valueOf(typeId).hashCode();
        }
    }
}
