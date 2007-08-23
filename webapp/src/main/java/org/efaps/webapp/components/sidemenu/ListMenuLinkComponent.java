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

package org.efaps.webapp.components.sidemenu;

import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.model.IModel;

import org.efaps.webapp.components.EFapsContainerComponent;
import org.efaps.webapp.models.EFapsApplicationSession;
import org.efaps.webapp.models.IMenuItemModel;
import org.efaps.webapp.wicket.WebFormPage;
import org.efaps.webapp.wicket.WebTablePage;

/**
 * @author jmo
 * @version $Id$
 * 
 */
public class ListMenuLinkComponent extends AjaxLink {

  private static final long serialVersionUID = 1L;

  public ListMenuLinkComponent(final String _id, final IModel _model) {
    super(_id, _model);

  }

  @Override
  public void onClick(final AjaxRequestTarget _target) {

    IMenuItemModel model = (IMenuItemModel) super.getModel();
    PageParameters para = new PageParameters();
    para.add("oid", model.getOid());
    para.add("command", model.getCommand().getName());

    EFapsContainerComponent page;
    if (model.getCommand().getTargetTable() != null) {

      page =
          new EFapsContainerComponent("eFapsContentContainer",
              WebTablePage.class, para);
    } else {
      page =
          new EFapsContainerComponent("eFapsContentContainer",
              WebFormPage.class, para);
    }

    Component component =
        getPage().get(
            "eFapsSplitContainer:containerrechts:eFapsContentContainer");
    component.replaceWith(page);
    _target.addComponent(page);
    ((EFapsApplicationSession) (Session.get())).setSideMenuSelected(this);
  }

}
