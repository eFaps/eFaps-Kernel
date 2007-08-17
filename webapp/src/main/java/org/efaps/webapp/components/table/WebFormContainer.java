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
import org.efaps.admin.ui.Field;
import org.efaps.util.EFapsException;
import org.efaps.webapp.models.IFormModel;

public class WebFormContainer extends WebMarkupContainer {
  private static final long serialVersionUID = 1550111712776698728L;

  private static String ROWID = "eFapsRow";

  private static String CELLID = "eFapsCell";

  private static String CELLVALUEID = "eFapsCellValue";

  public WebFormContainer(String id, IModel model) {
    super(id, model);
    initialise();
  }

  private void initialise() {
    int i = 0;
    int j = 0;
    try {
      IFormModel model = (IFormModel) super.getModel();
      model.execute();
      int groupCount = 1;
      int rowGroupCount = 1;
      RowContainer row = null;
      for (FieldValue value : model.getValues()) {
        if (value != null && value.getFieldDef().getField() != null) {
          Field field = value.getFieldDef().getField();
          if (field.getGroupCount() > 0
              && (!model.isCreateMode() || field.isCreatable())
              && (!model.isSearchMode() || field.isSearchable())) {
            rowGroupCount = field.getGroupCount();
          }

          if (value.getAttribute() != null
              && (!model.isCreateMode() || field.isCreatable())
              && (!model.isSearchMode() || field.isSearchable())) {

            if (groupCount == 1) {
              row = new RowContainer(ROWID + "_" + i);
              this.add(row);
              i++;
            }
            CellContainer cell = new CellContainer(CELLID + "_" + i + "_" + j);
            row.add(cell);
            if (field.isRequired()
                && (model.isSearchMode() || model.isCreateMode())) {
              cell.add(new SimpleAttributeModifier("class",
                  "eFapsFormLabelRequired"));

            } else {
              cell.add(new SimpleAttributeModifier("class", "eFapsFormLabel"));
            }

            cell.add(new CellValueComponent(CELLVALUEID + "_" + i + "_" + j, value
                .getFieldDef().getLabel()));
            j++;
            cell = new CellContainer(CELLID + "_" + i + "_" + j);
            row.add(cell);
            cell
                .add(new SimpleAttributeModifier("class", "eFapsFormInputField"));
            Integer colspan =
                2 * (model.getMaxGroupCount() - rowGroupCount) + 1;

            cell
                .add(new SimpleAttributeModifier("colspan", colspan.toString()));

            cell.add(new CellValueComponent(CELLVALUEID + "_" + i + "_" + j,
                getValueString(value)));

            if (groupCount < rowGroupCount) {
              groupCount++;
            } else if (groupCount == rowGroupCount) {
              groupCount = 1;
              rowGroupCount = 1;
            }

          }

        }
        j++;
      }

    } catch (Exception e) {

      e.printStackTrace();
    }

  }

  private String getValueString(FieldValue _value) {
    IFormModel model = (IFormModel) super.getModel();
    String ret = null;
    if (_value.getValue() != null) {
      try {
        if (model.isCreateMode()
            && _value.getFieldDef().getField().isEditable()) {
          ret = _value.getCreateHtml();
        } else if (model.isEditMode()
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
    Iterator<?> childs = this.iterator();
    while (childs.hasNext()) {
      markupStream.setCurrentIndex(markupStart);
      Component child = (Component) childs.next();
      child.render(getMarkupStream());
    }

  }

  // private String getTable() throws Exception {
  // IFormModel model = (IFormModel) super.getModel();
  //
  // model.execute();
  //
  // int groupCount = 1;
  // int rowGroupCount = 1;
  //
  // StringBuilder ret = new StringBuilder();
  //
  // ret.append("<table id=\"eFapsFormTabel\">");
  //
  // for (FieldValue value : model.getValues()) {
  // // if the field is a group count field, store the value in the row group
  // // count variable, (only if not create node or field is creatable)
  //
  // if (value != null && value.getFieldDef().getField() != null) {
  // Field field = value.getFieldDef().getField();
  // if (field.getGroupCount() > 0
  // && (!model.isCreateMode() || field.isCreatable())
  // && (!model.isSearchMode() || field.isSearchable())) {
  // rowGroupCount = field.getGroupCount();
  // }
  //
  // // only if:
  // // - in createmode field is createable
  // // - in searchmode field is searchable
  // // - otherwise show field
  //
  // if (value.getAttribute() != null
  // && (!model.isCreateMode() || field.isCreatable())
  // && (!model.isSearchMode() || field.isSearchable())) {
  //
  // if (groupCount == 1) {
  // ret.append("<tr>");
  // }
  //
  // if (field.isRequired()
  // && (model.isSearchMode() || model.isCreateMode())) {
  // ret.append("<td class=\"eFapsFormLabelRequired\">");
  // } else {
  // ret.append("<td class=\"eFapsFormLabel\">");
  // }
  // ret.append(value.getFieldDef().getLabel()).append("</td>")
  //
  // .append("<td class=\"eFapsFormInputField\" colspan=\"").append(
  // 2 * (model.getMaxGroupCount() - rowGroupCount) + 1).append("\">");
  //
  // if (field.getReference() != null
  // && value.getInstance().getOid() != null && model.isCreateMode()
  // && model.isSearchMode() != true) {
  // String targetUrl =
  // field.getReference() + "oid=" + value.getInstance().getOid();
  // String targetWindow;
  // if (field.isTargetPopup()) {
  // targetWindow = "Popup";
  // } else {
  // targetWindow = "Content";
  // if (model.getNodeId() != null) {
  // targetUrl += "&nodeId=" + model.getNodeId();
  // }
  // }
  // ret.append(
  // "<a href=\"javascript:eFapsCommonOpenUrl('<c:out value=\"")
  // .append(targetUrl).append("','").append(targetWindow).append(
  // "'\")>");
  //
  // }
  // if (field.isShowTypeIcon() && value.getInstance().getOid() != null
  // && model.isCreateMode() == false && model.isSearchMode() == false) {
  // ret.append("<img src=").append(
  // value.getInstance().getType().getIcon()).append("/>&nbsp;");
  //
  // }
  //
  // if (model.isCreateMode() && field.isCreatable()) {
  // ret.append(value.getCreateHtml());
  // } else if (model.isEditMode() && field.isEditable()) {
  // ret.append(value.getEditHtml());
  // } else if (model.isSearchMode() && field.isSearchable()) {
  // ret.append(value.getSearchHtml());
  // } else if (value.getAttribute() != null) {
  // ret.append(value.getViewHtml());
  // }
  //
  // if (field.getReference() != null
  // && value.getInstance().getOid() != null && model.isCreateMode()
  // && model.isSearchMode() != true) {
  // ret.append("</a>");
  // }
  // ret.append("</td>");
  //
  // if (groupCount == rowGroupCount) {
  // ret.append("</tr>");
  // }
  // if (groupCount < rowGroupCount) {
  // groupCount++;
  // } else if (groupCount == rowGroupCount) {
  //            groupCount = 1;
  //            rowGroupCount = 1;
  //          }
  //
  //        }
  //      }
  //
  //    }
  //
  //    ret.append("</table>");
  //    return ret.toString();
  //  }
}
