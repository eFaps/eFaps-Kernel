/*
 * Copyright 2003-2008 The eFaps Team
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
 * Revision:        $Rev:  $
 * Last Changed:    $Date:  $
 * Last Changed By: $Author: $
 */

package org.efaps.ui.wicket.components.form.set;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.efaps.ui.wicket.components.LabelComponent;
import org.efaps.ui.wicket.models.cell.UIFormCellSet;

/**
 * TODO comment
 *
 * @author jmox
 * @version $Id: $
 */
public class XPanel extends Panel{

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  /**
   * @param id
   * @param model
   */
  public XPanel(final String id, final IModel<UIFormCellSet> _model, final int _y) {
    super(id, _model);
    final UIFormCellSet set = (UIFormCellSet) super.getDefaultModelObject();
    final RepeatingView xRepeater = new RepeatingView("xRepeater");
    add(xRepeater);

    for (int x=0; x <set.getXsize();x++){
      final LabelComponent label = new LabelComponent(xRepeater.newChildId(), set.getXYValue(x,_y));
      xRepeater.add(label);
    }
  }
}
