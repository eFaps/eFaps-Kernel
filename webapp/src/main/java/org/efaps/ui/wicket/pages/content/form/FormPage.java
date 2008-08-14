/*
 * Copyright 2003-20078 The eFaps Team
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
 * Revision:        $Rev:1491 $
 * Last Changed:    $Date:2007-10-15 18:40:43 -0500 (Mon, 15 Oct 2007) $
 * Last Changed By: $Author:jmox $
 */

package org.efaps.ui.wicket.pages.content.form;

import org.apache.wicket.IPageMap;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;

import org.efaps.ui.wicket.components.FormContainer;
import org.efaps.ui.wicket.components.form.FormPanel;
import org.efaps.ui.wicket.components.heading.HeadingPanel;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.components.table.TablePanel;
import org.efaps.ui.wicket.components.table.header.HeaderPanel;
import org.efaps.ui.wicket.models.FormModel;
import org.efaps.ui.wicket.models.TableModel;
import org.efaps.ui.wicket.models.objects.UIFieldTable;
import org.efaps.ui.wicket.models.objects.UIForm;
import org.efaps.ui.wicket.models.objects.UIHeading;
import org.efaps.ui.wicket.models.objects.UIForm.Element;
import org.efaps.ui.wicket.models.objects.UIForm.ElementType;
import org.efaps.ui.wicket.models.objects.UIForm.FormElement;
import org.efaps.ui.wicket.pages.content.AbstractContentPage;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.resources.StaticHeaderContributor;

/**
 * @author jmox
 * @version $Id:FormPage.java 1491 2007-10-15 23:40:43Z jmox $
 */
public class FormPage extends AbstractContentPage {

  private static final long serialVersionUID = -3554311414948286302L;

  private static final EFapsContentReference CSS =
      new EFapsContentReference(FormPage.class, "FormPage.css");

  public FormPage(final PageParameters _parameters) {
    this(_parameters, null);
  }

  public FormPage(final PageParameters _parameters,
                  final ModalWindowContainer _modalWindow) {
    this(new FormModel(new UIForm(_parameters)), _modalWindow);
  }

  public FormPage(final IModel _model) {
    this(_model, null);
  }

  public FormPage(final IModel _model, final ModalWindowContainer _modalWindow) {
    super(_model, _modalWindow);
    this.addComponents();
  }

  public FormPage(final PageParameters _parameters,
                  final ModalWindowContainer _modalWindow,
                  final IPageMap _pagemap) {
    this(new FormModel(new UIForm(_parameters)), _modalWindow, _pagemap);
  }

  public FormPage(final IModel _model, final ModalWindowContainer _modalWindow,
                  final IPageMap _pagemap) {
    super(_model, _modalWindow, _pagemap);
    this.addComponents();
  }

  protected void addComponents() {
    add(StaticHeaderContributor.forCss(CSS));

    final FormContainer form = new FormContainer("form");
    add(form);

    final UIForm model = (UIForm) super.getDefaultModelObject();

    if (!model.isInitialised()) {
      model.execute();
    }
    super.addComponents(form);

    final WebMarkupContainer script = new WebMarkupContainer("selectscript");
    this.add(script);
    script.setVisible(model.isCreateMode()
        || model.isEditMode()
        || model.isSearchMode());
    int i = 0;
    final RepeatingView elementRepeater = new RepeatingView("elementRepeater");
    form.add(elementRepeater);
    for (final Element element : model.getElements()) {
      if (element.getType().equals(ElementType.FORM)) {
        elementRepeater.add(new FormPanel(elementRepeater.newChildId(), this,
            new FormModel(model), (FormElement) element.getElement()));
      } else if (element.getType().equals(ElementType.HEADING)) {
        final UIHeading headingmodel = (UIHeading) element.getElement();
        elementRepeater.add(new HeadingPanel(elementRepeater.newChildId(),
            headingmodel.getLabel(), headingmodel.getLevel()));
      } else if (element.getType().equals(ElementType.TABLE)) {
        i++;

        final UIFieldTable fieldTable = (UIFieldTable) element.getElement();
        fieldTable.setTableId(i);
        final TablePanel table =
            new TablePanel(elementRepeater.newChildId(), new TableModel(fieldTable), this);
        final HeaderPanel header =
            new HeaderPanel(elementRepeater.newChildId(), table);
        elementRepeater.add(header);
        elementRepeater.add(table);

      }
    }

  }
}
