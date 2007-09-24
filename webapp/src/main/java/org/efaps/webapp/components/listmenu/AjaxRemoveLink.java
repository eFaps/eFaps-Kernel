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

package org.efaps.webapp.components.listmenu;

import java.util.Iterator;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;

import org.efaps.webapp.EFapsSession;
import org.efaps.webapp.components.listmenu.ListMenuPanel.Rows;
import org.efaps.webapp.components.listmenu.ListMenuPanel.StyleClassName;
import org.efaps.webapp.models.MenuItemModel;

/**
 * This Class renders a Link wich removes a Sub-ListMenuPanel from the
 * Parent-ListMenuPanel.
 *
 * @author jmo
 * @version $Id$
 */
public class AjaxRemoveLink extends AbstractAjaxLink {

  private static final long serialVersionUID = 1L;

  /**
   * Construtor setting the ID and the Model of this Component
   *
   * @param _id
   * @param _model
   */
  public AjaxRemoveLink(final String _id, final MenuItemModel _model) {
    super(_id, _model);
  }

  @Override
  public void onClick(final AjaxRequestTarget _target) {
    // find the ListMenuPanel
    ListMenuPanel listmenupanel =
        (ListMenuPanel) this.findParent(ListMenuPanel.class);

    // change the Model of the ListMenuPanel and update it
    List<?> list =
        (List<?>) ((Component) listmenupanel.iterator().next())
            .getModelObject();
    ((Rows) listmenupanel.iterator().next()).setReuseItems(false);

    int del = list.indexOf(this.getModel());
    int index = 0;
    for (int i = 0; i < del; i++) {
      if (list.get(i) instanceof MenuItemModel) {
        index++;
      }

    }

    if (del > -1) {
      list.remove(del);
      if (list.size() > del && list.get(del) instanceof List) {
        list.remove(del);
      }
      if (listmenupanel.getHeaderComponents().size() > 1) {
        listmenupanel.getHeaderComponents().remove(index);
      }
    }
    _target.addComponent(listmenupanel);

    // get the previosly selected Component
    Component previous =
        ((EFapsSession) this.getSession()).getFromCache(listmenupanel
            .getMenuKey());

    if (isChildOf(previous, listmenupanel)) {
      ((EFapsSession) this.getSession()).removeFromCache(listmenupanel
          .getMenuKey());
      // get the root
      ListMenuPanel parentlistmenupanel =
          (ListMenuPanel) listmenupanel.findParent(ListMenuPanel.class);
      while (parentlistmenupanel.getHeaderComponents().isEmpty()) {
        parentlistmenupanel =
            (ListMenuPanel) parentlistmenupanel.findParent(ListMenuPanel.class);
      }
      // select the root
      if (parentlistmenupanel.getHeaderComponents().get(0) instanceof ListMenuAjaxLinkContainer) {
        _target
            .appendJavascript(((ListMenuAjaxLinkContainer) parentlistmenupanel
                .getHeaderComponents().get(0)).getCallbackScript());
      }
    }
  }

  /**
   * This method tetermines if the Component <i>_child</i> is a child or a
   * child of a child of the Component <i>_parent</i> by searching recursivly
   * throught all childs.
   *
   * @param _child
   *                Component to by searched for
   * @param _parent
   *                Component to by searched trough
   * @return true if <i>_child</i> is child of <i>_parent</i>, else false
   */
  private boolean isChildOf(final Component _child, final Component _parent) {

    boolean ret = false;

    if (_parent instanceof Component) {
      if (_parent instanceof MarkupContainer) {
        Iterator<?> it = ((MarkupContainer) _parent).iterator();
        while (it.hasNext() && !ret) {
          ret = isChildOf(_child, (Component) it.next());
        }
      }
      if (_child.equals(_parent)) {
        return true;
      }
    }
    return ret;
  }

  @Override
  public StyleClassName getSelectedStyleClass() {
    return StyleClassName.REMOVE_SELECTED;
  }

  @Override
  public StyleClassName getStyleClass() {
    return StyleClassName.REMOVE;
  }

}
