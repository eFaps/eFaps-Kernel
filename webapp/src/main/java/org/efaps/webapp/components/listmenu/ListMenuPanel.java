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

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.wicketstuff.dojo.markup.html.container.DojoPanelContainer;

import org.efaps.webapp.models.IMenuItemModel;

/**
 * @author jmo
 * @version $Id$
 */
public class ListMenuPanel extends DojoPanelContainer {

  private static final long serialVersionUID = 1L;

  public ListMenuPanel(final String _id, final PageParameters _parameters) {
    super(_id, "noTitel");
    setVersioned(false);
    // add(HeaderContributor.forCss(getClass(), "listmenu.css"));
    try {

      IMenuItemModel model =
          new IMenuItemModel(_parameters.getString("command"), _parameters
              .getString("oid"));
      this.setModel(model);

      List<Object> menu = new ArrayList<Object>();
      menu.add(model);
      menu.add(model.getChilds());
      add(new Rows("rows", menu));
      
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  public ListMenuPanel(final String _id, final List<?> _modelObject) {
    super(_id, "noTitel");
    setVersioned(false);
    add(new Rows("rows", _modelObject));
   
  }

  public ListMenuPanel(String _id) {
    super(_id, "noTitel");
  }

  /**
   * The list class.
   */
  private static class Rows extends ListView {

    private static final long serialVersionUID = 1L;

    public Rows(String id, List<?> childs) {
      super(id, childs);
    }

    protected void populateItem(final ListItem _listItem) {
      Object modelObject = _listItem.getModelObject();

      if (modelObject instanceof List) {
        // create a panel that renders the sub lis
        ListMenuPanel nested =
            new ListMenuPanel("nested", (List<?>) modelObject);
        _listItem.add(nested);
        // if the current element is a list, we create a dummy row/
        // label element
        // as we have to confirm to our HTML definition, and set it's
        // visibility
        // property to false as we do not want LI tags to be rendered.
        WebMarkupContainer row = new WebMarkupContainer("row");
        row.setVisible(false);
        row.setOutputMarkupPlaceholderTag(true);
        row.add(new WebMarkupContainer("link"));
        _listItem.add(row);
      } else {
        // if the current element is not a list, we create a dummy panel
        // element
        // to confirm to our HTML definition, and set this panel's
        // visibility
        // property to false to avoid having the UL tag rendered
        ListMenuPanel nested = new ListMenuPanel("nested");
        nested.setVisible(false);
        nested.setOutputMarkupPlaceholderTag(true);
        _listItem.add(nested);
        // add the row (with the LI element attached, and the label with
        // the
        // row's actual value to display
        WebMarkupContainer row = new WebMarkupContainer("row");
        IMenuItemModel model = (IMenuItemModel) modelObject;

        ListMenuLinkComponent x = new ListMenuLinkComponent("link", model);
        x.add(new Label("label", model.getLabel()));
        row.add(x);
        x.setOutputMarkupId(true);
        _listItem.add(row);
      }

    }
  }

}
