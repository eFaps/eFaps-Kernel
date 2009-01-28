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
 * Revision:        $Rev:1491 $
 * Last Changed:    $Date:2007-10-15 18:40:43 -0500 (Mon, 15 Oct 2007) $
 * Last Changed By: $Author:jmox $
 */

package org.efaps.ui.wicket.pages.content.table.filter;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.ui.wicket.components.FormContainer;
import org.efaps.ui.wicket.components.button.Button;
import org.efaps.ui.wicket.components.modalwindow.ModalWindowContainer;
import org.efaps.ui.wicket.models.objects.UITable;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.resources.StaticHeaderContributor;

/**
 * @author jmox
 * @version $Id:FilterPage.java 1491 2007-10-15 23:40:43Z jmox $
 */
public class FilterPage extends WebPage {

  private static final long serialVersionUID = 1L;

  private static final EFapsContentReference CSS =
      new EFapsContentReference(FilterPage.class, "FilterPage.css");

  private static String CHECKBOXNAME = "eFapsFilterSelection";

  public FilterPage(final IModel<UITable> _model,
                    final ModalWindowContainer _modalwindow) {
    super(_model);
    final UITable table  = (UITable)super.getDefaultModelObject();
    add(StaticHeaderContributor.forCss(CSS));

    final FormContainer form = new FormContainer("eFapsForm");
    this.add(form);

    final FilterListView checksList =
        new FilterListView("listview", table.getFilterList());

    form.add(checksList);

    final Label all = new Label("checkAll", 
                                DBProperties.getProperty("FilterPage.All"));
    form.add(all);
    
    final AjaxButton ajaxbutton = new AjaxButton(Button.LINKID, form) {

      private static final long serialVersionUID = 1L;

      @Override
      protected void onSubmit(final AjaxRequestTarget _target, final Form<?> _form) {
        final String[] selection =
            this.getRequestCycle().getRequest().getParameters(CHECKBOXNAME);

        if (selection != null) {
          if (selection.length == checksList.getViewSize()) {
            table.removeFilter();
          } else {
            table.setFilter(selection);
            table.filter();
          }
          _modalwindow.setUpdateParent(true);

        } else {
          _modalwindow.setUpdateParent(false);
        }
        _modalwindow.close(_target);

      }
    };

    form
        .add(new Button("submitButton", ajaxbutton, "send", Button.ICON_ACCEPT));

    final AjaxLink<Object> ajaxcancel = new AjaxLink<Object>(Button.LINKID) {

      private static final long serialVersionUID = 1L;

      @Override
      public void onClick(final AjaxRequestTarget _target) {
        _modalwindow.setUpdateParent(false);
        _modalwindow.close(_target);

      }
    };

    form
        .add(new Button("closeButton", ajaxcancel, "cancel", Button.ICON_CANCEL));
  }

  public class ValueCheckBox<T> extends FormComponent<T> {

    private static final long serialVersionUID = 1L;

    public ValueCheckBox(final String _id, final IModel<T> _model) {
      super(_id, _model);
    }

    @Override
    protected void onComponentTag(final ComponentTag tag) {
      super.onComponentTag(tag);
      tag.put("value", getValue());
      tag.put("name", CHECKBOXNAME);
    }
  }

  @SuppressWarnings("unchecked")
  public class FilterListView extends ListView {

    private static final long serialVersionUID = 1L;

    private boolean odd = true;

    public FilterListView(final String _id, final List<?> _list) {
      super(_id, _list);
      this.setReuseItems(true);
    }

    @Override
    protected void populateItem(final ListItem _item) {
      final WebMarkupContainer tr = new WebMarkupContainer("listview_tr");
      _item.add(tr);

      if (this.odd) {
        tr.add(new SimpleAttributeModifier("class", "eFapsTableRowOdd"));
      } else {
        tr.add(new SimpleAttributeModifier("class", "eFapsTableRowEven"));
      }

      this.odd = !this.odd;
      tr
          .add(new ValueCheckBox("listview_tr_check", new Model(_item
              .getIndex())));
      tr.add(new Label("listview_tr_label", _item.getDefaultModelObjectAsString()));
    }
  }

}
