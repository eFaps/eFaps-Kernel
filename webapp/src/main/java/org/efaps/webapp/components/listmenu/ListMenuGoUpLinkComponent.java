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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.model.IModel;

import org.efaps.webapp.components.listmenu.ListMenuPanel.Rows;
import org.efaps.webapp.models.MenuItemModel;

public class ListMenuGoUpLinkComponent extends AjaxLink {

  private static final long serialVersionUID = 1L;

  public ListMenuGoUpLinkComponent(String id, IModel model) {
    super(id, model);
    // TODO Auto-generated constructor stub
  }

  @Override
  public void onClick(final AjaxRequestTarget _target) {
    ListMenuPanel listmenupanel =
        (ListMenuPanel) this.findParent(ListMenuPanel.class);

    Rows row = (Rows) this.findParent(Rows.class);
    MenuItemModel model = (MenuItemModel) this.getModel();

    model.setLevel(model.previouslevel);

    for (MenuItemModel item : model.getChilds()) {
      item.setLevel(item.previouslevel);
    }

    row.removeAll();

    row.setModel(model.getAncestor());

    _target.addComponent(listmenupanel);
    model.setAncestor(null);
  }
}
