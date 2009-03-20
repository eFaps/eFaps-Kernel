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

package org.efaps.ui.wicket.components.menu;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.ui.wicket.components.FormContainer;
import org.efaps.ui.wicket.models.MenuItemModel;
import org.efaps.ui.wicket.models.objects.AbstractUIObject;
import org.efaps.ui.wicket.models.objects.UIMenuItem;
import org.efaps.ui.wicket.models.objects.UISearchItem;

/**
 * @author jmox
 * @version $Id:MenuPanel.java 1510 2007-10-18 14:35:40Z jmox $
 */
public class MenuPanel extends Panel {

  /**
   * Needed for serialization.
   */
  private static final long serialVersionUID = 1L;

  /**
   * Constructor.
   *
   * @param _wicketId     wicket id for the component
   * @param _model        model for this component
   */
  public MenuPanel(final String _wicketId, final IModel<?> _model) {
    this(_wicketId, _model, null);
  }

  /**
   * Constructor.
   *
   * @param _wicketId       wicket id for this component
   * @param _model    model for this component
   * @param _form     form
   */
  public MenuPanel(final String _wicketId, final IModel<?> _model,
                   final FormContainer _form) {
    super(_wicketId,  _model);

    final AbstractUIObject model = (AbstractUIObject) _model.getObject();

    if (model.getMode() == TargetMode.SEARCH
        && model.getCallingCommandUUID() != null) {
      final MenuContainer menu = new MenuContainer("eFapsMenu",
          new MenuItemModel(new UISearchItem(model.getCallingCommand()
              .getTargetSearch().getUUID())), _form);
      add(menu);
    } else if (model.getCommand().getTargetMenu() != null) {
      final MenuContainer menu = new MenuContainer("eFapsMenu",
          new MenuItemModel(new UIMenuItem(model.getCommand().getTargetMenu()
              .getUUID(), model.getInstanceKey())), _form);
      add(menu);
    } else {
      add(new WebMarkupContainer("eFapsMenu"));
    }
  }
}
