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

import org.apache.wicket.PageMap;
import org.apache.wicket.PageParameters;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.link.InlineFrame;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.model.IModel;

import org.efaps.admin.ui.CommandAbstract;
import org.efaps.webapp.EFapsSession;
import org.efaps.webapp.models.MenuItemModel;
import org.efaps.webapp.pages.WebFormPage;
import org.efaps.webapp.pages.WebTablePage;

/**
 * @author jmo
 * @version $Id$
 */
public class ListMenuLinkComponent extends AjaxLink {

  private static final long serialVersionUID = 1L;

  private final String menukey;

  public ListMenuLinkComponent(final String _id, final String _menukey,
                               final IModel _model) {
    super(_id, _model);
    this.menukey = _menukey;
  }

  @Override
  protected void onComponentTag(ComponentTag tag) {
    super.onComponentTag(tag);
    MenuItemModel model = (MenuItemModel) super.getModel();
    int padding = model.getLevel() * 4 + 1;
    if (((MenuItemModel) super.getModel()).hasChilds()
        && (this.findParent(ListItem.class) != null)) {
      tag.put("style", "padding-left:" + padding + "px;");
      tag.put("class", "eFapsListMenuHeader");
    } else {
      tag.put("style", "padding-left:" + padding + "px;");
      tag.put("class", "eFapsListMenuItemLink");
    }

  }

  @Override
  public void onClick(final AjaxRequestTarget _target) {

    MenuItemModel model = (MenuItemModel) super.getModel();
    CommandAbstract cmd = model.getCommand();
    PageParameters para = new PageParameters();
    para.add("oid", model.getOid());
    para.add("command", cmd.getName());

    InlineFrame page;
    if (cmd.getTargetTable() != null) {
      page =
          new InlineFrame("eFapsContentContainerFrame", PageMap
              .forName("content"), WebTablePage.class, para);
    } else {
      page =
          new InlineFrame("eFapsContentContainerFrame", PageMap
              .forName("content"), WebFormPage.class, para);
    }
    InlineFrame component =
        (InlineFrame) getPage()
            .get(
                "eFapsSplitContainer:containerrechts:aktParent:eFapsContentContainerFrame");
    page.setOutputMarkupId(true);

    component.replaceWith(page);
    _target.addComponent(page.getParent());
    ((EFapsSession) (Session.get()))
        .setListMenuSelectedItem(this.menukey, this);
  }

}
