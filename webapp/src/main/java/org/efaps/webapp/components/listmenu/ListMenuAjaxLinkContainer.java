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

package org.efaps.webapp.components.listmenu;

import org.apache.wicket.Page;
import org.apache.wicket.PageMap;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.IPageLink;
import org.apache.wicket.markup.html.link.InlineFrame;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.model.IModel;

import org.efaps.admin.ui.CommandAbstract;
import org.efaps.webapp.EFapsSession;
import org.efaps.webapp.components.listmenu.ListMenuPanel.StyleClassName;
import org.efaps.webapp.models.MenuItemModel;
import org.efaps.webapp.pages.content.form.FormPage;
import org.efaps.webapp.pages.content.table.TablePage;
import org.efaps.webapp.pages.contentcontainer.ContentContainerPage;

/**
 * This class renders the Link wich is followed when a ListMenuItem is clicked.<br>
 * In this class the Style of the Links, are also set.
 *
 * @author jmox
 * @version $Id$
 */
public class ListMenuAjaxLinkContainer extends WebMarkupContainer {

  private static final long serialVersionUID = 1L;

  private final String menukey;

  private StyleClassName defaultStyleClass;

  public ListMenuAjaxLinkContainer(final String _id, final String _menukey,
                                   final IModel _model) {
    super(_id, _model);
    this.menukey = _menukey;
    this.add(new AjaxClickBehaviour());
  }

  @Override
  protected void onComponentTag(ComponentTag tag) {
    super.onComponentTag(tag);
    MenuItemModel model = (MenuItemModel) super.getModel();

    if (model.isHeader() && (this.findParent(ListItem.class) != null)) {
      this.defaultStyleClass = StyleClassName.HEADER;

      if (model.isSelected()) {
        ((EFapsSession) this.getSession()).putIntoCache(this.menukey, this);
        tag.put("class", StyleClassName.ITEM_SELECTED.name);
        ListMenuPanel parentListMenuPanel =
            (ListMenuPanel) this.findParent(ListMenuPanel.class);
        parentListMenuPanel.addHeaderComponent(this);
      } else {
        if (this.equals(((EFapsSession) this.getSession())
            .getFromCache(this.menukey))) {
          tag.put("class", StyleClassName.ITEM_SELECTED.name);
        } else {
          tag.put("class", StyleClassName.HEADER.name);
        }
      }
    } else {
      ListMenuPanel listmenupanel =
          (ListMenuPanel) this.findParent(ListMenuPanel.class);
      int padding =
          model.getLevel()
              * listmenupanel.getPadding()
              + listmenupanel.getPaddingAdd();

      tag.put("style", "padding-left:" + padding + "px;");
      if (model.isSelected()) {
        tag.put("class", StyleClassName.ITEM_SELECTED.name);
        ((EFapsSession) this.getSession()).putIntoCache(this.menukey, this);
      } else {
        tag.put("class", StyleClassName.ITEM.name);
      }
      this.defaultStyleClass = StyleClassName.ITEM;
    }

  }

  public StyleClassName getDefaultStyleClass() {
    return this.defaultStyleClass;
  }

  public String getCallbackScript() {
    return ((AjaxClickBehaviour) super.getBehaviors().get(0))
        .getCallbackScript();
  }

  private class AjaxClickBehaviour extends AjaxEventBehavior {

    private static final long serialVersionUID = 1L;

    public AjaxClickBehaviour() {
      super("onclick");
    }

    @Override
    public String getCallbackScript() {
      return super.getCallbackScript().toString();
    }

    @Override
    protected void onEvent(final AjaxRequestTarget _target) {
      MenuItemModel model = (MenuItemModel) this.getComponent().getModel();

      CommandAbstract cmd = model.getCommand();
      final PageParameters para = new PageParameters();
      para.add("oid", model.getOid());
      para.add("command", cmd.getUUID().toString());

      InlineFrame page;
      if (cmd.getTargetTable() != null) {
        page =
            new InlineFrame(ContentContainerPage.IFRAME_WICKETID, PageMap
                .forName(ContentContainerPage.IFRAME_PAGEMAP_NAME),
                new IPageLink() {

                  private static final long serialVersionUID = 1L;

                  public Page getPage() {
                    TablePage page = new TablePage(para);
                    page.setListMenuKey(ListMenuAjaxLinkContainer.this.menukey);
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
                    FormPage page = new FormPage(para);
                    page.setListMenuKey(ListMenuAjaxLinkContainer.this.menukey);
                    return page;
                  }

                  public Class<FormPage> getPageIdentity() {
                    return FormPage.class;
                  }
                });
      }

      InlineFrame component =
          (InlineFrame) getPage().get(
              ((ContentContainerPage) getPage()).getInlinePath());
      page.setOutputMarkupId(true);

      component.replaceWith(page);
      _target.addComponent(page.getParent());
      ListMenuUpdate.setSelectedItem(ListMenuAjaxLinkContainer.this.menukey,
          this.getComponent(), _target);

    }
  }

  /*
   * (non-Javadoc)
   *
   * @see org.apache.wicket.Component#onAfterRender()
   */
  @Override
  protected void onAfterRender() {
    super.onAfterRender();
    ((MenuItemModel) super.getModel()).setSelected(false);
  }
}
