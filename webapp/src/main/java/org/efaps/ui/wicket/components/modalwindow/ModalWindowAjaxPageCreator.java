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
 * Revision:        $Rev:1510 $
 * Last Changed:    $Date:2007-10-18 09:35:40 -0500 (Thu, 18 Oct 2007) $
 * Last Changed By: $Author:jmox $
 */

package org.efaps.ui.wicket.components.modalwindow;

import org.apache.wicket.Page;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;

import org.efaps.ui.wicket.models.objects.UIMenuItem;
import org.efaps.ui.wicket.pages.content.form.FormPage;
import org.efaps.ui.wicket.pages.content.table.TablePage;

/**
 * Thic Class is used to create a page inside a modal window lazily.
 * @author jmox
 * @version $Id:ModalWindowAjaxPageCreator.java 1510 2007-10-18 14:35:40Z jmox $
 */
public class ModalWindowAjaxPageCreator implements ModalWindow.PageCreator {

  /**
   * Needed foer serialization.
   */
  private static final long serialVersionUID = 1L;

  /**
   * Model for the page to be created.
   */
  private final UIMenuItem imodel;

  /**
   * The modal window the page will be created in.
   */
  private final ModalWindowContainer modalWindow;

  /**
   * Constructor.
   *
   * @param _model        model for the page to create
   * @param _modalWindow  modal window the page will be created in
   */
  public ModalWindowAjaxPageCreator(final UIMenuItem _model,
                                    final ModalWindowContainer _modalWindow) {
    this.imodel = _model;
    this.modalWindow = _modalWindow;
  }
  /**
   * Method that creates the page.
   * @return new Page
   */
  public Page createPage() {
    Page ret = null;
    final UIMenuItem model = this.imodel;

    if (model.getCommand().getTargetTable() != null) {
      ret = new TablePage(model.getCommandUUID(), model.getInstanceKey());
    } else {
      ret = new FormPage(model.getCommandUUID(),
                         model.getInstanceKey(),
                         this.modalWindow);
    }
    return ret;
  }
}
