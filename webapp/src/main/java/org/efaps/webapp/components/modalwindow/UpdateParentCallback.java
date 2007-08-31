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
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;

import org.efaps.webapp.models.FormModel;
import org.efaps.webapp.models.TableModel;
import org.efaps.webapp.pages.WebFormPage;
import org.efaps.webapp.pages.WebTablePage;

/**
 * @author jmo
 * @version $Id$
 * 
 */
public class UpdateParentCallback implements ModalWindow.WindowClosedCallback {

  private static final long serialVersionUID = 1L;

  private final WebMarkupContainer panel;

  private final ModalWindowContainer modalwindow;

  public UpdateParentCallback(final WebMarkupContainer _panel,
                      ModalWindowContainer _modalwindow) {
    this.panel = _panel;
    this.modalwindow = _modalwindow;
  }

  public void onClose(AjaxRequestTarget _target) {
    if (this.modalwindow.isUpdateParent()) {
      panel.getPage().getModel().detach();
      Page page = null;
      if (panel.getPage().getModel() instanceof TableModel) {

        page = new WebTablePage(panel.getPage().getModel());

      } else if (panel.getPage().getModel() instanceof FormModel) {
        page = new WebFormPage(panel.getPage().getModel());
      }
      panel.setResponsePage(page);
    }
  }
}
