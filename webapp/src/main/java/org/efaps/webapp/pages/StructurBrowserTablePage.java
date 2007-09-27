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

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.resources.StyleSheetReference;

import org.efaps.webapp.components.FormContainer;
import org.efaps.webapp.components.tree.StructurBrowserPanel;
import org.efaps.webapp.models.StructurBrowserModel;

/**
 * @author jmo
 * @version $Id$
 */
public class StructurBrowserTablePage extends ContentPage {

  private static final long serialVersionUID = 7564911406648729094L;

  public StructurBrowserTablePage(final PageParameters _parameters) {
    super(new StructurBrowserModel(_parameters));
    this.addComponents();
  }

  protected void addComponents() {

    StructurBrowserModel model = (StructurBrowserModel) super.getModel();
    if (!model.isInitialised()) {
      model.execute();
    }

    FormContainer form = new FormContainer("form");
    this.add(form);
    super.addComponents(form);

    form.add(new StructurBrowserPanel("structurBrowserTable", model));
    this.add(new StyleSheetReference("structurtablecss", getClass(),
        "structurbrowsertablepage/StructurBrowserTablePage.css"));
  }

}
