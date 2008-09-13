/*
 * Copyright 2003-2008 The eFaps Team
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
import org.efaps.admin.datamodel.AttributeTypeInterface;
import org.efaps.admin.datamodel.MultipleAttributeTypeInterface;
import org.efaps.admin.datamodel.SQLTable;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.ListQuery;
import org.efaps.db.transaction.ConnectionResource;

/**
 * @todo description
 * @author tmo
 * @version $Id$
 */
public class OneRoundQuery {

  // ///////////////////////////////////////////////////////////////////////////
  // instance variables

  /**
   * Stores all instances for which this query is executed.
   */
  private final List<Instance> instances;

  private final SQLTable mainSQLTable;

  private final Type type;


  /**
   * Stores all select statements for this query.
   */
  private final Set<String> selects;

  private final Map<Type, TypeMapping2Instances> typeMappings =
      new HashMap<Type, TypeMapping2Instances>();

  private final Map<SQLTable, SQLTableMapping2Attributes> sqlTableMappings =
      new HashMap<SQLTable, SQLTableMapping2Attributes>();

  private final CachedResult cachedResult = new CachedResult();

  private int colTypeId = 0;

  private final ListQuery listquery;
  // ///////////////////////////////////////////////////////////////////////////
  // constructors / desctructors

  /**
   * @param _instances
   *          list of instances for which this query is executed
   * @param map
   * @todo check das alle instanzen von gleicher main table sind
   * @todo if no column for the type exists, all types must be the same!
   */
  public OneRoundQuery(final List<Instance> _instances,
                       final Set<String> _selects,
                       final ListQuery _listquery) {

    this.instances = _instances;
    this.selects = _selects;
    this.listquery = _listquery;
    if (this.listquery.getExpand()!=null){
      this.mainSQLTable = this.listquery.getExpand().getTable();
      this.type = this.listquery.getExpand().getLink();
    } else {
      this.mainSQLTable = _instances.get(0).getType().getMainTable();

      // if no column for the type exists, the type must be defined directly
      if (this.mainSQLTable.getSqlColType() == null) {
        this.type = _instances.get(0).getType();
      } else {
        this.type = null;
      }

      // das muss nur gemacht werden, wenn unterschiedliche typen existieren!?
      final SQLTableMapping2Attributes tmp =
          new SQLTableMapping2Attributes(this.mainSQLTable);
      tmp.addInstances(this.instances);
      this.sqlTableMappings.put(this.mainSQLTable, tmp);
    }
  }

  // ///////////////////////////////////////////////////////////////////////////
  // instance methods



  public void execute() {

    if (this.listquery.getExpand() == null){
      // make type mapping to instances
      for (final Instance instance : this.instances) {
        TypeMapping2Instances typeMapping =
            this.typeMappings.get(instance.getType());
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
      for (final SQLTableMapping2Attributes sqlTableMapping : this.sqlTableMappings
          .values()) {
        curIndex = sqlTableMapping.evaluateSQLStatement(curIndex - 1);
      }

    } else {
      //expand
      for (final Instance instance : this.instances) {
        TypeMapping2Instances typeMapping =
            this.typeMappings.get(this.type);
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
      for (final SQLTableMapping2Attributes sqlTableMapping : this.sqlTableMappings
          .values()) {
        sqlTableMapping.setExpand(true);
        sqlTableMapping.setLinkAttribute(this.listquery.getExpand());
        curIndex = sqlTableMapping.evaluateSQLStatement(curIndex - 1);
      }


    }
    beforeFirst();

    // get index of type id
    if (this.mainSQLTable.getSqlColType() != null) {
      final SQLTableMapping2Attributes sqlTableMapping =
          this.sqlTableMappings.get(this.mainSQLTable);
      this.colTypeId =
          sqlTableMapping.col2index.get(this.mainSQLTable.getSqlColType());
      //this.instances.clear();

    }

  }

  /**
   * Adds one select statement to this query.
   *
   * @param _select
   *          select statement to add
   * @see #selects
   */
  public void addSelect(final String _select) {
    this.selects.add(_select);
  }

  /**
   * @return <i>true</i> if a new row is selected and exists, otherwise
   *         <i>false</i>
   */
  public boolean next() {
    return this.cachedResult.next();
  }

  public void beforeFirst() {
    this.cachedResult.beforeFirst();
  }

  public boolean gotoKey(final Object _id) {
    return this.cachedResult.gotoKey(_id);
  }

  /**
   * The instance method returns for the given key the attribute value.
   *
   * @param _key
   *          key for which the attribute value must returned
   * @return atribute value for given key
   */
  public Object getValue(final String _expression) throws Exception {
    Object ret = null;

    Type type = getType();
    TypeMapping2Instances typeMapping = this.typeMappings.get(type);
    while ((type != null) && (typeMapping == null)) {
      type = type.getParentType();
      typeMapping = this.typeMappings.get(type);
    }

    ret = typeMapping.getValue(_expression);
    /*
     * if (hasAccess(context, _key)) { SelExpr2Attr selExpr =
     * getAllSelExprMap().get(_key); if (selExpr != null) { ret =
     * selExpr.getAttrValue(context); } }
     */
    return ret;
  }

  public Type getType() {
    Type ret = this.type;

    if (this.colTypeId > 0) {
      ret = Type.get(this.cachedResult.getLong(this.colTypeId));
    }
    return ret;
  }

  public Instance getInstance() throws Exception {
    return new Instance(getType(), this.cachedResult.getLong(1));
  }

  public List<Instance> getInstances() {
    if (this.listquery.getExpand() != null) {
      this.instances.clear();
      final SQLTableMapping2Attributes sqlTableMapping = this.sqlTableMappings
          .get(this.mainSQLTable);
      final List<?> ids = (List<?>) this.cachedResult
          .getObject(sqlTableMapping.col2index.get(this.mainSQLTable
              .getSqlColId()));
      for (final Object id : ids) {
        this.instances.add(new Instance(this.getType(), (Long) id));
      }
    }

    return this.instances;
  }

  /**
   * The instance method returns for the given key the attribute.
   *
   * @param _key
   *          key for which the attribute value must returned
   * @return attribute for given key
   */
  public Attribute getAttribute(final String _expression) throws Exception {
    Attribute ret = null;
    ret = getType().getAttribute(_expression);
    if (ret==null){
      ret = getType().getLinks().get(_expression);
    }
    /*
     * SelExpr2Attr selExpr = getAllSelExprMap().get(_key); if (selExpr != null) {
     * ret = selExpr.getAttribute(); }
     */
    return ret;
  }

  /**
   * @return
   * @throws Exception
   */
  public Object getMultiLineValue() throws Exception {

    Object ret = null;
    final Map<Integer, String> indexes =
              new HashMap<Integer, String>();

    for (final SQLTableMapping2Attributes sql2attr : this.sqlTableMappings.values()){
      for (final String select : this.selects){
        final Attribute attr = this.type.getAttribute(select);
        if (attr != null) {
          final List<Integer> idx = sql2attr.attr2index.get(attr);
          if (idx != null) {
            indexes.put(idx.get(0),attr.getName());
          }
        }
      }
    }

    final MultipleAttributeTypeInterface attrInterf
            = (MultipleAttributeTypeInterface) this.listquery.getExpand()
                                                             .newInstance();
    ret = attrInterf.readValues(OneRoundQuery.this.cachedResult, indexes);


    return ret;
  }

  // ///////////////////////////////////////////////////////////////////////////
  // ///////////////////////////////////////////////////////////////////////////
  // ///////////////////////////////////////////////////////////////////////////

  // class used to store all types related to instances
  class TypeMapping2Instances {

    /**
     * Defines the instances for which this type mapping to instances is
     * defined.
     *
     * @see #addInstance
     */
    final Set<Instance> instances = new HashSet<Instance>();

    /**
     * Stores the type for which this type mapping is defined.
     */
    final Type type;

    final Map<String, Attribute> expr2Attr = new HashMap<String, Attribute>();

    final Set<String> multiExpr = new HashSet<String>();
    final Map<SQLTable, SQLTableMapping2Attributes> sqlTable2Attrs =
        new HashMap<SQLTable, SQLTableMapping2Attributes>();



    /**
     * @param _type
     *          type for which this type mapping is defined
     * @see type
     */
    public TypeMapping2Instances(final Type _type) {
      this.type = _type;
    }

    /**
     * @return
     */
    public Object getMultiLineValue() {
      // TODO Auto-generated method stub
      return null;
    }

    /**
     * Adds an instance to the type mapping to instances.
     *
     * @param _instance
     * instance to add @
     * @see #instances
     */
    void addInstance(final Instance _instance) {
      this.instances.add(_instance);
    }

    void evaluateSelects() {
      for (final String select : OneRoundQuery.this.selects) {
        final Attribute attr = this.type.getAttribute(select);
        if (attr != null) {
          if (attr.isMultiline()) {
            for (final String subSelect : attr.getLink().getAttributes().keySet()) {
              if (!subSelect.equals("Type")) {
                ListQuery subQuery =  OneRoundQuery.this.listquery.getSubSelects().get(attr.getName());
                if (subQuery == null) {
                  subQuery = new ListQuery();
                  OneRoundQuery.this.listquery.getSubSelects().put(attr.getName(), subQuery);
                }
                subQuery.addSelect(subSelect);
                subQuery.setExpand(attr);

              }
            }
            OneRoundQuery.this.listquery.getMultiSelects().add(select);
            this.multiExpr.add(select);
          } else{
            this.expr2Attr.put(select, attr);
          }
        }
      }

      for (final Attribute attribute : this.expr2Attr.values()) {
        SQLTableMapping2Attributes sqlTable2Attr =
            this.sqlTable2Attrs.get(attribute.getTable());
        if (sqlTable2Attr == null) {
          sqlTable2Attr = OneRoundQuery.this.sqlTableMappings.get(attribute.getTable());
          if (sqlTable2Attr == null) {
            sqlTable2Attr =
                new SQLTableMapping2Attributes(attribute.getTable());
            OneRoundQuery.this.sqlTableMappings.put(attribute.getTable(), sqlTable2Attr);
          }
          this.sqlTable2Attrs.put(attribute.getTable(), sqlTable2Attr);
        }
        sqlTable2Attr.addAttribute(attribute);
      }
      // add all instances to the sql table mapping
      for (final SQLTableMapping2Attributes sqlTableMapping : this.sqlTable2Attrs
          .values()) {
        sqlTableMapping.addInstances(this.instances);
      }
    }

    public Object getValue(final String _expression) throws Exception {
      // System.out.println("getValue.expression="+_expression);
      Object ret = null;
      final Attribute attr = this.expr2Attr.get(_expression);
      if (attr != null && !attr.isMultiline()) {
        final SQLTableMapping2Attributes sqlTable2attr =
            this.sqlTable2Attrs.get(attr.getTable());
        if (sqlTable2attr != null) {
          ret = sqlTable2attr.getValue(attr);
        } else {
          System.out.println("!!! NULLLLLLLL " + _expression);
          // System.out.println("this.expr2Attr="+this.expr2Attr);
        }
      } else {
        //in case we have an expand we return the id of the object
        if (_expression.contains("\\") || this.multiExpr.contains(_expression)){
          final SQLTableMapping2Attributes sqlTable2attr =
            this.sqlTable2Attrs.get(OneRoundQuery.this.getType().getMainTable());
          if (sqlTable2attr != null) {
             final Integer idx = sqlTable2attr.col2index.get(OneRoundQuery.this.getType().getMainTable().getSqlColId());
             ret = OneRoundQuery.this.cachedResult.getLong(idx);
          }
        }

//      if (this.multiExpr.contains(_expression)) {
//        OneRoundQuery.this.listquery.getSubSelects().get(_expression)
//      }
      }
      return ret;
    }

    /**
     * Returns a string representation of this type mapping to instances.
     *
     * @return string representation of this type mapping to instances
     */
    @Override
    public String toString() {
      return new ToStringBuilder(this).appendSuper(super.toString()).append(
          "type", this.type.toString()).append("instances",
          this.instances.toString()).append("sqlTable2Attrs",
          this.sqlTable2Attrs.toString()).toString();
    }
  }

  // ///////////////////////////////////////////////////////////////////////////
  // ///////////////////////////////////////////////////////////////////////////
  // ///////////////////////////////////////////////////////////////////////////

  class SQLTableMapping2Attributes {

    /**
     *
     */
    private final SQLTable sqlTable;

    /**
     * @see #addAttribute
     */
    final private Set<Attribute> attributes = new HashSet<Attribute>();

    final private List<String> cols = new ArrayList<String>();

    final private Map<String, Integer> col2index =
        new HashMap<String, Integer>();

    final private Map<Attribute, List<Integer>> attr2index =
        new HashMap<Attribute, List<Integer>>();

    final Set<Instance> instances = new HashSet<Instance>();

    int index = 0;

    private boolean expand;

    private boolean expandHasResult = true;

    private Attribute linkAttribute;



    /**
     *
     */
    SQLTableMapping2Attributes(final SQLTable _sqlTable) {
      this.sqlTable = _sqlTable;
      this.col2index.put(this.sqlTable.getSqlColId(), this.index++);
      this.cols.add(this.sqlTable.getSqlColId());

      if (this.sqlTable.getSqlColType() != null) {
        this.col2index.put(this.sqlTable.getSqlColType(), this.index++);
        this.cols.add(this.sqlTable.getSqlColType());
      }
    }

    /**
     * @param _attribute
     */
    public void setLinkAttribute(final Attribute _attribute) {
      this.linkAttribute = _attribute;
      final String column = _attribute.getSqlColNames().get(0);
      if(!this.col2index.containsKey(column)){
        this.col2index.put(column, this.index++);
        this.cols.add(column);
      }
    }

    /**
     * @param _expand
     */
    public void setExpand(final boolean _expand) {
      this.expand = _expand;
    }

    void addAttribute(final Attribute _attribute) {
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

    void addInstances(final Collection<Instance> _instances) {
      this.instances.addAll(_instances);
    }

    public Object getValue(final Attribute _attribute) throws Exception {
      final AttributeTypeInterface attrInterf = _attribute.newInstance();
      Object ret = null;
      if (this.expandHasResult){
          ret = attrInterf.readValue(OneRoundQuery.this.cachedResult,
                                     this.attr2index.get(_attribute));
      }
      return ret;
    }

    int evaluateSQLStatement(final int _startIndex) {

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
      for (final Map.Entry<Attribute, List<Integer>> entry : this.attr2index
          .entrySet()) {
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
        // System.out.println(""+entry.getKey().getName()+"="+newList);
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
        // System.out.println(""+entry.getKey()+"="+curIndex);
      }

      final boolean shiftIndex = evaluateSQLStatement(instSQLs);
      // if we don't want to shift we must return the startvalue again
      return shiftIndex ? (_startIndex + this.index) : _startIndex + 1;
    }


    boolean evaluateSQLStatement(final List<StringBuilder> _instSQLs) {

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
        if (this.expand){
          sql.append(this.linkAttribute.getSqlColNames().get(0));
        }else{
          sql.append(" ID ");
        }
        sql.append(" in (").append(instSQL).append(")");
        System.out.println("sql="+sql);
      }
      ConnectionResource con = null;
      try {
        con = Context.getThreadContext().getConnectionResource();

        // if (LOG.isTraceEnabled()) {
        // LOG.trace(_complStmt.getStatement().toString());
        // }

        final Statement stmt = con.getConnection().createStatement();
        final ResultSet rs = stmt.executeQuery(sql.toString());
        int keyIndex = 1;
        int subKeyIndex = 0;
        if (this.expand){
          int idx=1;
          for(final String col :this.cols){
            if(col.equals(this.linkAttribute.getSqlColNames().get(0))){
              keyIndex=idx;
            }
            idx++;
          }
          subKeyIndex = 1;
        }
        OneRoundQuery.this.cachedResult.populate(rs, keyIndex, subKeyIndex);
        //we had an expand that did not deliver any Data
        if(!rs.isAfterLast() && this.expand){
          ret = false;
          this.expandHasResult = false;
        }
        rs.close();
        stmt.close();
        con.commit();
        con = null;
        /*
         * } catch (EFapsException e) { if (con != null) { con.abort(); } throw
         * e;
         */
      } catch (final Throwable e) {
        // TODO: exception eintragen!
        e.printStackTrace();
        // throw new EFapsException(getClass(),
        // "executeOneCompleteStmt.Throwable");
      }
      finally {
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
     * Returns a string representation of this .
     *
     * @return string representation of this
     */
    @Override
    public String toString() {
      return new ToStringBuilder(this).appendSuper(super.toString()).append(
          "sqlTable", this.sqlTable.toString()).append("attributes",
          this.attributes.toString()).toString();
    }
  }




}
