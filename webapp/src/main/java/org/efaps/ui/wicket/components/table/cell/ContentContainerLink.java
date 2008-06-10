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

import org.apache.wicket.PageParameters;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;

import org.efaps.admin.ui.Menu;
import org.efaps.admin.ui.AbstractCommand.Target;
import org.efaps.db.Instance;
import org.efaps.ui.wicket.models.cell.UITableCell;
import org.efaps.ui.wicket.pages.contentcontainer.ContentContainerPage;
import org.efaps.ui.wicket.pages.error.ErrorPage;

/**
 * @author jmox
 * @version $Id:LinkContainer.java 1510 2007-10-18 14:35:40Z jmox $
 */
public class ContentContainerLink<T> extends Link<T> {

  private static final long serialVersionUID = 1L;

  public ContentContainerLink(final String _wicketId,
                              final IModel<T> _model) {
    super(_wicketId, _model);
  }

  @Override
  public void onClick() {
    Instance instance = null;
    final UITableCell cellmodel = (UITableCell) super.getModelObject();
    if (cellmodel.getOid() != null) {
      instance = new Instance(cellmodel.getOid());
      Menu menu = null;
      try {
        menu = Menu.getTypeTreeMenu(instance.getType());
      } catch (final Exception e) {
        throw new RestartResponseException(new ErrorPage(e));
      }
      if (menu == null) {
        final Exception ex =
            new Exception("no tree menu defined for type "
                + instance.getType().getName());
        throw new RestartResponseException(new ErrorPage(ex));
      }
      final PageParameters parameters = new PageParameters();
      parameters.add("command", menu.getUUID().toString());
      parameters.add("oid", cellmodel.getOid());
      ContentContainerPage page;
      if (cellmodel.getTarget() == Target.POPUP) {
        page = new ContentContainerPage(parameters);
      } else {
        page =
            new ContentContainerPage(this.getPage().getPageMap(), parameters);
      }
      this.setResponsePage(page);

    }

  }

}
