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

package org.efaps.webapp.pages.content;

import org.apache.wicket.IPageMap;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.resources.StyleSheetReference;
import org.apache.wicket.model.IModel;

import org.efaps.webapp.components.FormContainer;
import org.efaps.webapp.components.footer.FooterPanel;
import org.efaps.webapp.components.menu.MenuPanel;
import org.efaps.webapp.components.modalwindow.ModalWindowContainer;
import org.efaps.webapp.components.titel.TitelPanel;
import org.efaps.webapp.models.AbstractModel;

/**
 * @author jmox
 * @version $Id$
 */
public abstract class AbstractContentPage extends WebPage {

  private static final long serialVersionUID = -2374207555009145191L;

  public static final String POPUP_PAGEMAP_NAME = "eFapsPopUp";

  private String listMenuKey;

  private final ModalWindowContainer modalWindow;

  private final ModalWindowContainer modal = new ModalWindowContainer("modal");

  public AbstractContentPage(final IModel _model) {
    this(_model, null);
  }

  public AbstractContentPage(final IModel _model,
                     final ModalWindowContainer _modalWindow) {
    super(_model);
    this.modalWindow = _modalWindow;
  }

  public AbstractContentPage(final IModel _model,
                     final ModalWindowContainer _modalWindow,
                     final IPageMap _pagemap) {
    super(_pagemap, _model);
    this.modalWindow = _modalWindow;
  }

  protected void addComponents(FormContainer _form) {

    add(new StyleSheetReference("css", AbstractContentPage.class,
        "AbstractContentPage.css"));

    add(this.modal);
    this.modal.setPageMapName("modal");

    AbstractModel model = (AbstractModel) super.getModel();
    add(new TitelPanel("titel", model.getTitle()));

    add(new MenuPanel("menu", model, _form));
    WebMarkupContainer footerpanel;
    if (model.isCreateMode() || model.isEditMode() || model.isSearchMode()) {
      footerpanel = new FooterPanel("footer", model, this.modalWindow, _form);
    } else {
      footerpanel = new WebMarkupContainer("footer");
      footerpanel.setVisible(false);
    }

    add(footerpanel);

  }

  public final ModalWindowContainer getModal() {
    return this.modal;
  }

  /**
   * This is the getter method for the instance variable {@link #listMenuKey}.
   *
   * @return value of instance variable {@link #listMenuKey}
   */

  public String getListMenuKey() {
    return this.listMenuKey;
  }

  /**
   * This is the setter method for the instance variable {@link #listMenuKey}.
   *
   * @param _listmenunkey
   *                the listMenuName to set
   */
  public void setListMenuKey(final String _listmenunkey) {
    this.listMenuKey = _listmenunkey;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.apache.wicket.Component#onAfterRender()
   */
  @Override
  protected void onAfterRender() {
    super.onAfterRender();
    if (this.listMenuKey == null) {
      this.listMenuKey =
          ((AbstractModel) this.getModel()).getParameter("listMenuKey");

    }
  }

}
