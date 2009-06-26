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

package org.efaps.db;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.efaps.admin.access.AccessTypeEnums;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.IAttributeType;
import org.efaps.admin.datamodel.SQLTable;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.query.CachedResult;
import org.efaps.db.query.CompleteStatement;
import org.efaps.db.query.WhereClause;
import org.efaps.db.query.WhereClauseAttrEqAttr;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.util.EFapsException;

/**
 * @author tmo
 * @version $Id$
 * @todo description
 */
public abstract class AbstractQuery
{

    // ///////////////////////////////////////////////////////////////////////////
    // static variables

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractQuery.class);

    // ///////////////////////////////////////////////////////////////////////////
    // instance variables

    private boolean checkAccess = false;

    private CachedResult cachedResult = null;

    protected Type type = null;

    ArrayList<Type> types = new ArrayList<Type>();

    /**
     * The instance variable stores the order of the select types.
     *
     * @see #getSelectTypesOrder
     */
    private final List<SelectType> selectTypesOrder = new ArrayList<SelectType>();

    /**
     * The instance variable stores all single join elements.
     *
     * @see #getJoinElements
     */
    private final List<JoinElement> joinElements = new ArrayList<JoinElement>();

    /**
     * The instance variable maps expressions to join elements.
     */
    private final Map<String, JoinElement> mapJoinElements = new HashMap<String, JoinElement>();

    /**
     * The instance variable stores the main instance of the join element. The
     * main join element is that join elment which stores the direct selectable
     * attribute values.
     *
     * @see #getMainJoinElement
     */
    private final JoinElement mainJoinElement = new JoinElement();

    /**
     * The instance variable stores all main selected types. The key in this map
     * is the main table.
     *
     * @see #getSelectTypes
     */
    private final Map<Type, SelectType> mainSelectTypes = new HashMap<Type, SelectType>();

    /**
     * The instance variable stores all main where clauses. This where clauses
     * must be used by all join elements! This is a different behaviour than the
     * where clauses for a join element.
     *
     * @see #getMainWhereClauses
     */
    private final List<WhereClause> mainWhereClauses = new ArrayList<WhereClause>();

    /**
     * Should the child types als be expanded?
     *
     * @see #isExpandChildTypes
     * @see #setExpandChildTypes
     */
    private boolean expandChildTypes = true;

    /**
     * The instance variable stores all select expressions and their relations
     * to attributes for this query.
     *
     * @see #getAllSelExprMap
     */
    private final Map<Object, SelExpr2Attr> allSelExprMap = new HashMap<Object, SelExpr2Attr>();

    /**
     * The instance variable stores all OID select expressions and their
     * relations to attributes for this query.
     *
     * @see #getAllOIDSelExprMap
     */
    private final Map<Object, SelExpr2Attr> allOIDSelExprMap = new HashMap<Object, SelExpr2Attr>();

    // ///////////////////////////////////////////////////////////////////////////

    /**
     * The instance destructor calls the instance method {@link #close} to close
     * the SQL statement if not happend till now. The exception thrown from
     * method {@link #close} is catched and no error is thrown from this
     * destructor.
     *
     * @see #close
     */
    @Override
    public void finalize()
    {
        try {
            close();
        } catch (final Exception e) {
        }
    }

    /**
     * The method closes the SQL statement. The method must be always called to
     * close the query!
     *
     * @see #statement
     */
    public void close() throws EFapsException
    {
        /*
         * if (getStatement()!=null) { try { getStatement().close(); } catch
         * (Exception e) { throw e; } finally { setStatement(null); } }
         */
    }

    // ///////////////////////////////////////////////////////////////////////////
    // add for selecting something

    /**
     * The method adds an expression to the selectstatement.
     *
     * @param _expression expression to add
     */
    public void addSelect(final String _expression) throws EFapsException
    {
        addSelect(false, _expression, this.type, _expression);
    }

    /**
     * @param _isOID must be set to <i>true</i> is select expression selects the
     *            OID of the business object.
     * @param _key key to store the select expression in the select map
     *            expression
     * @param _attr attribute itself which must be selected
     */
    protected void addSelect(final boolean _isOID, final Object _key, final Attribute _attr) throws EFapsException
    {
        getSelectType(_attr.getParent()).addSelect(_isOID, _key, _attr);
    }

    /**
     * @param _isOID must be set to <i>true</i> is select expression selects the
     *            OID of the business object.
     * @param _key key to store the select expression in the select map
     *            expression
     * @param _expression
     */
    protected void addSelect(final boolean _isOID, final Object _key, final Type _type, final String _expression)
                    throws EFapsException
    {
        getSelectType(_type).addSelect(_isOID, _key, _expression);
    }

    /**
     * The instance method adds types in the order of the expand.
     *
     * @param _type type to add in the correct order
     * @see #addTypes4Order(Type,boolean)
     */
    protected void addTypes4Order(final Type _type)
    {
        addTypes4Order(_type, false);
    }

    /**
     * The instance method adds types in the order of the expand.
     *
     * @param _type type to add in the correct order
     * @param _nullAllowed type can be null
     * @see #selectTypesOrder
     * @see #getSelectType
     */
    protected void addTypes4Order(final Type _type, final boolean _nullAllowed)
    {
        final SelectType selectType = getSelectType(_type);
        selectType.setOrderIndex(getSelectTypesOrder().size());
        selectType.setNullAllowed(_nullAllowed);
        getSelectTypesOrder().add(selectType);
    }

    // ///////////////////////////////////////////////////////////////////////////

    /**
     * The instance method returns for the given type the select type class
     * instance of {@link #SelectType}.
     *
     * @param _type type for which the instance of {@link #SelectType} is
     *            searched
     * @see #addTypes4Order
     * @see #selectTypesOrder
     */
    public SelectType getSelectType(final Type _type)
    {
        SelectType selectType = getMainSelectTypes().get(_type);
        if (selectType == null) {
            selectType = new SelectType(getMainJoinElement(), _type, getSelectTypesOrder().size());
            getMainSelectTypes().put(_type, selectType);
            for (final JoinElement elm : getJoinElements()) {
                elm.addSelectType(selectType);
            }
        }
        return selectType;
    }

    /**
     * The method returns the size of the select expressions.
     *
     * @return size of the select expressions
     * @see #getExpressions
     */
    public int selectSize()
    {
        int ret = 0;
        for (final JoinElement elm : getJoinElements()) {
            ret += elm.selectSize();
        }
        return ret;
    }

    // ///////////////////////////////////////////////////////////////////////////

    /**
     * The instance method returns for the given key the attribute value.
     *
     * @param _key key for which the attribute value must returned
     * @return atribute value for given key
     */
    public Object get(final String _key) throws EFapsException
    {
        Object ret = null;

        if (hasAccess(_key)) {
            final SelExpr2Attr selExpr = getAllSelExprMap().get(_key);
            if (selExpr != null) {
                ret = selExpr.getAttrValue();
            }
        }
        return ret;
    }

    private boolean hasAccess(final String _key) throws EFapsException
    {
        boolean hasAccess = true;
        if (this.checkAccess) {
            Instance instance = null;
            String oid = null;
            final SelExpr2Attr selExpr = getAllOIDSelExprMap().get(_key);
            if (selExpr != null) {
                oid = (String) selExpr.getAttrValue();
            }
            if ((oid != null) && !oid.equals("0.0")) {
                instance = Instance.get(oid);
            } else {
                instance = getInstance(this.type);
            }
            if (instance != null) {
                hasAccess = instance.getType().hasAccess(instance, AccessTypeEnums.SHOW.getAccessType());
            }
        }
        return hasAccess;
    }

    /**
     * The instance method returns for the given key the atribute.
     *
     * @param _key key for which the attribute value must returned
     * @return attribute for given key
     */
    public Attribute getAttribute(final String _key) throws Exception
    {
        Attribute ret = null;
        final SelExpr2Attr selExpr = getAllSelExprMap().get(_key);
        if (selExpr != null) {
            ret = selExpr.getAttribute();
        }
        return ret;
    }

    /**
     * All object ids for one row are returned. The objects id defined in the
     * expand are returned in the same order.
     *
     * @return list of instances from the expand
     */
    public List<Instance> getExpandInstances() throws EFapsException
    {
        final List<Instance> ret = new ArrayList<Instance>();
        for (final Type type : this.types) {
            ret.add(Instance.get(getOID(type)));
        }
        return ret;
    }

    /**
     * @param _key key for which the object id value must returned
     * @return object id for given key
     */
    protected String getOID(final Object _key) throws EFapsException
    {
        String ret = null;
        final SelExpr2Attr selExpr = getAllOIDSelExprMap().get(_key);
        if (selExpr != null) {
            ret = (String) selExpr.getAttrValue();
        }
        return ret;
    }

    /**
     * The instance method returns the instance for the current selected row.
     *
     * @param _type
     * @todo why is in this way implemented (other way than method getOID above)
     */
    protected Instance getInstance(final Type _type) throws EFapsException
    {
        final SelectType selectType = getMainSelectTypes().get(_type);
        if (selectType == null) {
            LOG.error("Type '" + _type.getName() + "' is not selected! New Instance can not created!");
            throw new EFapsException(getClass(), "getInstance.TypeNotSelected", _type.getName());
        }
        // String id =
        // getResultSet().getString(selectType.getIndexId().intValue());
        final String id = this.cachedResult.getString(selectType.getIndexId().intValue());

        Type type = _type;

        if (selectType.getIndexType() != null) {
            // long typeId =
            // getResultSet().getLong(selectType.getIndexType().intValue());
            final long typeId = this.cachedResult.getLong(selectType.getIndexType().intValue());
            type = Type.get(typeId);
        }

        return Instance.get(type, id);
    }

    // ///////////////////////////////////////////////////////////////////////////

    /**
     * The instance method executes the query.
     */
    public void execute() throws EFapsException
    {
        this.checkAccess = true;
        executeWithoutAccessCheck();
    }

    /**
     * The instance method executes the query.
     */
    public void executeWithoutAccessCheck() throws EFapsException
    {
        if (getMainJoinElement().selectSize() > 0) {

            int incSelIndex = 0;
            this.cachedResult = new CachedResult();

            for (final JoinElement joinElement : getJoinElements()) {

                joinElement.setIncSelIndex(incSelIndex);
                // warum diese Ueberpruefung? weil der join jeweils die spalte
                // mit der
                // id zum vergleichen rauschmeisst!!
                if (incSelIndex == 0) {
                    incSelIndex += joinElement.getSelectExpressions().size();
                } else {
                    incSelIndex += joinElement.getSelectExpressions().size() - 1;
                }

                final CompleteStatement completeStatement = new CompleteStatement();

                joinElement.appendStatement(completeStatement, -1, isExpandChildTypes());

                for (final SelectType selectType : getSelectTypesOrder()) {
                    if (selectType.isNullAllowed()) {
                        completeStatement.appendUnion();
                        joinElement
                                        .appendStatement(completeStatement, selectType.getOrderIndex(),
                                                        isExpandChildTypes());
                    }
                }

                executeOneCompleteStmt(completeStatement, joinElement.getMatchColumn());
            }

            for (final SelExpr2Attr selExpr : getAllSelExprMap().values()) {
                selExpr.initSelectIndex();
            }
            for (final SelExpr2Attr selExpr : getAllOIDSelExprMap().values()) {
                selExpr.initSelectIndex();
            }
        }
        this.cachedResult.beforeFirst();
    }

    /**
     * The instance method executes exact one complete statement and populates
     * the result in the cached result {@link #cachedResult}.
     *
     * @param _complStmt complete statement instance to execute
     * @param _matchColumn column in the complete statement (result set) used to
     *            as key to compare in the cached result
     */
    private void executeOneCompleteStmt(final CompleteStatement _complStmt, final int _matchColumn)
                    throws EFapsException
    {

        ConnectionResource con = null;
        try {
            con = Context.getThreadContext().getConnectionResource();

            if (LOG.isTraceEnabled()) {
                LOG.trace(_complStmt.getStatement().toString());
            }

            final Statement stmt = con.getConnection().createStatement();

            final ResultSet rs = stmt.executeQuery(_complStmt.getStatement().toString());

            this.cachedResult.populate(rs, _matchColumn, 0);

            rs.close();
            stmt.close();
            con.commit();
        } catch (final EFapsException e) {
            if (con != null) {
                con.abort();
            }
            throw e;
        } catch (final Throwable e) {
            if (con != null) {
                con.abort();
            }
            // TODO: exception eintragen!
            e.printStackTrace();
            throw new EFapsException(getClass(), "executeOneCompleteStmt.Throwable");
        }
    }

    /**
     * @return <i>true</i> if a new row is selected and exists, otherwise
     *         <i>false</i>
     */
    public boolean next()
    {
        return this.cachedResult.next();
    }

    // ///////////////////////////////////////////////////////////////////////////

    /**
     * This is the getter method for instance variable {@link #selectTypesOrder}
     * .
     *
     * @return value of instance variable {@link #selectTypesOrder}
     * @see #selectTypesOrder
     */
    public List<SelectType> getSelectTypesOrder()
    {
        return this.selectTypesOrder;
    }

    /**
     * This is the getter method for the instance variable {@link #type}.
     *
     * @return value of instance variable {@link #type}
     */
    public Type getType()
    {
        return this.type;
    }

    /**
     * This is the getter method for instance variable {@link #joinElements}.
     *
     * @return value of instance variable {@link #joinElements}
     * @see #joinElements
     */
    protected List<JoinElement> getJoinElements()
    {
        return this.joinElements;
    }

    /**
     * This is the getter method for instance variable {@link #mapJoinElements}.
     *
     * @return value of instance variable {@link #mapJoinElements}
     * @see #mapJoinElements
     */
    protected Map<String, JoinElement> getMapJoinElements()
    {
        return this.mapJoinElements;
    }

    /**
     * This is the getter method for instance variable {@link #mainJoinElement}.
     *
     * @return value of instance variable {@link #mainJoinElement}
     * @see #mainJoinElement
     */
    protected JoinElement getMainJoinElement()
    {
        return this.mainJoinElement;
    }

    /**
     * This is the getter method for instance variable {@link #mainSelectTypes}.
     *
     * @return value of instance variable {@link #mainSelectTypes}
     * @see #mainSelectTypes
     */
    private Map<Type, SelectType> getMainSelectTypes()
    {
        return this.mainSelectTypes;
    }

    /**
     * This is the getter method for instance variable {@link #mainWhereClauses}
     * .
     *
     * @return value of instance variable {@link #mainWhereClauses}
     * @see #mainWhereClauses
     */
    protected List<WhereClause> getMainWhereClauses()
    {
        return this.mainWhereClauses;
    }

    /**
     * This is the getter method for instance variable {@link #expandChildTypes}
     * .
     *
     * @return value of instance variable {@link #expandChildTypes}
     * @see #expandChildTypes
     * @see #setExpandChildTypes
     */
    protected boolean isExpandChildTypes()
    {
        return this.expandChildTypes;
    }

    /**
     * This is the setter method for instance variable {@link #expandChildTypes}
     * .
     *
     * @param _expandChildTypes new value for instance variable
     *            {@link #expandChildTypes}
     * @see #expandChildTypes
     * @see #isExpandChildTypes
     */
    public void setExpandChildTypes(final boolean _expandChildTypes)
    {
        this.expandChildTypes = _expandChildTypes;
    }

    /**
     * This is the getter method for instance variable {@link #allSelExprMap}.
     *
     * @return value of instance variable {@link #allSelExprMap}
     * @see #allSelExprMap
     */
    protected Map<Object, SelExpr2Attr> getAllSelExprMap()
    {
        return this.allSelExprMap;
    }

    /**
     * This is the getter method for instance variable {@link #allOIDSelExprMap}
     * .
     *
     * @return value of instance variable {@link #allOIDSelExprMap}
     * @see #allOIDSelExprMap
     */
    protected Map<Object, SelExpr2Attr> getAllOIDSelExprMap()
    {
        return this.allOIDSelExprMap;
    }

    // ###########################################################################
    // ###########################################################################
    // ###########################################################################
    // ###########################################################################
    // ###########################################################################

    /**
     * The class is used to make one join select.
     */
    public class JoinElement
    {

        private JoinElement()
        {
            if (getMainSelectTypes() != null) {
                getSelectTypes().addAll(getMainJoinElement().getSelectTypes());
            }
            getJoinElements().add(this);
        }

        protected void appendStatement(final CompleteStatement _completeStatement, final int _orderIndex,
                        final boolean _childTypes)
        {
            _completeStatement.append("select distinct ");
            appendSelectExpressions(_completeStatement, _orderIndex);
            appendFrom(_completeStatement, _orderIndex);
            appendWhereClause(_completeStatement, _orderIndex, _childTypes);

            for (final SelectType selectType : getSelectTypesOrder()) {
                if (selectType.isNullAllowed() && (_orderIndex < 0 || selectType.getOrderIndex() < _orderIndex)) {
                    _completeStatement.appendUnion();
                    _completeStatement.append("select distinct ");
                    appendSelectExpressions(_completeStatement, selectType.getOrderIndex());
                    appendFrom(_completeStatement, selectType.getOrderIndex());
                    appendWhereClause(_completeStatement, selectType.getOrderIndex(), _childTypes);
                }
            }
        }

        /**
         * Appends all select expressions from this one join element to the
         * comlete statement.
         *
         * @param _completeStatement complete SQL statement to select values
         * @param _orderIndex ????????????????????
         */
        private void appendSelectExpressions(final CompleteStatement _completeStatement, final int _orderIndex)
        {
            final Iterator<SQLSelectExpression> iter = getSelectExpressions().iterator();
            while (iter.hasNext()) {
                final SQLSelectExpression selectExpr = iter.next();
                if ((_orderIndex < 0) || selectExpr.getSelectType().getOrderIndex() < _orderIndex) {
                    _completeStatement.append(selectExpr.getExpression());
                } else {
                    _completeStatement.append(selectExpr.getNullString());
                }
                if (iter.hasNext()) {
                    _completeStatement.append(",");
                }
            }
        }

        private void appendFrom(final CompleteStatement _completeStatement, final int _orderIndex)
        {
            for (final SelectType selectType : getSelectTypes()) {
                if ((_orderIndex < 0) || (selectType.getOrderIndex() < _orderIndex)) {
                    selectType.appendFrom(_completeStatement);
                }
            }
        }

        private void appendWhereClause(final CompleteStatement _completeStatement, final int _orderIndex,
                        final boolean _childTypes)
        {
            final Iterator<SelectType> typeIter = getSelectTypes().iterator();
            while (typeIter.hasNext()) {
                final SelectType selectType = typeIter.next();
                if (_orderIndex < 0 || selectType.getOrderIndex() < _orderIndex) {
                    _completeStatement.appendWhereAnd();
                    selectType.appendTypeWhereClause(_completeStatement, _childTypes);
                }
            }

            for (final WhereClause whereClause : this.whereClauses) {
                whereClause.appendWhereClause(_completeStatement, _orderIndex);
            }

            for (final WhereClause whereClause : getMainWhereClauses()) {
                whereClause.appendWhereClause(_completeStatement, _orderIndex);
            }
        }

        /**
         * The method returns the size of the select expressions.
         *
         * @return size of the select expressions
         * @see #getExpressions
         */
        public int selectSize()
        {
            return getExpressions().size();
        }

        /**
         * Returns for the given expression the select expression. The select
         * expression is tested for uniqueness (and if defined already reused).
         *
         * @param _selectType instance of SelectType
         * @param _sqlTable SQL table which is selected
         * @param _aliasTableName alias table name (defined in the from clause)
         * @param _columnNAme column name within the SQL table
         * @param _isId is the selected column an internal eFaps id? if true,
         *            the null value select is "0", otherwise the null value
         *            select is direct evaluated from the table information
         * @return new select expression
         */
        private SQLSelectExpression getSelectExpression(final SelectType _selectType, final SQLTable _sqlTable,
                        final int _aliasIndex, final String _columnName, final boolean _isId)
        {
            final String sqlExpr = new StringBuilder().append(_sqlTable.getSqlTable()).append(_aliasIndex).append('.')
                            .append(_columnName).toString();
            SQLSelectExpression selectExpr = getExpressions().get(sqlExpr);
            if (selectExpr == null) {
                final String nullString = _isId ? "0" : _sqlTable.getTableInformation().getColInfo(_columnName)
                                .getNullValueSelect();
                selectExpr = new SQLSelectExpression(getExpressions().size() + 1, sqlExpr, this, _selectType,
                                nullString);
                getExpressions().put(sqlExpr, selectExpr);
                getSelectExpressions().add(selectExpr);
            }
            return selectExpr;
        }

        /**
         * @param _aliasIndex index used to create the sql prefix
         * @param _selectType instance of SelectType
         * @param _isOID the select statement is an OID select and must be
         *            stored in the OID map
         * @param _key key used to store in the (OID) map
         * @param _attribute attribute to select
         * @see #getSelectExpression
         */
        private void addSelectAttribute(final int _aliasIndex, final SelectType _selectType, final boolean _isOID,
                        final Object _key, final Attribute _attr)
        {
            final ArrayList<SQLSelectExpression> selectExprs = new ArrayList<SQLSelectExpression>();

            if (_attr.getTable() != null) {
                _selectType.getTypeTableNames().add(_attr.getTable());
                for (final String _sqlColName : _attr.getSqlColNames()) {
                    final SQLSelectExpression selectExpr = getSelectExpression(_selectType, _attr.getTable(),
                                    _aliasIndex, _sqlColName, false);
                    // System.out.println("selectExprs.add="+selectExpr);
                    selectExprs.add(selectExpr);
                }
            }
            final SelExpr2Attr selExpr2Attr = new SelExpr2Attr(_attr, selectExprs);
            // System.out.println("selectExprs="+selExpr2Attr);
            if (_isOID) {
                getAllOIDSelExprMap().put(_key, selExpr2Attr);
            } else {
                getAllSelExprMap().put(_key, selExpr2Attr);
            }
        }

        /**
         * The instance method creates a new instance of {@link #SelectType} add
         * adds them to {@link #selectTypesOrder}.
         *
         * @param _type type to add in the correct order
         * @param _nullAllowed type can be null
         * @return new created instance of {@link #SelectType}
         * @see #selectTypesOrder
         */
        protected SelectType getNewSelectType(final Type _type, final boolean _nullAllowed)
        {
            final SelectType selectType = new SelectType(this, _type, (getSelectTypesOrder().size() + 1000));
            selectType.setOrderIndex(getSelectTypesOrder().size() + 1000);
            selectType.setNullAllowed(_nullAllowed);
            getSelectTypesOrder().add(selectType);
            addSelectType(selectType);
            return selectType;
        }

        private void addWhere(final SelectType _selectType1, final Attribute _attr1, final SelectType _selectType2,
                        final Attribute _attr2) throws EFapsException
        {
            this.whereClauses.add(new WhereClauseAttrEqAttr(_selectType1, _attr1, _selectType2, _attr2));
        }

        private void addSelectType(final SelectType _selectType)
        {
            getSelectTypes().add(_selectType);
        }

        // /////////////////////////////////////////////////////////////////////////
        private final List<WhereClause> whereClauses = new ArrayList<WhereClause>();

        /**
         * The instance variable stores all select types of the select
         * statement.
         *
         * @see #getTableNames
         */
        private final Set<SelectType> selectTypes = new HashSet<SelectType>();

        /**
         * This is the instance variable to hold all expressions. The SQL
         * statement is stores as key, the value is the index of the expression
         * in the select statement. This is used that an expression is only once
         * in a select statement (uniqueness)!.
         *
         * @see #getExpressions
         */
        private final Map<String, SQLSelectExpression> expressions = new HashMap<String, SQLSelectExpression>();

        /**
         * The instance variable stores all select expressions.
         *
         * @see #getSelectExpressions
         */
        private final List<SQLSelectExpression> selectExpressions = new ArrayList<SQLSelectExpression>();

        /**
         * The instance variable stores the order of the select types. The
         * information is needed if an expand is made (and in an expand a null
         * value is possible!).
         *
         * @see #getSelectTypesOrder
         */
        private final List<SelectType> selectTypesOrder = new ArrayList<SelectType>();

        /**
         * The instance variable stores the column index of this join element.
         * The index is the select expression used to join with other select
         * statements. The default value is <i>1</i> for the first column.
         *
         * @see #getMatchColumn
         * @see #setMatchColumn
         */
        private int matchColumn = 1;

        /**
         * The instance variable stores the number of previous select
         * epxressions used to calculate the index of select expressions of this
         * join element in the complete join.
         *
         * @see #getIncSelIndex
         * @see #setIncSelIndex
         */
        private int incSelIndex = 0;

        // /////////////////////////////////////////////////////////////////////////

        /**
         * This is the getter method for instance variable {@link #selectTypes}.
         *
         * @return value of instance variable {@link #selectTypes}
         * @see #selectTypes
         */
        private Set<SelectType> getSelectTypes()
        {
            return this.selectTypes;
        }

        /**
         * This is the getter method for instance variable {@link #expressions}.
         *
         * @return value of instance variable {@link #expressions}
         * @see #expressions
         */
        protected Map<String, SQLSelectExpression> getExpressions()
        {
            return this.expressions;
        }

        /**
         * This is the getter method for instance variable
         * {@link #selectExpressions}.
         *
         * @return value of instance variable {@link #selectExpressions}
         * @see #selectExpressions
         */
        protected List<SQLSelectExpression> getSelectExpressions()
        {
            return this.selectExpressions;
        }

        /**
         * This is the getter method for instance variable
         * {@link #selectTypesOrder}.
         *
         * @return value of instance variable {@link #selectTypesOrder}
         * @see #selectTypesOrder
         */
        public List<SelectType> getSelectTypesOrder()
        {
            return this.selectTypesOrder;
        }

        /**
         * This is the setter method for instance variable {@link #matchColumn}.
         *
         * @param _orderIndex new value for instance variable
         *            {@link #matchColumn}
         * @see #matchColumn
         * @see #getMatchColumn
         */
        private void setMatchColumn(final int _matchColumn)
        {
            this.matchColumn = _matchColumn;
        }

        /**
         * This is the getter method for instance variable {@link #matchColumn}.
         *
         * @return value of instance variable {@link #matchColumn}
         * @see #matchColumn
         * @see #setMatchColumn
         */
        public int getMatchColumn()
        {
            return this.matchColumn;
        }

        /**
         * This is the setter method for instance variable {@link #incSelIndex}.
         *
         * @param _orderIndex new value for instance variable
         *            {@link #incSelIndex}
         * @see #incSelIndex
         * @see #getIncSelIndex
         */
        private void setIncSelIndex(final int _incSelIndex)
        {
            this.incSelIndex = _incSelIndex;
        }

        /**
         * This is the getter method for instance variable {@link #incSelIndex}.
         *
         * @return value of instance variable {@link #incSelIndex}
         * @see #incSelIndex
         * @see #setIncSelIndex
         */
        private int getIncSelIndex()
        {
            return this.incSelIndex;
        }
    }

    /**
     * The class stores the relation between an select expression and an
     * attribute.
     */
    private class SelExpr2Attr
    {

        /**
         * Stores the attribute.
         *
         * @see #getAttribute
         * @see #setAttribute
         */
        private Attribute attribute = null;

        /**
         * Stores all select expression.
         *
         * @see #getSelExpr
         * @see #setSelExpr
         */
        private ArrayList<SQLSelectExpression> selExprs = null;

        /**
         * Stores all the indexes of the SQL select expression where the values
         * of the attribute are found (in the same order than defined in the
         * attribute).
         *
         * @see #getIndexes
         */
        private final ArrayList<Integer> indexes = new ArrayList<Integer>();

        // /////////////////////////////////////////////////////////////////////////

        /**
         * Constructor
         *
         * @param _attr attribute
         * @param _selExprs select expressions
         */
        private SelExpr2Attr(final Attribute _attr, final ArrayList<SQLSelectExpression> _selExprs)
        {
            setAttribute(_attr);
            setSelExprs(_selExprs);
        }

        /**
         * After execution of the query, the indexes where that attribute values
         * stands, could be initialised and used by {@link #getAttrValue}.
         */
        protected void initSelectIndex()
        {
            // System.out.println("~~~~~~~~~~~~++initSelectIndex+"+getSelExprs());
            for (final SQLSelectExpression selExpr : getSelExprs()) {
                final int index = selExpr.getJoinElement().getIncSelIndex() + selExpr.getIndex();
                // System.out.println("~~~~~~~~~~~~++index="+index);
                getIndexes().add(new Integer(index));
            }
            // System.out.println("~~~~~~~~~~~~++getIndexes()="+getIndexes());
        }

        /**
         * @return attribute value with the value returned from the select
         *         expression
         */
        protected Object getAttrValue() throws EFapsException
        {
            if (getAttribute() == null) {
                throw new EFapsException(getClass(), "SelectExpression.get.NoAttribute");
            }
            // System.out.println("~~~~~~~~~~~~++getIndexes()="+getIndexes());
            final IAttributeType attrInterf = getAttribute().newInstance();
            Object ret = null;
            try {
                ret = attrInterf.readValue(AbstractQuery.this.cachedResult, getIndexes());
            } catch (final Exception e) {
                throw new EFapsException(getClass(), "getAttrValue.CouldNotReadValue", e);
            }
            return ret;
        }

        // /////////////////////////////////////////////////////////////////////////

        /**
         * This is the getter method for instance variable {@link #attribute}.
         *
         * @return value of instance variable {@link #attribute}
         * @see #attribute
         * @see #setAttribute
         */
        public Attribute getAttribute()
        {
            return this.attribute;
        }

        /**
         * This is the setter method for instance variable {@link #attribute}.
         *
         * @param _attribute new value for instance variable {@link #attribute}
         * @see #attribute
         * @see #getAttribute
         */
        private void setAttribute(final Attribute _attribute)
        {
            this.attribute = _attribute;
        }

        /**
         * This is the getter method for instance variable {@link #selExprs}.
         *
         * @return value of instance variable {@link #selExprs}
         * @see #selExprs
         * @see #setSelExprs
         */
        public ArrayList<SQLSelectExpression> getSelExprs()
        {
            return this.selExprs;
        }

        /**
         * This is the setter method for instance variable {@link #selExprs}.
         *
         * @param _selExpr new value for instance variable {@link #selExprs}
         * @see #selExprs
         * @see #getSelExprs
         */
        private void setSelExprs(final ArrayList<SQLSelectExpression> _selExprs)
        {
            this.selExprs = _selExprs;
        }

        /**
         * This is the getter method for instance variable {@link #indexes}.
         *
         * @return value of instance variable {@link #indexes}
         * @see #indexes
         */
        public ArrayList<Integer> getIndexes()
        {
            return this.indexes;
        }
    }

    // ###########################################################################
    // ###########################################################################
    // ###########################################################################
    // ###########################################################################
    // ###########################################################################

    private class SQLSelectExpression
    {

        /**
     *
     */
        private final int index;

        /**
     *
     */
        private final String expression;

        /**
     *
     */
        private final JoinElement joinElement;

        /**
     *
     */
        private final SelectType selectType;

        /**
     *
     */
        private final String nullString;

        /**
     *
     */
        protected SQLSelectExpression(final int _index, final String _expression, final JoinElement _joinElement,
                        final SelectType _selectType, final String _nullString)
        {
            this.index = _index;
            this.expression = _expression;
            this.joinElement = _joinElement;
            this.selectType = _selectType;
            this.nullString = _nullString;
        }

        // /////////////////////////////////////////////////////////////////////////

        /**
         * This is the getter method for instance variable {@link #index}.
         *
         * @return value of instance variable {@link #index}
         * @see #index
         */
        public int getIndex()
        {
            return this.index;
        }

        /**
         * This is the getter method for instance variable {@link #expression}.
         *
         * @return value of instance variable {@link #expression}
         * @see #expression
         */
        public String getExpression()
        {
            return this.expression;
        }

        /**
         * This is the getter method for instance variable {@link #joinElement}.
         *
         * @return value of instance variable {@link #joinElement}
         * @see #joinElement
         */
        public JoinElement getJoinElement()
        {
            return this.joinElement;
        }

        /**
         * This is the getter method for instance variable {@link #selectType}.
         *
         * @return value of instance variable {@link #selectType}
         * @see #selectType
         */
        public SelectType getSelectType()
        {
            return this.selectType;
        }

        /**
         * This is the getter method for instance variable {@link #nullString}.
         *
         * @return value of instance variable {@link #nullString}
         * @see #nullString
         */
        public String getNullString()
        {
            return this.nullString;
        }
    }

    // ###########################################################################
    // ###########################################################################
    // ###########################################################################
    // ###########################################################################
    // ###########################################################################

    public class SelectType
    {

        /**
         * The string instance variable stores the table names of the select
         * statement of this selected type.
         *
         * @see #getTableNames
         */
        private final Set<SQLTable> typeTableNames = new HashSet<SQLTable>();

        /**
         * The instance variable stores the index of the order.
         *
         * @see #getOrderIndex
         * @see #setOrderIndex
         */
        private int orderIndex = 0;

        final JoinElement joinElement;

        /**
         * The instance variable stores the type this class instance is
         * representing.
         *
         * @see #getType
         */
        private final Type type;

        /**
         * The instance variable stores the index number of the type in the
         * select expressions of the table. The index number is the number of
         * the SQL table (<code>from TABLENAME TABLENAME+INDEX_NUMBER</code>)
         * defined in the from statement of the complete SQL select statement.
         */
        private final int typeIndex;

        /**
         * The instance method stores the index of the id of this type in the
         * SQL table within the SQL select statement.
         *
         * @see #getIndexId
         * @see #setIndexId
         */
        private final Integer indexId;

        /**
         * The instance method stores the index of the type id of this type in
         * the SQL table within the SQL select statement.
         *
         * @see #getIndexId
         */
        private final Integer indexType;

        /**
         * The instance variable stores if the type can be null (and so the link
         * to a type must not be defined...).
         *
         * @see #isNullAllowed
         * @see #setNullAllowed
         */
        private boolean nullAllowed = false;

        private SelectType(final JoinElement _joinElement, final Type _type, final int _typeIndex)
        {
            this.joinElement = _joinElement;
            this.type = _type;
            this.typeIndex = _typeIndex;
            getTypeTableNames().add(getType().getMainTable());
            SQLSelectExpression selectExpr = this.joinElement.getSelectExpression(this, getType().getMainTable(),
                            this.typeIndex, getType().getMainTable().getSqlColId(), true);
            this.indexId = selectExpr.getIndex();
            this.joinElement.setMatchColumn(selectExpr.getIndex());

            if (getType().getMainTable().getSqlColType() != null) {
                selectExpr = this.joinElement.getSelectExpression(this, getType().getMainTable(), this.typeIndex,
                                getType().getMainTable().getSqlColType(), true);
                this.indexType = selectExpr.getIndex();
            } else {
                this.indexType = null;
            }
        }

        /**
         * @param _isOID must be set to <i>true</i> is select expression selects
         *            the OID of the object.
         * @param _key key to store the select expression in the select map
         *            expression
         * @param _attr attribute itself which must be selected
         */
        protected void addSelect(final boolean _isOID, final Object _key, final Attribute _attr)
        {
            this.joinElement.addSelectAttribute(this.typeIndex, this, _isOID, _key, _attr);
        }

        /**
         * @param _isOID must be set to <i>true</i> is select expression selects
         *            the OID of the business object.
         * @param _key key to store the select expression in the select map
         *            expression
         * @param _expression expression itself which must be selected
         * @todo EFapsException Property
         */
        protected void addSelect(final boolean _isOID, final Object _key, final String _expression)
                        throws EFapsException
        {
            // System.out.println("AbstractQuery.addSelect("+_isOID+","+_key+","+_expression+")");
            if ((_expression != null) && !"".equals(_expression)) {
                if (_expression.indexOf('.') >= 0) {
                    final JoinElement elm = new JoinElement();
                    final StringTokenizer tokens = new StringTokenizer(_expression, ".");
                    final String link = tokens.nextToken();
                    final Attribute attr = getType().getAttribute(link);
                    if (attr == null) {
                        LOG.error("Link for '" + link + "' does not exists on type " + "'" + getType().getName() + "'");
                        throw new EFapsException(getClass(), "addSelect.LinkDoesNotExists", link, getType().getName());
                    }

                    // add new link type
                    final Type linkType = attr.getLink();

                    if (linkType == null) {
                        LOG.error("For Link '" + link + "' of type " + "'" + getType().getName() + "' "
                                        + "the type is not defined.");
                        throw new EFapsException(getClass(), "addSelect.LinkDoesNotExists", link, getType().getName());
                    }
                    final SelectType selectType = elm.getNewSelectType(linkType, !attr.isRequired());
                    final Attribute attrFromLink = linkType.getAttribute(tokens.nextToken());

                    selectType.addSelect(_isOID, _key, attrFromLink);

                    elm.addWhere(this, attr, selectType, linkType.getAttribute("ID"));

                    // System.out.println("selectType.index="+selectType.getTypeIndex());

                    // for ID selection
                    final SQLSelectExpression selectExpr = elm.getSelectExpression(this, getType().getMainTable(),
                                    this.typeIndex, getType().getMainTable().getSqlColId(), true);
                    elm.setMatchColumn(selectExpr.getIndex());

                    getTypeTableNames().add(attr.getTable());

                    getMapJoinElements().put(_expression, elm);

                } else {
                    final Attribute attr = getType().getAttribute(_expression);
                    if (attr == null) {
                        LOG.error("attribute '" + _expression + "' for type " + "'" + getType().getName()
                                        + "' not found");
                        throw new EFapsException(getClass(), "addSelect.AttributeNotFound", _expression, getType()
                                        .getName());
                    }
                    addSelect(_isOID, _key, attr);
                }
            }
        }

        /**
         * Adds the tables from this attribute in a where clause.
         */
        public void add4Where(final Attribute _attr)
        {
            for (final String sqlColName : _attr.getSqlColNames()) {
                getTypeTableNames().add(_attr.getTable());
                this.joinElement.getSelectExpression(this, _attr.getTable(), this.typeIndex, sqlColName, false);
            }
        }

        protected void appendFrom(final CompleteStatement _completeStatement)
        {
            for (final SQLTable table : getTypeTableNames()) {
                _completeStatement.appendFrom(table.getSqlTable()).append(" ").append(table.getSqlTable()).append(
                                this.typeIndex);
            }
        }

        /**
         * @param _childTypes also child types are allowed
         */
        protected void appendTypeWhereClause(final CompleteStatement _completeStatement, final boolean _childTypes)
        {
            if (getType().getMainTable().getSqlColType() != null) {

                if (_childTypes) {
                    _completeStatement.appendWhereAnd();
                    _completeStatement.appendWhere(getType().getMainTable().getSqlTable()).appendWhere(this.typeIndex)
                                    .appendWhere(".");
                    _completeStatement.appendWhere(getType().getMainTable().getSqlColType());
                    _completeStatement.appendWhere(" in (");
                    _completeStatement.appendWhere(getType().getId());
                    for (final Type child : getType().getChildTypes()) {
                        _completeStatement.appendWhere(",").appendWhere(child.getId());
                    }
                    _completeStatement.appendWhere(")");
                } else {
                    _completeStatement.appendWhereAnd();
                    _completeStatement.appendWhere(getType().getMainTable().getSqlTable()).appendWhere(this.typeIndex)
                                    .appendWhere(".");
                    _completeStatement.appendWhere(getType().getMainTable().getSqlColType());
                    _completeStatement.appendWhere("=");
                    _completeStatement.appendWhere(getType().getId());
                }
            }

            final Iterator<SQLTable> iter = getTypeTableNames().iterator();
            final SQLTable table = iter.next();
            while (iter.hasNext()) {
                _completeStatement.appendWhereAnd();
                _completeStatement.appendWhere(table.getSqlTable()).appendWhere(this.typeIndex).appendWhere(".")
                                .appendWhere(table.getSqlColId());
                _completeStatement.appendWhere("=");
                final SQLTable nextTable = iter.next();
                _completeStatement.appendWhere(nextTable.getSqlTable()).appendWhere(this.typeIndex).appendWhere(".")
                                .appendWhere(nextTable.getSqlColId());
            }
        }

        // /////////////////////////////////////////////////////////////////////////
        // getter and setter methods

        /**
         * This is the getter method for instance variable {@link #typeIndex}.
         *
         * @return value of instance variable {@link #typeIndex}
         * @see #typeIndex
         */
        public int getTypeIndex()
        {
            return this.typeIndex;
        }

        /**
         * This is the getter method for instance variable {@link #type}.
         *
         * @return value of instance variable {@link #type}
         * @see #type
         * @see #setType
         */
        public Type getType()
        {
            return this.type;
        }

        /**
         * This is the getter method for instance variable {@link #indexType}.
         *
         * @return value of instance variable {@link #indexType}
         * @see #indexType
         * @see #setIndexType
         */
        protected Integer getIndexType()
        {
            return this.indexType;
        }

        /**
         * This is the getter method for instance variable {@link #indexId}.
         *
         * @return value of instance variable {@link #indexId}
         * @see #indexId
         * @see #setIndexId
         */
        protected Integer getIndexId()
        {
            return this.indexId;
        }

        /**
         * This is the getter method for instance variable
         * {@link #typeTableNames}.
         *
         * @return value of instance variable {@link #typeTableNames}
         * @see #typeTableNames
         */
        protected Set<SQLTable> getTypeTableNames()
        {
            return this.typeTableNames;
        }

        /**
         * This is the getter method for instance variable {@link #nullAllowed}.
         *
         * @return value of instance variable {@link #nullAllowed}
         * @see #nullAllowed
         * @see #setNullAllowed
         */
        public boolean isNullAllowed()
        {
            return this.nullAllowed;
        }

        /**
         * This is the setter method for instance variable {@link #nullAllowed}.
         *
         * @param _nullAllowed new value for instance variable
         *            {@link #nullAllowed}
         * @see #nullAllowed
         * @see #isNullAllowed
         */
        private void setNullAllowed(final boolean _nullAllowed)
        {
            this.nullAllowed = _nullAllowed;
        }

        /**
         * This is the getter method for instance variable {@link #orderIndex}.
         *
         * @return value of instance variable {@link #orderIndex}
         * @see #orderIndex
         * @see #setOrderIndex
         */
        public int getOrderIndex()
        {
            return this.orderIndex;
        }

        /**
         * This is the setter method for instance variable {@link #orderIndex}.
         *
         * @param _orderIndex new value for instance variable
         *            {@link #orderIndex}
         * @see #orderIndex
         * @see #getOrderIndex
         */
        private void setOrderIndex(final int _orderIndex)
        {
            this.orderIndex = _orderIndex;
        }
    }
}
