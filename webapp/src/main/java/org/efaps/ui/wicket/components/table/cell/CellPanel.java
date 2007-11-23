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

package org.efaps.ui.wicket.components.table.cell;

import org.apache.wicket.PageMap;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import org.efaps.admin.ui.CommandAbstract;
import org.efaps.ui.wicket.components.LabelComponent;
import org.efaps.ui.wicket.components.StaticImageComponent;
import org.efaps.ui.wicket.models.CellModel;
import org.efaps.ui.wicket.models.TableModel;

/**
 * @author jmo
 * @version $Id:CellPanel.java 1510 2007-10-18 14:35:40Z jmox $
 */
public class CellPanel extends Panel {

  private static final long serialVersionUID = 1L;

  public CellPanel(final String _id, final String _oid) {
    super(_id);
    this.add(new CheckBoxComponent("checkbox", _oid));
    this.add(new WebMarkupContainer("link").setVisible(false));
    this.add(new WebMarkupContainer("icon").setVisible(false));
    this.add(new WebMarkupContainer("label").setVisible(false));
  }

  public CellPanel(final String id, final CellModel _cellmodel,
                   final boolean _updateListMenu, final TableModel _tablemodel) {
    super(id, _cellmodel);
    CellModel cellmodel = (CellModel) super.getModel();

    // TODO add the titel only if wanted or??
    this.add(new SimpleAttributeModifier("title", cellmodel.getCellValue()));

    this.add(new WebMarkupContainer("checkbox").setVisible(false));
    WebMarkupContainer celllink;
    if (cellmodel.getReference() != null) {
      if (_updateListMenu
          && cellmodel.getTarget() != CommandAbstract.TARGET_POPUP) {
        celllink = new AjaxLinkContainer("link", cellmodel);
      } else {
        if (cellmodel.isCheckOut()) {
          celllink = new CheckOutLink("link", cellmodel);
        } else {
          if (_tablemodel.isSearchMode()
              && cellmodel.getTarget() != CommandAbstract.TARGET_POPUP) {
            // do we have "connectmode",then we don't want a link in a popup
            if (_tablemodel.isSubmit()) {
              celllink = new WebMarkupContainer("link");
              celllink.setVisible(false);
            } else {
              celllink = new AjaxOpenerLink("link", cellmodel);
            }
          } else {
            celllink = new ContentContainerLink("link", cellmodel);
            if (cellmodel.getTarget() == CommandAbstract.TARGET_POPUP) {
              PopupSettings popup = new PopupSettings(PageMap.forName("popup"));
              ((ContentContainerLink) celllink).setPopupSettings(popup);
            }
          }
        }
      }
    } else {
      celllink = new WebMarkupContainer("link");
      celllink.setVisible(false);
    }
    this.add(celllink);

    if (celllink.isVisible()) {
      celllink.add(new LabelComponent("linklabel", new Model(cellmodel
          .getCellValue())));

      if (cellmodel.getIcon() != null) {
        celllink.add(new StaticImageComponent("linkicon", new Model(cellmodel
            .getIcon())));
      } else {
        celllink.add(new WebMarkupContainer("linkicon").setVisible(false));
      }
      this.add(new WebMarkupContainer("icon").setVisible(false));
      this.add(new WebMarkupContainer("label").setVisible(false));
    } else {
      this
          .add(new LabelComponent("label", new Model(cellmodel.getCellValue())));
      if (cellmodel.getIcon() != null) {
        this.add(new StaticImageComponent("icon",
            new Model(cellmodel.getIcon())));
      } else {
        this.add(new WebMarkupContainer("icon").setVisible(false));
      }

    }
  }
}
