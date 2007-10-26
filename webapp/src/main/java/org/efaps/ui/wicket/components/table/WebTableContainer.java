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

package org.efaps.ui.wicket.components.table;

import java.util.Iterator;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.ui.wicket.components.table.cell.CellPanel;
import org.efaps.ui.wicket.models.CellModel;
import org.efaps.ui.wicket.models.TableModel;
import org.efaps.ui.wicket.models.TableModel.RowModel;
import org.efaps.ui.wicket.pages.contentcontainer.ContentContainerPage;

/**
 * @author jmox
 * @version $Id:WebTableContainer.java 1510 2007-10-18 14:35:40Z jmox $
 */
public class WebTableContainer extends WebMarkupContainer {

  private static final long serialVersionUID = 4336311424526370681L;

  private final static String ROWID = "eFapsRow";

  private final static String CELLID = "eFapsCell";

  public WebTableContainer(final String _id, final IModel _model,
                           final Page _page) {
    super(_id, _model);
    initialise(_page);
  }

  private void initialise(final Page _page) {
    int rowcount = 0;

    final TableModel model = (TableModel) super.getModel();
    if (!model.isInitialised()) {
      model.execute();
    }
    boolean odd = true;

    for (final Iterator<RowModel> rowIter = model.getValues().iterator(); rowIter
        .hasNext(); odd = !odd) {
      final RowModel modelrow = rowIter.next();

      final RowContainer row = new RowContainer(ROWID + "_" + rowcount);
      if (odd) {
        row.add(new SimpleAttributeModifier("class", "eFapsTableRowOdd"));
      } else {
        row.add(new SimpleAttributeModifier("class", "eFapsTableRowEven"));
      }
      row.setOutputMarkupId(true);

      this.add(row);
      int cellcount = 0;
      if (model.isShowCheckBoxes()) {
        final CellContainer cell =
            new CellContainer(CELLID + "_" + rowcount + "_" + cellcount);
        row.add(cell);
        final CellCheckBoxComponent checkbox =
            new CellCheckBoxComponent("box" + rowcount, modelrow.getOids());
        cell.add(new SimpleAttributeModifier("width", "1%"));
        cell.add(checkbox);
      }
      cellcount++;

      for (CellModel cellmodel : modelrow.getValues()) {
        final CellPanel cellpanel =
            new CellPanel("cell" + "_" + rowcount + "_" + cellcount, cellmodel,
                ContentContainerPage.IFRAME_PAGEMAP_NAME.equals(_page
                    .getPageMapName()), model);
        row.add(cellpanel);
        cellcount++;
      }
      rowcount++;
    }

    if (rowcount == 0) {
      final Label nodata =
          new Label("test", DBProperties.getProperty("WebTable.NoData"));
      nodata.add(new SimpleAttributeModifier("class", "eFapsTableNoData"));
      this.add(nodata);
    }
  }

  @Override
  protected final void onRender(final MarkupStream markupStream) {
    final int markupStart = markupStream.getCurrentIndex();
    final Iterator<?> childs = this.iterator();
    while (childs.hasNext()) {
      markupStream.setCurrentIndex(markupStart);
      final Component child = (Component) childs.next();
      child.render(getMarkupStream());
    }

  }

}
