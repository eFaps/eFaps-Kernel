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
import org.apache.wicket.model.IModel;

import org.efaps.ui.wicket.components.form.FormPanel;
import org.efaps.ui.wicket.components.form.cell.ValueCellPanel;
import org.efaps.ui.wicket.models.cell.FormCellModel;
import org.efaps.ui.wicket.models.cell.UIFormCell;
import org.efaps.ui.wicket.models.objects.UIForm;
import org.efaps.ui.wicket.models.objects.UIForm.FormRow;

import org.efaps.ui.wicket.pages.contentcontainer.ContentContainerPage;

public class RowPanel extends Panel {

  private static final long serialVersionUID = 1L;

  public RowPanel(final String _wicketId, final IModel<FormRow> _model,
                  final UIForm _formmodel, final Page _page,
                  final FormPanel _formPanel) {
    super(_wicketId, _model);
    final FormRow row = (FormRow) super.getDefaultModelObject();
    final RepeatingView cellRepeater = new RepeatingView("cellRepeater");
    add(cellRepeater);

    for (final UIFormCell cell : row.getValues()) {

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

      final ValueCellPanel valueCell =
          new ValueCellPanel(cellRepeater.newChildId(),new FormCellModel( cell),
              ContentContainerPage.IFRAME_PAGEMAP_NAME.equals(_page
                  .getPageMapName()));
      final Integer colspan =
          2 * (_formmodel.getMaxGroupCount() - _model.getObject().getGroupCount()) + 1;
      valueCell.add(new SimpleAttributeModifier("colspan", colspan.toString()));
      cellRepeater.add(valueCell);
      valueCell.add(new SimpleAttributeModifier("class", "eFapsFormValue"));

    }
  }
}
