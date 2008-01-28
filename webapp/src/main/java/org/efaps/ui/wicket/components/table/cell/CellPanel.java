/*
 * Copyright 2003-2008 The eFaps Team
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

import org.efaps.admin.ui.AbstractCommand.Target;
import org.efaps.ui.wicket.components.LabelComponent;
import org.efaps.ui.wicket.components.efapscontent.StaticImageComponent;
import org.efaps.ui.wicket.models.TableModel;
import org.efaps.ui.wicket.models.cell.TableCellModel;

/**
 * TODO description
 *
 * @author jmox
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

  public CellPanel(final String _wicketId, final TableCellModel _cellmodel,
                   final boolean _updateListMenu, final TableModel _tablemodel) {
    super(_wicketId, _cellmodel);
    final TableCellModel cellmodel = (TableCellModel) super.getModel();

    this.add(new SimpleAttributeModifier("title", cellmodel.getCellValue()));

    this.add(new WebMarkupContainer("checkbox").setVisible(false));
    WebMarkupContainer celllink;
    if (cellmodel.getReference() == null) {
      celllink = new WebMarkupContainer("link");
      celllink.setVisible(false);
    } else {
      if (_updateListMenu && cellmodel.getTarget() != Target.POPUP) {
        celllink = new AjaxLinkContainer("link", cellmodel);
      } else {
        if (cellmodel.isCheckOut()) {
          celllink = new CheckOutLink("link", cellmodel);
        } else {
          if (_tablemodel.isSearchMode()
              && cellmodel.getTarget() != Target.POPUP) {
            // do we have "connectmode",then we don't want a link in a popup
            if (_tablemodel.isSubmit()) {
              celllink = new WebMarkupContainer("link");
              celllink.setVisible(false);
            } else {
              celllink = new AjaxOpenerLink("link", cellmodel);
            }
          } else {
            celllink = new ContentContainerLink("link", cellmodel);
            if (cellmodel.getTarget() == Target.POPUP) {
              final PopupSettings popup =
                  new PopupSettings(PageMap.forName("popup"));
              ((ContentContainerLink) celllink).setPopupSettings(popup);
            }
          }
        }
      }
    }
    this.add(celllink);

    if (celllink.isVisible()) {
      celllink.add(new LabelComponent("linklabel", new Model(cellmodel
          .getCellValue())));

      if (cellmodel.getIcon() == null) {
        celllink.add(new WebMarkupContainer("linkicon").setVisible(false));
      } else {
        celllink.add(new StaticImageComponent("linkicon", cellmodel.getIcon()));
      }
      this.add(new WebMarkupContainer("icon").setVisible(false));
      this.add(new WebMarkupContainer("label").setVisible(false));
    } else {
      this
          .add(new LabelComponent("label", new Model(cellmodel.getCellValue())));
      if (cellmodel.getIcon() == null) {
        this.add(new WebMarkupContainer("icon").setVisible(false));
      } else {
        this.add(new StaticImageComponent("icon", cellmodel.getIcon()));
      }

    }
  }
}
