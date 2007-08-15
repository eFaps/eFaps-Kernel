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
import org.apache.wicket.model.IModel;

import org.efaps.admin.datamodel.ui.FieldValue;
import org.efaps.util.EFapsException;
import org.efaps.webapp.models.ITableModel;
import org.efaps.webapp.models.ITableModel.IRowModel;

/**
 * @author jmo
 * @version $Id$
 * 
 */
public class WebTable extends WebMarkupContainer {
  private static final long serialVersionUID = 4336311424526370681L;

  private static String ROWID = "eFapsRow";

  private static String CELLID = "eFapsCell";

  private static String CELLVALUEID = "eFapsCellValue";

  public WebTable(String id, IModel model) {
    super(id, model);
    initialise();
  }

  private void initialise() {
    int i = 0;
    try {
      ITableModel model = (ITableModel) super.getModel();
      model.execute();

      boolean odd = true;

      for (Iterator<IRowModel> rowIter = model.getValues().iterator(); rowIter
          .hasNext(); odd = !odd) {
        IRowModel modelrow = rowIter.next();

        Row row = new Row(ROWID + "_" + i);
        if (odd) {
          row.add(new SimpleAttributeModifier("class", "eFapsTableRowOdd"));
        } else {
          row.add(new SimpleAttributeModifier("class", "eFapsTableRowEven"));
        }
        row.setOutputMarkupId(true);

        this.add(row);
        int j = 0;
        if (model.isShowCheckBoxes()) {
          Cell cell = new Cell(CELLID + "_" + i + "_" + j);
          row.add(cell);
          CheckBoxCell checkbox =
              new CheckBoxCell("box" + i, modelrow.getOids());
          cell.add(new SimpleAttributeModifier("width", "1%"));
          cell.add(checkbox);

        }

        j++;

        for (FieldValue value : modelrow.getValues()) {

          Cell cell = new Cell(CELLID + "_" + i + "_" + j);
          cell.setOutputMarkupId(true);
          row.add(cell);

          if (value.getFieldDef().getField().getReference() != null) {

            cell.add(new CellLink(CELLVALUEID + "_" + i + "_" + j, value
                .getInstance().getOid(), getValueString(value)));
          } else {

            cell.add(new CellValue(CELLVALUEID + "_" + i + "_" + j,
                getValueString(value)));
          }
          j++;
        }
        i++;

      }

    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private String getValueString(FieldValue _value) {
    String ret = null;
    if (_value.getValue() != null) {
      try {
        if (getITableModel().isCreateMode()
            && _value.getFieldDef().getField().isEditable()) {
          ret = _value.getCreateHtml();
        } else if (getITableModel().isEditMode()
            && _value.getFieldDef().getField().isEditable()) {
          ret = _value.getEditHtml();
        } else {
          ret = _value.getViewHtml();
        }

      } catch (EFapsException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    return ret;
  }

  protected final void onRender(final MarkupStream markupStream) {
    final int markupStart = markupStream.getCurrentIndex();
    Iterator<Component> childs = this.iterator();
    while (childs.hasNext()) {
      markupStream.setCurrentIndex(markupStart);
      Component child = childs.next();
      child.render(getMarkupStream());
    }

  }

  public ITableModel getITableModel() {
    return (ITableModel) super.getModel();
  }

}
