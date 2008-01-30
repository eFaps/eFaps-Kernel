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
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.ui.wicket.components.table.row;

import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;

import org.efaps.ui.wicket.components.table.TablePanel;
import org.efaps.ui.wicket.components.table.cell.CellPanel;
import org.efaps.ui.wicket.models.TableModel;
import org.efaps.ui.wicket.models.TableModel.RowModel;
import org.efaps.ui.wicket.models.cell.TableCellModel;

/**
 * @author jmox
 * @version $Id$
 */
public class RowPanel extends Panel {

  private static final long serialVersionUID = 1L;

  public RowPanel(final String _id, final RowModel _model,
                  final TablePanel _tablePanel, final boolean _updateListMenu) {
    super(_id, _model);
    final TableModel tablemodel = (TableModel) _tablePanel.getModel();
    int i = tablemodel.getTableId();
    final RepeatingView cellRepeater = new RepeatingView("cellRepeater");
    add(cellRepeater);

    if (tablemodel.isShowCheckBoxes()) {
      final CellPanel cellpanel =
          new CellPanel(cellRepeater.newChildId(), _model.getOids());
      cellpanel.setOutputMarkupId(true);
      cellpanel.add(new SimpleAttributeModifier("class",
          "eFapsTableCheckBoxCell"));
      cellRepeater.add(cellpanel);
      i++;
    }

    for (TableCellModel cellmodel : _model.getValues()) {

      final CellPanel cellpanel =
          new CellPanel(cellRepeater.newChildId(), cellmodel, _updateListMenu,
              tablemodel);
      cellpanel.setOutputMarkupId(true);
      if (cellmodel.isFixedWidth()) {
        cellpanel.add(new SimpleAttributeModifier("class",
            "eFapsTableCell eFapsCellFixedWidth" + i));
      } else {
        cellpanel.add(new SimpleAttributeModifier("class",
            "eFapsTableCell eFapsCellWidth" + i));
      }
      cellRepeater.add(cellpanel);
      i++;
    }

  }
}
