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

package org.efaps.ui.wicket.components.tree;

import java.util.HashMap;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.PageMap;
import org.apache.wicket.PageParameters;
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
import org.efaps.admin.ui.AbstractCommand.TargetMode;
import org.efaps.ui.wicket.EFapsSession;
import org.efaps.ui.wicket.behaviors.update.AbstractAjaxUpdateBehavior;
import org.efaps.ui.wicket.components.menutree.MenuTree;
import org.efaps.ui.wicket.models.StructurBrowserModel;
import org.efaps.ui.wicket.models.StructurBrowserModel.BogusNode;
import org.efaps.ui.wicket.pages.content.form.FormPage;
import org.efaps.ui.wicket.pages.content.table.TablePage;
import org.efaps.ui.wicket.pages.contentcontainer.ContentContainerPage;

/**
 * This class renders a Tree, wich loads the childs asynchron.<br>
 * The items of the tree consists of junction link, icon and label. An aditional
 * arrow showing the direction of the child can be rendered depending on a
 * Tristate.
 *
 * @author jmox
 * @version $Id:StructurBrowserTree.java 1510 2007-10-18 14:35:40Z jmox $
 */
public class StructurBrowserTree extends DefaultAbstractTree {

  private static final long serialVersionUID = 1L;

  /**
   * ResourceReference to the StyleSheet used for this Tree
   */
  private static final ResourceReference CSS =
      new ResourceReference(StructurBrowserTree.class, "StructurTree.css");

  /**
   * instance variable holding the Key to the MenuTree (needed to update it)
   */
  private final String listMenuKey;

  /**
   * this instance map contains the relation between an oid and a node. This is
   * used to update a treenode via the AjaxUpdateBehavior
   *
   * @see {@link #org.efaps.ui.wicket.components.tree.StructurBrowserTree$AjaxUpdateBehavior}
   */
  private final Map<String, DefaultMutableTreeNode> oidToNode =
      new HashMap<String, DefaultMutableTreeNode>();

  /**
   * Constructor setting the WicketId, the Model and the key of the ListMenu
   *
   * @param _wicketId
   * @param _model
   * @param _listmenukey
   */
  public StructurBrowserTree(final String _wicketId, final TreeModel _model,
                             final String _listmenukey) {
    super(_wicketId, _model);
    this.listMenuKey = _listmenukey;
    this.setRootLess(true);
    // we want a tree that is collapsed and updated asynchron
    final ITreeState treeState = this.getTreeState();
    treeState.collapseAll();
    treeState.addTreeStateListener(new AsyncronTreeUpdateListener());
    // add an behavior that allows update of nodes on events
    final AjaxUpdateBehavior update = new AjaxUpdateBehavior();
    this.add(update);
  }

  /*
   * (non-Javadoc)
   *
   * @see org.apache.wicket.extensions.markup.html.tree.DefaultAbstractTree#getCSS()
   */
  @Override
  protected ResourceReference getCSS() {
    return CSS;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.apache.wicket.extensions.markup.html.tree.DefaultAbstractTree#newNodeIcon(org.apache.wicket.MarkupContainer,
   *      java.lang.String, javax.swing.tree.TreeNode)
   */
  @Override
  protected Component newNodeIcon(final MarkupContainer _parent,
                                  final String _wicketId, final TreeNode _node) {
    final StructurBrowserModel model =
        (StructurBrowserModel) ((DefaultMutableTreeNode) _node).getUserObject();
    // if we have the model contains a icon render it, else just pass it on to
    // the superMethod
    Component ret;
    if (model.getImage() == null) {
      ret = super.newNodeIcon(_parent, _wicketId, _node);
    } else {
      ret = new WebMarkupContainer(_wicketId) {

        private static final long serialVersionUID = 1L;

        @Override
        protected void onComponentTag(ComponentTag tag) {
          super.onComponentTag(tag);
          tag.put("style", "background-image: url('" + model.getImage() + "')");
        }
      };
    }
    return ret;
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
    final StructurBrowserModel model =
        (StructurBrowserModel) ((DefaultMutableTreeNode) _node).getUserObject();
    // add UpdateBehavior for thi oid to the Session
    ((EFapsSession) this.getSession()).addUpdateBehaviors(model.getOid(),
        (AjaxUpdateBehavior) getBehaviors(AjaxUpdateBehavior.class).get(0));
    // store the oid to Node Relation
    this.oidToNode.put(model.getOid(), (DefaultMutableTreeNode) _node);

    return newLink(_parent, _id, new ILinkCallback() {

      private static final long serialVersionUID = 1L;

      public void onClick(final AjaxRequestTarget _target) {
        final StructurBrowserModel model =
            (StructurBrowserModel) ((DefaultMutableTreeNode) _node)
                .getUserObject();

        AbstractCommand cmd = model.getCommand();

        if (cmd instanceof Menu) {
          for (AbstractCommand childcmd : ((Menu) cmd).getCommands()) {
            if (childcmd.isDefaultSelected()) {
              cmd = childcmd;
              break;
            }
          }
        }

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
                      final TablePage page = new TablePage(parameter);
                      page.setListMenuKey(StructurBrowserTree.this.listMenuKey);
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
                      final FormPage page = new FormPage(parameter);
                      page.setListMenuKey(StructurBrowserTree.this.listMenuKey);
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

        final PageParameters parameter2 = new PageParameters();
        parameter2.add("oid", model.getOid());
        parameter2.put("command", model.getCommand().getUUID().toString());

        final MenuTree menutree =
            (MenuTree) ((EFapsSession) getSession())
                .getFromCache(StructurBrowserTree.this.listMenuKey);

        final MenuTree newmenutree =
            new MenuTree(menutree.getId(), parameter2, menutree.getMenuKey());

        menutree.replaceWith(newmenutree);
        newmenutree.updateTree(_target);

      }

    });
  }

  /**
   * Populates the tree item. It creates all necesary components for the tree to
   * work properly.
   *
   * @param _item
   * @param _level
   */
  @Override
  protected void populateTreeItem(final WebMarkupContainer _item,
                                  final int _level) {
    final DefaultMutableTreeNode node =
        (DefaultMutableTreeNode) _item.getModelObject();

    _item.add(newIndentation(_item, "indent", node, _level));

    _item.add(newJunctionLink(_item, "link", "image", node));

    final WebComponent direction = new WebComponent("direction");
    _item.add(direction);

    final StructurBrowserModel model =
        (StructurBrowserModel) node.getUserObject();
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

    nodeLink.add(new Label("label", new AbstractReadOnlyModel() {

      private static final long serialVersionUID = 1L;

      @Override
      public Object getObject() {
        return renderNode(node);
      }
    }));

    // do distinguish between selected and unselected rows we add an
    // behavior
    // that modifies row css class.
    _item.add(new AbstractBehavior() {

      private static final long serialVersionUID = 1L;

      @Override
      public void onComponentTag(Component component, ComponentTag tag) {
        super.onComponentTag(component, tag);
        if (getTreeState().isNodeSelected(node)) {
          tag.put("class", "row-selected");
        } else {
          tag.put("class", "row");
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
   * This class is used to add an UpdateBehavior to this tree
   *
   * @author jmox
   * @version $Id$
   */
  public class AjaxUpdateBehavior extends AbstractAjaxUpdateBehavior {

    private static final long serialVersionUID = 1L;

    @Override
    protected void respond(final AjaxRequestTarget _target) {

      final DefaultMutableTreeNode node =
          StructurBrowserTree.this.oidToNode.get(getOid());
      final DefaultTreeModel treemodel =
          (DefaultTreeModel) this.getComponent().getModel().getObject();
      final StructurBrowserModel model =
          (StructurBrowserModel) node.getUserObject();
      final StructurBrowserTree tree =
          (StructurBrowserTree) this.getComponent();
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
