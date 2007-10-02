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

package org.efaps.webapp.components.tree;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.PageMap;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.tree.Tree;
import org.apache.wicket.markup.html.link.IPageLink;
import org.apache.wicket.markup.html.link.InlineFrame;
import org.apache.wicket.markup.html.tree.ITreeState;

import org.efaps.admin.ui.CommandAbstract;
import org.efaps.webapp.components.listmenu.ListMenuUpdate;
import org.efaps.webapp.models.StructurBrowserModel;
import org.efaps.webapp.pages.ContentContainerPage;
import org.efaps.webapp.pages.WebFormPage;
import org.efaps.webapp.pages.WebTablePage;

public class StructurBrowserTree extends Tree {

  private static final long serialVersionUID = 1L;

  private static final ResourceReference CSS =
      new ResourceReference(StructurBrowserTree.class, "StructurTree.css");

  public StructurBrowserTree(String id, TreeModel model) {
    super(id, model);
    this.setRootLess(true);

    ITreeState treeState = this.getTreeState();
    treeState.collapseAll();
    treeState.addTreeStateListener(new AsyncronTreeUpdateListener());

  }

  @Override
  protected ResourceReference getCSS() {
    return CSS;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.apache.wicket.extensions.markup.html.tree.DefaultAbstractTree#newNodeLink(org.apache.wicket.MarkupContainer,
   *      java.lang.String, javax.swing.tree.TreeNode)
   */
  @Override
  protected MarkupContainer newNodeLink(final MarkupContainer _parent,
                                        final String _id, final TreeNode _node) {

    return newLink(_parent, _id, new ILinkCallback() {

      private static final long serialVersionUID = 1L;

      public void onClick(final AjaxRequestTarget _target) {
        StructurBrowserModel model =
            (StructurBrowserModel) ((DefaultMutableTreeNode) _node)
                .getUserObject();

        CommandAbstract cmd = model.getCommand();
        final PageParameters parameter = new PageParameters();
        parameter.add("oid", model.getOid());
        parameter.add("command", cmd.getUUID().toString());

        InlineFrame page;
        if (cmd.getTargetTable() != null) {
          page =
              new InlineFrame(ContentContainerPage.IFRAME_WICKETID, PageMap
                  .forName(ContentContainerPage.IFRAME_PAGEMAP_NAME),
                  new IPageLink() {

                    private static final long serialVersionUID = 1L;

                    public Page getPage() {
                      WebTablePage page = new WebTablePage(parameter);

                      // page.setListMenuKey(ListMenuAjaxLinkContainer.this.menukey);
                      return page;
                    }

                    public Class<WebTablePage> getPageIdentity() {
                      return WebTablePage.class;
                    }
                  });
        } else {
          page =
              new InlineFrame(ContentContainerPage.IFRAME_WICKETID, PageMap
                  .forName(ContentContainerPage.IFRAME_PAGEMAP_NAME),
                  new IPageLink() {

                    private static final long serialVersionUID = 1L;

                    public Page getPage() {
                      WebFormPage page = new WebFormPage(parameter);
                      // page.setListMenuKey(ListMenuAjaxLinkContainer.this.menukey);
                      return page;
                    }

                    public Class<WebFormPage> getPageIdentity() {
                      return WebFormPage.class;
                    }
                  });
        }

        InlineFrame component =
            (InlineFrame) getPage().get(
                ((ContentContainerPage) getPage()).getInlinePath());
        page.setOutputMarkupId(true);

        component.replaceWith(page);
        _target.addComponent(page.getParent());

        ListMenuUpdate.newMenu(_target, ((ContentContainerPage) getPage())
            .getListMenuKey(), parameter);

      }

    });
  }

}
