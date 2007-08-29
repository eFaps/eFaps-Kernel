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
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.PopupCloseLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.html.resources.StyleSheetReference;
import org.apache.wicket.model.IModel;

import org.efaps.admin.ui.CommandAbstract;
import org.efaps.webapp.models.ModelAbstract;

/**
 * @author jmo
 * @version $Id$
 * 
 */
public class FooterPanel extends Panel {

  private static final long serialVersionUID = -1722339596237748160L;

  public FooterPanel(final String _id, final IModel _model,
                     final ModalWindow _modalWindow) {
    super(_id, _model);
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

    final Link CreateEditSearchLink = new Link("CreateEditSearch") {
      private static final long serialVersionUID = 1L;

      public void onClick() {

      }
    };
    CreateEditSearchLink.add(new Image("eFapsButtonDone"));
    CreateEditSearchLink.add(new Label("eFapsButtonDoneLabel", label));
    add(CreateEditSearchLink);

    WebMarkupContainer CancelLink;

    if (command.getTarget() == CommandAbstract.TARGET_POPUP) {
      CancelLink = new PopupCloseLink("Cancel");
    } else if (_modalWindow != null) {
      CancelLink = (new AjaxLink("Cancel") {

        private static final long serialVersionUID = 1L;

        public void onClick(AjaxRequestTarget target) {

          _modalWindow.close(target);
        }
      });
    } else {
      CancelLink = new Link("Cancel") {
        private static final long serialVersionUID = 1L;

        public void onClick() {

        }
      };
    }

    CancelLink.add(new Image("eFapsButtonCancel"));
    CancelLink.add(new Label("eFapsButtonCancelLabel", "Cancel"));
    add(CancelLink);

  }

}
