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

import org.apache.wicket.Component;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;

import org.efaps.admin.datamodel.ui.FieldValue;
import org.efaps.util.EFapsException;
import org.efaps.webapp.models.ITableModel;
import org.efaps.webapp.models.ITableModel.IRowModel;

public class Table extends WebMarkupContainer {
  private static final long serialVersionUID = 4336311424526370681L;

  private static String ROWID = "eFapsRow";

  private static String CELLID = "eFapsCell";

  private static String CELLVALUEID = "eFapsCellValue";

  public Table(String id, IModel model) {
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
          CheckBox checkbox = new CheckBox("box" + i, modelrow.getOids());
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

  public class CellLink extends ContentLink {
    private static final long serialVersionUID = 1L;

    private final String label;

    public CellLink(String id) {
      super(id);
      this.label = null;
    }

    public CellLink(String id, String _oid, String _label) {
      super(id, _oid);
      this.label = _label;

    }

    @Override
    protected void onComponentTag(final ComponentTag tag) {
      tag.setName("a");
      super.onComponentTag(tag);
    }

    @Override
    protected void onComponentTagBody(MarkupStream _markupStream,
        ComponentTag _openTag) {
      super.replaceComponentTagBody(_markupStream, _openTag, this.label);
    }

  }

  public class CellValue extends WebComponent {
    private static final long serialVersionUID = 1L;

    private final String label;

    public CellValue(String id) {
      super(id);
      this.label = null;
    }

    public CellValue(String id, String _label) {
      super(id);
      this.label = _label;

    }

    @Override
    protected void onComponentTagBody(MarkupStream _markupStream,
        ComponentTag _openTag) {
      super.replaceComponentTagBody(_markupStream, _openTag, this.label);
    }

  }

  public class CheckBox extends WebComponent {
    private static final long serialVersionUID = 1L;

    private final String oid;

    public CheckBox(String id, String _oid) {
      super(id);
      oid = _oid;
    }

    @Override
    protected void onComponentTag(ComponentTag tag) {
      tag.getAttributes().put("type", "checkbox");
      tag.getAttributes().put("name", "selectedRow");
      tag.getAttributes().put("value", oid);
      tag.setName("input");
    }

  }

  public class Cell extends ParentMarkupContainer {

    private static final long serialVersionUID = 1L;

    public Cell(String id) {
      super(id);
    }

    @Override
    protected void onComponentTag(ComponentTag tag) {
      super.onComponentTag(tag);
      tag.setName("td");
    }

  }

  public class ParentMarkupContainer extends WebMarkupContainer {

    private static final long serialVersionUID = 1L;

    public ParentMarkupContainer(String id) {
      super(id);
    }

    @Override
    protected void onRender(MarkupStream markupStream) {
      final int markupStart = markupStream.getCurrentIndex();

      // Get mutable copy of next tag
      final ComponentTag openTag = markupStream.getTag();
      final ComponentTag tag = openTag.mutable();

      // Call any tag handler
      onComponentTag(tag);

      // Render open tag
      if (getRenderBodyOnly() == false) {
        renderComponentTag(tag);
      }
      markupStream.next();

      // Render the body only if open-body-close. Do not render if
      // open-close.
      if (tag.isOpen()) {
        // Render the body
        onComponentTagBody(markupStream, tag);
      }
      markupStream.setCurrentIndex(markupStart);

      Iterator<Component> childs = this.iterator();
      while (childs.hasNext()) {
        markupStream.setCurrentIndex(markupStart);
        Component child = childs.next();
        child.render(getMarkupStream());

      }

      markupStream.setCurrentIndex(markupStart);
      markupStream.next();
      // Render close tag

      if (tag.isOpen()) {
        if (openTag.isOpen()) {
          // Get the close tag from the stream
          ComponentTag closeTag = markupStream.getTag();

          // If the open tag had its id changed
          if (tag.getNameChanged()) {
            // change the id of the close tag
            closeTag = closeTag.mutable();
            closeTag.setName(tag.getName());
          }

          // Render the close tag

          renderComponentTag(closeTag);

          markupStream.next();
        }

      }
    }

  }

  public class Row extends ParentMarkupContainer {
    private static final long serialVersionUID = 1L;

    public Row(String id) {
      super(id);

    }

    @Override
    protected void onComponentTag(ComponentTag tag) {
      super.onComponentTag(tag);
      tag.setName("tr");
      tag.setHasNoCloseTag(true);
    }

  }
}
// private String getTable() throws Exception {
// ITableModel model = (ITableModel) super.getModel();
//
// model.execute();
// StringBuilder ret = new StringBuilder();
//
// ret.append("<table id=\"eFapsTableBody\">");
//
// boolean odd = true;
//
// for (Iterator<IRowModel> rowIter = model.getValues().iterator(); rowIter
// .hasNext(); odd = !odd) {
//
// IRowModel row = rowIter.next();
//
// // show the tr tag depending on the odd attribute
// if (odd) {
// ret.append("<tr class=\"eFapsTableRowOdd\">");
// } else {
// ret.append("<tr class=\"eFapsTableRowEven\">");
// }
//
// if (model.isShowCheckBoxes()) {
// ret
// .append("<td width=\"1%\"><input type=\"checkbox\" name=\"selectedRow\"
// value=\"");
// ret.append(row.getOids());
// ret.append("\"/></td>");
// }
//
// for (FieldValue col : row.getValues()) {
//
// if (!col.getFieldDef().getField().isHidden()) {
// ret.append("<td>");
//
// // show href
// if (col.getFieldDef().getField().getReference() != null) {
// String href = col.getFieldDef().getField().getReference();
// String target = "Content";
// href += "&oid=" + col.getInstance().getOid();
// if (col.getFieldDef().getField().isTargetPopup()) {
// target = "Popup";
// } else if (model.getNodeId() != null) {
// href += "&nodeId=" + model.getNodeId();
// }
// ret.append("<a href=\"javascript:eFapsCommonOpenUrl('");
// ret.append(href);
// ret.append("', '");
// ret.append(target);
// ret.append("')\">");
// }
//
// // image
// if (col.getFieldDef().getField().getIcon() != null) {
// ret.append("<img src=\"");
// ret.append(col.getFieldDef().getField().getIcon());
// ret.append("\"/>");
// }
//
// // show type icon?
// if (col.getFieldDef().getField().isShowTypeIcon()) {
// String imgUrl = col.getInstance().getType().getIcon();
// if (imgUrl != null) {
// ret.append("<img src=\"");
// ret.append(imgUrl);
// ret.append("\"/>");
// ret.append("&nbsp;");
// };
// }
//
// // show column value
// if (col.getValue() != null) {
// if (model.isCreateMode()
// && col.getFieldDef().getField().isEditable()) {
// ret.append(col.getCreateHtml());
// } else if (model.isEditMode()
// && col.getFieldDef().getField().isEditable()) {
// ret.append(col.getEditHtml());
// } else {
// ret.append(col.getViewHtml());
// }
// }
//
// // end href
// if (col.getFieldDef().getField().getReference() != null) {
// ret.append("</a>");
// }
//
// ret.append("</td>");
// }
// }
//
// ret.append("</tr>");
// }
//
// ret.append("</table>");
// return ret.toString();
// }

