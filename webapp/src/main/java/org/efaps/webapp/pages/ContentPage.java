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

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.resources.StyleSheetReference;

import org.efaps.webapp.components.FormContainer;
import org.efaps.webapp.components.footer.FooterPanel;
import org.efaps.webapp.components.menu.MenuPanel;
import org.efaps.webapp.components.modalwindow.ModalWindowContainer;
import org.efaps.webapp.components.titel.TitelPanel;
import org.efaps.webapp.models.AbstractModel;

/**
 * @author jmo
 * @version $Id$
 */
public abstract class ContentPage extends WebPage {

  private static final long serialVersionUID = -2374207555009145191L;

  private ModalWindowContainer modalWindow;

  public ContentPage() {
  }

  public ContentPage(final ModalWindowContainer _modalWindow) {
    this.modalWindow = _modalWindow;
  }

  protected void addComponents(FormContainer _form) {
    try {

      AbstractModel model = (AbstractModel) super.getModel();
      add(new TitelPanel("eFapsTitel", model.getTitle()));

      add(new StyleSheetReference("ContentPageCSS", getClass(),
          "contentpage/ContentPage.css"));

      add(new MenuPanel("eFapsMenu", model, _form));
      WebMarkupContainer footerpanel;
      if (model.isCreateMode() || model.isEditMode() || model.isSearchMode()) {
        footerpanel = new FooterPanel("eFapsFooter", model, this.modalWindow, _form);
      } else {
        footerpanel = new WebMarkupContainer("eFapsFooter");
        footerpanel.setVisible(false);
      }

      add(footerpanel);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
