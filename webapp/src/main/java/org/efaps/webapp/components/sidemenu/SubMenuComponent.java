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

package org.efaps.webapp.components.sidemenu;

import org.apache.wicket.PageParameters;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.WebComponent;

import org.efaps.webapp.models.IMenuItemModel;

public class SubMenuComponent extends WebComponent {
  private static final long serialVersionUID = 1L;

  private ListItemLinkComponent link;

  public SubMenuComponent(ListItemLinkComponent _link,
                          PageParameters _parameters) {
    super("new");
    this.link = _link;
    try {
      super.setModel(new IMenuItemModel(_parameters.getString("command")));
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    SideMenuPanel menu = (SideMenuPanel) _link.findParent(SideMenuPanel.class);

    IMenuItemModel model = (IMenuItemModel) super.getModel();

    ListContainer root = new ListContainer(menu.getNewChildID());

    this.link.getParent().add(root);

    ListItemContainer rootitem = new ListItemContainer(menu.getNewChildID());
    root.add(rootitem);
    ListItemLinkComponent xitem =
        new ListItemLinkComponent(menu.getNewChildID(), model, _parameters
            .getString("oid"));
    rootitem.add(xitem);
    xitem.add(new SimpleAttributeModifier("style", "padding-left: 15px;"
        + " background-color: #036;  " + " font-weight: bold;"));

    rootitem.add(new SimpleAttributeModifier("class", "eFapsSideMenu"));
    rootitem.setOutputMarkupId(true);

    if (model.hasChilds()) {
      ListContainer sub = new ListContainer("SubMenu" + menu.getNewChildID());

      rootitem.add(sub);
      sub.setOutputMarkupId(true);
      for (IMenuItemModel child : model.getChilds()) {
        ListItemContainer subitem = new ListItemContainer(menu.getNewChildID());
        sub.add(subitem);

        ListItemLinkComponent yitem =
            new ListItemLinkComponent(menu.getNewChildID(), child, _parameters
                .getString("oid"));
        subitem.add(yitem);
        yitem.add(new SimpleAttributeModifier("style", " padding-left:  20px;"
            + " font-weight: normal;"));
        subitem.setOutputMarkupId(true);
      }
    }

  }
}
