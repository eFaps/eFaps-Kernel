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
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.extensions.markup.html.tree.table.IColumn;
import org.apache.wicket.extensions.markup.html.tree.table.TreeTable;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.tree.ITreeState;
import org.apache.wicket.model.AbstractReadOnlyModel;

import org.efaps.admin.ui.Menu;
import org.efaps.admin.ui.AbstractCommand.Target;
import org.efaps.db.Instance;
import org.efaps.ui.wicket.models.objects.UIStructurBrowser;
import org.efaps.ui.wicket.pages.contentcontainer.ContentContainerPage;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.resources.StaticHeaderContributor;
import org.efaps.util.EFapsException;

/**
 * This class renders a TreeTable, which loads the children asynchron.<br>
 * The items of the tree consists of junction link, icon and label. An
 * additional arrow showing the direction of the child can be rendered depending
 * on a Tristate. The table shows the columns as defined in the model.
 *
 * @author jmox
 * @version $Id$
 */
public class StructurBrowserTreeTable extends TreeTable {

  /**
   * Needed for serialization.
   */
  private static final long serialVersionUID = 1L;

  /**
   * ResourceReference to the StyleSheet used for this TreeTable.
   */
  private static final EFapsContentReference CSS =
      new EFapsContentReference(StructurBrowserTreeTable.class,
          "StructurTreeTable.css");

  /**
   * Constructor.
   *
   * @param _wicketId   wicket id for this component
   * @param _treeModel  model
   * @param _columns    columns
   */
  public StructurBrowserTreeTable(final String _wicketId,
                                  final TreeModel _treeModel,
                                  final IColumn[] _columns) {
    super(_wicketId, _treeModel, _columns);
    this.add(StaticHeaderContributor.forCss(CSS));
    setRootLess(true);

    final ITreeState treeState = getTreeState();

    treeState.addTreeStateListener(new AsyncronTreeUpdateListener());

    final DefaultMutableTreeNode root
                                = (DefaultMutableTreeNode) _treeModel.getRoot();
    expandChildren(root);
  }

  /**
   * Recursive method that expands all children that should be expanded.
   *
   * @param _parent parent
   */
  private void expandChildren(final DefaultMutableTreeNode _parent) {
    for (int i = 0; i < _parent.getChildCount(); i++) {
      final DefaultMutableTreeNode child
                               = (DefaultMutableTreeNode) _parent.getChildAt(i);
      final UIStructurBrowser struturBrowser
                                    = (UIStructurBrowser) child.getUserObject();
      if (struturBrowser.isExpanded()) {
        getTreeState().expandNode(child);
        expandChildren(child);
      }
    }
  }
  /**
   * @return null
   */
  @Override
  protected ResourceReference getCSS() {
    // return null here and set a own HeaderContributor, to use eFaps own
    // CSSResourceReference
    return null;
  }

  /**
   * Method is called to create a new Icon.
   * @param _parent   parent node
   * @param _wicketId wicket id for the new node
   * @param _node     related TreeNode
   * @return Component
   */
  @Override
  protected Component newNodeIcon(final MarkupContainer _parent,
                                  final String _wicketId,
                                  final TreeNode _node) {
    final UIStructurBrowser model =
        (UIStructurBrowser) ((DefaultMutableTreeNode) _node).getUserObject();
    if (model.getImage() == null) {
      return super.newNodeIcon(_parent, _wicketId, _node);
    } else {
      return new WebMarkupContainer(_wicketId) {

        private static final long serialVersionUID = 1L;

        @Override
        protected void onComponentTag(final ComponentTag _tag) {
          super.onComponentTag(_tag);
          _tag.put("style",
                   "background-image: url('" + model.getImage() + "')");
        }
      };
    }
  }

  /**
   * Method to add a new Fragment.
   *
   * @param _parent   parent node
   * @param _wicketId wicket id for the new node
   * @param _node     related TreeNode
   * @param _level    level
   * @param _nodeCallback callback
   * @return Component
   */
  @Override
  protected Component newTreePanel(final MarkupContainer _parent,
                                   final String _wicketId,
                                   final TreeNode _node, final int _level,
                                   final IRenderNodeCallback _nodeCallback) {

    return new StructurBrowserTreeFragment(_wicketId, _node, _level,
        _nodeCallback);

  }

  /**
   * Method creates a new node
   * .
   * @param _parent   parent node
   * @param _wicketId wicket id for the new node
   * @param _node     related TreeNode
   * @return MarkupContainer
   */
  @Override
  protected MarkupContainer newNodeLink(final MarkupContainer _parent,
                                        final String _wicketId,
                                        final TreeNode _node) {

    return newLink(_parent, _wicketId, new ILinkCallback() {

      private static final long serialVersionUID = 1L;

      public void onClick(final AjaxRequestTarget _target) {
        Instance instance = null;
        final UIStructurBrowser model =
            (UIStructurBrowser) ((DefaultMutableTreeNode) _node)
                .getUserObject();

        if (model.getInstanceKey() != null) {
          Menu menu = null;
          try {
            instance = model.getInstance();
            menu = Menu.getTypeTreeMenu(instance.getType());
          } catch (final Exception e) {
            throw new RestartResponseException(new ErrorPage(e));
          }
          if (menu == null) {
            final EFapsException excep =
                new EFapsException(this.getClass(), "newNodeLink.noTreeMenu",
                    instance.getType().getName());
            throw new RestartResponseException(new ErrorPage(excep));
          }
          ContentContainerPage page;
          if (model.getTarget() == Target.POPUP) {
            page = new ContentContainerPage(menu.getUUID(),
                                            model.getInstanceKey());
          } else {
            page = new ContentContainerPage(getPage().getPageMap(),
                                            menu.getUUID(),
                                            model.getInstanceKey(),
                                            true);
          }
          setResponsePage(page);
        }
      }
    });
  }

  /**
   * This class renders a Fragment of the TreeTable, representing a Node
   * including the junctionlink, the icon etc.
   *
   */
  private class StructurBrowserTreeFragment extends Panel {

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param _wicketId       wicket id for this component
     * @param _node           node
     * @param _level          level
     * @param _nodeCallback   callback
     */
    public StructurBrowserTreeFragment(final String _wicketId,
                                      final TreeNode _node,
                                      final int _level,
                                      final IRenderNodeCallback _nodeCallback) {
      super(_wicketId);

      add(newIndentation(this, "indent", _node, _level));

      add(newJunctionLink(this, "link", "image", _node));

      final WebComponent direction = new WebComponent("direction");
      add(direction);
      final UIStructurBrowser model =
          (UIStructurBrowser) ((DefaultMutableTreeNode) _node)
              .getUserObject();
      if (model.getDirection() == null) {
        direction.setVisible(false);
      } else if (model.getDirection()) {
        direction.add(new SimpleAttributeModifier("class", "directionDown"));
      } else {
        direction.add(new SimpleAttributeModifier("class", "directionUp"));
      }
      final MarkupContainer nodeLink = newNodeLink(this, "nodeLink", _node);
      add(nodeLink);

      nodeLink.add(newNodeIcon(nodeLink, "icon", _node));

      nodeLink.add(new Label("label", new AbstractReadOnlyModel<String>() {

        private static final long serialVersionUID = 1L;

        /**
         * @see org.apache.wicket.model.AbstractReadOnlyModel#getObject()
         */
        @Override
        public String getObject() {
          return _nodeCallback.renderNode(_node);
        }
      }));
    }
  }
}
