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
 * Revision:        $Rev:1510 $
 * Last Changed:    $Date:2007-10-18 09:35:40 -0500 (Thu, 18 Oct 2007) $
 * Last Changed By: $Author:jmox $
 */

package org.efaps.ui.wicket.models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.wicket.IClusterable;
import org.apache.wicket.PageParameters;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.model.Model;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.datamodel.ui.FieldDefinition;
import org.efaps.admin.datamodel.ui.FieldValue;
import org.efaps.admin.event.EventDefinition;
import org.efaps.admin.event.EventType;
import org.efaps.admin.ui.AbstractCommand;
import org.efaps.admin.ui.Form;
import org.efaps.admin.ui.Image;
import org.efaps.admin.ui.field.Field;
import org.efaps.admin.ui.field.FieldGroup;
import org.efaps.admin.ui.field.FieldHeading;
import org.efaps.admin.ui.field.FieldTable;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.ui.wicket.models.cell.FormCellModel;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.util.EFapsException;

/**
 * @author jmo
 * @version $Id:FormModel.java 1510 2007-10-18 14:35:40Z jmox $
 */
public class FormModel extends AbstractModel {

  public enum ElementType {
    FORM,
    HEADING,
    TABLE
  }

  private static final long serialVersionUID = 3026168649146801622L;

  /**
   * The instance variable stores the different elemnts of the Form
   *
   * @see #getValues
   * @see #setValues
   */
  private final List<Element> elements = new ArrayList<Element>();

  /**
   * The instance variable stores the form which must be shown.
   *
   * @see #getForm
   */
  private UUID formUUID;

  private boolean fileUpload = false;

  public FormModel(PageParameters _parameters) {
    super(_parameters);
    final AbstractCommand command = super.getCommand();
    if (command == null) {
      this.formUUID = null;
    } else if (command.getTargetForm() != null) {
      this.formUUID = command.getTargetForm().getUUID();
    }

  }

  public UUID getUUID() {
    return this.formUUID;
  }

  @Override
  public void resetModel() {
    this.setInitialised(false);
    this.elements.clear();
  }

  public Form getForm() {
    return Form.get(this.formUUID);
  }

  public void execute() {

    String strValue;
    int rowgroupcount = 1;
    FormRowModel row = new FormRowModel();
    Type type = null;
    SearchQuery query = null;
    boolean queryhasresult = false;
    try {
      final Form form = Form.get(this.formUUID);

      if (super.isCreateMode() || super.isSearchMode()) {
        if (super.isCreateMode()) {
          type = super.getCommand().getTargetCreateType();
        } else if (super.isSearchMode()) {
          final List<EventDefinition> events =
              getCommand().getEvents(EventType.UI_TABLE_EVALUATE);
          for (final EventDefinition eventDef : events) {
            final String tmp = eventDef.getProperty("Types");
            if (tmp != null) {
              type = Type.get(tmp);
            }
          }
        }

      } else {
        query = new SearchQuery();
        query.setObject(super.getOid());

        for (final Field field : form.getFields()) {
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
        boolean addNew = true;
        FormElementModel formelement = null;
        for (int i = 0; i < form.getFields().size(); i++) {
          final Field field = form.getFields().get(i);
          Object value = null;
          Attribute attr = null;
          Instance instance = null;

          if (field instanceof FieldGroup) {
            final FieldGroup group = (FieldGroup) field;
            if (getMaxGroupCount() < group.getGroupCount()) {
              setMaxGroupCount(group.getGroupCount());
            }
            rowgroupcount = group.getGroupCount();
          } else if (field instanceof FieldTable) {
            if (!isCreateMode() && !isEditMode() && !isSearchMode()) {
              final FieldTableModel tablemodel =
                  new FieldTableModel(this.getCommandUUID(), this.getOid(),
                      ((FieldTable) field));
              this.elements.add(new Element(ElementType.TABLE, tablemodel));
              addNew = true;
            }
          } else if (field instanceof FieldHeading) {
            if (!isCreateMode() && !isEditMode() && !isSearchMode()) {
              this.elements.add(new Element(ElementType.HEADING,
                  new HeadingModel((FieldHeading) field)));
              addNew = true;
            }
          } else {
            if (addNew) {
              formelement = new FormElementModel();
              this.elements.add(new Element(ElementType.FORM, formelement));
              addNew = false;
            }

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

            if (super.isCreateMode() && field.isCreatable()) {
              strValue = fieldvalue.getCreateHtml();
            } else if (super.isEditMode() && field.isEditable()) {
              strValue = fieldvalue.getEditHtml();
            } else if (super.isSearchMode() && field.isSearchable()) {
              strValue = fieldvalue.getSearchHtml();
            } else {
              strValue = fieldvalue.getViewHtml();
            }
            if (strValue != null) {
              final String tmp = strValue.replaceAll(" ", "");
              if (tmp.toLowerCase().contains("type=\"file\"")) {
                this.fileUpload = true;
                final String script =
                    "onchange=\"document.getElementById(\'eFapsHiddenfield"
                        + field.getName()
                        + "\').value = this.value;\"/>";

                strValue = strValue.replace(">", script);

                strValue +=
                    "<input type=\"hidden\" name=\""
                        + field.getName()
                        + "\" value=\"\" id=\"eFapsHiddenfield"
                        + field.getName()
                        + "\"/>";

              }
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
            // if we have ViewMode and the field is not Viewable than we don't
            // add the Cell to the row
            if (!(super.isViewMode() && !field.isViewable())) {
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
            }
            rowgroupcount--;
            if (rowgroupcount < 1) {
              rowgroupcount = 1;
              if (row.getGroupCount() > 0) {
                formelement.addRowModel(row);
                row = new FormRowModel();
              }
            }
          }

        }
      }

      if (query != null) {
        query.close();
      }
    } catch (final EFapsException e) {
      throw new RestartResponseException(new ErrorPage(e));
    } catch (final Exception e) {
      throw new RestartResponseException(new ErrorPage(e));
    }
    super.setInitialised(true);
  }

  /**
   * This is the getter method for the instance variable {@link #formUUID}.
   *
   * @return value of instance variable {@link #formUUID}
   */
  public UUID getFormUUID() {
    return this.formUUID;
  }

  /**
   * This is the setter method for the instance variable {@link #formUUID}.
   *
   * @param formUUID
   *                the formUUID to set
   */
  public void setFormUUID(UUID formUUID) {
    this.formUUID = formUUID;
  }

  /**
   * This is the getter method for the instance variable {@link #elements}.
   *
   * @return value of instance variable {@link #elements}
   */
  public List<Element> getElements() {
    return this.elements;
  }

  /**
   * This is the getter method for the instance variable {@link #fileUpload}.
   *
   * @return value of instance variable {@link #fileUpload}
   */
  public boolean isFileUpload() {
    return this.fileUpload;
  }

  /**
   * This is the setter method for the instance variable {@link #fileUpload}.
   *
   * @param fileUpload
   *                the fileUpload to set
   */
  public void setFileUpload(boolean fileUpload) {
    this.fileUpload = fileUpload;
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

  public class FormElementModel extends Model {

    private static final long serialVersionUID = 1L;

    private final List<FormRowModel> rowModels = new ArrayList<FormRowModel>();

    public void addRowModel(final FormRowModel _rowmodel) {
      this.rowModels.add(_rowmodel);
    }

    /**
     * This is the getter method for the instance variable {@link #rowModels}.
     *
     * @return value of instance variable {@link #rowModels}
     */
    public List<FormRowModel> getRowModels() {
      return this.rowModels;
    }

  }

  public class Element implements IClusterable {

    private static final long serialVersionUID = 1L;

    private final ElementType type;

    private final Model model;

    public Element(final ElementType _type, final Model _model) {
      this.type = _type;
      this.model = _model;
    }

    /**
     * This is the getter method for the instance variable {@link #type}.
     *
     * @return value of instance variable {@link #type}
     */
    public ElementType getType() {
      return this.type;
    }

    /**
     * This is the getter method for the instance variable {@link #model}.
     *
     * @return value of instance variable {@link #model}
     */
    public Model getModel() {
      return this.model;
    }

  }

}
