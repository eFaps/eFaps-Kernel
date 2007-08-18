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
import org.apache.wicket.behavior.HeaderContributor;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.wicketstuff.dojo.markup.html.container.DojoPanelContainer;

import org.efaps.webapp.models.IMenuItemModel;

public class SideMenuPanel extends DojoPanelContainer {
  private static final long serialVersionUID = 1L;

  private Integer childId = 0;

  public SideMenuPanel(String _id, PageParameters _parameters) {
    super(_id, "SideMenu");

    try {
      super.setModel(new IMenuItemModel(_parameters.getString("command")));
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    initialise();
  }

  private String getNewChildID() {
    return (childId++).toString();
  }

  private void initialise() {
    IMenuItemModel model = (IMenuItemModel) super.getModel();

    add(HeaderContributor.forCss(getClass(), "sidemenu.css"));

    ListContainer root = new ListContainer("baseSideMenu");
    root.add(new SimpleAttributeModifier("class", "eFapsSideMenu"));
    this.add(root);

    ListItemContainer rootitem = new ListItemContainer(getNewChildID());
    root.add(rootitem);
    rootitem.add(new ListItemComponent(getNewChildID(), model.getLabel()));
    rootitem.add(new SimpleAttributeModifier("class", "eFapsSideMenu"));

    if (model.hasChilds()) {
      ListContainer sub = new ListContainer("SubMenu" + getNewChildID());
      sub.add(new SimpleAttributeModifier("class", "eFapsSideMenuNested"));
      rootitem.add(sub);
      for (IMenuItemModel child : model.getChilds()) {
        ListItemContainer subitem = new ListItemContainer(getNewChildID());
        sub.add(subitem);
        subitem
            .add(new SimpleAttributeModifier("class", "eFapsSideMenuNested"));
        subitem.add(new ListItemComponent(getNewChildID(), child.getLabel()));
      }
    }
  }

}
