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

package org.efaps.webapp.pages;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.resources.StyleSheetReference;

import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.webapp.components.modalwindow.ModalWindowContainer;

/**
 * @author jmo
 * @version $Id$
 */
public class WarnDialogPage extends WebPage {

  private static final long serialVersionUID = 1L;

  public WarnDialogPage(final ModalWindowContainer _modal) {
    add(new StyleSheetReference("css", getClass(),
        "warndialogpage/WarnDialogPage.css"));
    this.add(new Label("label", DBProperties
        .getProperty("Common_UIForm_Mandotory.Message")));

    AjaxLink button = new AjaxLink("button") {

      private static final long serialVersionUID = 1L;

      @Override
      public void onClick(AjaxRequestTarget target) {
        _modal.close(target);
      }
    };

    this.add(button);
    button.add(new Label("buttonlabel", "close"));
  }

}
