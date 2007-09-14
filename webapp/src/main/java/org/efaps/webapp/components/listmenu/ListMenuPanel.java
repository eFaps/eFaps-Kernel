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
import java.util.UUID;

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

  public static final ResourceReference ICON_SUBMENUGOUP =
      new ResourceReference(ListMenuPanel.class, "eFapsSubMenuGoUp.gif");

  public enum StyleClassName {
    COLLAPSE("eFapsListCollapse"),
    COLLAPSE_SELECTED("eFapsListCollapseSelected"),
    GOINTO("eFapsListMenuGoInto"),
    GOINTO_SELECTED("eFapsListMenuGoIntoSelected"),
    GOUP("eFapsListMenuGoUp"),
    GOUP_SELECTED("eFapsListMenuGoUpSelected"),
    HEADER("eFapsListMenuHeader"),
    ITEM("eFapsListMenuItem"),
    ITEM_SELECTED("eFapsListMenuItemSelected"),
    REMOVE("eFapsListMenuRemove"),
    REMOVE_SELECTED("eFapsListMenuRemoveSelected");

    public final String name;

    private StyleClassName(final String _name) {
      this.name = _name;
    }

  }

  /**
   * this Instancevariable holds the key wich is used to retrieve a item of this
   * ListMenuPanel from the Map in the Session
   * {@link #org.efaps.webapp.EFapsSession}
   */
  private final String menuKey;

  private final int padding = 10;

  private final List<Component> headerComponents = new ArrayList<Component>();

  private final int paddingAdd = 18;

  public ListMenuPanel(final String _id, final String _menukey,
                       final PageParameters _parameters,
                       final boolean _setHeaderAsSelected) {
    this(_id, _menukey, _parameters, _setHeaderAsSelected, 0);

  }

  public ListMenuPanel(final String _id, final String _menukey,
                       final PageParameters _parameters,
                       final boolean _setHeaderAsSelected, final int _level) {
    super(_id, "noTitel");
    this.menuKey = _menukey;
    setVersioned(false);
    add(HeaderContributor.forCss(getClass(), "ListMenuPanel.css"));
    try {

      MenuItemModel model =
          new MenuItemModel(UUID.fromString(_parameters.getString("command")),
              _parameters.getString("oid"));
      this.setModel(model);
      model.setLevel(_level);
      model.setHeader(true);
      if (_setHeaderAsSelected) {
        model.setSelected(true);
      }
      List<Object> menu = new ArrayList<Object>();
      menu.add(model);
      if (model.hasChilds()) {
        menu.add(model.getChilds());
        for (MenuItemModel item : model.getChilds()) {
          item.setLevel(_level);
        }
      }
      add(new Rows("rows", _menukey, menu, model));

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public ListMenuPanel(final String _id, final String _menukey,
                       final List<?> _modelObject, IModel _model) {
    super(_id, "noTitel");
    this.menuKey = _menukey;
    this.setModel(_model);
    setVersioned(false);
    add(new Rows("rows", _menukey, _modelObject, _model));

  }

  public ListMenuPanel(String _id, String _menukey) {
    super(_id, "noTitel");
    this.menuKey = _menukey;
  }

  /**
   * This is the getter method for the instance variable
   * {@link #headerComponents}.
   *
   * @return value of instance variable {@link #headerComponent}
   */

  public List<Component> getHeaderComponents() {
    return this.headerComponents;
  }

  /**
   * This is the setter method for the instance variable
   * {@link #headerComponent}.
   *
   * @param headerComponent
   *                the headerComponent to set
   */
  public void addHeaderComponent(final Component _headerComponent) {
    this.headerComponents.add(_headerComponent);
  }

  /**
   * This is the getter method for the instance variable {@link #menuKey}.
   *
   * @return value of instance variable {@link #menuKey}
   */

  public String getMenuKey() {
    return this.menuKey;
  }

  /**
   * This is the getter method for the instance variable {@link #padding}.
   *
   * @return value of instance variable {@link #padding}
   */

  public int getPadding() {
    return this.padding;
  }

  /**
   * This is the getter method for the instance variable {@link #paddingAdd}.
   *
   * @return value of instance variable {@link #paddingAdd}
   */

  public int getPaddingAdd() {
    return this.paddingAdd;
  }

  /**
   * The list class.
   */
  public class Rows extends ListView {

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

    @Override
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

        ListMenuAjaxLinkContainer link =
            new ListMenuAjaxLinkContainer("link", this.menukey, model);
        link.add(new Label("link_label", model.getLabel()));

        link.setOutputMarkupId(true);
        row.add(link);
        if (model.isHeader()) {
          String imageUrl = model.getImage();
          if (imageUrl == null) {
            imageUrl = model.getTypeImage();
          }
          if (imageUrl != null) {
            link
                .add(new StaticImageComponent("link_icon", new Model(imageUrl)));
          } else {
            link.add(new WebMarkupContainer("link_icon").setVisible(false));
          }
          if (model.getAncestor() != null) {
            AjaxGoUpLink goup = new AjaxGoUpLink("gouplink", model);
            row.add(goup);
            goup.add(
            new Image("gouplink_icon", ICON_SUBMENUGOUP));
          } else {
            row.add(new WebMarkupContainer("gouplink").setVisible(false));


          }
        } else {
          link.add(new WebMarkupContainer("link_icon").setVisible(false));
          row.add(new WebMarkupContainer("gouplink").setVisible(false));
        }

        if (model.isHeader() && this.findParent(ListItem.class) != null) {
          AjaxRemoveLink remove = new AjaxRemoveLink("removelink", model);
          remove.add(new Image("removelink_icon", ICON_SUBMENUREMOVE));
          row.add(remove);
          AjaxGoIntoLink gointo = new AjaxGoIntoLink("gointolink", model);
          gointo.add(new Image("gointolink_icon", ICON_SUBMENUGOINTO));
          row.add(gointo);
          AjaxCollapseLink collapse =
              new AjaxCollapseLink("collapselink", model);
          collapse.add(new Image("collapselink_icon", ICON_SUBMENUCLOSE));
          row.add(collapse);

        } else {
          row.add(new WebMarkupContainer("removelink").setVisible(false));
          row.add(new WebMarkupContainer("gointolink").setVisible(false));
          row.add(new WebMarkupContainer("collapselink").setVisible(false));
        }
        _listItem.add(row);
      }

    }
  }

}
