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

package org.efaps.ui.wicket.components.menutree;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.UUID;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.Response;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.behavior.HeaderContributor;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.tree.AbstractTree;
import org.apache.wicket.model.Model;

import org.efaps.ui.wicket.EFapsSession;
import org.efaps.ui.wicket.components.StaticImageComponent;
import org.efaps.ui.wicket.models.MenuItemModel;

/**
 * @author jmox
 * @version $Id$
 */
public class MenuTree extends AbstractTree {

  private static final long serialVersionUID = 1L;

  public static final ResourceReference ICON_MENUTREEREMOVE =
      new ResourceReference(MenuTree.class, "eFapsMenuTreeRemove.gif");

  public static final ResourceReference ICON_MENUTREEGOINTO =
      new ResourceReference(MenuTree.class, "eFapsMenuTreeGoInto.gif");

  public static final ResourceReference ICON_MENUTREEGOUP =
      new ResourceReference(MenuTree.class, "eFapsMenuTreeGoUp.gif");

  public static final ResourceReference ICON_MENUTREECHILDCLOSED =
      new ResourceReference(MenuTree.class, "eFapsMenuTreeChildClosed.gif");

  public static final ResourceReference ICON_MENUTREECHILDOPENED =
      new ResourceReference(MenuTree.class, "eFapsMenuTreeChildOpened.gif");

  /**
   * this Instancevariable holds the key wich is used to retrieve a item of this
   * ListMenuPanel from the Map in the Session
   * {@link #org.efaps.ui.wicket.EFapsSession}
   */
  private final String menuKey;

  public MenuTree(final String _wicketId, final PageParameters _parameters,
                  final String _menukey) {

    super(_wicketId);
    this.menuKey = _menukey;
    final MenuItemModel model =
        new MenuItemModel(UUID.fromString(_parameters.getString("command")),
            _parameters.getString("oid"));

    this.setModel(new Model((Serializable) model.getTreeModel()));

    add(HeaderContributor.forCss(getClass(), "MenuTree.css"));
    ((EFapsSession) this.getSession()).putIntoCache(this.menuKey, this);

    DefaultMutableTreeNode rootNode =
        (DefaultMutableTreeNode) ((DefaultTreeModel) getModelObject())
            .getRoot();

    boolean noChildSelected = true;
    Enumeration<?> newNodes = rootNode.children();

    while (newNodes.hasMoreElements()) {
      DefaultMutableTreeNode newNode =
          (DefaultMutableTreeNode) newNodes.nextElement();
      MenuItemModel newmodel = (MenuItemModel) newNode.getUserObject();
      if (newmodel.isDefaultSelected()) {
        getTreeState().selectNode(newNode, true);
        noChildSelected = false;
      }
    }
    if (noChildSelected) {
      getTreeState().selectNode(rootNode, true);
    }
  }

  public MenuTree(final String _wicketId, final TreeModel _model,
                  final String _menukey) {
    super(_wicketId);
    this.menuKey = _menukey;
    this.setModel(new Model((Serializable) _model));

    add(HeaderContributor.forCss(getClass(), "MenuTree.css"));
    ((EFapsSession) this.getSession()).putIntoCache(this.menuKey, this);

  }

  /**
   * Populates the tree item. It creates all necesary components for the tree to
   * work properly.
   *
   * @param item
   * @param level
   */
  @Override
  protected void populateTreeItem(final WebMarkupContainer item, final int level) {
    final DefaultMutableTreeNode node =
        (DefaultMutableTreeNode) item.getModelObject();

    item.add(new AbstractBehavior() {

      private static final long serialVersionUID = 1L;

      @Override
      public void onComponentTag(Component component, ComponentTag tag) {
        super.onComponentTag(component, tag);
        if (getTreeState().isNodeSelected(node)) {
          tag.put("class", "eFapsMenuTreeRowSelected");
        } else {
          tag.put("class", "eFapsMenuTreeRow");
        }
      }
    });

    MenuItemModel model = (MenuItemModel) node.getUserObject();
    item.add(new Intendation("intend", level));

    final AjaxMenuTreeLink link = new AjaxMenuTreeLink("link", node);
    item.add(link);
    Label label = new Label("label", model.getLabel());
    link.add(label);

    if (node.children().hasMoreElements()
        && !node.isRoot()
        && !model.isStepInto()) {
      AjaxExpandLink expandLink = new AjaxExpandLink("expandLink", node);
      item.add(expandLink);
      if (getTreeState().isNodeExpanded(node)) {
        expandLink.add(new Image("expandIcon", ICON_MENUTREECHILDOPENED));
      } else {
        expandLink.add(new Image("expandIcon", ICON_MENUTREECHILDCLOSED));
      }
    } else {
      item.add(new WebMarkupContainer("expandLink").setVisible(false));
    }

    if (model.isHeader()) {
      label.add(new SimpleAttributeModifier("class", "eFapsMenuTreeHeader"));

      String imageUrl = model.getImage();
      if (imageUrl == null) {
        imageUrl = model.getTypeImage();
      }
      if (imageUrl == null) {
        link.add(new WebMarkupContainer("icon").setVisible(false));
      } else {
        link.add(new StaticImageComponent("icon", new Model(imageUrl)));
      }

      if (node.isRoot()) {
        item.add(new WebMarkupContainer("goIntolink").setVisible(false));
        item.add(new WebMarkupContainer("removelink").setVisible(false));
        item.add(new WebMarkupContainer("goUplink").setVisible(false));
      } else if (model.isStepInto()) {
        item.add(new WebMarkupContainer("goIntolink").setVisible(false));
        item.add(new WebMarkupContainer("removelink").setVisible(false));
        AjaxGoUpLink goUplink = new AjaxGoUpLink("goUplink", node);
        item.add(goUplink);
        goUplink.add(new Image("goUpIcon", ICON_MENUTREEGOUP));
      } else {
        AjaxGoIntoLink goIntolink = new AjaxGoIntoLink("goIntolink", node);
        item.add(goIntolink);
        goIntolink.add(new Image("goIntoIcon", ICON_MENUTREEGOINTO));

        AjaxRemoveLink removelink = new AjaxRemoveLink("removelink", node);
        item.add(removelink);
        removelink.add(new Image("removeIcon", ICON_MENUTREEREMOVE));
        item.add(new WebMarkupContainer("goUplink").setVisible(false));
      }
    } else {
      label.add(new SimpleAttributeModifier("class", "eFapsMenuTreeItem"));
      link.add(new WebMarkupContainer("icon").setVisible(false));
      item.add(new WebMarkupContainer("goIntolink").setVisible(false));
      item.add(new WebMarkupContainer("removelink").setVisible(false));
      item.add(new WebMarkupContainer("goUplink").setVisible(false));
    }

  }

  /**
   * This is the getter method for the instance variable {@link #menuKey}.
   *
   * @return value of instance variable {@link #menuKey}
   */
  public String getMenuKey() {
    return this.menuKey;
  }

  public void addChildMenu(final PageParameters _parameters,
                           final AjaxRequestTarget _target) {

    final DefaultMutableTreeNode node =
        (DefaultMutableTreeNode) getTreeState().getSelectedNodes().iterator()
            .next();
    String oid = _parameters.getString("oid");
    UUID uuid = UUID.fromString(_parameters.getString("command"));
    Enumeration<?> childs = node.children();
    boolean old = false;
    while (childs.hasMoreElements()) {

      DefaultMutableTreeNode child =
          (DefaultMutableTreeNode) childs.nextElement();

      MenuItemModel childmodel = (MenuItemModel) child.getUserObject();
      if (childmodel.getOid().equals(oid)
          && childmodel.getCommandUUID().equals(uuid)) {
        getTreeState().selectNode(child, true);
        old = true;
      }
    }
    if (!old) {
      final MenuItemModel model = new MenuItemModel(uuid, oid);
      DefaultMutableTreeNode rootNode = model.getNode();
      node.add(rootNode);
      boolean noChildSelected = true;
      Enumeration<?> newNodes = rootNode.children();
      while (newNodes.hasMoreElements()) {
        DefaultMutableTreeNode newNode =
            (DefaultMutableTreeNode) newNodes.nextElement();
        MenuItemModel newmodel = (MenuItemModel) newNode.getUserObject();
        if (newmodel.isDefaultSelected()) {
          getTreeState().selectNode(newNode, true);
          noChildSelected = false;
        }
      }
      if (noChildSelected) {
        getTreeState().selectNode(rootNode, true);
      }
    }
    getTreeState().expandNode(node);
    updateTree(_target);
  }

  public class Intendation extends WebMarkupContainer {

    private static final long serialVersionUID = 1L;

    private final int level;

    public Intendation(final String _wicketId, final int _level) {
      super(_wicketId);
      this.level = _level;
      setRenderBodyOnly(true);
    }

    @Override
    protected void onComponentTagBody(MarkupStream markupStream,
                                      ComponentTag openTag) {
      Response response = RequestCycle.get().getResponse();

      for (int i = this.level - 1; i >= 0; --i) {
        response.write("<td class=\"eFapsMenuTreeIntend\"><div></div></td>");
      }

    }

  }
}
