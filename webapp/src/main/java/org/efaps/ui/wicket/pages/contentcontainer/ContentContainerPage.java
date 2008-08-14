/*
 * Copyright 2003-2008 The eFaps Team
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
package org.efaps.ui.wicket.pages.contentcontainer;

import java.util.UUID;

import org.apache.wicket.IPageMap;
import org.apache.wicket.Page;
import org.apache.wicket.PageMap;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.IPageLink;
import org.apache.wicket.markup.html.link.InlineFrame;
import org.apache.wicket.protocol.http.ClientProperties;
import org.apache.wicket.protocol.http.request.WebClientInfo;
import org.efaps.admin.ui.AbstractCommand;
import org.efaps.admin.ui.Command;
import org.efaps.admin.ui.Menu;
import org.efaps.admin.ui.Search;
import org.efaps.ui.wicket.EFapsSession;
import org.efaps.ui.wicket.behaviors.dojo.ContentPaneBehavior;
import org.efaps.ui.wicket.behaviors.dojo.SplitContainerBehavior;
import org.efaps.ui.wicket.components.ChildCallBackHeaderContributer;
import org.efaps.ui.wicket.components.split.ListOnlyPanel;
import org.efaps.ui.wicket.components.split.StructBrowsSplitPanel;
import org.efaps.ui.wicket.pages.AbstractMergePage;
import org.efaps.ui.wicket.pages.content.AbstractContentPage;
import org.efaps.ui.wicket.pages.content.form.FormPage;
import org.efaps.ui.wicket.pages.content.table.TablePage;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.resources.StaticHeaderContributor;

/**
 * This class renders a Page with is used as a Container for the Content.
 * <br/>This is necessary to be able to have a spilt in the page, abd be able to
 * reuse the same classes for the ContentPages. The Split contains on the left a
 * menu or tree and on the right an iframe for the content.
 *
 * @author jmox
 * @version $Id:ContentContainerPage.java 1510 2007-10-18 14:35:40Z jmox $
 */
public class ContentContainerPage extends AbstractMergePage {

  private static final long serialVersionUID = 3169723830151134904L;

  /**
   * this static variable is used as an acesskey to the PageMap for the IFrame
   */
  public static final String IFRAME_PAGEMAP_NAME =
      "eFapsContentContainerIFrame";

  /**
   * this static variable is used as the wicketid for the IFrame.
   */
  public static final String IFRAME_WICKETID = "splitrightactiframe";

  /**
   * static variable as Reference to the Stylesheet for the Page (normal)
   */
  private static EFapsContentReference CSS =
      new EFapsContentReference(ContentContainerPage.class,
          "ContentContainerPage.css");

  /**
   * static variable as Reference to the Stylesheet for the Page (Internet
   * Explorer)
   */
  private static EFapsContentReference CSS_IE =
      new EFapsContentReference(ContentContainerPage.class,
          "ContentContainerPage_IE.css");

  /**
   * static variable as Reference to the Stylesheet for the Page (Safari)
   */
  private static EFapsContentReference CSS_SAFARI =
      new EFapsContentReference(ContentContainerPage.class,
          "ContentContainerPage_Safari.css");

  /**
   * variable contains the key to the MenuTree
   */
  private String menuTreeKey;

  /**
   * variable contains the Path to the IFrame-Component so that ist can be
   * accessed by other classes
   *
   * @see #getInlinePath()
   */
  private String inlinePath;

  /**
   * variable contains the Path to the Split-Component so that ist can be
   * accessed by other classes
   *
   * @see #getSplitPath()
   */
  private String splitPath;

  /**
   * does this Page contain a StucturBrowser
   */
  private boolean structurbrowser;

  /**
   * Is the content a WebForm or a Table?
   */
  private boolean webForm;

  /**
   * Constructor setting the PageParameters
   *
   * @param _parameters
   */
  public ContentContainerPage(final PageParameters _parameters) {
    super(_parameters);
    initialise();
  }

  /**
   * Constructor setting the PageMap and the PageParameters
   *
   * @param _pagemap
   * @param _parameters
   */
  public ContentContainerPage(final IPageMap _pagemap,
                              final PageParameters _parameters) {
    this(_pagemap, _parameters, false);
  }

  /**
   * Constructor setting the PageMap and the PageParameters
   *
   * @param _pagemap
   * @param _parameters
   * @param _addStructurBrowser
   *                does the Page contain a StructurBrowser
   */
  public ContentContainerPage(final IPageMap _pagemap,
                              final PageParameters _parameters,
                              final boolean _addStructurBrowser) {
    super(_pagemap, _parameters);
    this.structurbrowser = _addStructurBrowser;
    initialise();
  }

  /**
   * method to initialise the Page
   */
  private void initialise() {
    ((EFapsSession) getSession()).getUpdateBehaviors().clear();

    final ClientProperties properties =
        ((WebClientInfo) getRequestCycle().getClientInfo()).getProperties();
    // we use different StyleSheets for different Bowsers
    if (properties.isBrowserSafari()) {
      add(StaticHeaderContributor.forCss(CSS_SAFARI));
    } else if (properties.isBrowserInternetExplorer()) {
      add(StaticHeaderContributor.forCss(CSS_IE));
    } else {
      add(StaticHeaderContributor.forCss(CSS));
    }

    this.menuTreeKey = "MenuTree_" + this.getPageMapName();
    // add a Split
    final WebMarkupContainer split = new WebMarkupContainer("split");
    this.add(split);
    split.add(new SplitContainerBehavior());
    // add a StructurBowser?
    if (this.structurbrowser) {
      split.add(new StructBrowsSplitPanel("left", this.menuTreeKey,
          getPageParameters()));
    } else {
      split
          .add(new ListOnlyPanel("left", this.menuTreeKey, getPageParameters()));
    }
    final WebMarkupContainer right = new WebMarkupContainer("right");
    split.add(right);

    right.add(new ContentPaneBehavior(80, 20));

    final WebMarkupContainer parent = new WebMarkupContainer("splitrightact");
    right.add(parent);
    parent.setOutputMarkupId(true);

    // select the defaultCommand
    final PageParameters parametersForPage =
        (PageParameters) getPageParameters().clone();
    String uuid;
    if (parametersForPage.get("command") instanceof String[]) {
      uuid = ((String[]) parametersForPage.get("command"))[0];
    } else {
      uuid = (String) parametersForPage.get("command");
    }

    final AbstractCommand cmd = getCommand(UUID.fromString(uuid));
    this.webForm = cmd.getTargetForm() != null;
    if (cmd instanceof Menu) {
      for (final AbstractCommand childcmd : ((Menu) cmd).getCommands()) {
        if (childcmd.isDefaultSelected()) {
          parametersForPage.put("command", childcmd.getUUID().toString());
          this.webForm = childcmd.getTargetForm() != null;
          break;
        }
      }
    }
    // add the IFrame
    final InlineFrame inline =
        new InlineFrame(IFRAME_WICKETID, PageMap.forName(IFRAME_PAGEMAP_NAME),
            new IPageLink() {

              private static final long serialVersionUID = 1L;

              /*
               * (non-Javadoc)
               *
               * @see org.apache.wicket.markup.html.link.IPageLink#getPage()
               */
              public Page getPage() {
                AbstractContentPage page;
                if (ContentContainerPage.this.webForm) {
                  page = new FormPage(parametersForPage);
                } else {
                  page = new TablePage(parametersForPage);
                }
                page.setMenuTreeKey(ContentContainerPage.this.menuTreeKey);
                return page;
              }

              /*
               * (non-Javadoc)
               *
               * @see org.apache.wicket.markup.html.link.IPageLink#getPageIdentity()
               */
              public Class getPageIdentity() {
                return AbstractContentPage.class;
              }
            });

    parent.add(inline);
    // set the Path to the IFrame
    this.inlinePath =
        inline.getPath().substring(inline.getPath().indexOf(":") + 1);
    // set the Path to the Split
    this.splitPath =
        split.getPath().substring(inline.getPath().indexOf(":") + 1);

    this.add(new ChildCallBackHeaderContributer());
  }

  /**
   * This is the getter method for the instance variable {@link #inlinePath}.
   *
   * @return value of instance variable {@link #inlinePath}
   */

  public String getInlinePath() {
    return this.inlinePath;
  }

  /**
   * This is the getter method for the instance variable {@link #menuTreeKey}.
   *
   * @return value of instance variable {@link #menuTreeKey}
   */

  public String getMenuTreeKey() {
    return this.menuTreeKey;
  }

  /**
   * This is the getter method for the instance variable {@link #splitPath}.
   *
   * @return value of instance variable {@link #splitPath}
   */

  public String getSplitPath() {
    return this.splitPath;
  }

  /**
   * method to get a Command
   *
   * @param _uuid
   *                Uuid of the Command we watn
   * @return a AbstractCommand
   */
  private AbstractCommand getCommand(final UUID _uuid) {
    AbstractCommand cmd = Command.get(_uuid);
    if (cmd == null) {
      cmd = Menu.get(_uuid);
      if (cmd == null) {
        cmd = Search.get(_uuid);
      }
    }
    return cmd;
  }
}
