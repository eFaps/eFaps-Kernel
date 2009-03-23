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
 * Revision:        $Rev:1510 $
 * Last Changed:    $Date:2007-10-18 09:35:40 -0500 (Thu, 18 Oct 2007) $
 * Last Changed By: $Author:jmox $
 */

package org.efaps.ui.wicket.models.cell;

import java.util.ArrayList;
import java.util.List;

import org.efaps.admin.datamodel.ui.FieldValue;
import org.efaps.admin.datamodel.ui.UIInterface;
import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.ui.AbstractCommand;
import org.efaps.admin.ui.Menu;
import org.efaps.admin.ui.AbstractCommand.Target;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.admin.ui.field.Field;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.ui.wicket.models.AbstractInstanceObject;
import org.efaps.ui.wicket.models.objects.AbstractUIObject;
import org.efaps.util.EFapsException;

/**
 * This class represents the model wich is used for rendering the components of
 * one cell inside a Table.It uses a {@link org.efaps.admin.ui.field.Field} as
 * the base for the data.
 *
 * @author jmox
 * @version $Id:CellModel.java 1510 2007-10-18 14:35:40Z jmox $
 */
public class UITableCell extends AbstractInstanceObject {

    /**
   * instance variable storing the reference of the field.
   */
  private String reference;

  /**
   * Variable storing the value as it was retrieved from the eFaps-Database by
   * a query. The value is used for comparisons and to be able to access the
   * original value.
   */
  private final Object compareValue;

  /**
   * Variable storing the string representation of the value.
   * of the field.
   */
  private final String cellValue;

  /**
   * instance variable storing the icon of the field.
   */
  private final String icon;

  /**
   * instance variable storing the target of the field.
   */
  private final Target target;

  /**
   * instance variable storing if the cell is fixed width.
   */
  private final boolean fixedWidth;

  /**
   * Instance variable storing the name of the field.
   */
  private final String name;

  /**
   * Stores the underlying user interface class for this cell.
   */
  private final UIInterface uiClass;

  private final long fieldId;

  private final boolean creatable;

  private final boolean editable;

  private final boolean searchable;

  private final boolean viewable;

  private final AbstractUIObject parent;

  /**
   * Constructor.
   *
   * @param _fieldValue   FieldValue
   * @param _cellvalue    Value for the cell
   * @param _icon         icon of the cell
   * @param instance
   * @throws EFapsException on error
   */
  public UITableCell(final AbstractUIObject _parent,
                     final FieldValue _fieldValue, final Instance _instance,
                     final String _cellvalue, final String _icon)
      throws EFapsException  {
    super(_instance == null ? null : _instance.getKey());
    this.parent = _parent;
    this.uiClass =  _fieldValue.getClassUI();
    this.compareValue =  _fieldValue.getObject4Compare();
    this.target = _fieldValue.getField().getTarget();
    this.name = _fieldValue.getField().getName();
    this.fixedWidth = _fieldValue.getField().isFixedWidth();
    this.cellValue = _cellvalue;
    this.icon = _icon;
    this.fieldId = _fieldValue.getField().getId();
    this.creatable = _fieldValue.getField().isCreatable();
    this.editable = _fieldValue.getField().isEditable();
    this.searchable = _fieldValue.getField().isSearchable();
    this.viewable = _fieldValue.getField().isViewable();
    // check if the user has access to the typemenu, if not set the reference
    // to null
    if (_fieldValue.getField().getReference() != null) {
      if (getInstanceKey() != null) {
        final Menu menu = Menu.getTypeTreeMenu(_instance.getType());
        if (menu != null && menu.hasAccess(this.parent.getMode())) {
          this.reference = _fieldValue.getField().getReference();
        }
      }
    }
  }


  /**
   * This is the getter method for the instance variable {@link #reference}.
   *
   * @return value of instance variable {@link #reference}
   */

  public String getReference() {
    return this.reference;
  }

  /**
   * This is the setter method for the instance variable {@link #reference}.
   *
   * @param _reference
   *                the reference to set
   */
  public void setReference(final String _reference) {
    this.reference = _reference;
  }

  /**
   * This is the getter method for the instance variable {@link #compareValue}.
   *
   * @return value of instance variable {@link #compareValue}
   */
  public  Object getCompareValue() {
    return this.compareValue;
  }

  /**
   * This is the getter method for the instance variable {@link #cellvalue}.
   *
   * @return value of instance variable {@link #cellvalue}
   */
  public String getCellValue() {
    return this.cellValue;
  }

  /**
   * This is the getter method for the instance variable {@link #icon}.
   *
   * @return value of instance variable {@link #icon}
   */

  public String getIcon() {
    return this.icon;
  }

  /**
   * This is the getter method for the instance variable {@link #target}.
   *
   * @return value of instance variable {@link #target}
   */

  public Target getTarget() {
    return this.target;
  }

  /**
   * This is the getter method for the instance variable {@link #fixedWidth}.
   *
   * @return value of instance variable {@link #fixedWidth}
   */
  public boolean isFixedWidth() {
    return this.fixedWidth;
  }

  /**
   * This method returns if the field is a link which makes a checkout.
   *
   * @return true if it is a checkout
   */
  public boolean isCheckOut() {
    return this.reference.contains("/servlet/checkout");
  }

  /**
   * This is the getter method for the instance variable {@link #name}.
   *
   * @return value of instance variable {@link #name}
   */
  public String getName() {
    return this.name;
  }

  /**
   * This is the getter method for the instance variable {@link #uiClass}.
   *
   * @return value of instance variable {@link #uiClass}
   */
  public UIInterface getUiClass() {
    return this.uiClass;
  }

  /**
   * Getter method for instance variable {@link #fieldId}.
   *
   * @return value of instance variable {@link #fieldId}
   */
  public long getFieldId() {
    return this.fieldId;
  }

  public Field getField() {
    return Field.get(this.fieldId);
  }

  public List<Return> executeEvents(final Object _others,
                                    final EventType _eventType)
      throws EFapsException {
    List<Return> ret = new ArrayList<Return>();
    final Field field = getField();
    if (field.hasEvents(_eventType)) {
        final Context context = Context.getThreadContext();
        final String[] contextoid = { getInstanceKey() };
        context.getParameters().put("oid", contextoid);
        ret = field.executeEvents(_eventType,
                            ParameterValues.INSTANCE, getInstance(),
                            ParameterValues.OTHERS, _others,
                            ParameterValues.PARAMETERS, context.getParameters(),
                            ParameterValues.CLASS, this);
    }
  return ret;
}

  /**
   * Getter method for instance variable {@link #creatable}.
   *
   * @return value of instance variable {@link #creatable}
   */
  public boolean isCreatable() {
    return this.creatable;
  }

  /**
   * Getter method for instance variable {@link #editable}.
   *
   * @return value of instance variable {@link #editable}
   */
  public boolean isEditable() {
    return this.editable;
  }

  /**
   * Getter method for instance variable {@link #searchable}.
   *
   * @return value of instance variable {@link #searchable}
   */
  public boolean isSearchable() {
    return this.searchable;
  }

  /**
   * Getter method for instance variable {@link #viewable}.
   *
   * @return value of instance variable {@link #viewable}
   */
  public boolean isViewable() {
    return this.viewable;
  }

  public boolean render() {
    return ((this.parent.getMode() == TargetMode.CREATE && this.creatable)
          || (this.parent.getMode() == TargetMode.EDIT && this.editable)
          || (this.parent.getMode() == TargetMode.SEARCH && this.searchable)
          || (this.parent.getMode() == TargetMode.VIEW && this.viewable)
          || this.parent.getMode() == TargetMode.UNKNOWN
          || this.parent.getMode() == TargetMode.CONNECT);
  }

  /**
   * @see org.efaps.ui.wicket.models.AbstractInstanceObject#getInstanceFromManager()
   * @return
   * @throws EFapsException
   */
  @Override
  public Instance getInstanceFromManager() throws EFapsException {
    Instance ret = null;
    if (this.parent != null) {
      final AbstractCommand cmd = this.parent.getCommand();
      final List<Return> rets = cmd.executeEvents(EventType.UI_INSTANCEMANAGER,
                                  ParameterValues.OTHERS, getInstanceKey(),
                                  ParameterValues.PARAMETERS,
                                   Context.getThreadContext().getParameters());
      ret = (Instance) rets.get(0).get(ReturnValues.VALUES);
    }
    return ret;
  }


  /**
   * @see org.efaps.ui.wicket.models.AbstractInstanceObject#hasInstanceManager()
   * @return false
   */
  @Override
  public boolean hasInstanceManager() {
    return this.parent != null ? this.parent.hasInstanceManager() : false;
  }
}
