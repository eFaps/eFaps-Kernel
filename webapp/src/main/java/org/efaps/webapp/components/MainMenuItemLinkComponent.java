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

package org.efaps.webapp.components;

import org.apache.wicket.PageMap;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.link.InlineFrame;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;

import org.efaps.admin.event.EventType;
import org.efaps.admin.ui.CommandAbstract;
import org.efaps.util.EFapsException;
import org.efaps.webapp.models.MenuItemModel;
import org.efaps.webapp.wicket.MainPage;
import org.efaps.webapp.wicket.WebFormPage;
import org.efaps.webapp.wicket.WebTablePage;

/**
 * @author jmo
 * @version $Id$
 */
public class MainMenuItemLinkComponent extends Link {
  private static final long serialVersionUID = 1L;

  public MainMenuItemLinkComponent(String _id, IModel _menuItem) {
    super(_id, _menuItem);
  }

  @Override
  protected void onRender(MarkupStream markupStream) {
    markupStream.next();
  }

  @Override
  public void onClick() {
    MenuItemModel model = (MenuItemModel) super.getModel();
    CommandAbstract command = model.getCommand();
    if (command.getTargetTable() != null) {

      PageParameters para = new PageParameters("command=" + command.getName());

      InlineFrame c =
          new InlineFrame("eFapsContentFrame", PageMap
              .forName(MainPage.INLINEFRAMENAME), WebTablePage.class, para);

      this.getPage().addOrReplace(c);
    }
    if (command.getTargetForm() != null) {
      PageParameters para = new PageParameters("command=" + command.getName());

      InlineFrame c =
          new InlineFrame("eFapsContentFrame", PageMap
              .forName(MainPage.INLINEFRAMENAME), WebFormPage.class, para);

      this.getPage().addOrReplace(c);

    }

    else if (command.hasEvents(EventType.UI_COMMAND_EXECUTE)) {

      try {
        command.executeEvents(EventType.UI_COMMAND_EXECUTE);
      } catch (EFapsException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      if ("true".equals(command.getProperty("NoUpdateAfterCOMMAND"))) {
        this.getRequestCycle().setRequestTarget(null);
      }
    }

  }
}
