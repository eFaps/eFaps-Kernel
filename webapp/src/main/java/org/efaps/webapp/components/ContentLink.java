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

package org.efaps.webapp.components;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.link.Link;

import org.efaps.admin.ui.Menu;
import org.efaps.db.Instance;
import org.efaps.webapp.wicket.ContentContainerPage;

public class ContentLink extends Link {

  private static final long serialVersionUID = 1L;

  private String oid;

  private Instance instance;

  public ContentLink(String id) {
    super(id);

  }

  public ContentLink(String id, String _oid) {
    super(id);
    oid = _oid;

  }

  @Override
  public void onClick() {
    if (this.oid != null) {
      this.instance = new Instance(oid);
    }
    Menu menu;
    try {
      menu = this.instance.getType().getTreeMenu();
      PageParameters u = new PageParameters();
      u.add("command", menu.getName());
      u.add("oid", this.oid);

      ContentContainerPage page = new ContentContainerPage(u);
      this.setResponsePage(page);
     
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }
}
