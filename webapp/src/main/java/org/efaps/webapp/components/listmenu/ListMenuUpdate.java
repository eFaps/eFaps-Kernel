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

package org.efaps.webapp.components.listmenu;

import java.util.Iterator;
import java.util.List;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.PageParameters;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;

import org.efaps.admin.ui.Menu;
import org.efaps.webapp.EFapsSession;
import org.efaps.webapp.models.MenuItemModel;

public class ListMenuUpdate {

  public static void update(AjaxRequestTarget _target, Menu _menu,
      PageParameters _parameters, final String _oid) {

    ListMenuLinkComponent comp =
        ((EFapsSession) (Session.get())).getSideMenuSelected();
    MarkupContainer listitem = comp.findParent(ListItem.class);

    Iterator<?> childs = listitem.iterator();
    ListMenuPanel newmenu = null;
    ListView view = null;
    int level = ((MenuItemModel) comp.getModel()).getLevel();
    while (childs.hasNext()) {

      Object child = childs.next();
      if (child instanceof ListMenuPanel) {
        newmenu = (ListMenuPanel) child;
        break;
      }
    }

    childs = newmenu.iterator();
    while (childs.hasNext()) {
      Object child = childs.next();
      if (child instanceof ListView) {
        view = (ListView) child;
        break;
      }
    }
    boolean old = false;
    if (view != null) {
      List<Object> list = view.getList();
      level++;
      for (Object item : view.getList()) {
        if (item instanceof MenuItemModel) {
          item = (MenuItemModel) item;

          if (((MenuItemModel) item).getOid().equals(_oid)
              && ((MenuItemModel) item).getUUID().equals(_menu.getUUID())) {
            old = true;
            break;
          }

        }

      }
      if (!old) {
        MenuItemModel model;
        try {
          model = new MenuItemModel(_menu.getName(), _oid);
          model.setLevel(level);
          for (MenuItemModel item : model.getChilds()) {
            item.setLevel(level);
          }
          list.add(model);
          list.add(model.getChilds());
        } catch (Exception e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }

      }
    } else {
      newmenu = new ListMenuPanel("nested", _parameters, level + 1);
      listitem.replace(newmenu);
    }

    _target.addComponent(newmenu);

  }
}
