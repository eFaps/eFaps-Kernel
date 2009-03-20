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
 * Revision:        $Rev:1489 $
 * Last Changed:    $Date:2007-10-15 17:50:46 -0500 (Mon, 15 Oct 2007) $
 * Last Changed By: $Author:jmox $
 */

package org.efaps.ui.wicket.pages.dialog;

import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.HeaderContributor;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.ui.wicket.EFapsSession;
import org.efaps.ui.wicket.behaviors.update.UpdateInterface;
import org.efaps.ui.wicket.components.LabelComponent;
import org.efaps.ui.wicket.components.button.Button;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.components.modalwindow.UpdateParentCallback;
import org.efaps.ui.wicket.models.objects.UIMenuItem;
import org.efaps.ui.wicket.pages.AbstractMergePage;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.resources.StaticHeaderContributor;
import org.efaps.util.EFapsException;

/**
 * This Page renders a Dialog for Userinterference.<br>
 * e.g. "Do you really want to...?"
 *
 * @author jmox
 * @version $Id$
 */
public class DialogPage extends AbstractMergePage {

  private static final long serialVersionUID = 1L;

  /**
   * reference to the StyleSheet of this Page stored in the eFaps-DataBase
   */
  private static final EFapsContentReference CSS =
      new EFapsContentReference(DialogPage.class, "DialogPage.css");

  /**
   * this instance variable stores the ModalWindow this Page is opened in
   */
  private final ModalWindowContainer modal;

  /**
   * this instance variable stores the Compoment wich called this DialogPage, so
   * that it can be accessed
   */
  private Component parent;



  /**
   * Constructor used for a DialogPage that renders a Question like: "Are you
   * sure that??" with a Cancel and a SubmitButton.
   *
   * @param _modal
   *                ModalWindow this Page is opend in
   * @param _model
   *                the MenuItem that called this DialogPage
   * @param _parameters
   *                Parameters wich must be past on, in case of submit
   * @param _parent
   *                the ParentComponent
   */
  public DialogPage(final ModalWindowContainer _modal,
                    final IModel<UIMenuItem> _model, final Map<?, ?> _parameters,
                    final Component _parent) {
    super(_model);
    this.parent = _parent;
    this.modal = _modal;
    final UIMenuItem menuItem = _model.getObject();

    final String cmdName = menuItem.getCommand().getName();

    this.add(StaticHeaderContributor.forCss(CSS));

    this.add(new Label("textLabel", DBProperties.getProperty(cmdName
        + ".Question")));

    this.add(new Button("submitButton", new AjaxSubmitLink(Button.LINKID,
        _model, _parameters), getLabel(cmdName, "Submit"), Button.ICON_ACCEPT));

    this.add(new Button("closeButton", new AjaxCloseLink(Button.LINKID),
        getLabel(cmdName, "Cancel"), Button.ICON_CANCEL));
  }

  /**
   * Constructor setting the ModalWindow
   *
   * @param _modal
   * @param _key
   */
  public DialogPage(final ModalWindowContainer _modal, final String _key,
      final boolean _isSniplett) {
    this(_modal, _isSniplett ? _key : DBProperties.getProperty(_key
        + ".Message"), getLabel(_key, "Close"), _isSniplett);
  }

  /**
   * @param _modal
   * @param _message
   * @param _button
   */
  public DialogPage(final ModalWindowContainer _modal, final String _message,
                    final String _button, final boolean _isSniplett) {
    super();
    this.modal = _modal;
    this.add(StaticHeaderContributor.forCss(CSS));


    if (_isSniplett) {
      this.add(new LabelComponent("textLabel", _message));
    } else {
      this.add(new Label("textLabel", _message));
    }

    this.add(new WebMarkupContainer("submitButton").setVisible(false));
    final AjaxCloseLink ajaxCloseLink = new AjaxCloseLink(Button.LINKID);
    this.add(new Button("closeButton", ajaxCloseLink,_button, Button.ICON_CANCEL));

    this.add(new HeaderContributor(new KeyListenerContributor(ajaxCloseLink)));
  }



  /**
   * method that gets the Value for the Buttons from the DBProperties
   *
   * @param _cmdName
   *                Name of the Command, that Label for the Button should be
   *                retrieved
   * @param _keytype
   *                type of the key e.g. "Cancel", "Submit", "Close"
   * @return
   */
  private static String getLabel(final String _cmdName, final String _keytype) {
    String ret;
    if (DBProperties.hasProperty(_cmdName + ".Button." + _keytype)) {
      ret = DBProperties.getProperty(_cmdName + ".Button." + _keytype);
    } else {
      ret = DBProperties.getProperty("default.Button." + _keytype);
    }
    return ret;
  }



  /**
   * AjaxLink that closes the ModalWindow this Page was opened in
   */
  public class AjaxCloseLink extends AjaxLink<Object> {

    private static final long serialVersionUID = 1L;

    public AjaxCloseLink(final String _wicketId) {
      super(_wicketId);
    }

    @Override
    public void onClick(final AjaxRequestTarget _target) {
      DialogPage.this.modal.close(_target);

    final StringBuilder bldr = new StringBuilder();
    bldr.append("var inp = top.frames[0].document")
      .append(".getElementById('eFapsContentDiv')")
      .append(".getElementsByTagName('input');")
      .append("if(inp!=null){")
      .append("  inp[0].focus();")
      .append("}");
    _target.appendJavascript(bldr.toString());
    }
  }

  /**
   * AjaxLink that submits the Parameters and closes the ModalWindow
   */
  public class AjaxSubmitLink extends AjaxLink<UIMenuItem> {

    private static final long serialVersionUID = 1L;

    /**
     * the Paramters that will be submited
     */
    private final Map<?, ?> parameters;

    public AjaxSubmitLink(final String _wicketId, final IModel<UIMenuItem> _model,
                          final Map<?, ?> _parameters) {
      super(_wicketId, _model);
      this.parameters = _parameters;
    }

    @Override
    public void onClick(final AjaxRequestTarget _target) {

      final UIMenuItem model =  getModelObject();

      try {
        model.executeEvents(this.parameters.get("selectedRow"));
      } catch (final EFapsException e) {
        throw new RestartResponseException(new ErrorPage(e));
      }

      final List<UpdateInterface> updates =
         ((EFapsSession) getSession()).getUpdateBehavior(model.getInstanceKey());
      if (updates != null) {
        for (final UpdateInterface update : updates) {
          if (update.isAjaxCallback()) {
            update.setInstanceKey(model.getInstanceKey());
            update.setMode(model.getMode());
            _target.prependJavascript(update.getAjaxCallback());
          }
        }
      }
      DialogPage.this.modal.setWindowClosedCallback(new UpdateParentCallback(
          DialogPage.this.parent, DialogPage.this.modal));
      DialogPage.this.modal.setUpdateParent(true);
      DialogPage.this.modal.close(_target);
    }

  }
  private static final class KeyListenerContributor implements IHeaderContributor {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private final Component component;

    /**
     * @param ajaxCloseLink
     */
    public KeyListenerContributor(final Component _component) {
     this.component = _component;
    }

    public void renderHead(final IHeaderResponse _iheaderresponse) {

      final StringBuilder bldr = new StringBuilder();
      bldr.append("<script type=\"text/javascript\">")
      .append("function pressed (_event) {")
        .append("var b=Wicket.$('").append(this.component.getMarkupId())
        .append("'); if (typeof(b.onclick) != 'undefined') { b.onclick();  }")
        .append("}")
        .append("window.onkeydown = pressed;").append("</script>");
      _iheaderresponse.renderString(bldr);
    }
  }
}
