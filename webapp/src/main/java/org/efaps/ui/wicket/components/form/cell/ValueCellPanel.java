/*
 * Copyright 2003-2009 The eFaps Team
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
import org.efaps.ui.wicket.components.form.DateFieldWithPicker;
import org.efaps.ui.wicket.components.form.FormPanel;
import org.efaps.ui.wicket.components.table.cell.AjaxLinkContainer;
import org.efaps.ui.wicket.components.table.cell.ContentContainerLink;
import org.efaps.ui.wicket.models.cell.UIFormCell;
import org.efaps.ui.wicket.models.objects.UIForm;

/**
 * TODO description
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


  public ValueCellPanel(final String _wicketId, final IModel<UIFormCell> _model,
                        final UIForm _formmodel, final boolean _ajaxLink) {
    super(_wicketId, _model);

    final UIFormCell uiFormCell = (UIFormCell) super.getDefaultModelObject();

    if (uiFormCell.getReference() == null) {
      if (uiFormCell.getIcon() == null) {
        this.add(new WebComponent("icon").setVisible(false));
      } else {
        this.add(new StaticImageComponent("icon", uiFormCell.getIcon()));
      }
      // in case of create or edit for a Date
      if ((_formmodel.isCreateMode() || _formmodel.isEditMode())
          && "Date".equals(uiFormCell.getTypeName())) {

        final Date date = (_formmodel.isCreateMode()
                            || uiFormCell.getOrigValue() == null)
                    ? new Date()
                    : ((DateTime) uiFormCell.getOrigValue()).toDate();

        this.dateTextField = new DateFieldWithPicker("label",
                                                  new Model<Date>(date),
                                                  new StyleDateConverter(false),
                                                  uiFormCell.getName());

        this.add(this.dateTextField);

      } else {
        this.add(new LabelComponent("label",
                                 new Model<String>(uiFormCell.getCellValue())));
      }
      this.add(new WebMarkupContainer("link").setVisible(false));

    } else {
      this.add(new WebComponent("icon").setVisible(false));
      this.add(new WebComponent("label").setVisible(false));

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

  @Override
  protected void onAfterRender() {
    super.onAfterRender();
    if (this.dateTextField != null) {
      final FormPanel formpanel = this.findParent(FormPanel.class);
      formpanel.addDateComponent(this.dateTextField);
    }
  }
}
