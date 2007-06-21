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

import java.util.ArrayList;
import java.util.List;
import org.efaps.admin.datamodel.ui.UIInterface;
import org.efaps.admin.ui.Command;
import org.efaps.admin.ui.CommandAbstract;
import org.efaps.admin.ui.Field;
import org.efaps.admin.ui.Menu;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.util.EFapsException;

/**
 * @author tmo
 * @version $Id$
 * @todo description
 */
public abstract class AbstractCollectionBean extends AbstractBean implements CollectionBeanInterface  {

  /////////////////////////////////////////////////////////////////////////////
  // constructors / destructors
  
  public AbstractCollectionBean() throws EFapsException  {
    super();
  }

  /////////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * The query is executed and the result is stored (cached) internally. The
   * method must be overwritten by individual implementations.
   */
  abstract public void execute() throws Exception;

  /**
   * The instance method sets the command to the parameter name. Depending on
   * this command, the header menu, footer menu, mode and the target frame is
   * set.
   *
   * @param _name name of the command object
   * @see #menuFooter
   * @see #menuHeader
   * @see #mode
   * @see #targetFrame
   */
  public void setCommandName(String _name) throws EFapsException  {
      Context context = Context.getThreadContext();
      setCommand(Command.get(_name));
      if (getCommand() == null)  {
        setCommand(Menu.get(_name));
      }
      if (getCommand()!=null)  {
        setMode(getCommand().getTargetMode());
        setTargetFrame(getCommand().getTarget());
      }
  }

  /////////////////////////////////////////////////////////////////////////////

  /**
   * The instance method adds one hidden value to the list of hidden values in
   * variable {@link #hiddenValues}.
   *
   * @param _name   name of the hidden value
   * @param _value  value of the hidden value
   * @see #hiddenValues
   */
  protected void addHiddenValue(String _name, String _value)  {
    getHiddenValues().add(new HiddenValue(_name, _value));
  }

  /////////////////////////////////////////////////////////////////////////////

  /**
   * @see #mode
   */
  public boolean isConnectMode()  {
    return getMode() == CommandAbstract.TARGET_MODE_CONNECT;
  }

  /**
   * @see #mode
   */
  public boolean isCreateMode()  {
    return getMode() == CommandAbstract.TARGET_MODE_CREATE;
  }

  /**
   * @see #mode
   */
  public boolean isEditMode()  {
    return getMode() == CommandAbstract.TARGET_MODE_EDIT;
  }

  /**
   * @see #mode
   */
  public boolean isSearchMode()  {
    return false;
  }

  /**
   * @see #mode
   */
  public boolean isViewMode()  {
    return getMode() == CommandAbstract.TARGET_MODE_VIEW || getMode() == CommandAbstract.TARGET_MODE_UNKNOWN;
  }

  /////////////////////////////////////////////////////////////////////////////

  /**
   * @return <i>true</i> if the target frame is popup, otherwise <i>false</i>
   * @see #targetFrame
   */
  public boolean isPopup()  {
    return getTargetFrame() == CommandAbstract.TARGET_POPUP;
  }

  /**
   * @return <i>true</i> if the target frame is content, otherwise <i>false</i>
   * @see #targetFrame
   */
  public boolean isContent()  {
    return getTargetFrame() == CommandAbstract.TARGET_CONTENT;
  }

  /////////////////////////////////////////////////////////////////////////////

  /**
   * The instance variable stores the command instance for this form request.
   *
   * @see #getCommand
   * @see #setCommand
   */
  private CommandAbstract command = null;

  /**
   * The instance variable stores the result list of the execution of the
   * query.
   *
   * @see #getValues
   * @see #setValues
   */
  private List<Object> values = null;

  /**
   * The instance variable stores the mode of the form.
   *
   * @see #getMode
   * @see #setMode
   */
  private int mode = CommandAbstract.TARGET_MODE_UNKNOWN;

  /**
   * The instance variable stores the target frame where the form is shown.
   *
   * @see #getTargetFrame
   * @see #setTargetFrame
   */
  private int targetFrame = CommandAbstract.TARGET_UNKNOWN;

  /**
   * The instance variable stores the title of the form.
   *
   * @see #getTitle
   * @see #setTitle
   */
  private String title = null;

  /**
   * The instance variable store the node id for this table or form bean used
   * e.g. in references.
   *
   * @see #getNodeId
   * @see #setNodeId
   */
  private String nodeId = null;

  /**
   * The instance variable stores the hidden values printed as form value.
   *
   * @see #getHiddenValues
   */
  private List<HiddenValue> hiddenValues = new ArrayList<HiddenValue>();

  /**
   * Stores the maximal group count for a row.
   *
   * @see #getMaxGroupCount
   * @see #setMaxGroupCount
   */
  private int maxGroupCount = 1;

  /////////////////////////////////////////////////////////////////////////////

  /**
   * This is the getter method for the instance variable {@link #command}.
   *
   * @return value of instance variable {@link #command}
   * @see #command
   * @see #setCommand
   */
  public CommandAbstract getCommand()  {
    return this.command;
  }

  /**
   * This is the setter method for the instance variable {@link #command}.
   *
   * @param _command  new value for instance variable {@link #command}
   * @see #command
   * @see #getCommand
   */
  public void setCommand(CommandAbstract _command)  {
    this.command = _command;
  }

  /**
   * This is the getter method for the instance variable {@link #values}.
   *
   * @return value of instance variable {@link #values}
   * @see #values
   * @see #setValues
   */
  public List <Object>getValues()  {
    return this.values;
  }

  /**
   * This is the setter method for the instance variable {@link #values}.
   *
   * @param _values  new value for instance variable {@link #values}
   * @see #values
   * @see #getValues
   */
  public void setValues(List _values)  {
    this.values = _values;
  }

  /**
   * This is the getter method for the instance variable {@link #mode}.
   *
   * @return value of instance variable {@link #mode}
   * @see #mode
   * @see #setMode
   */
  public int getMode()  {
    return this.mode;
  }

  /**
   * This is the setter method for the instance variable {@link #mode}.
   *
   * @param _mode  new value for instance variable {@link #mode}
   * @see #mode
   * @see #getMode
   */
  protected void setMode(int _mode)  {
    this.mode = _mode;
  }

  /**
   * This is the getter method for the instance variable {@link #targetFrame}.
   *
   * @return value of instance variable {@link #targetFrame}
   * @see #targetFrame
   * @see #setTargetFrame
   */
  public int getTargetFrame()  {
    return this.targetFrame;
  }

  /**
   * This is the setter method for the instance variable {@link #targetFrame}.
   *
   * @param _targetFrame  new value for instance variable {@link #targetFrame}
   * @see #targetFrame
   * @see #getTargetFrame
   */
  private void setTargetFrame(int _targetFrame)  {
    this.targetFrame = _targetFrame;
  }

  /**
   * This is the getter method for the instance variable {@link #title}.
   *
   * @return value of instance variable {@link #title}
   * @see #title
   * @see #setTitle
   */
  public String getTitle()  {
    return this.title;
  }

  /**
   * This is the setter method for the instance variable {@link #title}.
   *
   * @param _title  new value for instance variable {@link #title}
   * @see #title
   * @see #getTitle
   */
  public void setTitle(String _title)  {
    this.title = _title;
  }

  /**
   * This is the getter method for the instance variable {@link #nodeId}.
   *
   * @return value of instance variable {@link #nodeId}
   * @see #nodeId
   * @see #setNodeId
   */
  public String getNodeId()  {
    return this.nodeId;
  }

  /**
   * This is the setter method for the instance variable {@link #nodeId}.
   *
   * @param _nodeId  new value for instance variable {@link #nodeId}
   * @see #nodeId
   * @see #getNodeId
   */
  public void setNodeId(String _nodeId)  {
    this.nodeId = _nodeId;
  }

  /**
   * This is the getter method for the instance variable {@link #hiddenValues}.
   *
   * @return value of instance variable {@link #hiddenValues}
   * @see #hiddenValues
   */
  public List<HiddenValue> getHiddenValues()  {
    return this.hiddenValues;
  }

  /**
   * This is the getter method for the instance variable
   * {@link #maxGroupCount}.
   *
   * @return value of instance variable {@link #maxGroupCount}
   * @see #maxGroupCount
   * @see #setMaxGroupCount
   */
  public int getMaxGroupCount()  {
    return this.maxGroupCount;
  }

  /**
   * This is the setter method for the instance variable
   * {@link #maxGroupCount}.
   *
   * @param _maxGroupCount  new value for instance variable
   *                        {@link #maxGroupCount}
   * @see #maxGroupCount
   * @see #getMaxGroupCount
   */
  protected void setMaxGroupCount(int _maxGroupCount)  {
    this.maxGroupCount = _maxGroupCount;
  }

  /////////////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////

  public class Value  {
    protected Value(String _label, Field _field, UIInterface _classUI, Object _value, Instance _instance)  {
      setLabel(_label);
      setField(_field);
      setClassUI(_classUI);
      setValue(_value);
      setInstance(_instance);
    }

    /**
     *
     */
    public String getCreateHtml() throws EFapsException  {
      return getClassUI().getCreateHtml(Context.getThreadContext(), getValue(), getField());
    }

    /**
     *
     */
    public String getViewHtml() throws EFapsException  {
      return getClassUI().getViewHtml(Context.getThreadContext(), getValue(), getField());
    }

    /**
     *
     */
    public String getEditHtml() throws EFapsException  {
      return getClassUI().getEditHtml(Context.getThreadContext(), getValue(), getField());
    }

    /**
     *
     */
    public String getSearchHtml() throws EFapsException  {
      return getClassUI().getSearchHtml(Context.getThreadContext(), getValue(), getField());
    }

    ///////////////////////////////////////////////////////////////////////////

    /**
     * The instance variable stores the label shown in a web form.
     *
     * @see #getClassUI
     * @see #setClassUI
     */
    private String label = null;

    /**
     * The instance variable stores the class to represent this form value.
     *
     * @see #getClassUI
     * @see #setClassUI
     */
    private UIInterface classUI = null;

    /**
     * The variable stores the instance for this value.
     *
     * @see #getInstance
     * @see #setInstance
     */
    private Instance instance = null;

    /**
     * The instance variable stores the field for this value.
     *
     * @see #getField
     * @see #setField
     */
    private Field field = null;

    /**
     * The instance variable stores the value for this form value.
     *
     * @see #getValue
     * @see #setValue
     */
    private Object value = null;

    ///////////////////////////////////////////////////////////////////////////

    /**
     * This is the getter method for the instance variable {@link #label}.
     *
     * @return value of instance variable {@link #label}
     * @see #label
     * @see #setLabel
     */
    public String getLabel()  {
      return this.label;
    }

    /**
     * This is the setter method for the instance variable {@link #label}.
     *
     * @param _label  new value for instance variable {@link #label}
     * @see #label
     * @see #getLabel
     */
    private void setLabel(String _label)  {
      this.label = _label;
    }

    /**
     * This is the getter method for the instance variable {@link #classUI}.
     *
     * @return value of instance variable {@link #classUI}
     * @see #classUI
     * @see #setClassUI
     */
    public UIInterface getClassUI()  {
      return this.classUI;
    }

    /**
     * This is the setter method for the instance variable {@link #classUI}.
     *
     * @param _classUI  new value for instance variable {@link #classUI}
     * @see #classUI
     * @see #getClassUI
     */
    private void setClassUI(UIInterface _classUI)  {
      this.classUI = _classUI;
    }

    /**
     * This is the getter method for the instance variable {@link #instance}.
     *
     * @return value of instance variable {@link #instance}
     * @see #instance
     * @see #setInstance
     */
    public Instance getInstance()  {
      return this.instance;
    }

    /**
     * This is the setter method for the instance variable {@link #instance}.
     *
     * @param _instance  new value for instance variable {@link #instance}
     * @see #instance
     * @see #getInstance
     */
    private void setInstance(Instance _instance)  {
      this.instance = _instance;
    }

    /**
     * This is the getter method for the field variable {@link #field}.
     *
     * @return value of field variable {@link #field}
     * @see #field
     * @see #setField
     */
    public Field getField()  {
      return this.field;
    }

    /**
     * This is the setter method for the field variable {@link #field}.
     *
     * @param _field  new value for field variable {@link #field}
     * @see #field
     * @see #getField
     */
    private void setField(Field _field)  {
      this.field = _field;
    }

    /**
     * This is the getter method for the instance variable {@link #value}.
     *
     * @return value of instance variable {@link #value}
     * @see #value
     * @see #setValue
     */
    public Object getValue()  {
      return this.value;
    }

    /**
     * This is the setter method for the instance variable {@link #value}.
     *
     * @param _value  new value for instance variable {@link #value}
     * @see #value
     * @see #getValue
     */
    private void setValue(Object _value)  {
      this.value = _value;
    }
  }

  /////////////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////

  /**
   * The class stores one hidden value in the instance variable
   * {@link #hiddenValues}.
   */
  public class HiddenValue  {

    /**
     * The constructor creates a new hidden value.
     *
     * @param _name   name of the hidden value
     * @param _value  value of the hidden value
     */
    private HiddenValue(String _name, String _value)  {
      setName(_name);
      setValue(_value);
    }

    ///////////////////////////////////////////////////////////////////////////

    /**
     * The instance variable stores the name of the hidden value.
     *
     * @see #getName
     * @see #setName
     */
    private String name = null;

    /**
     * The instance variable stores the value of the hidden value.
     *
     * @see #setValue
     * @see #getValue
     */
    private String value = null;

    ///////////////////////////////////////////////////////////////////////////

    /**
     * This is the getter method for the instance variable {@link #name}.
     *
     * @return value of instance variable {@link #name}
     * @see #name
     * @see #setName
     */
    public String getName()  {
      return this.name;
    }

    /**
     * This is the setter method for the instance variable {@link #name}.
     *
     * @param _name  new value for instance variable {@link #name}
     * @see #name
     * @see #getName
     */
    private void setName(String _name)  {
      this.name = _name;
    }

    /**
     * This is the getter method for the instance variable {@link #value}.
     *
     * @return value of instance variable {@link #value}
     * @see #value
     * @see #setValue
     */
    public String getValue()  {
      return this.value;
    }

    /**
     * This is the setter method for the instance variable {@link #value}.
     *
     * @param _value  new value for instance variable {@link #value}
     * @see #value
     * @see #getValue
     */
    private void setValue(String _value)  {
      this.value = _value;
    }
  }
}
