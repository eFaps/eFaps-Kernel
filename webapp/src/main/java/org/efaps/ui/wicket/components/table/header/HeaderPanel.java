/*
 * Copyright 2003-2007 The eFaps Team
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
 * Revision:        $Rev:1510 $
 * Last Changed:    $Date:2007-10-18 09:35:40 -0500 (Thu, 18 Oct 2007) $
 * Last Changed By: $Author:jmox $
 */

package org.efaps.ui.wicket.components.table.header;

import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;

import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.components.modalwindow.UpdateParentCallback;
import org.efaps.ui.wicket.models.HeaderModel;
import org.efaps.ui.wicket.models.TableModel;

/**
 * @author jmo
 * @version $Id:TableHeaderPanel.java 1510 2007-10-18 14:35:40Z jmox $
 */
public class HeaderPanel extends Panel {

  private static final long serialVersionUID = 1L;

  private final ModalWindowContainer modal =
      new ModalWindowContainer("eFapsModal");

  public HeaderPanel(final String _id, final TableModel _model) {
    super(_id, _model);

    final   RepeatingView cellRepeater = new RepeatingView("cellRepeater");
    add(cellRepeater);

    if (_model.isShowCheckBoxes()) {
      final    HeaderCellPanel cell = new HeaderCellPanel(cellRepeater.newChildId());
      cellRepeater.add(cell);
    }
    int i = 0;
    for (HeaderModel headermodel : _model.getHeaders()) {
      final   HeaderCellPanel cell =
          new HeaderCellPanel(cellRepeater.newChildId(), headermodel, _model);
      int width = 100 / _model.getWidthWeight() * headermodel.getWidth();
      i++;
      if (i == _model.getHeaders().size()) {
        width = width - (i * 2);
      } else {
        cell.add(new SimpleAttributeModifier("style", "width:" + width + "%"));
      }
      cellRepeater.add(cell);
    }
    add(this.modal);
    this.modal.setPageMapName("modal");
    this.modal.setWindowClosedCallback(new UpdateParentCallback(this,
        this.modal, false));

  }

  public final ModalWindowContainer getModal() {
    return this.modal;
  }

}
