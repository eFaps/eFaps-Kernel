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

package org.efaps.beans;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.fileupload.FileItem;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.AttributeTypeInterface;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.datamodel.attributetype.AbstractFileType;
import org.efaps.admin.datamodel.ui.FieldDefinition;
import org.efaps.admin.datamodel.ui.FieldValue;
import org.efaps.admin.datamodel.ui.UIInterface;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.event.EventDefinition;
import org.efaps.admin.event.EventType;
import org.efaps.admin.ui.CommandAbstract;
import org.efaps.admin.ui.Field;
import org.efaps.admin.ui.Form;
import org.efaps.beans.form.FormFieldUpdateInterface;
import org.efaps.db.Checkin;
import org.efaps.db.Context;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.db.Update;
import org.efaps.util.EFapsException;

/**
 * 
 * @author tmo
 * @version $Id: FormBean.java 675 2007-02-14 20:56:25 +0000 (Wed, 14 Feb 2007)
 *          jmo $
 * @todo description
 */
public class FormBean extends AbstractCollectionBean {

  /////////////////////////////////////////////////////////////////////////////
  // instance variables

  /**
   * The instance variable stores the form which must be shown.
   * 
   * @see #getForm
   */
  private final Form form;

  /**
   * The instance variable stores the result list of the execution of the
   * query.
   *
   * @see #getValues
   * @see #setValues
   */
  private final List<FieldValue> values = new ArrayList<FieldValue>();

  /**
   * The instance variable stores the instance for the unique key.
   * 
   * @see #getUkInstance
   * @see #setUkInstance
   * @see #ukTitle
   * @see #ukMode
   */
  private Instance ukInstance = null;

  /**
   * The instance variable stores the title if an object is found in create
   * mode.
   * 
   * @see #getUkTitle
   * @see #setUkTitle
   * @see #ukInstance
   * @see #ukMode
   */
  private String   ukTitle    = null;

  /**
   * The instance variable stores if in create mode for a type a given unique
   * key is found.
   * 
   * @see #isUkMode
   * @see #setUkMode
   * @see #ukInstance
   * @see #ukTitle
   */
  private boolean  ukMode     = false;

  /////////////////////////////////////////////////////////////////////////////
  // constructors / descructors

  /**
   * The command name is extracted from the form values.
   */
  public FormBean() throws EFapsException {
    super();
System.out.println("FormBean.constructor");

    // set target form
    if (getCommand() != null) {
      this.form = getCommand().getTargetForm();
    } else  {
      this.form = null;
    }
  }

  public void finalize() {
    System.out.println("FormBean.destructor");
  }

  /////////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * @todo search is not correct defined
   */
  public void execute() throws Exception {
    
    if (isCreateMode() || isSearchMode()) {
      getValues().add(null);

      Type type = null;
      if (isCreateMode())  {
        type = getCommand().getTargetCreateType();
      } else if (isSearchMode())  {
List<EventDefinition> events = getCommand().getEvents(EventType.UI_TABLE_EVALUATE);
for (EventDefinition eventDef : events)  {
  String tmp = eventDef.getProperty("Types");
  if (tmp != null)  {
    type = Type.get(tmp);
  }
}
      }

      for (int i = 0; i < getForm().getFields().size(); i++) {
        Field field = (Field) getForm().getFields().get(i);

        if (field.getExpression() != null) {
          Attribute attr = type.getAttribute(field.getExpression());
          if (attr != null) {
            addFieldValue(field, attr, null, null);
          }
        } else if (field.getClassUI() != null) {
          addFieldValue(field, null);
        } else if (field.getGroupCount() > 0) {
          addFieldValue(field, null);
          if (getMaxGroupCount() < field.getGroupCount()) {
            setMaxGroupCount(field.getGroupCount());
          }
        }

      }
    } else {
      Instance instance = getInstance();
      if (ukInstance != null) {
        instance = ukInstance;
      }
      SearchQuery query = new SearchQuery();
      query.setObject(instance);

      for (Field field : getForm().getFields()) {
        if (field.getExpression() != null)  {
          query.addSelect(field.getExpression());
        }
        if (field.getAlternateOID() != null) {
          query.addSelect(field.getAlternateOID());
        }
      }
      query.execute();

      if (query.next()) {
//        getValues().add(query.getInstance(context, instance.getType()));
addFieldValue(null, null, null, null, new Instance((String)query.get("OID")));

        for (int i = 0; i < getForm().getFields().size(); i++) {
          Field field = (Field) getForm().getFields().get(i);

          if (field.getExpression() != null) {
            Instance fldInstance;
            if (field.getAlternateOID() == null) {
              fldInstance = new Instance((String)query.get("OID"));
            } else  {
              fldInstance = new Instance((String)query.get(field.getAlternateOID()));
            }
            addFieldValue(field,
                          query.getAttribute(field.getExpression()),
                          query.get(field.getExpression()),
                          fldInstance);
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
   * The instance method is called to process the modifcation of a form.
   * 
   * @see #processCreate
   * @see #processUpdate
   */
  public void process() throws Exception {
    try {
      if (isCreateMode()) {
        processCreate(Context.getThreadContext());
      } else {
        processUpdate(Context.getThreadContext());
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }
  }

  /**
   * The instance method process the create of a new object.
   * 
   * @param _context
   *          context for this request
   * @todo maybe an axception must be thrown? see TODO comment
   */
  protected void processCreate(Context _context) throws Exception {
    Insert insert = new Insert(getCommand().getTargetCreateType());
    for (Field field : getForm().getFields()) {
      if (field.getExpression() != null
          && (field.isCreatable() || field.isHidden())) {
        Attribute attr = getCommand().getTargetCreateType().getAttribute(
            field.getExpression());
        if (attr != null
            && !AbstractFileType.class.isAssignableFrom(attr.getAttributeType()
                .getClassRepr())) {
          String value = getParameter(field.getName());
          insert.add(attr, value);
        }
      }
    }
    if (getCommand().getTargetConnectAttribute() != null) {
      Instance instance = new Instance(getParameter("oid"));
      insert.add(getCommand().getTargetConnectAttribute(), ""
          + instance.getId());
    }
    insert.execute();
    setInstance(new Instance(getCommand().getTargetCreateType(), insert.getId()));

    // "TargetConnectChildAttribute"
    // "TargetConnectParentAttribute"
    // "TargetConnectType"
    if (getCommand().getProperty("TargetConnectType") != null) {
      Instance parent = new Instance(getParameter("oid"));

      Insert connect = new Insert(getCommand().getProperty("TargetConnectType"));
      connect.add(getCommand().getProperty("TargetConnectParentAttribute"), ""
          + parent.getId());
      connect.add(getCommand().getProperty("TargetConnectChildAttribute"), ""
          + insert.getId());
      connect.execute();
    }

    for (Field field : getForm().getFields()) {
      if (field.getExpression() == null && field.isCreatable()) {
        FileItem fileItem = getFileParameter(field.getName());
        String updateClassName = field.getProperty("ClassNameUpdate");

        if (updateClassName != null) {
          Class<FormFieldUpdateInterface> updateClass = (Class<FormFieldUpdateInterface>) Class
              .forName(updateClassName);
          FormFieldUpdateInterface fieldUpdate = updateClass.newInstance();
          fieldUpdate.update(_context, this, field);
        } else if (fileItem != null) {
          System.out.println("-----------checkin ------" + fileItem);
          Checkin checkin = new Checkin(getInstance());
          checkin.execute(fileItem.getName(), fileItem.getInputStream(),
              (int) fileItem.getSize());
        }
        // TODO: ev. exception?
      }
    }
  }

  /**
   * The instance method process the update of current selected object.
   * 
   * @param _context
   *          context for this request
   * @todo maybe an axception must be thrown? see TODO comment
   */
  protected void processUpdate(Context _context) throws Exception {
    Update update = new Update(getInstance());
    for (Field field : getForm().getFields()) {
      if (field.getExpression() != null && field.isEditable()) {
        Attribute attr = getInstance().getType().getAttribute(
            field.getExpression());
        if (attr != null
            && !AbstractFileType.class.isAssignableFrom(attr.getAttributeType()
                .getClassRepr())) {
          System.out.println("field.getName()=" + field.getName());
          update.add(attr, getParameter(field.getName()).replace(',', '.'));
        }
      }
    }

    for (Field field : getForm().getFields()) {
      if (field.getExpression() == null && field.isEditable()) {
        FileItem fileItem = getFileParameter(field.getName());
        String updateClassName = field.getProperty("ClassNameUpdate");

        if (updateClassName != null) {
          Class<FormFieldUpdateInterface> updateClass = (Class<FormFieldUpdateInterface>) Class
              .forName(updateClassName);
          FormFieldUpdateInterface fieldUpdate = updateClass.newInstance();
          fieldUpdate.update(_context, this, field);
        } else if (fileItem != null) {
          Checkin checkin = new Checkin(getInstance());
          checkin.execute(fileItem.getName(), fileItem.getInputStream(),
              (int) fileItem.getSize());
        }
        // TODO: ev. exception?
      }
    }

    update.execute();
  }

  // ///////////////////////////////////////////////////////////////////////////

  /**
   * The instance method test for given user input parameter and unique keys of
   * the create type one (!) instance exists. If one instance exists, this
   * instance is stored in {@link #ukInstance}.
   * 
   * @see #ukInstance
   */
  public void ukTest() throws Exception {
/*    Context context = Context.getThreadContext();
    Map map = new HashMap();
    for (int i = 0; i < getForm().getFields().size(); i++) {
*/
      /*
       * Field field = (Field) getForm().getFields().get(i); if
       * (field.getAttribute()!=null &&
       * field.getAttribute().getUniqueKeys()!=null) {
       * map.put(field.getAttribute(),
       * getRequest().getParameter(field.getName())); }
       */
/*    }

    Type type = getCommand().getTargetCreateType();
    for (Iterator ukIter = type.getUniqueKeys().iterator(); ukIter.hasNext();) {
      UniqueKey uniqueKey = (UniqueKey) ukIter.next();
      SearchQuery query = new SearchQuery();
      for (Iterator attrIter = uniqueKey.getAttributes().iterator(); attrIter
          .hasNext();) {
        Attribute attr = (Attribute) attrIter.next();
        String value = (String) map.get(attr);
        if (value == null) {
          query = null;
          break;
        }
        query.addWhereAttrEqValue(attr, value);
      }
      if (query != null) {
        query.execute();
        if (query.next()) {
          Instance instance = query.getInstance(context, type);
          if (!query.next()) {
            setUkInstance(instance);
            break;
          }
        }
      }
    }
*/
  }

  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Sets the parameters values for file and form. Also the command name is
   * extracted from the form values.
   * 
   * @param _parameters
   *          new value for Parameters variable {@link AbstractBean.parameters}
   * @param _fileParameters
   *          new value for Parameters variable
   *          {@link AbstractBean.fileParameters}
   * @see AbstractBean.parameters
   * @see AbstractBean.fileParameters
   * @see AbstractBean.setParameters
   * @see #setCommandName
   */
  /*
   * public void setParameters(Map<String,String[]> _parameters, Map<String,FileItem>
   * _fileParameters) throws Exception { super.setParameters(_parameters,
   * _fileParameters);
   * 
   * String cmdName = getParameter("command"); if (cmdName==null ||
   * cmdName.length()==0 || "undefined".equals(cmdName)) { cmdName =
   * getParameter("formCommand"); } setCommandName(cmdName); }
   */
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
   
    addFieldValue(label, _field, _attr, _value, _instance);
  }

  /**
   * Adds a field value to the list of values.
   * 
   * @see #addFieldValue(String,Field,UIInterface,Object,Instance)
   */
  public void addFieldValue(Field _field, Instance _instance) {
    addFieldValue(_field.getLabel(), _field, null, null,
        _instance);
  }

  /**
   * The instance method adds a new attribute value (from instance
   * {@link AttributeTypeInterface}) to the values.
   * 
   * @see #values
   */
  public void addFieldValue(final String _label,
                            final Field _field,
                            final Attribute _attribute,
                            final Object _value,
                            final Instance _instance) {
    getValues().add(new FieldValue(new FieldDefinition(_label, _field), _attribute, _value, _instance));
  }

  /**
   * The instance method sets the object id for this bean. To set the object id
   * means to set the instance for this bean. The instande method also adds the
   * parameters to the hidden parameters.
   * 
   * @param _oid
   *          object id
   * @see #instance
   */
  /*
   * public void setOid(String _oid) throws EFapsException { super.setOid(_oid);
   * if (_oid!=null) { addHiddenValue("oid", _oid); } }
   */
  /**
   * The instance method sets the unique key instance object id. This happens,
   * if the user puts values in fields of unique key attributes for which an
   * instance already exists.
   * 
   * @param _ukOid
   * @see #ukInstance
   * @see #ukTitle
   * @see #ukMode
   */
  public void setUkOid(String _ukOid) throws Exception {
    if (_ukOid != null && _ukOid.length() > 0) {
      setMode(CommandAbstract.TARGET_MODE_EDIT);
      setUkMode(true);
      setUkInstance(new Instance(_ukOid));
      addHiddenValue("ukOid", _ukOid);
    }
  }

  // ///////////////////////////////////////////////////////////////////////////

  /**
   * This is the getter method for the instance variable {@link #form}.
   * 
   * @return value of instance variable {@link #form}
   * @see #form
   */
  public Form getForm() {
    return this.form;
  }

  /**
   * This is the getter method for the instance variable {@link #values}.
   *
   * @return value of instance variable {@link #values}
   * @see #values
   * @see #setValues
   */
  public List < FieldValue > getValues()  {
    return this.values;
  }

  /**
   * This is the getter method for the instance variable {@link #ukInstance}.
   * 
   * @return value of instance variable {@link #ukInstance}
   * @see #ukInstance
   * @see #setUkInstance
   */
  public Instance getUkInstance() {
    return this.ukInstance;
  }

  /**
   * This is the setter method for the instance variable {@link #ukInstance}.
   * 
   * @param _ukInstance
   *          new value for instance variable {@link #ukInstance}
   * @see #ukInstance
   * @see #getUkInstance
   */
  public void setUkInstance(Instance _ukInstance) {
    this.ukInstance = _ukInstance;
  }

  /**
   * This is the getter method for the instance variable {@link #ukTitle}.
   * 
   * @return value of instance variable {@link #ukTitle}
   * @see #ukTitle
   * @see #setUkTitle
   */
  public String getUkTitle() {
    return this.ukTitle;
  }

  /**
   * This is the setter method for the instance variable {@link #ukTitle}.
   * 
   * @param _ukTitle
   *          new value for instance variable {@link #ukTitle}
   * @see #ukTitle
   * @see #getUkTitle
   */
  public void setUkTitle(String _ukTitle) {
    this.ukTitle = _ukTitle;
  }

  /**
   * This is the getter method for the instance variable {@link #ukMode}.
   * 
   * @return value of instance variable {@link #ukMode}
   * @see #ukMode
   * @see #setUkMode
   */
  public boolean isUkMode() {
    return this.ukMode;
  }

  /**
   * This is the setter method for the instance variable {@link #ukMode}.
   * 
   * @param _ukMode
   *          new value for instance variable {@link #ukMode}
   * @see #ukMode
   * @see #getUkMode
   */
  public void setUkMode(boolean _ukMode) {
    this.ukMode = _ukMode;
  }
}