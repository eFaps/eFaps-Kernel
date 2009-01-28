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

import org.apache.wicket.markup.html.form.SubmitLink;
import org.efaps.ui.wicket.components.FormContainer;
import org.efaps.ui.wicket.models.objects.AbstractUIObject;

/**
 * Link using Ajax to submit the Form and close the ModalWindow or the PopUp
 * this FooterPanel is imbeded.
 *
 * @author jmox
 * @version $Id$
 */
public class AjaxSubmitCloseLink extends SubmitLink {

  private static final long serialVersionUID = 1L;

  public AjaxSubmitCloseLink(final String _wicketid,
                             final AbstractUIObject _model,
                             final FormContainer _form) {
    super(_wicketid, _form);
    this.add(new AjaxSubmitCloseBehavior(_model, _form));
    _form.setDefaultSubmit(this);
  }
}
