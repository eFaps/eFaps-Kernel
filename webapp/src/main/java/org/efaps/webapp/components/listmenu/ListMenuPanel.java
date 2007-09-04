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

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.Session;
import org.apache.wicket.behavior.HeaderContributor;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;
import org.wicketstuff.dojo.markup.html.container.DojoPanelContainer;

import org.efaps.webapp.EFapsSession;
import org.efaps.webapp.components.StaticImageComponent;
import org.efaps.webapp.models.MenuItemModel;

/**
 * @author jmo
 * @version $Id$
 */
public class ListMenuPanel extends DojoPanelContainer {

  private static final long serialVersionUID = 1L;

  /**
   * this Instancevariable holds the key wich is used to retrieve a item of this
   * ListMenuPanel from the Map in the Session
   * {@link #org.efaps.webapp.EFapsSession}
   */
  private final String menukey;

  private Component header;

  public ListMenuPanel(final String _id, final String _menukey,
                       final PageParameters _parameters) {
    this(_id, _menukey, _parameters, 0);
  }

  public ListMenuPanel(final String _id, final String _menukey,
                       final PageParameters _parameters, final int _level) {
    super(_id, "noTitel");
    this.menukey = _menukey;
    setVersioned(false);
    add(HeaderContributor.forCss(getClass(), "ListMenuPanel.css"));
    try {

      MenuItemModel model =
          new MenuItemModel(_parameters.getString("command"), _parameters
              .getString("oid"));
      this.setModel(model);
      model.setLevel(_level);
      List<Object> menu = new ArrayList<Object>();
      menu.add(model);
      menu.add(model.getChilds());

      for (MenuItemModel item : model.getChilds()) {
        item.setLevel(_level);
      }
      add(new Rows("rows", _menukey, menu));

      if (_level > 0) {
        Component comp =
            ((EFapsSession) (Session.get())).getSelectedComponent(_menukey);
        Rows row = (Rows) comp.findParent(Rows.class);

        List<Object> list = row.getList();
        final int index = list.indexOf(comp.getModel());
        list.add(index + 1, menu);
      }

    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  public String getMenuKey() {
    return this.menukey;
  }

  public ListMenuPanel(final String _id, final String _menukey,
                       final List<?> _modelObject) {
    super(_id, "noTitel");
    this.menukey = _menukey;
    setVersioned(false);
    add(new Rows("rows", _menukey, _modelObject));

  }

  public ListMenuPanel(String _id, String _menukey) {
    super(_id, "noTitel");
    this.menukey = _menukey;
  }

  /**
   * The list class.
   */
  public static class Rows extends ListView {

    private static final long serialVersionUID = 1L;

    private final String menukey;

    public Rows(String id, String _menukey, List<?> childs) {
      super(id, childs);
      this.menukey = _menukey;
      setReuseItems(true);
    }

    protected void populateItem(final ListItem _listItem) {
      Object modelObject = _listItem.getModelObject();

      if (modelObject instanceof List) {
        // create a panel that renders the sub lis
        ListMenuPanel nested =
            new ListMenuPanel("nested", this.menukey, (List<?>) modelObject);
        _listItem.add(nested);
        // if the current element is a list, we create a dummy row/
        // label element
        // as we have to confirm to our HTML definition, and set it's
        // visibility
        // property to false as we do not want LI tags to be rendered.
        WebMarkupContainer row = new WebMarkupContainer("row");
        row.setVisible(false);
        row.setOutputMarkupPlaceholderTag(true);
        _listItem.add(row);
      } else {
        // if the current element is not a list, we create a dummy panel
        // element
        // to confirm to our HTML definition, and set this panel's
        // visibility
        // property to false to avoid having the UL tag rendered
        ListMenuPanel nested = new ListMenuPanel("nested", this.menukey);
        nested.setVisible(false);
        nested.setOutputMarkupPlaceholderTag(true);
        _listItem.add(nested);
        // add the row (with the LI element attached, and the label with
        // the
        // row's actual value to display
        WebMarkupContainer row = new WebMarkupContainer("row");
        MenuItemModel model = (MenuItemModel) modelObject;

        ListMenuLinkComponent link =
            new ListMenuLinkComponent("link", this.menukey, model);
        link.add(new Label("label", model.getLabel()));

        link.setOutputMarkupId(true);
        row.add(link);
        if (model.hasChilds()) {
          String imageUrl = model.getImage();
          if (imageUrl == null) {
            imageUrl = model.getTypeImage();
          }
          if (imageUrl != null) {
            link.add(new StaticImageComponent("icon", new Model(imageUrl)));
          } else {
            link.add(new WebMarkupContainer("icon").setVisible(false));
          }
        } else {
          link.add(new WebMarkupContainer("icon").setVisible(false));
        }

        if (model.hasChilds() && this.findParent(ListItem.class) != null) {
          row.add(new ListMenuRemoveLinkComponent("removelink", model));
          row.add(new ListMenuGoIntoLinkComponent("gointolink", model));
        } else {
          row.add(new WebMarkupContainer("removelink").setVisible(false));
          row.add(new WebMarkupContainer("gointolink").setVisible(false));
        }
        _listItem.add(row);
      }

    }
  }

  public void setHeaderComponent(Component _header) {
    this.header = _header;

  }

  public Component getHeaderComponent() {
    return this.header;
  }

}
