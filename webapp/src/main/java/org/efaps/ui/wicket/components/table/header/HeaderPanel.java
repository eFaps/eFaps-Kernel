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

import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.HeaderContributor;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.behavior.StringHeaderContributor;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.protocol.http.request.WebClientInfo;
import org.apache.wicket.util.string.CssUtils;
import org.apache.wicket.util.string.JavascriptUtils;

import org.efaps.db.Context;
import org.efaps.ui.wicket.components.dojo.DojoReference;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.components.modalwindow.UpdateParentCallback;
import org.efaps.ui.wicket.models.HeaderModel;
import org.efaps.ui.wicket.models.TableModel;
import org.efaps.ui.wicket.models.TableModel.UserAttributeKey;
import org.efaps.util.EFapsException;

/**
 * @author jmox
 * @version $Id:TableHeaderPanel.java 1510 2007-10-18 14:35:40Z jmox $
 */
public class HeaderPanel extends Panel {

  private static final long serialVersionUID = 1L;

  private final ModalWindowContainer modal =
      new ModalWindowContainer("eFapsModal");

  AjaxStoreColumnWidth ajaxstore;

  public HeaderPanel(final String _id, final TableModel _model) {
    super(_id, _model);

    this.ajaxstore = new AjaxStoreColumnWidth();

    this.setMarkupId("eFapsTableHeader");
    this.add(this.ajaxstore);
    final int browserWidth =
        ((WebClientInfo) getRequestCycle().getClientInfo()).getProperties()
            .getBrowserWidth();

    final RepeatingView cellRepeater = new RepeatingView("cellRepeater");
    add(cellRepeater);
    int i = 0;
    if (_model.isShowCheckBoxes()) {
      final HeaderCellPanel cell =
          new HeaderCellPanel(cellRepeater.newChildId());
      cellRepeater.add(cell);
      i++;
    }
    List<String> widths = new ArrayList<String>();

    int fixed = 0;
    for (int j = 0; j < _model.getHeaders().size(); j++) {
      final HeaderModel headermodel = _model.getHeaders().get(j);

      final HeaderCellPanel cell =
          new HeaderCellPanel(cellRepeater.newChildId(), headermodel, _model);

      if (headermodel.isFixedWidth()) {
        widths.add("div.eFapsCellFixedWidth"
            + i
            + "{width: "
            + headermodel.getWidth()
            + "px;}\n");
        cell.add(new SimpleAttributeModifier("class",
            "eFapsTableHeaderCell eFapsCellFixedWidth" + i));
        fixed += headermodel.getWidth();
      } else {
        Integer width =
            browserWidth / _model.getWidthWeight() * headermodel.getWidth();
        widths.add("div.eFapsCellWidth"
            + i
            + "{width: "
            + width.toString()
            + "px;}\n");
        cell.add(new SimpleAttributeModifier("class",
            "eFapsTableHeaderCell eFapsCellWidth" + i));
      }
      cellRepeater.add(cell);

      if (j + 1 < _model.getHeaders().size() && !headermodel.isFixedWidth()) {
        boolean add = false;
        for (int k = j + 1; k < _model.getHeaders().size(); k++) {
          if (!_model.getHeaders().get(k).isFixedWidth()) {
            add = true;
            break;
          }
        }
        if (add) {
          cellRepeater.add(new Seperator(cellRepeater.newChildId(), i));
        }
      }
      i++;
    }

    add(this.modal);
    this.modal.setPageMapName("modal");
    this.modal.setWindowClosedCallback(new UpdateParentCallback(this,
        this.modal, false));

    this.add(new StringHeaderContributor(getWidthStyle(widths)));

    this.add(new HeaderContributor(HeaderContributor
        .forJavaScript(DojoReference.JS_DOJO)));
  }

  public final ModalWindowContainer getModal() {
    return this.modal;
  }

  private String getScript(final AjaxStoreColumnWidth _ajaxstore) {

    final String ret =
        JavascriptUtils.SCRIPT_OPEN_TAG
            + "  window.onresize = eFapsPositionTableHeader;\n"
            + "  window.onload = eFapsPositionTableHeader;\n"
            + "  function eFapsPositionTableHeader() {\n"
            + "    var header = document.getElementById(\"eFapsTableHeader\");\n"
            + "    var cells = new Array();\n"
            + "    var widthWeight = 0;\n"
            + "    var widthCor = 0;\n"
            + "    var addCell = 0;\n"
            + "    var celldivs = header.getElementsByTagName(\"div\");\n"
            + "    for(i = 0;i<celldivs.length;i++){\n"
            + "      var cell = celldivs[i];\n"
            + "      var fixed = cell.className.indexOf(\"eFapsCellFixedWidth\");\n"
            + "      if(fixed > -1){\n"
            + "        var addwith = getAdditionalWidth(cell);\n"
            + "        cells.push(new Array(cell.clientWidth, addwith,false));\n"
            + "        widthCor += cell.clientWidth + addwith;\n"
            + "      }\n"
            + "      var f = cell.className.indexOf(\"eFapsCellWidth\");\n"
            + "      if (f>-1){\n"
            + "        var addwith = getAdditionalWidth(cell);\n"
            + "        cells.push(new Array(cell.clientWidth, addwith,true));\n"
            + "        widthWeight += cell.clientWidth;\n"
            + "        widthCor+= addwith;\n"
            + "      }\n"
            + "    }\n"
            + "    var tablebody = document.getElementById(\"eFapsTableBody\");\n"
            + "    var completeWidth = (tablebody.clientWidth ) ;\n"
            + "    if (completeWidth != 0) {\n"
            + "      header.style.width = completeWidth + \"px\";\n"
            + "      var cellWidth;\n"
            + "      var rightshift = 0;\n"
            + "      for(k = 0;k < cells.length; k++){\n"
            + "        if(cells[k][2]==true){\n"
            + "          var rule = getStyleRule(k);\n"
            + "          cellWidth = ((100/widthWeight * cells[k][0])/100)* (completeWidth - widthCor - 5);\n"
            + "          rule.style.width= cellWidth + \"px\";\n"
            + "        }else {\n"
            + "          cellWidth = cells[k][0];\n"
            + "        }\n"
            + "        if(k+1 < cells.length){\n"
            + "          rightshift += cellWidth + cells[k][1];\n"
            + "          if(cells[k][2]==true ){\n"
            + "            if(document.getElementById(k+\"eFapsHeaderSeperator\")){\n"
            + "              document.getElementById(k+\"eFapsHeaderSeperator\").style.left = rightshift + \"px\";\n"
            + "            }\n"
            + "          }\n"
            + "        }\n"
            + "      }\n"
            + "    }\n"
            + "  }\n"
            + "  function getStyleRule(_styleIndex) {\n"
            + "    var selectorName = \"div.eFapsCellWidth\" + _styleIndex;\n"
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
            + "  function getAdditionalWidth(_cell){\n"
            + "    var compu = window.getComputedStyle(_cell,null); \n"
            + "    var width=0;\n"
            + "    width += parseInt(compu.getPropertyValue(\"margin-left\"));\n"
            + "    width += parseInt(compu.getPropertyValue(\"margin-right\"));\n"
            + "    width += parseInt(compu.getPropertyValue(\"padding-left\"));\n"
            + "    width += parseInt(compu.getPropertyValue(\"padding-right\"));\n"
            + "    width += parseInt(compu.getPropertyValue(\"border-left-width\"));\n"
            + "    width += parseInt(compu.getPropertyValue(\"border-right-width\"));\n"
            + "    return width;\n"
            + "  }\n"
            + "  var minWidth = 20;\n"
            + "  var seperatorWidth = 200;\n"
            + "  var lastpos= 0;\n"
            + "  var startpos= 0;\n"
            + "  var seperatorOffset= 0;\n"
            + "  var connections = [];\n"
            + "  var seperator;"
            + "  function beginColumnSize(_seperator,_event){\n"
            + "    sizing=true;\n"
            + "    lastpos=_event.screenX;\n"
            + "    startpos=lastpos;\n"
            + "    seperator=_seperator;\n"
            + "    seperatorOffset=_event.screenX- parseInt(_seperator.style.left);\n"
            + "    _seperator.style.width = parseInt(window.getComputedStyle(_seperator,null).getPropertyValue(\"width\")) + seperatorWidth +\"px\";\n"
            + "    _seperator.style.left = parseInt(_seperator.style.left) - seperatorWidth/2 +\"px\";\n"
            + "    _seperator.style.backgroundPosition=\"top center\";\n"
            + "    connections[0] = dojo.connect(_seperator,\"onmousemove\",this,  \"doColumnSize\" );\n"
            + "    connections[1] = dojo.connect(_seperator,\"onmouseout\",this,  \"cancelColumnSize\" );\n"
            + "  }\n"
            + "  function doColumnSize(_event){\n"
            + "    seperator.style.left= (_event.screenX-seperatorOffset) - seperatorWidth/2 +\"px\";\n"
            + "    lastpos=_event.screenX;\n"
            + "  }\n"
            + "  function endColumnSize(_seperator,_event){\n"
            + "    dojo.forEach(connections,dojo.disconnect);\n "
            + "    var dif = lastpos - startpos;\n"
            + "    var i = parseInt(seperator.id);\n"
            + "    var leftrule = getStyleRule(i);\n"
            + "    var rightrule = getStyleRule(i+1);\n"
            + "    var leftWidth = parseInt(leftrule.style.width);\n"
            + "    var rightWidth = parseInt(rightrule.style.width);\n"
            + "    var move = 0;\n"
            + "    if(leftWidth + dif > minWidth && rightWidth - dif > minWidth){\n"
            + "      leftrule.style.width = leftWidth + dif +\"px\";\n"
            + "      rightrule.style.width = rightWidth - dif +\"px\";\n"
            + "    } else {\n"
            + "      if(dif < 0){\n"
            + "        leftrule.style.width = minWidth +\"px\";\n"
            + "        rightrule.style.width = rightWidth + leftWidth - minWidth + \"px\";\n"
            + "        move = (leftWidth + dif-minWidth);\n"
            + "      } else {\n"
            + "        rightrule.style.width = minWidth +\"px\";\n"
            + "        leftrule.style.width = leftWidth + rightWidth - minWidth + \"px\";\n"
            + "        move = -(rightWidth - dif - minWidth);"
            + "      }\n"
            + "    }\n"
            + "    _seperator.style.width = parseInt(_seperator.style.width) - seperatorWidth +\"px\";\n"
            + "    _seperator.style.left = parseInt(_seperator.style.left) - move + seperatorWidth/2 +\"px\";\n"
            + "    _seperator.style.backgroundPosition=\"-200px 0\";\n"
            + "storeColumnWidths(getColumnWidths());\n"
            + "  }\n"
            + "  function cancelColumnSize(_event){\n"
            + "    endColumnSize(seperator,_event);\n"
            + "  }\n"
            + "  function getColumnWidths(){\n"
            + "    var header = document.getElementById(\"eFapsTableHeader\");\n"
            + "    var celldivs = header.getElementsByTagName(\"div\");\n"
            + "    var widths=\"\";\n"
            + "    for(i = 0;i<celldivs.length;i++){\n"
            + "      if(celldivs[i].className.indexOf(\"eFapsCellFixedWidth\")>-1 || celldivs[i].className.indexOf(\"eFapsCellWidth\")>-1){\n"
            + "        widths += window.getComputedStyle(celldivs[i],null).getPropertyValue(\"width\") +\";\";\n"
            + "      }\n"
            + "    }\n"
            + "    return widths;\n"
            + "  }\n"
            + _ajaxstore.getJavaScript()
            + JavascriptUtils.SCRIPT_CLOSE_TAG;

    return ret;
  }

  private String getWidthStyle(List<String> _widths) {

    final StringBuilder ret = new StringBuilder();
    ret
        .append("<style type=\"text/css\" title=\"eFapsTableWidthStyles\"><!--\n");

    for (String width : _widths) {
      ret.append(width);
    }

    ret.append(CssUtils.INLINE_CLOSE_TAG);
    return ret.toString();
  }

  /*
   * (non-Javadoc)
   *
   * @see org.apache.wicket.Component#onBeforeRender()
   */
  @Override
  protected void onBeforeRender() {
    this.add(new StringHeaderContributor(getScript(this.ajaxstore)));
    super.onBeforeRender();
  }

  public class Seperator extends WebComponent {

    private static final long serialVersionUID = 1L;

    private final int id;

    public Seperator(final String _wicketId, final int _outputid) {
      super(_wicketId);
      this.id = _outputid;
      this
          .add(new SimpleAttributeModifier("class", "eFapsTableHeaderSeperator"));
      this.add(new SimpleAttributeModifier("onmousedown",
          "beginColumnSize(this,event)"));

      this.add(new SimpleAttributeModifier("onmouseup",
          "endColumnSize(this,event)"));

    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.wicket.Component#onComponentTag(org.apache.wicket.markup.ComponentTag)
     */
    @Override
    protected void onComponentTag(final ComponentTag _tag) {
      super.onComponentTag(_tag);
      _tag.put("id", this.id + "eFapsHeaderSeperator");
      _tag.setName("span");
    }

  }

  public class AjaxStoreColumnWidth extends AbstractDefaultAjaxBehavior {

    private static final long serialVersionUID = 1L;

    public final static String COLUMNW_PARAMETERNAME = "eFapsColumnWidths";

    public String getJavaScript() {
      StringBuilder ret = new StringBuilder();
      ret.append("function storeColumnWidths(_widths){\n").append(
          generateCallbackScript("wicketAjaxPost('"
              + getCallbackUrl(false)
              + "','"
              + COLUMNW_PARAMETERNAME
              + "=' + _widths")).append("\n " + "}\n");
      return ret.toString();
    }

    @Override
    protected void respond(final AjaxRequestTarget arg0) {
      String widths =
          this.getComponent().getRequest().getParameter(COLUMNW_PARAMETERNAME);
      try {
        Context.getThreadContext().setUserAttribute(
            ((TableModel) this.getComponent().getModel())
                .getUserAttributeKey(UserAttributeKey.COLUMNWIDTH), widths);
      } catch (EFapsException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

    }

  }
}
