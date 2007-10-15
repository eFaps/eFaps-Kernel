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

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;

import org.efaps.webapp.models.AbstractModel;
import org.efaps.webapp.models.FormModel;
import org.efaps.webapp.models.TableModel;
import org.efaps.webapp.pages.content.form.FormPage;
import org.efaps.webapp.pages.content.table.TablePage;

/**
 * @author jmox
 * @version $Id$
 */
public class UpdateParentCallback implements ModalWindow.WindowClosedCallback {

  private static final long serialVersionUID = 1L;

  private final Component panel;

  private final ModalWindowContainer modalwindow;

  private final boolean clearmodel;

  public UpdateParentCallback(final Component _panel,
                              ModalWindowContainer _modalwindow) {
    this(_panel, _modalwindow, true);
  }

  public UpdateParentCallback(final Component _panel,
                              ModalWindowContainer _modalwindow,
                              final boolean _clearmodel) {
    this.panel = _panel;
    this.modalwindow = _modalwindow;
    this.clearmodel = _clearmodel;
  }

  public void onClose(AjaxRequestTarget _target) {
    if (this.modalwindow.isUpdateParent()) {
      if (this.clearmodel) {
        ((AbstractModel) this.panel.getPage().getModel()).resetModel();
      }

      Page page = null;
      if (this.panel.getPage().getModel() instanceof TableModel) {

        page = new TablePage(this.panel.getPage().getModel());

      } else if (this.panel.getPage().getModel() instanceof FormModel) {
        page = new FormPage(this.panel.getPage().getModel());
      }
      this.panel.setResponsePage(page);
    }
  }
}
