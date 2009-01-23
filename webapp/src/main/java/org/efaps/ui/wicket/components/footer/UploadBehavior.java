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

import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.PageMap;
import org.apache.wicket.PageParameters;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.Session;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.behavior.StringHeaderContributor;
import org.apache.wicket.util.string.JavascriptUtils;

import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.ui.AbstractCommand.Target;
import org.efaps.ui.wicket.EFapsSession;
import org.efaps.ui.wicket.Opener;
import org.efaps.ui.wicket.components.FileUploadListener;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.models.AbstractModel;
import org.efaps.ui.wicket.models.TableModel;
import org.efaps.ui.wicket.models.objects.AbstractUIObject;
import org.efaps.ui.wicket.models.objects.UIForm;
import org.efaps.ui.wicket.pages.content.form.FormPage;
import org.efaps.ui.wicket.pages.content.table.TablePage;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.ui.wicket.pages.main.MainPage;
import org.efaps.util.EFapsException;

/**
 * Behavior enabling a form to make file uploads.
 *
 * @author jmox
 * @version $Id$
 */
public class UploadBehavior extends AbstractBehavior implements
    FileUploadListener {

  /**
   * Needed for serialization.
   */
  private static final long serialVersionUID = 1L;

  /**
   * This instance variable stores the Component this IBhevaior is bind to.
   */
  private Component component;

  /**
   * It this UploadBehavior is used inside a modal window it is stored.
   */
  private final ModalWindowContainer modalWindow;

  /**
   * Constructor setting the modal window.
   *
   * @param _modalWindow modal window to set
   */
  public UploadBehavior(final ModalWindowContainer _modalWindow) {
    this.modalWindow = _modalWindow;
  }

  /**
   * The component this behavior belongs to must be stored.
   * @param _form component to bind to
   */
  @Override
  public void bind(final Component _form) {
    super.bind(_form);
    this.component = _form;
  }

  /**
   * @see org.efaps.ui.wicket.components.FileUploadListener#onSubmit()
   */
  public void onSubmit() {

    final UIForm uiForm = (UIForm) this.component.getPage()
        .getDefaultModelObject();

    try {
      executeEvents(uiForm.getNewValues());
    } catch (final EFapsException e) {
      throw new RestartResponseException(new ErrorPage(e));
    }

    final StringBuilder script = new StringBuilder();
    if (uiForm.getTarget() == Target.MODAL) {

      script.append(JavascriptUtils.SCRIPT_OPEN_TAG)
        .append("  window.onload = function() {")
        .append(this.modalWindow.getReloadJavaScript())
        .append(ModalWindowContainer.getCloseJavacript())
        .append("}")
        .append(JavascriptUtils.SCRIPT_CLOSE_TAG);

    } else {

      final AbstractModel<?> openermodel
          = (AbstractModel<?>) ((EFapsSession) Session.get())
                                  .getOpener(uiForm.getOpenerId()).getModel();
      Class<? extends Page> clazz;
      if (openermodel instanceof TableModel) {
        clazz = TablePage.class;
      } else {
        clazz = FormPage.class;
      }

      final PageParameters parameters = new PageParameters();
      parameters.add(Opener.OPENER_PARAKEY, uiForm.getOpenerId());

      final CharSequence url
          = this.component.urlFor(PageMap.forName(MainPage.IFRAME_PAGEMAP_NAME),
                                  clazz,
                                  parameters);

      script.append(JavascriptUtils.SCRIPT_OPEN_TAG)
        .append("  window.onload = function() {")
        .append(" opener.location.href = '")
        .append(url)
        .append("'; self.close();")
        .append("  top.window.close();}")
        .append(JavascriptUtils.SCRIPT_CLOSE_TAG);

    }
    this.component.getRequestCycle().getResponsePage().add(
        new StringHeaderContributor(script.toString()));
  }

  /**
   * Method that executes the events which are related to the Model of the
   * ParentComponent.
   *
   * @param _other others
   * @return true if the ESJP returned the ReturnValue.TRUE , else false
   * @throws EFapsException on error
   */
  private boolean executeEvents(final Map<String, String[]> _other)
      throws EFapsException {

    boolean ret = true;
    final List<Return> returns =
        ((AbstractUIObject) this.component.getParent().getDefaultModelObject())
            .executeEvents(_other);
    for (final Return oneReturn : returns) {
      if (oneReturn.get(ReturnValues.TRUE) == null && !oneReturn.isEmpty()) {
        ret = false;
        break;
      }
    }
    return ret;
  }

}
