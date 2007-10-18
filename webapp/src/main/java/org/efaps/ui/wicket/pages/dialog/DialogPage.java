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
 * Revision:        $Rev:1489 $
 * Last Changed:    $Date:2007-10-15 17:50:46 -0500 (Mon, 15 Oct 2007) $
 * Last Changed By: $Author:jmox $
 */

package org.efaps.ui.wicket.pages.dialog;

import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.resources.StyleSheetReference;

import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.ui.CommandAbstract;
import org.efaps.util.EFapsException;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.components.modalwindow.UpdateParentCallback;
import org.efaps.ui.wicket.models.MenuItemModel;
import org.efaps.ui.wicket.pages.error.ErrorPage;

/**
 * @author jmox
 * @version $Id:DialogPage.java 1489 2007-10-15 22:50:46Z jmox $
 */
public class DialogPage extends WebPage {

  private static final long serialVersionUID = 1L;

  private final ModalWindowContainer modal;

  private Component parent;

  public DialogPage(final ModalWindowContainer _modal,
                    final MenuItemModel _model, final Map<?, ?> _parameters,
                    final Component _parent) {
    super(_model);
    this.parent = _parent;
    this.modal = _modal;
    String commandName = _model.getCommand().getName();
    add(new StyleSheetReference("css", getClass(), "DialogPage.css"));

    this.add(new Label("textLabel", getQuestion(commandName)));
    AjaxSubmitLink submit =
        new AjaxSubmitLink("submitButton", _model, _parameters);
    this.add(submit);
    submit.add(new Label("submitButtonLabel", getSubmitText(commandName)));
    AjaxCloseLink button = new AjaxCloseLink("closeButton");
    this.add(button);
    button.add(new Label("closeButtonLabel", getCancelText(commandName)));

  }

  public DialogPage(final ModalWindowContainer _modal, final String _message,
                    final String _button) {
    this.modal = _modal;
    add(new StyleSheetReference("css", getClass(), "DialogPage.css"));
    this.add(new Label("textLabel", _message));
    this.add(new WebMarkupContainer("submitButton").setVisible(false));
    AjaxCloseLink button = new AjaxCloseLink("closeButton");
    this.add(button);
    button.add(new Label("closeButtonLabel", _button));
  }

  private String getCancelText(final String _commandName) {
    String ret;
    if (DBProperties.hasProperty(_commandName + ".Cancel")) {
      ret = DBProperties.getProperty(_commandName + ".Cancel");
    } else {
      ret = DBProperties.getProperty("webapp.EFapsModalDialog.Cancel");
    }
    return ret;
  }

  private String getSubmitText(final String _commandName) {
    String ret;
    if (DBProperties.hasProperty(_commandName + ".Submit")) {
      ret = DBProperties.getProperty(_commandName + ".Submit");
    } else {
      ret = DBProperties.getProperty("webapp.EFapsModalDialog.Submit");
    }
    return ret;
  }

  private String getQuestion(final String _commandName) {
    return DBProperties.getProperty(_commandName + ".Question");
  }

  public class AjaxCloseLink extends AjaxLink {

    private static final long serialVersionUID = 1L;

    public AjaxCloseLink(String id) {
      super(id);
    }

    @Override
    public void onClick(final AjaxRequestTarget _target) {
      DialogPage.this.modal.close(_target);
    }

  }

  public class AjaxSubmitLink extends AjaxLink {

    private final Map<?, ?> parameters;

    public AjaxSubmitLink(final String id, final MenuItemModel model,
                          final Map<?, ?> _parameters) {
      super(id, model);
      this.parameters = _parameters;
    }

    private static final long serialVersionUID = 1L;

    @Override
    public void onClick(final AjaxRequestTarget _target) {

      CommandAbstract command = ((MenuItemModel) getModel()).getCommand();

      if (command.hasEvents(EventType.UI_COMMAND_EXECUTE)) {
        try {
          String[] oids = (String[]) this.parameters.get("selectedRow");
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
      DialogPage.this.modal.setWindowClosedCallback(new UpdateParentCallback(
          DialogPage.this.parent, DialogPage.this.modal));
      DialogPage.this.modal.setUpdateParent(true);
      DialogPage.this.modal.close(_target);
    }

  }
}
