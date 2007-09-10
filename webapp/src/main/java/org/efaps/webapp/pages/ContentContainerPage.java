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
import org.wicketstuff.dojo.markup.html.container.DojoSimpleContainer;
import org.wicketstuff.dojo.markup.html.container.split.DojoSplitContainer;
import org.wicketstuff.dojo.widgets.StyleAttribute;

import org.efaps.webapp.EFapsSession;
import org.efaps.webapp.components.ChildCallBackHeaderContributer;
import org.efaps.webapp.components.listmenu.ListMenuPanel;

/**
 * @author jmo
 * @version $Id$
 */
public class ContentContainerPage extends WebPage {

  private static final long serialVersionUID = 3169723830151134904L;

  public static String LISTMENU = "MainListMenu";

  public ContentContainerPage(PageParameters _parameters) {
    super(PageMap.forName(MainPage.IFRAME_PAGEMAP_NAME));
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
        new DojoSplitContainer("eFapsSplitContainer") {

          private static final long serialVersionUID = 1L;

          @Override
          protected void onStyleAttribute(StyleAttribute styleAttribute) {
            styleAttribute.put("position", "absolute");
            styleAttribute.put("width", "100%");
            styleAttribute.put("top", "0px");
            styleAttribute.put("bottom", "0px");
            styleAttribute.put("left", "0px");
            styleAttribute.put("right", "0px");

          }

        };

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
    this.add(new ChildCallBackHeaderContributer());
  }

  @Override
  protected void onBeforeRender() {

    super.onBeforeRender();
    ((EFapsSession) this.getSession()).setContentContainer(this.getNumericId(),
        this.getCurrentVersionNumber());
  }


}
