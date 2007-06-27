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

import java.util.List;
import java.util.Vector;

import org.apache.myfaces.custom.navmenu.NavigationMenuItem;

import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.ui.Command;
import org.efaps.admin.ui.CommandAbstract;
import org.efaps.admin.ui.Menu;
import org.efaps.admin.ui.MenuAbstract;
import org.efaps.db.Context;
import org.efaps.util.EFapsException;

/**
 * The bean is used for the main tool bar on the main header JSP page.
 *
 * @author tmo
 * @version $Id$
 */
public class ToolbarBean  {

  /////////////////////////////////////////////////////////////////////////////
  // instance variables

  /**
   *
   */
  private MenuAbstract menu = null;

  /**
   * Object id of the instance which is used to evalute the expressions.
   *
   * @see #getTitle
   * @see #setOid
   */
  private String oid = null;

  /**
   * Is the current toolbar sed from a search?
   */
  private boolean search = false;

  /**
   * Original command defining this toolbar menu.
   */
  private CommandAbstract originalCommand = null;

  /////////////////////////////////////////////////////////////////////////////
  // instance methods

  public List < NavigationMenuItem > getJSFMenu() throws Exception {
    return getJSFMenu(this.menu);
  }

    /**
   * This is the getter method for the jsf NavigationMenuItems which are parsed
   * from the instance variable {@link #menuHolder}.
   *
   * @return List of the NavigationMenuItems
   * @see org.efaps.beans.MenuAbstractBean#menuHolder
   * @see org.efaps.beans.MenuAbstractBean#setMenuHolder
   * @see org.efaps.beans.MenuAbstractBean#getMenuHolder
   */
  protected List < NavigationMenuItem > getJSFMenu(final MenuAbstract _menu) throws Exception {
System.out.println("----MenuMainToolBarBean.getJSFMenu");
      List < NavigationMenuItem > ret = new Vector < NavigationMenuItem > ();

      if (_menu != null)  {

        for (CommandAbstract command : _menu.getCommands())  {
          if (command.hasAccess())  {
            ret.add(getJSFNavigationMenuItem(command));
          }
        }
      }

      return ret;
  }

  protected NavigationMenuItem getJSFNavigationMenuItem(final CommandAbstract _command) throws Exception  {
    // initalise the NavigationMenuItem instance with mapped Label and raw
    // action
    NavigationMenuItem ret =
            new NavigationMenuItem(DBProperties.getProperty(_command.getLabel()),
                                   String.valueOf(_command.getAction()));
    // map the icon path
    if (_command.getIcon() != null)  {
      ret.setIcon("/.." + _command.getIcon());
    }

    // map the target url
    ret.setAction(getTargetURL(_command));

    if (_command instanceof MenuAbstract) {
      // map the sub items
      ret.setNavigationMenuItems(getJSFMenu((MenuAbstract) _command));
    }

    return ret;
  }

  protected String getTargetURL(final CommandAbstract _command)  {
    StringBuilder url = new StringBuilder();

    // always javascript (needed for faces..)
    url.append("javascript:");
    
    if(_command.isSubmit()){
      url.append("eFapsCommonSubmit(\"");
    }else{
      url.append("eFapsCommonOpenUrl(\"");
    }
      
   
    // add link
    if ((_command.getReference() != null) &&
        (_command.getReference().length() > 0))  {
      url.append(_command.getReference());
    } else if ((_command.getTargetTable() != null) ||
               (_command.getTargetForm() != null) ||
               (_command.getTargetSearch() != null) ||
               (_command.hasTrigger()))  {
      url.append("Link.jsf?");
// hack (no url found!)
} else  {
return null;
    }

    // append always the command name
    url.append("command=")
       .append(_command.getName());

    // append oid
    if ((this.oid != null) && (this.oid.length() > 0))  {
      url.append("&oid=").append(this.oid);
    }

// TODO append nodeId

    // append target
    if (this.search)  {
      url.append("&search=")
         .append(this.originalCommand.getName())
         .append("\",\"Replace\"");
    } else  {
      url.append("\",\"");
      switch (_command.getTarget())  {
        case CommandAbstract.TARGET_CONTENT:
          url.append("Content");
          break;
        case CommandAbstract.TARGET_HIDDEN:
          url.append("eFapsFrameHidden");
          break;
        case CommandAbstract.TARGET_POPUP:
          url.append("popup");
          if ((_command.getWindowWidth() > 0) &&
             (_command.getWindowHeight() > 0))  {
            url.append("\",\"")
               .append(_command.getWindowWidth())
               .append("\",\"")
               .append(_command.getWindowHeight());
          }
          break;
        default:
          if (_command.hasTrigger())  {
            url.append("eFapsFrameHidden");
          } else  {
            url.append("Content");
          }
      }
      url.append("\""); 
    }

      url.append(")"); 
    return url.toString();
  }

  public void setToolbarMenu(final String _name) throws Exception  {
    this.menu = Menu.get(_name);
  }

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
  public void setCommandName(final String _name) throws EFapsException  {
    this.originalCommand = getCommand(_name);

    if (this.originalCommand != null)  {

if (this.originalCommand.getTargetMode() == CommandAbstract.TARGET_MODE_SEARCH)  {
  Context context = Context.getThreadContext();
  this.originalCommand = getCommand(context.getParameter("search"));
}

if (this.originalCommand != null)  {
      if (this.originalCommand.getTargetMenu() != null)  {

        if (this.originalCommand.getTargetMenu().hasAccess())  {
          this.menu = this.originalCommand.getTargetMenu();
        }
      } else if (this.originalCommand.getTargetSearch() != null)  {

        if (this.originalCommand.getTargetSearch().hasAccess())  {
          this.menu = this.originalCommand.getTargetSearch();
          this.search = true;
        }
      }
}
    }
  }

  protected CommandAbstract getCommand(final String _name) throws EFapsException  {
    CommandAbstract command = Command.get(_name);
    if (command == null)  {
      command = Menu.get(_name);
    }
    return command;
  }

  /////////////////////////////////////////////////////////////////////////////
  // getter and setter methods

  /**
   * The instance method sets the object id which is used to evalute the
   * expression within the title.
   *
   * @param _oid  new object id to set
   * @see #oid
   */
  public void setOid(final String _oid)  {
      this.oid = _oid;
  }

}
