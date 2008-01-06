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
 * Revision:        $Rev:1510 $
 * Last Changed:    $Date:2007-10-18 09:35:40 -0500 (Thu, 18 Oct 2007) $
 * Last Changed By: $Author:jmox $
 */

package org.efaps.ui.wicket.components.form;

import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;

import org.efaps.ui.wicket.components.form.row.RowPanel;
import org.efaps.ui.wicket.models.FormModel;
import org.efaps.ui.wicket.models.FormModel.FormElementModel;
import org.efaps.ui.wicket.models.FormModel.FormRowModel;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.resources.StaticHeaderContributor;

/**
 * @author jmox
 * @version $Id:WebFormContainer.java 1510 2007-10-18 14:35:40Z jmox $
 */
public class FormPanel extends Panel {

  private static final long serialVersionUID = 1550111712776698728L;

  public static EFapsContentReference CSS =
      new EFapsContentReference(FormPanel.class, "FormPanel.css");

  private final Map<String, Label> requiredComponents =
      new HashMap<String, Label>();

  public FormPanel(final String _wicketId, final Page _page,
                   final FormModel _model,
                   final FormElementModel _formelementmodel) {
    super(_wicketId, _model);

    if (!_model.isInitialised()) {
      _model.execute();
    }

    add(StaticHeaderContributor.forCss(CSS));

    final RepeatingView rowRepeater = new RepeatingView("rowRepeater");
    this.add(rowRepeater);

    for (final FormRowModel rowmodel : _formelementmodel.getRowModels()) {

      final RowPanel row =
          new RowPanel(rowRepeater.newChildId(), rowmodel, _model, _page, this);
      rowRepeater.add(row);

    }

  }

  /**
   * This is the getter method for the instance variable
   * {@link #requiredComponents}.
   *
   * @return value of instance variable {@link #requiredComponents}
   */

  public Map<String, Label> getRequiredComponents() {
    return this.requiredComponents;
  }

  public void addRequiredComponent(final String _name, final Label _label) {
    this.requiredComponents.put(_name, _label);
  }

}
