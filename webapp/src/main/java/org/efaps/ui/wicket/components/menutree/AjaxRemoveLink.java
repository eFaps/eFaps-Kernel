/*
 * Copyright 2003 - 2009 The eFaps Team
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

package org.efaps.ui.wicket.components.menutree;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.efaps.ui.wicket.models.objects.UIMenuItem;

/**
 * This Class renders a Link wich removes a Child from a MenuTree
 *
 * @author jmox
 * @version $Id:AjaxRemoveLink.java 1510 2007-10-18 14:35:40Z jmox $
 */
public class AjaxRemoveLink extends AjaxLink<Object> {

  private static final long serialVersionUID = 1L;

  private final DefaultMutableTreeNode node;

  /**
   * Construtor setting the ID and the Node of this Component
   *
   * @param _id
   * @param _model
   */
  public AjaxRemoveLink(final String _id, final DefaultMutableTreeNode _node) {
    super(_id);
    this.node = _node;
  }

  @Override
  public void onClick(final AjaxRequestTarget _target) {
    final MenuTree menutree = (MenuTree) findParent(MenuTree.class);
    final DefaultMutableTreeNode parent =
        (DefaultMutableTreeNode) this.node.getParent();
    final DefaultMutableTreeNode selected =
        (DefaultMutableTreeNode) menutree.getTreeState().getSelectedNodes()
            .iterator().next();
    boolean selectParent = false;
    if (this.node.isNodeDescendant(selected)) {
      selectParent = true;
    }
    menutree.getTreeState().selectNode(parent, true);

    ((DefaultTreeModel) menutree.getDefaultModelObject())
        .removeNodeFromParent(this.node);

    if (selectParent) {
      menutree.getTreeState().selectNode(parent, true);
      menutree.changeContent((UIMenuItem) parent.getUserObject(), _target);
    } else {
      menutree.getTreeState().selectNode(selected, true);
      _target.addComponent(menutree.getNodeComponent(parent));
    }

    menutree.updateTree(_target);

  }

}
