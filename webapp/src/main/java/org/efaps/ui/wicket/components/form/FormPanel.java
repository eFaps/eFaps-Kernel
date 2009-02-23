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
 * Revision:        $Rev:1510 $
 * Last Changed:    $Date:2007-10-18 09:35:40 -0500 (Thu, 18 Oct 2007) $
 * Last Changed By: $Author:jmox $
 */

package org.efaps.ui.wicket.components.form;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;

import org.efaps.ui.wicket.components.FormContainer;
import org.efaps.ui.wicket.components.form.row.RowPanel;
import org.efaps.ui.wicket.models.FormRowModel;
import org.efaps.ui.wicket.models.objects.UIForm;
import org.efaps.ui.wicket.models.objects.UIForm.FormElement;
import org.efaps.ui.wicket.models.objects.UIForm.FormRow;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.resources.StaticHeaderContributor;


/**
 * TODO description
 *
 * @author jmox
 * @version $Id$
 *
 */
public class FormPanel extends Panel {

  private static final long serialVersionUID = 1550111712776698728L;

  public static EFapsContentReference CSS =
      new EFapsContentReference(FormPanel.class, "FormPanel.css");

  public static final EFapsContentReference FILEINPUT =
      new EFapsContentReference(FormPanel.class, "EFapsFileInput.js");

  private final Map<String, Label> requiredComponents =
      new HashMap<String, Label>();

  private final Set<DateFieldWithPicker> dateComponents
                                      = new HashSet<DateFieldWithPicker>();

  public FormPanel(final String _wicketId, final Page _page,
                   final IModel<UIForm> _model,
                   final FormElement _formelementmodel,
                   final FormContainer _form) {
    super(_wicketId, _model);
    final UIForm uiForm = _model.getObject();
    if (!uiForm.isInitialised()) {
      uiForm.execute();
    }

    add(StaticHeaderContributor.forCss(CSS));
    if (uiForm.isFileUpload()) {
      add(StaticHeaderContributor.forJavaScript(FILEINPUT));
    }
    final RepeatingView rowRepeater = new RepeatingView("rowRepeater");
    this.add(rowRepeater);

    for (final FormRow rowmodel : _formelementmodel.getRowModels()) {

      final RowPanel row = new RowPanel(rowRepeater.newChildId(),
                                        new FormRowModel(rowmodel),
                                        uiForm,
                                        _page,
                                        this,
                                        _form);
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

  public void addDateComponent(final DateFieldWithPicker _datePicker) {
    this.dateComponents.add(_datePicker);
  }

  public Set<DateFieldWithPicker> getDateComponents() {
    return this.dateComponents;
  }

}
