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

package org.efaps.ui.wicket.components.split;

import org.apache.wicket.PageParameters;
import org.apache.wicket.behavior.HeaderContributor;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;

import org.efaps.ui.wicket.components.dojo.ContentPaneBehavior;
import org.efaps.ui.wicket.components.dojo.SplitContainerBehavior;
import org.efaps.ui.wicket.components.dojo.SplitContainerBehavior.Orientation;
import org.efaps.ui.wicket.components.menutree.MenuTree;
import org.efaps.ui.wicket.components.tree.StructurBrowserTreePanel;

/**
 * @author jmo
 * @version $Id:StructBrowsSplitPanel.java 1510 2007-10-18 14:35:40Z jmox $
 */
public class StructBrowsSplitPanel extends Panel {

  private static final long serialVersionUID = 1L;

  public StructBrowsSplitPanel(final String _id, final String _listmenukey,
                               final PageParameters _parameters) {
    super(_id);

    final SplitContainerBehavior beh = new SplitContainerBehavior();
    beh.setOrientation(Orientation.VERTICAL);
    this.add(beh);

    this.add(HeaderContributor.forCss(SplitHeaderPanel.class,
        "StructBrowsSplitPanel.css"));
    this.add(new SplitHeaderPanel("header", true));
    final WebMarkupContainer top = new WebMarkupContainer("top");
    top.add(new ContentPaneBehavior(50, 20));
    this.add(top);

    top.add(new StructurBrowserTreePanel("stuctbrows", _parameters,
        _listmenukey));

    final WebMarkupContainer bottom = new WebMarkupContainer("bottom");
    bottom.add(new ContentPaneBehavior(50, 20));
    this.add(bottom);

    final WebMarkupContainer menuact = new WebMarkupContainer("menuact");
    menuact.setOutputMarkupId(true);
    bottom.add(menuact);
    menuact.add(new MenuTree("menu", _parameters, _listmenukey));

  }

}
