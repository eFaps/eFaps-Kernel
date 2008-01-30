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

package org.efaps.ui.wicket.components.form.row;

import org.apache.wicket.Page;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;

import org.efaps.ui.wicket.components.form.FormPanel;
import org.efaps.ui.wicket.components.form.cell.ValueCellPanel;
import org.efaps.ui.wicket.models.FormModel;
import org.efaps.ui.wicket.models.FormModel.FormRowModel;
import org.efaps.ui.wicket.models.cell.FormCellModel;
import org.efaps.ui.wicket.pages.contentcontainer.ContentContainerPage;

public class RowPanel extends Panel {

  private static final long serialVersionUID = 1L;

  public RowPanel(final String _wicketId, final FormRowModel _model,
                  final FormModel _formmodel, final Page _page,
                  final FormPanel _formPanel) {
    super(_wicketId, _model);

    RepeatingView cellRepeater = new RepeatingView("cellRepeater");
    add(cellRepeater);

    for (FormCellModel cellmodel : _model.getValues()) {

      Label labelCell =
          new Label(cellRepeater.newChildId(), cellmodel.getCellLabel());
      cellRepeater.add(labelCell);

      if (cellmodel.isRequired()) {
        labelCell.add(new SimpleAttributeModifier("class",
            "eFapsFormLabelRequired"));
        labelCell.setOutputMarkupId(true);
        _formPanel.addRequiredComponent(cellmodel.getName(), labelCell);
      } else {
        labelCell.add(new SimpleAttributeModifier("class", "eFapsFormLabel"));
      }

      ValueCellPanel valueCell =
          new ValueCellPanel(cellRepeater.newChildId(), cellmodel,
              ContentContainerPage.IFRAME_PAGEMAP_NAME.equals(_page
                  .getPageMapName()));
      Integer colspan =
          2 * (_formmodel.getMaxGroupCount() - _model.getGroupCount()) + 1;
      valueCell.add(new SimpleAttributeModifier("colspan", colspan.toString()));
      cellRepeater.add(valueCell);
      valueCell.add(new SimpleAttributeModifier("class", "eFapsFormValue"));

    }
  }
}
