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

package org.efaps.beans;

import org.efaps.admin.ui.Menu;
import org.efaps.db.Context;
import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;

/**
 * @author tmo
 * @version $Id: MenuTreeBean.java 675 2007-02-14 20:56:25 +0000 (Wed, 14 Feb
 *          2007) jmo $
 * @todo description
 */
public class MenuTreeBean extends MenuAbstractBean {

  // ///////////////////////////////////////////////////////////////////////////
  // instance variables

  /**
   * The instance variable stores the menu label for this menu tree bean.
   * 
   * @see #setMenuLabel
   * @see #getMenuLabel
   */
  private String menuLabel = null;

  // ///////////////////////////////////////////////////////////////////////////
  // constructors / destructors

  public MenuTreeBean() throws EFapsException {
    super();
    System.out.println("MenuTreeBean.constructor");
  }

  public void finalize() {
    super.finalize();
    System.out.println("MenuTreeBean.destructor");
  }

  // ///////////////////////////////////////////////////////////////////////////
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
  protected void execute(Context _context) throws Exception {
    Menu menu = getInstance().getType().getTreeMenu();
    if (menu == null) {
      throw new Exception("no tree menu defined for type "
          + getInstance().getType().getName());
    }
    setMenu(_context, menu);

    if (getMenuHolder().getIcon() == null) {
      getMenuHolder().setIcon(getInstance().getType().getIcon());
    }

    SearchQuery query = new SearchQuery();
    query.setObject(getInstance());
    query.addAllFromString(_context, getMenuLabel());
    query.execute();

    if (query.next()) {
      setMenuLabel(query.replaceAllInString(_context, getMenuLabel()));
    }

    query.close();
  }

  // ///////////////////////////////////////////////////////////////////////////

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
    this.menuLabel = _menuLabel;
  }

}
