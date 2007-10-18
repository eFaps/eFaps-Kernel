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

package org.efaps.ui.wicket.components.table.header;

import org.apache.wicket.ResourceReference;
import org.apache.wicket.behavior.HeaderContributor;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;

import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.components.modalwindow.UpdateParentCallback;
import org.efaps.ui.wicket.models.HeaderModel;
import org.efaps.ui.wicket.models.TableModel;
import org.efaps.ui.wicket.models.TableModel.SortDirection;

/**
 * @author jmo
 * @version $Id$
 */
public class TableHeaderPanel extends Panel {

  private static final long serialVersionUID = 1L;

  public static final ResourceReference ICON_FILTER =
      new ResourceReference(TableHeaderPanel.class, "eFapsFilter.gif");

  public static final ResourceReference ICON_FILTERACTIVE =
      new ResourceReference(TableHeaderPanel.class, "eFapsFilterActive.gif");

  public static final ResourceReference ICON_SORTDESC =
      new ResourceReference(TableHeaderPanel.class, "eFapsSortDescending.gif");

  public static final ResourceReference ICON_SORTASC =
      new ResourceReference(TableHeaderPanel.class, "eFapsSortAscending.gif");

  private final ModalWindowContainer modal =
      new ModalWindowContainer("eFapsModal");

  public TableHeaderPanel(final String _id, final IModel _model) {
    super(_id, _model);
    add(HeaderContributor.forCss(getClass(), "TableHeaderPanel.css"));
    TableModel model = (TableModel) super.getModel();

    WebMarkupContainer checkbox = new WebMarkupContainer("checkbox");
    add(checkbox);
    if (!model.isShowCheckBoxes()) {
      checkbox.setVisible(false);
    }

    RepeatingView repeating = new RepeatingView("repeating");
    add(repeating);

    for (HeaderModel headermodel : model.getHeaders()) {
      WebMarkupContainer container =
          new WebMarkupContainer(repeating.newChildId());

      repeating.add(container);
      WebMarkupContainer sortlink;
      if (headermodel.isSortable()) {
        sortlink = new SortLinkContainer("sortlink", headermodel);
        sortlink.add(new SimpleAttributeModifier("class", "eFapsHeaderSortTD"));

      } else {
        sortlink = new WebMarkupContainer("sortlink");
      }
      if (headermodel.getSortDirection() == SortDirection.NONE) {
        sortlink.add((new WebMarkupContainer("iconsort")).setVisible(false));
      } else if (headermodel.getSortDirection() == SortDirection.ASCENDING) {
        sortlink.add(new Image("iconsort", ICON_SORTASC));
      } else {
        sortlink.add(new Image("iconsort", ICON_SORTDESC));
      }
      container.add(sortlink);
      sortlink.add(new Label("label", headermodel.getLabel()));
      WebMarkupContainer filterlink;
      if (headermodel.isFilterable()) {
        filterlink = new AjaxFilterLinkContainer("filterlink", headermodel);

        if (headermodel.getName().equals(model.getFilterKey())
            && model.isFiltered()) {
          filterlink.add(new Image("iconfilter", ICON_FILTERACTIVE));

        } else {
          filterlink.add(new Image("iconfilter", ICON_FILTER));
        }
      } else {
        filterlink =
            (WebMarkupContainer) (new WebMarkupContainer("filterlink"))
                .setVisible(false);
      }
      container.add(filterlink);

      add(this.modal);
      this.modal.setPageMapName("modal");

      this.modal
          .setWindowClosedCallback(new UpdateParentCallback(this, this.modal, false));
    }
  }

  public final ModalWindowContainer getModal() {
    return this.modal;
  }
}
