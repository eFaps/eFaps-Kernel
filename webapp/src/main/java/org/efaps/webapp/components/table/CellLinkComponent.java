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

package org.efaps.webapp.components.table;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.link.Link;

import org.efaps.admin.ui.Menu;
import org.efaps.db.Instance;
import org.efaps.webapp.wicket.ContentContainerPage;

/**
 * @author jmo
 * @version $Id$
 */
public class CellLinkComponent extends Link {
  private static final long serialVersionUID = 1L;

  private final String label;

  private final String oid;

  public CellLinkComponent(String id, String _oid, String _label) {
    super(id);
    this.label = _label;
    this.oid = _oid;

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
  public void onClick() {

    Instance instance = null;

    if (this.oid != null) {
      instance = new Instance(oid);
    }
    Menu menu;
    try {
      menu = instance.getType().getTreeMenu();
      PageParameters u = new PageParameters();
      u.add("command", menu.getName());
      u.add("oid", this.oid);
      
      
      this.setResponsePage(ContentContainerPage.class, u);

    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

}
