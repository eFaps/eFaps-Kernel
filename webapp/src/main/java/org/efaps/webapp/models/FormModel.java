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
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.ui.CommandAbstract;
import org.efaps.admin.ui.Field;
import org.efaps.admin.ui.Form;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;

/**
 * @author jmo
 * @version $Id$
 */
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

  public UUID getUUID() {
    return this.formuuid;
  }

  public Object getObject() {
    return null;
  }

  public void setObject(Object object) {
  }

  public void detach() {
    this.setInitialised(false);
    this.values.clear();
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

  public Form getForm() {
    return (Form.get(this.formuuid));
  }

  public void execute() throws Exception {

    Form form = Form.get(this.formuuid);

    String strValue;
    int rowgroupcount = 1;
    FormRowModel row = new FormRowModel();
    Type type = null;
    SearchQuery query = null;
    boolean queryhasresult = false;

    if (super.isCreateMode() || super.isSearchMode()) {
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

    } else {
      query = new SearchQuery();
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
      if (query.next()) {
        queryhasresult = true;
      }
    }
    if (queryhasresult || type != null) {
      for (int i = 0; i < form.getFields().size(); i++) {
        Field field = (Field) form.getFields().get(i);
        Object value = null;
        Attribute attr;
        Instance instance = null;

        if (field.getExpression() != null) {
          if (queryhasresult) {
            if (field.getAlternateOID() == null) {
              instance = new Instance((String) query.get("OID"));
            } else {
              instance =
                  new Instance((String) query.get(field.getAlternateOID()));
            }
            value = query.get(field.getExpression());
            attr = query.getAttribute(field.getExpression());

          } else {
            attr = type.getAttribute(field.getExpression());
          }

          String label;
          if (field.getLabel() != null) {
            label = field.getLabel();
          } else {
            label =
                attr.getParent().getName() + "/" + attr.getName() + ".Label";
          }

          FieldValue fieldvalue =
              new FieldValue(new FieldDefinition("egal", field), attr, value,
                  instance);

          if (super.isCreateMode() && field.isEditable()) {
            strValue = fieldvalue.getCreateHtml();
          } else if (super.isEditMode() && field.isEditable()) {
            strValue = fieldvalue.getEditHtml();
          } else if (super.isSearchMode() && field.isSearchable()) {
            strValue = fieldvalue.getSearchHtml();
          } else {
            strValue = fieldvalue.getViewHtml();
          }
          if (queryhasresult) {
            FormCellModel cell =
                new FormCellModel(instance, strValue,
                    super.isEditMode() ? field.isRequired() : false, label);
            row.add(cell);
          } else if (strValue != null && !strValue.equals("")) {
            FormCellModel cell =
                new FormCellModel(instance, strValue, field.isRequired(), label);
            row.add(cell);
          }

          rowgroupcount--;
          if (rowgroupcount < 1) {
            rowgroupcount = 1;
            if (row.getGroupCount() > 0) {
              this.values.add(row);
              row = new FormRowModel();
            }
          }

        } else if (field.getGroupCount() > 0) {
          if (getMaxGroupCount() < field.getGroupCount()) {
            setMaxGroupCount(field.getGroupCount());
          }
          rowgroupcount = field.getGroupCount();
        }
      }
    }
    if (query != null) {
      query.close();
    }
  }

  public void updateDB() {
    CommandAbstract command = super.getCommand();
    try {

      if (command.hasEvents(EventType.UI_COMMAND_EXECUTE)) {
        if (super.getOid() != null) {
          command.executeEvents(EventType.UI_COMMAND_EXECUTE,
              ParameterValues.INSTANCE, new Instance(super.getOid()));
        } else {
          command.executeEvents(EventType.UI_COMMAND_EXECUTE);
        }
      }
    } catch (EFapsException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
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

    private final String cellLabel;

    private final String cellValue;

    private final String oid;

    private boolean required;

    public FormCellModel(final Instance _instance, final String _cellValue,
                         final boolean _required, String _label) {
      if (_instance != null) {
        this.oid = _instance.getOid();
      } else {
        this.oid = null;
      }
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

    public String getOid() {
      return this.oid;
    }
  }
}
