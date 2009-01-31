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
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.ui.wicket.components.tree;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.apache.wicket.IClusterable;
import org.apache.wicket.markup.html.tree.ITreeStateListener;

import org.efaps.ui.wicket.models.objects.UIStructurBrowser;
import org.efaps.ui.wicket.models.objects.UIStructurBrowser.BogusNode;

/**
 *
 * @author jmox
 * @version $Id$
 */
public class AsyncronTreeUpdateListener implements ITreeStateListener,
    IClusterable {

  /**
   * Needed for serialization.
   */
  private static final long serialVersionUID = 1L;

  /**
   * Method is called on expand of a node. The expand of the node is stored
   * in the model.
   *
   * @param _treenode   TreeNode to expand
   */
  public void nodeExpanded(final Object _treenode) {
    final TreeNode node = (TreeNode) _treenode;
    final UIStructurBrowser model =
        (UIStructurBrowser) ((DefaultMutableTreeNode) node).getUserObject();
    model.setExpanded(true);

    if (!node.isLeaf() && node.getChildAt(0) instanceof BogusNode) {
      model.addChildren((DefaultMutableTreeNode) node);
    }
  }

  /**
   * Not needed.
   */
  public void allNodesCollapsed() {
    // not needed here
  }

  /**
   * Not needed.
   */
  public void allNodesExpanded() {
    // not needed here
  }

  /**
   * Method is called on collapse of a node. The collapse of the node is stored
   * in the model.
   *
   * @param _treenode   TreeNode to expand
   */
  public void nodeCollapsed(final Object _treenode) {
    final TreeNode node = (TreeNode) _treenode;
    final UIStructurBrowser model =
      (UIStructurBrowser) ((DefaultMutableTreeNode) node).getUserObject();
    model.setExpanded(false);
  }

  /**
   * Not needed.
   * @param _obj Object
   */
  public void nodeSelected(final Object _obj) {
    // not needed here
  }

  /**
   * Not needed.
   * @param _obj Object
   */
  public void nodeUnselected(final Object _obj) {
    // not needed here
  }
}
