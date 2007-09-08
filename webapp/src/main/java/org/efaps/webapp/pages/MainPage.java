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

import java.util.UUID;

import org.apache.wicket.PageMap;
import org.apache.wicket.behavior.StringHeaderContributor;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.InlineFrame;
import org.apache.wicket.markup.html.resources.StyleSheetReference;
import org.apache.wicket.protocol.http.request.WebClientInfo;
import org.apache.wicket.util.string.CssUtils;

import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.db.Context;
import org.efaps.util.EFapsException;
import org.efaps.webapp.components.menu.MenuComponent;
import org.efaps.webapp.models.MenuItemModel;

/**
 * @author jmo
 * @version $Id$
 */
public class MainPage extends WebPage {

  private static final long serialVersionUID = -4231606613730698766L;

  public static String INLINEFRAMENAME = "ILFP";

  public MainPage() throws Exception {
    // hack to show the IFrame correctly in safari
    if (((WebClientInfo) getRequestCycle().getClientInfo()).getProperties()
        .isBrowserSafari()) {
      add(new StringHeaderContributor(CssUtils.INLINE_OPEN_TAG
          + "#eFapsFrameContent {\n "
          + "  height: 100%; \n"
          + "}\n"
          + CssUtils.INLINE_CLOSE_TAG));
    }

    // MainToolBar
    MenuComponent menu =
        new MenuComponent("eFapsMainMenu", new MenuItemModel(UUID
            .fromString("87001cc3-c45c-44de-b8f1-776df507f268")));
    add(menu);

    add(new InlineFrame("eFapsContentFrame", PageMap.forName(INLINEFRAMENAME),
        EmptyPage.class));

    add(new InlineFrame("eFapsFrameHidden", getPageMap(), EmptyPage.class));
    add(new Label("eFapsWelcomeLabel", DBProperties
        .getProperty("LogoRowInclude.Welcome.Label")));

    try {
      add(new Label("eFapsWelcomePersonFirstNameLabel", Context
          .getThreadContext().getPerson().getFirstName()));
      add(new Label("eFapsWelcomePersonLastNameLabel", Context
          .getThreadContext().getPerson().getLastName()));
      add(new Label("eFapsLogoVersionLabel", DBProperties
          .getProperty("LogoRowInclude.Version.Label")));

      add(new StyleSheetReference("eFapsMainPageCSS", getClass(),
          "mainpage/MainPage.css"));

    } catch (EFapsException e) {

      e.printStackTrace();
    }

  }
}
