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

import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.resources.StyleSheetReference;

import org.efaps.webapp.components.footer.FooterPanel;
import org.efaps.webapp.components.menu.MenuPanel;
import org.efaps.webapp.components.titel.TitelPanel;
import org.efaps.webapp.models.ModelAbstract;

/**
 * @author jmo
 * @version $Id$
 * 
 */
public abstract class ContentPage extends WebPage {

  private static final long serialVersionUID = -2374207555009145191L;

  private ModalWindow modalWindow;

  public ContentPage() {
  }

  public ContentPage(final ModalWindow _modalWindow) {
    this.modalWindow = _modalWindow;
  }

  protected void addComponents() {
    try {

      ModelAbstract model = (ModelAbstract) super.getModel();
      add(new TitelPanel("eFapsTitel", model.getTitle()));

      add(new StyleSheetReference("ContentPageCSS", getClass(),
          "contentpage/ContentPage.css"));

      add(new MenuPanel("eFapsMenu", model));
      FooterPanel footerpanel =
          new FooterPanel("eFapsFooter", model, modalWindow);
      footerpanel.setVisible(model.isCreateMode() || model.isEditMode()
          || model.isSearchMode());
      add(footerpanel);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
