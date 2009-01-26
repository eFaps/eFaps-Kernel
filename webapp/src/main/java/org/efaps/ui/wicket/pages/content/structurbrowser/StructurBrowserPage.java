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

package org.efaps.ui.wicket.pages.content.structurbrowser;

import java.util.UUID;

import org.apache.wicket.IPageMap;
import org.apache.wicket.PageParameters;
import org.apache.wicket.model.IModel;

import org.efaps.ui.wicket.components.FormContainer;
import org.efaps.ui.wicket.components.tree.StructurBrowserTreeTablePanel;
import org.efaps.ui.wicket.models.StructurBrowserModel;
import org.efaps.ui.wicket.models.objects.UIStructurBrowser;
import org.efaps.ui.wicket.pages.content.AbstractContentPage;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.resources.StaticHeaderContributor;

/**
 * Class renders a page containing a structure browser.
 *
 * @author jmox
 * @version $Id:StructurBrowserPage.java 1491 2007-10-15 23:40:43Z jmox $
 */
public class StructurBrowserPage extends AbstractContentPage {

  /**
   * Reference to the style sheet.
   */
  private static final EFapsContentReference CSS
                         = new EFapsContentReference(StructurBrowserPage.class,
                                                     "StructurBrowserPage.css");

  /**
   * Needed for serialization.
   */
  private static final long serialVersionUID = 7564911406648729094L;

  /**
   * Constructor called from the client directly by using parameters. Normally
   * it should only contain one parameter Opener.OPENER_PARAKEY to access the
   * opener.
   *
   * @param _parameters PageParameters
   */
  public StructurBrowserPage(final PageParameters _parameters) {
    this(new StructurBrowserModel(new UIStructurBrowser(_parameters)));
  }

  /**
   * @param _model model for this pager
   */
  public StructurBrowserPage(final IModel<UIStructurBrowser> _model) {
    super(_model);
    this.addComponents();
  }

  /**
   * @param _commandUUID     UUID of the calling command
   * @param _oid             oid
   */
  public StructurBrowserPage(final UUID _commandUUID, final String _oid) {
    super(new StructurBrowserModel(new UIStructurBrowser(_commandUUID, _oid)));
    this.addComponents();
  }

  /**
   * @param _pageMap      pagemap
   * @param _commandUUID  UUID of the calling command
   * @param _oid          oid
   */
  public StructurBrowserPage(final IPageMap _pageMap, final UUID _commandUUID,
                             final String _oid) {
    super(_pageMap,
          new StructurBrowserModel(new UIStructurBrowser(_commandUUID, _oid)),
          null);
    this.addComponents();
  }

  /**
   * Method to add the components to this page.
   */
  protected void addComponents() {
    add(StaticHeaderContributor.forCss(CSS));

    final UIStructurBrowser model
                            = (UIStructurBrowser) super.getDefaultModelObject();
    if (!model.isInitialised()) {
      model.execute();
    }

    final FormContainer form = new FormContainer("form");
    this.add(form);
    super.addComponents(form);

    form.add(new StructurBrowserTreeTablePanel("structurBrowserTable",
                                              new StructurBrowserModel(model)));

  }
}
