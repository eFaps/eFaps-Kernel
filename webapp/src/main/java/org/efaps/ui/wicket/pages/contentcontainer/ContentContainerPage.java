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
import org.efaps.ui.wicket.Opener;
import org.efaps.ui.wicket.behaviors.dojo.BorderBehavior;
import org.efaps.ui.wicket.behaviors.dojo.ContentPaneBehavior;
import org.efaps.ui.wicket.behaviors.dojo.BorderBehavior.Design;
import org.efaps.ui.wicket.behaviors.dojo.ContentPaneBehavior.Region;
import org.efaps.ui.wicket.components.ChildCallBackHeaderContributer;
import org.efaps.ui.wicket.components.split.ListOnlyPanel;
import org.efaps.ui.wicket.components.split.StructBrowsSplitPanel;
import org.efaps.ui.wicket.models.objects.AbstractUIObject;
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

  /**
   * This static variable is used as an acesskey to the PageMap for the IFrame.
   */
  public static final String IFRAME_PAGEMAP_NAME =
      "eFapsContentContainerIFrame";

  /**
   * this static variable is used as the wicketid for the IFrame.
   */
  public static final String IFRAME_WICKETID = "splitrightactiframe";

  /**
   * Needed for serialization.
   */
  private static final long serialVersionUID = 3169723830151134904L;


  /**
   * Static variable as Reference to the Stylesheet for the Page (normal).
   */
  private static final EFapsContentReference CSS =
      new EFapsContentReference(ContentContainerPage.class,
          "ContentContainerPage.css");

  /**
   * static variable as Reference to the Stylesheet for the Page (Internet
   * Explorer).
   */
  private static final EFapsContentReference CSS_IE =
      new EFapsContentReference(ContentContainerPage.class,
          "ContentContainerPage_IE.css");


  /**
   * Variable contains the key to the MenuTree.
   */
  private String menuTreeKey;

  /**
   * Variable contains the Path to the IFrame-Component so that ist can be
   * accessed by other classes.
   *
   * @see #getInlinePath()
   */
  private String inlinePath;

  /**
   * Variable contains the Path to the Split-Component so that ist can be
   * accessed by other classes.
   *
   * @see #getSplitPath()
   */
  private String splitPath;

  /**
   * Does this Page contain a StucturBrowser.
   */
  private boolean structurbrowser;

  /**
   * Is the content a WebForm or a Table?
   */
  private boolean webForm;

  /**
   * Constructor called from the client directly by using parameters. Normally
   * it should only contain one parameter Opener.OPENER_PARAKEY to access the
   * opener.
   *
   * @param _parameters PageParameters
   */
  public ContentContainerPage(final PageParameters _parameters) {
    super();
    final Opener opener = ((EFapsSession) getSession()).getOpener(_parameters
        .getString(Opener.OPENER_PARAKEY));
    final UUID commandUUID;
    final String instanceKey;
    if (opener.getModel() != null) {
      final AbstractUIObject uiObject = ((AbstractUIObject) opener.getModel()
          .getObject());
      commandUUID = uiObject.getCommandUUID();
      instanceKey = uiObject.getInstanceKey();
    } else {
      commandUUID = opener.getCommandUUID();
      instanceKey = opener.getInstanceKey();
    }

    initialise(commandUUID, instanceKey);
  }

  /**
   * @param _uuid   UUID of the command
   * @param _instanceKey    oid
   */
  public ContentContainerPage(final UUID _uuid, final String _instanceKey) {
    super();
    initialise(_uuid, _instanceKey);
  }

  /**
   * @param _pageMap page map
   * @param _uuid    UUID of the command
   * @param _instanceKey     oid
   */
  public ContentContainerPage(final IPageMap _pageMap,
                              final UUID _uuid, final String _instanceKey) {
    this(_pageMap, _uuid, _instanceKey, false);
  }

  /**
   * @param _pageMap              page map
   * @param _uuid                 UUID of the command
   * @param _instanceKey                  oid
   * @param _addStructurBrowser   add a structor browser
   */
  public ContentContainerPage(final IPageMap _pageMap, final UUID _uuid,
                              final String _instanceKey,
                              final boolean _addStructurBrowser) {
    super(_pageMap);
    this.structurbrowser = _addStructurBrowser;
    initialise(_uuid, _instanceKey);
  }

  /**
   * Method to initialize the Page.
   * @param _uuid   uuid of the command
   * @param _instanceId    oid
   */
  private void initialise(final UUID _uuid, final String _instanceKey) {
    ((EFapsSession) getSession()).getUpdateBehaviors().clear();

    final ClientProperties properties =
        ((WebClientInfo) getRequestCycle().getClientInfo()).getProperties();
    // we use different StyleSheets for different Bowsers
    if (properties.isBrowserInternetExplorer()) {
      add(StaticHeaderContributor.forCss(CSS_IE));
    } else {
      add(StaticHeaderContributor.forCss(CSS));
    }

    this.menuTreeKey = "MenuTree_" + getPageMapName();
    // add a Split
    final WebMarkupContainer split = new WebMarkupContainer("split");
    this.add(split);
    split.add(new BorderBehavior(Design.SIDEBAR));
    // add a StructurBowser?
    if (this.structurbrowser) {
      split.add(new StructBrowsSplitPanel("left", _uuid, _instanceKey,
                                          this.menuTreeKey));
    } else {
      split.add(new ListOnlyPanel("left",
                                  _uuid, _instanceKey, this.menuTreeKey));
    }
    final WebMarkupContainer right = new WebMarkupContainer("right");
    split.add(right);

    right.add(new ContentPaneBehavior(Region.CENTER, false));

    final WebMarkupContainer parent = new WebMarkupContainer("splitrightact");
    right.add(parent);
    parent.setOutputMarkupId(true);

    // select the defaultCommand

    final AbstractCommand cmd = getCommand(_uuid);
    UUID uuidTmp = _uuid;
    this.webForm = cmd.getTargetForm() != null;
    if (cmd instanceof Menu) {
      for (final AbstractCommand childcmd : ((Menu) cmd).getCommands()) {
        if (childcmd.isDefaultSelected()) {
          uuidTmp = childcmd.getUUID();
          this.webForm = childcmd.getTargetForm() != null;
          break;
        }
      }
    }
    final UUID uuid4NewPage = uuidTmp;
    // add the IFrame
    final InlineFrame inline = new InlineFrame(IFRAME_WICKETID, PageMap
        .forName(IFRAME_PAGEMAP_NAME), new IPageLink() {

      private static final long serialVersionUID = 1L;

      public Page getPage() {
        AbstractContentPage page;
        if (ContentContainerPage.this.webForm) {
          page = new FormPage(uuid4NewPage, _instanceKey);
        } else {
          page = new TablePage(uuid4NewPage, _instanceKey);
        }
        page.setMenuTreeKey(ContentContainerPage.this.menuTreeKey);
        return page;
      }

      public Class<AbstractContentPage> getPageIdentity() {
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
   * Method to get a Command.
   *
   * @param _uuid   Uuid of the Command
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
