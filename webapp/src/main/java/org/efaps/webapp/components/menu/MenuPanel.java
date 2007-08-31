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

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import org.efaps.webapp.components.FormContainer;
import org.efaps.webapp.components.modalwindow.ModalWindowContainer;
import org.efaps.webapp.components.modalwindow.UpdateParentCallback;
import org.efaps.webapp.models.MenuItemModel;
import org.efaps.webapp.models.ModelAbstract;

/**
 * @author jmo
 * @version $Id$
 */
public class MenuPanel extends Panel {

  private static final long serialVersionUID = 1L;

  private final ModalWindowContainer modal =
      new ModalWindowContainer("eFapsModal");

  public MenuPanel(final String _id, IModel _model) {
    this(_id, _model, null);
  }

  public MenuPanel(final String _id, IModel _model, FormContainer _form) {
    super(_id, _model);

    if (_model instanceof ModelAbstract) {
      ModelAbstract model = (ModelAbstract) _model;
      super.setModel(model);
      try {
        if (model.getCommand().getTargetMenu() != null) {
          MenuComponent menu =
              new MenuComponent("eFapsMenu", new MenuItemModel(model
                  .getCommand().getTargetMenu().getName(), model.getOid()),
                  _form);
          add(menu);
        } else {
          add(new WebMarkupContainer("eFapsMenu"));
        }
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

    }

    add(modal);
    modal.setPageMapName("modal");

    modal.setWindowClosedCallback(new UpdateParentCallback(this, modal));

  }

  public final ModalWindowContainer getModal() {
    return this.modal;
  }

}
