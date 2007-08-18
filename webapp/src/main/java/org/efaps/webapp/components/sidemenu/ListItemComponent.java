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

package org.efaps.webapp.components.sidemenu;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.wicketstuff.dojo.markup.html.container.split.DojoSplitContainer;

import org.efaps.webapp.components.EFapsContainerComponent;
import org.efaps.webapp.wicket.WebTablePage;

/**
 * @author jmo
 * @version $Id$
 */
public class ListItemComponent extends AjaxLink {
  private static final long serialVersionUID = 1L;

  private final String label;

  public ListItemComponent(String id, String _label) {
    super(id);
    this.label = _label;

  }

  @Override
  protected void onComponentTag(final ComponentTag tag) {
    tag.setName("a");
    super.onComponentTag(tag);
  }

  @Override
  protected void onComponentTagBody(MarkupStream _markupStream,
      ComponentTag _openTag) {
    super.replaceComponentTagBody(_markupStream, _openTag, this.label);
  }

  @Override
  public void onClick(AjaxRequestTarget target) {
    System.out.println("test");

    PageParameters u = new PageParameters();
    u.add("oid", "64.1");
    u.add("command", "Admin_User_PersonTree_Roles");

    EFapsContainerComponent page =
        new EFapsContainerComponent("containerrechts", WebTablePage.class, u);
    MarkupContainer x = this.findParent(DojoSplitContainer.class);
    x.replace(page);
    target.addComponent(page);
    target.appendJavascript("djConfig.searchIds.push(\"containerrechts2\");");

  }
}
