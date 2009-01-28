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
 * Revision:        $Rev:1510 $
 * Last Changed:    $Date:2007-10-18 09:35:40 -0500 (Thu, 18 Oct 2007) $
 * Last Changed By: $Author:jmox $
 */

package org.efaps.ui.wicket.components.menu;

import java.util.Map;

import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;

import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.ui.AbstractCommand;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.models.FormModel;
import org.efaps.ui.wicket.models.MenuItemModel;
import org.efaps.ui.wicket.models.TableModel;
import org.efaps.ui.wicket.models.objects.AbstractUIObject;
import org.efaps.ui.wicket.models.objects.UIForm;
import org.efaps.ui.wicket.models.objects.UIMenuItem;
import org.efaps.ui.wicket.models.objects.UITable;
import org.efaps.ui.wicket.pages.content.AbstractContentPage;
import org.efaps.ui.wicket.pages.content.form.FormPage;
import org.efaps.ui.wicket.pages.content.table.TablePage;
import org.efaps.ui.wicket.pages.dialog.DialogPage;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.ui.wicket.pages.main.MainPage;
import org.efaps.util.EFapsException;

/**
 * @author jmox
 * @version $Id:AjaxSubmitComponent.java 1510 2007-10-18 14:35:40Z jmox $
 */
public class AjaxSubmitComponent extends AbstractMenuItemAjaxComponent {

  private static final long serialVersionUID = 1L;

  public AjaxSubmitComponent(final String id, final IModel<UIMenuItem> _menuItem,
                             final Form<?> _form) {
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

    private final Form<?> form;

    public String getJavaScript() {
      final String script = super.getEventHandler().toString();
      return "javascript:" + script.replace("'", "\"");
    }

    public SubmitAndUpdateBehavior(final Form<?> _form) {
      super(_form, "onClick");
      this.form = _form;
    }

    @Override
    protected CharSequence getPreconditionScript() {
      // we have to override the original Script, because it breaks the eval in
      // the eFapsScript
      return null;
    }

    @Override
    protected void onSubmit(final AjaxRequestTarget _target) {
      final UIMenuItem uiMenuItem =
          (UIMenuItem) super.getComponent().getDefaultModelObject();

      final Map<?, ?> para = this.form.getRequest().getParameterMap();

      if (uiMenuItem.isAskUser()) {
        final ModalWindowContainer modal;
        if (super.getComponent().getPage() instanceof MainPage) {
          modal = ((MainPage) super.getComponent().getPage()).getModal();
        } else {
          modal =
              ((AbstractContentPage) super.getComponent().getPage()).getModal();
        }
        modal.setPageCreator(new ModalWindow.PageCreator() {

          private static final long serialVersionUID = 1L;

          public Page createPage() {
            return new DialogPage(modal, new MenuItemModel(uiMenuItem), para, AjaxSubmitComponent.this);
          }
        });
        modal.setInitialHeight(150);
        modal.setInitialWidth(350);
        modal.show(_target);
      } else {
        final AbstractCommand command =
            ((UIMenuItem) super.getComponent().getDefaultModelObject()).getCommand();

        if (command.hasEvents(EventType.UI_COMMAND_EXECUTE)) {
          try {
            final String[] oids = (String[]) para.get("selectedRow");
            if (oids != null) {
              command.executeEvents(EventType.UI_COMMAND_EXECUTE,
                  ParameterValues.OTHERS, oids);
            } else {
              command.executeEvents(EventType.UI_COMMAND_EXECUTE);
            }
          } catch (final EFapsException e) {
            throw new RestartResponseException(new ErrorPage(e));
          }
        }
       final AbstractUIObject uiObject = ((AbstractUIObject) this.form.getPage().getDefaultModelObject());
       uiObject.resetModel();

        Page page = null;
        if (uiObject instanceof UITable) {
          page = new TablePage(new TableModel( (UITable)uiObject));
        } else if (uiObject instanceof UIForm) {
          page = new FormPage(new FormModel((UIForm)  uiObject));
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
