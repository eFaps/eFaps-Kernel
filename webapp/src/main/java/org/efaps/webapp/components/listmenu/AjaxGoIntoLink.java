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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.PageMap;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.link.InlineFrame;
import org.apache.wicket.model.Model;

import org.efaps.admin.ui.CommandAbstract;
import org.efaps.webapp.EFapsSession;
import org.efaps.webapp.components.listmenu.ListMenuPanel.Rows;
import org.efaps.webapp.components.listmenu.ListMenuPanel.StyleClassName;
import org.efaps.webapp.models.MenuItemModel;
import org.efaps.webapp.pages.ContentContainerPage;
import org.efaps.webapp.pages.WebFormPage;
import org.efaps.webapp.pages.WebTablePage;

/**
 * @author jmo
 * @version $Id$
 */
public class AjaxGoIntoLink extends AbstractAjaxLink {

  private static final long serialVersionUID = 1L;

  public AjaxGoIntoLink(final String _id, final MenuItemModel _model) {
    super(_id, _model);
  }

  @Override
  public void onClick(final AjaxRequestTarget _target) {
    // update the Content
    MenuItemModel model = (MenuItemModel) super.getModel();

    CommandAbstract cmd = model.getCommand();
    PageParameters para = new PageParameters();
    para.add("oid", model.getOid());
    para.add("command", cmd.getUUID().toString());

    InlineFrame page;
    if (cmd.getTargetTable() != null) {
      page =
          new InlineFrame(ContentContainerPage.IFRAME_WICKETID, PageMap
              .forName(ContentContainerPage.IFRAME_PAGEMAP_NAME),
              WebTablePage.class, para);
    } else {
      page =
          new InlineFrame(ContentContainerPage.IFRAME_WICKETID, PageMap
              .forName(ContentContainerPage.IFRAME_PAGEMAP_NAME),
              WebFormPage.class, para);
    }

    InlineFrame component =
        (InlineFrame) getPage().get(
            ((ContentContainerPage) getPage()).getInlinePath());
    page.setOutputMarkupId(true);

    component.replaceWith(page);
    _target.addComponent(page.getParent());

    // get the selected ListMenuPanel
    ListMenuPanel listmenupanel =
        (ListMenuPanel) this.findParent(ListMenuPanel.class);
    // clear the selected cache, because we remove the ListMenu
    ((EFapsSession) this.getSession()).removeFromCache(listmenupanel
        .getMenuKey());

    // get the RootListMenuPanel
    ListMenuPanel rootlistmenupanel = listmenupanel;
    while (rootlistmenupanel.findParent(ListMenuPanel.class) != null) {
      rootlistmenupanel =
          (ListMenuPanel) rootlistmenupanel.findParent(ListMenuPanel.class);
    }

    // get the Rows to exchange them
    Rows row = (Rows) this.findParent(Rows.class);
    Rows rowparent = (Rows) row.findParent(Rows.class);
    while (rowparent.findParent(Rows.class) != null) {
      rowparent = (Rows) rowparent.findParent(Rows.class);
    }
    // get the complete Model for the Root and save a copy of it
    List<?> rootmodel = getCompleteModelOf(rootlistmenupanel);
    model.setAncestor(new Model((Serializable) getCopyOf(rootmodel, null)));

    // correct the actual model
    List<?> newmodel = getCompleteModelOf(listmenupanel);
    Iterator<?> it = newmodel.iterator();
    while (it.hasNext()) {
      Object object = it.next();
      boolean remove = false;
      if (!object.equals(this.getModel())) {
        it.remove();
        remove = true;
      }
      if (it.hasNext()) {
        object = it.next();
        if (object instanceof List && remove) {
          it.remove();
        }
      }
    }
    setLevel(newmodel, -1);
    model = (MenuItemModel) this.getModel();
    model.setSelected(true);

    rowparent.removeAll();
    rowparent.setList(row.getList());
    _target.addComponent(rootlistmenupanel);

  }

  private List<?> getCopyOf(List<?> model, List<MenuItemModel> list) {
    List<Object> ret = new ArrayList<Object>();
    MenuItemModel copyMenuItemModel = null;
    int j = 0;

    for (int i = 0; i < model.size(); i++) {
      if (model.get(i) instanceof MenuItemModel) {
        MenuItemModel originalMenuItemModel = (MenuItemModel) model.get(i);
        if (originalMenuItemModel.isHeader()) {
          copyMenuItemModel =
              new MenuItemModel(originalMenuItemModel.getCommandUUID(),
                  originalMenuItemModel.getOid());
          copyMenuItemModel.setHeader(true);
          if (originalMenuItemModel.getLevel() == 0) {
            copyMenuItemModel.setSelected(true);
          }
        } else {
          copyMenuItemModel =
              new MenuItemModel((list.get(j)).getCommand().getUUID(),
                  originalMenuItemModel.getOid());
          j++;
        }
        copyMenuItemModel.setLevel(originalMenuItemModel.getLevel());
        ret.add(copyMenuItemModel);
      }
      if (model.get(i) instanceof List) {
        ret
            .add(getCopyOf((List<?>) model.get(i), copyMenuItemModel
                .getChilds()));
      }
    }

    return ret;
  }

  private void setLevel(final List<?> _list, final int _level) {
    int level = _level;
    boolean add = true;
    for (int i = 0; i < _list.size(); i++) {
      if (_list.get(i) instanceof MenuItemModel) {
        MenuItemModel itemmodel = (MenuItemModel) _list.get(i);
        if (itemmodel.isHeader() && add) {
          level++;
          add = false;
        }
        itemmodel.setLevel(level);
      }
      if (_list.get(i) instanceof List) {
        setLevel((List<?>) _list.get(i), level);
      }

    }
  }

  @SuppressWarnings("unchecked")
  private List<Object> getCompleteModelOf(final Component _child) {
    List<Object> model = null;

    if (_child instanceof MarkupContainer) {
      Iterator<?> it = ((MarkupContainer) _child).iterator();
      int i = 0;
      if (_child instanceof Rows) {
        model = (List<Object>) _child.getModelObject();
        if (model.isEmpty()) {
          model = null;
        }
      }
      while (it.hasNext()) {
        List<Object> model2 = getCompleteModelOf((Component) it.next());
        if (model2 != null) {
          if (model != null) {
            if (!model.contains(model2)) {
              model.add(i + 1, model2);
            }
          } else {
            model = model2;
          }
          if (model.isEmpty()) {
            model = null;
          }
        }
        i++;
      }
    }
    return model;
  }

  @Override
  public StyleClassName getSelectedStyleClass() {
    return StyleClassName.GOINTO_SELECTED;
  }

  @Override
  public StyleClassName getStyleClass() {
    return StyleClassName.GOINTO;
  }
}
