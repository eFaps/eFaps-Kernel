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

package org.efaps.ui.wicket.components.table.header;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.behavior.StringHeaderContributor;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.protocol.http.request.WebClientInfo;
import org.apache.wicket.util.string.CssUtils;
import org.apache.wicket.util.string.JavascriptUtils;

import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.components.modalwindow.UpdateParentCallback;
import org.efaps.ui.wicket.models.HeaderModel;
import org.efaps.ui.wicket.models.TableModel;

/**
 * @author jmo
 * @version $Id:TableHeaderPanel.java 1510 2007-10-18 14:35:40Z jmox $
 */
public class HeaderPanel extends Panel {

  private static final long serialVersionUID = 1L;

  private final ModalWindowContainer modal =
      new ModalWindowContainer("eFapsModal");

  public HeaderPanel(final String _id, final TableModel _model) {
    super(_id, _model);

    final int browserWidth =
        ((WebClientInfo) getRequestCycle().getClientInfo()).getProperties()
            .getBrowserWidth();

    final RepeatingView cellRepeater = new RepeatingView("cellRepeater");
    add(cellRepeater);

    if (_model.isShowCheckBoxes()) {
      final HeaderCellPanel cell =
          new HeaderCellPanel(cellRepeater.newChildId());
      cellRepeater.add(cell);
    }
    List<Long> widths = new ArrayList<Long>();
    int i = 0;
    for (HeaderModel headermodel : _model.getHeaders()) {

      final HeaderCellPanel cell =
          new HeaderCellPanel(cellRepeater.newChildId(), headermodel, _model);
      long width =
          browserWidth / _model.getWidthWeight() * headermodel.getWidth();
      widths.add(width);
      cell.add(new SimpleAttributeModifier("class",
          "eFapsTableHeaderCell eFapsCellWidth" + i));
      cellRepeater.add(cell);
      i++;
    }

    add(this.modal);
    this.modal.setPageMapName("modal");
    this.modal.setWindowClosedCallback(new UpdateParentCallback(this,
        this.modal, false));

    this.add(new StringHeaderContributor(getWidthStyle(widths)));
    this.add(new StringHeaderContributor(getScript()));
  }

  public final ModalWindowContainer getModal() {
    return this.modal;
  }

  private String getScript() {

    final String ret =
        JavascriptUtils.SCRIPT_OPEN_TAG
            + "  window.onresize = eFapsPositionTableHeader;\n"
            + "  window.onload = eFapsPositionTableHeader;\n"
            + "function eFapsPositionTableHeader() {\n"
            + "    var header = document.getElementById(\"eFapsTableHeader\");\n"
            + "    var cells= new Array();"
            + "    var celldivs= header.getElementsByTagName(\"div\");\n"
            + "    var widthWeight=0;"
            + "    var widthCor=0;"
            + "    for(i = 0;i<celldivs.length;i++){\n"
            + "      var cell = celldivs[i];"
            + "      var checkbox = cell.className.indexOf(\"eFapsTableCheckBoxCell\");\n"
            + "      if(checkbox > -1){\n"
            + "        widthCor += celldivs[i].clientWidth;\n"
            + "      }\n"
            + "      var f = cell.className.indexOf(\"eFapsCellWidth\");\n"
            + "      if (f>-1){\n"
            + "        cells.push(cell.clientWidth);\n"
            + "        widthWeight += cell.clientWidth;\n"
            + "      }\n"
            + "      if (f>-1 || checkbox > -1){\n"
            + "        widthCor += parseInt(window.getComputedStyle(cell,null).getPropertyValue(\"margin-left\"));\n"
            + "        widthCor += parseInt(window.getComputedStyle(cell,null).getPropertyValue(\"margin-right\"));\n"
            + "        widthCor += parseInt(window.getComputedStyle(cell,null).getPropertyValue(\"padding-left\"));\n"
            + "        widthCor += parseInt(window.getComputedStyle(cell,null).getPropertyValue(\"margin-right\"));\n"
            + "        widthCor += parseInt(window.getComputedStyle(cell,null).getPropertyValue(\"border-left-width\"));\n"
            + "        widthCor += parseInt(window.getComputedStyle(cell,null).getPropertyValue(\"border-right-width\"));\n"
            + "      }\n"
            + "    }\n"
            + "    var tablebody = document.getElementById(\"eFapsTableBody\");\n"
            + "    var w = (tablebody.clientWidth ) ;\n"
            + "    if (w != 0) {\n"
            + "      header.style.width = w + \"px\";\n"
            + "        for(j=0;j<cells.length;j++){\n"
            + "          var rule = getStyleRule(j);\n"
            + "          var cw = ((100/widthWeight * cells[j])/100)* (w-widthCor);\n"
            + "          rule.style.width= cw + \"px\";\n"
            + "        }\n"
            + "      }\n"
            + "}\n"
            + "  function getStyleRule(_styleIndex) {\n"
            + "  var selectorName = \"div.eFapsCellWidth\" + _styleIndex;\n"
            + "    for (i = 0; i < document.styleSheets.length; i++) { \n"
            + "      if(document.styleSheets[i].title==\"eFapsTableWidthStyles\"){\n"
            + "        for (j = 0; j < document.styleSheets[i].cssRules.length; j++) {\n"
            + "          if (document.styleSheets[i].cssRules[j].selectorText == selectorName) {\n"
            + "            return document.styleSheets[i].cssRules[j];\n"
            + "          }\n"
            + "        }\n"
            + "      }\n"
            + "    }\n"
            + "  }\n"
            + JavascriptUtils.SCRIPT_CLOSE_TAG;

    return ret;
  }

  private String getWidthStyle(List<Long> _widths) {

    final StringBuilder ret = new StringBuilder();
    ret
        .append("<style type=\"text/css\" title=\"eFapsTableWidthStyles\"><!--\n");

    for (int i = 0; i < _widths.size(); i++) {
      ret.append("div.eFapsCellWidth").append(i).append("{width: ").append(
          _widths.get(i)).append("px;}\n");
    }

    ret.append(CssUtils.INLINE_CLOSE_TAG);
    return ret.toString();
  }
}
