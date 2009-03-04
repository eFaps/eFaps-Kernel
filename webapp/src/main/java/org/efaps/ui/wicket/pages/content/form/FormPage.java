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
 * Revision:        $Rev:1491 $
 * Last Changed:    $Date:2007-10-15 18:40:43 -0500 (Mon, 15 Oct 2007) $
 * Last Changed By: $Author:jmox $
 */

package org.efaps.ui.wicket.pages.content.form;

import java.util.UUID;

import org.apache.wicket.IPageMap;
import org.apache.wicket.Page;
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

  private static final EFapsContentReference CSS
                    = new EFapsContentReference(FormPage.class, "FormPage.css");

  /**
   * Constructor called from the client directly by using parameters. Normally
   * it should only contain one parameter Opener.OPENER_PARAKEY to access the
   * opener.
   *
   * @param _parameters PageParameters
   */
  public FormPage(final PageParameters _parameters) {
    this(new FormModel(new UIForm(_parameters)));
  }

  public FormPage(final IModel<?> _model) {
    super(_model, null);
    this.addComponents();
  }

  /**
   * @param _commandUUID
   * @param _oid
   */
  public FormPage(final IPageMap _pagemap, final UUID _commandUUID,
                  final String _oid) {
   super(_pagemap, new FormModel(new UIForm(_commandUUID, _oid)), null);
   this.addComponents();
  }

  /**
   * @param _pageMap
   * @param _commandUUID
   * @param _oid
   * @param _openerId
   */
  public FormPage(final IPageMap _pageMap, final UUID _commandUUID,
                  final String _oid, final String _openerId) {
    super(_pageMap, new FormModel(new UIForm(_commandUUID, _oid, _openerId)),
                                  null);
    this.addComponents();
  }

  /**
   * @param _commandUUID
   * @param _oid
   */
  public FormPage(final UUID _commandUUID, final String _oid) {
   this(_commandUUID, _oid, (ModalWindowContainer) null);
  }

  /**
   * @param commandUUID
   * @param oid
   * @param modalWindow
   */
  public FormPage(final UUID commandUUID, final String oid,
                  final ModalWindowContainer _modalWindow) {
    super(new FormModel(new UIForm(commandUUID,oid)), _modalWindow);
    this.addComponents();
  }

  protected void addComponents() {
    final UIForm model = (UIForm) super.getDefaultModelObject();

    if (!model.isInitialised()) {
      model.execute();
    }

    add(StaticHeaderContributor.forCss(CSS));

    final FormContainer form = new FormContainer("form");
    add(form);
    super.addComponents(form);

    final WebMarkupContainer script = new WebMarkupContainer("selectscript");
    this.add(script);
    script.setVisible(model.isCreateMode()
        || model.isEditMode()
        || model.isSearchMode());
    updateFormContainer(this, form, model);
  }

  /**
   * @param _page
   * @param _form
   * @param _model
   */
  public static void updateFormContainer(final Page _page,
                                         final FormContainer _form,
                                         final UIForm _model) {

    if (!_model.isInitialised()) {
      _model.execute();
    }

    int i = 0;
    final RepeatingView elementRepeater = new RepeatingView("elementRepeater");
    _form.add(elementRepeater);
    for (final Element element : _model.getElements()) {
      if (element.getType().equals(ElementType.FORM)) {
        elementRepeater.add(new FormPanel(elementRepeater.newChildId(),
                                         _page,
                                         new FormModel(_model),
                                         (FormElement) element.getElement(),
                                         _form));
      } else if (element.getType().equals(ElementType.HEADING)) {
        final UIHeading headingmodel = (UIHeading) element.getElement();
        elementRepeater.add(new HeadingPanel(elementRepeater.newChildId(),
                                             headingmodel.getLabel(),
                                             headingmodel.getLevel()));
      } else if (element.getType().equals(ElementType.TABLE)) {
        i++;
        final UIFieldTable fieldTable = (UIFieldTable) element.getElement();
        fieldTable.setTableId(i);
        final TablePanel table = new TablePanel(elementRepeater.newChildId(),
                                                new TableModel(fieldTable),
                                                _page);
        final HeaderPanel header = new HeaderPanel(elementRepeater.newChildId(),
                                                   table);
        elementRepeater.add(header);
        elementRepeater.add(table);
      }
    }
  }
}
