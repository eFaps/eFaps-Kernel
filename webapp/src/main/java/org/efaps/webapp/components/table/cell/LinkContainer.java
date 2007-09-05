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

package org.efaps.webapp.components.table.cell;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;

import org.efaps.admin.ui.Menu;
import org.efaps.db.Instance;
import org.efaps.webapp.models.TableModel.CellModel;
import org.efaps.webapp.pages.ContentContainerPage;

/**
 * @author jmo
 * @version $Id$
 * 
 */
public class LinkContainer extends Link {

  private static final long serialVersionUID = 1L;

  public LinkContainer(String id, IModel object) {
    super(id, object);

  }

  @Override
  public void onClick() {
    Instance instance = null;
    CellModel cellmodel = (CellModel) super.getModel();
    if (cellmodel.getOid() != null) {
      instance = new Instance(cellmodel.getOid());
    }
    Menu menu;
    try {
      menu = instance.getType().getTreeMenu();
      PageParameters parameters = new PageParameters();
      parameters.add("command", menu.getName());
      parameters.add("oid", cellmodel.getOid());

      this.setResponsePage(ContentContainerPage.class, parameters);

    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

}
