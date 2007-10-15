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

package org.efaps.webapp.components.menu;

import java.util.Map;

import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.form.Form;

import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.ui.CommandAbstract;
import org.efaps.util.EFapsException;
import org.efaps.webapp.components.modalwindow.ModalWindowContainer;
import org.efaps.webapp.models.AbstractModel;
import org.efaps.webapp.models.FormModel;
import org.efaps.webapp.models.MenuItemModel;
import org.efaps.webapp.models.TableModel;
import org.efaps.webapp.pages.ContentPage;
import org.efaps.webapp.pages.WebFormPage;
import org.efaps.webapp.pages.WebTablePage;
import org.efaps.webapp.pages.dialog.DialogPage;
import org.efaps.webapp.pages.error.ErrorPage;
import org.efaps.webapp.pages.main.MainPage;

/**
 * @author jmo
 * @version $Id$
 */
public class AjaxSubmitComponent extends AbstractMenuItemAjaxComponent {

  private static final long serialVersionUID = 1L;

  public AjaxSubmitComponent(final String id, final MenuItemModel _menuItem,
                             final Form _form) {
    super(id, _menuItem);
    this.add(new SubmitAndUpdateBehavior(_form));
  }

  @Override
  public String getJavaScript() {
    return ((SubmitAndUpdateBehavior) super.getBehaviors().get(0))
        .getJavaScript();
  }

  public class SubmitAndUpdateBehavior extends AjaxFormSubmitBehavior {

    private static final long serialVersionUID = 1L;

    private final Form form;

    public String getJavaScript() {
      String script = super.getEventHandler().toString();
      return "javascript:" + script.replace("'", "\"");
    }

    public SubmitAndUpdateBehavior(final Form _form) {
      super(_form, "onClick");
      this.form = _form;
    }

    @Override
    protected void onSubmit(final AjaxRequestTarget _target) {
      final MenuItemModel model =
          (MenuItemModel) super.getComponent().getModel();

      final Map<?, ?> para = this.form.getRequest().getParameterMap();

      if (model.isAskUser()) {
        final ModalWindowContainer modal;
        if (super.getComponent().getPage() instanceof MainPage) {
          modal = ((MainPage) super.getComponent().getPage()).getModal();
        } else {
          modal = ((ContentPage) super.getComponent().getPage()).getModal();
        }
        modal.setPageCreator(new ModalWindow.PageCreator() {

          private static final long serialVersionUID = 1L;

          public Page createPage() {
            return new DialogPage(modal, model, para,
                AjaxSubmitComponent.this);
          }
        });
        modal.setInitialHeight(150);
        modal.setInitialWidth(350);
        modal.show(_target);
      } else {
        CommandAbstract command =
            ((MenuItemModel) super.getComponent().getModel()).getCommand();

        if (command.hasEvents(EventType.UI_COMMAND_EXECUTE)) {
          try {
            String[] oids = (String[]) para.get("selectedRow");
            if (oids != null) {
              command.executeEvents(EventType.UI_COMMAND_EXECUTE,
                  ParameterValues.OTHERS, oids);
            } else {
              command.executeEvents(EventType.UI_COMMAND_EXECUTE);
            }
          } catch (EFapsException e) {
            throw new RestartResponseException(new ErrorPage(e));
          }
        }
        ((AbstractModel) this.form.getPage().getModel()).resetModel();
        Page page = null;
        if (this.form.getPage().getModel() instanceof TableModel) {
          page = new WebTablePage(this.form.getPage().getModel());
        } else if (this.form.getPage().getModel() instanceof FormModel) {
          page = new WebFormPage(this.form.getPage().getModel());
        }
        this.form.setResponsePage(page);
      }
    }

    @Override
    protected void onError(AjaxRequestTarget arg0) {
      // TODO Auto-generated method stub
    }
  }

}
