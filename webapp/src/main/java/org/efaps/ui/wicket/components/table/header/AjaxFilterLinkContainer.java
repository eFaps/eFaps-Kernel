/*
 * Copyright 2003 - 2009 The eFaps Team
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

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.model.IModel;

import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.models.TableModel;
import org.efaps.ui.wicket.models.objects.UITable;
import org.efaps.ui.wicket.models.objects.UITableHeader;
import org.efaps.ui.wicket.pages.content.table.filter.FilterPage;

/**
 * @author jmox
 * @version $Id$
 */
public class AjaxFilterLinkContainer extends AjaxLink<UITableHeader> {

  private static final long serialVersionUID = 1L;

  public AjaxFilterLinkContainer(final String _id, final IModel<UITableHeader> _model) {
    super(_id, _model);

  }

  @Override
  public void onClick(final AjaxRequestTarget _target) {
    final HeaderPanel tableheaderpanel =
        this.findParent(HeaderPanel.class);

    final UITable tablemodel =
        (UITable) tableheaderpanel.getDefaultModelObject();
    tablemodel.setFilterKey(super.getModelObject().getName());

    final FilterPageCreator pagecreator =
        new FilterPageCreator(tablemodel, tableheaderpanel.getModal());

    tableheaderpanel.getModal().setPageCreator(pagecreator);
    tableheaderpanel.getModal().setInitialWidth(300);
    tableheaderpanel.getModal().show(_target);
  }

  private class FilterPageCreator implements ModalWindow.PageCreator {

    private static final long serialVersionUID = 1L;

    private final UITable uitable;

    private final ModalWindowContainer modalwindow;

    public FilterPageCreator(final UITable _model,
                             final ModalWindowContainer _modalwindow) {
      this.uitable = _model;
      this.modalwindow = _modalwindow;
    }

    public Page createPage() {

      return new FilterPage(new TableModel(this.uitable), this.modalwindow);
    }

  }
}
