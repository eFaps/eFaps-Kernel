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
import org.efaps.ui.wicket.models.cell.UIFormCellSet;

/**
 * TODO comment
 *
 * @author jmox
 * @version $Id: $
 */
public class YPanel extends Panel{

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  /**
   * @param id
   * @param _model
   */
  public YPanel(final String id, final IModel<UIFormCellSet> _model) {
    super(id, _model);
    final UIFormCellSet set = (UIFormCellSet) super.getDefaultModelObject();
    final RepeatingView yRepeater = new RepeatingView("yRepeater");
    add(yRepeater);

    for (int y=0;y<set.getYsize();y++){
      final XPanel xpanel = new XPanel(yRepeater.newChildId(), _model,y);
      yRepeater.add(xpanel);
    }
  }

}
