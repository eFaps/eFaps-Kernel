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

package org.efaps.webapp.components;

import java.util.Iterator;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.model.IModel;

import org.efaps.admin.datamodel.ui.FieldValue;
import org.efaps.webapp.models.ITableModel;
import org.efaps.webapp.models.ITableModel.Row;

public class Table extends WebComponent {
  private static final long serialVersionUID = 4336311424526370681L;

  public Table(String id, IModel model) {
    super(id, model);
  }

  protected void onComponentTagBody(MarkupStream _markupStream,
      ComponentTag _openTag) {
    try {
      super.replaceComponentTagBody(_markupStream, _openTag, getTable());
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private String getTable() throws Exception {
    ITableModel model = (ITableModel) super.getModel();

    model.execute();
    StringBuilder ret = new StringBuilder();

    ret.append("<table id=\"eFapsTableBody\">");

    boolean odd = true;

    for (Iterator<Row> rowIter = model.getValues().iterator(); rowIter
        .hasNext(); odd = !odd) {

      Row row = (Row) rowIter.next();

      // show the tr tag depending on the odd attribute
      if (odd) {
        ret.append("<tr class=\"eFapsTableRowOdd\">");
      } else {
        ret.append("<tr class=\"eFapsTableRowEven\">");
      }

      if (model.isShowCheckBoxes()) {
        ret
            .append("<td width=\"1%\"><input type=\"checkbox\" name=\"selectedRow\" value=\"");
        ret.append(row.getOids());
        ret.append("\"/></td>");
      }

      for (FieldValue col : row.getValues()) {

        if (!col.getFieldDef().getField().isHidden()) {
          ret.append("<td>");

          // show href
          if (col.getFieldDef().getField().getReference() != null) {
            String href = col.getFieldDef().getField().getReference();
            String target = "Content";
            href += "&oid=" + col.getInstance().getOid();
            if (col.getFieldDef().getField().isTargetPopup()) {
              target = "Popup";
            } else if (model.getNodeId() != null) {
              href += "&nodeId=" + model.getNodeId();
            }
            ret.append("<a href=\"javascript:eFapsCommonOpenUrl('");
            ret.append(href);
            ret.append("', '");
            ret.append(target);
            ret.append("')\">");
          }

          // image
          if (col.getFieldDef().getField().getIcon() != null) {
            ret.append("<img src=\"");
            ret.append(col.getFieldDef().getField().getIcon());
            ret.append("\"/>");
          }

          // show type icon?
          if (col.getFieldDef().getField().isShowTypeIcon()) {
            String imgUrl = col.getInstance().getType().getIcon();
            if (imgUrl != null) {
              ret.append("<img src=\"");
              ret.append(imgUrl);
              ret.append("\"/>");
              ret.append("&nbsp;");
            };
          }

          // show column value
          if (col.getValue() != null) {
            if (model.isCreateMode()
                && col.getFieldDef().getField().isEditable()) {
              ret.append(col.getCreateHtml());
            } else if (model.isEditMode()
                && col.getFieldDef().getField().isEditable()) {
              ret.append(col.getEditHtml());
            } else {
              ret.append(col.getViewHtml());
            }
          }

          // end href
          if (col.getFieldDef().getField().getReference() != null) {
            ret.append("</a>");
          }

          ret.append("</td>");
        }
      }

      ret.append("</tr>");
    }

    ret.append("</table>");
    return ret.toString();
  }
}
