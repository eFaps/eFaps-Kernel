/*
 * Copyright 2005 The eFaps Team
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
 */

package org.efaps.beans;

import java.util.List;

import org.apache.myfaces.custom.navmenu.NavigationMenuItem;
import org.efaps.admin.ui.Menu;
import org.efaps.db.Context;

/**
 * The bean is used for the main tool bar on the main header JSP page.
 */
public class MenuMainToolBarBean extends MenuAbstractBean  {

  /**
   * The value stores the bean used to translate strings.
   */
  private ResourceBundleBean i18nBean = null;

  public MenuMainToolBarBean()  {
    super();
System.out.println("MenuMainToolBarBean.constructor");
  }

  public void finalize()  {
    super.finalize();
System.out.println("MenuMainToolBarBean.destructor");
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
  public List<NavigationMenuItem> getJSFMenu() throws Exception {
    execute(Context.getThreadContext());
    return JSFMapper.getJSFNavigationMenuItems(this.i18nBean, getMenuHolder());
  }

  /**
   * The instance method is called from {@link MenuAbstractBean.execute()}. The
   * method gets as menu <i>MainToolBar</i>. The menu is used to get all
   * commands for which the user has access.
   *
   * @param _context  context for this request
   * @param _menu     menu object (the value is <i>null</i>!)
   */
  protected void execute(Context _context) throws Exception  {
    setMenu(_context, Menu.get(_context, "MainToolBar"));
  }

  /**
   * This is the setter method for instance variable {@link #i18nBean}.
   *
   * @param _i18nBean  new value to set
   * @see #i18nBean
   */
  public void setI18nBean(final ResourceBundleBean _i18nBean)  {
    this.i18nBean = _i18nBean;
  }
}
