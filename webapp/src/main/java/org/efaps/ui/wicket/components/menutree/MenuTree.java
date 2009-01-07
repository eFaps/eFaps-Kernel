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

package org.efaps.ui.wicket.components.menutree;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.PageMap;
import org.apache.wicket.PageParameters;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.Response;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.IPageLink;
import org.apache.wicket.markup.html.link.InlineFrame;
import org.apache.wicket.markup.html.tree.AbstractTree;
import org.apache.wicket.markup.html.tree.ITreeState;
import org.apache.wicket.model.Model;

import org.efaps.admin.ui.AbstractCommand;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.ui.wicket.EFapsSession;
import org.efaps.ui.wicket.behaviors.update.AbstractAjaxUpdateBehavior;
import org.efaps.ui.wicket.components.efapscontent.StaticImageComponent;
import org.efaps.ui.wicket.models.objects.UIMenuItem;
import org.efaps.ui.wicket.pages.content.form.FormPage;
import org.efaps.ui.wicket.pages.content.table.TablePage;
import org.efaps.ui.wicket.pages.contentcontainer.ContentContainerPage;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.resources.StaticHeaderContributor;

/**
 * @author jmox
 * @version $Id$
 */
public class MenuTree extends AbstractTree {

  private static final long serialVersionUID = 1L;

  public static final EFapsContentReference ICON_REMOVE =
      new EFapsContentReference(MenuTree.class, "Remove.gif");

  public static final EFapsContentReference ICON_GOINTO =
      new EFapsContentReference(MenuTree.class, "GoInto.gif");

  public static final EFapsContentReference ICON_GOUP =
      new EFapsContentReference(MenuTree.class, "GoUp.gif");

  public static final EFapsContentReference ICON_CHILDCLOSED =
      new EFapsContentReference(MenuTree.class, "ChildClosed.gif");

  public static final EFapsContentReference ICON_CHILDOPENED =
      new EFapsContentReference(MenuTree.class, "ChildOpened.gif");

  public static final EFapsContentReference CSS =
      new EFapsContentReference(MenuTree.class, "MenuTree.css");

  /**
   * this Instancevariable holds the key wich is used to retrieve a item of this
   * ListMenuPanel from the Map in the Session
   * {@link #org.efaps.ui.wicket.EFapsSession}
   */
  private final String menuKey;

  private final Map<String, DefaultMutableTreeNode> oidToNode =
      new HashMap<String, DefaultMutableTreeNode>();

  public MenuTree(final String _wicketId, final PageParameters _parameters,
                  final String _menukey) {
    super(_wicketId);

    final ITreeState treestate = this.getTreeState();
    treestate.expandAll();

    this.menuKey = _menukey;
    final UIMenuItem model =
        new UIMenuItem(UUID.fromString(_parameters.getString("command")),
            _parameters.getString("oid"));

    this.setDefaultModel(new Model<Serializable>((Serializable) model.getTreeModel()));

    add(StaticHeaderContributor.forCss(CSS));
    ((EFapsSession) this.getSession()).putIntoCache(this.menuKey, this);

    final DefaultMutableTreeNode rootNode =
        (DefaultMutableTreeNode) ((DefaultTreeModel) getDefaultModelObject())
            .getRoot();

    boolean noChildSelected = true;
    final Enumeration<?> newNodes = rootNode.children();

    while (newNodes.hasMoreElements()) {
      final DefaultMutableTreeNode newNode =
          (DefaultMutableTreeNode) newNodes.nextElement();
      final UIMenuItem newmodel = (UIMenuItem) newNode.getUserObject();
      if (newmodel.isDefaultSelected()) {
        getTreeState().selectNode(newNode, true);
        noChildSelected = false;
      }
    }
    if (noChildSelected) {
      getTreeState().selectNode(rootNode, true);
    }

    final AjaxUpdateBehavior update = new AjaxUpdateBehavior();
    this.add(update);
  }

  public MenuTree(final String _wicketId, final TreeModel _model,
                  final String _menukey) {
    super(_wicketId);
    this.menuKey = _menukey;
    this.setDefaultModel(new Model<Serializable>((Serializable) _model));

    final ITreeState treestate = this.getTreeState();
    treestate.expandAll();

    add(StaticHeaderContributor.forCss(CSS));
    ((EFapsSession) this.getSession()).putIntoCache(this.menuKey, this);

    final AjaxUpdateBehavior update = new AjaxUpdateBehavior();
    this.add(update);
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
        (DefaultMutableTreeNode) item.getDefaultModelObject();

    item.add(new AbstractBehavior() {

      private static final long serialVersionUID = 1L;

      @Override
      public void onComponentTag(final Component component, final ComponentTag tag) {
        super.onComponentTag(component, tag);
        if (getTreeState().isNodeSelected(node)) {
          tag.put("class", "eFapsMenuTreeRowSelected");
        } else {
          tag.put("class", "eFapsMenuTreeRow");
        }
      }
    });

    final UIMenuItem model = (UIMenuItem) node.getUserObject();
    if (model.isHeader()) {
      ((EFapsSession) this.getSession()).addUpdateBehaviors(model.getOid(),
          (AjaxUpdateBehavior) getBehaviors(AjaxUpdateBehavior.class).get(0));
      this.oidToNode.put(model.getOid(), node);
    }

    item.add(new Intendation("intend", level));

    final AjaxMenuTreeLink link = new AjaxMenuTreeLink("link", node);
    item.add(link);
    final Label label = new Label("label", model.getLabel());
    link.add(label);

    if (node.children().hasMoreElements()
        && !node.isRoot()
        && !model.isStepInto()) {
      final AjaxExpandLink expandLink = new AjaxExpandLink("expandLink", node);
      item.add(expandLink);
      if (getTreeState().isNodeExpanded(node)) {
        expandLink.add(new StaticImageComponent("expandIcon",
            ICON_CHILDOPENED));
      } else {
        expandLink.add(new StaticImageComponent("expandIcon",
            ICON_CHILDCLOSED));
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
        link.add(new StaticImageComponent("icon", imageUrl));
      }

      if (node.isRoot()) {
        item.add(new WebMarkupContainer("goIntolink").setVisible(false));
        item.add(new WebMarkupContainer("removelink").setVisible(false));
        item.add(new WebMarkupContainer("goUplink").setVisible(false));
      } else if (model.isStepInto()) {
        item.add(new WebMarkupContainer("goIntolink").setVisible(false));
        item.add(new WebMarkupContainer("removelink").setVisible(false));
        final AjaxGoUpLink goUplink = new AjaxGoUpLink("goUplink", node);
        item.add(goUplink);
        goUplink.add(new StaticImageComponent("goUpIcon", ICON_GOUP));
      } else {
        final AjaxGoIntoLink goIntolink =
            new AjaxGoIntoLink("goIntolink", node);
        item.add(goIntolink);
        goIntolink.add(new StaticImageComponent("goIntoIcon",
            ICON_GOINTO));

        final AjaxRemoveLink removelink =
            new AjaxRemoveLink("removelink", node);
        item.add(removelink);
        removelink.add(new StaticImageComponent("removeIcon",
            ICON_REMOVE));
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
    final String oid = _parameters.getString("oid");
    final UUID uuid = UUID.fromString(_parameters.getString("command"));
    final Enumeration<?> childs = node.children();
    boolean old = false;
    while (childs.hasMoreElements()) {

      final DefaultMutableTreeNode child =
          (DefaultMutableTreeNode) childs.nextElement();

      final UIMenuItem childmodel = (UIMenuItem) child.getUserObject();
      if (childmodel.getOid().equals(oid)
          && childmodel.getCommandUUID().equals(uuid)) {
        getTreeState().selectNode(child, true);
        old = true;
      }
    }
    if (!old) {
      final UIMenuItem model = new UIMenuItem(uuid, oid);
      final DefaultMutableTreeNode rootNode = model.getNode();
      node.add(rootNode);
      boolean noChildSelected = true;
      final Enumeration<?> newNodes = rootNode.children();
      while (newNodes.hasMoreElements()) {
        final DefaultMutableTreeNode newNode =
            (DefaultMutableTreeNode) newNodes.nextElement();
        final UIMenuItem newmodel = (UIMenuItem) newNode.getUserObject();
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

  public void changeContent(final UIMenuItem _model,
                            final AjaxRequestTarget _target) {

    final AbstractCommand cmd = _model.getCommand();
    final PageParameters para = new PageParameters();
    para.add("oid", _model.getOid());
    para.add("command", cmd.getUUID().toString());

    InlineFrame page = null;
    if (cmd.getTargetTable() != null) {
      page =
          new InlineFrame(ContentContainerPage.IFRAME_WICKETID, PageMap
              .forName(ContentContainerPage.IFRAME_PAGEMAP_NAME),
              new IPageLink() {

                private static final long serialVersionUID = 1L;

                public Page getPage() {
                  final TablePage page = new TablePage(para);
                  page.setMenuTreeKey(getMenuKey());
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
                  final FormPage page = new FormPage(para);
                  page.setMenuTreeKey(getMenuKey());
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
    protected void onComponentTagBody(final MarkupStream markupStream,
                                      final ComponentTag openTag) {
      final Response response = RequestCycle.get().getResponse();

      for (int i = this.level - 1; i >= 0; --i) {
        response.write("<td class=\"eFapsMenuTreeIntend\"><div></div></td>");
      }

    }

  }

  public class AjaxUpdateBehavior extends AbstractAjaxUpdateBehavior {

    private static final long serialVersionUID = 1L;

    @Override
    protected void respond(final AjaxRequestTarget _target) {
      final DefaultMutableTreeNode node = MenuTree.this.oidToNode.get(getOid());
      final DefaultTreeModel treemodel =
          (DefaultTreeModel) this.getComponent().getDefaultModel().getObject();
      final UIMenuItem model = (UIMenuItem) node.getUserObject();
      final MenuTree tree = (MenuTree) this.getComponent();
      if (getMode() == TargetMode.EDIT) {
        model.requeryLabel();
        treemodel.nodeChanged(node);
        tree.updateTree(_target);
      }

    }

    @Override
    public String getAjaxCallback() {
      String ret = "";
      if (getMode() == TargetMode.EDIT) {
        ret = getCallbackScript().toString();
      }
      return ret;
    }

  }
}
