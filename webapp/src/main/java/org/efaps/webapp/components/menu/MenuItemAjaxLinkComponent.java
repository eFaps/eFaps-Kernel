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

import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.model.IModel;

import org.efaps.admin.ui.CommandAbstract;
import org.efaps.webapp.models.MenuItemModel;
import org.efaps.webapp.pages.WebFormPage;
import org.efaps.webapp.pages.WebTablePage;

/**
 * @author jmo
 * @version $Id$
 */
public class MenuItemAjaxLinkComponent extends AjaxLink {
  private static final long serialVersionUID = 1L;

  public MenuItemAjaxLinkComponent(final String _id, final IModel _menuItem) {
    super(_id, _menuItem);
  }

  @Override
  protected void onRender(final MarkupStream _markupStream) {
    _markupStream.next();
  }

  @Override
  public void onClick(final AjaxRequestTarget _target) {
    MenuPanel menupanel = (MenuPanel) this.findParent(MenuPanel.class);
    ModalWindowAjaxPageCreator pageCreator =
        new ModalWindowAjaxPageCreator(super.getModel(), menupanel.getModal());
    menupanel.getModal().setPageCreator(pageCreator);
    menupanel.getModal().show(_target);
  }

  public class ModalWindowAjaxPageCreator implements ModalWindow.PageCreator {

    private static final long serialVersionUID = 1L;

    private final IModel imodel;

    private final ModalWindow modalWindow;

    public ModalWindowAjaxPageCreator(final IModel _model,
                                      final ModalWindow _modalWindow) {
      this.imodel = _model;
      this.modalWindow = _modalWindow;
    }

    public Page createPage() {
      Page ret = null;
      MenuItemModel model = (MenuItemModel) this.imodel;
      CommandAbstract command = model.getCommand();
      PageParameters para = new PageParameters("command=" + command.getName());
      para.add("oid", model.getOid());
      try {

        if (command.getTargetTable() != null) {
          ret = new WebTablePage(para);

        } else {
          ret = new WebFormPage(para, this.modalWindow);
        }
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      return ret;
    }
  }
}
