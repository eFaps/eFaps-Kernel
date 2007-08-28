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
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;

import org.efaps.webapp.models.TableModel;
import org.efaps.webapp.models.TableModel.CellModel;
import org.efaps.webapp.models.TableModel.RowModel;

/**
 * @author jmo
 * @version $Id$
 */
public class WebTableContainer extends WebMarkupContainer {
  private static final long serialVersionUID = 4336311424526370681L;

  private static String ROWID = "eFapsRow";

  private static String CELLID = "eFapsCell";

  private static String CELLVALUEID = "eFapsCellValue";

  public WebTableContainer(String id, IModel model) {
    super(id, model);
  }

  @Override
  protected void onBeforeRender() {
    super.onBeforeRender();
    initialise();
  }

  private void initialise() {
    int i = 0;
    try {
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

        for (CellModel value : modelrow.getValues()) {

          CellContainer cell = new CellContainer(CELLID + "_" + i + "_" + j);
          cell.setOutputMarkupId(true);
          row.add(cell);

          if (value.hasReference()) {
            if (getPage().getPageMapName().equals("content")) {
              cell.add(new CellAjaxLinkComponent(CELLVALUEID + "_" + i + "_"
                  + j, value.getOid(), value.getCellValue()));
            } else {
              cell.add(new CellLinkComponent(CELLVALUEID + "_" + i + "_" + j,
                  value.getOid(), value.getCellValue()));
            }
          } else {

            cell.add(new CellValueComponent(CELLVALUEID + "_" + i + "_" + j,
                value.getCellValue()));
          }
          j++;
        }
        i++;

      }

    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    if (i == 0) {
      this
          .add(new Label("test",
              "keine Daten; muss noch gegen eine vernueftige meldung getauscht werden"));
    }
  }

  protected final void onRender(final MarkupStream markupStream) {
    final int markupStart = markupStream.getCurrentIndex();
    Iterator<?> childs = this.iterator();
    while (childs.hasNext()) {
      markupStream.setCurrentIndex(markupStart);
      Component child = (Component) childs.next();
      child.render(getMarkupStream());
    }

  }

  public TableModel getITableModel() {
    return (TableModel) super.getModel();
  }

}
