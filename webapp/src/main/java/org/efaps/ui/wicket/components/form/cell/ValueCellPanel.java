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

package org.efaps.ui.wicket.components.form.cell;

import java.util.Date;

import org.apache.wicket.PageMap;
import org.apache.wicket.datetime.StyleDateConverter;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.joda.time.DateTime;

import org.efaps.admin.ui.AbstractCommand.Target;
import org.efaps.ui.wicket.components.LabelComponent;
import org.efaps.ui.wicket.components.efapscontent.StaticImageComponent;
import org.efaps.ui.wicket.components.form.AutoCompleteField;
import org.efaps.ui.wicket.components.form.DateFieldWithPicker;
import org.efaps.ui.wicket.components.form.FormPanel;
import org.efaps.ui.wicket.components.form.valuepicker.Value4Picker;
import org.efaps.ui.wicket.components.form.valuepicker.ValuePicker;
import org.efaps.ui.wicket.components.table.cell.AjaxLinkContainer;
import org.efaps.ui.wicket.components.table.cell.ContentContainerLink;
import org.efaps.ui.wicket.models.cell.UIFormCell;
import org.efaps.ui.wicket.models.objects.UIForm;

/**
 * Class renders a cell in a Form.
 *
 * @author jmox
 * @version $Id$
 */
public class ValueCellPanel extends Panel {

  /**
   * Needed for serialization.
   */
  private static final long serialVersionUID = 1L;

  /**
   * Instance field needed in case that a field needs a datefield.
   */
  private DateFieldWithPicker dateTextField = null;


  /**
   * Constructor.
   *
   * @param _wicketId     wicket id of the component
   * @param _model        model of the component
   * @param _formmodel    model of the form
   * @param _ajaxLink     is this panel an ajax link
   */
  public ValueCellPanel(final String _wicketId, final IModel<UIFormCell> _model,
                        final UIForm _formmodel, final boolean _ajaxLink) {
    super(_wicketId, _model);
    setOutputMarkupId(true);
    final UIFormCell uiFormCell = (UIFormCell) super.getDefaultModelObject();
    // if we don't have a reference or we are inside a modal window
    if (uiFormCell.getReference() == null
              || _formmodel.getTarget().equals(Target.MODAL)) {
      if (uiFormCell.getIcon() == null) {
        this.add(new WebComponent("icon").setVisible(false));
      } else {
        this.add(new StaticImageComponent("icon", uiFormCell.getIcon()));
      }
      // in case of create or edit for a Date
      if ((_formmodel.isCreateMode() || _formmodel.isEditMode())
          && "Date".equals(uiFormCell.getTypeName())) {

        final Date date = (uiFormCell.getCompareValue() == null
                        || !(uiFormCell.getCompareValue() instanceof DateTime))
                           ? new Date()
                           : ((DateTime) uiFormCell.getCompareValue()).toDate();

        this.dateTextField = new DateFieldWithPicker("label",
                                                  new Model<Date>(date),
                                                  new StyleDateConverter(false),
                                                  uiFormCell.getName());

        this.add(this.dateTextField);

      } else {
        if (uiFormCell.isAutoComplete()
            && (_formmodel.isCreateMode() || _formmodel.isSearchMode())) {
          this.add(new AutoCompleteField("label", _model));
        } else {
          if (uiFormCell.isValuePicker() && uiFormCell.render()) {
           final Value4Picker value = new Value4Picker("label", _model);
           value.setValue(_formmodel.isSearchMode() ? "*" : "0",
                          _formmodel.isSearchMode() ? "*" : "0");
           this.add(value);
           this.add(new ValuePicker("valuePicker", _model, value));

          } else {
            this.add(new LabelComponent("label",
                new Model<String>(uiFormCell.getCellValue()))
                                                     .setOutputMarkupId(true));

            this.add(new WebComponent("valuePicker").setVisible(false));
          }
        }
      }
      this.add(new WebMarkupContainer("link").setVisible(false));

    } else {
      this.add(new WebComponent("icon").setVisible(false));
      this.add(new WebComponent("label").setVisible(false));
      this.add(new WebComponent("valuePicker").setVisible(false));

      WebMarkupContainer link;
      if (_ajaxLink && uiFormCell.getTarget() != Target.POPUP) {
        link = new AjaxLinkContainer("link", _model);
      } else {
        link = new ContentContainerLink<UIFormCell>("link", _model);
        if (uiFormCell.getTarget() == Target.POPUP) {
          final PopupSettings popup =
              new PopupSettings(PageMap.forName("popup"));
         ((ContentContainerLink<?>) link).setPopupSettings(popup);
        }
      }
      if (uiFormCell.getIcon() == null) {
        link.add(new WebComponent("linkIcon").setVisible(false));
      } else {
        link.add(new StaticImageComponent("linkIcon", uiFormCell.getIcon()));
      }
      link.add(new LabelComponent("linkLabel",
                                 new Model<String>(uiFormCell.getCellValue())));
      this.add(link);
    }
  }

  /**
   * After rendering the datefields are added to the parent.
   */
  @Override
  protected void onAfterRender() {
    super.onAfterRender();
    if (this.dateTextField != null) {
      final FormPanel formpanel = this.findParent(FormPanel.class);
      formpanel.addDateComponent(this.dateTextField);
    }
  }
}
