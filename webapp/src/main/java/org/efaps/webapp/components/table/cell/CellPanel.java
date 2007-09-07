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

import org.apache.wicket.PageMap;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import org.efaps.admin.ui.CommandAbstract;
import org.efaps.webapp.components.StaticImageComponent;
import org.efaps.webapp.models.TableModel.CellModel;

/**
 * @author jmo
 * @version $Id$
 */
public class CellPanel extends Panel {

  private static final long serialVersionUID = 1L;

  public CellPanel(final String id, final IModel model, final boolean _ajax) {
    super(id, model);
    CellContainer cellcontainer = new CellContainer("td", model);
    cellcontainer.setOutputMarkupId(true);
    this.add(cellcontainer);
    CellModel cellmodel = (CellModel) super.getModel();

    WebMarkupContainer celllink;
    if (cellmodel.getReference() != null) {
      if (_ajax && cellmodel.getTarget() != CommandAbstract.TARGET_POPUP) {
        celllink = new AjaxLinkContainer("link", cellmodel);
      } else {
        celllink = new LinkContainer("link", cellmodel);
        if (cellmodel.getTarget() == CommandAbstract.TARGET_POPUP) {
          PopupSettings popup = new PopupSettings(PageMap.forName("popup"));
          ((LinkContainer) celllink).setPopupSettings(popup);
        }

      }
    } else {
      celllink = new WebMarkupContainer("link");
    }
    cellcontainer.add(celllink);

    celllink.add(new LabelComponent("label",
        new Model(cellmodel.getCellValue())));

    if (cellmodel.getIcon() != null) {
      celllink.add(new StaticImageComponent("icon", new Model(cellmodel
          .getIcon())));
    } else {
      celllink.add(new WebMarkupContainer("icon").setVisible(false));
    }
  }
}
