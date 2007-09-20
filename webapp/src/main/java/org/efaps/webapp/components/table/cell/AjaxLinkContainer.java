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

package org.efaps.webapp.components.table.cell;

import org.apache.wicket.PageMap;
import org.apache.wicket.PageParameters;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;

import org.efaps.admin.ui.Menu;
import org.efaps.db.Instance;
import org.efaps.webapp.components.AbstractAjaxCallBackBehavior;
import org.efaps.webapp.components.listmenu.ListMenuUpdate;
import org.efaps.webapp.models.CellModel;
import org.efaps.webapp.pages.ContentContainerPage;
import org.efaps.webapp.pages.ErrorPage;
import org.efaps.webapp.pages.WebFormPage;
import org.efaps.webapp.pages.WebTablePage;

/**
 * @author jmo
 * @version $Id$
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
      CellModel cellmodel = (CellModel) super.getComponent().getModel();
      Instance instance = null;
      if (cellmodel.getOid() != null) {
        instance = new Instance(cellmodel.getOid());

        Menu menu = null;
        try {
          menu = instance.getType().getTreeMenu();
        } catch (Exception e) {
          e.printStackTrace();
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
        ListMenuUpdate.update(_target, ContentContainerPage.LISTMENU, menu,
            para, ((CellModel) super.getComponent().getModel()).getOid());
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
      CellModel cellmodel = (CellModel) super.getComponent().getModel();
      Instance instance = null;
      if (cellmodel.getOid() != null) {
        instance = new Instance(cellmodel.getOid());

        Menu menu = null;
        try {
          menu = instance.getType().getTreeMenu();
        } catch (Exception e) {
          e.printStackTrace();
          throw new RestartResponseException(new ErrorPage(e));
        }
        if (menu == null) {
          Exception ex =
              new Exception("no tree menu defined for type "
                  + instance.getType().getName());
          throw new RestartResponseException(new ErrorPage(ex));
        }

        PageParameters parameters = new PageParameters();
        parameters.add("command", menu.getUUID().toString());
        parameters.add("oid", cellmodel.getOid());

        if (menu.getTargetTable() != null) {

          super.getComponent().getRequestCycle().setResponsePage(
              WebTablePage.class, parameters);
        } else {
          WebFormPage page =
              new WebFormPage(parameters, null, PageMap
                  .forName(ContentContainerPage.IFRAME_PAGEMAP_NAME));
          super.getComponent().getRequestCycle().setResponsePage(page);
        }
      }
    }
  }
}
