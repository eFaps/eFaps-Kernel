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
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.model.Model;

import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.ui.wicket.components.LabelComponent;
import org.efaps.ui.wicket.models.cell.UIFormCellCmd;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author jmox
 * @version $Id$
 */
public class AjaxExecuteLink extends AjaxLink<UIFormCellCmd> {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  /**
   * @param linkid
   * @param uiObject
   */
  public AjaxExecuteLink(final String _wicketId, final UIFormCellCmd _uiObject) {
    super(_wicketId, new Model<UIFormCellCmd>(_uiObject));
  }

  @Override
  public void onClick(final AjaxRequestTarget _target) {

    final UIFormCellCmd uiObject = (UIFormCellCmd) getDefaultModelObject();
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
    final CommandCellPanel cmdCell = this.findParent(CommandCellPanel.class);
    cmdCell.addOrReplace(new LabelComponent("targetBottom", snip.toString()));
    _target.addComponent(cmdCell);

System.out.println("cklick");
  }

}
