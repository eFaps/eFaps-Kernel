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
import org.apache.wicket.ResourceReference;
import org.apache.wicket.behavior.HeaderContributor;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.wicketstuff.dojo.markup.html.container.DojoPanelContainer;

import org.efaps.webapp.components.StaticImageComponent;
import org.efaps.webapp.models.MenuItemModel;

/**
 * @author jmo
 * @version $Id$
 */
public class ListMenuPanel extends DojoPanelContainer {

  private static final long serialVersionUID = 1L;

  public static final ResourceReference ICON_SUBMENUREMOVE =
      new ResourceReference(ListMenuPanel.class, "eFapsSubMenuRemove.gif");

  public static final ResourceReference ICON_SUBMENUGOINTO =
      new ResourceReference(ListMenuPanel.class, "eFapsSubMenuGoInto.gif");

  public static final ResourceReference ICON_SUBMENUOPEN =
      new ResourceReference(ListMenuPanel.class, "eFapsSubMenuOpen.gif");

  public static final ResourceReference ICON_SUBMENUCLOSE =
      new ResourceReference(ListMenuPanel.class, "eFapsSubMenuClose.gif");

  /**
   * this Instancevariable holds the key wich is used to retrieve a item of this
   * ListMenuPanel from the Map in the Session
   * {@link #org.efaps.webapp.EFapsSession}
   */
  private final String menukey;

  private int padding = 10;

  private Component header;

  private int paddingAdd = 18;

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
      add(new Rows("rows", _menukey, menu, model));

    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public String getMenuKey() {
    return this.menukey;
  }

  public int getPadding() {
    return this.padding;
  }

  public int getPaddingAdd() {
    return this.paddingAdd;
  }

  public ListMenuPanel(final String _id, final String _menukey,
                       final List<?> _modelObject, IModel _model) {
    super(_id, "noTitel");
    this.menukey = _menukey;
    this.setModel(_model);
    setVersioned(false);
    add(new Rows("rows", _menukey, _modelObject, _model));

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

    private final IModel model;

    public Rows(final String id, final String _menukey, final List<?> childs,
                final IModel _model) {
      super(id, childs);
      this.menukey = _menukey;
      this.model = _model;
      setReuseItems(true);
    }

    protected void populateItem(final ListItem _listItem) {
      Object modelObject = _listItem.getModelObject();

      if (modelObject instanceof List) {
        // create a panel that renders the sub lis
        ListMenuPanel nested =
            new ListMenuPanel("nested", this.menukey, (List<?>) modelObject,
                this.model);
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
          if (model.ancestor != null) {
            row.add(new ListMenuGoUpLinkComponent("gouplink", model));
          } else {
            row.add(new WebMarkupContainer("gouplink").setVisible(false));
          }
        } else {
          link.add(new WebMarkupContainer("icon").setVisible(false));
          row.add(new WebMarkupContainer("gouplink").setVisible(false));
        }

        if (model.hasChilds() && this.findParent(ListItem.class) != null) {
          ListMenuRemoveLinkComponent remove =
              new ListMenuRemoveLinkComponent("removelink", model);
          remove.add(new Image("iconremove", ICON_SUBMENUREMOVE));
          row.add(remove);
          ListMenuGoIntoLinkComponent gointo =
              new ListMenuGoIntoLinkComponent("gointolink", model);
          gointo.add(new Image("icongointo", ICON_SUBMENUGOINTO));
          row.add(gointo);
          ListMenuCollapseLinkComponent collapse =
              new ListMenuCollapseLinkComponent("collapse", model);
          collapse.add(new Image("iconcollapse", ICON_SUBMENUCLOSE));
          row.add(collapse);

        } else {
          row.add(new WebMarkupContainer("removelink").setVisible(false));
          row.add(new WebMarkupContainer("gointolink").setVisible(false));
          row.add(new WebMarkupContainer("collapse").setVisible(false));
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
