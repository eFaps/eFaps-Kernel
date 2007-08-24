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

package org.efaps.webapp.components.menu;

import java.util.Iterator;

import org.apache.wicket.behavior.HeaderContributor;
import org.apache.wicket.behavior.StringHeaderContributor;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.link.ILinkListener;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.string.CssUtils;
import org.apache.wicket.util.string.JavascriptUtils;

import org.efaps.webapp.components.AbstractParentMarkupContainer;
import org.efaps.webapp.components.MainMenuItemLinkComponent;
import org.efaps.webapp.models.MenuItemModel;

/**
 * @author tmo
 * @version $Id$
 */
public class MenuComponent extends AbstractParentMarkupContainer {

  private static final long serialVersionUID = 1L;

  private static String URL =
      "resources/" + MenuComponent.class.getName() + "/";

  private static String HEADER_RESOURCE = ""
          + JavascriptUtils.SCRIPT_OPEN_TAG 
               + "var myThemeOfficeBase=\"" + URL + "\";" 
          + JavascriptUtils.SCRIPT_CLOSE_TAG 
          + CssUtils.INLINE_OPEN_TAG
             + "span.eFapsMenuLabel  {\n" 
             + "  vertical-align: middle;\n" 
             + "}\n"
             + "img.eFapsMenuMainImage {\n" 
             + "  padding-left: 2px;\n"
             + "  vertical-align: bottom;\n" 
             + "  width: 16px;\n"
             + "  height: 16px;\n" 
             + "}\n" 
             + "img.eFapsMenuMainBlankImage  {\n"
             + "  vertical-align: bottom;\n" 
             + "  width: 2px;\n"
             + "  height: 16px;\n" + "}\n" 
             + "img.eFapsMenuSubImage {\n"
             + "  vertical-align: bottom;\n" 
             + "  width: 16px;\n"
             + "  height: 16px;\n" 
             + "}\n" 
         + CssUtils.INLINE_CLOSE_TAG;

  private static String IMG_BLANK_SUB =
      "<img src=\"" + URL + "blank.gif\" class=\"eFapsMenuSubImage\"/>";

  private static String IMG_BLANK_MAIN =
      "<img src=\"" + URL + "blank.gif\" class=\"eFapsMenuMainBlankImage\"/>";

  private final long position;

  public MenuComponent(final String _id, final IModel _model,
                       final long _position) {
    super(_id, _model);
    this.position = _position;

    add(HeaderContributor.forJavaScript(getClass(), "JSCookMenu.js"));
    add(HeaderContributor.forJavaScript(getClass(), "EFapsExtension.js"));
    add(HeaderContributor.forCss(getClass(), "theme.css"));
    add(new StringHeaderContributor(HEADER_RESOURCE));
    add(HeaderContributor.forJavaScript(getClass(), "theme.js"));
    initialise();
  }

  private void initialise() {
    MenuItemModel model = (MenuItemModel) super.getModel();

    for (MenuItemModel menuItem : model.childs) {
      addLink(menuItem);
    }
  }

  private Integer childID = 0;

  private String getNewChildId() {
    return "ItemLink" + (childID++).toString();
  }

  private void addLink(MenuItemModel menuItem) {
    MainMenuItemLinkComponent item =
        new MainMenuItemLinkComponent(getNewChildId(), menuItem);
    this.add(item);
    for (MenuItemModel childs : menuItem.childs) {
      addLink(childs);
    }
  }

  @Override
  protected void onBeforeRender() {
    // this.getRequestCycle().urlFor(test, AjaxEventBehavior.INTERFACE);
    Iterator<?> childs = this.iterator();
    while (childs.hasNext()) {
      MainMenuItemLinkComponent child =
          (MainMenuItemLinkComponent) childs.next();
      MenuItemModel childModel = (MenuItemModel) child.getModel();

// childModel.setURL((String) child.urlFor(((IBehavior) child.getBehaviors()
// .get(0)), AjaxEventBehavior.INTERFACE));
//      
      
      childModel.setURL((String)child.urlFor(ILinkListener.INTERFACE));
    }
    super.onBeforeRender();
  }

  protected void onComponentTagBody(final MarkupStream _markupStream,
      final ComponentTag _openTag) {
    try {
      super.replaceComponentTagBody(_markupStream, _openTag,
          convertToHtml(_openTag));
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public String convertToHtml(final ComponentTag _openTag) {
    CharSequence id = _openTag.getString("id");

    MenuItemModel model = (MenuItemModel) super.getModel();

    StringBuilder html = new StringBuilder();

    // appendCSS(_openTag, html);

    html.append(JavascriptUtils.SCRIPT_OPEN_TAG).append("var ").append(id)
        .append("=[");

    for (MenuItemModel menuItem : model.childs) {
      convertToHtml(menuItem, html, true, new StringBuilder());
      html.append(",\n");
    }

    // Todo: id darf nicht hard-coded sein!!!

    html.append("];").append("cmDraw ('").append(id).append("', ").append(id)
        .append(", 'hbr', cmThemeOffice);").append(
            JavascriptUtils.SCRIPT_CLOSE_TAG);

    return html.toString();
  }

  protected void appendCSS(final ComponentTag _openTag,
      final StringBuilder _html) {
    CharSequence id = _openTag.getString("id");

    _html
        .append(CssUtils.INLINE_OPEN_TAG)
        .append("#").append(id).append("  {\n" 
        	+ "  position: absolute;\n" 
        	+ "  top: ").append(this.position).append("px;\n" 
        	+ "  height: 22px;\n" 
        	+ "  left: 0;\n" 
       		+ "  right: 0;\n"
        	+ "  background-color: #EFEBDE;\n" 
        	+ "  border-style: solid;\n"
        	+ "  border-width: 1px 0;\n" 
        	+ "  border-color: black;\n"
        	+ "  margin: 0;\n" 
        	+ "  padding-top: 2px;\n"
        	+ "  padding-left: 2px;\n" 
        	+ "  padding-bottom: 0px;\n" 
        + "}\n")
        .append(CssUtils.INLINE_CLOSE_TAG);
  }

  public void convertToHtml(final MenuItemModel _menuItem,
      final StringBuilder _html, final boolean _isMain,
      final StringBuilder _prefix) {

    _html.append("['");
    if (_menuItem.image != null) {
      if (_isMain) {
        _html.append("<img src=\"/..")
             .append(_menuItem.image)
             .append("\" class=\"eFapsMenuMainImage\"/>");
      } else {
        _html.append("<img src=\"/..")
             .append(_menuItem.image)
             .append("\" class=\"eFapsMenuSubImage\"/>");
      }
    } else if (!_isMain) {
      _html.append(IMG_BLANK_SUB);
    } else {
      _html.append(IMG_BLANK_MAIN);
    }
    _html.append("','<span class=\"eFapsMenuLabel\">")
         .append(_menuItem.label)
        .append("</span>', '");
    if (_menuItem.url != null) {
      _html.append(_menuItem.url);
    }
    if ("true".equals(_menuItem.getCommand()
        .getProperty("NoUpdateAfterCOMMAND"))) {
      _html.append("', 'eFapsFrameHidden', '");
    } else {
      _html.append("', '_self', '");
    }

    if (_menuItem.description != null) {
      _html.append(_menuItem.description);
    }
    _html.append("'");
    for (MenuItemModel menuItem : _menuItem.childs) {
      _html.append("\n").append(_prefix).append("  ,");
      convertToHtml(menuItem, _html, false, new StringBuilder(_prefix)
          .append("  "));
    }
    _html.append("]");
  }
}
