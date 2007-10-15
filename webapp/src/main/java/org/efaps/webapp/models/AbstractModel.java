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
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.model.Model;

import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.ui.Command;
import org.efaps.admin.ui.CommandAbstract;
import org.efaps.admin.ui.Menu;
import org.efaps.admin.ui.Search;
import org.efaps.beans.ValueList;
import org.efaps.beans.valueparser.ParseException;
import org.efaps.beans.valueparser.ValueParser;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;
import org.efaps.webapp.pages.error.ErrorPage;

/**
 * @author jmo
 * @version $Id$
 */
public abstract class AbstractModel extends Model {

  /**
   * This instance variable stores the UUID of the CommandAbstract wich was
   * originaly called from the Frontend and let to the construction of this
   * model
   *
   * @see #getCallingCommandUUID()
   * @see #getCallingCommand()
   * @see #setCallingCommandUUID(UUID)
   */
  private UUID callingCommandUUID;

  /**
   * The instance variable stores the UUID of the Command for this Model
   *
   * @see #getCommandUUID()
   * @see #getCommand
   */
  private UUID commandUUID;

  /**
   * The instance variable is the flag if this class instance is already
   * initialised.
   *
   * @see #isInitialised
   * @see #setInitialised
   */
  private boolean initialised = false;

  /**
   * Stores the maximal group count for a row.
   *
   * @see #getMaxGroupCount
   * @see #setMaxGroupCount
   */
  private int maxGroupCount = 1;

  /**
   * The instance variable stores the mode of the form.
   *
   * @see #getMode
   * @see #setMode
   */
  private int mode = CommandAbstract.TARGET_MODE_UNKNOWN;

  /**
   * This instance variable stores the OID of the Instance
   *
   * @see #getOid()
   */
  private String oid;

  /**
   * This instance variable stores the PageParameters wich are used to create
   * the Model
   *
   * @see #getPageParameters()
   * @see #getParameter(String)
   */
  private final PageParameters parameters;

  /**
   * This instance variable stores, if the Model is supposed to be submited
   *
   * @see #isSubmit()
   * @see #setSubmit(boolean)
   */

  private boolean submit = false;

  /**
   * This instance variable stores the Target of this Model
   *
   * @see #getTarget()
   */

  private int target = CommandAbstract.TARGET_UNKNOWN;

  /**
   * Constructor
   *
   * @param _parameters
   *                PageParameters for this Model
   */
  public AbstractModel(PageParameters _parameters) {
    this.parameters = _parameters;
    initialise();
  }

  public AbstractModel(final UUID _commandUUID, final String _oid) {
    this.parameters = new PageParameters();
    this.parameters.add("oid", _oid);
    this.parameters.add("command", _commandUUID.toString());
    initialise();
  }

  /**
   * This method initialises the AbstractModel by setting the instance variables
   *
   * @throws EFapsException
   */
  private void initialise() {
    this.oid = getParameter("oid");
    CommandAbstract command =
        getCommand(UUID.fromString(getParameter("command")));
    this.commandUUID = command.getUUID();
    this.mode = command.getTargetMode();
    this.target = command.getTarget();
    this.submit = command.isSubmit();
    if (command.getTargetSearch() != null && !(this instanceof MenuItemModel)) {
      this.callingCommandUUID = this.commandUUID;
      this.commandUUID =
          command.getTargetSearch().getDefaultCommand().getUUID();
      this.setMode(CommandAbstract.TARGET_MODE_SEARCH);
      if (command.hasEvents(EventType.UI_COMMAND_EXECUTE)) {
        this.submit = true;
      }
    }

  }

  /**
   * This Method resets the Model, so that the next time the Model is going to
   * be connected, the underlying Data will be recieved newly from the
   * eFapsDataBase
   */
  public abstract void resetModel();

  /**
   * get the CommandAbstract wich was originaly called from the Frontend and let
   * to the construction of this model
   *
   * @see #callingCommandUUID
   * @return the calling CommandAbstract
   */
  public CommandAbstract getCallingCommand() {
    CommandAbstract cmd = Command.get(this.callingCommandUUID);
    if (cmd == null) {
      cmd = Menu.get(this.callingCommandUUID);
    }
    return cmd;
  }

  /**
   * This is the getter method for the instance variable
   * {@link #callingCommandUUID}.
   *
   * @return value of instance variable {@link #commandUUID}
   */
  public UUID getCallingCommandUUID() {
    return this.callingCommandUUID;
  }

  /**
   * This is the setter method for the instance variable
   * {@link #callingCommandUUID}.
   *
   * @param _uuid
   *                UUID of the CommandAbstract
   */
  public void setCallingCommandUUID(UUID _uuid) {
    this.callingCommandUUID = _uuid;
  }

  /**
   * get the CommandAbstract for the instance variable {@link #commandUUID}.
   *
   * @return CommandAbstract for the instance variable {@link #commandUUID}
   * @see #command
   */
  public CommandAbstract getCommand() {
    CommandAbstract cmd = Command.get(this.commandUUID);
    if (cmd == null) {
      cmd = Menu.get(this.commandUUID);
    }
    if (cmd == null) {
      cmd = Search.get(this.commandUUID);
    }
    return cmd;
  }

  /**
   * For given UUID of command / menu / Search, the related command / menu
   * /search Java instance is searched and, if found, returned.
   *
   * @param _name
   *                name of searched command object
   * @return found command / menu instance, or <code>null</null> if not found
   */
  protected CommandAbstract getCommand(final UUID _uuid) {
    CommandAbstract cmd = Command.get(_uuid);
    if (cmd == null) {
      cmd = Menu.get(_uuid);
      if (cmd == null) {
        cmd = Search.get(_uuid);
      }
    }
    return cmd;
  }

  /**
   * This is the getter method for the instance variable {@link #commandUUID}.
   *
   * @return value of instance variable {@link #commandUUID}
   */
  public UUID getCommandUUID() {
    return this.commandUUID;
  }

  /**
   * This is the setter method for the instance variable {@link #commandUUID}.
   *
   * @param _uuid
   *                UUID to set for teh instance varaiable {@link #commandUUID}.
   */
  public void setCommandUUID(UUID _uuid) {
    this.commandUUID = _uuid;
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
   * This is the getter method for the instance variable {@link #oid}.
   *
   * @return value of instance variable {@link #oid}
   */
  public String getOid() {
    return this.oid;
  }

  /**
   * This is the getter method for the instance variable {@link #parameters}.
   *
   * @return value of instance variable {@link #parameters}
   */
  public PageParameters getPageParameters() {
    return this.parameters;
  }

  /**
   * This Method returns the Value of a Parameter for the given key. It searches
   * for the Parameter first in the instance variable {@link #parameters} and if
   * not found in the Context.
   *
   * @param _key
   *                Key for the Parameter to retrieve
   * @return Parameter for the key, null if not found
   * @throws EFapsException
   */
  public String getParameter(String _key) {
    String ret = null;
    try {
      String[] values;

      if (this.parameters.get(_key) instanceof String[]) {
        values = (String[]) this.parameters.get(_key);
        if (values != null) {
          ret = values[0];
        }
      } else {
        ret = (String) this.parameters.get(_key);
      }
      if (ret == null) {
        values = Context.getThreadContext().getParameters().get(_key);
        if (values != null) {
          ret = values[0];
        }
      }
    } catch (EFapsException e) {
      throw new RestartResponseException(new ErrorPage(e));
    }

    return ret;
  }

  /**
   * This is the getter method for the instance variable {@link #target}.
   *
   * @return value of instance variable {@link #target}
   */
  public int getTarget() {
    return this.target;
  }

  /**
   * This method retrieves the Value for the Titel from the eFaps Database
   *
   * @return Value of the Title
   * @throws Exception
   */
  public String getTitle() {
    String title =
        DBProperties.getProperty(this.getCommand().getName() + ".Title");
    try {

      if ((title != null) && (this.getOid() != null)) {
        SearchQuery query = new SearchQuery();
        query.setObject(this.getOid());
        ValueParser parser = new ValueParser(new StringReader(title));
        ValueList list;
        list = parser.ExpressionString();
        list.makeSelect(query);
        if (query.selectSize() > 0) {
          query.execute();
          if (query.next()) {
            title = list.makeString(query);
          }
          query.close();
        }
      }
    } catch (ParseException e) {
      throw new RestartResponseException(new ErrorPage(new EFapsException(this
          .getClass(), "", "Error reading the Title")));
    } catch (Exception e) {
      throw new RestartResponseException(new ErrorPage(new EFapsException(this
          .getClass(), "", "Error reading the Title")));
    }

    return title;
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
  public boolean isEditMode() {
    return getMode() == CommandAbstract.TARGET_MODE_EDIT;
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
  public boolean isViewMode() {
    return getMode() == CommandAbstract.TARGET_MODE_VIEW
        || getMode() == CommandAbstract.TARGET_MODE_UNKNOWN;
  }

  /**
   * This is the getter method for the instance variable {@link #initialised}.
   *
   * @return value of instance variable {@link #initialised}
   * @see #initialised
   * @see #setInitialised
   */
  public boolean isInitialised() {
    return this.initialised;
  }

  /**
   * This is the setter method for the instance variable {@link #initialised}.
   *
   * @param _initialised
   *                new value for instance variable {@link #initialised}
   * @see #initialised
   * @see #isInitialised
   */
  public void setInitialised(boolean _initialised) {
    this.initialised = _initialised;
  }

  /**
   * This is the getter method for the instance variable {@link #submit}.
   *
   * @return value of instance variable {@link #submit}
   * @see #setSubmit(boolean)
   */
  public boolean isSubmit() {
    return this.submit;
  }

  /**
   * This is the setter method for the instance variable {@link #submit}.
   *
   * @see #isSubmit()
   */
  public void setSubmit(final boolean _submit) {
    this.submit = _submit;
  }

  /**
   * This method executes the Events wich are related to this Model. It will
   * take the Events of the CallingCommand {@link #callingCommandUUID}, if it
   * is declared, otherwise it will take the Events of the Command
   * {@link #commandUUID}. The Method also adds the oid {@link #oid} to the
   * Context, so that it is accessable for the esjp.
   *
   * @param _others
   *                The values will be atached to the call as
   *                ParameterValues.OTHERS
   */
  public void executeEvents(String[] _others) {
    CommandAbstract command;
    if (this.callingCommandUUID != null) {
      command = this.getCallingCommand();
    } else {
      command = this.getCommand();
    }
    try {
      if (command.hasEvents(EventType.UI_COMMAND_EXECUTE)) {
        if (this.getOid() != null) {
          String[] contextoid = { this.getOid() };
          Context.getThreadContext().getParameters().put("oid", contextoid);
          command.executeEvents(EventType.UI_COMMAND_EXECUTE,
              ParameterValues.INSTANCE, new Instance(this.getOid()),
              ParameterValues.OTHERS, _others);
        } else {
          command.executeEvents(EventType.UI_COMMAND_EXECUTE,
              ParameterValues.OTHERS, _others);
        }
      }
    } catch (EFapsException e) {
      throw new RestartResponseException(new ErrorPage(e));
    }

  }
}
