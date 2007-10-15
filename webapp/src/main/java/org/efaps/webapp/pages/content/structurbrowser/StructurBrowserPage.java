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

package org.efaps.webapp.pages.content.structurbrowser;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.resources.StyleSheetReference;

import org.efaps.webapp.components.FormContainer;
import org.efaps.webapp.components.tree.StructurBrowserTreeTablePanel;
import org.efaps.webapp.models.StructurBrowserModel;
import org.efaps.webapp.pages.content.AbstractContentPage;

/**
 * @author jmox
 * @version $Id$
 */
public class StructurBrowserPage extends AbstractContentPage {

  private static final long serialVersionUID = 7564911406648729094L;

  public StructurBrowserPage(final PageParameters _parameters) {
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

    form.add(new StructurBrowserTreeTablePanel("structurBrowserTable", model));
    this.add(new StyleSheetReference("structurtablecss", getClass(),
        "StructurBrowserPage.css"));
  }

}
