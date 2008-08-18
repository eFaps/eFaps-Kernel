/*
 * Copyright 2003-2008 The eFaps Team
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

import org.apache.wicket.PageMap;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.efaps.admin.ui.AbstractCommand.Target;
import org.efaps.ui.wicket.components.LabelComponent;
import org.efaps.ui.wicket.components.efapscontent.StaticImageComponent;
import org.efaps.ui.wicket.components.table.cell.AjaxLinkContainer;
import org.efaps.ui.wicket.components.table.cell.ContentContainerLink;
import org.efaps.ui.wicket.models.cell.UIFormCell;

/**
 * TODO description
 *
 * @author jmox
 * @version $Id$
 */
public class ValueCellPanel extends Panel {

  private static final long serialVersionUID = 1L;

  public ValueCellPanel(final String _wicketId, final IModel<UIFormCell> _model,
                        final boolean _ajaxLink) {
    super(_wicketId, _model);

    final UIFormCell uiFormCell = (UIFormCell) super.getDefaultModelObject();

    if (uiFormCell.getReference() == null) {
      if (uiFormCell.getIcon() == null) {
        this.add(new WebComponent("icon").setVisible(false));
      } else {
        this.add(new StaticImageComponent("icon", uiFormCell.getIcon()));
      }

      this.add(new LabelComponent("label", new Model<String>(uiFormCell.getCellValue())));
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
      link
          .add(new LabelComponent("linkLabel", new Model<String>(uiFormCell.getCellValue())));
      this.add(link);
    }

  }


}
