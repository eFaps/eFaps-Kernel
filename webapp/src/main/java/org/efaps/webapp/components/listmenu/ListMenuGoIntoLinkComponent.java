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

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.model.Model;

import org.efaps.webapp.components.listmenu.ListMenuPanel.Rows;
import org.efaps.webapp.models.MenuItemModel;

/**
 * @author jmo
 * @version $Id$
 */
public class ListMenuGoIntoLinkComponent extends AjaxLink {

  private static final long serialVersionUID = 1L;

  public ListMenuGoIntoLinkComponent(final String _id,
                                     final MenuItemModel _model) {
    super(_id, _model);

  }

  @Override
  public void onClick(final AjaxRequestTarget _target) {

    ListMenuPanel listmenupanel =
        (ListMenuPanel) this.findParent(ListMenuPanel.class);

    ListMenuPanel rootlistmenupanel = listmenupanel;

    while (rootlistmenupanel.findParent(ListMenuPanel.class) != null) {
      rootlistmenupanel =
          (ListMenuPanel) rootlistmenupanel.findParent(ListMenuPanel.class);
    }

    Rows row = (Rows) this.findParent(Rows.class);
    Rows rowparent = (Rows) row.findParent(Rows.class);
    while (rowparent.findParent(Rows.class) != null) {
      rowparent = (Rows) rowparent.findParent(Rows.class);
    }

    MenuItemModel model = (MenuItemModel) listmenupanel.getModel();
    model.previouslevel = model.getLevel();
    model.setLevel(0);
    for (Object child : model.getChilds()) {
      if (child instanceof MenuItemModel) {
        MenuItemModel item = (MenuItemModel) child;
        item.previouslevel = item.getLevel();
        item.setLevel(0);
      }
    }

    List<?> x = getChildModel(rootlistmenupanel);

    model.ancestor = new Model((Serializable) x);

    rowparent.removeAll();
    rowparent.setList(row.getList());

    _target.addComponent(rootlistmenupanel);

  }

  private List<?> getChildModel(final Component _child) {
    List<Object> model = null;

    if (_child instanceof MarkupContainer) {
      Iterator<Component> it = ((MarkupContainer) _child).iterator();
      int i = 0;
      if (_child instanceof Rows) {
        model = (List<Object>) _child.getModelObject();
        if (model.isEmpty()) {
          model = null;
        }
      }

      while (it.hasNext()) {
        List<?> model2 = getChildModel(it.next());
        if (model2 != null) {
          if (model != null) {
            if (!model.contains(model2)) {
              model.add(i + 1, model2);
            }
          } else {
            model = (List<Object>) model2;
          }
          if (model.isEmpty()) {
            model = null;
          }
        }
        i++;
      }
    }

    return model;
  }
}
