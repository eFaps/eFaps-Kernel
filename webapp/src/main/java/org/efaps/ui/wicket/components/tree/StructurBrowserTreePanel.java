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

package org.efaps.ui.wicket.components.tree;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.panel.Panel;

import org.efaps.ui.wicket.models.StructurBrowserModel;

/**
 * This class renders a Panel containing a StructurBrowserTree
 * 
 * @author jmox
 * @version $Id$
 */
public class StructurBrowserTreePanel extends Panel {

  private static final long serialVersionUID = 1L;

  public StructurBrowserTreePanel(final String _wicketId,
                                  final PageParameters _parameters,
                                  final String _listmenukey) {
    this(_wicketId, new StructurBrowserModel(_parameters), _listmenukey);
  }

  public StructurBrowserTreePanel(final String _wicketId,
                                  final StructurBrowserModel _model,
                                  final String _listmenukey) {
    super(_wicketId, _model);

    if (!_model.isInitialised()) {
      _model.execute();
    }

    final StructurBrowserTree tree =
        new StructurBrowserTree("tree", _model.getTreeModel(), _listmenukey);
    this.add(tree);

  }
}
