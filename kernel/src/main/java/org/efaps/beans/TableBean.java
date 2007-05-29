/*
 * Copyright 2006 The eFaps Team
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

package org.efaps.beans;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.AttributeTypeInterface;
import org.efaps.admin.datamodel.ui.UIInterface;
import org.efaps.admin.ui.CommandAbstract;
import org.efaps.admin.ui.Field;
import org.efaps.admin.ui.Table;
import org.efaps.beans.valueparser.ValueParser;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;

/**
 * @author tmo
 * @version $Id: TableBean.java 675 2007-02-14 20:56:25 +0000 (Wed, 14 Feb 2007)
 *          jmo $
 * @todo description
 */
public class TableBean extends AbstractCollectionBean implements
    TableBeanInterface {

  public TableBean() throws EFapsException {
    super();
    System.out.println("TableBean.constructor");
  }

  public void finalize() {
    System.out.println("TableBean.destructor");
  }

  public void execute() throws Exception {
    Context context = Context.getThreadContext();
    System.out.println("--->selectedFilter=" + getSelectedFilter());
    executeTitle(context);

    SearchQuery query = new SearchQuery();

    if (getCommand().getProperty("TargetQueryTypes") != null) {
      query.setQueryTypes(getCommand().getProperty("TargetQueryTypes"));
    } else if (getCommand().getProperty("TargetExpand") != null) {
      query.setExpand(context, getInstance(), getCommand().getProperty(
          "TargetExpand"));
    }

    query.add(context, getTable());

    if (getCommand().getTargetTableFilters() != null) {
      if (getSelectedFilter() == 0
          && getCommand().getTargetTableFilters().size() > 0) {
        setSelectedFilter(1);
      }
      /*
       * if (getCommand().getTargetTableFilters().size()>=getSelectedFilter() &&
       * getSelectedFilter()>0) { String clause =
       * ((CommandAbstract.TargetTableFilter)getCommand().getTargetTableFilters().get(getSelectedFilter()-1)).getClause();
       * if (clause!=null) { query.addWhere(context, clause); } }
       */
    }

    query.execute();

    setValues(new ArrayList<Row>());

    executeRowResult(context, query);

    setInitialised(true);
  }

  protected void executeTitle(Context _context) throws Exception {
    if (getTitle() != null && getInstance() != null) {
      SearchQuery query = new SearchQuery();
      query.setObject(getInstance());
      // query.addAllFromString(_context, getTitle());
      ValueParser parser = new ValueParser(new StringReader(getTitle()));
      ValueList list = parser.ExpressionString();
      list.makeSelect(query);
      if (query.selectSize() > 0) {
        query.execute();
        if (query.next()) {
          setTitle(list.makeString(_context, query));
          // setTitle(query.replaceAllInString(_context, getTitle()));
        }
        query.close();
      }
    }
  }

  void executeRowResult(Context _context, SearchQuery _query) throws Exception {
    while (_query.next()) {
      Row row = new Row(_query.getRowOIDs(_context));
      boolean toAdd = false;
      for (Field field : getTable().getFields()) {
        Object value = null;
        Attribute attr = null;
        // if (field.getProgramValue()!=null) {
        // attrValue = field.getProgramValue().evalAttributeValue(_context,
        // _query);
        // } else
        if (field.getExpression() != null) {
          value = _query.get(field);
          attr = _query.getAttribute(_context, field);
        }
        Instance instance = _query.getInstance(_context, field);
        // if (attrValue!=null) {
        // attrValue.setField(field);
        // }
        UIInterface classUI = null;
        if (attr != null) {
          classUI = attr.getAttributeType().getUI();
        }
        toAdd = toAdd || (value != null) || (instance != null);
        row.add(field, classUI, value, instance);
      }
      if (toAdd) {
        getValues().add(row);
      }
    }
  }

  /**
   * The instance method sorts the table values depending on the sort key in
   * {@link #sortKey} and the sort direction in {@link #sortDirection}.
   */
  public boolean sort() {
    /*
     * if (getSortKey()!=null && getSortKey().length()>0) { int sortKey = 0; for
     * (int i=0; i<getTable().getFields().size(); i++) { Field field =
     * (Field)getTable().getFields().get(i); if
     * (field.getName().equals(getSortKey())) { sortKey = i; break; } }
     * 
     * final int index = sortKey; Collections.sort(getValues(), new Comparator<Row>(){
     * public int compare(Row _o1, Row _o2) { int ret; AttributeTypeInterface a1 =
     * _o1.getValues().get(index).getAttrValue(); AttributeTypeInterface a2 =
     * _o2.getValues().get(index).getAttrValue(); return
     * a1.compareTo(getLocale(), a2); } } );
     * 
     * if (getSortDirection()!=null && getSortDirection().equals("-")) {
     * Collections.reverse(getValues()); } }
     */
    return true;
  }

  /**
   * 
   * @param _name
   *          name of the command object
   */
  public void setCommandName(String _name) throws EFapsException {
    super.setCommandName(_name);
    if (getCommand() != null) {
      setTable(getCommand().getTargetTable());

      if (getCommand().getTargetTableSortKey() != null) {
        setSortKey(getCommand().getTargetTableSortKey());
        if (getCommand().getTargetTableSortDirection() == CommandAbstract.TABLE_SORT_DIRECTION_DOWN) {
          setSortDirection("-");
        }
      }
    }
  }

  /**
   * With this instance method the checkboxes for the web table is controlled.
   * The value is get from the calling command which owns a property
   * <i>targetShowCheckBoxes</i> if the value <i>true</i>.
   * 
   * @return <i>true</i> if the check boxes must be shown, other <i>false</i>
   *         is returned.
   */
  public boolean isShowCheckBoxes() {
    return getCommand().isTargetShowCheckBoxes();
  }

  // ///////////////////////////////////////////////////////////////////////////

  /**
   * The instance variable stores the table which must be shown.
   * 
   * @see #getTable
   * @see #setTable
   */
  private Table  table          = null;

  /**
   * The instance variable stores the string of the sort key.
   * 
   * @see #getSortKey
   * @see #setSortKey
   */
  private String sortKey        = null;

  /**
   * The instance variable stores the string of the sort direction.
   * 
   * @see #getSortDirection
   * @see #setSortDirection
   */
  private String sortDirection  = null;

  /**
   * The instance variable stores the current selected filter of this web table
   * representation.
   * 
   * @see #getSelectedFilter
   * @see #setSelectedFilter(int)
   * @see #setSelectedFilter(String)
   */
  int            selectedFilter = 0;

  // ///////////////////////////////////////////////////////////////////////////

  /**
   * This is the getter method for the instance variable {@link #table}.
   * 
   * @return value of instance variable {@link #table}
   * @see #table
   * @see #setTable
   */
  public Table getTable() {
    return this.table;
  }

  /**
   * This is the setter method for the instance variable {@link #table}.
   * 
   * @param _table
   *          new value for instance variable {@link #table}
   * @see #table
   * @see #getTable
   */
  public void setTable(Table _table) {
    this.table = _table;
  }

  /**
   * This is the getter method for the instance variable {@link #sortKey}.
   * 
   * @return value of instance variable {@link #sortKey}
   * @see #sortKey
   * @see #setSortKey
   */
  public String getSortKey() {
    return this.sortKey;
  }

  /**
   * This is the setter method for the instance variable {@link #sortKey}.
   * 
   * @param _sortKey
   *          new value for instance variable {@link #sortKey}
   * @see #sortKey
   * @see #getSortKey
   */
  public void setSortKey(String _sortKey) {
    this.sortKey = _sortKey;
  }

  /**
   * This is the getter method for the instance variable {@link #sortDirection}.
   * 
   * @return value of instance variable {@link #sortDirection}
   * @see #sortDirection
   * @see #setSortDirection
   */
  public String getSortDirection() {
    return this.sortDirection;
  }

  /**
   * This is the setter method for the instance variable {@link #sortDirection}.
   * 
   * @param _sortDirection
   *          new value for instance variable {@link #sortDirection}
   * @see #sortDirection
   * @see #getSortDirection
   */
  public void setSortDirection(String _sortDirection) {
    this.sortDirection = _sortDirection;
  }

  /**
   * This is the getter method for the instance variable {@link #selectedFilter}.
   * 
   * @return value of instance variable {@link #selectedFilter}
   * @see #selectedFilter
   * @see #setSelectedFilter
   */
  public int getSelectedFilter() {
    return this.selectedFilter;
  }

  /**
   * This is the setter method for the instance variable {@link #selectedFilter}.
   * 
   * @param _selectedFilter
   *          new value for instance variable {@link #selectedFilter}
   * @see #selectedFilter
   * @see #getSelectedFilter
   */
  public void setSelectedFilter(int _selectedFilter) {
    this.selectedFilter = _selectedFilter;
  }

  // ///////////////////////////////////////////////////////////////////////////
  // ///////////////////////////////////////////////////////////////////////////
  // ///////////////////////////////////////////////////////////////////////////

  /**
   * The inner class stores one row of the table.
   */
  public class Row {

    /**
     * The constructor creates a new instance of class Row.
     * 
     * @param _oids
     *          string with all oids for this row
     */
    public Row(String _oids) {
      setOids(_oids);
    }

    /**
     * The instance method adds a new attribute value (from instance
     * {@link AttributeTypeInterface}) to the values.
     * 
     * @see #values
     */
    public void add(Field _field, UIInterface _classUI, Object _value,
                    Instance _instance) {
      getValues().add(
          new Value(_field.getLabel(), _field, _classUI, _value, _instance));
    }

    /**
     * The instance method returns the size of the array list {@link #values}.
     * 
     * @see #values
     */
    public int getSize() {
      return getValues().size();
    }

    // /////////////////////////////////////////////////////////////////////////

    /**
     * The instance variable stores the values for the table.
     * 
     * @see #getValues
     */
    private List<Value> values = new ArrayList<Value>();

    /**
     * The instance variable stores all oids in a string.
     * 
     * @see #getOids
     * @see #setOids
     */
    private String      oids   = null;

    // /////////////////////////////////////////////////////////////////////////

    /**
     * This is the getter method for the values variable {@link #values}.
     * 
     * @return value of values variable {@link #values}
     * @see #values
     */
    public List<Value> getValues() {
      return this.values;
    }

    /**
     * This is the getter method for the instance variable {@link #oids}.
     * 
     * @return value of instance variable {@link #oids}
     * @see #oids
     * @see #setOids
     */
    public String getOids() {
      return this.oids;
    }

    /**
     * This is the setter method for the instance variable {@link #oids}.
     * 
     * @param _oids
     *          new value for instance variable {@link #oids}
     * @see #oids
     * @see #getOids
     */
    public void setOids(String _oids) {
      this.oids = _oids;
    }
  }
}
