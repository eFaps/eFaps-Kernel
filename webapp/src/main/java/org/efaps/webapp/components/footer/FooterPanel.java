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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.ComponentTag;
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
import org.efaps.webapp.components.FormContainer;
import org.efaps.webapp.models.ModelAbstract;

/**
 * @author jmo
 * @version $Id$
 */
public class FooterPanel extends Panel {

  private static final long serialVersionUID = -1722339596237748160L;

  private WebMarkupContainer closeLink;

  private final ModalWindow modalWindow;

  public FooterPanel(final String _id, final IModel _model,
                     final ModalWindow _modalWindow, FormContainer _form) {
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
          new SubmitAndCloseLink("CreateEditSearch", _form, model);
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
    this.closeLink = cancelLink;
  }

  public class SubmitAndCloseLink extends SubmitLink {

    private static final long serialVersionUID = 1L;

    public SubmitAndCloseLink(final String id, final Form form,
                              final IModel _model) {
      super(id, form);
      ModelAbstract model = (ModelAbstract) _model;
      if (model.getCommand().getTarget() == CommandAbstract.TARGET_MODAL) {
        this.add(new AjaxFormSubmitBehavior(form, "onclick") {

          private static final long serialVersionUID = 1L;

          @Override
          protected void onSubmit(final AjaxRequestTarget _target) {
            modalWindow.close(_target);
          }

        });
      }
    }

    @Override
    protected void onComponentTag(ComponentTag tag) {
      super.onComponentTag(tag);
      if (closeLink instanceof PopupCloseLink) {
        String onclick = (String) tag.getAttributes().get("onclick");
        String url =
            onclick.substring(0, onclick.length() - 13)
                + " window.self.close();";
        tag.put("onclick", url);

      }
    }

  }

  public class AjaxCancelLink extends AjaxLink {
    public AjaxCancelLink(String id) {
      super(id);
    }

    private static final long serialVersionUID = 1L;

    public void onClick(AjaxRequestTarget target) {
      modalWindow.close(target);
    }
  }
}
