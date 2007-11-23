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

package org.efaps.ui.wicket.components.menu;

import org.efaps.admin.ui.AbstractCommand;
import org.efaps.ui.wicket.models.FormModel;
import org.efaps.ui.wicket.models.MenuItemModel;
import org.efaps.ui.wicket.pages.content.form.FormPage;

/**
 * @author jmox
 * @version $Id:SearchLink.java 1510 2007-10-18 14:35:40Z jmox $
 *
 */
public class SearchLink extends AbstractMenuItemLink {

  private static final long serialVersionUID = 1L;

  public SearchLink(final String _id, final MenuItemModel _model) {
    super(_id, _model);
  }

  @Override
  public void onClick() {
    FormModel formmodel =
        (FormModel) this.findParent(MenuPanel.class).getModel();
    formmodel.resetModel();
    AbstractCommand command = ((MenuItemModel) super.getModel()).getCommand();
    formmodel.setCommandUUID(command.getUUID());
    formmodel.setFormUUID(command.getTargetForm().getUUID());
    FormPage page = new FormPage(formmodel);

    this.getRequestCycle().setResponsePage(page);

  }

}
