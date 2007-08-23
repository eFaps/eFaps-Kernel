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

import org.efaps.webapp.components.AbstractParentMarkupContainer;
import org.efaps.webapp.models.IMenuItemModel;

public class SideMenuContainer extends AbstractParentMarkupContainer {
  private static final long serialVersionUID = 1L;

  public SideMenuContainer(String _id, PageParameters _parameters) {
    super(_id);
    try {
      super.setModel(new IMenuItemModel(_parameters.getString("command")));
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    // SideMenuPanel menu = (SideMenuPanel)
    // _link.findParent(SideMenuPanel.class);
    //
    // IMenuItemModel model = (IMenuItemModel) super.getModel();
    //
    // ListContainer root = new ListContainer(menu.getNewChildID());
    //
    // this.link.getParent().add(root);
    //
    // ListItemContainer rootitem = new ListItemContainer(menu.getNewChildID());
    // root.add(rootitem);
    // ListItemLinkComponent xitem =
    // new ListItemLinkComponent(menu.getNewChildID(), model, _parameters
    // .getString("oid"));
    // rootitem.add(xitem);
    //
    // rootitem.setOutputMarkupId(true);
    //
    // if (model.hasChilds()) {
    // ListContainer sub = new ListContainer("SubMenu" + menu.getNewChildID());
    //
    // rootitem.add(sub);
    // sub.setOutputMarkupId(true);
    // for (IMenuItemModel child : model.getChilds()) {
    // ListItemContainer subitem = new ListItemContainer(menu.getNewChildID());
    // sub.add(subitem);
    //
    // ListItemLinkComponent yitem =
    // new ListItemLinkComponent(menu.getNewChildID(), child, _parameters
    // .getString("oid"));
    // subitem.add(yitem);
    //
    // subitem.setOutputMarkupId(true);
    // }
    // }

  }

  private Integer childId = 0;

  private String getNewChildID() {
    return this.getId() + "_eFapsSideMenu_" + (childId++).toString();
  }

  public SideMenuContainer(String string, IMenuItemModel model, String oid) {
    super(string, model);

    ListContainer root = new ListContainer(getNewChildID());

    root.add(new SimpleAttributeModifier("class", "eFapsSideMenu"));
    this.add(root);
    
    ListItemContainer rootitem = new ListItemContainer(getNewChildID());
    root.add(rootitem);
    rootitem.add(new ListItemLinkComponent(getNewChildID(), model, oid));
    rootitem.add(new SimpleAttributeModifier("class", "eFapsSideMenu"));
    rootitem.setOutputMarkupId(true);
   
    if (model.hasChilds()) {
      ListContainer sub = new ListContainer("SubMenu" + getNewChildID());
      rootitem.add(sub);
      sub.add(new SimpleAttributeModifier("class", "eFapsSideMenuNested"));
      sub.setOutputMarkupId(true);

      for (IMenuItemModel child : model.getChilds()) {
        ListItemContainer subitem = new ListItemContainer(getNewChildID());
        sub.add(subitem);
        subitem.setOutputMarkupId(true);
        subitem
            .add(new SimpleAttributeModifier("class", "eFapsSideMenuNested"));
        subitem.add(new ListItemLinkComponent(getNewChildID(), child, oid));
      }
    }

  }
}
