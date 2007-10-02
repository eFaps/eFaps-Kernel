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
import javax.swing.tree.TreeNode;

import org.apache.wicket.IClusterable;
import org.apache.wicket.markup.html.tree.ITreeStateListener;

import org.efaps.webapp.models.StructurBrowserModel;
import org.efaps.webapp.models.StructurBrowserModel.BogusNode;

public class AsyncronTreeUpdateListener implements ITreeStateListener,
    IClusterable {

  private static final long serialVersionUID = 1L;

  public void allNodesCollapsed() {
    // not needed here
  }

  public void allNodesExpanded() {
    // not needed here
  }

  public void nodeCollapsed(final TreeNode _node) {
    // not needed here
  }

  public void nodeExpanded(final TreeNode _node) {
    if (!_node.isLeaf() && _node.getChildAt(0) instanceof BogusNode) {

      StructurBrowserModel model =
          (StructurBrowserModel) ((DefaultMutableTreeNode) _node)
              .getUserObject();
      model.addChildren((DefaultMutableTreeNode) _node);
    }

  }

  public void nodeSelected(final TreeNode _node) {
    // not needed here
  }

  public void nodeUnselected(final TreeNode _node) {
    // not needed here
  }

}
