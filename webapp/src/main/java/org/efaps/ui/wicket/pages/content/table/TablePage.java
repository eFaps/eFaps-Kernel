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

package org.efaps.ui.wicket.pages.content.table;

import org.apache.wicket.IPageMap;
import org.apache.wicket.PageParameters;
import org.apache.wicket.behavior.HeaderContributor;
import org.apache.wicket.model.IModel;

import org.efaps.ui.wicket.components.FormContainer;
import org.efaps.ui.wicket.components.table.TablePanel;
import org.efaps.ui.wicket.components.table.header.HeaderPanel;
import org.efaps.ui.wicket.models.TableModel;
import org.efaps.ui.wicket.pages.content.AbstractContentPage;
import org.efaps.ui.wicket.resources.CSSResourceReference;

/**
 * @author jmox
 * @version $Id:TablePage.java 1491 2007-10-15 23:40:43Z jmox $
 */
public class TablePage extends AbstractContentPage {

  private static final long serialVersionUID = 7564911406648729094L;

  private static final CSSResourceReference CSS =
      new CSSResourceReference(TablePage.class, "TablePage.css");

  public TablePage(final PageParameters _parameters) {
    this(new TableModel(_parameters));
  }

  public TablePage(final IModel _model) {
    super(_model);
    this.addComponents();
  }

  public TablePage(final PageParameters _parameters, final IPageMap _pagemap) {
    this(new TableModel(_parameters), _pagemap);
  }

  public TablePage(final IModel _model, final IPageMap _pagemap) {
    super(_model, null, _pagemap);
    this.addComponents();
  }

  protected void addComponents() {
    this.add(HeaderContributor.forCss(CSS));
    final TableModel model = (TableModel) super.getModel();
    if (!model.isInitialised()) {
      model.execute();
    }
    final TablePanel tablebody = new TablePanel("tablebody", model, this);
    this.add(new HeaderPanel("header", tablebody));

    final FormContainer form = new FormContainer("form");
    this.add(form);
    super.addComponents(form);

    form.add(tablebody);

  }
}
