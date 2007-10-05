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

import org.apache.wicket.IPageMap;
import org.apache.wicket.Page;
import org.apache.wicket.PageMap;
import org.apache.wicket.PageParameters;
import org.apache.wicket.behavior.StringHeaderContributor;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.IPageLink;
import org.apache.wicket.markup.html.link.InlineFrame;
import org.apache.wicket.markup.html.resources.StyleSheetReference;
import org.apache.wicket.protocol.http.ClientProperties;
import org.apache.wicket.protocol.http.request.WebClientInfo;
import org.apache.wicket.util.string.CssUtils;

import org.efaps.webapp.components.ChildCallBackHeaderContributer;
import org.efaps.webapp.components.dojo.ContentPaneBehavior;
import org.efaps.webapp.components.dojo.SplitContainerBehavior;
import org.efaps.webapp.components.split.ListOnlyPanel;
import org.efaps.webapp.components.split.StructBrowsSplitPanel;

/**
 * @author jmo
 * @version $Id$
 */
public class ContentContainerPage extends WebPage {

  private static final long serialVersionUID = 3169723830151134904L;

  public static final String IFRAME_PAGEMAP_NAME =
      "eFapsContentContainerIFrame";

  public static final String IFRAME_WICKETID = "splitrightactiframe";

  private String listMenuKey;

  private String inlinePath;

  private String splitPath;

  private final PageParameters parameters;

  private boolean structurbrowser;

  public ContentContainerPage(final PageParameters _parameters) {
    this.parameters = _parameters;
    initialise();
  }

  public ContentContainerPage(final PageParameters _parameters,
                              final IPageMap _pagemap) {
    super(_pagemap);
    this.parameters = _parameters;
    initialise();
  }

  public ContentContainerPage(final PageParameters _parameters,
                              final IPageMap _pagemap, final boolean _strucbrow) {
    super(_pagemap);
    this.parameters = _parameters;
    this.structurbrowser = _strucbrow;
    initialise();
  }

  private void initialise() {
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

    add(new StyleSheetReference("css", getClass(),
        "contentcontainerpage/ContentContainerPage.css"));

    WebMarkupContainer split = new WebMarkupContainer("split");
    this.add(split);
    split.add(new SplitContainerBehavior());
    if (this.structurbrowser) {
      split.add(new StructBrowsSplitPanel("left", this.listMenuKey,
          this.parameters));
    } else {
      split.add(new ListOnlyPanel("left", this.listMenuKey, this.parameters));
    }
    WebMarkupContainer right = new WebMarkupContainer("right");
    split.add(right);

    right.add(new ContentPaneBehavior(80, 20));

    WebMarkupContainer parent = new WebMarkupContainer("splitrightact");
    right.add(parent);
    parent.setOutputMarkupId(true);

    InlineFrame inline =
        new InlineFrame(IFRAME_WICKETID, PageMap.forName(IFRAME_PAGEMAP_NAME),
            new IPageLink() {

              private static final long serialVersionUID = 1L;

              public Page getPage() {
                WebFormPage page =
                    new WebFormPage(ContentContainerPage.this.parameters);
                page.setListMenuKey(ContentContainerPage.this.listMenuKey);
                return page;
              }

              public Class<WebFormPage> getPageIdentity() {
                return WebFormPage.class;
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

}
