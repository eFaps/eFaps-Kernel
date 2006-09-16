/*
 * Copyright 2006 The eFaps Team
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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.fileupload.FileItem;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.attributetype.AbstractFileType;
import org.efaps.admin.datamodel.AttributeTypeInterface;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.datamodel.ui.UIInterface;
import org.efaps.admin.datamodel.UniqueKey;
import org.efaps.admin.ui.Command;
import org.efaps.admin.ui.CommandAbstract;
import org.efaps.admin.ui.Field;
import org.efaps.admin.ui.Form;
import org.efaps.admin.ui.Menu;
import org.efaps.beans.valueparser.ValueParser;
import org.efaps.db.Checkin;
import org.efaps.db.Context;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.db.Update;
import org.efaps.beans.form.FormFieldUpdateInterface;

/**
 *
 * @author tmo
 * @version $Id$
 * @todo description
 */
public class FormBean extends AbstractCollectionBean  {

  public FormBean()  {
System.out.println("FormBean.constructor");
  }

  public void finalize()  {
System.out.println("FormBean.destructor");
  }

  /////////////////////////////////////////////////////////////////////////////

  public void execute() throws Exception  {
    Context context = Context.getThreadContext();
    if (isCreateMode())  {
      setValues(new ArrayList());
      getValues().add(null);

Type type = getCommand().getTargetCreateType();

      for (int i=0; i<getForm().getFields().size(); i++)  {
        Field field = (Field)getForm().getFields().get(i);


if (field.getExpression()!=null)  {
  Attribute attr = type.getAttribute(field.getExpression());
  if (attr!=null)  {
    addFieldValue(field, attr, null, null);
  }
} else if (field.getClassUI()!=null)  {
  addFieldValue(field, null);
} else if (field.getGroupCount()>0)  {
  addFieldValue(field, null);
  if (getMaxGroupCount()<field.getGroupCount())  {
    setMaxGroupCount(field.getGroupCount());
  }
}

      }
    } else  {
Instance instance = getInstance();
if (ukInstance!=null)  {
  instance = ukInstance;
}
      SearchQuery query = new SearchQuery();
      query.setObject(context, instance);
      query.add(context, getForm());
//        query.addAllFromString(context, getTitle());

ValueParser parser = new ValueParser(new StringReader(getTitle()));
ValueList list = parser.ExpressionString();
list.makeSelect(context, query);

      query.addAllFromString(context, getUkTitle());
      query.execute();

      if (query.next())  {
        setValues(new ArrayList());
        getValues().add(query.getInstance(context, instance.getType()));
        for (int i=0; i<getForm().getFields().size(); i++)  {
          Field field = (Field)getForm().getFields().get(i);

if (field.getExpression()!=null)  {
  Object value = query.get(context, field);
  addFieldValue(field, query.getAttribute(context, field), value, query.getInstance(context, field));
} else if (field.getClassUI()!=null)  {
  addFieldValue(field, instance);
} else if (field.getGroupCount()>0)  {
  addFieldValue(field, instance);
  if (getMaxGroupCount()<field.getGroupCount())  {
    setMaxGroupCount(field.getGroupCount());
  }
}
        }
//          setTitle(query.replaceAllInString(context, getTitle()));
setTitle(list.makeString(context, query));
        setUkTitle(query.replaceAllInString(context, getUkTitle()));
      }

      query.close();
    }
  }

  /////////////////////////////////////////////////////////////////////////////

  /**
   * The instance method is called to process the modifcation of a form.
   *
   * @see #processCreate
   * @see #processUpdate
   */
  public void process() throws Exception  {
    if (isCreateMode())  {
      processCreate(Context.getThreadContext());
    } else  {
      processUpdate(Context.getThreadContext());
    }
  }

  /**
   * The instance method process the create of a new object.
   *
   * @param _context  context for this request
   * @todo maybe an axception must be thrown? see TODO comment
   */
  protected void processCreate(Context _context) throws Exception  {
    Insert insert = new Insert(_context, getCommand().getTargetCreateType());
    for (Field field : getForm().getFields())  {
      if (field.getExpression()!=null && (field.isCreatable() || field.isHidden()))  {
        Attribute attr = getCommand().getTargetCreateType().getAttribute(field.getExpression());
        if (attr!=null && !AbstractFileType.class.isAssignableFrom(attr.getAttributeType().getClassRepr()))  {
          String value = getParameter(field.getName());
          insert.add(_context, attr, value);
        }
      }
    }
    if (getCommand().getTargetConnectAttribute()!=null)  {
      Instance instance = new Instance(_context, getParameter("oid"));
      insert.add(_context, getCommand().getTargetConnectAttribute(), ""+instance.getId());
    }
    insert.execute();
    setInstance(new Instance(_context, getCommand().getTargetCreateType(), insert.getId()));

// "TargetConnectChildAttribute"
// "TargetConnectParentAttribute"
// "TargetConnectType"
if (getCommand().getProperty("TargetConnectType")!=null)  {
  Instance parent = new Instance(_context, getParameter("oid"));

  Insert connect = new Insert(_context, getCommand().getProperty("TargetConnectType"));
  connect.add(_context, getCommand().getProperty("TargetConnectParentAttribute"), ""+parent.getId());
  connect.add(_context, getCommand().getProperty("TargetConnectChildAttribute"), ""+insert.getId());
  connect.execute();
}

    for (Field field : getForm().getFields())  {
      if (field.getExpression()==null && field.isCreatable())  {
        FileItem fileItem = getFileParameter(field.getName());
        String updateClassName = field.getProperty("ClassNameUpdate");

        if (updateClassName != null)  {
          Class < FormFieldUpdateInterface > updateClass = (Class < FormFieldUpdateInterface >) Class.forName(updateClassName);
          FormFieldUpdateInterface fieldUpdate = updateClass.newInstance();
          fieldUpdate.update(_context, this, field);
        } else if (fileItem!=null)  {
          Checkin checkin = new Checkin(_context, getInstance());
          checkin.execute(_context, fileItem.getName(), fileItem.getInputStream(), (int)fileItem.getSize());
        }
// TODO: ev. exception?
      }
    }
  }

  /**
   * The instance method process the update of current selected object.
   *
   * @param _context  context for this request
   * @todo maybe an axception must be thrown? see TODO comment
   */
  protected void processUpdate(Context _context) throws Exception  {
    Update update = new Update(_context, getInstance());
    for (Field field : getForm().getFields())  {
      if (field.getExpression()!=null && field.isEditable())  {
Attribute attr = getInstance().getType().getAttribute(field.getExpression());
if (attr!=null && !AbstractFileType.class.isAssignableFrom(attr.getAttributeType().getClassRepr()))  {
System.out.println("field.getName()="+field.getName());
  update.add(_context, attr, getParameter(field.getName()).replace(',','.'));
}
      }
    }

    for (Field field : getForm().getFields())  {
      if (field.getExpression()==null && field.isEditable())  {
        FileItem fileItem = getFileParameter(field.getName());
        String updateClassName = field.getProperty("ClassNameUpdate");

        if (updateClassName != null)  {
          Class < FormFieldUpdateInterface > updateClass = (Class < FormFieldUpdateInterface >) Class.forName(updateClassName);
          FormFieldUpdateInterface fieldUpdate = updateClass.newInstance();
          fieldUpdate.update(_context, this, field);
        } else if (fileItem!=null)  {
          Checkin checkin = new Checkin(_context, getInstance());
          checkin.execute(_context, fileItem.getName(), fileItem.getInputStream(), (int)fileItem.getSize());
        }
// TODO: ev. exception?
      }
    }

    update.execute();
  }


  /////////////////////////////////////////////////////////////////////////////

  /**
   * The instance method test for given user input parameter and unique keys
   * of the create type one (!) instance exists. If one instance exists, this
   * instance is stored in {@link #ukInstance}.
   *
   * @see #ukInstance
   */
  public void ukTest() throws Exception  {
    Context context = Context.getThreadContext();
    Map map = new HashMap();
    for (int i=0; i< getForm().getFields().size(); i++)  {
      Field field = (Field)getForm().getFields().get(i);
/*        if (field.getAttribute()!=null && field.getAttribute().getUniqueKeys()!=null)  {
          map.put(field.getAttribute(), getRequest().getParameter(field.getName()));
        }
*/
    }


    Type type = getCommand().getTargetCreateType();
    for (Iterator ukIter = type.getUniqueKeys().iterator(); ukIter.hasNext(); )  {
      UniqueKey uniqueKey = (UniqueKey)ukIter.next();
      SearchQuery query = new SearchQuery();
      for (Iterator attrIter = uniqueKey.getAttributes().iterator(); attrIter.hasNext(); )  {
        Attribute attr = (Attribute)attrIter.next();
        String value = (String)map.get(attr);
        if (value==null)  {
          query = null;
          break;
        }
        query.addWhereAttrEqValue(context, attr, value);
      }
      if (query!=null)  {
        query.execute();
        if (query.next())  {
          Instance instance = query.getInstance(context, type);
          if (!query.next())  {
            setUkInstance(instance);
            break;
          }
        }
      }
    }
  }

  /////////////////////////////////////////////////////////////////////////////

  /**
   *
   * @param _name name of the command object
   */
  public void setCommandName(String _name) throws Exception  {
    super.setCommandName(_name);
    if (getCommand()!=null)  {
      addHiddenValue("formCommand", _name);
      setForm(getCommand().getTargetForm());
    }
  }

  /**
   * Sets the parameters values for file and form. Also the command name is
   * extracted from the form values.
   *
   * @param _parameters     new value for Parameters variable
   *                        {@link AbstractBean.parameters}
   * @param _fileParameters new value for Parameters variable
   *                        {@link AbstractBean.fileParameters}
   * @see AbstractBean.parameters
   * @see AbstractBean.fileParameters
   * @see AbstractBean.setParameters
   * @see #setCommandName
   */
  public void setParameters(Map<String,String[]> _parameters, Map<String,FileItem> _fileParameters) throws Exception  {
    super.setParameters(_parameters, _fileParameters);

    String cmdName = getParameter("command");
    if (cmdName==null || cmdName.length()==0 || "undefined".equals(cmdName))  {
      cmdName = getParameter("formCommand");
    }
    setCommandName(cmdName);
  }

  /**
   * Adds a field value to the list of values.
   *
   * @see #addFieldValue(String,Field,UIInterface,Object,Instance)
   */
  public void addFieldValue(Field _field, Attribute _attr, Object _value, Instance _instance)  {
    String label = null;
    if (_field.getLabel()!=null)  {
      label = _field.getLabel();
    } else  {
      label = _attr.getParent().getName() + "/" + _attr.getName() + ".Label";
    }
    UIInterface classUI = null;
    if (_field.getClassUI()!=null)  {
      classUI = _field.getClassUI();
    } else  {
      classUI = _attr.getAttributeType().getUI();
    }
    addFieldValue(label, _field, classUI, _value, _instance);
  }

  /**
   * Adds a field value to the list of values.
   *
   * @see #addFieldValue(String,Field,UIInterface,Object,Instance)
   */
  public void addFieldValue(Field _field, Instance _instance)  {
    addFieldValue(_field.getLabel(), _field, _field.getClassUI(), null, _instance);
  }

  /**
   * The instance method adds a new attribute value (from instance
   * {@link AttributeTypeInterface}) to the values.
   *
   * @see #values
   */
  public void addFieldValue(String _label, Field _field, UIInterface _classUI, Object _value, Instance _instance)  {
    getValues().add(new Value(_label, _field, _classUI, _value, _instance));
  }

  /**
   * The instance method sets the object id for this bean. To set the
   * object id means to set the instance for this bean. The instande method
   * also adds the parameters to the hidden parameters.
   *
   * @param _oid    object id
   * @see #instance
   */
  public void setOid(String _oid) throws Exception  {
    super.setOid(_oid);
    if (_oid!=null)  {
      addHiddenValue("oid", _oid);
    }
  }

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
  public void setUkOid(String _ukOid) throws Exception  {
    if (_ukOid!=null && _ukOid.length()>0)  {
      setMode(CommandAbstract.TARGET_MODE_EDIT);
      setUkMode(true);
      setUkInstance(new Instance(Context.getThreadContext(), _ukOid));
      addHiddenValue("ukOid", _ukOid);
    }
  }

  /////////////////////////////////////////////////////////////////////////////

  /**
   * The instance variable stores the form which must be shown.
   *
   * @see #getForm
   * @see #setForm
   */
  private Form form = null;

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
  private String ukTitle = null;

  /**
   * The instance variable stores if in create mode for a type a given unique
   * key is found.
   *
   * @see #isUkMode
   * @see #setUkMode
   * @see #ukInstance
   * @see #ukTitle
   */
  private boolean ukMode = false;

  /////////////////////////////////////////////////////////////////////////////

  /**
   * This is the getter method for the instance variable {@link #form}.
   *
   * @return value of instance variable {@link #form}
   * @see #form
   * @see #setForm
   */
  public Form getForm()  {
    return this.form;
  }

  /**
   * This is the setter method for the instance variable {@link #form}.
   *
   * @param _form  new value for instance variable {@link #form}
   * @see #form
   * @see #getForm
   */
  public void setForm(Form _form)  {
    this.form = _form;
  }

  /**
   * This is the getter method for the instance variable {@link #ukInstance}.
   *
   * @return value of instance variable {@link #ukInstance}
   * @see #ukInstance
   * @see #setUkInstance
   */
  public Instance getUkInstance()  {
    return this.ukInstance;
  }

  /**
   * This is the setter method for the instance variable {@link #ukInstance}.
   *
   * @param _ukInstance  new value for instance variable {@link #ukInstance}
   * @see #ukInstance
   * @see #getUkInstance
   */
  public void setUkInstance(Instance _ukInstance)  {
    this.ukInstance = _ukInstance;
  }

  /**
   * This is the getter method for the instance variable {@link #ukTitle}.
   *
   * @return value of instance variable {@link #ukTitle}
   * @see #ukTitle
   * @see #setUkTitle
   */
  public String getUkTitle()  {
    return this.ukTitle;
  }

  /**
   * This is the setter method for the instance variable {@link #ukTitle}.
   *
   * @param _ukTitle  new value for instance variable {@link #ukTitle}
   * @see #ukTitle
   * @see #getUkTitle
   */
  public void setUkTitle(String _ukTitle)  {
    this.ukTitle = _ukTitle;
  }

  /**
   * This is the getter method for the instance variable {@link #ukMode}.
   *
   * @return value of instance variable {@link #ukMode}
   * @see #ukMode
   * @see #setUkMode
   */
  public boolean isUkMode()  {
    return this.ukMode;
  }

  /**
   * This is the setter method for the instance variable {@link #ukMode}.
   *
   * @param _ukMode  new value for instance variable {@link #ukMode}
   * @see #ukMode
   * @see #getUkMode
   */
  public void setUkMode(boolean _ukMode)  {
    this.ukMode = _ukMode;
  }
}