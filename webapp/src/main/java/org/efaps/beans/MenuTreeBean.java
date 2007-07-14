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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.ui.CommandAbstract;
import org.efaps.admin.ui.Menu;
import org.efaps.admin.ui.MenuAbstract;
import org.efaps.admin.ui.UserInterfaceObject;
import org.efaps.beans.valueparser.ValueParser;
import org.efaps.db.Context;
import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;


/**
 * @author tmo
 * @version $Id$
 * @todo description
 */
public class MenuTreeBean extends AbstractBean {

  /////////////////////////////////////////////////////////////////////////////
  // instance variables

  /**
   * The instance variable stores the menu label for this menu tree bean.
   * 
   * @see #setMenuLabel
   * @see #getMenuLabel
   */
  private String menuLabel = null;

  /**
   * The instance variable stores the menu holder instance object.
   *
   * @see #getMenuHolder
   * @see #MenuHolder
   */
  private MenuHolder menuHolder = null;

  /////////////////////////////////////////////////////////////////////////////
  // constructors / destructors

  public MenuTreeBean() throws EFapsException {
    super();
  }

  /////////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * Following steps are made from this method;
   * <ul>
   * <li>The method sets the menu from the type (setting 'Tree').</li>
   * <li>If for the type a icon is specified, this icon is set if no icon is
   * set for the menu.</li>
   * <li>The object value are substitued in the label of the menu.</li>
   * </ul>
   * 
   * @param _context
   *          eFaps context for this request
   */
  public void execute() throws Exception {
    Context context = Context.getThreadContext();
    Menu menu = getInstance().getType().getTreeMenu();
    if (menu == null) {
      throw new Exception("no tree menu defined for type "
          + getInstance().getType().getName());
    }
    setMenu(menu);

    if (getMenuHolder().getIcon() == null) {
      getMenuHolder().setIcon(getInstance().getType().getIcon());
    }

    // change menu label
    SearchQuery query = new SearchQuery();
    query.setObject(getInstance());
    ValueParser parser = new ValueParser(new StringReader(this.menuLabel));
    ValueList list = parser.ExpressionString();
    list.makeSelect(query);
    if (query.selectSize() > 0) {
      query.execute();
      if (query.next()) {
        this.menuLabel = list.makeString(query);
      }
      query.close();
    }
  }

  /**
   *
   * @param _context  context for this request
   * @param _menu     menu instance object
   * @see #execute()
   */
  protected void setMenu(final Menu _menu) throws Exception  {
    if (_menu.hasAccess())  {
      this.menuHolder = new MenuHolder(_menu);
    }
  }

  /////////////////////////////////////////////////////////////////////////////
  // getter / setter methods

  /**
   * This is the getter method for the instance variable {@link #menuLabel}.
   * 
   * @return value of instance variable {@link #menuLabel}
   * @see #menuLabel
   * @see #setMenuLabel
   */
  public String getMenuLabel() {
    return this.menuLabel;
  }

  /**
   * This is the setter method for the instance variable {@link #menuLabel}.
   * 
   * @param _menuLabel
   *          new value for instance variable {@link #menuLabel}
   * @see #menuLabel
   * @see #getMenuLabel
   */
  public void setMenuLabel(String _menuLabel) {
    this.menuLabel = DBProperties.getProperty(_menuLabel + ".Label");
  }

  /**
   * This is the getter method for the instance variable {@link #menuHolder}.
   *
   * @return value of instance variable {@link #menuHolder}
   * @see #menuHolder
   * @see #setMenuHolder
   */
  public MenuHolder getMenuHolder()  {
    return this.menuHolder;
  }

  /////////////////////////////////////////////////////////////////////////////
  // class used to hold one command

  /**
   * The class wraps commands. An instance of the command holder is only added
   * to an instance of {@link #MenuHolder}, if the context user is allowed to
   * access this command / menu.
   */
  static public class CommandHolder  {

    /**
     * The constructor only stores the given command.
     *
     * @param _context  context for this request
     * @param _sub      command or menu which is holded by this class
     * @see #sub
     */
    protected CommandHolder(CommandAbstract _sub)  {
      setSub(_sub);
      setIcon(_sub.getIcon());
    }

    /**
     * The instance method returns always <i>false</i>, because the holder is
     * only for commands. In {@link #MenuHolder.isMenu} the method is
     * overwritten (and the class {@link #MenuHolder} is used for Menus).
     *
     * @return always <i>false</i>
     */
    public boolean isMenu()  {
      return false;
    }

    ///////////////////////////////////////////////////////////////////////////

    /**
     * The instance variable stores the command / menu instance for which this
     * is instance is created.
     *
     * @see #getSub
     * @see #setSub
     */
    private CommandAbstract sub = null;

    /**
     * The instance variable stores the icon of this command. This is used to
     * override icon information from a derived menu bean.
     */
    private String icon = null;

    ///////////////////////////////////////////////////////////////////////////

    /**
     * This is the getter method for the instance variable {@link #sub}.
     *
     * @return value of instance variable {@link #sub}
     * @see #sub
     * @see #setSub
     */
    public CommandAbstract getSub()  {
      return this.sub;
    }

    /**
     * This is the setter method for the instance variable {@link #sub}.
     *
     * @param _sub  new value for instance variable {@link #sub}
     * @see #sub
     * @see #getSub
     */
    private void setSub(CommandAbstract _sub)  {
      this.sub = _sub;
    }

    /**
     * This is the getter method for the instance variable {@link #icon}.
     *
     * @return value of instance variable {@link #icon}
     * @see #icon
     * @see #setIcon
     */
    public String getIcon()  {
      return this.icon;
    }

    /**
     * This is the setter method for the instance variable {@link #icon}.
     *
     * @param _icon  new value for instance variable {@link #icon}
     * @see #icon
     * @see #getIcon
     */
    protected void setIcon(String _icon)  {
      this.icon = _icon;
    }
  }

  /////////////////////////////////////////////////////////////////////////////
  // class used to hold one menu

  /**
   * The class wraps a menu. An instance of the menu holder checks for all
   * sub commands / menus of the wrapped menu instance and adds all sub
   * commands / menus for which the context user is allowed to access.
   */
  static public class MenuHolder extends CommandHolder  {

    /**
     * The constructor adds all sub menus / commands to the bean for which
     * the context user has the right.
     *
     * @param _context  context for this request
     * @param _menu     menu instance object
     * @see #execute(Context)
     */
    public MenuHolder(MenuAbstract _menu)  {
      super(_menu);
      Iterator iter = _menu.getCommands().iterator();
      while (iter.hasNext())  {
        UserInterfaceObject obj = (UserInterfaceObject)iter.next();
        if (obj.hasAccess())  {
          if (obj instanceof MenuAbstract)  {
            getSubs().add(new MenuHolder((MenuAbstract)obj));
          } else  {
            getSubs().add(new CommandHolder((CommandAbstract)obj));
          }
        }
      }
    }

    /**
     * The instance method returns always <i>true</i>, because the holder is
     * for menus.
     *
     * @return always <i>true</i>
     */
    public boolean isMenu()  {
      return true;
    }

    ///////////////////////////////////////////////////////////////////////////

    /**
     * The instance variable holds the list of the sub tree commands and
     * menus.
     *
     * @see #getSubs
     */
    private List<CommandHolder> subs = new ArrayList<CommandHolder>();

    ///////////////////////////////////////////////////////////////////////////

    /**
     * This is the getter method for the instance variable {@link #subs}.
     *
     * @return value of instance variable {@link #subs}
     * @see #subs
     */
    public List<CommandHolder> getSubs()  {
      return this.subs;
    }
  }
}
