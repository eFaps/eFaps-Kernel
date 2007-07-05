/*
 * Copyright 2003 - 2007 The eFaps Team
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

package org.efaps.webapp.tags;

import java.util.Iterator;
import java.util.List;

import javax.faces.el.VariableResolver;

import org.apache.myfaces.custom.dialog.ModalDialog;

import org.efaps.beans.ToolbarBean;

public class UIModalDialogs extends ModalDialog {

  public static final String COMPONENT_TYPE =
      "org.efaps.webapp.tags.ModalDialogs";

  public static final String COMPONENT_FAMILY =
      "org.efaps.webapp.tags.ModalDialogs";

  private List modaldialogs;

  private Iterator iterator;

  private EFapsModalDialog activemodaldialog;

  public UIModalDialogs() {
    super();
    setRendererType(null);
  }

  @Override
  public String getFamily() {
    return COMPONENT_FAMILY;
  }

  @Override
  public String getDialogId() {
    return this.activemodaldialog.getDialogId();
  }

  @Override
  public String getDialogVar() {
    return this.activemodaldialog.getDialogVar();
  }

  @Override
  public String getHiderIds() {
    return "cancel" + this.activemodaldialog.getDialogVar();
  }

  public Iterator getIterator() {
    if (iterator == null) {
      iterator = getDialogList().iterator();
    }
    return iterator;

  }

  public void setActiveModalDialog(final EFapsModalDialog _modaldialog) {
    this.activemodaldialog = _modaldialog;
  }

  private List getDialogList() {
    if (modaldialogs == null) {
      VariableResolver resolver =
          getFacesContext().getApplication().getVariableResolver();

      ToolbarBean bean =
          (ToolbarBean) resolver.resolveVariable(getFacesContext(),
              "menuTableToolbar");
      modaldialogs = bean.getModalDialogs();
    }
    return modaldialogs;

  }

}
