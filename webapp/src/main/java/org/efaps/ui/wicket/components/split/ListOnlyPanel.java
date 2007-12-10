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

package org.efaps.ui.wicket.components.split;

import org.apache.wicket.PageParameters;
import org.apache.wicket.behavior.HeaderContributor;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;

import org.efaps.ui.wicket.behaviors.dojo.ContentPaneBehavior;
import org.efaps.ui.wicket.components.menutree.MenuTree;

/**
 * @author jmox
 * @version $Id$
 */
public class ListOnlyPanel extends Panel {

  private static final long serialVersionUID = 1L;

  public ListOnlyPanel(final String _wicketId, final String _listmenukey,
                       final PageParameters _parameters) {
    super(_wicketId);
    this.add(HeaderContributor.forCss(SplitHeaderPanel.class,
        "ListOnlyPanel.css"));
    this.add(new ContentPaneBehavior(20, 20));

    final SplitHeaderPanel header = new SplitHeaderPanel("header", false);
    this.add(header);

    final WebMarkupContainer overflow = new WebMarkupContainer("overflow");
    overflow.setOutputMarkupId(true);
    overflow.add(new MenuTree("menu", _parameters, _listmenukey)
        .setOutputMarkupId(true));
    this.add(overflow);

    header.addHideComponent(overflow);
  }

}
