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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.PageMap;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.extensions.markup.html.tree.DefaultAbstractTree;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.IPageLink;
import org.apache.wicket.markup.html.link.InlineFrame;
import org.apache.wicket.markup.html.tree.ITreeState;
import org.apache.wicket.model.AbstractReadOnlyModel;

import org.efaps.admin.ui.AbstractCommand;
import org.efaps.admin.ui.Menu;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.ui.wicket.EFapsSession;
import org.efaps.ui.wicket.behaviors.update.AbstractAjaxUpdateBehavior;
import org.efaps.ui.wicket.components.menutree.MenuTree;
import org.efaps.ui.wicket.models.objects.UIStructurBrowser;
import org.efaps.ui.wicket.models.objects.UIStructurBrowser.BogusNode;
import org.efaps.ui.wicket.pages.content.form.FormPage;
import org.efaps.ui.wicket.pages.content.table.TablePage;
import org.efaps.ui.wicket.pages.contentcontainer.ContentContainerPage;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.resources.StaticHeaderContributor;

/**
 * This class renders a Tree, which loads the children asynchron.<br>
 * The items of the tree consists of junction link, icon and label. An
 * additional arrow showing the direction of the child can be rendered depending
 * on a Tristate.
 *
 * @author jmox
 * @version $Id$
 */
public class StructurBrowserTree extends DefaultAbstractTree {

  /**
   * Needed for serialization.
   */
  private static final long serialVersionUID = 1L;

  /**
   * ResourceReference to the StyleSheet used for this Tree.
   */
  private static final EFapsContentReference CSS =
      new EFapsContentReference(StructurBrowserTree.class, "StructurTree.css");

  /**
   * Instance variable holding the Key to the MenuTree (needed to update it).
   */
  private final String listMenuKey;

  /**
   * This instance map contains the relation between an oid and a node. This is
   * used to update a treenode via the AjaxUpdateBehavior.
   *
   */
  private final Map<String, DefaultMutableTreeNode> instanceKey2Node
                                = new HashMap<String, DefaultMutableTreeNode>();

  /**
   * Constructor setting the WicketId, the Model and the key of the ListMenu.
   *
   * @param _wicketId     wicket id of this component
   * @param _model        model for this component
   * @param _listMenuKey  key to the list menu
   */
  public StructurBrowserTree(final String _wicketId, final TreeModel _model,
                             final String _listMenuKey) {
    super(_wicketId, _model);
    this.listMenuKey = _listMenuKey;
    this.add(StaticHeaderContributor.forCss(CSS));

    setRootLess(true);
    // we want a tree that is collapsed and updated asynchron
    final ITreeState treeState = getTreeState();
    treeState.collapseAll();
    treeState.addTreeStateListener(new AsyncronTreeUpdateListener());
    // add an behavior that allows update of nodes on events
    final AjaxUpdateBehavior update = new AjaxUpdateBehavior();
    this.add(update);
  }

  /**
   * Overwritten to deactivate.
   * @return null
   */
  @Override
  protected ResourceReference getCSS() {
    // return null here and set a own HeaderContributor, to be able to use
    // eFaps own CSSResourceReference
    return null;
  }

  /**
   * Method is used to get the icon for a node.
   *
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
    // if we have the model contains a icon render it, else just pass it on to
    // the superMethod
    Component ret;
    if (model.getImage() == null) {
      ret = super.newNodeIcon(_parent, _wicketId, _node);
    } else {
      ret = new WebMarkupContainer(_wicketId) {

        private static final long serialVersionUID = 1L;

        @Override
        protected void onComponentTag(final ComponentTag _tag) {
          super.onComponentTag(_tag);
          _tag.put("style",
                   "background-image: url('" + model.getImage() + "')");
        }
      };
    }
    return ret;
  }

  /**
   * Method creates a new node.
   * @param _parent   parent node
   * @param _wicketId wicket id for the new node
   * @param _node     related TreeNode
   * @return MarkupContainer
   */
  @Override
  protected MarkupContainer newNodeLink(final MarkupContainer _parent,
                                        final String _wicketId,
                                        final TreeNode _node) {
    final UIStructurBrowser model =
        (UIStructurBrowser) ((DefaultMutableTreeNode) _node).getUserObject();
    // add UpdateBehavior for thi oid to the Session
    ((EFapsSession) getSession()).addUpdateBehaviors(model.getInstanceKey(),
        (AjaxUpdateBehavior) getBehaviors(AjaxUpdateBehavior.class).get(0));
    // store the oid to Node Relation
    this.instanceKey2Node.put(model.getInstanceKey(),
                              (DefaultMutableTreeNode) _node);

    return newLink(_parent, _wicketId, new ILinkCallback() {

      private static final long serialVersionUID = 1L;

      public void onClick(final AjaxRequestTarget _target) {
        final UIStructurBrowser model =
            (UIStructurBrowser) ((DefaultMutableTreeNode) _node)
                .getUserObject();
        //get the standart Command
        AbstractCommand cmd = model.getCommand();

        //check if one of its childs is default selected
        if (cmd instanceof Menu) {
          for (final AbstractCommand childcmd : ((Menu) cmd).getCommands()) {
            if (childcmd.isDefaultSelected()) {
              cmd = childcmd;
              break;
            }
          }
        }
        final UUID commandUUID = cmd.getUUID();
        InlineFrame page;
        if (cmd.getTargetTable() != null) {
          page = new InlineFrame(ContentContainerPage.IFRAME_WICKETID, PageMap
              .forName(ContentContainerPage.IFRAME_PAGEMAP_NAME),
              new IPageLink() {

                private static final long serialVersionUID = 1L;

                public Page getPage() {
                  final TablePage page = new TablePage(commandUUID,
                                                       model.getInstanceKey());
                  page.setMenuTreeKey(StructurBrowserTree.this.listMenuKey);
                  return page;
                }

                public Class<TablePage> getPageIdentity() {
                  return TablePage.class;
                }
              });
        } else {
          page = new InlineFrame(ContentContainerPage.IFRAME_WICKETID, PageMap
              .forName(ContentContainerPage.IFRAME_PAGEMAP_NAME),
              new IPageLink() {

                private static final long serialVersionUID = 1L;

                public Page getPage() {
                  final FormPage page = new FormPage(commandUUID,
                                                     model.getInstanceKey());
                  page.setMenuTreeKey(StructurBrowserTree.this.listMenuKey);
                  return page;
                }

                public Class<FormPage> getPageIdentity() {
                  return FormPage.class;
                }
              });
        }

        final InlineFrame component =
            (InlineFrame) getPage().get(
                ((ContentContainerPage) getPage()).getInlinePath());
        page.setOutputMarkupId(true);

        component.replaceWith(page);
        _target.addComponent(page.getParent());

        final MenuTree menutree =
            (MenuTree) ((EFapsSession) getSession())
                .getFromCache(StructurBrowserTree.this.listMenuKey);

        final MenuTree newmenutree = new MenuTree(menutree.getId(),
                                                  model.getCommandUUID(),
                                                  model.getInstanceKey(),
                                                  menutree.getMenuKey());

        menutree.replaceWith(newmenutree);
        newmenutree.updateTree(_target);
      }
    });
  }

  /**
   * Populates the tree item. It creates all necessary components for the tree
   * to work properly.
   *
   * @param _item     item to populate
   * @param _level    level of the item
   */
  @Override
  protected void populateTreeItem(final WebMarkupContainer _item,
                                  final int _level) {
    final DefaultMutableTreeNode node =
        (DefaultMutableTreeNode) _item.getDefaultModelObject();

    _item.add(newIndentation(_item, "indent", node, _level));

    _item.add(newJunctionLink(_item, "link", "image", node));

    final WebComponent direction = new WebComponent("direction");
    _item.add(direction);

    final UIStructurBrowser model =
        (UIStructurBrowser) node.getUserObject();
    if (model.getDirection() == null) {
      direction.setVisible(false);
    } else if (model.getDirection()) {
      direction.add(new SimpleAttributeModifier("class", "directionDown"));
    } else {
      direction.add(new SimpleAttributeModifier("class", "directionUp"));
    }

    final MarkupContainer nodeLink = newNodeLink(_item, "nodeLink", node);
    _item.add(nodeLink);

    nodeLink.add(newNodeIcon(nodeLink, "icon", node));

    nodeLink.add(new Label("label", new AbstractReadOnlyModel<String>() {

      private static final long serialVersionUID = 1L;

      @Override
      public String getObject() {
        return renderNode(node);
      }
    }));

    // do distinguish between selected and unselected rows we add an
    // behavior
    // that modifies row css class.
    _item.add(new AbstractBehavior() {

      private static final long serialVersionUID = 1L;

      @Override
      public void onComponentTag(final Component _component,
                                 final ComponentTag _tag) {
        super.onComponentTag(_component, _tag);
        if (getTreeState().isNodeSelected(node)) {
          _tag.put("class", "row-selected");
        } else {
          _tag.put("class", "row");
        }
      }
    });
  }

  /**
   * This method is called for every node to get it's string representation.
   *
   * @param _node
   *                The tree node to get the string representation for
   * @return The string representation
   */
  protected String renderNode(final TreeNode _node) {
    return _node.toString();
  }

  /**
   * This class is used to add an UpdateBehavior to this tree.
   *
   * @author jmox
   * @version $Id$
   */
  public class AjaxUpdateBehavior extends AbstractAjaxUpdateBehavior {

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param _target AjaxRequestTarget
     */
    @Override
    protected void respond(final AjaxRequestTarget _target) {

      final DefaultMutableTreeNode node =
          StructurBrowserTree.this.instanceKey2Node.get(getInstanceKey());
      final DefaultTreeModel treemodel =
          (DefaultTreeModel) getComponent().getDefaultModel().getObject();
      final UIStructurBrowser model =
          (UIStructurBrowser) node.getUserObject();
      final StructurBrowserTree tree =
          (StructurBrowserTree) getComponent();
      // in case of edit, we just update the actual node
      if (getMode() == TargetMode.EDIT) {
        treemodel.nodeChanged(node);
        model.requeryLabel();
      }
      // in case of create or delete (unknown)
      if (getMode() == TargetMode.CREATE || getMode() == TargetMode.UNKNOWN) {
        // in case that we allready had childs
        if (node.getChildCount() > 0) {
          // the parentnode was allready expanded so add a new child and update
          // the whole tree
          if (!(node.getChildAt(0) instanceof BogusNode)) {
            node.removeAllChildren();
            model.resetModel();
            model.addChildren(node);
            tree.invalidateAll();
          }
        } else {
          // we had no childs yet, so we add a BogusNode (produces the
          // junctionlink)
          model.setParent(true);
          model.addBogusNode(node);
          treemodel.nodeChanged(node);
        }
      }
      tree.updateTree(_target);
    }
  }
}
