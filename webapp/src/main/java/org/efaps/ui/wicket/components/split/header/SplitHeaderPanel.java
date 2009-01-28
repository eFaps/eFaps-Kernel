/*
 * Copyright 2003 - 2009 The eFaps Team
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

package org.efaps.ui.wicket.components.split.header;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.behavior.StringHeaderContributor;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.util.string.JavascriptUtils;

import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.db.Context;
import org.efaps.ui.wicket.behaviors.dojo.ContentPaneBehavior;
import org.efaps.ui.wicket.pages.contentcontainer.ContentContainerPage;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.resources.StaticHeaderContributor;
import org.efaps.util.EFapsException;

/**
 * Class renders the header inside a ListOnlyPanel or a StructBrowsSplitPanel.
 * The header contains all functionalities to expand and collapse the menus.
 *
 * @author jmox
 * @version $Id:SplitHeaderPanel.java 1510 2007-10-18 14:35:40Z jmox $
 */
public class SplitHeaderPanel extends Panel {

  /**
   * Reference to the style sheet.
   */
  public static final EFapsContentReference CSS =
      new EFapsContentReference(SplitHeaderPanel.class, "SplitHeaderPanel.css");

  /**
   * Enum holds the keys used to store the different Positions as UserAttrbiutes
   * in eFaps.
   */
  public enum PositionUserAttribute {
    /** position of the horizontal splitter.*/
    HORIZONTAL("positionOfHorizontalSplitter"),
    /** is the horizontal splitter collapsed.*/
    HORIZONTAL_COLLAPSED("horizontalSplitterIsCollapsed"),
    /** position of the vertical splitter.*/
    VERTICAL("positionOfVerticalSplitter"),
    /** is the vertical splitter collapsed.*/
    VERTICAL_COLLAPSED("verticalSplitterIsCollapsed");

    /**
     * Stores the key of the Region.
     */
    private final String key;

    /**
     * Private Constructor.
     * @param _key Key
     */
    private PositionUserAttribute(final String _key) {
      this.key = _key;
    }

    /**
     * Getter method for instance variable {@link #key}.
     *
     * @return value of instance variable {@link #key}
     */
    public String getKey() {
      return this.key;
    }
  }

  /**
   * Enum for the StyleSheets which are used in this Component.
   */
  public enum Css {
    /** style sheet for the open header.*/
    HEADER_OPEN("eFapsSplitHeader"),
    /** style sheet for the closed header.*/
    HEADER_CLOSED("eFapsSplitHeaderClosed"),
    /** style sheet for image.*/
    IMAGE_CONTRACT_VERTICAL("eFapsSplitImageContractVertical"),
    /** style sheet for image.*/
    IMAGE_EXPAND_VERTICAL("eFapsSplitImageExpandVertical"),
    /** style sheet image.*/
    IMAGE_EXPAND("eFapsSplitImageExpand"),
    /** style sheet image.*/
    IMAGE_CONTRACT("eFapsSplitImageContract"),
    /** style sheet for the open title.*/
    TITEL("eFapsSplitTitel"),
    /** style sheet for the hidden title.*/
    TITEL_HIDE("eFapsSplitTitelHide");

    /**
     * Stores the key of the Region.
     */
    private final String value;

    /**
     * Private Constructor.
     * @param _value Key
     */
    private Css(final String _value) {
      this.value = _value;
    }

    /**
     * Getter method for instance variable {@link #value}.
     *
     * @return value of instance variable {@link #value}
     */
    public String getValue() {
      return this.value;
    }
  }

  /**
   * Needed for serialization.
   */
  private static final long serialVersionUID = 1L;

  /**
   * This instance variable stores if the Split needs to have the link to hide
   * the panel vertically.
   */
  private final boolean showVertical;

  /**
   * this instance set contains the components which must be hidden in the case
   * of collapsing the panel. This is done through a javascript which sets the
   * style "display" to "none". This is necessary because Firefox is is showing
   * the scrollbars of a div even if it is behind another div (z-index)
   */
  private final Set<Component> hideComponents = new HashSet<Component>();

  /**
   * Is it horizontal hidden.
   */
  private final boolean hiddenH;

  /**
   * @param _wicketId             wicket id of this component
   * @param _showVertical         show the vertical collapse/expand button
   * @param _horizontalCollapsed  is horizontal collapsed
   * @param _verticalCollapsed    is vertical collapsed
   */
  public SplitHeaderPanel(final String _wicketId, final boolean _showVertical,
                          final boolean _horizontalCollapsed,
                          final boolean _verticalCollapsed) {
    super(_wicketId);
    this.showVertical = _showVertical;
    this.hiddenH = _horizontalCollapsed;
    setOutputMarkupId(true);
    this.add(StaticHeaderContributor.forCss(CSS));

    this.add(new AjaxStoreHorizontalPositionBehavior());
    final Label titel = new Label("titel",
                                  DBProperties.getProperty("Split.Titel"));
    this.add(titel);

    if (this.showVertical) {
      this.add(new AjaxStoreVerticalPositionBehavior());
      final AjaxLink<?> linkvertical = new AjaxLink<Object>(
          "expandContractV") {

        private static final long serialVersionUID = 1L;

        @Override
        public void onClick(final AjaxRequestTarget _target) {

          String position = null;
          String hidden = "false";
          try {
            position = Context.getThreadContext().getUserAttribute(
                PositionUserAttribute.VERTICAL.getKey());
            hidden = Context.getThreadContext().getUserAttribute(
                PositionUserAttribute.VERTICAL_COLLAPSED.getKey());
            //store the value
            Context.getThreadContext().setUserAttribute(
                PositionUserAttribute.VERTICAL_COLLAPSED.getKey(),
                ((Boolean) "false".equalsIgnoreCase(hidden)).toString());
          } catch (final EFapsException e) {
            // catch because only User attributes
            e.printStackTrace();
          }
          if (position == null) {
            position = "0";
          }
          final StringBuilder ret = new StringBuilder();
          ret.append("togglePaneVertical(").append(position)
            .append(",").append("false".equalsIgnoreCase(hidden)).append(");");
          _target.appendJavascript(ret.toString());
          _target.focusComponent(getParent());
        }
      };
      this.add(linkvertical);
      final WebMarkupContainer linkDiv
                                 = new WebMarkupContainer("expandContractVDiv");
      linkvertical.add(linkDiv);
      if (_verticalCollapsed) {
        linkDiv.add(new SimpleAttributeModifier("class",
                                              Css.IMAGE_EXPAND_VERTICAL.value));
      } else {
        linkDiv.add(new SimpleAttributeModifier("class",
                                            Css.IMAGE_CONTRACT_VERTICAL.value));
      }
    } else {
      this.add(new WebMarkupContainer("expandContractV")
                  .setVisible(false));
    }

    final AjaxLink<?> link = new AjaxLink<Object>("expandContractH") {

      private static final long serialVersionUID = 1L;

      @Override
      public void onClick(final AjaxRequestTarget _target) {

        String position = null;
        String hidden = "false";
        try {
          position = Context.getThreadContext().getUserAttribute(
              PositionUserAttribute.HORIZONTAL.getKey());
          hidden = Context.getThreadContext().getUserAttribute(
              PositionUserAttribute.HORIZONTAL_COLLAPSED.getKey());
          Context.getThreadContext().setUserAttribute(
              PositionUserAttribute.HORIZONTAL_COLLAPSED.getKey(),
              ((Boolean) "false".equalsIgnoreCase(hidden)).toString());
        } catch (final EFapsException e) {
          // catch because only User attributes
          e.printStackTrace();
        }
        if (position == null) {
          position = "200";
        }

        final StringBuilder ret = new StringBuilder();
        ret.append("togglePaneHorizontal(").append(position)
          .append(",").append("false".equalsIgnoreCase(hidden)).append(");");
        _target.appendJavascript(ret.toString());
        _target.focusComponent(getParent());
      }
    };

    this.add(link);
    final WebMarkupContainer linkDiv
                                 = new WebMarkupContainer("expandContractHDiv");
    link.add(linkDiv);

    if (_horizontalCollapsed) {
      linkDiv.add(new SimpleAttributeModifier("class", Css.IMAGE_EXPAND.value));
      this.add(new SimpleAttributeModifier("class", Css.HEADER_CLOSED.value));
      titel.add(new SimpleAttributeModifier("class", Css.TITEL_HIDE.value));
    } else {
      linkDiv.add(new SimpleAttributeModifier("class",
                                              Css.IMAGE_CONTRACT.value));
      this.add(new SimpleAttributeModifier("class", Css.HEADER_OPEN.value));
      titel.add(new SimpleAttributeModifier("class", Css.TITEL.value));
    }
  }

  /**
   * Add a component which will be hidden by setting the style "display" to
   * "none".
   *
   * @param _component  Component which must be hidden
   */
  public void addHideComponent(final Component _component) {
    this.hideComponents.add(_component);
  }

  /**
   * before rendering the JavaScript must be added to the header of the page.
   */
  @Override
  protected void onBeforeRender() {
    this.add(new StringHeaderContributor(getJavaScript()));
    super.onBeforeRender();

  }

  /**
   * Get the JavaScript which actually toggles the Split.
   * @param string
   *
   * @return String with the JavaScript
   */
  private String getJavaScript() {
    final StringBuilder ret = new StringBuilder();

    final String headerId = getMarkupId();

    final String borderId = getPage().get(((ContentContainerPage) getPage())
                                  .getSplitPath()).getMarkupId();

    final String paneId = getParent().getMarkupId();
    String innerPane = null;
    if (this.showVertical) {
      final Iterator<?> iter = getParent().iterator();
      boolean found = false;
      while (iter.hasNext()) {
        final Object child = iter.next();
        if (child instanceof WebMarkupContainer) {
          final List<?> behaviors = ((WebMarkupContainer) child).getBehaviors();
          for (final Object behavior : behaviors) {
            if (behavior instanceof ContentPaneBehavior) {
              innerPane = ((WebMarkupContainer) child).getMarkupId();
              found = true;
              break;
            }
          }
          if (found) {
            break;
          }
        }
      }
    }

    boolean addComma = false;
    final StringBuilder hideIds = new StringBuilder().append("new Array(");
    for (final Component component : SplitHeaderPanel.this.hideComponents) {
      if (addComma) {
        hideIds.append(",");
      }
      hideIds.append("\"").append(component.getMarkupId()).append("\"");
      addComma = true;
    }
    hideIds.append(")");

    ret.append(JavascriptUtils.SCRIPT_OPEN_TAG)
      .append("  var connections = [];\n")
      .append("  function togglePaneHorizontal(_width, _hide) {\n")
      .append("    var header = dojo.byId('").append(headerId).append("');\n")
      .append("    var hideIds = ").append(hideIds).append(";\n")
      .append("    var border = dijit.byId('").append(borderId).append("');\n")
      .append("    var pane = dijit.byId('").append(paneId).append("');\n")
      .append("    if(_hide) {\n")
      .append("      pane.resize({w: 20});\n")
      .append("      border.layout();\n")
      .append("      dojo.forEach(hideIds, function(id){\n")
      .append("          dojo.byId(id).style.display = \"none\"\n")
      .append("      });\n")
      .append("      header.className=\"").append(Css.HEADER_CLOSED.value)
         .append("\";\n")
      .append("      header.getElementsByTagName(\"div\")[")
        .append(this.showVertical ? "1" : "0")
      .append("].className=\"").append(Css.IMAGE_EXPAND.value).append("\";\n")
      .append("      header.getElementsByTagName(\"span\")[0].className=\"")
        .append(Css.TITEL_HIDE.value).append("\";\n")
      .append("      var splitter = border.getSplitter(\"left\");\n")
      .append("      connections[0] = dojo.connect(splitter,\"_startDrag\","
                + " this, \"toggleHeaderHorizontal\" );\n")
      .append("    } else {\n")
      .append("      toggleHeaderHorizontal();\n")
      .append("      pane.resize({w: _width});\n")
      .append("      border.layout();\n")
      .append("    }\n")
      .append("  }\n");

    if (this.showVertical) {
      ret.append("  function togglePaneVertical(_height, _hide){\n")
        .append("    var header = dojo.byId('").append(headerId).append("');\n")
        .append("    var border = dijit.byId('").append(paneId).append("');\n")
        .append("    var pane = dijit.byId('").append(innerPane).append("');\n")
        .append("    if(_hide) {\n")
        .append("      pane.resize({h: 20});\n")
        .append("      border.layout();\n")
        .append("      var splitter = border.getSplitter(\"top\");\n")
        .append("      connections[1] = dojo.connect(splitter,\"_startDrag\","
                  + "this, \"toggleHeaderVertical\" );\n")
        .append("      header.getElementsByTagName(\"div\")[0].className=\"")
          .append(Css.IMAGE_EXPAND_VERTICAL.value).append("\";\n")
        .append("    } else {\n")
        .append("      toggleHeaderVertical();\n")
        .append("      if (_height == 0) {\n")
        .append("        _height = border._borderBox.h / 2;")
        .append("      }\n")
        .append("      pane.resize({h: _height});\n")
        .append("      border.layout();\n")
        .append("    }\n")
        .append("  }\n");
    }

    ret.append("  function toggleHeaderHorizontal(){\n")
      .append("    var header = dojo.byId('").append(headerId).append("');\n")
      .append("    var hideIds = ").append(hideIds).append(";\n")
      .append("    dojo.forEach(hideIds, function(id){\n")
      .append("      dojo.byId(id).style.display = \"inline\"\n")
      .append("    });\n")
      .append("    header.className=\"").append(Css.HEADER_OPEN.value)
        .append("\";\n")
      .append("    header.getElementsByTagName(\"div\")[");

    if (this.showVertical) {
      ret.append("1");
    } else {
      ret.append("0");
    }

    ret.append("].className=\"").append(Css.IMAGE_CONTRACT.value)
        .append("\";\n")
      .append("    header.getElementsByTagName(\"span\")[0].className=\"")
        .append(Css.TITEL.value).append("\";\n")
      .append("    dojo.disconnect(connections[0]);\n")
      .append("  }\n");

    if (this.showVertical) {
      ret.append("  function toggleHeaderVertical(){\n")
        .append("    var header = dojo.byId('").append(headerId).append("');\n")
        .append("    header.getElementsByTagName(\"div\")[0].className=\"")
          .append(Css.IMAGE_CONTRACT_VERTICAL.value).append("\";\n")
        .append("    dojo.disconnect(connections[1]);\n")
        .append("  }\n");
    }

    // render a script that stores the positions
    ret.append("dojo.addOnLoad(initialiseSplitter);")
      .append("  function initialiseSplitter(){\n")
      .append("    var borderH = dijit.byId(\"").append(borderId)
          .append("\");\n")
      .append("      var splitterH = borderH.getSplitter(\"left\");\n")
      .append("      dojo.connect(splitterH,\"_stopDrag\","
              + "\"storePositionH\" );\n");

    if (this.showVertical) {
      ret.append("    var borderV = dijit.byId(\"").append(paneId)
          .append("\");\n")
        .append("      var splitterV = borderV.getSplitter(\"top\");\n")
        .append("      dojo.connect(splitterV,\"_stopDrag\","
          + "\"storePositionV\" );\n");
      if (this.hiddenH) {
        ret.append("    var hideIds = ").append(hideIds).append(";\n")
        .append("    dojo.forEach(hideIds, function(id){\n")
        .append("      dojo.byId(id).style.display = \"none\"\n")
        .append("    });\n");
      }
    }
    ret.append("  }\n").append(
        ((AjaxStoreHorizontalPositionBehavior) this.getBehaviors(
            AjaxStoreHorizontalPositionBehavior.class).get(0))
            .getJavaScript(paneId));

    if (this.showVertical) {
      ret.append(((AjaxStoreVerticalPositionBehavior) this.getBehaviors(
          AjaxStoreVerticalPositionBehavior.class).get(0))
          .getJavaScript(innerPane));
    }

    ret.append(JavascriptUtils.SCRIPT_CLOSE_TAG);
    return ret.toString();
  }
}
