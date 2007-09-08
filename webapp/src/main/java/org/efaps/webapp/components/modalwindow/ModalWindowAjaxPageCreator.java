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

package org.efaps.webapp.components.modalwindow;

import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;

import org.efaps.admin.ui.CommandAbstract;
import org.efaps.webapp.models.MenuItemModel;
import org.efaps.webapp.pages.WebFormPage;
import org.efaps.webapp.pages.WebTablePage;

/**
 * @author jmo
 * @version $Id$
 */
public class ModalWindowAjaxPageCreator implements ModalWindow.PageCreator {

  private static final long serialVersionUID = 1L;

  private final MenuItemModel imodel;

  private final ModalWindowContainer modalWindow;

  public ModalWindowAjaxPageCreator(final MenuItemModel _model,
                                    final ModalWindowContainer _modalWindow) {
    this.imodel = _model;
    this.modalWindow = _modalWindow;
  }

  public Page createPage() {
    Page ret = null;
    MenuItemModel model = (MenuItemModel) this.imodel;
    CommandAbstract command = model.getCommand();
    PageParameters para = new PageParameters("command=" + command.getUUID());
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
