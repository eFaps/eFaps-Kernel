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

package org.efaps.webapp.components.table.header;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;

import org.efaps.webapp.components.modalwindow.ModalWindowContainer;
import org.efaps.webapp.models.HeaderModel;
import org.efaps.webapp.models.TableModel;
import org.efaps.webapp.pages.content.table.filter.FilterPage;


/**
 * @author jmox
 * @version $Id$
 *
 */
public class AjaxFilterLinkContainer extends AjaxLink {

  private static final long serialVersionUID = 1L;

  public AjaxFilterLinkContainer(final String _id, final HeaderModel _model) {
    super(_id, _model);

  }

  @Override
  public void onClick(final AjaxRequestTarget _target) {
    TableHeaderPanel tableheaderpanel =
        (TableHeaderPanel) this.findParent(TableHeaderPanel.class);

    TableModel tablemodel =
        (TableModel) this.findParent(TableHeaderPanel.class).getModel();
    tablemodel.setFilterKey(((HeaderModel) super.getModel()).getName());

    FilterPageCreator pagecreator =
        new FilterPageCreator(tablemodel, tableheaderpanel.getModal());

    tableheaderpanel.getModal().setPageCreator(pagecreator);
    tableheaderpanel.getModal().setInitialWidth(300);
    tableheaderpanel.getModal().show(_target);
  }

  private class FilterPageCreator implements ModalWindow.PageCreator {

    private static final long serialVersionUID = 1L;

    private final TableModel model;

    private final ModalWindowContainer modalwindow;

    public FilterPageCreator(final TableModel _model,
                             final ModalWindowContainer _modalwindow) {
      this.model = _model;
      this.modalwindow = _modalwindow;
    }

    public Page createPage() {

      return new FilterPage(this.model, this.modalwindow);
    }

  }
}
