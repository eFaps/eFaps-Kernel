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

package org.efaps.webapp.components.menu;

import org.apache.wicket.PageMap;
import org.apache.wicket.PageParameters;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.link.InlineFrame;

import org.efaps.admin.event.EventType;
import org.efaps.admin.ui.CommandAbstract;
import org.efaps.util.EFapsException;
import org.efaps.webapp.EFapsSession;
import org.efaps.webapp.models.MenuItemModel;
import org.efaps.webapp.pages.content.form.FormPage;
import org.efaps.webapp.pages.content.structurbrowser.StructurBrowserPage;
import org.efaps.webapp.pages.content.table.TablePage;
import org.efaps.webapp.pages.error.ErrorPage;
import org.efaps.webapp.pages.main.MainPage;

/**
 * @author jmox
 * @version $Id$
 */
public class StandardLink extends AbstractMenuItemLink {

  private static final long serialVersionUID = 1L;

  public StandardLink(String _id, final MenuItemModel _model) {
    super(_id, _model);
  }

  @Override
  public void onClick() {
    MenuItemModel model = (MenuItemModel) super.getModel();

    CommandAbstract command = model.getCommand();
    if (command.getTarget() == CommandAbstract.TARGET_POPUP) {
      ((EFapsSession) this.getSession()).setOpenerModel(this.getPage()
          .getModel());
    }
    PageParameters para = new PageParameters("command=" + command.getUUID());
    para.add("oid", model.getOid());
    if (command.getTargetTable() != null) {
      if (command.getProperty("TargetStructurBrowserField") != null) {
        InlineFrame c =
            new InlineFrame(MainPage.IFRAME_WICKETID, PageMap
                .forName(MainPage.IFRAME_PAGEMAP_NAME),
                StructurBrowserPage.class, para);
        this.getPage().addOrReplace(c);
      } else {
        if (this.getPage() instanceof MainPage) {
          InlineFrame c =
              new InlineFrame(MainPage.IFRAME_WICKETID, PageMap
                  .forName(MainPage.IFRAME_PAGEMAP_NAME), TablePage.class,
                  para);

          this.getPage().addOrReplace(c);
        } else {
          this.setResponsePage(TablePage.class, para);
        }
      }
    } else if (command.getTargetForm() != null
        || command.getTargetSearch() != null) {
      if (this.getPage() instanceof MainPage
          && command.getTargetSearch() == null) {
        InlineFrame c =
            new InlineFrame(MainPage.IFRAME_WICKETID, PageMap
                .forName(MainPage.IFRAME_PAGEMAP_NAME), FormPage.class, para);
        this.getPage().addOrReplace(c);
      } else {
        this.setResponsePage(FormPage.class, para);
      }
    } else if (command.hasEvents(EventType.UI_COMMAND_EXECUTE)) {

      try {
        command.executeEvents(EventType.UI_COMMAND_EXECUTE);
      } catch (EFapsException e) {
        throw new RestartResponseException(new ErrorPage(e));
      }
      if ("true".equals(command.getProperty("NoUpdateAfterCOMMAND"))) {
        this.getRequestCycle().setRequestTarget(null);
      }
    }

  }
}
