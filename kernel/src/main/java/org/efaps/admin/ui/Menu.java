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

package org.efaps.admin.ui;

import org.efaps.db.Context;
import org.efaps.util.EFapsException;

/**
 *
 */
public class Menu extends MenuAbstract  {

  /**
   * The static variable defines the class name in eFaps.
   */
  static public EFapsClassName EFAPS_CLASSNAME = EFapsClassName.MENU;

  /**
   * Constructor to set the id and name of the menu object.
   *
   * @param _id   id  of the command to set
   * @param _name name of the command to set
   */
  public Menu(Long _id, String _name)  {
    super(_id, _name);
  }

  /**
   * An sub command or menu with the given id is added to this menu.
   *
   * @param _context  eFaps context for this request
   * @param _id       command / menu id
   */
  protected void add(Context _context, long _id) throws Exception  {
    Command command = Command.get(_id);
    if (command != null)  {
      add(command);
    } else  {
      Menu subMenu = Menu.get(_id);
      add(subMenu);
    }
  }

  /////////////////////////////////////////////////////////////////////////////

  /**
   * Returns for given parameter <i>_id</i> the instance of class {@link Menu}.
   *
   * @param _id id to search in the cache
   * @return instance of class {@link Menu}
   * @see #getCache
   */
  static public Menu get(final long _id) throws EFapsException  {
    return getCache().get(_id);
  }

  /**
   * Returns for given parameter <i>_name</i> the instance of class
   * {@link Menu}.
   *
   * @param _name name to search in the cache
   * @return instance of class {@link Menu}
   * @see #getCache
   */
  static public Menu get(final String _name) throws EFapsException  {
    return getCache().get(_name);
  }

  /**
   * Static getter method for the type hashtable {@link #cache}.
   *
   * @return value of static variable {@link #cache}
   */
  static UserInterfaceObjectCache<Menu> getCache()  {
    return cache;
  }

  /**
   * Stores all instances of class {@link Menu}.
   *
   * @see #getCache
   */
  static final private UserInterfaceObjectCache<Menu> cache = new UserInterfaceObjectCache<Menu>(Menu.class);
}
