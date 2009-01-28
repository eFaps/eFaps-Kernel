/*
 * Copyright 2003 - 2009 The eFaps Team
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

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import org.efaps.admin.ui.AbstractCommand.SortDirection;
import org.efaps.ui.wicket.behaviors.dojo.DnDBehavior;
import org.efaps.ui.wicket.models.objects.UITable;
import org.efaps.ui.wicket.models.objects.UITableHeader;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.resources.StaticHeaderContributor;

/**
 * This class renders the Cells inside a Header, providing all necessary Links
 *
 * @author jmox
 * @version $Id$
 */
public class HeaderCellPanel extends Panel {

  private static final long serialVersionUID = 1L;

  public static final EFapsContentReference ICON_FILTER =
      new EFapsContentReference(HeaderCellPanel.class, "Filter.gif");

  public static final EFapsContentReference ICON_FILTERACTIVE =
      new EFapsContentReference(HeaderCellPanel.class, "FilterActive.gif");

  public static final EFapsContentReference ICON_SORTDESC =
      new EFapsContentReference(HeaderCellPanel.class, "SortDescending.gif");

  public static final EFapsContentReference ICON_SORTASC =
      new EFapsContentReference(HeaderCellPanel.class, "SortAscending.gif");

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
   * @param _uitable
   */
  public HeaderCellPanel(final String _id, final IModel<UITableHeader> _model,
                         final UITable _uitable) {
    super(_id, _model);

    final UITableHeader uiTableHeader = (UITableHeader) super.getDefaultModelObject();

    add(StaticHeaderContributor.forCss(CSS));

    this.add(new SimpleAttributeModifier("title", uiTableHeader.getLabel()));

    this.add(new WebComponent("checkBox").setVisible(false));

    if (uiTableHeader.isSortable()) {
      final SortLink sortlink = new SortLink("sortlink", _model);

      if (uiTableHeader.getSortDirection() == SortDirection.NONE) {
        sortlink.add(new SimpleAttributeModifier("class", "eFapsHeaderSort"));
      } else if (uiTableHeader.getSortDirection() == SortDirection.ASCENDING) {
        sortlink.add(new SimpleAttributeModifier("class",
            "eFapsHeaderSortAscending"));
        sortlink.add(new SimpleAttributeModifier("style",
            " background-image: url(" + ICON_SORTASC.getImageUrl() + ");"));
      } else if (uiTableHeader.getSortDirection() == SortDirection.DESCENDING) {
        sortlink.add(new SimpleAttributeModifier("class",
            "eFapsHeaderSortDescending"));
        sortlink.add(new SimpleAttributeModifier("style",
            " background-image: url(" + ICON_SORTDESC.getImageUrl() + ");"));
      }

      if (uiTableHeader.isFilterable()) {
        sortlink
            .add(new AttributeAppender("style", new Model<String>("width:80%"), ";"));
      }
      this.add(sortlink);
      final Label sortlabel = new Label("sortlabel", uiTableHeader.getLabel());
      sortlabel.add(DnDBehavior.getHandleBehavior());
      sortlink.add(sortlabel);
      this.add(new WebComponent("label").setVisible(false));
    } else {
      this.add(new WebMarkupContainer("sortlink").setVisible(false));
      final Label label = new Label("label", uiTableHeader.getLabel());
      label.add(DnDBehavior.getHandleBehavior());
      this.add(label);
    }

    if (uiTableHeader.isFilterable()) {

      final AjaxFilterLinkContainer filterlink =
          new AjaxFilterLinkContainer("filterlink", _model);

      if (uiTableHeader.getName().equals(_uitable.getFilterKey())
          && _uitable.isFiltered()) {
        filterlink.add(new SimpleAttributeModifier("class",
            "eFapsHeaderFilterActive"));
        filterlink.add(new SimpleAttributeModifier("style",
            " background-image: url(" + ICON_FILTERACTIVE.getImageUrl() + ");"));
      } else {
        filterlink
            .add(new SimpleAttributeModifier("class", "eFapsHeaderFilter"));
        filterlink.add(new SimpleAttributeModifier("style",
            " background-image: url(" + ICON_FILTER.getImageUrl() + ");"));
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
    if (this.getDefaultModel() != null) {
      final UITableHeader headermodel = (UITableHeader) this.getDefaultModelObject();
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
