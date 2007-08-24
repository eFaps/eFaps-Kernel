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

import org.apache.wicket.model.IModel;

import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.event.EventType;
import org.efaps.admin.ui.CommandAbstract;
import org.efaps.admin.ui.Menu;
import org.efaps.admin.ui.MenuAbstract;
import org.efaps.beans.ValueList;
import org.efaps.beans.valueparser.ValueParser;
import org.efaps.db.SearchQuery;

/**
 * @author tmo
 * @version $Id$
 */
public class IMenuItemModel implements IModel {

  // ///////////////////////////////////////////////////////////////////////////
  // instance variables

  private static final long serialVersionUID = 505704924081527139L;

  /** Url to the image of this menu item. */
  public final String image;

  /** Label of this menu item. */
  public final String label;

  /** Description of this menu item. */
  public final String description;

  /** Url of this menu item. */
  public String url;

  /** All childs of this menu item. */
  public final List<IMenuItemModel> childs = new ArrayList<IMenuItemModel>();

  private CommandAbstract command;

  private final String oid;

  // ///////////////////////////////////////////////////////////////////////////
  // constructors / destructors

  public IMenuItemModel(final String _name) throws Exception {
    this(Menu.get(_name));
  }

  // ///////////////////////////////////////////////////////////////////////////
  // constructors / destructors

  public IMenuItemModel(final String _name, final String _oid) throws Exception {
    this(Menu.get(_name), _oid);
  }

  private IMenuItemModel(final CommandAbstract _command) throws Exception {
    this.image = _command.getIcon();
    this.label = DBProperties.getProperty(_command.getLabel());
    this.description = "";
    this.command = _command;
    this.oid = null;
    if (_command instanceof MenuAbstract) {
      for (CommandAbstract subCmd : ((MenuAbstract) _command).getCommands()) {
        if (subCmd.hasAccess()) {
          this.childs.add(new IMenuItemModel(subCmd));
        }
      }
    }
    // this.url = getTargetURL(_command);
  }

  private IMenuItemModel(final CommandAbstract _command, String _oid)
                                                                     throws Exception {
    this.image = _command.getIcon();

    String label = DBProperties.getProperty(_command.getLabel());

    SearchQuery query = new SearchQuery();
    query.setObject(_oid);
    ValueParser parser = new ValueParser(new StringReader(label));
    ValueList list = parser.ExpressionString();
    list.makeSelect(query);
    if (query.selectSize() > 0) {
      query.execute();
      if (query.next()) {
        label = list.makeString(query);
      }
      query.close();
    }

    this.label = label;
    this.description = "";
    this.command = _command;
    this.oid = _oid;
    if (_command instanceof MenuAbstract) {
      for (CommandAbstract subCmd : ((MenuAbstract) _command).getCommands()) {
        if (subCmd.hasAccess()) {
          this.childs.add(new IMenuItemModel(subCmd, _oid));
        }
      }
    }
  }

  public String getOid() {
    return this.oid;
  }

  public CommandAbstract getCommand() {
    return this.command;
  }

  public void setURL(String _url) {
    this.url = _url;
  }

  public List<IMenuItemModel> getChilds() {
    return this.childs;
  }

  public boolean hasChilds() {
    return !this.childs.isEmpty();
  }

  public String getLabel() {
    return this.label;
  }

  // ///////////////////////////////////////////////////////////////////////////
  // instance methods

  public Object getObject() {
    // TODO Auto-generated method stub
    return null;
  }

  public void setObject(final Object object) {
    // TODO Auto-generated method stub

  }

  public void detach() {
    // TODO Auto-generated method stub
  }

  protected String getTargetURL(final CommandAbstract _command)
      throws Exception {
    StringBuilder url = new StringBuilder();

    // always javascript (needed for faces..)

    if (_command.isSubmit()) {
      url.append("javascript:eFapsCommonSubmit(\"");
    } else {
      url.append("javascript:eFapsCommonOpenUrl(\"");
    }

    // add link
    if ((_command.getReference() != null)
        && (_command.getReference().length() > 0)) {
      url.append(_command.getReference());
    } else if ((_command.getTargetTable() != null)
        || (_command.getTargetForm() != null)
        || (_command.getTargetSearch() != null)
        || (_command.hasEvents(EventType.UI_COMMAND_EXECUTE))) {
      url.append("../common/Link.jsf?");
      // hack (no url found!)
    } else {
      return null;
    }

    // append always the command name
    url.append("command=").append(_command.getName());
    /*
     * // append oid if ((this.oid != null) && (this.oid.length() > 0)) {
     * url.append("&oid=").append(this.oid); }
     */
    // TODO append nodeId
    // append target
    /*
     * if (this.search) { url.append("&eFapsCallingCommand=").append(
     * Context.getThreadContext().getParameter("eFapsCallingCommand"))
     * .append("&search=").append(this.originalCommand.getName()).append(
     * "\",\"Replace"); } else {
     */url.append("\",\"");
    switch (_command.getTarget()) {
      case CommandAbstract.TARGET_CONTENT:
        url.append("Content");
      break;
      case CommandAbstract.TARGET_HIDDEN:
        url.append("eFapsFrameHidden");
      break;
      case CommandAbstract.TARGET_POPUP:
        url.append("popup");
        if ((_command.getWindowWidth() > 0) && (_command.getWindowHeight() > 0)) {
          url.append("\",\"").append(_command.getWindowWidth()).append("\",\"")
              .append(_command.getWindowHeight());
        }
      break;

      case CommandAbstract.TARGET_MODAL:
        url.append("eFapsFrameModal");
      break;
      default:
        if (_command.hasEvents(EventType.UI_COMMAND_EXECUTE)) {
          url.append("eFapsFrameHidden");
        } else {
          url.append("Content");
        }
    }
    // }

    url.append("\");");

    /*
     * if (_command.isAskUser()) { EFapsModalDialog modaldialog = new
     * EFapsModalDialog(_command, url.toString());
     * modalDialogs.add(modaldialog); return ("javascript:" +
     * modaldialog.getDialogVar() + ".show();"); } if (_command.getTarget() ==
     * CommandAbstract.TARGET_MODAL) {
     * url.append(";eFapsOpenFrameModal(").append(_command.getWindowHeight()).append(",")
     * .append(_command.getWindowWidth()).append(");"); }
     */

    return url.toString();
  }

}
