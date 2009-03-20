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
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.ui.wicket.components.footer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.wicket.Page;
import org.apache.wicket.PageMap;
import org.apache.wicket.PageParameters;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;

import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.ui.AbstractCommand.Target;
import org.efaps.ui.wicket.EFapsSession;
import org.efaps.ui.wicket.Opener;
import org.efaps.ui.wicket.behaviors.update.UpdateInterface;
import org.efaps.ui.wicket.components.FormContainer;
import org.efaps.ui.wicket.components.form.DateFieldWithPicker;
import org.efaps.ui.wicket.components.form.FormPanel;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.models.TableModel;
import org.efaps.ui.wicket.models.objects.AbstractUIObject;
import org.efaps.ui.wicket.models.objects.UIForm;
import org.efaps.ui.wicket.pages.content.AbstractContentPage;
import org.efaps.ui.wicket.pages.content.form.FormPage;
import org.efaps.ui.wicket.pages.content.table.TablePage;
import org.efaps.ui.wicket.pages.dialog.DialogPage;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.util.EFapsException;

/**
 * TODO description.
 *
 * @author jmox
 * @version $Id$
 */
public class AjaxSubmitCloseBehavior extends AjaxFormSubmitBehavior {

  /**
   * Needed for serialization.
   */
  private static final long serialVersionUID = 1L;

  /**
   * Instance variable storing the model, because the super classes of a
   * behavior, doesn't store the model.
   */
  private final AbstractUIObject uiObject;

  /**
   * Instance variable storing the form to be submited.
   */
  private final FormContainer form;

  /**
   * Constructor.
   *
   * @param _uiobject UUIOBject
   * @param _form     form
   */
  public AjaxSubmitCloseBehavior(final AbstractUIObject _uiobject,
                                 final FormContainer _form) {
    super(_form, "onclick");
    this.uiObject = _uiobject;
    this.form = _form;
  }

  /**
   * On submit the action must be done.
   *
   * @param _target AjaxRequestTarget
   */
  @Override
  protected void onSubmit(final AjaxRequestTarget _target) {

    final Map<String, String[]> others = new HashMap<String, String[]>();
    final String[] other = getComponent().getRequestCycle().getRequest()
        .getParameters("selectedRow");
    others.put("selectedRow", other);

    convertDateFieldValues();
    if (checkForRequired(_target) && (validateForm(_target, others))) {

      if (this.uiObject instanceof UIForm
          && ((UIForm) this.uiObject).isFileUpload()) {
        doFileUpload(_target);
      } else {
        if (this.uiObject instanceof UIForm) {
          others.putAll(((UIForm) this.uiObject).getNewValues());
        }
        try {
          if (!executeEvents(_target, others)) {
            return;
          }
        } catch (final EFapsException e) {
          final ModalWindowContainer modal =
              ((AbstractContentPage) getComponent().getPage()).getModal();
          modal.setPageCreator(new ModalWindow.PageCreator() {

            private static final long serialVersionUID = 1L;

            public Page createPage() {
              return new ErrorPage(e);
            }
          });
          modal.show(_target);
          return;
        }

        final FooterPanel footer = getComponent().findParent(FooterPanel.class);
        // if inside a modal
        if (this.uiObject.getCommand().getTarget() == Target.MODAL) {
          footer.getModalWindow().setReloadChild(true);
          footer.getModalWindow().close(_target);
        } else {
          final Opener opener = ((EFapsSession) Session.get())
              .getOpener(this.uiObject.getOpenerId());
          // mark the opener that it can be removed
          opener.setMarked4Remove(true);

          Class<? extends Page> clazz;
          if (opener.getModel() instanceof TableModel) {
            clazz = TablePage.class;
          } else {
            clazz = FormPage.class;
          }

          final PageParameters parameters = new PageParameters();
          parameters.add(Opener.OPENER_PARAKEY, this.uiObject.getOpenerId());

          final CharSequence url = this.form.urlFor(PageMap.forName(opener
              .getPageMapName()), clazz, parameters);

          _target.appendJavascript("opener.location.href = '" + url
              + "'; self.close();");
        }
        footer.setSuccess(true);

        // execute the CallBacks
        final List<UpdateInterface> updates =
            ((EFapsSession) getComponent().getSession())
                .getUpdateBehavior(this.uiObject.getInstanceKey());
        if (updates != null) {
          for (final UpdateInterface update : updates) {
            if (update.isAjaxCallback()) {
              update.setInstanceKey(this.uiObject.getInstanceKey());
              update.setMode(this.uiObject.getMode());
              _target.prependJavascript(update.getAjaxCallback());
            }
          }
        }
      }
    }
  }

  /**
   * Method used to convert the date value from the ui in date values for eFaps.
   */
  private void convertDateFieldValues() {
    final FormPanel formpl = getFormPanel();
    if (formpl != null) {
      for (final DateFieldWithPicker datepicker : formpl.getDateComponents()) {
        final Map<String, String[]> map =
          getComponent().getRequestCycle().getRequest().getParameterMap();

        if (map.containsKey(datepicker.getInputName())) {
          final String[] value = map.get(datepicker.getInputName());
          map.put(datepicker.getInputName(),
                  new String[]{datepicker.getDateAsString(value[0])});
        }
      }
    }
  }

  /**
   * Method to enable file upload.
   *
   * @param _target AjaxRequestTarget
   */
  private void doFileUpload(final AjaxRequestTarget _target) {
    final StringBuilder script = new StringBuilder();
    script.append("var f=document.getElementById('")
          .append(this.form.getMarkupId())
          .append("');f.onsubmit=undefined;f.action=\"")
          .append(this.form.getActionUrl())
          .append("\";f.submit();");
    this.form.setFileUpload(true);
    _target.appendJavascript(script.toString());
  }

  /**
   * Method is not used, but needed from the api.
   *
   * @param _target AjaxRequestTarget
   */
  @Override
  protected void onError(final AjaxRequestTarget _target) {
    // not useful here
  }

  /**
   * In case of a file upload a precondition is needed.
   * @return precondition JavaScript
   */
  @Override
  protected CharSequence getPreconditionScript() {
    String ret = null;
    if (this.uiObject instanceof UIForm
        && ((UIForm) this.uiObject).isFileUpload()) {
      ret = "return eFapsFileInput()";
    }
    return ret;
  }

  /**
   * Execute the events which are related to CommandAbstract calling the Form.
   *
   * @param _target AjaxRequestTarget to be used in the case a ModalPage should
   *                be called
   * @param _other  Parameters to be passed on to the Event
   * @return true if the events where executed successfully, otherwise false
   * @throws EFapsException on error
   */
  private boolean executeEvents(final AjaxRequestTarget _target,
                                final Map<String, String[]> _other)
      throws EFapsException {
    boolean ret = true;
    final List<Return> returns =
        ((AbstractUIObject) this.form.getParent().getDefaultModelObject())
            .executeEvents(_other);
    for (final Return oneReturn : returns) {
      if (oneReturn.get(ReturnValues.TRUE) == null && !oneReturn.isEmpty()) {
        boolean sniplett = false;
        String key = (String) oneReturn.get(ReturnValues.VALUES);
        if (key == null) {
          key = (String) oneReturn.get(ReturnValues.SNIPLETT);
          sniplett = true;
        }
        showDialog(_target, key, sniplett);

        ret = false;
        break;
      }
    }
    return ret;
  }

  /**
   * Executes the Validation-Events related to the CommandAbstract which called
   * this Form.
   *
   * @param _target AjaxRequestTarget to be used in the case a ModalPage should
   *                be called
   * @return true if the Validation was valid, otherwise false
   */
  private boolean validateForm(final AjaxRequestTarget _target,
                               final Map<String, String[]> _other) {
    boolean ret = true;

    final List<Return> validation = ((AbstractUIObject) this.form.getParent()
        .getDefaultModelObject()).validate(_other);

    for (final Return oneReturn : validation) {
      if (oneReturn.get(ReturnValues.TRUE) == null) {
        boolean sniplett = false;
        String key = (String) oneReturn.get(ReturnValues.VALUES);
        if (key == null) {
          key = (String) oneReturn.get(ReturnValues.SNIPLETT);
          sniplett = true;
        }
        showDialog(_target, key, sniplett);

        ret = false;
        break;
      }
    }
    return ret;
  }

  /**
   * Method checking if the mandatory field of the Form are filled in, and if
   * not opens a WarnDialog and marks the fields in the Form via Ajax.
   *
   * @param _target
   *                RequestTarget used for this Request
   * @return true if all mandatory fields are filled, else false
   */
  private boolean checkForRequired(final AjaxRequestTarget _target) {
    boolean ret = true;
    if (this.form.getParent().getDefaultModel() instanceof TableModel) {
      return true;
    }

    final Map<?, ?> map = getComponent().getRequestCycle().getRequest()
        .getParameterMap();
    for (final Entry<String, Label> entry : getFormPanel()
        .getRequiredComponents().entrySet()) {

      final String[] values = (String[]) map.get(entry.getKey());
      final String value = values[0];
      if (value == null || value.length() == 0) {
        final Label label = entry.getValue();
        label.add(new SimpleAttributeModifier("class",
                                              "eFapsFormLabelRequiredForce"));
        _target.addComponent(label);
        ret = false;
      }
    }
    if (!ret) {
      showDialog(_target, "MandatoryDialog", false);
    }
    return ret;
  }


  /**
   * Method to get the FormPanel of this Page.
   * @return FormPanel
   */
  private FormPanel getFormPanel() {
    FormPanel ret = null;
    final Iterator<?> iterator = this.form.iterator();
    while (iterator.hasNext()) {
      final Object object = iterator.next();
      if (object instanceof WebMarkupContainer) {
        final Iterator<?> iterator2 = ((WebMarkupContainer) object).iterator();
        while (iterator2.hasNext()) {
          final Object object2 = iterator2.next();
          if (object2 instanceof FormPanel) {
            ret = (FormPanel) object2;
            break;
          }
        }
        break;
      }
    }
    return ret;
  }

  /**
   * Shows a modal DialogPage.
   *
   * @param _target AjaxRequestTarget to be used for opening the modal
   *                DialogPage
   * @param _key    the Key to get the DBProperties from the eFapsDataBaase or
   *                a code sniplett
   * @param _isSniplett is the parameter _key a key to a property or a sniplett
   */
  private void showDialog(final AjaxRequestTarget _target, final String _key,
                          final boolean _isSniplett) {
    final ModalWindowContainer modal =
        ((AbstractContentPage) getComponent().getPage()).getModal();

    modal.setResizable(false);
    modal.setInitialWidth(20);
    modal.setInitialHeight(12);
    modal.setWidthUnit("em");
    modal.setHeightUnit("em");
    modal.setPageMapName("warn");

    modal.setPageCreator(new ModalWindow.PageCreator() {

      private static final long serialVersionUID = 1L;

      public Page createPage() {
        return new DialogPage(modal, _key, _isSniplett);
      }
    });
    modal.show(_target);
  }
}
