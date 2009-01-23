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
 * Revision:        $Rev:1491 $
 * Last Changed:    $Date:2007-10-15 18:40:43 -0500 (Mon, 15 Oct 2007) $
 * Last Changed By: $Author:jmox $
 */

package org.efaps.ui.wicket.pages.content.table;

import java.util.UUID;

import org.apache.wicket.IPageMap;
import org.apache.wicket.PageParameters;
import org.apache.wicket.model.IModel;

import org.efaps.ui.wicket.components.FormContainer;
import org.efaps.ui.wicket.components.table.TablePanel;
import org.efaps.ui.wicket.components.table.header.HeaderPanel;
import org.efaps.ui.wicket.models.TableModel;
import org.efaps.ui.wicket.models.objects.UITable;
import org.efaps.ui.wicket.pages.content.AbstractContentPage;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.resources.StaticHeaderContributor;

/**
 * @author jmox
 * @version $Id:TablePage.java 1491 2007-10-15 23:40:43Z jmox $
 */
public class TablePage extends AbstractContentPage {

  private static final long serialVersionUID = 7564911406648729094L;

  private static final EFapsContentReference CSS =
      new EFapsContentReference(TablePage.class, "TablePage.css");

  /**
   * Constructor called from the client directly by using parameters. Normally
   * it should only contain one parameter Opener.OPENER_PARAKEY to access the
   * opener.
   *
   * @param _parameters PageParameters
   */
  public TablePage(final PageParameters _parameters) {
    this(new TableModel(new UITable(_parameters)));
  }

  public TablePage(final IModel<UITable> _model) {
    super(_model);
    this.addComponents();
  }

  public TablePage(final IPageMap _pagemap, final UUID _uuid, final String _oid) {
    this(_pagemap, _uuid, _oid, null);
  }

  public TablePage(final IPageMap _pagemap, final UUID _uuid, final String _oid, final String _openerId) {
    this(_pagemap, new TableModel(new UITable(_uuid, _oid, _openerId)));
  }

  public TablePage(final IPageMap _pagemap, final TableModel _model) {
    super(_pagemap, _model, null);
    this.addComponents();
  }

  /**
   * @param _uuid
   * @param _oid
   */
  public TablePage(final UUID _uuid, final String _oid) {
    this(new TableModel(new UITable(_uuid, _oid)));
  }

  protected void addComponents() {
    this.add(StaticHeaderContributor.forCss(CSS));

    final UITable table = (UITable) super.getDefaultModelObject();
    if (!table.isInitialised()) {
      table.execute();
    }
    final TablePanel tablebody = new TablePanel("tablebody", new TableModel(table), this);
    this.add(new HeaderPanel("header", tablebody));

    final FormContainer form = new FormContainer("form");
    this.add(form);
    super.addComponents(form);

    form.add(tablebody);

  }
}
