/*
 * Copyright 2003-2007 The eFaps Team
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Collection;

import org.apache.commons.lang.builder.ToStringBuilder;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.AttributeTypeInterface;
import org.efaps.admin.datamodel.SQLTable;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.query.CachedResult;
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

  // ///////////////////////////////////////////////////////////////////////////
  // constructors / desctructors

  /**
   * @param _instances
   *          list of instances for which this query is executed
   * @todo check das alle instanzen von gleicher main table sind
   * @todo if no column for the type exists, all types must be the same!
   */
  public OneRoundQuery(final List<Instance> _instances,
                       final Set<String> _selects) {
    this.instances = _instances;
    this.selects = _selects;

    this.mainSQLTable = _instances.get(0).getType().getMainTable();

    // if no column for the type exists, the type must be defined directly
    if (this.mainSQLTable.getSqlColType() == null) {
      this.type = _instances.get(0).getType();
    } else {
      this.type = null;
    }

    // das muss nur gemacht werden, wenn unterschiedliche typen existieren!?
    SQLTableMapping2Attributes tmp =
        new SQLTableMapping2Attributes(this.mainSQLTable);
    tmp.addInstances(this.instances);
    this.sqlTableMappings.put(this.mainSQLTable, tmp);
  }

  // ///////////////////////////////////////////////////////////////////////////
  // instance methods

  public void execute() {
    // make type mapping to instances
    for (Instance instance : this.instances) {
      TypeMapping2Instances typeMapping =
          this.typeMappings.get(instance.getType());
      if (typeMapping == null) {
        typeMapping = new TypeMapping2Instances(instance.getType());
        this.typeMappings.put(instance.getType(), typeMapping);
      }
      typeMapping.addInstance(instance);
    }

    for (TypeMapping2Instances typeMapping : this.typeMappings.values()) {
      typeMapping.evaluateSelects();
    }

    // evalute sql statements
    int curIndex = 2;
    for (SQLTableMapping2Attributes sqlTableMapping : this.sqlTableMappings
        .values()) {
      curIndex = sqlTableMapping.evaluateSQLStatement(curIndex - 1);
    }

    // get index of type id
    if (this.mainSQLTable.getSqlColType() != null) {
      SQLTableMapping2Attributes sqlTableMapping =
          this.sqlTableMappings.get(this.mainSQLTable);
      colTypeId =
          sqlTableMapping.col2index.get(this.mainSQLTable.getSqlColType());
    }

    beforeFirst();
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
    TypeMapping2Instances typeMapping = typeMappings.get(type);
    while ((type != null) && (typeMapping == null)) {
      type = type.getParentType();
      typeMapping = typeMappings.get(type);
    }

    ret = typeMapping.getValue(_expression);
    /*
     * if (hasAccess(context, _key)) { SelExpr2Attr selExpr =
     * getAllSelExprMap().get(_key); if (selExpr != null) { ret =
     * selExpr.getAttrValue(context); } }
     */
    return ret;
  }

  public Type getType() throws Exception {
    Type ret = this.type;

    if (colTypeId > 0) {
      ret = Type.get(this.cachedResult.getLong(colTypeId));
    }
    return ret;
  }

  public Instance getInstance() throws Exception {
    return new Instance(getType(), this.cachedResult.getLong(1));
  }

  /**
   * The instance method returns for the given key the atribute.
   * 
   * @param _key
   *          key for which the attribute value must returned
   * @return attribute for given key
   */
  public Attribute getAttribute(final String _expression) throws Exception {
    Attribute ret = null;
    ret = getType().getAttribute(_expression);
    /*
     * SelExpr2Attr selExpr = getAllSelExprMap().get(_key); if (selExpr != null) {
     * ret = selExpr.getAttribute(); }
     */
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

    final Map<SQLTable, SQLTableMapping2Attributes> sqlTable2Attrs =
        new HashMap<SQLTable, SQLTableMapping2Attributes>();

    /**
     * @param _type
     *          type for which this type mapping is defined
     * @see type
     */
    TypeMapping2Instances(final Type _type) {
      this.type = _type;
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
      for (String select : selects) {
        Attribute attr = this.type.getAttribute(select);
        if (attr != null) {
          this.expr2Attr.put(select, attr);
        }
      }
      for (Attribute attribute : this.expr2Attr.values()) {
        SQLTableMapping2Attributes sqlTable2Attr =
            this.sqlTable2Attrs.get(attribute.getTable());
        if (sqlTable2Attr == null) {
          sqlTable2Attr = sqlTableMappings.get(attribute.getTable());
          if (sqlTable2Attr == null) {
            sqlTable2Attr =
                new SQLTableMapping2Attributes(attribute.getTable());
            sqlTableMappings.put(attribute.getTable(), sqlTable2Attr);
          }
          this.sqlTable2Attrs.put(attribute.getTable(), sqlTable2Attr);
        }
        sqlTable2Attr.addAttribute(attribute);
      }
      // add all instances to the sql table mapping
      for (SQLTableMapping2Attributes sqlTableMapping : this.sqlTable2Attrs
          .values()) {
        sqlTableMapping.addInstances(this.instances);
      }
    }

    public Object getValue(final String _expression) throws Exception {
      // System.out.println("getValue.expression="+_expression);
      Object ret = null;
      Attribute attr = this.expr2Attr.get(_expression);
      if (attr != null) {
        SQLTableMapping2Attributes sqlTable2attr =
            this.sqlTable2Attrs.get(attr.getTable());
        if (sqlTable2attr != null) {
          ret = sqlTable2attr.getValue(attr);
        } else {
          System.out.println("!!! NULLLLLLLL " + _expression);
          // System.out.println("this.expr2Attr="+this.expr2Attr);
        }
      } else {
        System.out.println("!!! NULLLLLLLL" + _expression);
        // System.out.println("_expression="+_expression);
      }
      return ret;
    }

    /**
     * Returns a string representation of this type mapping to instances.
     * 
     * @return string representation of this type mapping to instances
     */
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
    final SQLTable sqlTable;

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

    /**
     *
     */
    SQLTableMapping2Attributes(final SQLTable _sqlTable) {
      this.sqlTable = _sqlTable;
      this.col2index.put(this.sqlTable.getSqlColId(), index++);
      this.cols.add(this.sqlTable.getSqlColId());

      if (this.sqlTable.getSqlColType() != null) {
        this.col2index.put(this.sqlTable.getSqlColType(), index++);
        this.cols.add(this.sqlTable.getSqlColType());
      }
    }

    void addAttribute(final Attribute _attribute) {
      if (!this.attr2index.containsKey(_attribute)) {
        ArrayList<Integer> idxs = new ArrayList<Integer>();
        for (String col : _attribute.getSqlColNames()) {
          Integer idx = this.col2index.get(col);
          if (idx == null) {
            idx = index++;
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
      AttributeTypeInterface attrInterf = _attribute.newInstance();
      return attrInterf.readValue(cachedResult,
          (ArrayList<Integer>) this.attr2index.get(_attribute));
    }

    int evaluateSQLStatement(final int _startIndex) {

      int maxExpression = Context.getDbType().getMaxExpressions();
      List<StringBuilder> instSQLs = new ArrayList<StringBuilder>();
      StringBuilder instSQL = new StringBuilder();
      instSQLs.add(instSQL);
      int i = 0;
      for (Instance instance : this.instances) {
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
      for (Map.Entry<Attribute, List<Integer>> entry : this.attr2index
          .entrySet()) {
        List<Integer> newList = new ArrayList<Integer>();
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
      for (Map.Entry<String, Integer> entry : this.col2index.entrySet()) {
        int curIndex = entry.getValue();
        if (curIndex > 0) {
          curIndex += _startIndex;
        } else {
          curIndex = 1;
        }
        this.col2index.put(entry.getKey(), curIndex);
        // System.out.println(""+entry.getKey()+"="+curIndex);
      }

      evaluateSQLStatement(instSQLs);
      return (_startIndex + this.index);
    }

    void evaluateSQLStatement(final List<StringBuilder> _instSQLs) {

      StringBuilder sql = new StringBuilder();

      boolean first = true;

      for (StringBuilder instSQL : _instSQLs) {
        if (first) {
          sql.append("select distinct ");
          first = false;
        } else {
          sql.append("union select ");
        }

        // append columns including the id
        for (String col : this.cols) {
          sql.append(col).append(",");
        }
        sql.deleteCharAt(sql.length() - 1);

        sql.append(" from ").append(this.sqlTable.getSqlTable()).append(
            " where ID in (").append(instSQL).append(")");
        // System.out.println("sql="+sql);
      }
      ConnectionResource con = null;
      try {
        con = Context.getThreadContext().getConnectionResource();

        // if (LOG.isTraceEnabled()) {
        // LOG.trace(_complStmt.getStatement().toString());
        // }

        Statement stmt = con.getConnection().createStatement();
        ResultSet rs = stmt.executeQuery(sql.toString());

        cachedResult.populate(rs, 1);

        rs.close();
        stmt.close();
        con.commit();
        con = null;
        /*
         * } catch (EFapsException e) { if (con != null) { con.abort(); } throw
         * e;
         */
      } catch (Throwable e) {
        // TODO: exception eintragen!
        e.printStackTrace();
        // throw new EFapsException(getClass(),
        // "executeOneCompleteStmt.Throwable");
      }
      finally {
        if (con != null) {
          try {
            con.abort();
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }

    }

    /**
     * Returns a string representation of this .
     * 
     * @return string representation of this
     */
    public String toString() {
      return new ToStringBuilder(this).appendSuper(super.toString()).append(
          "sqlTable", this.sqlTable.toString()).append("attributes",
          this.attributes.toString()).toString();
    }
  }
}
