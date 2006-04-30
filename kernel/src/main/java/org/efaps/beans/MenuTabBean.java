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

/**
 * The bean is used for
 */
public class MenuTabBean extends MenuAbstractBean  {

  public MenuTabBean()  {
    super();
System.out.println("MenuTabBean.constructor");
  }

  public void finalize()  {
    super.finalize();
System.out.println("MenuTabBean.destructor");
  }

  /**
   * The instance method is called from {@link MenuAbstractBean.execute()}. The
   * method gets as menu <i>MyDesk</i>. The menu is used to get all commands
   * for which the user has access.
   *
   * @param _context  context for this request
   */
  protected void execute(Context _context) throws Exception  {
    setMenu(_context, Menu.get("MyDesk"));
  }
}
