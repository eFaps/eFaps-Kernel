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

package org.efaps.webapp.components.menu;

import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.link.Link;

import org.efaps.admin.ui.CommandAbstract;
import org.efaps.webapp.models.FormModel;
import org.efaps.webapp.models.MenuItemModel;
import org.efaps.webapp.pages.WebFormPage;

public class SearchItemLinkComponent extends Link {

  private static final long serialVersionUID = 1L;

  public SearchItemLinkComponent(String id, MenuItemModel _model) {
    super(id, _model);
  }

  @Override
  protected void onRender(final MarkupStream _markupStream) {
    _markupStream.next();
  }

  @Override
  public void onClick() {
    FormModel formmodel =
        (FormModel) this.findParent(MenuPanel.class).getModel();
    formmodel.clearModel();
    CommandAbstract command = ((MenuItemModel) super.getModel()).getCommand();
    formmodel.setCommandUUID(command.getUUID());
    formmodel.setFormUUID(command.getTargetForm().getUUID());
    WebFormPage page = new WebFormPage(formmodel);

    this.getRequestCycle().setResponsePage(page);

  }

}
