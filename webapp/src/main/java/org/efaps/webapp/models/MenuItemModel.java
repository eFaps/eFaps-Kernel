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

import org.apache.wicket.model.IModel;

import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.ui.Command;
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
public class MenuItemModel implements IModel {

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
  public final List<MenuItemModel> childs = new ArrayList<MenuItemModel>();

  private final String oid;

  private final int target;

  private final UUID uuid;

  // ///////////////////////////////////////////////////////////////////////////
  // constructors / destructors

  public MenuItemModel(final String _name) throws Exception {
    this(Menu.get(_name), null);
  }

  // ///////////////////////////////////////////////////////////////////////////
  // constructors / destructors

  public MenuItemModel(final String _name, final String _oid) throws Exception {
    this(Menu.get(_name), _oid);
  }

  private MenuItemModel(final CommandAbstract _command, String _oid)
                                                                    throws Exception {
    this.image = _command.getIcon();

    String label = DBProperties.getProperty(_command.getLabel());
    if (_oid != null) {
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
    }
    this.label = label;
    this.description = "";
    this.oid = _oid;
    this.target = _command.getTarget();
    this.uuid = _command.getUUID();
    if (_command instanceof MenuAbstract) {
      for (CommandAbstract subCmd : ((MenuAbstract) _command).getCommands()) {
        if (subCmd.hasAccess()) {
          this.childs.add(new MenuItemModel(subCmd, _oid));
        }
      }
    }
  }

  public int getTarget() {
    return target;
  }

  public String getOid() {
    return this.oid;
  }

  public UUID getUUID() {
    return uuid;

  }

  public void setURL(String _url) {
    this.url = _url;
  }

  public List<MenuItemModel> getChilds() {
    return this.childs;
  }

  public boolean hasChilds() {
    return !this.childs.isEmpty();
  }

  public String getLabel() {
    return this.label;
  }

  public CommandAbstract getCommand() {
    CommandAbstract cmd = Command.get(this.uuid);
    if (cmd == null) {
      cmd = Menu.get(this.uuid);
    }
    return cmd;
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

}
