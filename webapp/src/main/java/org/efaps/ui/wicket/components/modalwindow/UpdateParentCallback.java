/*
 * Copyright 2003 - 2009 The eFaps Team
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

package org.efaps.ui.wicket.components.modalwindow;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;

import org.efaps.ui.wicket.models.FormModel;
import org.efaps.ui.wicket.models.TableModel;
import org.efaps.ui.wicket.models.objects.AbstractUIObject;
import org.efaps.ui.wicket.models.objects.UIForm;
import org.efaps.ui.wicket.models.objects.UITable;
import org.efaps.ui.wicket.pages.content.AbstractContentPage;
import org.efaps.ui.wicket.pages.content.form.FormPage;
import org.efaps.ui.wicket.pages.content.table.TablePage;

/**
 * Class implements the WindowClosedCallback to be able to update the parent
 * page on closing a modal window.
 *
 * @author jmox
 * @version $Id$
 */
public class UpdateParentCallback implements ModalWindow.WindowClosedCallback {

  /**
   * Needed for serialization.
   */
  private static final long serialVersionUID = 1L;

  /**
   * Panel this call back belongs to.
   */
  private final Component panel;

  /**
   * Modal window this call back belongs to.
   */
  private final ModalWindowContainer modalwindow;

  /**
   * Must the model of the page be updated on update of the parent page.
   */
  private final boolean clearmodel;

  /**
   * Constructor setting the panel and the modal window.
   *
   * @see #UpdateParentCallback(Component, ModalWindowContainer, boolean)
   *
   * @param _panel        Panel belonging to this call back
   * @param _modalwindow  modal window belonging to this call back
   *
   */
  public UpdateParentCallback(final Component _panel,
                              final ModalWindowContainer _modalwindow) {
    this(_panel, _modalwindow, true);
  }

  /**
   * Constructor setting the panel and the modal window.
   *
   *
   *
   * @param _panel        Panel belonging to this call back
   * @param _modalwindow  modal window belonging to this call back
   * @param _clearmodel   must the model of the page be updated
   */
  public UpdateParentCallback(final Component _panel,
                              final ModalWindowContainer _modalwindow,
                              final boolean _clearmodel) {
    this.panel = _panel;
    this.modalwindow = _modalwindow;
    this.clearmodel = _clearmodel;
  }

  /**
   * Method is executed on close of the modal window.
   * @param _target Target
   */
  public void onClose(final AjaxRequestTarget _target) {
    if (this.modalwindow.isUpdateParent()) {

      final AbstractUIObject uiObject
              = (AbstractUIObject) this.panel.getPage().getDefaultModelObject();

      if (this.clearmodel) {
        uiObject.resetModel();
      }

      AbstractContentPage page = null;
      if (uiObject instanceof UITable) {
        page = new TablePage(new TableModel((UITable) uiObject));
      } else if (uiObject instanceof UIForm) {
        page = new FormPage(new FormModel((UIForm) uiObject));
      }
      //copy the MenuKey to the new page
      page.setMenuTreeKey(((TablePage) this.panel.getPage()).getMenuTreeKey());
      this.panel.setResponsePage(page);
    }
  }
}
