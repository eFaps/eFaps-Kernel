/*
 * Copyright 2003 - 2009 The eFaps Team
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

package org.efaps.db.query;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.AttributeSet;
import org.efaps.admin.datamodel.IAttributeType;
import org.efaps.admin.datamodel.IMultipleAttributeType;
import org.efaps.admin.datamodel.SQLTable;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.ListQuery;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.util.EFapsException;

/**
 * @todo description
 * @author The eFasp Team
 * @version $Id$
 */
public class OneRoundQuery
{

    /**
     * Stores all instances for which this query is executed.
     */
    private final List<Instance> instances;

    /**
     * The main sql table.
     */
    private final SQLTable mainSQLTable;

    /**
     * The type of this query.
     */
    private final Type type;

    /**
     * Stores all select statements for this query.
     */
    private final Set<String> selects;

    /**
     * Mapping of type to typemapping.
     */
    private final Map<Type, TypeMapping2Instances> typeMappings = new HashMap<Type, TypeMapping2Instances>();

    /**
     * Mappeing of sql tables.
     */
    private final Map<SQLTable, SQLTableMapping2Attributes> sqlTableMappings
                                                                  = new HashMap<SQLTable, SQLTableMapping2Attributes>();

    /**
     * The result of this query will be cached.
     */
    private final CachedResult cachedResult = new CachedResult();

    /**
     * Number of the column containing the type id.
     */
    private int colTypeId = 0;

    /**
     * Listquery this query is based on.
     */
    private final ListQuery listquery;

    /**
     * @param _instances list of instances for which this query is executed
     * @param _selects aset of attributes to be selected
     * @param _listquery listquery
     * @todo check das alle instanzen von gleicher main table sind
     * @todo if no column for the type exists, all types must be the same!
     */
    public OneRoundQuery(final List<Instance> _instances, final Set<String> _selects, final ListQuery _listquery)
    {

        this.instances = _instances;
        this.selects = _selects;
        this.listquery = _listquery;
        if (this.listquery.getAttributeSet() != null) {
            this.mainSQLTable = this.listquery.getAttributeSet().getMainTable();
            this.type = this.listquery.getAttributeSet();
        } else {
            this.mainSQLTable = _instances.get(0).getType().getMainTable();

            // if no column for the type exists, the type must be defined
            // directly
            if (this.mainSQLTable.getSqlColType() == null) {
                this.type = _instances.get(0).getType();
            } else {
                this.type = null;
            }

            // das muss nur gemacht werden, wenn unterschiedliche typen
            // existieren!?
            final SQLTableMapping2Attributes tmp = new SQLTableMapping2Attributes(this.mainSQLTable);
            tmp.addInstances(this.instances);
            this.sqlTableMappings.put(this.mainSQLTable, tmp);
        }
    }

    /**
     * Method to execute the Query.
     *
     * @throws EFapsException on error during execution of statement
     */
    public void execute() throws EFapsException
    {

        if (this.listquery.getAttributeSet() == null) {
            // make type mapping to instances
            for (final Instance instance : this.instances) {
                TypeMapping2Instances typeMapping = this.typeMappings.get(instance.getType());
                if (typeMapping == null) {
                    typeMapping = new TypeMapping2Instances(instance.getType());
                    this.typeMappings.put(instance.getType(), typeMapping);
                }
                typeMapping.addInstance(instance);
            }

            for (final TypeMapping2Instances typeMapping : this.typeMappings.values()) {
                typeMapping.evaluateSelects();
            }

            // evalute sql statements
            int curIndex = 2;
            for (final SQLTableMapping2Attributes sqlTableMapping : this.sqlTableMappings.values()) {
                curIndex = sqlTableMapping.evaluateSQLStatement(curIndex - 1);
            }

        } else {
            // expand
            for (final Instance instance : this.instances) {
                TypeMapping2Instances typeMapping = this.typeMappings.get(this.type);
                if (typeMapping == null) {
                    typeMapping = new TypeMapping2Instances(this.type);
                    this.typeMappings.put(this.type, typeMapping);
                }
                typeMapping.addInstance(instance);
            }
            for (final TypeMapping2Instances typeMapping : this.typeMappings.values()) {
                typeMapping.evaluateSelects();
            }
            // evalute sql statements
            int curIndex = 2;
            for (final SQLTableMapping2Attributes sqlTableMapping : this.sqlTableMappings.values()) {
                sqlTableMapping.setExpand(true);
                sqlTableMapping.setAttributeSet((this.listquery.getAttributeSet()));
                curIndex = sqlTableMapping.evaluateSQLStatement(curIndex - 1);
            }
        }
        beforeFirst();

        // get index of type id
        if (this.mainSQLTable.getSqlColType() != null) {
            final SQLTableMapping2Attributes sqlTableMapping = this.sqlTableMappings.get(this.mainSQLTable);
            this.colTypeId = sqlTableMapping.col2index.get(this.mainSQLTable.getSqlColType());
        }
    }

    /**
     * Adds one select statement to this query.
     *
     * @param _select select statement to add
     * @see #selects
     */
    public void addSelect(final String _select)
    {
        this.selects.add(_select);
    }

    /**
     * @return <i>true</i> if a new row is selected and exists, otherwise
     *         <i>false</i>
     */
    public boolean next()
    {
        return this.cachedResult.next();
    }

    /**
     * Move the cached result before the first.
     */
    public void beforeFirst()
    {
        this.cachedResult.beforeFirst();
    }

    /**
     * Move the cached result to a defined position.
     *
     * @param _key Key to move to
     * @return true if cache result was moves sucessfully
     */
    public boolean gotoKey(final Object _key)
    {
        return this.cachedResult.gotoKey(_key);
    }

    /**
     * The instance method returns for the given key the attribute value.
     *
     * @param _expression expression for which the attribute value must returned
     * @return attribute value for given key
     * @throws EFapsException
     * @throws Exception on error
     */
    public Object getValue(final String _expression) throws EFapsException
    {
        Object ret = null;

        Type typeTmp = getType();
        TypeMapping2Instances typeMapping = this.typeMappings.get(typeTmp);
        while ((typeTmp != null) && (typeMapping == null)) {
            typeTmp = typeTmp.getParentType();
            typeMapping = this.typeMappings.get(typeTmp);
        }
        ret = typeMapping.getValue(_expression);
        return ret;
    }

    /**
     * Get the type of this query.
     *
     * @return Type of this query
     */
    public Type getType()
    {
        Type ret = this.type;
        if (this.colTypeId > 0) {
            ret = Type.get(this.cachedResult.getLong(this.colTypeId));
        }
        return ret;
    }

    /**
     * Method to get the first instance of the cached result.
     *
     * @return Instance
     *
     */
    public Instance getInstance()
    {
        return Instance.get(getType(), this.cachedResult.getLong(1));
    }

    /**
     * Method to get all instances.
     *
     * @return List of instances
     */
    public List<Instance> getInstances()
    {
        if (this.listquery.getAttributeSet() != null) {
            this.instances.clear();
            final SQLTableMapping2Attributes sqlTableMapping = this.sqlTableMappings.get(this.mainSQLTable);
            final List<?> ids = (List<?>) this.cachedResult.getObject(sqlTableMapping.col2index.get(this.mainSQLTable
                            .getSqlColId()));
            for (final Object id : ids) {
                this.instances.add(Instance.get(getType(), (Long) id));
            }
        }
        return this.instances;
    }

    /**
     * The instance method returns for the given key the attribute.
     *
     * @param _expression expression for which the attribute value must returned
     * @return attribute for given expression
     *
     */
    public Attribute getAttribute(final String _expression)
    {
        Attribute ret = null;
        ret = getType().getAttribute(_expression);
        if (ret == null) {
            ret = getType().getLinks().get(_expression);
        }
        return ret;
    }

    /**
     * The instance method returns for the given key the attribute set.
     *
     * @param _expression expression for which the attribute value must returned
     * @return attribute for given expression
     */
    public AttributeSet getAttributeSet(final String _expression)
    {
        return (AttributeSet) Type.get(AttributeSet.evaluateName(getType().getName(), _expression));
    }

    /**
     * Method to get the values of a attribute set.
     *
     * @return Object
     * @throws Exception on error
     */
    public Object getMultiLineValue() throws Exception
    {

        Object ret = null;
        final Map<Integer, String> indexes = new HashMap<Integer, String>();

        for (final SQLTableMapping2Attributes sql2attr : this.sqlTableMappings.values()) {
            for (final String select : this.selects) {
                final Attribute attr = this.type.getAttribute(select);
                if (attr != null) {
                    final List<Integer> idx = sql2attr.attr2index.get(attr);
                    if (idx != null) {
                        indexes.put(idx.get(0), attr.getName());
                    }
                }
            }
        }

        final IMultipleAttributeType attrInterf = this.listquery.getAttributeSet().getAttributeTypeInstance();
        ret = attrInterf.readValues(OneRoundQuery.this.cachedResult, indexes);
        return ret;
    }

    /**
     * Class used to store all types related to instances.
     */
    private class TypeMapping2Instances
    {

        /**
         * Defines the instances for which this type mapping to instances is
         * defined.
         *
         * @see #addInstance
         */
        private final Set<Instance> instances = new HashSet<Instance>();

        /**
         * Stores the type for which this type mapping is defined.
         */
        private final Type type;

        /**
         * Mapping between the expression and the attribute.
         */
        private final Map<String, Attribute> expr2Attr = new HashMap<String, Attribute>();

        /**
         *
         */
        private final Set<String> multiExpr = new HashSet<String>();

        /**
         *
         */
        private final Map<SQLTable, SQLTableMapping2Attributes> sqlTable2Attrs
                                                                = new HashMap<SQLTable, SQLTableMapping2Attributes>();

        /**
         * @param _type type for which this type mapping is defined
         * @see type
         */
        public TypeMapping2Instances(final Type _type)
        {
            this.type = _type;
        }

        /**
         * Adds an instance to the type mapping to instances.
         *
         * @param _instance instance to add @
         * @see #instances
         */
        public void addInstance(final Instance _instance)
        {
            this.instances.add(_instance);
        }

        /**
         * Method to evaluate the selects.
         */
        public void evaluateSelects()
        {
            for (final String select : OneRoundQuery.this.selects) {
                final Attribute attr = this.type.getAttribute(select);
                if (attr != null) {
                    this.expr2Attr.put(select, attr);
                } else {
                    final AttributeSet set = AttributeSet.find(this.type.getName(), select);
                    if (set != null) {
                        for (final String subSelect : set.getSetAttributes()) {
                            ListQuery subQuery = OneRoundQuery.this.listquery.getSubSelects().get(select);
                            if (subQuery == null) {
                                subQuery = new ListQuery();
                                OneRoundQuery.this.listquery.getSubSelects().put(select, subQuery);
                            }
                            subQuery.addSelect(subSelect);
                            subQuery.setExpand(set);
                        }
                        OneRoundQuery.this.listquery.getMultiSelects().add(select);
                        this.multiExpr.add(select);
                    }
                }
            }

            for (final Attribute attribute : this.expr2Attr.values()) {
                SQLTableMapping2Attributes sqlTable2Attr = this.sqlTable2Attrs.get(attribute.getTable());
                if (sqlTable2Attr == null) {
                    sqlTable2Attr = OneRoundQuery.this.sqlTableMappings.get(attribute.getTable());
                    if (sqlTable2Attr == null) {
                        sqlTable2Attr = new SQLTableMapping2Attributes(attribute.getTable());
                        OneRoundQuery.this.sqlTableMappings.put(attribute.getTable(), sqlTable2Attr);
                    }
                    this.sqlTable2Attrs.put(attribute.getTable(), sqlTable2Attr);
                }
                sqlTable2Attr.addAttribute(attribute);
            }
            // in case that only fieldsets are selected, it must be added at least one SQLTableMapping2Attributes
            // to the sqlTable2Atte mapping so that the table is stored for later access
            if (this.expr2Attr.values().isEmpty() && !this.multiExpr.isEmpty()) {
                final SQLTableMapping2Attributes sqlTable2Attr
                                                      = new SQLTableMapping2Attributes(OneRoundQuery.this.mainSQLTable);
                OneRoundQuery.this.sqlTableMappings.put(OneRoundQuery.this.mainSQLTable, sqlTable2Attr);
                this.sqlTable2Attrs.put(OneRoundQuery.this.mainSQLTable, sqlTable2Attr);
            }

            // add all instances to the sql table mapping
            for (final SQLTableMapping2Attributes sqlTableMapping : this.sqlTable2Attrs.values()) {
                sqlTableMapping.addInstances(this.instances);
            }
        }

        /**
         * Method to get the value for an expression.
         *
         * @param _expr expression the value will be returned for
         * @return Object
         * @throws EFapsException
         * @throws Exception on error
         */
        public Object getValue(final String _expr) throws EFapsException
        {
            // System.out.println("getValue.expression="+_expression);
            Object ret = null;
            final Attribute attr = this.expr2Attr.get(_expr);
            if (attr != null) {
                final SQLTableMapping2Attributes sqlTable2attr = this.sqlTable2Attrs.get(attr.getTable());
                if (sqlTable2attr != null) {
                    ret = sqlTable2attr.getValue(attr);
                } else {
                    System.out.println("!!! NULLLLLLLL " + _expr);
                }
            } else {
                // in case we have an expand we return the id of the object
                if (_expr.contains("\\") || this.multiExpr.contains(_expr)) {
                    final SQLTableMapping2Attributes sqlTable2attr = this.sqlTable2Attrs.get(getType().getMainTable());
                    if (sqlTable2attr != null) {
                        final Integer idx = sqlTable2attr.col2index.get(getType().getMainTable().getSqlColId());
                        ret = OneRoundQuery.this.cachedResult.getLong(idx);
                    }
                }
            }
            return ret;
        }

        /**
         * Returns a string representation of this type mapping to instances.
         *
         * @return string representation of this type mapping to instances
         */
        @Override
        public String toString()
        {
            return new ToStringBuilder(this).appendSuper(super.toString()).append("type", this.type.toString()).append(
                            "instances", this.instances.toString()).append("sqlTable2Attrs",
                            this.sqlTable2Attrs.toString()).toString();
        }
    }

    /**
     * Class used to store all sql tables related to attributes.
     */
    private class SQLTableMapping2Attributes
    {

        /**
         * Sql table.
         */
        private final SQLTable sqlTable;

        /**
         * @see #addAttribute
         */
        private final Set<Attribute> attributes = new HashSet<Attribute>();

        /**
         * List of all columns.
         */
        private final List<String> cols = new ArrayList<String>();

        /**
         * Mapping of name of the column to its index.
         */
        private final Map<String, Integer> col2index = new HashMap<String, Integer>();

        /**
         * Mapping of attribute to indexes.
         */
        private final Map<Attribute, List<Integer>> attr2index = new HashMap<Attribute, List<Integer>>();

        /**
         * Set of Instances.
         */
        private final Set<Instance> instances = new HashSet<Instance>();

        /**
         * Actual index.
         */
        private int index = 0;

        /**
         * Is this query an expand.
         */
        private boolean expand;

        /**
         * Has the expand a result.
         */
        private boolean expandHasResult = true;

        /**
         * Contains the active attributeset.
         */
        private AttributeSet attributeSet;

        /**
         * Constructor.
         *
         * @param _sqlTable SQL table
         */
        public SQLTableMapping2Attributes(final SQLTable _sqlTable)
        {
            this.sqlTable = _sqlTable;
            this.col2index.put(this.sqlTable.getSqlColId(), this.index++);
            this.cols.add(this.sqlTable.getSqlColId());

            if (this.sqlTable.getSqlColType() != null) {
                this.col2index.put(this.sqlTable.getSqlColType(), this.index++);
                this.cols.add(this.sqlTable.getSqlColType());
            }
        }

        /**
         * Set an attribute set.
         *
         * @param _attributeSet AttributeSet
         */
        public void setAttributeSet(final AttributeSet _attributeSet)
        {
            this.attributeSet = _attributeSet;
            final String column = this.attributeSet.getSqlColNames().get(0);
            if (!this.col2index.containsKey(column)) {
                this.col2index.put(column, this.index++);
                this.cols.add(column);
            }
        }

        /**
         * Method to add an attribute.
         *
         * @param _attribute Attribute to add
         */
        public void addAttribute(final Attribute _attribute)
        {
            if (!this.attr2index.containsKey(_attribute)) {
                final ArrayList<Integer> idxs = new ArrayList<Integer>();
                for (final String col : _attribute.getSqlColNames()) {
                    Integer idx = this.col2index.get(col);
                    if (idx == null) {
                        idx = this.index++;
                        this.col2index.put(col, idx);
                        this.cols.add(col);
                    }
                    idxs.add(idx);
                }
                this.attr2index.put(_attribute, idxs);
            }
            this.attributes.add(_attribute);
        }

        /**
         * Method to add a collection of Instances.
         *
         * @param _instances Instance to add
         */
        public void addInstances(final Collection<Instance> _instances)
        {
            this.instances.addAll(_instances);
        }

        /**
         * Method to get the value for an attribute.
         *
         * @param _attribute Attribute the value is wanted for
         * @return value Object
         * @throws EFapsException
         * @throws Exception on error
         */
        public Object getValue(final Attribute _attribute) throws EFapsException
        {
            final IAttributeType attrInterf = _attribute.newInstance();
            Object ret = null;
            if (this.expandHasResult) {
                try {
                    ret = attrInterf.readValue(OneRoundQuery.this.cachedResult, this.attr2index.get(_attribute));
                } catch (final Exception e) {
                    // TODO correct errorhandling
                    throw new EFapsException(SQLTableMapping2Attributes.class, "TODO", e);
                }
            }
            return ret;
        }

        /**
         * Method to evaluate a sql statement.
         *
         * @param _startIndex index to start from
         * @return new index
         * @throws EFapsException on error during execution of statement
         */
        public int evaluateSQLStatement(final int _startIndex) throws EFapsException
        {

            final int maxExpression = Context.getDbType().getMaxExpressions();
            final List<StringBuilder> instSQLs = new ArrayList<StringBuilder>();
            StringBuilder instSQL = new StringBuilder();
            instSQLs.add(instSQL);
            int i = 0;
            for (final Instance instance : this.instances) {
                i++;
                if (i > maxExpression - 1 && maxExpression > 0) {
                    instSQL.deleteCharAt(instSQL.length() - 1);
                    instSQL = new StringBuilder();
                    instSQLs.add(instSQL);
                    i = 0;
                }
                instSQL.append(instance.getId()).append(",");
            }
            if (this.instances.size() > 0) {
                instSQL.deleteCharAt(instSQL.length() - 1);
            }

            // update mapping from attribute to indexes
            for (final Map.Entry<Attribute, List<Integer>> entry : this.attr2index.entrySet()) {
                final List<Integer> newList = new ArrayList<Integer>();
                for (int curIndex : entry.getValue()) {
                    if (curIndex > 0) {
                        curIndex += _startIndex;
                    } else {
                        curIndex = 1;
                    }
                    newList.add(curIndex);
                }
                this.attr2index.put(entry.getKey(), newList);
            }

            // update mapping from columns to index
            for (final Map.Entry<String, Integer> entry : this.col2index.entrySet()) {
                int curIndex = entry.getValue();
                if (curIndex > 0) {
                    curIndex += _startIndex;
                } else {
                    curIndex = 1;
                }
                this.col2index.put(entry.getKey(), curIndex);
            }

            final boolean shiftIndex = executeSQLStatement(instSQLs);
            // if we don't want to shift we must return the startvalue again
            return shiftIndex ? (_startIndex + this.index) : _startIndex + 1;
        }

        /**
         * Method to execute an sql statement.
         *
         * @param _instSQLs builders
         * @return false if we had an expand that did not deliver any Data
         * @throws EFapsException on error during execution of statement
         */
        private boolean executeSQLStatement(final List<StringBuilder> _instSQLs) throws EFapsException
        {

            final StringBuilder sql = new StringBuilder();
            boolean ret = true;
            boolean first = true;

            for (final StringBuilder instSQL : _instSQLs) {
                if (first) {
                    sql.append("select distinct ");
                    first = false;
                } else {
                    sql.append("union select ");
                }

                // append columns including the id
                for (final String col : this.cols) {
                    sql.append(col).append(",");
                }

                sql.deleteCharAt(sql.length() - 1);

                sql.append(" from ").append(this.sqlTable.getSqlTable()).append(" where ");
                if (this.expand) {
                    sql.append(this.attributeSet.getSqlColNames().get(0));
                } else {
                    sql.append(" ID ");
                }
                sql.append(" in (").append(instSQL).append(")");
                // in case of a attributeset we must also give the type as a
                // additional
                // filter
                if (this.expand && this.sqlTable.getSqlColType() != null) {
                    sql.append(" and ").append(this.sqlTable.getSqlColType()).append(" = ").append(
                                    this.attributeSet.getId());
                }
            }
            ConnectionResource con = null;
            try {
                con = Context.getThreadContext().getConnectionResource();

                final Statement stmt = con.getConnection().createStatement();
                final ResultSet rs = stmt.executeQuery(sql.toString());
                int keyIndex = 1;
                int subKeyIndex = 0;
                if (this.expand) {
                    int idx = 1;
                    for (final String col : this.cols) {
                        if (col.equals(this.attributeSet.getSqlColNames().get(0))) {
                            keyIndex = idx;
                        }
                        idx++;
                    }
                    subKeyIndex = 1;
                }
                OneRoundQuery.this.cachedResult.populate(rs, keyIndex, subKeyIndex);
                // we had an expand that did not deliver any Data
                if (!rs.isAfterLast() && this.expand) {
                    ret = false;
                    this.expandHasResult = false;
                }
                rs.close();
                stmt.close();
                con.commit();
                con = null;
            } catch (final Throwable e) {
                throw new EFapsException(getClass(), "executeOneCompleteStmt.Throwable", e);
            } finally {
                if (con != null) {
                    try {
                        con.abort();
                    } catch (final Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            return ret;
        }

        /**
         * Getter method for instance variable {@link #expand}.
         *
         * @return value of instance variable {@link #expand}
         */
        public boolean isExpand()
        {
            return this.expand;
        }

        /**
         * Setter method for instance variable {@link #expand}.
         *
         * @param _expand value for instance variable {@link #expand}
         */
        public void setExpand(final boolean _expand)
        {
            this.expand = _expand;
        }

        /**
         * Returns a string representation of this .
         *
         * @return string representation of this
         */
        @Override
        public String toString()
        {
            return new ToStringBuilder(this).appendSuper(super.toString()).append("sqlTable", this.sqlTable.toString())
                            .append("attributes", this.attributes.toString()).toString();
        }
    }

}
