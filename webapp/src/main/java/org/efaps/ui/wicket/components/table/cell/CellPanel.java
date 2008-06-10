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
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import org.efaps.admin.ui.AbstractCommand.Target;
import org.efaps.ui.wicket.components.LabelComponent;
import org.efaps.ui.wicket.components.efapscontent.StaticImageComponent;
import org.efaps.ui.wicket.models.cell.UITableCell;
import org.efaps.ui.wicket.models.objects.UITable;

/**
 * TODO description
 *
 * @author jmox
 * @version $Id:CellPanel.java 1510 2007-10-18 14:35:40Z jmox $
 */
public class CellPanel extends Panel<UITableCell> {

  private static final long serialVersionUID = 1L;

  public CellPanel(final String _id, final String _oid) {
    super(_id);
    this.add(new CheckBoxComponent("checkbox", _oid));
    this.add(new WebMarkupContainer<Object>("link").setVisible(false));
    this.add(new WebMarkupContainer<Object>("icon").setVisible(false));
    this.add(new WebMarkupContainer<Object>("label").setVisible(false));
  }

  public CellPanel(final String _wicketId, final IModel<UITableCell> _cellmodel,
                   final boolean _updateListMenu, final UITable _uitable) {
    super(_wicketId, _cellmodel);
    final UITableCell cellmodel = super.getModelObject();

    this.add(new SimpleAttributeModifier("title", cellmodel.getCellValue()));

    this.add(new WebMarkupContainer<Object>("checkbox").setVisible(false));
    WebMarkupContainer<UITableCell> celllink;
    if (cellmodel.getReference() == null) {
      celllink = new WebMarkupContainer<UITableCell>("link");
      celllink.setVisible(false);
    } else {
      if (_updateListMenu && cellmodel.getTarget() != Target.POPUP) {
        celllink = new AjaxLinkContainer<UITableCell>("link", _cellmodel);
      } else {
        if (cellmodel.isCheckOut()) {
          celllink = new CheckOutLink("link", _cellmodel);
        } else {
          if (_uitable.isSearchMode()
              && cellmodel.getTarget() != Target.POPUP) {
            // do we have "connectmode",then we don't want a link in a popup
            if (_uitable.isSubmit()) {
              celllink = new WebMarkupContainer<UITableCell>("link");
              celllink.setVisible(false);
            } else {
              celllink = new AjaxOpenerLink("link", _cellmodel);
            }
          } else {
            celllink = new ContentContainerLink<UITableCell>("link", _cellmodel);
            if (cellmodel.getTarget() == Target.POPUP) {
              final PopupSettings popup =
                  new PopupSettings(PageMap.forName("popup"));
              ((ContentContainerLink<UITableCell>) celllink).setPopupSettings(popup);
            }
          }
        }
      }
    }
    this.add(celllink);

    if (celllink.isVisible()) {
      celllink.add(new LabelComponent("linklabel", new Model<String>(cellmodel
          .getCellValue())));

      if (cellmodel.getIcon() == null) {
        celllink.add(new WebMarkupContainer<Object>("linkicon").setVisible(false));
      } else {
        celllink.add(new StaticImageComponent("linkicon", cellmodel.getIcon()));
      }
      this.add(new WebMarkupContainer<Object>("icon").setVisible(false));
      this.add(new WebMarkupContainer<Object>("label").setVisible(false));
    } else {
      this
          .add(new LabelComponent("label", new Model<String>(cellmodel.getCellValue())));
      if (cellmodel.getIcon() == null) {
        this.add(new WebMarkupContainer<Object>("icon").setVisible(false));
      } else {
        this.add(new StaticImageComponent("icon", cellmodel.getIcon()));
      }

    }
  }
}
