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
package org.efaps.webapp.pages;

import org.apache.wicket.PageMap;
import org.apache.wicket.PageParameters;
import org.apache.wicket.behavior.StringHeaderContributor;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.InlineFrame;
import org.apache.wicket.markup.html.resources.StyleSheetReference;
import org.apache.wicket.protocol.http.ClientProperties;
import org.apache.wicket.protocol.http.request.WebClientInfo;
import org.apache.wicket.util.string.CssUtils;
import org.apache.wicket.util.string.JavascriptUtils;
import org.wicketstuff.dojo.markup.html.container.DojoSimpleContainer;
import org.wicketstuff.dojo.markup.html.container.split.DojoSplitContainer;

import org.efaps.webapp.EFapsSession;
import org.efaps.webapp.components.listmenu.ListMenuPanel;

/**
 * @author jmo
 * @version $Id$
 */
public class ContentContainerPage extends WebPage {

  private static final long serialVersionUID = 3169723830151134904L;

  public static String LISTMENU = "MainListMenu";

  public ContentContainerPage(PageParameters _parameters) {
    super(PageMap.forName(MainPage.INLINEFRAMENAME));
    final ClientProperties properties =
        ((WebClientInfo) getRequestCycle().getClientInfo()).getProperties();

    if (properties.isBrowserSafari()) {
      add(new StringHeaderContributor(CssUtils.INLINE_OPEN_TAG
          + ".eFapsContentContainerFrame{\n"
          + "  height:100%; \n"
          + "}\n"
          + CssUtils.INLINE_CLOSE_TAG));
    }

    add(new StyleSheetReference("ContentContainerPageCSS", getClass(),
        "contentcontainerpage/ContentContainerPage.css"));

    DojoSplitContainer parentcontainer =
        new DojoSplitContainer("eFapsSplitContainer");
    parentcontainer.setHeight(properties.getBrowserHeight() + "px");

    add(parentcontainer);
    parentcontainer.setOrientation(DojoSplitContainer.ORIENTATION_HORIZONTAL);

    DojoSimpleContainer containerlinks =
        new DojoSimpleContainer("containerlinks", "Menu");
    parentcontainer.add(containerlinks);
    containerlinks
        .add(new ListMenuPanel("eFapsListMenu", LISTMENU, _parameters));

    DojoSimpleContainer containerrechts =
        new DojoSimpleContainer("containerrechts", "Content");
    parentcontainer.add(containerrechts);

    WebMarkupContainer parent = new WebMarkupContainer("aktParent");
    parent.setOutputMarkupId(true);
    containerrechts.add(parent);
    InlineFrame inline =
        new InlineFrame("eFapsContentContainerFrame", PageMap
            .forName("content"), WebFormPage.class, _parameters);

    parent.add(inline);
    this.add(new ChildCallBack());
  }

  @Override
  protected void onBeforeRender() {

    super.onBeforeRender();
    ((EFapsSession) this.getSession()).setContentContainer(this.getNumericId(),
        this.getCurrentVersionNumber());
  }

  public class ChildCallBack extends StringHeaderContributor {

    private static final long serialVersionUID = 1L;

    private final static String javaScript =
        JavascriptUtils.SCRIPT_OPEN_TAG
            + "function childCallBack(_call){\n"
            + "  _call = _call.replace(/^javascript:/, \"\");\n"
            + "  eval(_call);\n"
            + "}\n"
            + JavascriptUtils.SCRIPT_CLOSE_TAG;

    public ChildCallBack() {
      super(javaScript);
    }

  }
}
