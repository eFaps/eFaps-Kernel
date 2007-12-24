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
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.ui.wicket.components.table;

import java.util.Iterator;

import org.apache.wicket.Page;
import org.apache.wicket.behavior.HeaderContributor;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;

import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.ui.wicket.components.table.row.RowPanel;
import org.efaps.ui.wicket.models.TableModel;
import org.efaps.ui.wicket.models.TableModel.RowModel;
import org.efaps.ui.wicket.pages.contentcontainer.ContentContainerPage;
import org.efaps.ui.wicket.resources.CSSResourceReference;

public class TablePanel extends Panel {

  private static final long serialVersionUID = 1L;

  public static final CSSResourceReference CSS =
      new CSSResourceReference(TablePanel.class, "TablePanel.css");

  public TablePanel(final String _id, final TableModel _model, final Page _page) {
    super(_id, _model);

    if (!_model.isInitialised()) {
      _model.execute();
    }
    this.setOutputMarkupId(true);
    this.add(new SimpleAttributeModifier("class", "eFapsTableBody"));

    add(HeaderContributor.forCss(CSS));

    final RepeatingView rowsRepeater = new RepeatingView("rowRepeater");
    add(rowsRepeater);

    if (_model.getValues().isEmpty()) {
      final Label nodata =
          new Label(rowsRepeater.newChildId(), DBProperties
              .getProperty("WebTable.NoData"));
      nodata.add(new SimpleAttributeModifier("class", "eFapsTableNoData"));
      rowsRepeater.add(nodata);
    } else {
      boolean odd = true;

      for (final Iterator<RowModel> rowIter = _model.getValues().iterator(); rowIter
          .hasNext(); odd = !odd) {

        final RowPanel row =
            new RowPanel(rowsRepeater.newChildId(), rowIter.next(), this,
                ContentContainerPage.IFRAME_PAGEMAP_NAME.equals(_page
                    .getPageMapName()));
        row.setOutputMarkupId(true);
        if (odd) {
          row.add(new SimpleAttributeModifier("class", "eFapsTableRowOdd"));
        } else {
          row.add(new SimpleAttributeModifier("class", "eFapsTableRowEven"));
        }
        rowsRepeater.add(row);
      }
    }
  }
}
