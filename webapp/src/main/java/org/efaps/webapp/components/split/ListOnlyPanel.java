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

package org.efaps.webapp.components.split;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.panel.Panel;

import org.efaps.webapp.components.dojo.ContentPaneBehavior;
import org.efaps.webapp.components.listmenu.ListMenuPanel;

public class ListOnlyPanel extends Panel {

  private static final long serialVersionUID = 1L;

  public ListOnlyPanel(final String _id, final String _listmenukey,
                            final PageParameters _parameters) {
    super(_id);

    this.add(new ListMenuPanel("menu", _listmenukey, _parameters, true)
        .setOutputMarkupId(true));
    ContentPaneBehavior cpb = new ContentPaneBehavior();
    cpb.setSizemin(20);
    cpb.setSizeshare(20);
    this.add(cpb);

  }

}
