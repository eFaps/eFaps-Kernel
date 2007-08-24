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

package org.efaps.webapp.components.table.header;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;

import org.efaps.admin.datamodel.ui.FieldDefinition;
import org.efaps.webapp.models.TableModel;

/**
 * @author jmo
 * @version $Id$
 */
public class TableHeaderPanel extends Panel {

  private static final long serialVersionUID = 1L;

  public TableHeaderPanel(final String _id, final IModel _model) {
    super(_id, _model);
    TableModel model = (TableModel) super.getModel();
    if (!model.isInitialised()) {
      try {
        model.execute();
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    WebMarkupContainer checkbox = new WebMarkupContainer("checkbox");
    add(checkbox);
    if (!model.isShowCheckBoxes()) {
      checkbox.setVisible(false);
    }

    RepeatingView repeating = new RepeatingView("repeating");
    add(repeating);

    for (FieldDefinition fielddef : model.getFieldDefs()) {
      WebMarkupContainer item = new WebMarkupContainer(repeating.newChildId());
      repeating.add(item);

      item.add(new Label("label", fielddef.getLabel()));
    }
  }
}
