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

package org.efaps.ui.wicket.components.table.row;

import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;

import org.efaps.ui.wicket.components.table.TablePanel;
import org.efaps.ui.wicket.components.table.cell.CellPanel;
import org.efaps.ui.wicket.models.cell.TableCellModel;
import org.efaps.ui.wicket.models.cell.UITableCell;
import org.efaps.ui.wicket.models.objects.UIRow;
import org.efaps.ui.wicket.models.objects.UITable;

/**
 * @author jmox
 * @version $Id$
 */
public class RowPanel extends Panel {

  private static final long serialVersionUID = 1L;

  public RowPanel(final String _id, final IModel<UIRow> _model,
                  final TablePanel _tablePanel, final boolean _updateListMenu) {
    super(_id, _model);
    final UIRow uirow = (UIRow) super.getDefaultModelObject();

    final UITable uiTable = (UITable) _tablePanel.getDefaultModelObject();
    int i = uiTable.getTableId();

    final RepeatingView cellRepeater = new RepeatingView("cellRepeater");
    add(cellRepeater);

    if (uiTable.isShowCheckBoxes()) {
      final CellPanel cellpanel =
          new CellPanel(cellRepeater.newChildId(), uirow.getInstanceKeys());
      cellpanel.setOutputMarkupId(true);
      cellpanel.add(new SimpleAttributeModifier("class",
          "eFapsTableCheckBoxCell"));
      cellRepeater.add(cellpanel);
      i++;
    }

    for (final UITableCell cellmodel : uirow.getValues()) {

      final CellPanel cellpanel =
          new CellPanel(cellRepeater.newChildId(), new TableCellModel(cellmodel), _updateListMenu,
              uiTable);
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
