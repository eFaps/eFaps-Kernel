/*
 * Copyright 2003 - 2007 The eFaps Team
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

package org.efaps.admin.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.builder.ToStringBuilder;

import org.efaps.admin.datamodel.Type;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;

/**
 * @author tmo
 * @version $Id: MenuAbstract.java 466 2006-10-20 15:15:42 +0000 (Fri, 20 Oct
 *          2006) tmo $
 */
abstract public class AbstractMenu extends AbstractCommand {

  /**
   * All sub commands or menus are store in the tree map. The tree map is used
   * to sort the commands / menus belonging to their id.
   *
   * @see #getCommands
   * @add
   */
  private final Map<Long, AbstractCommand> commands = new TreeMap<Long, AbstractCommand>();

  // ///////////////////////////////////////////////////////////////////////////

  /**
   * Constructor to set the id,uuid and  name of the menu object.
   *
   * @param _id
   *          id of the command to set
   * @param _name
   *          name of the command to set
   */
  protected AbstractMenu(final long _id, final String _uuid, final String _name) {
    super(_id, _uuid, _name);
  }

  /**
   * Adds a command or menu to this menu instance. The method must be specific
   * implemented by all menu implementations.
   *
   * @param _sortId
   *          id used to sort
   * @param _id
   *          id of the sub command / menu to add
   */
  abstract protected void add(final long _sortId, final long _id);

  /**
   * Add a command to the menu structure.
   *
   * @param _sortId
   *          id used to sort
   * @param _command
   *          command to add
   */
  public void add(final long _sortId, final AbstractCommand _command) {
    this.commands.put(_sortId, _command);
  }

  /**
   * Add all sub commands and menus of the given menu to this menu structure.
   *
   * @param _menu
   *          menu with sub structure
   */
  public void addAll(final AbstractMenu _menu) {
    this.commands.putAll(_menu.commands);
  }

  /**
   * Check, if the user of the context has access to this user interface object.
   * First, the instance method checks, if some acces configuration exists for
   * this menu instance object. If the user has access for this menu, it is
   * test, if the context user has access to minimum one sub command command /
   * menu. If yes, the user is allowed to access this menu instance, other the
   * user is not allowed to access this menu.
   *
   *
   * @return <i>true</i>if context user has access, otherwise <i>false</i> is
   *         returned
   * @throws EFapsException
   */
  @Override
  public boolean hasAccess() throws EFapsException {
    boolean ret = super.hasAccess();

    if (ret && getCommands().size() > 0) {
      ret = false;
      for (AbstractCommand cmd : getCommands()) {
        if (cmd.hasAccess()) {
          ret = true;
          break;
        }
      }
    }
    return ret;
  }


  /**
   * Returns all information from the menu as string.
   */
  @Override
  public String toString() {
 final   ToStringBuilder buf = new ToStringBuilder(this).appendSuper(super
        .toString());

    for (AbstractCommand cmd : getCommands()) {
      buf.append(" ").append(cmd);
    }
    return buf.toString();
  }

  /**
   * The method takes values of the {@link #commands} and returnes them as
   * {@link java.util.ArrayList}.
   *
   * @return the values of the {@link #commands} map instance as array list
   * @see #commands
   * @see #add(Command)
   * @see #add(Menu)
   */
  public List<AbstractCommand> getCommands() {
    return new ArrayList<AbstractCommand>(this.commands.values());
  }

  // ///////////////////////////////////////////////////////////////////////////

  /**
   * The instance method reads all needed information for this user interface
   * object. The method extends the original method, because the sub menus and
   * commands must be read.
   *
   * @param _context
   *          eFaps context for this request
   * @see #readFromDB4Childs
   */
  @Override
  protected void readFromDB() throws CacheReloadException {
    super.readFromDB();
    readFromDB4Childs();
  }

  /**
   * The instance method gets all sub menus and commands and adds them to this
   * menu instance via method {@link #add(long)}.
   *
   * @param _context
   *          eFaps context for this request
   * @see #readFromDB
   * @see #add(long)
   */
  private void readFromDB4Childs() throws CacheReloadException {
    try {
      final   Instance menuInst = new Instance(Type.get(EFapsClassName.MENU.name),
          getId());
      final   SearchQuery query = new SearchQuery();
      query.setExpand(menuInst, "Admin_UI_Menu2Command\\FromMenu");
      query.addSelect("ID");
      query.addSelect("ToCommand");
      query.executeWithoutAccessCheck();

      while (query.next()) {
        final     long commandId = ((Number) query.get("ToCommand")).longValue();
        final  long sortId = (Long) query.get("ID");
        add(sortId, commandId);
      }
    } catch (EFapsException e) {
      throw new CacheReloadException("could not read childs for menu " + "'"
          + getName() + "'", e);
    }
  }
}
