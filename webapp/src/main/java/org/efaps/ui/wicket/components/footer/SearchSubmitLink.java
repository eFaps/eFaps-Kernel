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

package org.efaps.ui.wicket.components.footer;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.model.IModel;

import org.efaps.ui.wicket.models.TableModel;
import org.efaps.ui.wicket.models.objects.AbstractUIObject;
import org.efaps.ui.wicket.models.objects.UITable;
import org.efaps.ui.wicket.pages.content.table.TablePage;

/**
 * Link used to submit a Search
 *
 * @author jmox
 * @version $Id$
 */
public class SearchSubmitLink extends SubmitLink {

  private static final long serialVersionUID = 1L;

  public SearchSubmitLink(final String _id, final IModel _model,
                          final Form<?> _form) {
    super(_id, _form);
    super.setDefaultModel(_model);
  }

  @Override
  public void onSubmit() {
    super.onSubmit();
    final AbstractUIObject uiObject = (AbstractUIObject) super.getDefaultModelObject();

    final PageParameters parameters = new PageParameters();
    parameters.add("command", uiObject.getCommand().getUUID().toString());
    parameters.add("oid", uiObject.getOid());

    final UITable newTable = new UITable(parameters);
    if (uiObject.isSubmit()) {
      newTable.setSubmit(true);
      newTable.setCallingCommandUUID(uiObject.getCallingCommandUUID());
    }

    final TablePage page = new TablePage(new TableModel(newTable));

    this.getRequestCycle().setResponsePage(page);

  }
}
