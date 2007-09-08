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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.PageParameters;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;

import org.efaps.admin.ui.Menu;
import org.efaps.webapp.EFapsSession;
import org.efaps.webapp.components.listmenu.ListMenuPanel.Rows;
import org.efaps.webapp.models.MenuItemModel;

/**
 * @author jmo
 * @version $Id$
 */
public class ListMenuUpdate {

  public static void update(final AjaxRequestTarget _target,
                            final String _menukey, final Menu _menu,
                            final PageParameters _parameters, final String _oid) {

    Component comp =
        ((EFapsSession) (Session.get())).getSelectedComponent(_menukey);
    deselectItem(_menukey, comp, _target);
    MarkupContainer listitem = comp.findParent(ListItem.class);

    Iterator<?> childs = listitem.iterator();
    ListMenuPanel newmenu = null;
    Rows view = null;
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
        view = (Rows) child;
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
          model = new MenuItemModel(_menu.getUUID(), _oid);
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

      } else {
        setSelectedItem(_menukey, newmenu.getHeaderComponent(), _target);
      }
    } else {
      newmenu = new ListMenuPanel("nested", _menukey, _parameters, level + 1);

      listitem.replace(newmenu);
    }

    _target.addComponent(newmenu);

  }

  public static void setSelectedItem(final String _menukey,
                                     final Component _component) {
    setSelectedItem(_menukey, _component, null, true);

  }

  public static void setSelectedItem(final String _menukey,
                                     final Component _component,
                                     final AjaxRequestTarget _target) {
    setSelectedItem(_menukey, _component, _target, true);
  }

  public static void setSelectedItem(final String _menukey,
                                     final Component _component,
                                     final AjaxRequestTarget _target,
                                     final boolean _deselect) {
    _component.add(new AttributeModifier("class", new Model(
        "eFapsListMenuSelected")));
    EFapsSession session = (EFapsSession) (Session.get());
    Component previous = session.getSelectedComponent(_menukey);

    if (previous != null && !previous.equals(_component) && _deselect) {
      if (previous instanceof ListMenuLinkComponent) {
        deselectItem(_menukey, previous, _target);
      }
    }
    session.setSelectedComponent(_menukey, _component);
    if (_target != null) {
      _target.addComponent(_component);
    }
  }

  public static void deselectItem(final String _menukey,
                                  final Component _component,
                                  final AjaxRequestTarget _target) {
    String styleClass =
        ((ListMenuLinkComponent) _component).getDefaultStyleClass();
    _component.add(new AttributeModifier("class", new Model(styleClass)));
    if (_target != null) {
      _target.addComponent(_component);
    }
    ((EFapsSession) (Session.get())).removeSelectedComponent(_menukey);
  }

  /**
   * checks if the previous is child of the Component _parent, and if true will
   * not deselect it
   * 
   * @param _menukey
   * @param _component
   * @param _target
   * @param _parent
   */
  public static void setSelectedItem(final String _menukey,
                                     final Component _component,
                                     final AjaxRequestTarget _target,
                                     final Component _parent) {

    Component selected =
        ((EFapsSession) Session.get()).getSelectedComponent(_menukey);

    boolean deselect = true;
    if (selected != null) {
      if (_parent instanceof MarkupContainer) {
        Iterator it = ((MarkupContainer) _parent).iterator();
        while (it.hasNext() && deselect) {
          deselect = isChild(it.next(), selected);
        }
      }
    }

    setSelectedItem(_menukey, _component, _target, deselect);
  }

  private static boolean isChild(final Object _child, final Component _parent) {
    boolean ret = true;

    if (_child instanceof Component) {

      if (_child instanceof MarkupContainer) {
        Iterator it = ((MarkupContainer) _child).iterator();
        while (it.hasNext() && ret) {
          ret = isChild(it.next(), _parent);
        }
      }
      if (_child.equals(_parent)) {
        return false;
      }
    }

    return ret;
  }

}
