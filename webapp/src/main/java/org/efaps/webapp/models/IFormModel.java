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

import org.apache.wicket.PageParameters;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.AttributeTypeInterface;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.datamodel.ui.FieldDefinition;
import org.efaps.admin.datamodel.ui.FieldValue;
import org.efaps.admin.datamodel.ui.UIInterface;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.event.EventDefinition;
import org.efaps.admin.event.EventType;
import org.efaps.admin.ui.Field;
import org.efaps.admin.ui.Form;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;

public class IFormModel extends IModelAbstract {
  private static final long serialVersionUID = 3026168649146801622L;

  /**
   * The instance variable stores the result list of the execution of the query.
   * 
   * @see #getValues
   * @see #setValues
   */
  private final List<FieldValue> values = new ArrayList<FieldValue>();

  /**
   * The instance variable stores the form which must be shown.
   * 
   * @see #getForm
   */
  private final Form form;

  public IFormModel() throws EFapsException {
    super();
    // set target form
    if (getCommand() != null) {
      this.form = getCommand().getTargetForm();
    } else {
      this.form = null;
    }
  }

  public IFormModel(PageParameters _parameters) {
    super(_parameters);
    if (getCommand() != null) {
      this.form = getCommand().getTargetForm();
    } else {
      this.form = null;
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
    // TODO Auto-generated method stub

  }

  /**
   * This is the getter method for the instance variable {@link #values}.
   * 
   * @return value of instance variable {@link #values}
   * @see #values
   * @see #setValues
   */
  public List<FieldValue> getValues() {
    return this.values;
  }

  public void execute() throws Exception {

    if (super.isCreateMode() || super.isSearchMode()) {
      this.values.add(null);

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

      for (int i = 0; i < this.form.getFields().size(); i++) {
        Field field = (Field) this.form.getFields().get(i);

        if (field.getExpression() != null) {
          Attribute attr = type.getAttribute(field.getExpression());
          if (attr != null) {
            addFieldValue(field, attr, null, null);
          }
        } else if (field.getClassUI() != null) {
          addFieldValue(field, null);
        } else if (field.getGroupCount() > 0) {
          addFieldValue(field, null);
          if (super.getMaxGroupCount() < field.getGroupCount()) {
            super.setMaxGroupCount(field.getGroupCount());
          }
        }

      }
    } else {
      Instance instance = super.getInstance();
      SearchQuery query = new SearchQuery();
      query.setObject(instance);

      for (Field field : this.form.getFields()) {
        if (field.getExpression() != null) {
          query.addSelect(field.getExpression());
        }
        if (field.getAlternateOID() != null) {
          query.addSelect(field.getAlternateOID());
        }
      }
      query.execute();

      if (query.next()) {
        // getValues().add(query.getInstance(context, instance.getType()));
        addFieldValue(null, null, null, null, new Instance((String) query
            .get("OID")));

        for (int i = 0; i < this.form.getFields().size(); i++) {
          Field field = (Field) this.form.getFields().get(i);

          if (field.getExpression() != null) {
            Instance fldInstance;
            if (field.getAlternateOID() == null) {
              fldInstance = new Instance((String) query.get("OID"));
            } else {
              fldInstance =
                  new Instance((String) query.get(field.getAlternateOID()));
            }
            addFieldValue(field, query.getAttribute(field.getExpression()),
                query.get(field.getExpression()), fldInstance);
          } else if (field.getGroupCount() > 0) {
            addFieldValue(field, instance);
            if (getMaxGroupCount() < field.getGroupCount()) {
              setMaxGroupCount(field.getGroupCount());
            }
          }
        }
      }

      query.close();
    }
  }

  /**
   * Adds a field value to the list of values.
   * 
   * @see #addFieldValue(String,Field,UIInterface,Object,Instance)
   */
  public void addFieldValue(Field _field, Attribute _attr, Object _value,
      Instance _instance) {
    String label = null;
    if (_field.getLabel() != null) {
      label = _field.getLabel();
    } else {
      label = _attr.getParent().getName() + "/" + _attr.getName() + ".Label";
    }
    label = DBProperties.getProperty(label);

    this.addFieldValue(label, _field, _attr, _value, _instance);
  }

  /**
   * Adds a field value to the list of values.
   * 
   * @see #addFieldValue(String,Field,UIInterface,Object,Instance)
   */
  public void addFieldValue(Field _field, Instance _instance) {
    this.addFieldValue(_field.getLabel(), _field, null, null, _instance);
  }

  /**
   * The instance method adds a new attribute value (from instance
   * {@link AttributeTypeInterface}) to the values.
   * 
   * @see #values
   */
  public void addFieldValue(final String _label, final Field _field,
      final Attribute _attribute, final Object _value, final Instance _instance) {
    this.values.add(new FieldValue(new FieldDefinition(_label, _field),
        _attribute, _value, _instance));
  }

}
