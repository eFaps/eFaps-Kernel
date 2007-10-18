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

package org.efaps.ui.wicket.components.listmenu;

import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.model.IModel;

import org.efaps.ui.wicket.components.listmenu.ListMenuPanel.StyleClassName;
import org.efaps.ui.wicket.models.MenuItemModel;

/**
 * Abstract class for the AjaxLinks wich are placed inside the ListMenu to
 * change the ListMenu itself. e.g. Remove SubMenus
 *
 * @author jmo
 * @version $Id$
 */
public abstract class AbstractAjaxLink extends AjaxLink {

  /**
   * Constructor setting the <i>id</i> and the <i>IModel</i>
   *
   * @param _id
   * @param _model
   */
  public AbstractAjaxLink(final String _id, final IModel _model) {
    super(_id, _model);
  }

  public abstract StyleClassName getStyleClass();

  public abstract StyleClassName getSelectedStyleClass();

  @Override
  protected void onComponentTag(ComponentTag tag) {
    super.onComponentTag(tag);
    if (((MenuItemModel) super.getModel()).isSelected()) {
      tag.put("class", getSelectedStyleClass().name);
    } else {
      tag.put("class", getStyleClass().name);
    }
  }

}
