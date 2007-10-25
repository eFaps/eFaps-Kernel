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

package org.efaps.ui.wicket.components.split;


import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.HeaderContributor;
import org.apache.wicket.behavior.StringHeaderContributor;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.util.string.JavascriptUtils;

import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.ui.wicket.pages.contentcontainer.ContentContainerPage;

/**
 * @author jmox
 * @version $Id:SplitHeaderPanel.java 1510 2007-10-18 14:35:40Z jmox $
 */
public class SplitHeaderPanel extends Panel {

  private static final long serialVersionUID = 1L;

  /**
   * enum for the StyleSheets which are used in this Component
   */
  public enum Css {
    HEADER_OPEN("eFapsSplitHeader"),
    HEADER_CLOSE_VERT("eFapsSplitHeaderVert"),
    IMAGE_EXPAND("eFapsSplitImageExpand"),
    IMAGE_CONTRACT("eFapsSplitImageContract"),
    TITEL("eFapsSplitTitel"),
    TITEL_HIDE("eFapsSplitTitelHide");

    public String value;

    private Css(String _value) {
      this.value = _value;
    }

  }

  public SplitHeaderPanel(String id) {
    super(id);
    this.setOutputMarkupId(true);
    this.add(HeaderContributor.forCss(SplitHeaderPanel.class,
        "SplitHeaderPanel.css"));
    this.add(new StringHeaderContributor(getJavaScript()));

    this
        .add(new Label("titel", DBProperties.getProperty("Standart.SplitTitel")));

   final AjaxLink link = new AjaxLink("expandcontract") {

      private static final long serialVersionUID = 1L;

      @Override
      public void onClick(final AjaxRequestTarget _target) {

        String panelId;
        if (findParent(ListOnlyPanel.class) == null) {
          panelId = findParent(StructBrowsSplitPanel.class).getMarkupId();
        } else {
          panelId = findParent(ListOnlyPanel.class).getMarkupId();
        }

        String ret =
            "togglePane(\""
                + getPage().get(
                    ((ContentContainerPage) getPage()).getSplitPath())
                    .getMarkupId()
                + "\",\""
                + panelId
                + "\",\""
                + this.getParent().getMarkupId()
                + "\")";

        _target.appendJavascript(ret);
        _target.focusComponent(this.getParent());
      }
    };
    this.add(link);

  }

  /**
   * get the JavaScript wich actually toggles the Split
   *
   * @return String with the JavaScript
   */
  private String getJavaScript() {
    final StringBuilder ret = new StringBuilder();

    ret
        .append(JavascriptUtils.SCRIPT_OPEN_TAG)
        .append("  var connections = [];\n")
        .append("  var header;\n")
        .append("  function togglePane(splitId, paneId, headerId) {\n")
        .append("    header = dojo.byId(headerId);\n")
        .append("    var split = dijit.byId(splitId);\n")
        .append("    var pane = dijit.byId(paneId);\n")
        .append("    if(pane.sizeShare > 0) {\n")
        .append("      split._saveState();\n")
        .append("      connections[0] = dojo.connect(split,\"beginSizing\",this," +
                               " \"toggleHeader\" );\n")
        .append("      header.className=\"")
        .append(Css.HEADER_CLOSE_VERT.value).append("\";\n")
        .append("      header.getElementsByTagName(\"div\")[0].className=\"")
        .append(Css.IMAGE_EXPAND.value).append("\";\n")
        .append("      header.getElementsByTagName(\"span\")[0].className=\"")
        .append(Css.TITEL_HIDE.value).append("\";\n")
        .append("      pane.sizeShare = 0;\n")
        .append("      split.layout();\n")
        .append("    } else {\n")
        .append("      toggleHeader();\n")
        .append("      split._restoreState();\n")
        .append("      split.layout();\n")
        .append("    }\n")
        .append("  }\n")
        .append("  function toggleHeader(){\n")
        .append("    header.className=\"")
        .append(Css.HEADER_OPEN.value).append("\";\n")
        .append("    header.getElementsByTagName(\"div\")[0].className=\"")
        .append(Css.IMAGE_CONTRACT.value).append("\";\n")
        .append("    header.getElementsByTagName(\"span\")[0].className=\"")
        .append(Css.TITEL.value).append("\";\n")
        .append("    dojo.disconnect(connections[0]);\n")
        .append("}\n")
        .append(JavascriptUtils.SCRIPT_CLOSE_TAG);

    return ret.toString();
  }

  /*
   * (non-Javadoc)
   *
   * @see org.apache.wicket.markup.html.panel.Panel#onComponentTag(org.apache.wicket.markup.ComponentTag)
   */
  @Override
  protected void onComponentTag(final ComponentTag _tag) {
    super.onComponentTag(_tag);
    _tag.put("class", "eFapsSplitHeader");
  }

}
