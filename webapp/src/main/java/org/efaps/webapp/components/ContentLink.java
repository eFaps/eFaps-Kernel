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
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;

import org.efaps.admin.ui.Menu;
import org.efaps.db.Instance;
import org.efaps.webapp.wicket.ContentContainerPage;

public class ContentLink extends AjaxLink {

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
  public void onClick(AjaxRequestTarget target) {
    initialise();
    PageParameters u = new PageParameters("command=Admin_User_PersonMyDesk");
    this.setResponsePage(ContentContainerPage.class, u);
  }

  private void initialise() {

    if (this.oid != null) {
      this.instance = new Instance(oid);
    }

    try {
      Menu menu = this.instance.getType().getTreeMenu();
      String form = menu.getTargetForm().getName();

    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

}
