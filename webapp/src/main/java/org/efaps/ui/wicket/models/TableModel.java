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
 * Revision:        $Rev:1510 $
 * Last Changed:    $Date:2007-10-18 09:35:40 -0500 (Thu, 18 Oct 2007) $
 * Last Changed By: $Author:jmox $
 */

package org.efaps.ui.wicket.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;

import org.apache.wicket.PageParameters;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.model.Model;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.AttributeTypeInterface;
import org.efaps.admin.datamodel.ui.FieldDefinition;
import org.efaps.admin.datamodel.ui.FieldValue;
import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.ui.CommandAbstract;
import org.efaps.admin.ui.Field;
import org.efaps.admin.ui.Image;
import org.efaps.admin.ui.Table;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.ListQuery;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.util.EFapsException;

/**
 * @author jmox
 * @version $Id:TableModel.java 1510 2007-10-18 14:35:40Z jmox $
 */
public class TableModel extends AbstractModel {

  /**
   * enum holding the different directions a column can be sorted
   */
  public static enum SortDirection {
    DESCENDING,
    ASCENDING,
    NONE;
  }

  public static enum UserAttributeKey {
    SORTKEY("sortKey"),
    COLUMNWIDTH("columnWidths");

    public String value;

    private UserAttributeKey(String _value) {
      this.value = _value;
    }
  }

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
  private SortDirection sortDirection = SortDirection.NONE;

  /**
   * The instance variable stores the UUID for the table which must be shown.
   *
   * @see #getTable
   */
  private UUID tableuuid;

  /**
   * This instance variable sores if the Table should show CheckBodes
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
  private List<String> filterValues = new ArrayList<String>();

  /**
   * The instance Array holds the Label for the Columns
   */
  private final List<HeaderModel> headers = new ArrayList<HeaderModel>();

  /**
   * contains the sequential numbers of the filter
   */
  private String[] filterSequence;

  /**
   * This instance variable stores the total weight of the widths of the Cells.
   * (Sum of all widths)
   */
  private int widthWeight;

  private boolean userWidths = false;

  public TableModel(PageParameters _parameters) {
    super(_parameters);
    initialise();
  }

  private void initialise() {
    final CommandAbstract command = getCommand();
    if (command == null) {
      this.showCheckBoxes = false;
    } else {
      // set target table
      this.tableuuid = command.getTargetTable().getUUID();

      // set default sort
      if (command.getTargetTableSortKey() != null) {
        this.sortKey = getCommand().getTargetTableSortKey();
        if (command.getTargetTableSortDirection() == CommandAbstract.TABLE_SORT_DIRECTION_DESC) {
          this.sortDirection = SortDirection.DESCENDING;
        } else {
          this.sortDirection = SortDirection.ASCENDING;
        }
      }

      // set show check boxes
      boolean showCheckBoxes = getCommand().isTargetShowCheckBoxes();
      if (!showCheckBoxes) {
        final UUID cldUUID = UUID.fromString(getParameter("command"));
        if (cldUUID != null) {
          final CommandAbstract cmd = getCommand(cldUUID);
          showCheckBoxes =
              (cmd != null) && cmd.hasEvents(EventType.UI_COMMAND_EXECUTE);
        }
      }
      this.showCheckBoxes = showCheckBoxes;
      try {
        if (Context.getThreadContext().containsUserAtribute(
            command.getUUID().toString() + "-sortKey")) {
          this.sortKey =
              Context.getThreadContext().getUserAttribute(
                  getUserAttributeKey(UserAttributeKey.SORTKEY));
        }
      } catch (EFapsException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

  }

  @Override
  public void resetModel() {
    super.setInitialised(false);
    this.values.clear();
    this.headers.clear();
  }

  @SuppressWarnings("unchecked")
  public void execute() {
    try {
      // first get list of object ids
      final List<Return> ret =
          getCommand().executeEvents(EventType.UI_TABLE_EVALUATE,
              ParameterValues.INSTANCE, new Instance(super.getOid()));

      final List<List<Instance>> lists =
          (List<List<Instance>>) ret.get(0).get(ReturnValues.VALUES);
      final List<Instance> instances = new ArrayList<Instance>();
      final Map<Instance, List<Instance>> instMapper =
          new HashMap<Instance, List<Instance>>();
      for (List<Instance> oneList : lists) {
        final Instance inst = oneList.get(oneList.size() - 1);
        instances.add(inst);
        instMapper.put(inst, oneList);
      }

      // evaluate for all expressions in the table
      final ListQuery query = new ListQuery(instances);
      final List<Integer> userWidths = getUserWidths();

      for (int i = 0; i < this.getTable().getFields().size(); i++) {
        final Field field = this.getTable().getFields().get(i);
        if (field.getExpression() != null) {
          query.addSelect(field.getExpression());
        }
        if (field.getAlternateOID() != null) {
          query.addSelect(field.getAlternateOID());
        }
        SortDirection sortdirection = SortDirection.NONE;
        if (field.getName().equals(this.sortKey)) {
          sortdirection = this.getSortDirection();
        }
        HeaderModel headermodel = new HeaderModel(field, sortdirection);
        this.headers.add(headermodel);
        if (!field.isFixedWidth()) {
          if (userWidths != null) {
            if (this.isShowCheckBoxes()) {
              headermodel.setWidth(userWidths.get(i + 1));
            } else {
              headermodel.setWidth(userWidths.get(i));
            }
          }
          this.widthWeight += field.getWidth();
        }
      }
      query.execute();

      this.executeRowResult(instMapper, query);

      if (this.sortKey != null) {
        this.sort();
      }
      super.setInitialised(true);
    } catch (Exception e) {
      throw new RestartResponseException(new ErrorPage(e));
    }
  }

  private List<Integer> getUserWidths() throws EFapsException {
    if (Context.getThreadContext().containsUserAtribute(
        getUserAttributeKey(UserAttributeKey.COLUMNWIDTH))) {
      this.userWidths = true;
      final String widths =
          Context.getThreadContext().getUserAttribute(
              getUserAttributeKey(UserAttributeKey.COLUMNWIDTH));

      final StringTokenizer tokens = new StringTokenizer(widths, ";");

      final List<Integer> wList = new ArrayList<Integer>();

      while (tokens.hasMoreTokens()) {
        String token = tokens.nextToken();
        for (int i = 0; i < token.length(); i++) {
          if (!Character.isDigit(token.charAt(i))) {
            int width = Integer.parseInt(token.substring(0, i));
            wList.add(width);
            break;
          }
        }
      }
      return wList;
    }
    return null;
  }

  public List<HeaderModel> getHeaders() {
    return this.headers;
  }

  private void executeRowResult(
                                final Map<Instance, List<Instance>> _instMapper,
                                final ListQuery _query) {
    try {
      while (_query.next()) {

        // get all found oids (typically more than one if it is an expand)
        final Instance instance = _query.getInstance();
        final StringBuilder oids = new StringBuilder();
        boolean first = true;
        for (Instance oneInstance : _instMapper.get(instance)) {
          if (first) {
            first = false;
          } else {
            oids.append("|");
          }
          oids.append(oneInstance.getOid());
        }
        final RowModel row = new RowModel(oids.toString());
        Attribute attr = null;

        String strValue = "";
        String oid = "";
        for (Field field : this.getTable().getFields()) {
          Object value = null;

          if (field.getExpression() != null) {
            value = _query.get(field.getExpression());
            attr = _query.getAttribute(field.getExpression());
          }

          final FieldValue fieldvalue =
              new FieldValue(new FieldDefinition("egal", field), attr, value,
                  instance);
          if (value == null) {
            strValue = "";
          } else {
            if (this.isCreateMode() && field.isEditable()) {
              strValue = fieldvalue.getCreateHtml();
            } else if (this.isEditMode() && field.isEditable()) {
              strValue = fieldvalue.getEditHtml();
            } else {
              strValue = fieldvalue.getViewHtml();
            }
          }
          String icon = field.getIcon();
          if (field.getAlternateOID() == null) {
            oid = instance.getOid();
            if (field.isShowTypeIcon()) {
              final Image image = Image.getTypeIcon(instance.getType());
              if (image != null) {
                icon = image.getUrl();
              }
            }
          } else {
            final Instance inst =
                new Instance((String) _query.get(field.getAlternateOID()));
            oid = inst.getOid();
            if (field.isShowTypeIcon()) {
              final Image image = Image.getTypeIcon(inst.getType());
              if (image != null) {
                icon = image.getUrl();
              }
            }

          }
          row.add(new CellModel(field, oid, strValue, icon));
        }
        this.values.add(row);
      }
    } catch (Exception e) {
      throw new RestartResponseException(new ErrorPage(e));
    }
  }

  /**
   * The instance method sorts the table values depending on the sort key in
   * {@link #sortKey} and the sort direction in {@link #sortDirection}.
   */
  public boolean sort() {

    if (getSortKey() != null && getSortKey().length() > 0) {
      int sortKey = 0;
      for (int i = 0; i < getTable().getFields().size(); i++) {
        final Field field = getTable().getFields().get(i);
        if (field.getName().equals(getSortKey())) {
          sortKey = i;
          break;
        }
      }
      final int index = sortKey;
      Collections.sort(this.values, new Comparator<RowModel>() {

        public int compare(RowModel _rowModel1, RowModel _rowModel2) {

          final String value1 =
              (_rowModel1.getValues().get(index)).getCellValue();
          final String value2 =
              (_rowModel2.getValues().get(index)).getCellValue();
          return value1.compareTo(value2);
        }
      });
      if (getSortDirection() == SortDirection.DESCENDING) {
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
  public void setSortKey(final String _sortKey) {
    this.sortKey = _sortKey;
    try {
      Context.getThreadContext().setUserAttribute(
          getUserAttributeKey(UserAttributeKey.SORTKEY), _sortKey);
    } catch (EFapsException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  /**
   * This is the getter method for the instance variable {@link #sortDirection}.
   *
   * @return value of instance variable {@link #sortDirection}
   * @see #sortDirection
   * @see #setSortDirection
   */
  public SortDirection getSortDirection() {
    return this.sortDirection;
  }

  public void setSortDirection(final SortDirection _sortdirection) {
    this.sortDirection = _sortdirection;
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
    boolean ret;
    if (super.isSubmit()) {
      ret = true;
    } else {
      ret = this.showCheckBoxes;
    }
    return ret;
  }

  /**
   * are the values of the Rows filtered or not
   *
   * @return true if filtered, else false
   */
  public boolean isFiltered() {
    return !this.filterValues.isEmpty();
  }

  public void removeFilter() {
    this.filterSequence = null;
    this.filterKey = null;
    this.filterValues.clear();
  }

  /**
   * This is the getter method for the instance variable {@link #values}.
   *
   * @return value of instance variable {@link #values}
   * @throws EFapsException
   * @see #values
   * @see #setValues
   */
  public List<RowModel> getValues() {
    List<RowModel> ret = new ArrayList<RowModel>();
    if (isFiltered()) {
      for (RowModel row : this.values) {
        boolean filtered = false;
        final String value =
            (row.getValues().get(this.filterKeyInt)).getCellValue();
        for (String key : this.filterValues) {
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
  public void setFilterKey(final String _filterkey) {
    this.filterKey = _filterkey;
    for (int i = 0; i < getTable().getFields().size(); i++) {
      final Field field = getTable().getFields().get(i);
      if (field.getName().equals(_filterkey)) {
        this.filterKeyInt = i;
        break;
      }
    }

  }

  /**
   * This Map is used for contruction of the items in a myfaces "<h:selectManyCheckbox>".
   * It produces Selectboxes with a sequential number as value and the
   * Fieldvalue as the Label.
   *
   * @return Map
   * @throws EFapsException
   */
  public List<String> getFilterList() {
    final List<String> filterList = new ArrayList<String>();
    this.filterValues = filterList;

    for (RowModel rowmodel : this.values) {
      final CellModel cellmodel = rowmodel.getValues().get(this.filterKeyInt);
      final String value = cellmodel.getCellValue();
      if (!filterList.contains(value)) {
        filterList.add(value);
      }
    }
    return filterList;
  }

  /**
   * prepares the filter to bes used in getValues
   *
   * @see #getValues()
   */
  public void filter() {
    if (this.filterSequence != null) {
      final List<String> filterList = new ArrayList<String>();
      for (int i = 0; i < this.filterSequence.length; i++) {
        final Integer intpos = Integer.valueOf(this.filterSequence[i]);
        filterList.add(this.filterValues.get(intpos));
      }
      this.filterValues = filterList;
    }
  }

  public void setFilter(final String[] _filter) {
    this.filterSequence = _filter;
  }

  /**
   * This is the getter method for the instance variable {@link #widthWeight}.
   *
   * @return value of instance variable {@link #widthWeight}
   */
  public int getWidthWeight() {
    return this.widthWeight;
  }

  public String getUserAttributeKey(UserAttributeKey _key) {
    return super.getCommandUUID() + "-" + _key.value;
  }

  public boolean isUserSetWidth() {
    return this.userWidths;
  }

  /**
   * The inner class stores one row of the table.
   */
  public class RowModel extends Model {

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

    /**
     * The constructor creates a new instance of class Row.
     *
     * @param _oids
     *                string with all oids for this row
     */
    public RowModel(final String _oids) {
      super();
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

}
