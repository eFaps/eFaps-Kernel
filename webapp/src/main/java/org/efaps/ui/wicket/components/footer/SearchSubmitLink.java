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

import org.efaps.ui.wicket.models.AbstractModel;
import org.efaps.ui.wicket.models.TableModel;
import org.efaps.ui.wicket.pages.content.table.TablePage;

/**
 * Link used to submit a Search
 *
 * @author jmox
 * @version $Id$
 */
public class SearchSubmitLink extends SubmitLink {

  private static final long serialVersionUID = 1L;

  public SearchSubmitLink(final String _id, final AbstractModel _model,
                          final Form _form) {
    super(_id, _form);
    super.setModel(_model);
  }

  @Override
  public void onSubmit() {
    super.onSubmit();
    final AbstractModel model = (AbstractModel) super.getModel();

    final PageParameters parameters = new PageParameters();
    parameters.add("command", model.getCommand().getUUID().toString());
    parameters.add("oid", model.getOid());

    final TableModel newmodel = new TableModel(parameters);
    if (model.isSubmit()) {
      newmodel.setSubmit(true);
      newmodel.setCallingCommandUUID(model.getCallingCommandUUID());
    }

    final TablePage page = new TablePage(newmodel);

    this.getRequestCycle().setResponsePage(page);

  }
}
