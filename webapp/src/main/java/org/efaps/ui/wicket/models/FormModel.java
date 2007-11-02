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
import java.util.List;
import java.util.UUID;

import org.apache.wicket.PageParameters;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.model.Model;

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
import org.efaps.admin.ui.Image;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.util.EFapsException;

/**
 * @author jmo
 * @version $Id:FormModel.java 1510 2007-10-18 14:35:40Z jmox $
 */
public class FormModel extends AbstractModel {

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
  private UUID formuuid;

  public FormModel(PageParameters _parameters) {
    super(_parameters);
    final CommandAbstract command = super.getCommand();
    if (command == null) {
      this.formuuid = null;
    } else if (command.getTargetForm() != null) {
      this.formuuid = command.getTargetForm().getUUID();
    }
  }

  public UUID getUUID() {
    return this.formuuid;
  }

  @Override
  public void resetModel() {
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
    return Form.get(this.formuuid);
  }

  public void execute() {

    String strValue;
    int rowgroupcount = 1;
    FormRowModel row = new FormRowModel();
    Type type = null;
    SearchQuery query = null;
    boolean queryhasresult = false;
    try {
      final Form form = Form.get(this.formuuid);

      if (super.isCreateMode() || super.isSearchMode()) {
        if (super.isCreateMode()) {
          type = super.getCommand().getTargetCreateType();
        } else if (super.isSearchMode()) {
          final List<EventDefinition> events =
              getCommand().getEvents(EventType.UI_TABLE_EVALUATE);
          for (EventDefinition eventDef : events) {
            final String tmp = eventDef.getProperty("Types");
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
          final Field field = form.getFields().get(i);
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
            if (field.getLabel() == null) {
              label =
                  attr.getParent().getName() + "/" + attr.getName() + ".Label";
            } else {
              label = field.getLabel();
            }

            final FieldValue fieldvalue =
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
            String oid = null;
            String icon = field.getIcon();
            if (instance != null) {
              oid = instance.getOid();
              if (field.isShowTypeIcon() && instance.getType() != null) {
                final Image image = Image.getTypeIcon(instance.getType());
                if (image != null) {
                  icon = image.getUrl();
                }
              }
            }

            if (queryhasresult) {
              final FormCellModel cell =
                  new FormCellModel(field, oid, strValue, icon, super
                      .isEditMode() ? field.isRequired() : false, label);
              row.add(cell);
            } else if (strValue != null && !strValue.equals("")) {
              final FormCellModel cell =
                  new FormCellModel(field, oid, strValue, icon, super
                      .isSearchMode() ? false : field.isRequired(), label);
              if (super.isSearchMode()) {
                cell.setReference(null);
              }
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
    } catch (EFapsException e) {
      throw new RestartResponseException(new ErrorPage(e));
    } catch (Exception e) {
      throw new RestartResponseException(new ErrorPage(e));
    }
    super.setInitialised(true);
  }

  public void setFormUUID(final UUID _uuid) {
    this.formuuid = _uuid;
  }

  public class FormRowModel extends Model {

    private static final long serialVersionUID = 1L;

    private final List<FormCellModel> values = new ArrayList<FormCellModel>();

    public void add(final FormCellModel _cellmodel) {
      this.values.add(_cellmodel);
    }

    public List<FormCellModel> getValues() {
      return this.values;
    }

    public int getGroupCount() {
      return this.values.size();
    }
  }

  public class FormCellModel extends CellModel {

    private static final long serialVersionUID = 1L;

    private final String cellLabel;

    private final boolean required;

    private final String name;

    public FormCellModel(final Field _field, final String _oid,
                         final String _cellValue, final String _icon,
                         final boolean _required, final String _label) {
      super(_field, _oid, _cellValue, _icon);
      this.required = _required;
      this.cellLabel = DBProperties.getProperty(_label);
      this.name = _field.getName();
    }

    public boolean isRequired() {
      return this.required;
    }

    public String getCellLabel() {
      return this.cellLabel;
    }

    /**
     * This is the getter method for the instance variable {@link #name}.
     * 
     * @return value of instance variable {@link #name}
     */

    public String getName() {
      return this.name;
    }

  }
}
