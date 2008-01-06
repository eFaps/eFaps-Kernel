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
 * Revision:        $Rev:1491 $
 * Last Changed:    $Date:2007-10-15 18:40:43 -0500 (Mon, 15 Oct 2007) $
 * Last Changed By: $Author:jmox $
 */

package org.efaps.ui.wicket.pages.content.structurbrowser;

import org.apache.wicket.PageParameters;
import org.efaps.ui.wicket.components.FormContainer;
import org.efaps.ui.wicket.components.tree.StructurBrowserTreeTablePanel;
import org.efaps.ui.wicket.models.StructurBrowserModel;
import org.efaps.ui.wicket.pages.content.AbstractContentPage;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.resources.StaticHeaderContributor;

/**
 * @author jmox
 * @version $Id:StructurBrowserPage.java 1491 2007-10-15 23:40:43Z jmox $
 */
public class StructurBrowserPage extends AbstractContentPage {

  private static final long serialVersionUID = 7564911406648729094L;

  private static final EFapsContentReference CSS =
      new EFapsContentReference(StructurBrowserPage.class,
          "StructurBrowserPage.css");

  public StructurBrowserPage(final PageParameters _parameters) {
    super(new StructurBrowserModel(_parameters));
    this.addComponents();
  }

  protected void addComponents() {
    add(StaticHeaderContributor.forCss(CSS));

    final StructurBrowserModel model = (StructurBrowserModel) super.getModel();
    if (!model.isInitialised()) {
      model.execute();
    }

    final FormContainer form = new FormContainer("form");
    this.add(form);
    super.addComponents(form);

    form.add(new StructurBrowserTreeTablePanel("structurBrowserTable", model));

  }

}
