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

import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;

import org.efaps.admin.ui.Menu;
import org.efaps.db.Instance;
import org.efaps.webapp.components.AbstractParentAjaxLink;
import org.efaps.webapp.components.listmenu.ListMenuUpdate;
import org.efaps.webapp.models.TableModel.CellModel;
import org.efaps.webapp.pages.ContentContainerPage;
import org.efaps.webapp.pages.WebFormPage;
import org.efaps.webapp.pages.WebTablePage;

/**
 * @author jmo
 * @version $Id$
 * 
 */
public class AjaxLinkContainer extends AbstractParentAjaxLink {

  private static final long serialVersionUID = 1L;

  private int step = 1;

  public AjaxLinkContainer(final String id, final IModel model) {
    super(id, model);
  }

  @Override
  public void onClick(final AjaxRequestTarget _target) {
    CellModel cellmodel = (CellModel) super.getModel();
    Instance instance = null;
    if (cellmodel.getOid() != null) {
      instance = new Instance(cellmodel.getOid());
    }
    Menu menu;

    try {
      menu = instance.getType().getTreeMenu();

      PageParameters para = new PageParameters();
      para.add("command", menu.getName());
      para.add("oid", cellmodel.getOid());

      if (isFirstStep()) {
        this.firstStep(_target, menu, para);
      } else {
        this.secondStep(menu, para);
      }

    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  private boolean isFirstStep() {
    if (this.step == 1) {
      this.step = 2;
      return true;
    } else {
      return false;
    }
  }

  private void firstStep(final AjaxRequestTarget _target, final Menu _menu,
                         final PageParameters _parameters) {
    ListMenuUpdate.update(_target, ContentContainerPage.LISTMENU, _menu,
        _parameters, ((CellModel) super.getModel()).getOid());
  }

  private void secondStep(final Menu _menu, final PageParameters _parameters) {
    try {

      if (_menu.getTargetTable() != null) {

        this.getRequestCycle().setResponsePage(WebTablePage.class, _parameters);
      } else {

        this.getRequestCycle().setResponsePage(WebFormPage.class, _parameters);
      }

    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
