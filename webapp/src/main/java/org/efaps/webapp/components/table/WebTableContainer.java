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

package org.efaps.webapp.components.table;

import java.util.Iterator;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.webapp.components.table.cell.CellPanel;
import org.efaps.webapp.models.CellModel;
import org.efaps.webapp.models.TableModel;
import org.efaps.webapp.models.TableModel.RowModel;
import org.efaps.webapp.pages.contentcontainer.ContentContainerPage;

/**
 * @author jmox
 * @version $Id$
 */
public class WebTableContainer extends WebMarkupContainer {

  private static final long serialVersionUID = 4336311424526370681L;

  private static String ROWID = "eFapsRow";

  private static String CELLID = "eFapsCell";

  public WebTableContainer(String id, IModel model, Page _page) {
    super(id, model);
    initialise(_page);
  }

  private void initialise(Page _page) {
    int i = 0;

    TableModel model = (TableModel) super.getModel();
    if (!model.isInitialised()) {
      model.execute();
    }
    boolean odd = true;

    for (Iterator<RowModel> rowIter = model.getValues().iterator(); rowIter
        .hasNext(); odd = !odd) {
      RowModel modelrow = rowIter.next();

      RowContainer row = new RowContainer(ROWID + "_" + i);
      if (odd) {
        row.add(new SimpleAttributeModifier("class", "eFapsTableRowOdd"));
      } else {
        row.add(new SimpleAttributeModifier("class", "eFapsTableRowEven"));
      }
      row.setOutputMarkupId(true);

      this.add(row);
      int j = 0;
      if (model.isShowCheckBoxes()) {
        CellContainer cell = new CellContainer(CELLID + "_" + i + "_" + j);
        row.add(cell);
        CellCheckBoxComponent checkbox =
            new CellCheckBoxComponent("box" + i, modelrow.getOids());
        cell.add(new SimpleAttributeModifier("width", "1%"));
        cell.add(checkbox);
      }
      j++;

      for (CellModel cellmodel : modelrow.getValues()) {
        CellPanel cellpanel =
            new CellPanel("cell" + "_" + i + "_" + j, cellmodel,
                ContentContainerPage.IFRAME_PAGEMAP_NAME.equals(_page
                    .getPageMapName()), model);
        row.add(cellpanel);
        j++;
      }
      i++;
    }

    if (i == 0) {
      Label nodata =
          new Label("test", DBProperties.getProperty("WebApp_WebTable.NoData"));
      nodata.add(new SimpleAttributeModifier("class", "eFapsTableNoData"));
      this.add(nodata);
    }
  }

  @Override
  protected final void onRender(final MarkupStream markupStream) {
    final int markupStart = markupStream.getCurrentIndex();
    Iterator<?> childs = this.iterator();
    while (childs.hasNext()) {
      markupStream.setCurrentIndex(markupStart);
      Component child = (Component) childs.next();
      child.render(getMarkupStream());
    }

  }

}
