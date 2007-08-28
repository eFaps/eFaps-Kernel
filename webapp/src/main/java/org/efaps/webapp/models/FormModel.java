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
import java.util.List;
import java.util.UUID;

import org.apache.wicket.IClusterable;
import org.apache.wicket.PageParameters;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.datamodel.ui.FieldDefinition;
import org.efaps.admin.datamodel.ui.FieldValue;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.event.EventDefinition;
import org.efaps.admin.event.EventType;
import org.efaps.admin.ui.CommandAbstract;
import org.efaps.admin.ui.Field;
import org.efaps.admin.ui.Form;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;

public class FormModel extends ModelAbstract {
  private static final long serialVersionUID = 3026168649146801622L;

  /**
   * The instance variable stores the result list of the execution of the query.
   * 
   * @see #getValues
   * @see #setValues
   */
  private final List<FormRowModel> values = new ArrayList<FormRowModel>();

  /**
   * The instance variable stores the form which must be shown.
   * 
   * @see #getForm
   */
  private final UUID formuuid;

  public FormModel() throws EFapsException {
    super();
    CommandAbstract command = super.getCommand();
    if (command != null) {
      this.formuuid = command.getTargetForm().getUUID();
    } else {
      this.formuuid = null;
    }
  }

  public FormModel(PageParameters _parameters) {
    super(_parameters);
    CommandAbstract command = super.getCommand();
    if (command != null) {
      this.formuuid = command.getTargetForm().getUUID();
    } else {
      this.formuuid = null;
    }
  }

  public UUID getUUI() {
    return this.formuuid;
  }

  public Object getObject() {
    // TODO Auto-generated method stub
    return null;
  }

  public void setObject(Object object) {
    // TODO Auto-generated method stub

  }

  public void detach() {
    // TODO Auto-generated method stub

  }

  /**
   * This is the getter method for the instance variable {@link #values}.
   * 
   * @return value of instance variable {@link #values}
   * @see #values
   * @see #setValues
   */
  public List<FormRowModel> getValues() {
    return this.values;
  }

  public void execute() throws Exception {

    Form form = Form.get(this.formuuid);

    if (super.isCreateMode() || super.isSearchMode()) {

      Type type = null;
      if (super.isCreateMode()) {
        type = super.getCommand().getTargetCreateType();
      } else if (super.isSearchMode()) {
        List<EventDefinition> events =
            getCommand().getEvents(EventType.UI_TABLE_EVALUATE);
        for (EventDefinition eventDef : events) {
          String tmp = eventDef.getProperty("Types");
          if (tmp != null) {
            type = Type.get(tmp);
          }
        }
      }

      for (int i = 0; i < form.getFields().size(); i++) {
        Field field = (Field) form.getFields().get(i);

        // if (field.getExpression() != null) {
        // Attribute attr = type.getAttribute(field.getExpression());
        // if (attr != null) {
        // addFieldValue(field, attr, null, null);
        // }
        // } else if (field.getClassUI() != null) {
        // addFieldValue(field, null);
        // } else if (field.getGroupCount() > 0) {
        // addFieldValue(field, null);
        // if (super.getMaxGroupCount() < field.getGroupCount()) {
        // super.setMaxGroupCount(field.getGroupCount());
        // }
        // }

      }
    } else {

      SearchQuery query = new SearchQuery();
      query.setObject(super.getOid());

      for (Field field : form.getFields()) {
        if (field.getExpression() != null) {
          query.addSelect(field.getExpression());
        }
        if (field.getAlternateOID() != null) {
          query.addSelect(field.getAlternateOID());
        }
      }
      query.execute();
      String oid;
      String strValue;
      int rowgroupcount = 1;
      FormRowModel row = new FormRowModel();

      if (query.next()) {
        for (int i = 0; i < form.getFields().size(); i++) {
          Field field = (Field) form.getFields().get(i);

          if (field.getExpression() != null) {
            if (field.getAlternateOID() == null) {
              oid = (String) query.get("OID");
            } else {
              oid = (String) query.get(field.getAlternateOID());
            }
            Object value = query.get(field.getExpression());
            Attribute attr = query.getAttribute(field.getExpression());

            FieldValue fieldvalue =
                new FieldValue(new FieldDefinition("egal", field), attr, value,
                    new Instance(oid));

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

            String label;
            if (field.getLabel() != null) {
              label = field.getLabel();
            } else {
              label =
                  attr.getParent().getName() + "/" + attr.getName() + ".Label";
            }
            FormCellModel cell = new FormCellModel(oid, strValue, false, label);
            row.add(cell);
            rowgroupcount--;
            if (rowgroupcount < 1) {
              rowgroupcount = 1;
              this.values.add(row);
              row = new FormRowModel();
            }

          } else if (field.getGroupCount() > 0) {
            if (getMaxGroupCount() < field.getGroupCount()) {
              setMaxGroupCount(field.getGroupCount());
            }
            rowgroupcount = field.getGroupCount();
          }
        }
      }

      query.close();
    }
  }

  public class FormRowModel implements IClusterable {

    private static final long serialVersionUID = 1L;

    private final List<FormCellModel> values = new ArrayList<FormCellModel>();

    public void add(FormCellModel _cellmodel) {
      this.values.add(_cellmodel);
    }

    public List<FormCellModel> getValues() {
      return this.values;
    }

    public int getGroupCount() {
      return values.size();
    }
  }

  public class FormCellModel implements IClusterable {

    private static final long serialVersionUID = 1L;

    private String cellLabel;

    private String cellValue;

    private String oid;

    private boolean required;

    public FormCellModel(final String _oid, final String _cellValue,
                         final boolean _required, String _label) {
      this.oid = _oid;
      this.cellValue = _cellValue;
      this.required = _required;
      this.cellLabel = DBProperties.getProperty(_label);;
    }

    public boolean isRequired() {
      return this.required;
    }

    public String getCellLabel() {
      return this.cellLabel;
    }

    public String getCellValue() {
      return this.cellValue;
    }
  }
}
