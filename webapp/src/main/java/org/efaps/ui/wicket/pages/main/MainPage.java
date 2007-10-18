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
 * Revision:        $Rev:1490 $
 * Last Changed:    $Date:2007-10-15 18:04:02 -0500 (Mon, 15 Oct 2007) $
 * Last Changed By: $Author:jmox $
 */

package org.efaps.ui.wicket.pages.main;

import java.util.UUID;

import org.apache.wicket.PageMap;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.behavior.StringHeaderContributor;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.InlineFrame;
import org.apache.wicket.markup.html.resources.StyleSheetReference;
import org.apache.wicket.protocol.http.request.WebClientInfo;
import org.apache.wicket.util.string.JavascriptUtils;

import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.db.Context;
import org.efaps.util.EFapsException;
import org.efaps.ui.wicket.components.ChildCallBackHeaderContributer;
import org.efaps.ui.wicket.components.menu.MenuContainer;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.models.MenuItemModel;
import org.efaps.ui.wicket.pages.empty.EmptyPage;
import org.efaps.ui.wicket.pages.error.ErrorPage;

/**
 * @author jmo
 * @version $Id:MainPage.java 1490 2007-10-15 23:04:02Z jmox $
 */
public class MainPage extends WebPage {

  private static final long serialVersionUID = -4231606613730698766L;

  public static String IFRAME_PAGEMAP_NAME = "MainPageIFramePageMap";

  public static String IFRAME_WICKETID = "content";

  private final ModalWindowContainer modal = new ModalWindowContainer("modal");

  public MainPage() {
    // TODO remove the 67 from this trick
    // hack to show the IFrame correctly in safari
    if (((WebClientInfo) getRequestCycle().getClientInfo()).getProperties()
        .isBrowserSafari()) {
      this.add(new StringHeaderContributor(JavascriptUtils.SCRIPT_OPEN_TAG
          + "  window.onresize = eFapsSetIFrameHeight; \n"
          + "  window.onload = eFapsSetIFrameHeight; \n"
          + "  function eFapsSetIFrameHeight() {\n"
          + "    var x = window.innerHeight - 67; \n"
          + "    document.getElementById('eFapsFrameContent').height=x;\n"
          + "  }"
          + JavascriptUtils.SCRIPT_CLOSE_TAG));
    }
    this.add(new StringHeaderContributor("<title>"
        + DBProperties.getProperty("LogoRowInclude.Version.Label")
        + "</title>"));

    add(this.modal);
    this.modal.setPageMapName("modal");

    this
        .add(new StyleSheetReference("css", getClass(), "MainPage.css"));

    this.add(new ChildCallBackHeaderContributer());

    this.add(new Label("welcome", DBProperties
        .getProperty("LogoRowInclude.Welcome.Label")));

    try {
      this.add(new Label("firstname", Context.getThreadContext().getPerson()
          .getFirstName()));
      this.add(new Label("lastname", Context.getThreadContext().getPerson()
          .getLastName()));
    } catch (EFapsException e) {
      throw new RestartResponseException(new ErrorPage(e));
    }

    this.add(new Label("version", DBProperties
        .getProperty("LogoRowInclude.Version.Label")));

    // MainToolBar
    MenuContainer menu =
        new MenuContainer("menu", new MenuItemModel(UUID
            .fromString("87001cc3-c45c-44de-b8f1-776df507f268")));
    this.add(menu);

    this.add(new InlineFrame(IFRAME_WICKETID, PageMap
        .forName(IFRAME_PAGEMAP_NAME), EmptyPage.class));

    this.add(new InlineFrame("hidden", getPageMap(), EmptyPage.class));

  }

  public final ModalWindowContainer getModal() {
    return this.modal;
  }
}
