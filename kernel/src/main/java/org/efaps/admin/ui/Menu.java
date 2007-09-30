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

package org.efaps.admin.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.efaps.admin.datamodel.Type;


/**
 *
 * @author tmo
 * @version $Id$
 * @todo description
 */
public class Menu extends MenuAbstract  {

  /**
   * The static variable defines the class name in eFaps.
   */
  public static EFapsClassName EFAPS_CLASSNAME = EFapsClassName.MENU;

  /**
   * Logging instance used in this class.
   */
  private static final Logger LOG = LoggerFactory.getLogger(Menu.class);

  /**
   * Stores the mapping from type to tree menu.
   */
  private static Map<Type, Menu> TYPE2MENUS = new HashMap<Type, Menu>();

  /**
   * Constructor to set the id and name of the menu object.
   *
   * @param _id   id  of the command to set
   * @param _name name of the command to set
   */
  public Menu(final Long _id, final String _uuid, final String _name) {
    super(_id, _uuid, _name);
  }

  /**
   * An sub command or menu with the given id is added to this menu.
   *
   * @param _context  eFaps context for this request
   * @param _sortId   id used to sort
   * @param _id       command / menu id
   */
  @Override
  protected void add(long _sortId, long _id)  {
    Command command = Command.get(_id);
    if (command != null)  {
      add(_sortId, command);
    } else  {
      Menu subMenu = Menu.get(_id);
      add(_sortId, subMenu);
    }
  }

  /**
   * Sets the link properties for this object.
   * 
   * @param _linkType type of the link property
   * @param _toId     to id
   * @param _toType   to type
   * @param _toName   to name
   */
  @Override
  protected void setLinkProperty(final EFapsClassName _linkType,
                                 final long _toId,
                                 final EFapsClassName _toType,
                                 final String _toName)  throws Exception {
    switch (_linkType) {
      case LINK_MENUISTYPETREEFOR:
        Type type = Type.get(_toId);
        if (type == null)  {
          LOG.error("Menu '" + this.getName() + "' could not defined as type "
                    + "tree menu for type '" + _toName + "'! Type does not "
                    + "exists!");
        } else  {
          TYPE2MENUS.put(type, this);
        }
        break;
      default:
        super.setLinkProperty(_linkType, _toId, _toType, _toName);
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
  static public Menu get(final long _id)  {
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
  static public Menu get(final String _name)  {
    return getCache().get(_name);
  }
  
  /**
   * Returns for given parameter <i>UUID</i> the instance of class
   * {@link Menu}.
   *
   * @param _uuid UUID to search in the cache
   * @return instance of class {@link Menu}
   * @see #getCache
   */
  static public Menu get(final UUID _uuid){
    return getCache().get(_uuid);
  }
  
  /**
   * 
   * @param _type
   * @return
   * @todo remove old method to get type tree menu from type
   */
  public static Menu getTypeTreeMenu(final Type _type)  {
    Menu ret = TYPE2MENUS.get(_type);
    if (ret == null)  {
      LOG.error("Type Tree Menu for '" + _type.getName() + "' not defined correctly!");
      ret = _type.getTreeMenu();
    }
    return ret;
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
  static final private UserInterfaceObjectCache<Menu> cache
          = new UserInterfaceObjectCache<Menu>(Menu.class);
}
