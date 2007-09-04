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

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.model.IModel;

import org.efaps.webapp.components.listmenu.ListMenuPanel.Rows;
import org.efaps.webapp.models.MenuItemModel;

/**
 * @author jmo
 * @version $Id$
 * 
 */
public class ListMenuCollapseLinkComponent extends AjaxLink {

  private static final long serialVersionUID = 1L;

  private boolean visible = false;

  /**
   * @param id
   */
  public ListMenuCollapseLinkComponent(String id, final IModel _model) {
    super(id, _model);
  }

  @Override
  protected void onComponentTag(ComponentTag tag) {
    super.onComponentTag(tag);
    MenuItemModel model = (MenuItemModel) super.getModel();
    if (model.hasChilds() && (this.findParent(ListItem.class) != null)) {
      int padding =
          model.getLevel()
              * ((ListMenuPanel) this.findParent(ListMenuPanel.class))
                  .getPadding();
      tag.put("style", "padding-left:" + padding + "px;");

    }
  }

  @Override
  public void onClick(AjaxRequestTarget _target) {

    Rows rows = (Rows) this.findParent(Rows.class);
    ListItem item = (ListItem) this.findParent(ListItem.class);
    Iterator<?> it = rows.iterator();
    while (it.hasNext()) {
      Component child = (Component) it.next();
      if (child.equals(item)) {
      } else {
        child.setVisible(this.visible);

      }
    }
    this.visible = !this.visible;
    _target.addComponent(rows.getParent());
  }

}
