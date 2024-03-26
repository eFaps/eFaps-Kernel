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
package org.efaps.db.print;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.handlers.ArrayListHandler;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.AbstractPrintQuery;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.InstanceQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.db.QueryCache;
import org.efaps.db.QueryKey;
import org.efaps.db.QueryValue;
import org.efaps.db.search.AbstractQPart;
import org.efaps.db.search.QAnd;
import org.efaps.db.search.section.QWhereSection;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.db.wrapper.SQLPart;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.eql.InvokerUtil;
import org.efaps.eql.stmt.parts.where.AbstractWhere;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Select Part for <code>linkfrom[TYPERNAME#ATTRIBUTENAME]</code>.
 *
 * @author The eFaps Team
 */
public class LinkFromSelect
    extends AbstractPrintQuery
{
    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(OneSelect.class);

    /**
     * Name of the Attribute the link to is based on.
     */
    private final String attrName;

    /**
     * Type the {@link #attrName} belongs to.
     */
    private final Type type;

    /**
     * Did this query return a result.
     */
    private boolean hasResult;

    /**
     * Used for Caching.
     */
    private final String key;

    /** The select part. */
    private final LinkFromSelectPart selectPart;

    /**
     * Instantiates a new link from select.
     *
     * @param _linkFrom linkfrom element of the query
     * @param _key used to cache the result
     * @throws CacheReloadException on error
     */
    public LinkFromSelect(final String _linkFrom,
                          final String _key)
        throws CacheReloadException
    {
        this.key = _key;
        final String[] linkfrom = _linkFrom.split("#");
        this.type = Type.get(linkfrom[0]);
        this.attrName = linkfrom[1];
        if (this.type == null) {
            LinkFromSelect.LOG.error("Could not get type for linkfrom: '{}'", _linkFrom);
        }
        LinkFromSelect.LOG.debug("adding linkfrom: '{}'", _linkFrom);
        final OneSelect onsel = new OneSelect(this, _linkFrom);
        addOneSelect(onsel);
        onsel.setFromSelect(this);
        this.selectPart = new LinkFromSelectPart(this.type);
        onsel.getSelectParts().add(this.selectPart);
    }

    /**
     * Adds the where.
     *
     * @param _where the where
     * @throws EFapsException on error
     */
    public void addWhere(final String _where)
        throws EFapsException
    {
        if (_where != null) {
            final List<AbstractWhere> wheres = InvokerUtil.analyzeWheres(_where);
            QWhereSection whereSection = null;
            for (final AbstractWhere where : wheres) {
                final AbstractQPart part = QueryBuilder.getQPart(where);
                if (whereSection == null) {
                    whereSection = new QWhereSection(part);
                } else {
                    whereSection.setPart(new QAnd(whereSection.getPart(), part));
                }
            }
            this.selectPart.setWhereSection(whereSection);
        }
    }

    /**
     * Getter method for instance variable {@link #hasResult}.
     *
     * @return value of instance variable {@link #hasResult}
     */
    public boolean hasResult()
    {
        return this.hasResult;
    }

    /**
     * Execute the from select for the given instance.
     *
     * @param _onesel instance
     * @return true if statement didi return values, else false
     * @throws EFapsException on error
     */
    public boolean execute(final OneSelect _onesel)
        throws EFapsException
    {
        this.hasResult = executeOneCompleteStmt(createSQLStatement(_onesel), getAllSelects());
        if (this.hasResult) {
            for (final OneSelect onesel :  getAllSelects()) {
                if (onesel.getFromSelect() != null && !onesel.getFromSelect().equals(this)) {
                    onesel.getFromSelect().execute(onesel);
                }
            }
        }
        return this.hasResult;
    }

    /**
     * Method to create on Statement out of the different parts.
     *
     * @param _parentOnesel instance
     * @return String containing the SQL statement
     * @throws EFapsException on error
     */
    private String createSQLStatement(final OneSelect _parentOnesel)
        throws EFapsException
    {
        final Attribute attr = this.type.getAttribute(this.attrName);

        if (attr == null) {
            LinkFromSelect.LOG.error("Could not find Attribute '{}' in Type '{}'", this.attrName, this.type.getName());
            throw new EFapsException(LinkFromSelect.class, "NoAttribute");
        }

        final SQLSelect select = new SQLSelect()
                .column(0, "ID")
                .column(0, attr.getSqlColNames().get(0))
                .from(this.type.getMainTable().getSqlTable(), 0);

        // on a from  select only one table is the base
        getAllSelects().get(0).append2SQLFrom(select);

        int colIndex = select.getColumns().size() + 1;

        for (final OneSelect oneSel : getAllSelects()) {
            colIndex += oneSel.append2SQLSelect(select, colIndex);
        }
        select.addPart(SQLPart.WHERE)
            .addColumnPart(0, attr.getSqlColNames().get(0))
            .addPart(SQLPart.IN).addPart(SQLPart.PARENTHESIS_OPEN);

        if (_parentOnesel.isMultiple()) {
            boolean first = true;
            final List<?> ids = (List<?>) _parentOnesel.getObject();
            for (final Object id : ids) {
                if (first) {
                    first = false;
                } else {
                    select.addPart(SQLPart.COMMA);
                }
                select.addValuePart(id);
            }
        } else {
            select.addValuePart(_parentOnesel.getObject());
        }
        select.addPart(SQLPart.PARENTHESIS_CLOSE);

        _parentOnesel.setValueSelect(null);

        // in a subquery the type must also be set
        if (this.type.getMainTable().getSqlColType() != null) {
            select.addPart(SQLPart.AND)
                .addColumnPart(0, this.type.getMainTable().getSqlColType())
                .addPart(SQLPart.IN).addPart(SQLPart.PARENTHESIS_OPEN);
            boolean first = true;
            if (this.type.isAbstract()) {
                for (final Type atype : getAllChildTypes(this.type)) {
                    if (first) {
                        first = false;
                    } else {
                        select.addPart(SQLPart.COMMA);
                    }
                    select.addValuePart(atype.getId());
                }
                if (first) {
                    LinkFromSelect.LOG.error("The type is declared abstract but does not have children: {}", this.type);
                }
            } else {
                select.addValuePart(this.type.getId());
            }
            select.addPart(SQLPart.PARENTHESIS_CLOSE);
        }

        for (final OneSelect oneSel : getAllSelects()) {
            oneSel.append2SQLWhere(select);
        }
        return select.getSQL();
    }

    /**
     * Recursive method to get all child types for a type.
     *
     * @param _parent parent type
     * @return list of all child types
     * @throws CacheReloadException on error
     */
    private List<Type> getAllChildTypes(final Type _parent)
        throws CacheReloadException
    {
        final List<Type> ret = new ArrayList<>();
        for (final Type child : _parent.getChildTypes()) {
            ret.addAll(getAllChildTypes(child));
            ret.add(child);
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    protected boolean executeOneCompleteStmt(final String _complStmt,
                                             final List<OneSelect> _oneSelects)
        throws EFapsException
    {
        boolean ret = false;
        ConnectionResource con = null;
        try {
            LinkFromSelect.LOG.debug("Executing SQLL: {}", _complStmt);
            List<Object[]> rows = null;
            boolean cached = false;
            if (isCacheEnabled()) {
                final QueryKey querykey = QueryKey.get(getKey(), _complStmt);
                final var cache = QueryCache.get();
                if (cache.containsKey(querykey)) {
                    final var queryValue = cache.get(querykey);
                    if (queryValue.getContent() instanceof List) {
                        rows = (List<Object[]>) queryValue.getContent();
                    }
                    cached = true;
                }
            }
            if (!cached) {
                con = Context.getThreadContext().getConnectionResource();
                final Statement stmt = con.createStatement();

                final ResultSet rs = stmt.executeQuery(_complStmt.toString());
                final ArrayListHandler handler = new ArrayListHandler(Context.getDbType().getRowProcessor());
                rows = handler.handle(rs);
                rs.close();
                stmt.close();
                if (isCacheEnabled()) {
                    final var querykey = QueryKey.get(getKey(), _complStmt);
                    final var queryvalue = QueryValue.get(getKey(), rows);
                    final var cache = QueryCache.get();
                    cache.put(querykey, queryvalue);
                }
            }
            for (final Object[] row : rows) {
                for (final OneSelect onesel : _oneSelects) {
                    onesel.addObject(row);
                }
                ret = true;
            }
        } catch (final SQLException e) {
            throw new EFapsException(InstanceQuery.class, "executeOneCompleteStmt", e);
        }
        return ret;
    }

    /**
     * Getter method for instance variable {@link #oneSelect}.
     *
     * @return value of instance variable {@link #oneSelect}
     */
    public OneSelect getMainOneSelect()
    {
        return getAllSelects().get(0);
    }

    /**
     * Returns the {@link #type} for which the attribute {@link #attrName}
     * belongs to.
     *
     * @return type
     * @see #type
     */
    public Type getType()
    {
        return this.type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Instance getCurrentInstance()
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Instance> getInstanceList()
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Type getMainType()
    {
        return getType();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCacheEnabled()
    {
        return this.key != null;
    }

    /**
     * The Class LinkFromSelectPart.
     *
     */
    public static class LinkFromSelectPart
        implements ISelectPart
    {

        /** The type. */
        private final Type type;

        /** The where section. */
        private QWhereSection whereSection;

        /**
         * Instantiates a new link from select part.
         *
         * @param _type the type
         */
        public LinkFromSelectPart(final Type _type)
        {
            this.type = _type;
        }

        /**
         * Sets the where section.
         *
         * @param _whereSection the new where section
         */
        public void setWhereSection(final QWhereSection _whereSection)
        {
            this.whereSection = _whereSection;
        }

        @Override
        public Type getType()
        {
            return this.type;
        }

        @Override
        public int join(final OneSelect _oneselect,
                        final SQLSelect _select,
                        final int _relIndex)
        {
            // nothing to join here
            return 0;
        }

        @Override
        public void addObject(final Object[] _rs)
            throws SQLException
        {
            // no objects must be added
        }

        @Override
        public Object getObject()
        {
            return null;
        }

        @Override
        public void add2Where(final OneSelect _oneselect,
                              final SQLSelect _select)
            throws EFapsException
        {
            if (this.whereSection != null) {
                this.whereSection.getPart().prepare(_oneselect.getQuery(), null);
                _select.addPart(SQLPart.AND);
                this.whereSection.getPart().appendSQL(_select);
            }
        }

        @Override
        public void next()
            throws EFapsException
        {
            // no clause must be added
        }
    }
}
