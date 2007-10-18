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

import java.util.Iterator;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.list.ListItem;

import org.efaps.ui.wicket.components.listmenu.ListMenuPanel.Rows;
import org.efaps.ui.wicket.components.listmenu.ListMenuPanel.StyleClassName;
import org.efaps.ui.wicket.models.MenuItemModel;

/**
 * This class renders a Link wich is used to collapse and expand the ChildItems
 * of a Header inside a ListMenu.
 *
 * @author jmo
 * @version $Id$
 */
public class AjaxCollapseLink extends AbstractAjaxLink {

  private static final long serialVersionUID = 1L;

  /**
   * This instance Variable stores, if the the childs of the Header should be
   * visible or invisible. With this it information the childs will be collapsed
   * or expanded.
   */
  private boolean visible = false;

  /**
   * Constructor setting the id and the Model for this Component
   *
   * @param _id
   * @param _model
   */
  public AjaxCollapseLink(final String _id, final MenuItemModel _model) {
    super(_id, _model);
  }

  @Override
  protected void onComponentTag(ComponentTag tag) {
    super.onComponentTag(tag);
    MenuItemModel model = (MenuItemModel) super.getModel();
    if (model.isHeader() && (this.findParent(ListItem.class) != null)) {
      int padding =
          model.getLevel()
              * ((ListMenuPanel) this.findParent(ListMenuPanel.class))
                  .getPadding();
      tag.put("style", "padding-left:" + padding + "px;");
    }
  }

  @Override
  public void onClick(final AjaxRequestTarget _target) {

    Rows rows = (Rows) this.findParent(Rows.class);
    ListItem item = (ListItem) this.findParent(ListItem.class);
    boolean mark = false;
    Iterator<?> it = rows.iterator();

    while (it.hasNext()) {
      Component child = (Component) it.next();
      if (child.equals(item)) {
        Component image = (Component) this.iterator().next();
        if (image instanceof Image) {
          ((Image) image)
              .setImageResourceReference(this.visible ? ListMenuPanel.ICON_SUBMENUCLOSE
                  : ListMenuPanel.ICON_SUBMENUOPEN);
        }
        mark = true;
      } else if (mark) {
        if (child.getModelObject() instanceof MenuItemModel) {
          mark = false;
        } else {
          child.setVisible(this.visible);
        }
      }
    }
    this.visible = !this.visible;
    _target.addComponent(rows.getParent());
  }

  @Override
  public StyleClassName getSelectedStyleClass() {
    return StyleClassName.COLLAPSE_SELECTED;
  }

  @Override
  public StyleClassName getStyleClass() {
    return StyleClassName.COLLAPSE;
  }
}
