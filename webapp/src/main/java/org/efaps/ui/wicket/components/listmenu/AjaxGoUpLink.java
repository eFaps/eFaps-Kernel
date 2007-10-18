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

package org.efaps.ui.wicket.components.listmenu;

import java.util.List;

import org.apache.wicket.PageMap;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.link.InlineFrame;
import org.apache.wicket.model.IModel;

import org.efaps.admin.ui.CommandAbstract;
import org.efaps.ui.wicket.EFapsSession;
import org.efaps.ui.wicket.components.listmenu.ListMenuPanel.Rows;
import org.efaps.ui.wicket.components.listmenu.ListMenuPanel.StyleClassName;
import org.efaps.ui.wicket.models.MenuItemModel;
import org.efaps.ui.wicket.pages.content.form.FormPage;
import org.efaps.ui.wicket.pages.content.table.TablePage;
import org.efaps.ui.wicket.pages.contentcontainer.ContentContainerPage;

/**
 * @author jmox
 * @version $Id$
 *
 */
public class AjaxGoUpLink extends AbstractAjaxLink {

  private static final long serialVersionUID = 1L;

  public AjaxGoUpLink(String id, IModel model) {
    super(id, model);
  }

  @Override
  public void onClick(final AjaxRequestTarget _target) {
    ListMenuPanel listmenupanel =
        (ListMenuPanel) this.findParent(ListMenuPanel.class);
    ((EFapsSession) this.getSession()).removeFromCache(listmenupanel
        .getMenuKey());
    listmenupanel.setOutputMarkupId(true);
    // update the Content
    MenuItemModel model = (MenuItemModel) super.getModel();
    MenuItemModel rootModel =
        (MenuItemModel) ((List<?>) model.getAncestor().getObject()).get(0);
    CommandAbstract cmd = rootModel.getCommand();
    PageParameters para = new PageParameters();
    para.add("oid", rootModel.getOid());
    para.add("command", cmd.getUUID().toString());

    InlineFrame page;
    if (cmd.getTargetTable() != null) {
      page =
          new InlineFrame(ContentContainerPage.IFRAME_WICKETID, PageMap
              .forName(ContentContainerPage.IFRAME_PAGEMAP_NAME),
              TablePage.class, para);
    } else {
      page =
          new InlineFrame(ContentContainerPage.IFRAME_WICKETID, PageMap
              .forName(ContentContainerPage.IFRAME_PAGEMAP_NAME),
              FormPage.class, para);
    }

    InlineFrame component =
        (InlineFrame) getPage().get(
            ((ContentContainerPage) getPage()).getInlinePath());
    page.setOutputMarkupId(true);

    component.replaceWith(page);
    _target.addComponent(page.getParent());

    Rows row = (Rows) this.findParent(Rows.class);

    row.removeAll();

    row.setModel(model.getAncestor());

    _target.addComponent(listmenupanel);
    model.setAncestor(null);
  }

  @Override
  public StyleClassName getSelectedStyleClass() {
    return StyleClassName.GOUP_SELECTED;
  }

  @Override
  public StyleClassName getStyleClass() {
    return StyleClassName.GOUP;
  }
}
