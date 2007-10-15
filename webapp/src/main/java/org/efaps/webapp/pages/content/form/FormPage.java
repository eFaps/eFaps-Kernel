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

package org.efaps.webapp.pages.content.form;

import org.apache.wicket.IPageMap;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.resources.StyleSheetReference;
import org.apache.wicket.model.IModel;

import org.efaps.webapp.components.FormContainer;
import org.efaps.webapp.components.modalwindow.ModalWindowContainer;
import org.efaps.webapp.components.table.WebFormContainer;
import org.efaps.webapp.models.AbstractModel;
import org.efaps.webapp.models.FormModel;
import org.efaps.webapp.pages.content.AbstractContentPage;

/**
 * @author jmox
 * @version $Id$
 */
public class FormPage extends AbstractContentPage {

  private static final long serialVersionUID = -3554311414948286302L;

  public FormPage(final PageParameters _parameters) {
    this(new FormModel(_parameters), null);
  }

  public FormPage(final PageParameters _parameters,
                     final ModalWindowContainer _modalWindow) {
    this(new FormModel(_parameters), _modalWindow);
  }

  public FormPage(final IModel _model) {
    this(_model, null);
  }

  public FormPage(final IModel _model,
                     final ModalWindowContainer _modalWindow) {
    super(_model, _modalWindow);
    this.addComponents();
  }

  public FormPage(final PageParameters _parameters,
                     final ModalWindowContainer _modalWindow,
                     final IPageMap _pagemap) {
    this(new FormModel(_parameters), _modalWindow, _pagemap);
  }

  public FormPage(final IModel _model,
                     final ModalWindowContainer _modalWindow,
                     final IPageMap _pagemap) {
    super(_model, _modalWindow, _pagemap);
    this.addComponents();
  }

  protected void addComponents() {
    add(new StyleSheetReference("webformcss", getClass(),
        "FormPage.css"));
    FormContainer form = new FormContainer("form");
    add(form);
    super.addComponents(form);

    AbstractModel model = (AbstractModel) super.getModel();
    form.add(new WebFormContainer("formtable", model, this));

    WebMarkupContainer script = new WebMarkupContainer("selectscript");
    this.add(script);
    script.setVisible(model.isCreateMode()
        || model.isEditMode()
        || model.isSearchMode());
  }
}
