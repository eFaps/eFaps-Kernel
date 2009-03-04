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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.model.Model;

import org.efaps.ui.wicket.models.FormModel;
import org.efaps.ui.wicket.models.objects.AbstractUIObject;
import org.efaps.ui.wicket.models.objects.UIForm;
import org.efaps.ui.wicket.models.objects.UIWizardObject;
import org.efaps.ui.wicket.pages.content.form.FormPage;

/**
 * Class renders a Link used on a search result to return to the previous
 * page and revise the search.
 *
 * @author jmox
 * @version $Id$
 */
public class AjaxReviseLink extends AjaxLink<AbstractUIObject> {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  /**
   * @param _wicketId  wicket id of this component
   * @param _uiObject  uiobject for this component
   */
  public AjaxReviseLink(final String _wicketId,
                        final AbstractUIObject _uiObject) {
    super(_wicketId, new Model<AbstractUIObject>(_uiObject));
  }

  /**
   * On click the previous page will be restored using wizard from the uiobject.
   * @param _target target for this request
   */
  @Override
  public void onClick(final AjaxRequestTarget _target) {
    final AbstractUIObject uiobject
                                  = (AbstractUIObject) getDefaultModelObject();
    final UIWizardObject wizard = uiobject.getWizard();
    final AbstractUIObject newForm = wizard.getPrevious();
    newForm.setWizardCall(true);
    newForm.resetModel();
    final FormPage page = new FormPage(new FormModel((UIForm) newForm));
    getRequestCycle().setResponsePage(page);
  }
}
