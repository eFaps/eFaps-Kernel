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
 * Revision:        $Rev:1490 $
 * Last Changed:    $Date:2007-10-15 18:04:02 -0500 (Mon, 15 Oct 2007) $
 * Last Changed By: $Author:jmox $
 */

package org.efaps.ui.wicket.pages.main;

import java.util.UUID;

import org.apache.wicket.PageMap;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.behavior.StringHeaderContributor;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.InlineFrame;
import org.apache.wicket.util.string.JavascriptUtils;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.db.Context;
import org.efaps.ui.wicket.components.ChildCallBackHeaderContributer;
import org.efaps.ui.wicket.components.menu.MenuContainer;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.models.MenuItemModel;
import org.efaps.ui.wicket.models.objects.UIMenuItem;
import org.efaps.ui.wicket.pages.AbstractMergePage;
import org.efaps.ui.wicket.pages.empty.EmptyPage;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.resources.StaticHeaderContributor;
import org.efaps.util.EFapsException;

/**
 * This Page is the MainPage for eFaps and also the Homepage as set in
 * {@link #org.efaps.ui.wicket.EFapsApplication.getHomePage()}.<br>
 * It contains the MainMenu and two iFrames. One for the Content and one hidden
 * to provide the possibilty to set a response into the hidden FRame.
 *
 * @author jmox
 * @version $Id$
 */
public class MainPage extends AbstractMergePage {

  private static final long serialVersionUID = -4231606613730698766L;

  /**
   * this static variable contians the Key for the PageMap for the IFrame
   */
  public final static String IFRAME_PAGEMAP_NAME = "MainPageIFramePageMap";

  /**
   * this static variable contians the id for the htmlFrame
   */
  public final static String IFRAME_WICKETID = "content";

  /**
   * Reference to the StyleSheet for this Page
   */
  private static final EFapsContentReference CSS =
      new EFapsContentReference(MainPage.class, "MainPage.css");

  /**
   * Reference to a JavaScript used for this Page
   */
  private static final EFapsContentReference FRAMEJS =
      new EFapsContentReference(MainPage.class, "SetFrameHeight.js");

  /**
   * the MainPage has a ModalWindow that can be called from the childPages
   */
  private final ModalWindowContainer modal = new ModalWindowContainer("modal");

  /**
   * Constructor adding all Compoments to this Page
   */
  public MainPage() {
    super();

    // we need to add a JavaScript Function to resize the iFrame
      // don't merge it to keep the sequence
    this.add(StaticHeaderContributor.forJavaScript(FRAMEJS, true));
    this.add(new StringHeaderContributor(JavascriptUtils.SCRIPT_OPEN_TAG
          + "  window.onresize = eFapsSetIFrameHeight; \n"
          + "  window.onload = eFapsSetIFrameHeight; \n"
          + JavascriptUtils.SCRIPT_CLOSE_TAG));

    // set the title for the Page
    this.add(new StringHeaderContributor("<title>"
        + DBProperties.getProperty("Logo.Version.Label")
        + "</title>"));

    add(this.modal);
    this.modal.setPageMapName("modal");

    this.add(StaticHeaderContributor.forCss(CSS));

    this.add(new ChildCallBackHeaderContributer());

    this.add(new Label("welcome", DBProperties
        .getProperty("Logo.Welcome.Label")));

    try {
      this.add(new Label("firstname", Context.getThreadContext().getPerson()
          .getFirstName()));
      this.add(new Label("lastname", Context.getThreadContext().getPerson()
          .getLastName()));
    } catch (final EFapsException e) {
      throw new RestartResponseException(new ErrorPage(e));
    }

    this.add(new Label("version", DBProperties
        .getProperty("Logo.Version.Label")));

    // add the MainToolBar to the Page
    final MenuContainer menu =
        new MenuContainer("menu",new MenuItemModel( new UIMenuItem(UUID
            .fromString("87001cc3-c45c-44de-b8f1-776df507f268"))));
    this.add(menu);

    this.add(new InlineFrame(IFRAME_WICKETID, PageMap
        .forName(IFRAME_PAGEMAP_NAME), EmptyPage.class));

    this.add(new InlineFrame("hidden", getPageMap(), EmptyPage.class));

  }

  /**
   * method to get the ModalWindow of this Page
   *
   * @return
   */
  public final ModalWindowContainer getModal() {
    return this.modal;
  }

}
