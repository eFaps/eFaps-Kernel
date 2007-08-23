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
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.HeaderContributor;
import org.wicketstuff.dojo.markup.html.container.DojoPanelContainer;

import org.efaps.webapp.models.EFapsApplicationSession;
import org.efaps.webapp.models.IMenuItemModel;

public class SideMenuPanel extends DojoPanelContainer {
  private static final long serialVersionUID = 1L;

  private final PageParameters parameters;

  public SideMenuPanel(String _id, PageParameters _parameters) {
    super(_id, "SideMenu");
    parameters = _parameters;
    try {
      super.setModel(new IMenuItemModel(_parameters.getString("command")));
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
  }

  
  
  
  @Override
  protected void onBeforeRender() {
    super.onBeforeRender();
    initialise();
  }




  private void initialise() {
    add(HeaderContributor.forCss(getClass(), "sidemenu.css"));

    SideMenuContainer menu =
        new SideMenuContainer("baseSideMenu",
            (IMenuItemModel) super.getModel(), parameters.getString("oid"));
    this.add(menu);
  }

  private Integer childId = 0;

  private String getNewChildID() {
    return "Sub_" + (childId++).toString();
  }

  public void insertSubMenu(PageParameters _parameters, AjaxRequestTarget target) {

    EFapsApplicationSession session =
        (EFapsApplicationSession) this.getSession();
    ListItemLinkComponent link = null;
    IMenuItemModel imenu;
   
    try {
      imenu = new IMenuItemModel(_parameters.getString("command"));
      link.getParent().add(
          new SideMenuContainer(getNewChildID(), imenu, _parameters
              .getString("oid")));
      target.addComponent(link.getParent().getParent().getParent());
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  
}
