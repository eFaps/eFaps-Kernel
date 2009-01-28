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

/**
 * This class renders a Link wich is used to collapse and expand the ChildItems
 * of a Header inside a MenuTree.
 *
 * @author jmox
 * @version $Id:AjaxCollapseLink.java 1510 2007-10-18 14:35:40Z jmox $
 */
public class AjaxExpandLink extends AjaxLink<Object> {

  private static final long serialVersionUID = 1L;

  private final DefaultMutableTreeNode node;

  /**
   * Construtor setting the ID and the Node of this Component
   *
   * @param _id
   * @param _model
   */
  public AjaxExpandLink(final String _id, final DefaultMutableTreeNode _node) {
    super(_id);
    this.node = _node;
  }

  @Override
  public void onClick(final AjaxRequestTarget _target) {
    MenuTree menutree = (MenuTree) findParent(MenuTree.class);
    if (menutree.getTreeState().isNodeExpanded(this.node)) {
      menutree.getTreeState().collapseNode(this.node);
      menutree.nodeCollapsed(this.node);
    } else {
      menutree.getTreeState().expandNode(this.node);
      menutree.nodeExpanded(this.node);
    }

    ((DefaultTreeModel) menutree.getDefaultModelObject()).nodeChanged(this.node);

    menutree.updateTree(_target);
  }

}
