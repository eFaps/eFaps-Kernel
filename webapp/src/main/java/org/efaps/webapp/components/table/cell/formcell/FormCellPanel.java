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

package org.efaps.webapp.components.table.cell.formcell;

import org.apache.wicket.PageMap;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import org.efaps.admin.ui.CommandAbstract;
import org.efaps.webapp.components.StaticImageComponent;
import org.efaps.webapp.components.table.cell.AjaxLinkContainer;
import org.efaps.webapp.components.table.cell.CellContainer;
import org.efaps.webapp.components.table.cell.LabelComponent;
import org.efaps.webapp.components.table.cell.LinkContainer;
import org.efaps.webapp.models.FormModel.FormCellModel;

/**
 * @author jmo
 * @version $Id$
 */
public class FormCellPanel extends Panel {

  private static final long serialVersionUID = 1L;

  public FormCellPanel(final String id, final IModel model,
                       final int _maxgroupcount, final int groupcount,
                       final boolean _updateListMenu) {
    super(id, model);
    FormCellModel cellmodel = (FormCellModel) super.getModel();
    CellContainer labelcontainer = new CellContainer("tdlabel", model);
    this.add(labelcontainer);

    if (cellmodel.isRequired()) {
      labelcontainer.add(new SimpleAttributeModifier("class",
          "eFapsFormLabelRequired"));
    } else {
      labelcontainer
          .add(new SimpleAttributeModifier("class", "eFapsFormLabel"));
    }

    labelcontainer.add(new LabelComponent("labellabel", new Model(cellmodel
        .getCellLabel())));

    CellContainer valuecontainer = new CellContainer("tdvalue", model);
    valuecontainer.add(new SimpleAttributeModifier("class",
        "eFapsFormInputField"));
    Integer colspan = 2 * (_maxgroupcount - groupcount) + 1;
    valuecontainer.add(new SimpleAttributeModifier("colspan", colspan
        .toString()));

    this.add(valuecontainer);

    WebMarkupContainer celllink;
    if (cellmodel.getReference() != null) {
      if (_updateListMenu
          && cellmodel.getTarget() != CommandAbstract.TARGET_POPUP) {
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
    valuecontainer.add(celllink);

    if (cellmodel.getIcon() != null) {
      celllink.add(new StaticImageComponent("icon", new Model(cellmodel
          .getIcon())));
    } else {
      celllink.add(new WebMarkupContainer("icon").setVisible(false));
    }

    celllink.add(new LabelComponent("valuelabel", new Model(cellmodel
        .getCellValue())));

  }

}
