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
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.ui.wicket.components.tree;

import java.util.UUID;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import org.efaps.ui.wicket.models.StructurBrowserModel;
import org.efaps.ui.wicket.models.objects.UIStructurBrowser;

/**
 * This class renders a Panel containing a StructurBrowserTree.
 *
 * @author jmox
 * @version $Id$
 */
public class StructurBrowserTreePanel extends Panel {

  /**
   * Needed foer serialization.
   */
  private static final long serialVersionUID = 1L;

  /**
   * Constructor.
   *
   * @param _wicketId     wicket id of this component
   * @param _commandUUID  UUID of the related command
   * @param _oid          oid
   * @param _listmenukey  key to the list menu
   */
  public StructurBrowserTreePanel(final String _wicketId,
                                  final UUID _commandUUID,
                                  final String _oid,
                                  final String _listmenukey) {
    this(_wicketId, new StructurBrowserModel(new UIStructurBrowser(
        _commandUUID, _oid)), _listmenukey);
  }

  /**
   * Constructor.
   *
   * @param _wicketId     wicket id of this component
   * @param _model        model for this component
   * @param _listmenukey  key to the list menu
   */
  public StructurBrowserTreePanel(final String _wicketId,
                                  final IModel<UIStructurBrowser> _model,
                                  final String _listmenukey) {
    super(_wicketId, _model);
    final UIStructurBrowser model = _model.getObject();
    if (!model.isInitialised()) {
      model.execute();
    }

    final StructurBrowserTree tree =
        new StructurBrowserTree("tree", model.getTreeModel(), _listmenukey);
    this.add(tree);

  }
}
