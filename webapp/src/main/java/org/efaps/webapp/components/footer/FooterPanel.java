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

package org.efaps.webapp.components.footer;

import java.util.Map;

import org.apache.wicket.PageMap;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.PopupCloseLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.resources.StyleSheetReference;
import org.apache.wicket.model.IModel;

import org.efaps.admin.ui.CommandAbstract;
import org.efaps.webapp.EFapsSession;
import org.efaps.webapp.components.FormContainer;
import org.efaps.webapp.components.modalwindow.ModalWindowContainer;
import org.efaps.webapp.models.FormModel;
import org.efaps.webapp.models.ModelAbstract;
import org.efaps.webapp.models.TableModel;
import org.efaps.webapp.pages.MainPage;
import org.efaps.webapp.pages.WebFormPage;
import org.efaps.webapp.pages.WebTablePage;

/**
 * @author jmo
 * @version $Id$
 */
public class FooterPanel extends Panel {

  private static final long serialVersionUID = -1722339596237748160L;

  private final ModalWindowContainer modalWindow;

  public FooterPanel(final String _id, final IModel _model,
                     final ModalWindowContainer _modalWindow,
                     FormContainer _form) {
    super(_id, _model);
    this.modalWindow = _modalWindow;
    ModelAbstract model = (ModelAbstract) super.getModel();

    CommandAbstract command = model.getCommand();

    String label = null;
    if (model.isCreateMode()) {
      label = "Create";
    } else if (model.isEditMode()) {
      label = "Update";
    } else if (model.isSearchMode()) {
      label = "Search";
    }

    add(new StyleSheetReference("eFapsFooterPanelCSS", getClass(),
        "FooterPanel.css"));
    WebMarkupContainer createEditSearchLink = null;

    if (_form != null) {
      createEditSearchLink =
          new SubmitAndCloseLink("CreateEditSearch", model, _form);
    } else {

      createEditSearchLink = new Link("CreateEditSearch") {

        private static final long serialVersionUID = 1L;

        @Override
        public void onClick() {
          // TODO Auto-generated method stub

        }
      };
    }
    createEditSearchLink.add(new Image("eFapsButtonDone"));
    createEditSearchLink.add(new Label("eFapsButtonDoneLabel", label));
    add(createEditSearchLink);

    WebMarkupContainer cancelLink = null;
    if (command.getTarget() == CommandAbstract.TARGET_POPUP) {
      cancelLink = new PopupCloseLink("Cancel");
    } else if (_modalWindow != null) {
      cancelLink = new AjaxCancelLink("Cancel");
    }

    cancelLink.add(new Image("eFapsButtonCancel"));
    cancelLink.add(new Label("eFapsButtonCancelLabel", "Cancel"));
    add(cancelLink);

  }

  public class SubmitAndCloseLink extends SubmitLink {

    private static final long serialVersionUID = 1L;

    public SubmitAndCloseLink(final String id, final IModel _model,
                              final Form form) {
      super(id, form);
      this.add(new SubmitandCloseBehavior(_model, form));
    }
  }

  public class SubmitandCloseBehavior extends AjaxFormSubmitBehavior {

    private static final long serialVersionUID = 1L;

    private final IModel imodel;

    private final Form form;

    public SubmitandCloseBehavior(final IModel _model, final Form _form) {
      super(_form, "onclick");
      this.imodel = _model;
      this.form = _form;

    }

    @Override
    protected void onSubmit(AjaxRequestTarget _target) {

      Map<?, ?> para = this.form.getRequest().getParameterMap();
      if (this.form.getParent().getModel() instanceof FormModel) {
        ((FormModel) this.form.getParent().getModel()).update(para);
      }

      ModelAbstract model = (ModelAbstract) this.imodel;

      if (model.getCommand().getTarget() == CommandAbstract.TARGET_MODAL) {
        modalWindow.setUpdateParent(true);
        modalWindow.close(_target);
      }
      if (model.getCommand().getTarget() == CommandAbstract.TARGET_POPUP) {
        ModelAbstract openermodel =
            (ModelAbstract) ((EFapsSession) Session.get()).getOpenerModel();
        Class<?> clazz;
        if (openermodel instanceof TableModel) {
          clazz = WebTablePage.class;
        } else {
          clazz = WebFormPage.class;
        }

        CharSequence url =
            this.form.urlFor(PageMap.forName(MainPage.INLINEFRAMENAME), clazz,
                openermodel.getPageParameters());
        _target.appendJavascript("opener.location.href = '" + url
            + "'; self.close();");

      }

    }
  }

  public class AjaxCancelLink extends AjaxLink {
    public AjaxCancelLink(String id) {
      super(id);
    }

    private static final long serialVersionUID = 1L;

    public void onClick(AjaxRequestTarget target) {
      modalWindow.setUpdateParent(false);
      modalWindow.close(target);
    }
  }

}
