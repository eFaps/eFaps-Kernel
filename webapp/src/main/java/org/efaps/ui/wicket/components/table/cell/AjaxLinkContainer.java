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

package org.efaps.ui.wicket.components.table.cell;

import org.apache.wicket.PageMap;
import org.apache.wicket.PageParameters;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;

import org.efaps.admin.ui.AbstractCommand;
import org.efaps.admin.ui.Menu;
import org.efaps.db.Instance;
import org.efaps.ui.wicket.EFapsSession;
import org.efaps.ui.wicket.behaviors.AbstractAjaxCallBackBehavior;
import org.efaps.ui.wicket.components.menutree.MenuTree;
import org.efaps.ui.wicket.models.cell.TableCellModel;
import org.efaps.ui.wicket.pages.content.AbstractContentPage;
import org.efaps.ui.wicket.pages.content.form.FormPage;
import org.efaps.ui.wicket.pages.content.table.TablePage;
import org.efaps.ui.wicket.pages.contentcontainer.ContentContainerPage;
import org.efaps.ui.wicket.pages.error.ErrorPage;

/**
 * @author jmox
 * @version $Id:AjaxLinkContainer.java 1510 2007-10-18 14:35:40Z jmox $
 */
public class AjaxLinkContainer extends WebMarkupContainer {

  private static final long serialVersionUID = 1L;

  public AjaxLinkContainer(final String id, final IModel model) {
    super(id, model);
    this.add(new AjaxSelfCallBackBehavior());
    this.add(new AjaxParentCallBackBehavior());
  }

  @Override
  protected void onComponentTag(ComponentTag tag) {
    super.onComponentTag(tag);
    tag.put("href", "#");
  }

  public class AjaxParentCallBackBehavior extends AbstractAjaxCallBackBehavior {

    private static final long serialVersionUID = 1L;

    public AjaxParentCallBackBehavior() {
      super("onmouseup", Target.PARENT);
    }

    @Override
    protected void onEvent(final AjaxRequestTarget _target) {
      TableCellModel cellmodel = (TableCellModel) super.getComponent().getModel();
      Instance instance = null;
      if (cellmodel.getOid() != null) {
        instance = new Instance(cellmodel.getOid());

        Menu menu = null;
        try {
          menu = Menu.getTypeTreeMenu(instance.getType());
        } catch (Exception e) {
          throw new RestartResponseException(new ErrorPage(e));
        }
        if (menu == null) {
          Exception ex =
              new Exception("no tree menu defined for type "
                  + instance.getType().getName());
          throw new RestartResponseException(new ErrorPage(ex));
        }

        PageParameters para = new PageParameters();
        para.add("command", menu.getUUID().toString());
        para.add("oid", cellmodel.getOid());

        String listMenuKey =
            ((AbstractContentPage) this.getComponent().getPage())
                .getListMenuKey();
        MenuTree menutree =
            (MenuTree) ((EFapsSession) this.getComponent().getSession())
                .getFromCache(listMenuKey);

        menutree.addChildMenu(para,_target);

      }
    }
  }

  public class AjaxSelfCallBackBehavior extends AjaxEventBehavior {

    private static final long serialVersionUID = 1L;

    public AjaxSelfCallBackBehavior() {
      super("onClick");
    }

    @Override
    protected void onEvent(AjaxRequestTarget arg0) {
      TableCellModel cellmodel = (TableCellModel) super.getComponent().getModel();
      Instance instance = null;
      if (cellmodel.getOid() != null) {
        instance = new Instance(cellmodel.getOid());
        AbstractCommand menu = null;
        try {
          menu = Menu.getTypeTreeMenu(instance.getType());
        } catch (Exception e) {
          throw new RestartResponseException(new ErrorPage(e));
        }
        if (menu == null) {
          Exception ex =
              new Exception("no tree menu defined for type "
                  + instance.getType().getName());
          throw new RestartResponseException(new ErrorPage(ex));
        }

        for (AbstractCommand childcmd : ((Menu) menu).getCommands()) {
          if (childcmd.isDefaultSelected()) {
            menu = childcmd;
            break;
          }
        }

        PageParameters parameters = new PageParameters();
        parameters.add("command", menu.getUUID().toString());
        parameters.add("oid", cellmodel.getOid());
        AbstractContentPage page;
        if (menu.getTargetTable() != null) {
          page =
              new TablePage(parameters, PageMap
                  .forName(ContentContainerPage.IFRAME_PAGEMAP_NAME));
        } else {
          page =
              new FormPage(parameters, null, PageMap
                  .forName(ContentContainerPage.IFRAME_PAGEMAP_NAME));
        }
        page.setListMenuKey(((AbstractContentPage) this.getComponent()
            .getPage()).getListMenuKey());
        super.getComponent().getRequestCycle().setResponsePage(page);
      }
    }
  }
}
