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

package org.efaps.webapp.components.table;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.PageMap;
import org.apache.wicket.PageParameters;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.list.ListItem;

import org.efaps.admin.ui.Menu;
import org.efaps.db.Instance;
import org.efaps.webapp.components.EFapsContainerComponent;
import org.efaps.webapp.components.sidemenu.ListMenuLinkComponent;
import org.efaps.webapp.components.sidemenu.ListMenuPanel;
import org.efaps.webapp.models.EFapsApplicationSession;
import org.efaps.webapp.wicket.MainPage;
import org.efaps.webapp.wicket.WebFormPage;
import org.efaps.webapp.wicket.WebTablePage;

/**
 * @author jmo
 * @version $Id$
 */
public class CellAjaxLinkComponent extends AjaxLink {
  private static final long serialVersionUID = 1L;

  private final String label;

  private final String oid;

  public CellAjaxLinkComponent(String id, String _oid, String _label) {
    super(id);
    this.label = _label;
    this.oid = _oid;

  }

  @Override
  protected void onComponentTag(final ComponentTag tag) {
    tag.setName("a");
    super.onComponentTag(tag);
  }

  @Override
  protected void onComponentTagBody(MarkupStream _markupStream,
      ComponentTag _openTag) {
    super.replaceComponentTagBody(_markupStream, _openTag, this.label);
  }

  @Override
  public void onClick(AjaxRequestTarget target) {
    Instance instance = null;

    if (this.oid != null) {
      instance = new Instance(oid);
    }
    Menu menu;

    try {
      menu = instance.getType().getTreeMenu();

      PageParameters u = new PageParameters();
      u.add("command", menu.getName());
      u.add("oid", this.oid);

      EFapsContainerComponent page;
      if (menu.getTargetTable() != null) {

        page =
            new EFapsContainerComponent("eFapsContentContainer",
                WebTablePage.class, u);
      } else {
        page =
            new EFapsContainerComponent("eFapsContentContainer",
                WebFormPage.class, u);
      }

      EFapsApplicationSession session =
          (EFapsApplicationSession) this.getSession();

      Page parentpage =
          PageMap.forName(MainPage.INLINEFRAMENAME).get(
              session.getContentContainerId(),
              session.getContentContainerVersion());

      Component x =
          parentpage
              .get("eFapsSplitContainer:containerrechts:eFapsContentContainer");
      x.replaceWith(page);
      target.addComponent(page);

      ListMenuPanel sidemenu =
          (ListMenuPanel) parentpage.get("eFapsSplitContainer:eFapsSideMenu");

      ListMenuLinkComponent comp = ((EFapsApplicationSession)(Session.get())).getSideMenuSelected();
      MarkupContainer y = comp.findParent(ListItem.class);
      ListMenuPanel newmenu =new ListMenuPanel("nested",u);
y.addOrReplace(newmenu) ;   
target.addComponent(newmenu);
      
      
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
