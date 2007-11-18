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

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.wicket.Page;
import org.apache.wicket.PageMap;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.link.IPageLink;
import org.apache.wicket.markup.html.link.InlineFrame;

import org.efaps.admin.ui.CommandAbstract;
import org.efaps.ui.wicket.models.MenuItemModel;
import org.efaps.ui.wicket.pages.content.form.FormPage;
import org.efaps.ui.wicket.pages.content.table.TablePage;
import org.efaps.ui.wicket.pages.contentcontainer.ContentContainerPage;

/**
 * @author jmox
 * @version $Id$
 */
public class AjaxMenuTreeLink extends AjaxLink {

  private static final long serialVersionUID = 1L;

  private final DefaultMutableTreeNode node;

  public AjaxMenuTreeLink(final String _wicketId, DefaultMutableTreeNode _node) {
    super(_wicketId);
    this.node = _node;
  }

  @Override
  public void onClick(final AjaxRequestTarget _target) {
    MenuItemModel model = (MenuItemModel) this.node.getUserObject();
    final MenuTree menutree = (MenuTree) this.findParent(MenuTree.class);
    CommandAbstract cmd = model.getCommand();
    final PageParameters para = new PageParameters();
    para.add("oid", model.getOid());
    para.add("command", cmd.getUUID().toString());

    InlineFrame page = null;
    if (cmd.getTargetTable() != null) {
      page =
          new InlineFrame(ContentContainerPage.IFRAME_WICKETID, PageMap
              .forName(ContentContainerPage.IFRAME_PAGEMAP_NAME),
              new IPageLink() {

                private static final long serialVersionUID = 1L;

                public Page getPage() {
                  TablePage page = new TablePage(para);
                  page.setListMenuKey(menutree.getMenuKey());
                  return page;
                }

                public Class<TablePage> getPageIdentity() {
                  return TablePage.class;
                }
              });
    } else {
      page =
          new InlineFrame(ContentContainerPage.IFRAME_WICKETID, PageMap
              .forName(ContentContainerPage.IFRAME_PAGEMAP_NAME),
              new IPageLink() {

                private static final long serialVersionUID = 1L;

                public Page getPage() {
                  FormPage page = new FormPage(para);
                  page.setListMenuKey(menutree.getMenuKey());
                  return page;
                }

                public Class<FormPage> getPageIdentity() {
                  return FormPage.class;
                }
              });
    }

    InlineFrame component =
        (InlineFrame) getPage().get(
            ((ContentContainerPage) getPage()).getInlinePath());
    page.setOutputMarkupId(true);

    component.replaceWith(page);
    _target.addComponent(page.getParent());

    menutree.getTreeState().selectNode(this.node, true);
    menutree.updateTree(_target);
  }

}
