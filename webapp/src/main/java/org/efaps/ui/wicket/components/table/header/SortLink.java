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
 * Revision:        $Rev:1510 $
 * Last Changed:    $Date:2007-10-18 09:35:40 -0500 (Thu, 18 Oct 2007) $
 * Last Changed By: $Author:jmox $
 */

package org.efaps.ui.wicket.components.table.header;

import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;

import org.efaps.admin.ui.AbstractCommand.SortDirection;
import org.efaps.ui.wicket.models.FormModel;
import org.efaps.ui.wicket.models.TableModel;
import org.efaps.ui.wicket.models.objects.UIForm;
import org.efaps.ui.wicket.models.objects.UITable;
import org.efaps.ui.wicket.models.objects.UITableHeader;
import org.efaps.ui.wicket.pages.content.AbstractContentPage;
import org.efaps.ui.wicket.pages.content.form.FormPage;
import org.efaps.ui.wicket.pages.content.table.TablePage;

/**
 * This class renders the SortLink for the Header
 *
 * @author jmox
 * @version $Id:SortLinkContainer.java 1510 2007-10-18 14:35:40Z jmox $
 */
public class SortLink extends Link<UITableHeader> {

  private static final long serialVersionUID = 1L;


  public SortLink(final String _id, final IModel<UITableHeader> _model) {
    super(_id, _model);
  }

  @Override
  public void onClick() {

    final UITable uiTable = (UITable) (this
        .findParent(HeaderPanel.class)).getDefaultModelObject();
    final UITableHeader uiTableHeader = super.getModelObject();
    uiTable.setSortKey(uiTableHeader.getName());

    for (final UITableHeader headermodel : uiTable.getHeaders()) {
      if (!headermodel.equals(uiTableHeader)) {
        headermodel.setSortDirection(SortDirection.NONE);
      }
    }

    if (uiTableHeader.getSortDirection() == SortDirection.NONE
        || uiTableHeader.getSortDirection() == SortDirection.DESCENDING) {
      uiTableHeader.setSortDirection(SortDirection.ASCENDING);
    } else {
      uiTableHeader.setSortDirection(SortDirection.DESCENDING);
    }

    uiTable.setSortDirection(uiTableHeader.getSortDirection());
    uiTable.sort();

    final String menuTreeKey
                           = ((AbstractContentPage) getPage()).getMenuTreeKey();
    AbstractContentPage page;
    if (getPage() instanceof TablePage) {
      page = new TablePage(new TableModel(uiTable));
    } else {
      page = new FormPage(new FormModel((UIForm) getPage()
          .getDefaultModelObject()));
    }
    page.setMenuTreeKey(menuTreeKey);
    getRequestCycle().setResponsePage(page);
  }
}
