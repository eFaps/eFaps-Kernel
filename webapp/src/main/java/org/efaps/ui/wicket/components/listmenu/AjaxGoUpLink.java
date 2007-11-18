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
 * Revision:        $Rev:1510 $
 * Last Changed:    $Date:2007-10-18 09:35:40 -0500 (Thu, 18 Oct 2007) $
 * Last Changed By: $Author:jmox $
 */

package org.efaps.ui.wicket.components.listmenu;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;

import org.efaps.ui.wicket.models.MenuItemModel;

/**
 * @author jmox
 * @version $Id:AjaxGoUpLink.java 1510 2007-10-18 14:35:40Z jmox $
 */
public class AjaxGoUpLink extends AjaxLink {

  private static final long serialVersionUID = 1L;

  private final DefaultMutableTreeNode node;

  /**
   * Construtor setting the ID and the Node of this Component
   *
   * @param _id
   * @param _model
   */
  public AjaxGoUpLink(final String _id, final DefaultMutableTreeNode _node) {
    super(_id);
    this.node = _node;
  }

  @Override
  public void onClick(final AjaxRequestTarget _target) {
    MenuTree menutree = (MenuTree) findParent(MenuTree.class);

    // update the Content
    MenuItemModel model = (MenuItemModel) this.node.getUserObject();
    model.setStepInto(false);
    MenuTree newMenuTree =
        new MenuTree(menutree.getId(),
            new DefaultTreeModel(model.getAncestor()), menutree.getMenuKey());

    menutree.replaceWith(newMenuTree);

    newMenuTree.updateTree(_target);
    // MenuItemModel rootModel =
    // (MenuItemModel) ((List<?>) model.getAncestor().getObject()).get(0);
    // CommandAbstract cmd = rootModel.getCommand();
    // PageParameters para = new PageParameters();
    // para.add("oid", rootModel.getOid());
    // para.add("command", cmd.getUUID().toString());
    //
    // InlineFrame page;
    // if (cmd.getTargetTable() != null) {
    // page =
    // new InlineFrame(ContentContainerPage.IFRAME_WICKETID, PageMap
    // .forName(ContentContainerPage.IFRAME_PAGEMAP_NAME),
    // TablePage.class, para);
    // } else {
    // page =
    // new InlineFrame(ContentContainerPage.IFRAME_WICKETID, PageMap
    // .forName(ContentContainerPage.IFRAME_PAGEMAP_NAME),
    // FormPage.class, para);
    // }
    //
    // InlineFrame component =
    // (InlineFrame) getPage().get(
    // ((ContentContainerPage) getPage()).getInlinePath());
    // page.setOutputMarkupId(true);
    //
    // component.replaceWith(page);
    // _target.addComponent(page.getParent());
    //
    // Rows row = (Rows) this.findParent(Rows.class);
    //
    // row.removeAll();
    //
    // row.setModel(model.getAncestor());
    //
    // _target.addComponent(listmenupanel);
    // model.setAncestor(null);
  }

}
