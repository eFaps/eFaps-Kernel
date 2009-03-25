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

package org.efaps.ui.wicket.models.objects;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.ui.FieldValue;
import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.ui.AbstractCommand;
import org.efaps.admin.ui.Image;
import org.efaps.admin.ui.Table;
import org.efaps.admin.ui.AbstractCommand.SortDirection;
import org.efaps.admin.ui.field.Field;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.ListQuery;
import org.efaps.ui.wicket.models.cell.UITableCell;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.util.EFapsException;

/**
 * TODO description!
 *
 * @author jmox
 * @version $Id$
 */
public class UITable extends AbstractUIObject {

  /**
   * This enum holds the Values used as part of the key for the UserAttributes
   * wich belong to a TableModel.
   *
   * @author jmox
   * @version $Id$
   */
  public static enum UserAttributeKey {
    /**
     * Key used for the order of Columns.
     */
    COLUMNORDER("columnOrder"),
    /**
     * Key used for the widths of Columns.
     */
    COLUMNWIDTH("columnWidths"),
    /**
     * Key used for the sort direction.
     */
    SORTDIRECTION("sortDirection"),
    /**
     * Key used for the Column.
     */
    SORTKEY("sortKey");

    /**
     * Value of the user attribute.
     */
    private final String value;

    /**
     * Constructor setting the instance variable.
     * @param _value Value
     */
    private UserAttributeKey(final String _value) {
      this.value = _value;
    }

    /**
     * @return the value
     */
    public String getValue() {
      return this.value;
    }
  }

  /**
   * Logging instance used in this class.
   */
  private static final Logger LOG = LoggerFactory.getLogger(UITable.class);

  /**
   * Serial Id.
   */
  private static final long serialVersionUID = 1L;

  /**
   * The instance variable stores the current selected filterKey as the name of
   * the field of this web table representation.
   */
  private String filterKey;

  /**
   * The instance variable stores the current selected filterKey as the
   * sequential number of the field of this web table representation.
   *
   * @see #getFilterKey
   * @see #setFilterKey(String)
   */
  private int filterKeyInt = 0;

  /**
   * Contains the sequential numbers of the filter.
   */
  private String[] filterSequence;

  /**
   * The instance Map contains the Values to be filtered.
   */
  private List<String> filterValues = new ArrayList<String>();

  /**
   * The instance Array holds the Label for the Columns.
   */
  private final List<UITableHeader> headers = new ArrayList<UITableHeader>();

  /**
   * This instance variable sores if the Table should show CheckBodes.
   */
  private boolean showCheckBoxes;

  /**
   * The instance variable stores the string of the sort direction.
   *
   * @see #getSortDirection
   * @see #setSortDirection
   */
  private SortDirection sortDirection = SortDirection.NONE;

  /**
   * The instance variable stores the string of the sort key.
   *
   * @see #getSortKey
   * @see #setSortKey
   */
  private String sortKey = null;

  /**
   * This instance variable stores the Id of the table. This int is used to
   * distinguish tables in case that there are more than one table on one page.
   */
  private int tableId = 1;

  /**
   * The instance variable stores the UUID for the table which must be shown.
   *
   * @see #getTable
   */
  private UUID tableUUID;

  /**
   * This instance variable stores if the Widths of the Columns are set by
   * UserAttributes.
   */
  private boolean userWidths = false;

  /**
   * All evaluated rows of this table are stored in this list.
   *
   * @see #getValues
   */
  private final List<UIRow> values = new ArrayList<UIRow>();

  /**
   * This instance variable stores the total weight of the widths of the Cells.
   * (Sum of all widths)
   */
  private int widthWeight;

  /**
   * Constructor setting the parameters.
   *
   * @param _parameters PageParameters
   */
  public UITable(final PageParameters _parameters) {
    super(_parameters);
    initialise();
  }

  /**
   * Constructor setting the uuid and Key of the instance.
   *
   * @param _commandUUID  UUID of the Command
   * @param _instanceKey  Key of the instance
   */
  public UITable(final UUID _commandUUID, final String _instanceKey) {
    super(_commandUUID, _instanceKey);
    initialise();
  }

  /**
   * Constructor setting the uuid and Key of the instance.
   *
   * @param _commandUUID  UUID of the Command
   * @param _instanceKey  Key of the instance
   * @param _openerId     id of the opener
   */
  public UITable(final UUID _commandUUID, final String _instanceKey,
                 final String _openerId) {
    super(_commandUUID, _instanceKey, _openerId);
    initialise();
  }

  /**
   * Method to get the List of Instances for the table.
   * @return List with List of instances
   * @throws EFapsException on error
   */
  @SuppressWarnings("unchecked")
  protected List<List<Instance>> getInstanceLists() throws EFapsException {
    final List<Return> ret =
        getCommand().executeEvents(EventType.UI_TABLE_EVALUATE,
                                   ParameterValues.INSTANCE,
                                   getInstance());
    final List<List<Instance>> lists =
        (List<List<Instance>>) ret.get(0).get(ReturnValues.VALUES);
    return lists;
  }

  /**
   * this method executes the TableModel, that means this method has to be
   * called so that this model contains actual data from the eFaps-DataBase. The
   * method works in conjunction with {@link #executeRowResult(Map, ListQuery)}.
   */
  public void execute() {
    try {
      // first get list of object ids

      final List<List<Instance>> lists = getInstanceLists();

      final List<Instance> instances = new ArrayList<Instance>();
      final Map<Instance, List<Instance>> instMapper =
          new HashMap<Instance, List<Instance>>();
      for (final List<Instance> oneList : lists) {
        final Instance inst = oneList.get(oneList.size() - 1);
        instances.add(inst);
        instMapper.put(inst, oneList);
      }

      // evaluate for all expressions in the table
      final ListQuery query = new ListQuery(instances);
      final List<Integer> userWidthList = getUserWidths();

      final List<Field> fields = getUserSortedColumns();

      for (int i = 0; i < fields.size(); i++) {
        final Field field = fields.get(i);
        if (field.getExpression() != null) {
          query.addSelect(field.getExpression());
        }
        if (field.getAlternateOID() != null) {
          query.addSelect(field.getAlternateOID());
        }
        SortDirection sortdirection = SortDirection.NONE;
        if (field.getName().equals(this.sortKey)) {
          sortdirection = getSortDirection();
        }
        final UITableHeader headermodel
                                    = new UITableHeader(field, sortdirection);
        this.headers.add(headermodel);
        if (!field.isFixedWidth()) {
          if (userWidthList != null) {
            if (isShowCheckBoxes()) {
              headermodel.setWidth(userWidthList.get(i + 1));
            } else {
              headermodel.setWidth(userWidthList.get(i));
            }
          }
          this.widthWeight += field.getWidth();
        }
      }
      query.execute();

      executeRowResult(instMapper, query, fields);

      if (this.sortKey != null) {
        sort();
      }
      super.setInitialised(true);
    } catch (final Exception e) {
      throw new RestartResponseException(new ErrorPage(e));
    }
  }

  /**
   * This method works together with {@link #execute()} to fill this Model with
   * Data.
   *
   * @param _instMapper   Map of instances
   * @param _query        Query with results
   * @param _fields       List of the Fields
   */
  private void executeRowResult(final Map<Instance, List<Instance>> _instMapper,
                                final ListQuery _query,
                                final List<Field> _fields) {
    try {
      while (_query.next()) {

        // get all found oids (typically more than one if it is an expand)
        Instance instance = _query.getInstance();
        final StringBuilder instanceKeys = new StringBuilder();
        boolean first = true;
        if (_instMapper.get(instance) != null) {
          final List<Instance> list = _instMapper.get(instance);
          final Instance inst = list.get(list.size() - 1);
          if (!instance.getKey().equals(inst.getKey())) {
            instance = inst;
          }
          for (final Instance oneInstance : list) {
            if (first) {
              first = false;
            } else {
              instanceKeys.append("|");
            }
            instanceKeys.append(oneInstance.getKey());
          }
        }
        final UIRow row = new UIRow(instanceKeys.toString());
        Attribute attr = null;

        String strValue = "";
        for (final Field field : _fields) {
          Object value = null;

          if (field.getExpression() != null) {
            value = _query.get(field.getExpression());
            attr = _query.getAttribute(field.getExpression());
          }

          final FieldValue fieldvalue =
                                  new FieldValue(field, attr, value, instance);
          if (isCreateMode() && field.isEditable()) {
            strValue = fieldvalue.getCreateHtml(getInstance(), instance);
          } else if (isEditMode() && field.isEditable()) {
            strValue = fieldvalue.getEditHtml(getInstance(), instance);
          } else {
            strValue = fieldvalue.getViewHtml(getInstance(), instance);
          }
          if (strValue == null) {
            strValue = "";
          }
          String icon = field.getIcon();
          if (field.getAlternateOID() == null) {
            if (field.isShowTypeIcon()) {
              final Image image = Image.getTypeIcon(instance.getType());
              if (image != null) {
                icon = image.getUrl();
              }
            }
          } else {
            final Instance inst =
                Instance.get((String) _query.get(field.getAlternateOID()));
            if (field.isShowTypeIcon()) {
              final Image image = Image.getTypeIcon(inst.getType());
              if (image != null) {
                icon = image.getUrl();
              }
            }
          }
          row.add(new UITableCell(this, fieldvalue, instance, strValue,
                                  icon));
        }
        this.values.add(row);
      }
    } catch (final Exception e) {
      throw new RestartResponseException(new ErrorPage(e));
    }
  }

  /**
   * Prepares the filter to be used in getValues.
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
   * @param _filterkey new value for instance variable {@link #filterKeyInt}
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
   * This is the setter method for the instance variable
   * {@link #filterSequence}.
   *
   * @param _filter new value for instance variable {@link #filterSequence}
   */
  public void setFilter(final String[] _filter) {
    this.filterSequence = _filter;
  }

  /**
   *
   * @return Map
   * @throws EFapsException
   */
  public List<String> getFilterList() {
    final List<String> filterList = new ArrayList<String>();
    this.filterValues = filterList;

    for (final UIRow rowmodel : this.values) {
      final UITableCell cellmodel =
          rowmodel.getValues().get(this.filterKeyInt);
      final String value = cellmodel.getCellValue();
      if (!filterList.contains(value)) {
        filterList.add(value);
      }
    }
    return filterList;
  }

  /**
   * This is the getter method for the instance variable {@link #headers}.
   *
   * @return value of instance variable {@link #headers}
   */
  public List<UITableHeader> getHeaders() {
    return this.headers;
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

  /**
   * Method to set he sort direction.
   * @param _sortdirection sort direction to set
   */
  public void setSortDirection(final SortDirection _sortdirection) {
    this.sortDirection = _sortdirection;
    try {
      Context.getThreadContext().setUserAttribute(
          getUserAttributeKey(UserAttributeKey.SORTDIRECTION),
                             _sortdirection.value);
    } catch (final EFapsException e) {
      // we don't throw an error because this are only Usersettings
      LOG.error("error during the retrieve of UserAttributes", e);
    }
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
    } catch (final EFapsException e) {
      // we don't throw an error because this are only Usersettings
      LOG.error("error during the retrieve of UserAttributes", e);
    }

  }

  /**
   * This is the getter method for the instance variable {@link #table}.
   *
   * @return value of instance variable {@link #table}
   * @see #table
   */
  public Table getTable() {
    return Table.get(this.tableUUID);
  }

  /**
   * This is the getter method for the instance variable {@link #tableId}.
   *
   * @return value of instance variable {@link #tableId}
   */
  public int getTableId() {
    return this.tableId * 100;
  }

  /**
   * This is the setter method for the instance variable {@link #tableId}.
   *
   * @param _tableId
   *                the tableId to set
   */
  public void setTableId(final int _tableId) {
    this.tableId = _tableId;
  }

  /**
   * This method generates the Key for a UserAttribute by using the UUID of the
   * Command and the given UserAttributeKey, so that for every Table a unique
   * key for sorting etc, is created.
   *
   * @param _key    UserAttributeKey the Key is wanted
   * @return String with the key
   */
  public String getUserAttributeKey(final UserAttributeKey _key) {
    return super.getCommandUUID() + "-" + _key.getValue();
  }

  /**
   * This method looks if for this TableModel a UserAttribute for the sorting of
   * the Columns exist. If they exist the Fields will be sorted as defined by
   * the User. If no definition of the User exist the Original default sorting
   * of the columns will be used. In the Case that the Definition of the Table
   * was altered Field wich are not sorted yet will be sorted in at the last
   * position.
   *
   * @return  List of fields
   */
  private List<Field> getUserSortedColumns() {
    final List<Field> fields = getTable().getFields();
    List<Field> ret = new ArrayList<Field>();
    try {
      if (Context.getThreadContext().containsUserAttribute(
          getUserAttributeKey(UserAttributeKey.COLUMNORDER))) {

        final String columnOrder =
            Context.getThreadContext().getUserAttribute(
                getUserAttributeKey(UserAttributeKey.COLUMNORDER));

        final StringTokenizer tokens = new StringTokenizer(columnOrder, ";");
        while (tokens.hasMoreTokens()) {
          final String fieldname = tokens.nextToken();
          for (int i = 0; i < fields.size(); i++) {
            if (fieldname.equals(fields.get(i).getName())) {
              ret.add(fields.get(i));
              fields.remove(i);
            }
          }
        }
        if (!fields.isEmpty()) {
          for (final Field field : fields) {
            ret.add(field);
          }
        }

      } else {
        ret = fields;
      }
      return ret;
    } catch (final EFapsException e) {
      e.printStackTrace();
    }
    return fields;
  }

  /**
   * This method retieves the UserAttribute for the ColumnWidths and evaluates
   * the string.
   *
   * @return List with the values of the columns in Pixel
   */
  private List<Integer> getUserWidths() {
    try {
      if (Context.getThreadContext().containsUserAttribute(
          getUserAttributeKey(UserAttributeKey.COLUMNWIDTH))) {
        this.userWidths = true;
        final String widths =
            Context.getThreadContext().getUserAttribute(
                getUserAttributeKey(UserAttributeKey.COLUMNWIDTH));

        final StringTokenizer tokens = new StringTokenizer(widths, ";");

        final List<Integer> wList = new ArrayList<Integer>();

        while (tokens.hasMoreTokens()) {
          final String token = tokens.nextToken();
          for (int i = 0; i < token.length(); i++) {
            if (!Character.isDigit(token.charAt(i))) {
              final int width = Integer.parseInt(token.substring(0, i));
              wList.add(width);
              break;
            }
          }
        }
        return wList;
      }
    } catch (final NumberFormatException e) {
      // we don't throw an error because this are only Usersettings
      LOG.error(
          "error during the retrieve of UserAttributes in getUserWidths()", e);
    } catch (final EFapsException e) {
      // we don't throw an error because this are only Usersettings
      LOG.error(
          "error during the retrieve of UserAttributes in getUserWidths()", e);
    }
    return null;
  }

  /**
   * This is the getter method for the instance variable {@link #values}.
   *
   * @return value of instance variable {@link #values}
   * @throws EFapsException
   * @see #values
   * @see #setValues
   */
  public List<UIRow> getValues() {
    List<UIRow> ret = new ArrayList<UIRow>();
    if (isFiltered()) {
      for (final UIRow row : this.values) {
        boolean filtered = false;
        final String value =
            (row.getValues().get(this.filterKeyInt)).getCellValue();
        for (final String key : this.filterValues) {
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
   * This is the getter method for the instance variable {@link #widthWeight}.
   *
   * @return value of instance variable {@link #widthWeight}
   */
  public int getWidthWeight() {
    return this.widthWeight;
  }

  /**
   * Method that initializes the TableModel.
   */
  private void initialise() {
    final AbstractCommand command = getCommand();
    if (command == null) {
      this.showCheckBoxes = false;
    } else {
      // set target table
      if (command.getTargetTable() != null) {
        this.tableUUID = command.getTargetTable().getUUID();
      }
      // set default sort
      if (command.getTargetTableSortKey() != null) {
        this.sortKey = command.getTargetTableSortKey();
        this.sortDirection = command.getTargetTableSortDirection();
      }

      // set show check boxes
 //     final boolean showCheckBoxesTmp = command.isTargetShowCheckBoxes() && command.hasEvents(EventType.UI_COMMAND_EXECUTE);
//      if (!showCheckBoxesTmp) {
//        final UUID cldUUID = UUID.fromString(getParameter("command"));
//        if (cldUUID != null) {
//          final AbstractCommand cmd = getCommand(cldUUID);
//          showCheckBoxesTmp =
//              (cmd != null) && command.hasEvents(EventType.UI_COMMAND_EXECUTE);
//        }
//      }
      this.showCheckBoxes = command.isTargetShowCheckBoxes();
      // get the User spesific Attributes if exist overwrite the defaults
      try {
        if (Context.getThreadContext().containsUserAttribute(
            getUserAttributeKey(UserAttributeKey.SORTKEY))) {
          this.sortKey =
              Context.getThreadContext().getUserAttribute(
                  getUserAttributeKey(UserAttributeKey.SORTKEY));
        }
        if (Context.getThreadContext().containsUserAttribute(
            getUserAttributeKey(UserAttributeKey.SORTDIRECTION))) {
          this.sortDirection =
              SortDirection
                  .getEnum((Context.getThreadContext().getUserAttribute(
                         getUserAttributeKey(UserAttributeKey.SORTDIRECTION))));
        }
      } catch (final EFapsException e) {
        // we don't throw an error because this are only Usersettings
        LOG.error("error during the retrieve of UserAttributes", e);
      }
    }

  }

  /**
   * Are the values of the Rows filtered or not.
   *
   * @return true if filtered, else false
   */
  public boolean isFiltered() {
    return !this.filterValues.isEmpty();
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
   * This is the setter method for the
   * instance variable {@link #showCheckBoxes}.
   *
   * @param _showCheckBoxes    the showCheckBoxes to set
   */
  public void setShowCheckBoxes(final boolean _showCheckBoxes) {
    this.showCheckBoxes = _showCheckBoxes;
  }

  /**
   * This is the getter method for the instance variable {@link #userWidths}.
   *
   * @return value of instance variable {@link #userWidths}
   */
  public boolean isUserSetWidth() {
    return this.userWidths;
  }

  /**
   * Method to remove the filter.
   */
  public void removeFilter() {
    this.filterSequence = null;
    this.filterKey = null;
    this.filterValues.clear();
  }

  /**
   * Method to reset the Model.
   *
   * @see org.efaps.ui.wicket.models.AbstractModel#resetModel()
   */
  @Override
  public void resetModel() {
    super.setInitialised(false);
    this.values.clear();
    this.headers.clear();
  }

  /**
   * Method to set the order of the columns.
   *
   * @param _markupsIds ids of the columns as a string with ; separated
   */
  public void setColumnOrder(final String _markupsIds) {
    final StringTokenizer tokens = new StringTokenizer(_markupsIds, ";");
    final StringBuilder columnOrder = new StringBuilder();
    while (tokens.hasMoreTokens()) {
      final String markupId = tokens.nextToken();
      for (final UITableHeader header : this.headers) {
        if (markupId.equals(header.getMarkupId())) {
          columnOrder.append(header.getName()).append(";");
          break;
        }
      }
    }
    try {
      Context.getThreadContext().setUserAttribute(
          getUserAttributeKey(UserAttributeKey.COLUMNORDER),
          columnOrder.toString());
    } catch (final EFapsException e) {
      // we don't throw an error because this are only Usersettings
      LOG.error("error during the setting of UserAttributes", e);
    }
  }

  /**
   * This is the setter method for the instance variable {@link #tableUUID}.
   *
   * @param _tableUUID  the tableUUID to set
   */
  protected void setTableUUID(final UUID _tableUUID) {
    this.tableUUID = _tableUUID;
  }

  /**
   * The instance method sorts the table values depending on the sort key in
   * {@link #sortKey} and the sort direction in {@link #sortDirection}.
   */
  public void sort() {
    if (getSortKey() != null && getSortKey().length() > 0) {
      int sortKeyTmp = 0;
      for (int i = 0; i < getTable().getFields().size(); i++) {
        final Field field = getTable().getFields().get(i);
        if (field.getName().equals(getSortKey())) {
          sortKeyTmp = i;
          break;
        }
      }
      final int index = sortKeyTmp;
      Collections.sort(this.values, new Comparator<UIRow>() {

        public int compare(final UIRow _rowModel1, final UIRow _rowModel2) {


          final UITableCell cellModel1 = _rowModel1.getValues().get(index);
          final FieldValue fValue1
                         = new FieldValue(getTable().getFields().get(index),
                                         cellModel1.getUiClass(),
                                         cellModel1.getCompareValue() != null
                                           ? cellModel1.getCompareValue()
                                           : cellModel1.getCellValue());

          final UITableCell cellModel2 = _rowModel2.getValues().get(index);
          final FieldValue fValue2
                         = new FieldValue(getTable().getFields().get(index),
                                         cellModel2.getUiClass(),
                                         cellModel2.getCompareValue() != null
                                           ? cellModel2.getCompareValue()
                                           : cellModel2.getCellValue());

          return fValue1.compareTo(fValue2);
        }
      });
      if (getSortDirection() == SortDirection.DESCENDING) {
        Collections.reverse(this.values);
      }
    }
  }
}
