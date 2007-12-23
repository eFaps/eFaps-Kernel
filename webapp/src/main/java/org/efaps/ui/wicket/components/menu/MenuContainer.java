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

package org.efaps.ui.wicket.components.menu;

import java.util.Iterator;

import org.apache.wicket.PageMap;
import org.apache.wicket.behavior.HeaderContributor;
import org.apache.wicket.behavior.StringHeaderContributor;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.link.ILinkListener;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.markup.html.resources.JavascriptResourceReference;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.string.CssUtils;
import org.apache.wicket.util.string.JavascriptUtils;

import org.efaps.admin.event.EventType;
import org.efaps.admin.ui.AbstractCommand;
import org.efaps.admin.ui.AbstractCommand.Target;
import org.efaps.ui.wicket.components.AbstractParentMarkupContainer;
import org.efaps.ui.wicket.components.FormContainer;
import org.efaps.ui.wicket.models.MenuItemModel;
import org.efaps.ui.wicket.models.SearchItemModel;

/**
 * @author tmo
 * @author jmox
 * @version $Id:MenuContainer.java 1510 2007-10-18 14:35:40Z jmox $
 */
public class MenuContainer extends AbstractParentMarkupContainer {

  public static final JavascriptResourceReference JSCOOKMENU =
      new JavascriptResourceReference(MenuContainer.class, "JSCookMenu.js");

  public static final JavascriptResourceReference EFAPSEXTENSION =
    new JavascriptResourceReference(MenuContainer.class, "EFapsExtension.js");

  private static final long serialVersionUID = 1L;

  private static String URL =
      "resources/" + MenuContainer.class.getName() + "/";

  private static String HEADER_RESOURCE =
      JavascriptUtils.SCRIPT_OPEN_TAG
          + "var myThemeOfficeBase=\""
          + URL
          + "\";"
          + JavascriptUtils.SCRIPT_CLOSE_TAG
          + CssUtils.INLINE_OPEN_TAG
          + "span.eFapsMenuLabel  {\n "
          + "  vertical-align: middle;\n "
          + "}\n"
          + "img.eFapsMenuMainImage {\n"
          + "   padding-left: 2px;\n"
          + "   vertical-align: bottom;\n"
          + "   width: 16px;\n"
          + "   height: 16px;\n"
          + "}\n"
          + "img.eFapsMenuMainBlankImage  {\n"
          + "  vertical-align: bottom;\n"
          + "  width: 2px;\n"
          + "  height: 16px;\n "
          + "}\n"
          + "img.eFapsMenuSubImage {\n"
          + " vertical-align: bottom;\n"
          + "  width: 16px;\n"
          + "  height: 16px;"
          + "\n}\n"
          + CssUtils.INLINE_CLOSE_TAG;

  private static String IMG_BLANK_SUB =
      "<img src=\"" + URL + "blank.gif\" class=\"eFapsMenuSubImage\"/>";

  private static String IMG_BLANK_MAIN =
      "<img src=\"" + URL + "blank.gif\" class=\"eFapsMenuMainBlankImage\"/>";

  private final FormContainer form;

  private Integer childID = 0;

  public MenuContainer(final String _id, final IModel _model) {
    this(_id, _model, null);
  }

  public MenuContainer(final String _id, final IModel _model,
                       final FormContainer _form) {
    super(_id, _model);
    this.form = _form;
    add(HeaderContributor.forJavaScript(JSCOOKMENU));
    add(HeaderContributor.forJavaScript(EFAPSEXTENSION));
    add(HeaderContributor.forCss(getClass(), "theme.css"));
    add(new StringHeaderContributor(HEADER_RESOURCE));
    add(HeaderContributor.forJavaScript(getClass(), "theme.js"));
    initialise();
  }

  private void initialise() {
    final MenuItemModel model = (MenuItemModel) super.getModel();
    for (final MenuItemModel menuItem : model.getChilds()) {
      addLink(menuItem);
    }
  }

  private String getNewChildId() {
    return "ItemLink" + (this.childID++).toString();
  }

  private void addLink(final MenuItemModel _menuItemModel) {
    if (!_menuItemModel.hasChilds()) {
      if (_menuItemModel.getTarget() != Target.UNKNOWN) {
        if (_menuItemModel.getTarget() == Target.MODAL) {
          final AjaxOpenModalComponent item =
              new AjaxOpenModalComponent(getNewChildId(), _menuItemModel);
          this.add(item);
        } else {
          final StandardLink item =
              new StandardLink(getNewChildId(), _menuItemModel);
          this.add(item);
        }
      } else {
        if (_menuItemModel.getCommand().isSubmit()) {
          final AjaxSubmitComponent item =
              new AjaxSubmitComponent(getNewChildId(), _menuItemModel,
                  this.form);
          this.add(item);
        } else if (super.getModel() instanceof SearchItemModel) {

          final SearchLink item =
              new SearchLink(getNewChildId(), _menuItemModel);
          this.add(item);

        }
      }
    } else if (_menuItemModel.getCommand().hasEvents(
        EventType.UI_COMMAND_EXECUTE)) {

      final StandardLink item =
          new StandardLink(getNewChildId(), _menuItemModel);
      this.add(item);
    }
    if (_menuItemModel.getReference() != null) {
      _menuItemModel.setURL(_menuItemModel.getReference());
      if (_menuItemModel.getReference().equals("/eFaps/logout?")) {
        this.add(new LogOutLink(getNewChildId(), _menuItemModel));
      }
    }
    for (final MenuItemModel childs : _menuItemModel.getChilds()) {
      addLink(childs);
    }
  }

  @Override
  protected void onBeforeRender() {

    final Iterator<?> childs = this.iterator();
    while (childs.hasNext()) {
      final Object child = childs.next();
      if (child instanceof StandardLink) {
        final StandardLink item = (StandardLink) child;
        final MenuItemModel childModel = (MenuItemModel) item.getModel();

        String url = (String) item.urlFor(ILinkListener.INTERFACE);
        if (childModel.getTarget() == Target.POPUP) {
          final AbstractCommand command = childModel.getCommand();

          final PopupSettings popup =
              new PopupSettings(PageMap.forName("popup")).setHeight(
                  command.getWindowHeight()).setWidth(command.getWindowWidth());
          item.setPopupSettings(popup);
          popup.setTarget("\"" + url + "\"");
          final String tmp = popup.getPopupJavaScript().replaceAll("'", "\"");
          url = "javascript:" + tmp.replace("return false;", "");
        }

        childModel.setURL(url);

      } else if (child instanceof AjaxOpenModalComponent) {
        final AjaxOpenModalComponent item = (AjaxOpenModalComponent) child;
        final MenuItemModel childModel = (MenuItemModel) item.getModel();

        final String url = item.getJavaScript();

        childModel.setURL(url);

      } else if (child instanceof AjaxSubmitComponent) {
        final AjaxSubmitComponent item = (AjaxSubmitComponent) child;
        final MenuItemModel childModel = (MenuItemModel) item.getModel();

        final String url = item.getJavaScript();

        childModel.setURL(url);
      } else if (child instanceof SearchLink) {
        final SearchLink item = (SearchLink) child;
        final MenuItemModel childModel = (MenuItemModel) item.getModel();

        final String url = (String) item.urlFor(ILinkListener.INTERFACE);

        childModel.setURL(url);
      }

    }
    super.onBeforeRender();
  }

  @Override
  protected void onComponentTagBody(final MarkupStream _markupStream,
                                    final ComponentTag _openTag) {

    super.replaceComponentTagBody(_markupStream, _openTag,
        convertToHtml(_openTag));
  }

  public String convertToHtml(final ComponentTag _openTag) {
    final CharSequence id = _openTag.getString("id");

    final MenuItemModel model = (MenuItemModel) super.getModel();

    final StringBuilder html = new StringBuilder();

    html.append(JavascriptUtils.SCRIPT_OPEN_TAG).append("var ").append(id)
        .append("=[");

    for (final MenuItemModel menuItem : model.getChilds()) {
      convertToHtml(menuItem, html, true, new StringBuilder());
      html.append(",\n");
    }

    html.append("];").append("cmDraw ('").append(id).append("', ").append(id)
        .append(", 'hbr', cmThemeOffice);").append(
            JavascriptUtils.SCRIPT_CLOSE_TAG);

    return html.toString();
  }

  public void convertToHtml(final MenuItemModel _menuItem,
                            final StringBuilder _html, final boolean _isMain,
                            final StringBuilder _prefix) {

    _html.append("['");
    if (_menuItem.getImage() != null) {
      if (_isMain) {
        _html.append("<img src=\"/..").append(_menuItem.getImage()).append(
            "\" class=\"eFapsMenuMainImage\"/>");
      } else {
        _html.append("<img src=\"/..").append(_menuItem.getImage()).append(
            "\" class=\"eFapsMenuSubImage\"/>");
      }
    } else if (!_isMain) {
      _html.append(IMG_BLANK_SUB);
    } else {
      _html.append(IMG_BLANK_MAIN);
    }
    _html.append("','<span class=\"eFapsMenuLabel\">").append(
        _menuItem.getLabel()).append("</span>', '");
    if (_menuItem.getUrl() != null) {
      _html.append(_menuItem.getUrl());
    }
    if (_menuItem.getTarget() == Target.HIDDEN) {
      _html.append("', 'eFapsFrameHidden', '");
    } else if (_menuItem.getTarget() == Target.MODAL) {
      _html.append("', 'top', '");
    } else {
      _html.append("', '_self', '");
    }

    if (_menuItem.getDescription() != null) {
      _html.append(_menuItem.getDescription());
    }
    _html.append("'");
    for (final MenuItemModel menuItem : _menuItem.getChilds()) {
      _html.append("\n").append(_prefix).append("  ,");
      convertToHtml(menuItem, _html, false, new StringBuilder(_prefix)
          .append("  "));
    }
    _html.append("]");
  }
}
