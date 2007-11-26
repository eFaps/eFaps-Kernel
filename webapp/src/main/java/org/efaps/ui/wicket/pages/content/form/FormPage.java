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
 * Revision:        $Rev:1491 $
 * Last Changed:    $Date:2007-10-15 18:40:43 -0500 (Mon, 15 Oct 2007) $
 * Last Changed By: $Author:jmox $
 */

package org.efaps.ui.wicket.pages.content.form;

import java.util.List;

import org.apache.wicket.IPageMap;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.resources.StyleSheetReference;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;

import org.efaps.admin.ui.AbstractCommand;
import org.efaps.ui.wicket.components.FormContainer;
import org.efaps.ui.wicket.components.form.FormPanel;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.components.table.TablePanel;
import org.efaps.ui.wicket.components.table.header.HeaderPanel;
import org.efaps.ui.wicket.components.titel.TitelPanel;
import org.efaps.ui.wicket.models.FormModel;
import org.efaps.ui.wicket.models.TableModel;
import org.efaps.ui.wicket.pages.content.AbstractContentPage;

/**
 * @author jmox
 * @version $Id:FormPage.java 1491 2007-10-15 23:40:43Z jmox $
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

  public FormPage(final IModel _model, final ModalWindowContainer _modalWindow) {
    super(_model, _modalWindow);
    this.addComponents();
  }

  public FormPage(final PageParameters _parameters,
                  final ModalWindowContainer _modalWindow,
                  final IPageMap _pagemap) {
    this(new FormModel(_parameters), _modalWindow, _pagemap);
  }

  public FormPage(final IModel _model, final ModalWindowContainer _modalWindow,
                  final IPageMap _pagemap) {
    super(_model, _modalWindow, _pagemap);
    this.addComponents();
  }

  protected void addComponents() {
    add(new StyleSheetReference("webformcss", getClass(), "FormPage.css"));
    FormContainer form = new FormContainer("form");
    add(form);
    super.addComponents(form);

    FormModel model = (FormModel) super.getModel();
    form.add(new FormPanel("formtable", model, this));

    WebMarkupContainer script = new WebMarkupContainer("selectscript");
    this.add(script);
    script.setVisible(model.isCreateMode()
        || model.isEditMode()
        || model.isSearchMode());

    RepeatingView subTableFormRepeater =
        new RepeatingView("subTableFormRepeater");
    form.add(subTableFormRepeater);
    if (model.hasSubCommands()) {
      List<AbstractCommand> cmds = model.getSubCommands();
      for (AbstractCommand cmd : cmds) {
        TableModel submodel = new TableModel(cmd.getUUID(), model.getOid());
        submodel.setSubmit(false);
        submodel.setShowCheckBoxes(false);
        subTableFormRepeater.add(new TitelPanel(subTableFormRepeater
            .newChildId(), submodel.getTitle()));
        final TablePanel tablebody =
            new TablePanel(subTableFormRepeater.newChildId(), submodel, this);
        subTableFormRepeater.add(new HeaderPanel(subTableFormRepeater
            .newChildId(), tablebody));
        subTableFormRepeater.add(tablebody);
      }
    } else {
      subTableFormRepeater.setVisible(false);
    }
  }
}
