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

package org.efaps.ui.wicket.components.table.header;

import org.apache.wicket.ResourceReference;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import org.efaps.admin.ui.AbstractCommand.SortDirection;
import org.efaps.ui.wicket.behaviors.dojo.DnDBehavior;
import org.efaps.ui.wicket.components.efapscontent.EFapsContentReference;
import org.efaps.ui.wicket.components.efapscontent.StaticHeaderContributor;
import org.efaps.ui.wicket.models.HeaderModel;
import org.efaps.ui.wicket.models.TableModel;

/**
 * This class renders the Cells inside a Header, providing all necessary Links
 *
 * @author jmox
 * @version $Id$
 */
public class HeaderCellPanel extends Panel {

  private static final long serialVersionUID = 1L;

  public static final ResourceReference ICON_FILTER =
      new ResourceReference(HeaderCellPanel.class, "eFapsFilter.gif");

  public static final ResourceReference ICON_FILTERACTIVE =
      new ResourceReference(HeaderCellPanel.class, "eFapsFilterActive.gif");

  public static final ResourceReference ICON_SORTDESC =
      new ResourceReference(HeaderCellPanel.class, "eFapsSortDescending.gif");

  public static final ResourceReference ICON_SORTASC =
      new ResourceReference(HeaderCellPanel.class, "eFapsSortAscending.gif");

  public static final EFapsContentReference CSS =
    new EFapsContentReference(HeaderCellPanel.class, "HeaderCellPanel.css");

  /**
   * Constructor used to render only a CheckBoxCell
   *
   * @param _id
   */
  public HeaderCellPanel(final String _id) {
    super(_id);
    this.add(new SimpleAttributeModifier("class",
        "eFapsTableCheckBoxCell eFapsCellFixedWidth0"));
    this.add(new Checkbox("checkBox"));

    this.add(new WebMarkupContainer("sortlink").setVisible(false));
    this.add(new WebComponent("label").setVisible(false));
    this.add(new WebComponent("filterlink").setVisible(false));
  }

  /**
   * Constructor used to render a Cell for the Header with (depending on the
   * model) SortLink, Filterlink etc.
   *
   * @param _id
   * @param _model
   * @param _tablemodel
   */
  public HeaderCellPanel(final String _id, final HeaderModel _model,
                         final TableModel _tablemodel) {
    super(_id, _model);

    add(StaticHeaderContributor.forCss(CSS));

    this.add(new SimpleAttributeModifier("title", _model.getLabel()));

    this.add(new WebComponent("checkBox").setVisible(false));

    if (_model.isSortable()) {
      final SortLink sortlink = new SortLink("sortlink", _model);

      if (_model.getSortDirection() == SortDirection.NONE) {
        sortlink.add(new SimpleAttributeModifier("class", "eFapsHeaderSort"));
      } else if (_model.getSortDirection() == SortDirection.ASCENDING) {
        sortlink.add(new SimpleAttributeModifier("class",
            "eFapsHeaderSortAscending"));
        sortlink.add(new SimpleAttributeModifier("style",
            " background-image: url(" + this.urlFor(ICON_SORTASC) + ");"));
      } else if (_model.getSortDirection() == SortDirection.DESCENDING) {
        sortlink.add(new SimpleAttributeModifier("class",
            "eFapsHeaderSortDescending"));
        sortlink.add(new SimpleAttributeModifier("style",
            " background-image: url(" + this.urlFor(ICON_SORTDESC) + ");"));
      }

      if (_model.isFilterable()) {
        sortlink
            .add(new AttributeAppender("style", new Model("width:80%"), ";"));
      }
      this.add(sortlink);
      final Label sortlabel = new Label("sortlabel", _model.getLabel());
      sortlabel.add(DnDBehavior.getHandleBehavior());
      sortlink.add(sortlabel);
      this.add(new WebComponent("label").setVisible(false));
    } else {
      this.add(new WebMarkupContainer("sortlink").setVisible(false));
      final Label label = new Label("label", _model.getLabel());
      label.add(DnDBehavior.getHandleBehavior());
      this.add(label);
    }

    if (_model.isFilterable()) {

      final AjaxFilterLinkContainer filterlink =
          new AjaxFilterLinkContainer("filterlink", _model);

      if (_model.getName().equals(_tablemodel.getFilterKey())
          && _tablemodel.isFiltered()) {
        filterlink.add(new SimpleAttributeModifier("class",
            "eFapsHeaderFilterActive"));
        filterlink.add(new SimpleAttributeModifier("style",
            " background-image: url(" + this.urlFor(ICON_FILTERACTIVE) + ");"));
      } else {
        filterlink
            .add(new SimpleAttributeModifier("class", "eFapsHeaderFilter"));
        filterlink.add(new SimpleAttributeModifier("style",
            " background-image: url(" + this.urlFor(ICON_FILTER) + ");"));
      }
      this.add(filterlink);

    } else {
      this.add(new WebComponent("filterlink").setVisible(false));

    }
  }

  /*
   * (non-Javadoc)
   *
   * @see org.apache.wicket.Component#onAfterRender()
   */
  @Override
  protected void onAfterRender() {
    super.onAfterRender();
    if (this.getModel() != null) {
      final HeaderModel headermodel = (HeaderModel) this.getModel();
      headermodel.setMarkupId(this.getMarkupId());
    }
  }

  public class Checkbox extends WebComponent {

    private static final long serialVersionUID = 1L;

    public Checkbox(final String _id) {
      super(_id);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.wicket.Component#onComponentTag(org.apache.wicket.markup.ComponentTag)
     */
    @Override
    protected void onComponentTag(final ComponentTag _tag) {
      super.onComponentTag(_tag);
      _tag.put("type", "checkbox");
      _tag.put("onClick", getScript());
    }

    private String getScript() {
      return "var cb=document.getElementsByName('selectedRow');"
          + "if(!isNaN(cb.length)) {"
          + " for(var i=0;i<cb.length;i++){"
          + "   cb[i].checked=this.checked;}"
          + " }else{"
          + " cb.checked=this.checked;"
          + "}";
    }
  }
}
