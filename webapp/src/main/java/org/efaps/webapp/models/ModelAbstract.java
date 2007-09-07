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
import java.util.UUID;

import org.apache.wicket.PageParameters;
import org.apache.wicket.model.Model;

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
 */
public abstract class ModelAbstract extends Model {

  /**
   * The instance variable stores the commandUUID instance for this form
   * request.
   * 
   * @see #getCommand
   */
  private UUID commandUUID;

  /**
   * The instance variable stores the mode of the form.
   * 
   * @see #getMode
   * @see #setMode
   */
  private int mode = CommandAbstract.TARGET_MODE_UNKNOWN;

  /**
   * Stores the maximal group count for a row.
   * 
   * @see #getMaxGroupCount
   * @see #setMaxGroupCount
   */
  private int maxGroupCount = 1;

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

  private UUID callingCommandUUID;

  private int target = CommandAbstract.TARGET_UNKNOWN;

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
    CommandAbstract command = getCommand(getParameter("command"));
    this.commandUUID = command.getUUID();
    this.setMode(command.getTargetMode());
    this.target = command.getTarget();

    if (command.getTargetSearch() != null) {
      this.callingCommandUUID = this.commandUUID;
      this.commandUUID =
          command.getTargetSearch().getDefaultCommand().getUUID();
      this.setMode(CommandAbstract.TARGET_MODE_SEARCH);

    }

  }

  public abstract void clearModel();

  public void setCommandUUID(UUID _uuid) {
    this.commandUUID = _uuid;
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

  public PageParameters getPageParameters() {
    return this.parameters;
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
    CommandAbstract cmd = Command.get(this.commandUUID);
    if (cmd == null) {
      cmd = Menu.get(this.commandUUID);
    }
    return cmd;
  }

  public CommandAbstract getCallingCommand() {
    CommandAbstract cmd = Command.get(this.callingCommandUUID);
    if (cmd == null) {
      cmd = Menu.get(this.callingCommandUUID);
    }
    return cmd;
  }

  public UUID getCallingCommandUUID() {
    return this.callingCommandUUID;
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

  public int getTarget() {
    return this.target;
  }

}
