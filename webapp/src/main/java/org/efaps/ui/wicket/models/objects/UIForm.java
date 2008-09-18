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
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.ui.wicket.models.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.wicket.IClusterable;
import org.apache.wicket.PageParameters;
import org.apache.wicket.RestartResponseException;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.AttributeSet;
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
import org.efaps.admin.ui.field.FieldSet;
import org.efaps.admin.ui.field.FieldTable;
import org.efaps.db.Instance;
import org.efaps.db.ListQuery;
import org.efaps.ui.wicket.models.cell.UIFormCell;
import org.efaps.ui.wicket.models.cell.UIFormCellSet;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.util.EFapsException;


/**
 * TODO description
 *
 * @author jmox
 * @version $Id$
 */
public class UIForm extends AbstractUIObject {

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


  private final Map<String,String[]> newValues = new HashMap<String,String[]>();

  public UIForm(final PageParameters _parameters) {
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

  private void createForm() throws EFapsException{
    int rowgroupcount = 1;
    FormRow row = new FormRow();
    final Form form = Form.get(this.formUUID);

    Type type = null;
    if (isCreateMode()) {
      type = getCommand().getTargetCreateType();
    } else {
      final List<EventDefinition> events = getCommand().getEvents(
          EventType.UI_TABLE_EVALUATE);
      for (final EventDefinition eventDef : events) {
        final String tmp = eventDef.getProperty("Types");
        if (tmp != null) {
          type = Type.get(tmp);
        }
      }
    }
    if (type!=null) {
      FormElement formelement = new FormElement();
      this.elements.add(new Element(ElementType.FORM, formelement));

      for (final Field field : form.getFields()) {
        if (field instanceof FieldGroup) {
          final FieldGroup group = (FieldGroup) field;
          if (getMaxGroupCount() < group.getGroupCount()) {
            setMaxGroupCount(group.getGroupCount());
          }
          rowgroupcount = group.getGroupCount();
        } else if (field instanceof FieldTable ){

        } else if (field instanceof FieldHeading && field.isCreatable()){
          this.elements.add(new Element(ElementType.HEADING,
              new UIHeading((FieldHeading) field)));
          formelement = new FormElement();
          this.elements.add(new Element(ElementType.FORM, formelement));
        } else if (field.isCreatable() && isCreateMode()
                  || field.isSearchable() && isSearchMode()){

          final Attribute attr = type.getAttribute(field.getExpression());

          String label;
          if (field.getLabel() != null) {
            label = field.getLabel();
          } else if (attr != null)  {
            label = attr.getParent().getName() + "/" + attr.getName() + ".Label";
          } else {
            label = "Unknown";
          }
          final Instance fieldInstance = getCallInstance();
          final FieldValue fieldvalue = new FieldValue(new FieldDefinition(
              "egal", field), attr, "", fieldInstance);

          String strValue = null;
          if (isCreateMode()) {
            strValue = fieldvalue.getCreateHtml(getCallInstance());
          } else if (isSearchMode()) {
            strValue = fieldvalue.getSearchHtml(getCallInstance());
          }
          final UIFormCell cell =
            new UIFormCell(field, null, strValue, null, field.isRequired(), label);
          if (isSearchMode()) {
            cell.setReference(null);
          }

          row.add(cell);
        }

        rowgroupcount--;
        if (rowgroupcount < 1) {
          rowgroupcount = 1;
          if (row.getGroupCount() > 0) {
            formelement.addRowModel(row);
            row = new FormRow();
          }
        }
      }
    }
  }

  public void execute() {
    try {
      if (isCreateMode() || isSearchMode()) {
        createForm();
      } else {
        int rowgroupcount = 1;
        FormRow row = new FormRow();
        ListQuery query = null;

        final Form form = Form.get(this.formUUID);

        final List<Instance> instances = new ArrayList<Instance>();
        instances.add(getCallInstance());
        query = new ListQuery(instances);

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
          FormElement formelement = null;
          boolean addNew = true;
          for (final Field field : form.getFields()) {
            if (field instanceof FieldGroup) {
              final FieldGroup group = (FieldGroup) field;
              if (getMaxGroupCount() < group.getGroupCount()) {
                setMaxGroupCount(group.getGroupCount());
              }
              rowgroupcount = group.getGroupCount();
            } else if (field instanceof FieldTable) {
              if (!isEditMode()) {
                final UIFieldTable tablemodel = new UIFieldTable(this
                    .getCommandUUID(), this.getOid(), ((FieldTable) field));
                this.elements.add(new Element(ElementType.TABLE, tablemodel));
                addNew = true;
              }
            } else if (field instanceof FieldHeading) {
              if (!isEditMode()) {
                this.elements.add(new Element(ElementType.HEADING,
                    new UIHeading((FieldHeading) field)));
                addNew = true;
              }
            } else if (!(isViewMode() && !field.isViewable())) {
              if (addNew) {
                formelement = new FormElement();
                this.elements.add(new Element(ElementType.FORM, formelement));
                addNew = false;
              }
              Attribute attr = null;
              if (field.getExpression()!=null) {
                attr = query.getAttribute(field.getExpression());
              }
              // evaluate the label of the field
              String label;
              if (field.getLabel() != null) {
                label = field.getLabel();
              } else if (attr != null) {
                label = attr.getParent().getName() + "/" + attr.getName()
                    + ".Label";
              } else {
                label = "Unknown";
              }

              String oid = null;
              Instance fieldInstance;
              if (field.getAlternateOID() != null) {
                fieldInstance = new Instance((String) query.get(field
                    .getAlternateOID()));
              } else {
                fieldInstance = getCallInstance();
              }
              if (fieldInstance != null) {
                oid = fieldInstance.getOid();
              }
              if (field instanceof FieldSet) {
                final AttributeSet set = AttributeSet.find(getCallInstance()
                    .getType().getName(), field.getExpression());
                final Map<?, ?> tmp = (Map<?, ?>) query.get(field
                    .getExpression());
                final List<Instance> fieldins = new ArrayList<Instance>();
                if (tmp != null) {
                  fieldins.addAll(query.getInstances(field.getExpression()));
                }
                int y = 0;
                boolean add = true;
                final UIFormCellSet cellset = new UIFormCellSet(field, oid, "",
                    "", isEditMode() ? field.isRequired() : false, label,
                    isEditMode());
                final Iterator<Instance> iter = fieldins.iterator();
                while (add) {
                  int x = 0;
                  if (iter.hasNext()) {
                    cellset.addInstance(y, iter.next());
                  }
                  for (final String attrName : ((FieldSet) field).getOrder()) {
                    final Attribute child = set.getAttribute(attrName);
                    if (isEditMode()) {
                      final FieldValue fieldvalue = new FieldValue(
                          new FieldDefinition("egal", field), child, "",
                          getCallInstance());
                      cellset.addDefiniton(x, fieldvalue
                          .getCreateHtml(getCallInstance()));
                    }
                    if (tmp == null) {
                      add = false;
                    } else {
                      final List<?> tmplist = (List<?>) tmp
                          .get(child.getName());
                      if (y < tmplist.size()) {
                        final Object value = tmplist.get(y);
                        final FieldValue fieldvalue = new FieldValue(
                            new FieldDefinition("egal", field), child, value,
                            getCallInstance());
                        String tmpStr = null;
                        if (isEditMode() && field.isEditable()) {
                          tmpStr = fieldvalue.getEditHtml(getCallInstance());
                        } else if (field.isViewable()) {
                          tmpStr = fieldvalue.getViewHtml(getCallInstance());
                        }
                        cellset.add(x, y, tmpStr);
                      } else {
                        add = false;
                      }
                    }
                    x++;
                  }
                  y++;
                }

                // we only add multiline if we have a value or we are in
                // editmodus
                if (tmp != null || isEditMode()) {
                  row.add(cellset);
                }
              } else {
                Object value = null;
                if (field.getExpression()!=null){
                  value = query.get(field.getExpression());
                }

                final FieldValue fieldvalue = new FieldValue(
                    new FieldDefinition("egal", field), attr, value,
                    fieldInstance);

                String strValue = null;
                if (isEditMode() && field.isEditable()) {
                  strValue = fieldvalue.getEditHtml(getCallInstance());
                } else if (field.isViewable()) {
                  strValue = fieldvalue.getViewHtml(getCallInstance());
                }
                if (strValue != null && !this.fileUpload) {
                  final String tmp = strValue.replaceAll(" ", "");
                  if (tmp.toLowerCase().contains("type=\"file\"")) {
                    this.fileUpload = true;
                  }
                }

                String icon = field.getIcon();
                if (fieldInstance != null) {
                  oid = fieldInstance.getOid();
                  if (field.isShowTypeIcon() && fieldInstance.getType() != null) {
                    final Image image = Image.getTypeIcon(fieldInstance
                        .getType());
                    if (image != null) {
                      icon = image.getUrl();
                    }
                  }
                  final UIFormCell cell = new UIFormCell(field, oid, strValue,
                      icon, isEditMode() ? field.isRequired() : false, label);
                  row.add(cell);
                }
              }
            }
            rowgroupcount--;
            if (rowgroupcount < 1) {
              rowgroupcount = 1;
              if (row.getGroupCount() > 0) {
                formelement.addRowModel(row);
                row = new FormRow();
              }
            }
          }
        }
      }

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
  public void setFormUUID(final UUID formUUID) {
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
  public void setFileUpload(final boolean fileUpload) {
    this.fileUpload = fileUpload;
  }




  public Map<String, String[]> getNewValues() {
    return this.newValues;
  }




  public class FormRow  implements IClusterable{

    private static final long serialVersionUID = 1L;

    private final List<UIFormCell> values = new ArrayList<UIFormCell>();

    public void add(final UIFormCell _cellmodel) {
      this.values.add(_cellmodel);
    }

    public List<UIFormCell> getValues() {
      return this.values;
    }

    public int getGroupCount() {
      return this.values.size();
    }
  }

  public class FormElement implements IFormElement,IClusterable {

    private static final long serialVersionUID = 1L;

    private final List<FormRow> rowModels = new ArrayList<FormRow>();

    public void addRowModel(final FormRow _rowmodel) {
      this.rowModels.add(_rowmodel);
    }

    /**
     * This is the getter method for the instance variable {@link #rowModels}.
     *
     * @return value of instance variable {@link #rowModels}
     */
    public List<FormRow> getRowModels() {
      return this.rowModels;
    }

  }

  public class Element implements IClusterable {

    private static final long serialVersionUID = 1L;

    private final ElementType type;

    private final IFormElement element;

    public Element(final ElementType _type, final IFormElement _model) {
      this.type = _type;
      this.element = _model;
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
     * This is the getter method for the instance variable {@link #element}.
     *
     * @return value of instance variable {@link #element}
     */
    public IFormElement getElement() {
      return this.element;
    }

  }


}
