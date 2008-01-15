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
package org.efaps.ui.wicket.pages.contentcontainer;

import java.util.UUID;

import org.apache.wicket.IPageMap;
import org.apache.wicket.Page;
import org.apache.wicket.PageMap;
import org.apache.wicket.PageParameters;
import org.apache.wicket.behavior.StringHeaderContributor;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.IPageLink;
import org.apache.wicket.markup.html.link.InlineFrame;
import org.apache.wicket.protocol.http.ClientProperties;
import org.apache.wicket.protocol.http.request.WebClientInfo;
import org.apache.wicket.util.string.CssUtils;

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
import org.efaps.ui.wicket.pages.AbstractEFapsPage;
import org.efaps.ui.wicket.pages.content.AbstractContentPage;
import org.efaps.ui.wicket.pages.content.form.FormPage;
import org.efaps.ui.wicket.pages.content.table.TablePage;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.resources.StaticHeaderContributor;

/**
 * @author jmox
 * @version $Id:ContentContainerPage.java 1510 2007-10-18 14:35:40Z jmox $
 */
public class ContentContainerPage extends AbstractEFapsPage {

  private static final long serialVersionUID = 3169723830151134904L;

  public static final String IFRAME_PAGEMAP_NAME =
      "eFapsContentContainerIFrame";

  public static final String IFRAME_WICKETID = "splitrightactiframe";

  private static EFapsContentReference CSS =
      new EFapsContentReference(ContentContainerPage.class,
          "ContentContainerPage.css");

  private String listMenuKey;

  private String inlinePath;

  private String splitPath;

  private final PageParameters parameters;

  private boolean structurbrowser;

  private boolean webForm;

  public ContentContainerPage(final PageParameters _parameters) {
    this.parameters = _parameters;
    initialise();
  }

  public ContentContainerPage(final PageParameters _parameters,
                              final IPageMap _pagemap) {
    this(_parameters, _pagemap, false);
  }

  public ContentContainerPage(final PageParameters _parameters,
                              final IPageMap _pagemap, final boolean _strucbrow) {
    super(_pagemap);
    this.parameters = _parameters;
    this.structurbrowser = _strucbrow;
    initialise();
  }

  private void initialise() {
    ((EFapsSession) getSession()).getUpdateBehaviors().clear();

    final ClientProperties properties =
        ((WebClientInfo) getRequestCycle().getClientInfo()).getProperties();

    if (properties.isBrowserSafari()) {
      add(new StringHeaderContributor(CssUtils.INLINE_OPEN_TAG
          + ".eFapsContentContainerFrame{\n"
          + "  height:100%; \n"
          + "}\n"
          + CssUtils.INLINE_CLOSE_TAG));
    }

    this.listMenuKey = "ListMenu_" + this.getPageMapName();

    add(StaticHeaderContributor.forCss(CSS));

    final WebMarkupContainer split = new WebMarkupContainer("split");
    this.add(split);
    split.add(new SplitContainerBehavior());
    if (this.structurbrowser) {
      split.add(new StructBrowsSplitPanel("left", this.listMenuKey,
          this.parameters));
    } else {
      split.add(new ListOnlyPanel("left", this.listMenuKey, this.parameters));
    }
    final WebMarkupContainer right = new WebMarkupContainer("right");
    split.add(right);

    right.add(new ContentPaneBehavior(80, 20));

    final WebMarkupContainer parent = new WebMarkupContainer("splitrightact");
    right.add(parent);
    parent.setOutputMarkupId(true);

    final PageParameters parametersForPage =
        (PageParameters) this.parameters.clone();
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

    final InlineFrame inline =
        new InlineFrame(IFRAME_WICKETID, PageMap.forName(IFRAME_PAGEMAP_NAME),
            new IPageLink() {

              private static final long serialVersionUID = 1L;

              public Page getPage() {
                AbstractContentPage page;
                if (ContentContainerPage.this.webForm) {
                  page = new FormPage(parametersForPage);
                } else {
                  page = new TablePage(parametersForPage);
                }
                page.setListMenuKey(ContentContainerPage.this.listMenuKey);
                return page;
              }

              public Class<AbstractContentPage> getPageIdentity() {
                return AbstractContentPage.class;
              }
            });

    parent.add(inline);

    this.inlinePath =
        inline.getPath().substring(inline.getPath().indexOf(":") + 1);
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
   * This is the getter method for the instance variable {@link #listMenuKey}.
   *
   * @return value of instance variable {@link #listMenuKey}
   */

  public String getListMenuKey() {
    return this.listMenuKey;
  }

  /**
   * This is the getter method for the instance variable {@link #splitPath}.
   *
   * @return value of instance variable {@link #splitPath}
   */

  public String getSplitPath() {
    return this.splitPath;
  }

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
