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

package org.efaps.ui.wicket.components.table.header;

import org.apache.wicket.markup.html.link.Link;

import org.efaps.admin.ui.AbstractCommand.SortDirection;
import org.efaps.ui.wicket.models.HeaderModel;
import org.efaps.ui.wicket.models.TableModel;
import org.efaps.ui.wicket.pages.content.form.FormPage;
import org.efaps.ui.wicket.pages.content.table.TablePage;

/**
 * This class renders the SortLink for the Header
 *
 * @author jmox
 * @version $Id:SortLinkContainer.java 1510 2007-10-18 14:35:40Z jmox $
 */
public class SortLink extends Link {

  private static final long serialVersionUID = 1L;

  public SortLink(final String _id, final HeaderModel _model) {
    super(_id, _model);
  }

  @Override
  public void onClick() {

    final TableModel tablemodel =
        (TableModel) this.findParent(HeaderPanel.class).getModel();
    final HeaderModel model = (HeaderModel) super.getModel();
    tablemodel.setSortKey(model.getName());

    for (HeaderModel headermodel : tablemodel.getHeaders()) {
      if (!headermodel.equals(model)) {
        headermodel.setSortDirection(SortDirection.NONE);
      }
    }

    if (model.getSortDirection() == SortDirection.NONE
        || model.getSortDirection() == SortDirection.DESCENDING) {
      model.setSortDirection(SortDirection.ASCENDING);
    } else {
      model.setSortDirection(SortDirection.DESCENDING);
    }

    tablemodel.setSortDirection(model.getSortDirection());

    tablemodel.sort();
    if (this.getPage() instanceof TablePage) {
      this.getRequestCycle().setResponsePage(new TablePage(tablemodel));
    } else {
      this.getRequestCycle().setResponsePage(
          new FormPage(this.getPage().getModel()));
    }

  }
}
