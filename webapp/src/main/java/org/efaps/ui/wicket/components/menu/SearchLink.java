/*
 * Copyright 2003-2008 The eFaps Team
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

import org.apache.wicket.model.IModel;

import org.efaps.admin.ui.AbstractCommand;
import org.efaps.ui.wicket.models.FormModel;
import org.efaps.ui.wicket.models.objects.UIForm;
import org.efaps.ui.wicket.models.objects.UIMenuItem;
import org.efaps.ui.wicket.pages.content.form.FormPage;

/**
 * @author jmox
 * @version $Id:SearchLink.java 1510 2007-10-18 14:35:40Z jmox $
 *
 */
public class SearchLink extends AbstractMenuItemLink {

  private static final long serialVersionUID = 1L;

  public SearchLink(final String _id, final IModel<UIMenuItem> _model) {
    super(_id, _model);
  }

  @Override
  public void onClick() {
    final UIForm form =
        (UIForm) ((MenuPanel)this.findParent(MenuPanel.class)).getDefaultModelObject();
    form.resetModel();
    final AbstractCommand command = super.getModelObject().getCommand();
    form.setCommandUUID(command.getUUID());
    form.setFormUUID(command.getTargetForm().getUUID());
    final FormPage page = new FormPage(new FormModel(form));

    this.getRequestCycle().setResponsePage(page);

  }

}
