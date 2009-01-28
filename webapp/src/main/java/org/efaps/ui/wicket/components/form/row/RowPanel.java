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

package org.efaps.ui.wicket.components.form.row;

import org.apache.wicket.Page;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;

import org.efaps.ui.wicket.components.form.FormPanel;
import org.efaps.ui.wicket.components.form.cell.ValueCellPanel;
import org.efaps.ui.wicket.components.form.set.YPanel;
import org.efaps.ui.wicket.models.cell.FormCellModel;
import org.efaps.ui.wicket.models.cell.FormCellSetModel;
import org.efaps.ui.wicket.models.cell.UIFormCell;
import org.efaps.ui.wicket.models.cell.UIFormCellSet;
import org.efaps.ui.wicket.models.objects.UIForm;
import org.efaps.ui.wicket.models.objects.UIForm.FormRow;
import org.efaps.ui.wicket.pages.contentcontainer.ContentContainerPage;

/**
 * TODO description
 *
 * @author jmox
 * @version $Id$
 */
public class RowPanel extends Panel {

  /**
   * Needed for serialization.
   */
  private static final long serialVersionUID = 1L;

  public RowPanel(final String _wicketId, final IModel<FormRow> _model,
                  final UIForm _formmodel, final Page _page,
                  final FormPanel _formPanel) {
    super(_wicketId, _model);

    final FormRow row = (FormRow) super.getDefaultModelObject();
    final RepeatingView cellRepeater = new RepeatingView("cellRepeater");
    add(cellRepeater);

    for (final UIFormCell cell : row.getValues()) {
      if (!cell.isHideLabel()) {
        final Label labelCell =
            new Label(cellRepeater.newChildId(), cell.getCellLabel());
        cellRepeater.add(labelCell);

        if (cell.isRequired()) {
          labelCell.add(new SimpleAttributeModifier("class",
              "eFapsFormLabelRequired"));
          labelCell.setOutputMarkupId(true);
          _formPanel.addRequiredComponent(cell.getName(), labelCell);
        } else {
          labelCell.add(new SimpleAttributeModifier("class", "eFapsFormLabel"));
        }
        if (cell.getRowSpan() > 0) {
          labelCell.add(new SimpleAttributeModifier("rowspan",
                                     ((Integer) cell.getRowSpan()).toString()));
        }
      }
      Panel valueCell;
      if (cell instanceof UIFormCellSet) {
        valueCell = new YPanel(cellRepeater.newChildId(),
                               new FormCellSetModel((UIFormCellSet) cell));
      } else {
         valueCell =
          new ValueCellPanel(cellRepeater.newChildId(),
                             new FormCellModel(cell),
                             _formmodel,
                          ContentContainerPage.IFRAME_PAGEMAP_NAME.equals(_page
                                                      .getPageMapName()));
      }
      if (cell.getRowSpan() > 0) {
        valueCell.add(new SimpleAttributeModifier("rowspan",
                                    ((Integer) cell.getRowSpan()).toString()));
      }

      Integer colspan =
        2 * (_formmodel.getMaxGroupCount() - _model.getObject().getGroupCount())
        + 1;

      if (cell.isHideLabel()) {
        colspan++;
      }

      if (row.isRowSpan()) {
        colspan = colspan - 2;

      }
      valueCell.add(new SimpleAttributeModifier("colspan", colspan.toString()));
      cellRepeater.add(valueCell);
      valueCell.add(new SimpleAttributeModifier("class", "eFapsFormValue"));
    }
  }
}
