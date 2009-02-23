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

package org.efaps.ui.wicket.components.form.command;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.markup.ComponentTag;

import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.ui.wicket.components.FormContainer;
import org.efaps.ui.wicket.components.LabelComponent;
import org.efaps.ui.wicket.models.cell.UIFormCellCmd;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author jmox
 * @version $Id$
 */
public class AjaxCmdBehavior extends AjaxFormSubmitBehavior {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  /**
   * @param event
   */
  public AjaxCmdBehavior(final FormContainer _form) {
    super(_form, "onclick");
  }

  /**
   * This Method returns the JavaScript which is executed by the
   * JSCooKMenu.
   *
   * @return String with the JavaScript
   */
  public String getJavaScript() {
    final String script = super.getEventHandler().toString();
    return script;
  }

  @Override
  protected void onError(final AjaxRequestTarget ajaxrequesttarget) {
    // TODO Auto-generated method stub

  }

  @Override
  protected void onSubmit(final AjaxRequestTarget _target) {

    final UIFormCellCmd uiObject = (UIFormCellCmd) getComponent()
        .getDefaultModelObject();

    final StringBuilder snip = new StringBuilder();
    try {
      final List<Return> returns = uiObject.executeEvents(null);
      for (final Return oneReturn : returns) {
        if (oneReturn.contains(ReturnValues.SNIPLETT)) {
          snip.append(oneReturn.get(ReturnValues.SNIPLETT));
        }
      }
    } catch (final EFapsException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    final CommandCellPanel cmdCell = getComponent().findParent(
        CommandCellPanel.class);
    cmdCell.addOrReplace(new LabelComponent("targetBottom", snip.toString()));
    _target.addComponent(cmdCell);

  }


  /**
   * Dont do anythging.
   */
  @Override
  protected void onComponentTag(final ComponentTag _tag) {
  }

}
