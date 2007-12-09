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
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.tree.Tree;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.IPageLink;
import org.apache.wicket.markup.html.link.InlineFrame;
import org.apache.wicket.markup.html.tree.ITreeState;

import org.efaps.admin.ui.AbstractCommand;
import org.efaps.admin.ui.Menu;
import org.efaps.admin.ui.AbstractCommand.TargetMode;
import org.efaps.ui.wicket.EFapsSession;
import org.efaps.ui.wicket.UpdateInterface;
import org.efaps.ui.wicket.components.menutree.MenuTree;
import org.efaps.ui.wicket.models.StructurBrowserModel;
import org.efaps.ui.wicket.models.StructurBrowserModel.BogusNode;
import org.efaps.ui.wicket.pages.content.form.FormPage;
import org.efaps.ui.wicket.pages.content.table.TablePage;
import org.efaps.ui.wicket.pages.contentcontainer.ContentContainerPage;

/**
 * @author jmox
 * @version $Id:StructurBrowserTree.java 1510 2007-10-18 14:35:40Z jmox $
 */
public class StructurBrowserTree extends Tree {

  private static final long serialVersionUID = 1L;

  private static final ResourceReference CSS =
      new ResourceReference(StructurBrowserTree.class, "StructurTree.css");

  /**
   * instance variable holding the Key to the MenuTree (needed to update it)
   */
  private final String listMenuKey;

  private final Map<String, DefaultMutableTreeNode> oidToNode =
      new HashMap<String, DefaultMutableTreeNode>();

  public StructurBrowserTree(final String _wicketId, final TreeModel _model,
                             final String _listmenukey) {
    super(_wicketId, _model);
    this.listMenuKey = _listmenukey;
    this.setRootLess(true);

    final ITreeState treeState = this.getTreeState();
    treeState.collapseAll();
    treeState.addTreeStateListener(new AsyncronTreeUpdateListener());

    final AjaxUpdateBehavior update = new AjaxUpdateBehavior();
    this.add(update);
  }

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

    ((EFapsSession) this.getSession()).addUpdateBehaviors(model.getOid(),
        (AjaxUpdateBehavior) getBehaviors(AjaxUpdateBehavior.class).get(0));

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
                      FormPage page = new FormPage(parameter);
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

  public class AjaxUpdateBehavior extends AbstractDefaultAjaxBehavior implements
      UpdateInterface {

    private static final long serialVersionUID = 1L;

    private String oid;

    private TargetMode mode;

    @Override
    protected void respond(final AjaxRequestTarget _target) {
      DefaultMutableTreeNode node =
          StructurBrowserTree.this.oidToNode.get(this.oid);
      DefaultTreeModel treemodel =
          (DefaultTreeModel) this.getComponent().getModel().getObject();
      StructurBrowserModel model = (StructurBrowserModel) node.getUserObject();
      StructurBrowserTree tree = (StructurBrowserTree) this.getComponent();
      if (this.mode == TargetMode.EDIT) {
        treemodel.nodeChanged(node);
        model.requeryLabel();
      }
      if (this.mode == TargetMode.CREATE || this.mode == TargetMode.UNKNOWN) {
        if (node.getChildCount() > 0) {
          if (!(node.getChildAt(0) instanceof BogusNode)) {
            node.removeAllChildren();
            model.resetModel();
            model.addChildren(node);
            tree.invalidateAll();
          }
        } else {
          model.setParent(true);
          model.addBogusNode(node);
          treemodel.nodeChanged(node);
        }
      }

      tree.updateTree(_target);
    }

    @Override
    protected CharSequence getCallbackScript() {
      return "function findFrame(_current, _target)  {"
          + "  var ret = _current.frames[_target];"
          + "  if (!ret) {"
          + "    for (var i=0; i < _current.frames.length && !ret; i++)  {"
          + "      ret = findFrame(_current.frames[i], _target);"
          + "    }"
          + "  }"
          + "  return ret;"
          + "}"
          + "var fen = findFrame(top,\"eFapsFrameContent\");"
          + "if(!fen){"
          + "  fen = top;"
          + "}"
          + "fen.setTimeout(function(){ fen.childCallBack(\"javascript:"
          + generateCallbackScript("wicketAjaxGet('"
              + getCallbackUrl(false)
              + "'")
          + "\");},0);";
      // the timeout is needed due to a bug in firefox, that does not close the
      // nsIXMLHttpRequest and therfore throws an error that disables any
      // further javascript. The timeout is a workaround for this bug.
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.wicket.ajax.AbstractDefaultAjaxBehavior#getPreconditionScript()
     */
    @Override
    protected CharSequence getPreconditionScript() {
      return null;
    }

    public boolean isAjaxCallback() {
      return true;
    }

    public String getAjaxCallback() {
      return getCallbackScript().toString();
    }

    public void setOid(final String _oid) {
      this.oid = _oid;
    }

    public void setMode(final TargetMode _mode) {
      this.mode = _mode;
    }

  }
}
