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

package org.efaps.webapp.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.apache.wicket.IClusterable;
import org.apache.wicket.PageParameters;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.AttributeTypeInterface;
import org.efaps.admin.datamodel.ui.FieldDefinition;
import org.efaps.admin.datamodel.ui.FieldValue;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.ui.CommandAbstract;
import org.efaps.admin.ui.Field;
import org.efaps.admin.ui.Table;
import org.efaps.db.Instance;
import org.efaps.db.ListQuery;
import org.efaps.util.EFapsException;

/**
 * @author jmo
 * @version $Id$
 */
public class TableModel extends ModelAbstract {

  private static final long serialVersionUID = 1L;

  // ///////////////////////////////////////////////////////////////////////////
  // instance variables

  /**
   * All evaluated rows of this table are stored in this list.
   * 
   * @see #getValues
   */
  private final List<RowModel> values = new ArrayList<RowModel>();

  /**
   * The instance variable stores the string of the sort key.
   * 
   * @see #getSortKey
   * @see #setSortKey
   */
  private String sortKey = null;

  /**
   * The instance variable stores the string of the sort direction.
   * 
   * @see #getSortDirection
   * @see #setSortDirection
   */
  private String sortDirection = null;

  /**
   * The instance variable stores the UUID for the table which must be shown.
   * 
   * @see #getTable
   */
  private UUID tableuuid;

  /**
   *
   */
  private boolean showCheckBoxes;

  /**
   * The instance variable stores the current selected filterKey as the
   * sequential number of the field of this web table representation.
   * 
   * @see #getFilterKey
   * @see #setFilterKey(String)
   */
  private int filterKeyInt = 0;

  /**
   * The instance variable stores the current selected filterKey as the name of
   * the field of this web table representation.
   */
  private String filterKey;

  /**
   * The instance Map contains the Values to be filtered
   */
  private Map<String, String> filterValues = new TreeMap<String, String>();

  /**
   * The instance Array holds the Label for the Columns
   */
  private List<String> columnLabels = new ArrayList<String>();

  public TableModel() throws EFapsException {
    super();
    initialise();
  }

  public TableModel(PageParameters _parameters) throws EFapsException {
    super(_parameters);
    initialise();

  }

  private void initialise() throws EFapsException {
    CommandAbstract command = getCommand();
    if (command != null) {
      // set target table
      this.tableuuid = command.getTargetTable().getUUID();

      // set default sort
      if (command.getTargetTableSortKey() != null) {
        setSortKey(getCommand().getTargetTableSortKey());
        if (command.getTargetTableSortDirection() == CommandAbstract.TABLE_SORT_DIRECTION_DESC) {
          this.sortDirection = "-";
        }
      }

      // set show check boxes
      boolean showCheckBoxes = getCommand().isTargetShowCheckBoxes();
      if (!showCheckBoxes) {
        String cldName = getParameter(PARAM_CALL_CMD_NAME);
        if (cldName != null) {
          CommandAbstract cmd = getCommand(cldName);
          showCheckBoxes =
              (cmd != null) && cmd.hasEvents(EventType.UI_COMMAND_EXECUTE);
        }
      }
      this.showCheckBoxes = showCheckBoxes;

    } else {
      this.showCheckBoxes = false;
    }
  }

  public Object getObject() {
    // TODO Auto-generated method stub
    return null;
  }

  public void setObject(Object object) {
    // TODO Auto-generated method stub

  }

  public void detach() {
    super.setInitialised(false);
    this.values.clear();
    this.columnLabels.clear();
  }

  public void execute() throws Exception {

    // first get list of object ids
    List<Return> ret =
        getCommand().executeEvents(EventType.UI_TABLE_EVALUATE,
            ParameterValues.INSTANCE, new Instance(super.getOid()));

    List<List<Instance>> lists =
        (List<List<Instance>>) ret.get(0).get(ReturnValues.VALUES);
    List<Instance> instances = new ArrayList<Instance>();
    Map<Instance, List<Instance>> instMapper =
        new HashMap<Instance, List<Instance>>();
    for (List<Instance> oneList : lists) {
      Instance inst = oneList.get(oneList.size() - 1);
      instances.add(inst);
      instMapper.put(inst, oneList);
    }

    // evaluate for all expressions in the table
    ListQuery query = new ListQuery(instances);
    for (Field field : this.getTable().getFields()) {
      if (field.getExpression() != null) {
        query.addSelect(field.getExpression());
      }
      if (field.getAlternateOID() != null) {
        query.addSelect(field.getAlternateOID());
      }
      addLabel(field.getLabel());
    }
    query.execute();

    this.executeRowResult(instMapper, query);

    if (this.sortKey != null) {
      this.sort();
    }

    super.setInitialised(true);
  }

  private void addLabel(String _label) {
    if (_label != null) {
      this.columnLabels.add(DBProperties.getProperty(_label));
    } else {
      this.columnLabels.add("");
    }
  }

  public List<String> getColumnLables() {
    return this.columnLabels;
  }

  private void executeRowResult(
                                final Map<Instance, List<Instance>> _instMapper,
                                final ListQuery _query) throws Exception {
    while (_query.next()) {

      // get all found oids (typically more than one if it is an expand)
      Instance instance = _query.getInstance();
      StringBuilder oids = new StringBuilder();
      boolean first = true;
      for (Instance oneInstance : _instMapper.get(instance)) {
        if (first) {
          first = false;
        } else {
          oids.append("|");
        }
        oids.append(oneInstance.getOid());
      }
      RowModel row = new RowModel(oids.toString());
      Attribute attr = null;

      String strValue = "";
      String oid = "";
      for (Field field : this.getTable().getFields()) {
        Object value = null;

        if (field.getExpression() != null) {
          value = _query.getValue(field.getExpression());
          attr = _query.getAttribute(field.getExpression());
        }

        FieldValue fieldvalue =
            new FieldValue(new FieldDefinition("egal", field), attr, value,
                instance);
        if (value != null) {
          if (this.isCreateMode() && field.isEditable()) {
            strValue = fieldvalue.getCreateHtml();
          } else if (this.isEditMode() && field.isEditable()) {
            strValue = fieldvalue.getEditHtml();
          } else {
            strValue = fieldvalue.getViewHtml();
          }
        } else {
          strValue = "";
        }
        if (field.getAlternateOID() != null) {
          Instance inst =
              new Instance((String) _query.getValue(field.getAlternateOID()));
          oid = inst.getOid();
        } else {
          oid = instance.getOid();
        }

        row.add(new CellModel(oid, field.getReference() != null, strValue));

      }

      this.values.add(row);

    }
  }

  /**
   * The instance method sorts the table values depending on the sort key in
   * {@link #sortKey} and the sort direction in {@link #sortDirection}.
   * 
   * @throws EFapsException
   */
  public boolean sort() throws EFapsException {

    if (getSortKey() != null && getSortKey().length() > 0) {
      int sortKey = 0;
      for (int i = 0; i < getTable().getFields().size(); i++) {
        Field field = (Field) getTable().getFields().get(i);
        if (field.getName().equals(getSortKey())) {
          sortKey = i;
          break;
        }
      }
      final int index = sortKey;
      Collections.sort(this.values, new Comparator<RowModel>() {

        public int compare(RowModel _o1, RowModel _o2) {

          String a1 = (_o1.getValues().get(index)).getCellValue();
          String a2 = (_o2.getValues().get(index)).getCellValue();
          return a1.compareTo(a2);
        }
      });
      if (getSortDirection() != null && getSortDirection().equals("-")) {
        Collections.reverse(this.values);
      }
    }

    return true;
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
   *                new value for instance variable {@link #sortKey}
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
   * This is the getter method for the instance variable {@link #table}.
   * 
   * @return value of instance variable {@link #table}
   * @see #table
   */
  public Table getTable() {
    return Table.get(this.tableuuid);
  }

  /**
   * @return <i>true</i> if the check boxes must be shown, other <i>false</i>
   *         is returned.
   * @see #showCheckBoxes
   */
  public boolean isShowCheckBoxes() {
    return this.showCheckBoxes;
  }

  /**
   * are the values of the Rows filtered or not
   * 
   * @return true if filtered, else false
   */
  public boolean isFiltered() {
    return !this.filterValues.isEmpty();
  }

  /**
   * This is the getter method for the instance variable {@link #values}.
   * 
   * @return value of instance variable {@link #values}
   * @throws EFapsException
   * @see #values
   * @see #setValues
   */
  public List<RowModel> getValues() throws EFapsException {
    List<RowModel> ret = new ArrayList<RowModel>();
    if (isFiltered()) {
      for (RowModel row : this.values) {
        boolean filtered = false;
        String value = (row.getValues().get(this.filterKeyInt)).getCellValue();
        for (String key : this.filterValues.keySet()) {
          if (value.equals(key)) {
            filtered = true;
          }
        }
        if (filtered) {
          ret.add(row);
        }
      }
    } else {
      ret = this.values;
    }

    return ret;
  }

  /**
   * This is the getter method for the instance variable {@link #filterKeyInt}.
   * 
   * @return value of instance variable {@link #filterKeyInt}
   * @see #filterKey
   * @see #setFilterKey
   */
  public String getFilterKey() {
    return this.filterKey;
  }

  /**
   * This is the setter method for the instance variable {@link #filterKeyInt}.
   * 
   * @param _selectedFilter
   *                new value for instance variable {@link #filterKeyInt}
   * @see #filterKeyInt
   * @see #getFilterKey
   */
  public void setFilterKey(String _filterkey) {
    this.filterKey = _filterkey;
    for (int i = 0; i < getTable().getFields().size(); i++) {
      Field field = (Field) getTable().getFields().get(i);
      if (field.getName().equals(_filterkey)) {
        this.filterKeyInt = i;
        break;
      }
    }

  }

  /**
   * The inner class stores one row of the table.
   */
  public class RowModel implements IClusterable {

    // /////////////////////////////////////////////////////////////////////////
    // instance variables

    private static final long serialVersionUID = 1L;

    /**
     * The instance variable stores the values for the table.
     * 
     * @see #getValues
     */
    private final List<CellModel> values = new ArrayList<CellModel>();

    /**
     * The instance variable stores all oids in a string.
     * 
     * @see #getOids
     */
    private final String oids;

    // /////////////////////////////////////////////////////////////////////////
    // contructors / destructors

    /**
     * The constructor creates a new instance of class Row.
     * 
     * @param _oids
     *                string with all oids for this row
     */
    public RowModel(final String _oids) {
      this.oids = _oids;
    }

    // /////////////////////////////////////////////////////////////////////////
    // instance methods

    /**
     * The instance method adds a new attribute value (from instance
     * {@link AttributeTypeInterface}) to the values.
     * 
     * @see #values
     */
    public void add(final CellModel _cellmodel) {

      this.values.add(_cellmodel);
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
     * This is the getter method for the values variable {@link #values}.
     * 
     * @return value of values variable {@link #values}
     * @see #values
     */
    public List<CellModel> getValues() {
      return this.values;
    }

    /**
     * This is the getter method for the instance variable {@link #oids}.
     * 
     * @return value of instance variable {@link #oids}
     * @see #oids
     */
    public String getOids() {
      return this.oids;
    }
  }

  public class CellModel implements IClusterable {

    private static final long serialVersionUID = 1L;

    private final String oid;

    private final boolean hasReference;

    private final String cellvalue;

    public CellModel(final String _oid, final boolean _hasReference,
                     final String _cellvalue) {
      this.oid = _oid;
      this.hasReference = _hasReference;
      this.cellvalue = _cellvalue;
    }

    public String getOid() {
      return this.oid;
    }

    public boolean hasReference() {
      return this.hasReference;
    }

    public String getCellValue() {
      return this.cellvalue;
    }
  }
}
