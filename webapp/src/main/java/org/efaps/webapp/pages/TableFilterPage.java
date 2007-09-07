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

package org.efaps.webapp.pages;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import org.efaps.webapp.components.FormContainer;
import org.efaps.webapp.components.modalwindow.ModalWindowContainer;
import org.efaps.webapp.models.TableModel;

/**
 * @author jmo
 * @version $Id$
 */
public class TableFilterPage extends WebPage {

  private static final long serialVersionUID = 1L;

  private static String CHECKBOXNAME = "eFapsFilterSelection";

  public TableFilterPage(final TableModel _model,
                         final ModalWindowContainer _modalwindow) {
    super(_model);
    FormContainer form = new FormContainer("eFapsForm");
    this.add(form);

    final ListView checksList =
        new ListView("listview", _model.getFilterList()) {

          private static final long serialVersionUID = 1L;

          @Override
          protected void populateItem(final ListItem _item) {

            _item.add(new ValueCheckBox("check", new Model(_item.getIndex())));
            _item.add(new Label("label", _item.getModelObjectAsString()));

          }
        }.setReuseItems(true);

    form.add(checksList);

    AjaxButton ajaxbutton = new AjaxButton("submit", form) {

      private static final long serialVersionUID = 1L;

      @Override
      protected void onSubmit(AjaxRequestTarget _target, Form arg1) {
        String[] selection =
            this.getRequestCycle().getRequest().getParameters(CHECKBOXNAME);

        if (selection != null) {
          if (selection.length == checksList.getViewSize()) {
            _model.removeFilter();
          } else {
            _model.setFilter(selection);
            _model.filter();
          }
          _modalwindow.setUpdateParent(true);

        } else {
          _modalwindow.setUpdateParent(false);
        }
        _modalwindow.close(_target);

      }
    };
    form.add(ajaxbutton);
    ajaxbutton.add(new Label("submitlabel", new Model("send")));
    AjaxLink ajaxcancel = new AjaxLink("cancel") {

      private static final long serialVersionUID = 1L;

      @Override
      public void onClick(final AjaxRequestTarget _target) {
        _modalwindow.setUpdateParent(false);
        _modalwindow.close(_target);

      }
    };
    form.add(ajaxcancel);
    ajaxcancel.add(new Label("cancellabel", new Model("cancel")));
  }

  public class ValueCheckBox extends FormComponent {

    private static final long serialVersionUID = 1L;

    public ValueCheckBox(final String _id, final IModel _model) {
      super(_id, _model);
    }

    @Override
    protected void onComponentTag(ComponentTag tag) {
      super.onComponentTag(tag);
      tag.put("value", getValue());
      tag.put("name", CHECKBOXNAME);
    }
  }
}
