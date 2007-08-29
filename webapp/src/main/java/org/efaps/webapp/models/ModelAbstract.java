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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.wicket.IClusterable;
import org.apache.wicket.PageParameters;
import org.apache.wicket.model.IModel;

import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.ui.Command;
import org.efaps.admin.ui.CommandAbstract;
import org.efaps.admin.ui.Menu;
import org.efaps.beans.ValueList;
import org.efaps.beans.valueparser.ValueParser;
import org.efaps.db.Context;
import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;

/**
 * @author jmo
 * @version $Id$
 * 
 */
public abstract class ModelAbstract implements IModel {

  /**
   *
   */
  private static String PARAM_ORIG_CMD_NAME = "eFapsOriginalCommand";

  /**
   *
   */
  protected static String PARAM_CALL_CMD_NAME = "eFapsCallingCommand";

  /**
   * The instance variable stores the commandUUID instance for this form
   * request.
   * 
   * @see #getCommand
   */
  private UUID commanduuid;

  /**
   * The instance variable stores the mode of the form.
   * 
   * @see #getMode
   * @see #setMode
   */
  private int mode = CommandAbstract.TARGET_MODE_UNKNOWN;

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

  /**
   * The instance variable store the node id for this table or form bean used
   * e.g. in references.
   * 
   * @see #getNodeId
   * @see #setNodeId
   */
  private String nodeId = null;

  /**
   * The instance variable is the flag if this class instance is already
   * initialised.
   * 
   * @see #isInitialised
   * @see #setInitialised
   */
  private boolean initialised = false;

  private PageParameters parameters;

  private String oid;

  public ModelAbstract() throws EFapsException {
    initialise();
  }

  public ModelAbstract(PageParameters _parameters) {
    this.parameters = _parameters;
    try {
      initialise();
    } catch (EFapsException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private void initialise() throws EFapsException {
    this.oid = getParameter("oid");
    this.nodeId = getParameter("nodeId");

    String cmdName = getParameter("command");
    if ((cmdName == null) || (cmdName.length() == 0)
        || ("undefined".equals(cmdName))) {
      cmdName = getParameter(PARAM_ORIG_CMD_NAME);
    }
    CommandAbstract command = getCommand(cmdName);
    this.commanduuid = command.getUUID();
    if (command != null) {
      setMode(command.getTargetMode());
      addHiddenValue(PARAM_ORIG_CMD_NAME, cmdName);
    }

    // store original calling command (e.g. the command calling the search)
    String cldName = getParameter(PARAM_CALL_CMD_NAME);
    if ((cldName != null) && (cldName.length() > 0)) {
      addHiddenValue(PARAM_CALL_CMD_NAME, cldName);
    }
  }

  /**
   * This is the getter method for the initialised variable {@link #initialised}.
   * 
   * @return value of initialised variable {@link #initialised}
   * @see #initialised
   * @see #setInitialised
   */
  public boolean isInitialised() {
    return this.initialised;
  }

  /**
   * This is the setter method for the initialised variable {@link #initialised}.
   * 
   * @param _initialised
   *                new value for initialised variable {@link #initialised}
   * @see #initialised
   * @see #isInitialised
   */
  public void setInitialised(boolean _initialised) {
    this.initialised = _initialised;
  }

  public String getTitle() throws Exception {
    String title =
        DBProperties.getProperty(this.getCommand().getName() + ".Title");

    if ((title != null) && (this.getOid() != null)) {
      SearchQuery query = new SearchQuery();
      query.setObject(this.getOid());
      ValueParser parser = new ValueParser(new StringReader(title));
      ValueList list = parser.ExpressionString();
      list.makeSelect(query);
      if (query.selectSize() > 0) {
        query.execute();
        if (query.next()) {
          title = list.makeString(query);
        }
        query.close();
      }
    }

    return title;
  }

  /**
   * @return value for given parameter
   * @todo description
   */
  public String getParameter(String _name) throws EFapsException {
    String ret = null;

    String[] values = Context.getThreadContext().getParameters().get(_name);
    if (values != null) {
      ret = values[0];
    } else {
      if (this.parameters.get(_name) instanceof String[]) {
        values = (String[]) this.parameters.get(_name);
        if (values != null) {
          ret = values[0];
        }
      } else {
        ret = (String) this.parameters.get(_name);

      }
    }

    return ret;
  }

  public String getOid() {
    return this.oid;
  }

  /**
   * This is the getter method for the instance variable {@link #command}.
   * 
   * @return value of instance variable {@link #command}
   * @see #command
   */
  public CommandAbstract getCommand() {
    CommandAbstract cmd = Command.get(this.commanduuid);
    if (cmd == null) {
      cmd = Menu.get(this.commanduuid);
    }
    return cmd;
  }

  /**
   * For given name of command / menu, the related command and menu Java
   * instance is searched and, if found, returned.
   * 
   * @param _name
   *                name of searched command object
   * @return found command / menu instance, or <code>null</null> if not found
   */
  public CommandAbstract getCommand(final String _name) throws EFapsException {
    CommandAbstract cmd = Command.get(_name);
    if (cmd == null) {
      cmd = Menu.get(_name);
    }
    return cmd;
  }

  /**
   * This is the getter method for the instance variable {@link #mode}.
   * 
   * @return value of instance variable {@link #mode}
   * @see #mode
   * @see #setMode
   */
  public int getMode() {
    return this.mode;
  }

  /**
   * This is the setter method for the instance variable {@link #mode}.
   * 
   * @param _mode
   *                new value for instance variable {@link #mode}
   * @see #mode
   * @see #getMode
   */
  protected void setMode(int _mode) {
    this.mode = _mode;
  }

  /**
   * The instance method adds one hidden value to the list of hidden values in
   * variable {@link #hiddenValues}.
   * 
   * @param _name
   *                name of the hidden value
   * @param _value
   *                value of the hidden value
   * @see #hiddenValues
   */
  protected void addHiddenValue(final String _name, final String _value) {
    getHiddenValues().add(new HiddenValue(_name, _value));
  }

  /**
   * This is the getter method for the instance variable {@link #hiddenValues}.
   * 
   * @return value of instance variable {@link #hiddenValues}
   * @see #hiddenValues
   */
  public List<HiddenValue> getHiddenValues() {
    return this.hiddenValues;
  }

  /**
   * @see #mode
   */
  public boolean isCreateMode() {
    return getMode() == CommandAbstract.TARGET_MODE_CREATE;
  }

  /**
   * @see #mode
   */
  public boolean isSearchMode() {
    return getMode() == CommandAbstract.TARGET_MODE_SEARCH;
  }

  /**
   * @see #mode
   */
  public boolean isEditMode() {
    return getMode() == CommandAbstract.TARGET_MODE_EDIT;
  }

  /**
   * @see #mode
   */
  public boolean isViewMode() {
    return getMode() == CommandAbstract.TARGET_MODE_VIEW
        || getMode() == CommandAbstract.TARGET_MODE_UNKNOWN;
  }

  /**
   * This is the getter method for the instance variable {@link #maxGroupCount}.
   * 
   * @return value of instance variable {@link #maxGroupCount}
   * @see #maxGroupCount
   * @see #setMaxGroupCount
   */
  public int getMaxGroupCount() {
    return this.maxGroupCount;
  }

  /**
   * This is the setter method for the instance variable {@link #maxGroupCount}.
   * 
   * @param _maxGroupCount
   *                new value for instance variable {@link #maxGroupCount}
   * @see #maxGroupCount
   * @see #getMaxGroupCount
   */
  protected void setMaxGroupCount(int _maxGroupCount) {
    this.maxGroupCount = _maxGroupCount;
  }

  /**
   * This is the getter method for the instance variable {@link #nodeId}.
   * 
   * @return value of instance variable {@link #nodeId}
   * @see #nodeId
   */
  public String getNodeId() {
    return this.nodeId;
  }

  /**
   * The class stores one hidden value in the instance variable
   * {@link #hiddenValues}.
   */
  public class HiddenValue implements IClusterable {

    // /////////////////////////////////////////////////////////////////////////
    // instance variables

    private static final long serialVersionUID = 1L;

    /**
     * The instance variable stores the name of the hidden value.
     * 
     * @see #setName
     */
    private final String name;

    /**
     * The instance variable stores the value of the hidden value.
     * 
     * @see #getValue
     */
    private final String value;

    // /////////////////////////////////////////////////////////////////////////
    // constructors / destructors

    /**
     * The constructor creates a new hidden value.
     * 
     * @param _name
     *                name of the hidden value
     * @param _value
     *                value of the hidden value
     */
    private HiddenValue(final String _name, final String _value) {
      this.name = _name;
      this.value = _value;
    }

    // /////////////////////////////////////////////////////////////////////////
    // instance getter / setter methods

    /**
     * This is the getter method for the instance variable {@link #name}.
     * 
     * @return value of instance variable {@link #name}
     * @see #name
     */
    public String getName() {
      return this.name;
    }

    /**
     * This is the getter method for the instance variable {@link #value}.
     * 
     * @return value of instance variable {@link #value}
     * @see #value
     */
    public String getValue() {
      return this.value;
    }
  }
}
