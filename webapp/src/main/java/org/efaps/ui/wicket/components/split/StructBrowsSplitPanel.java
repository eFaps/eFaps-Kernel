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
 * Revision:        $Rev:1510 $
 * Last Changed:    $Date:2007-10-18 09:35:40 -0500 (Thu, 18 Oct 2007) $
 * Last Changed By: $Author:jmox $
 */

package org.efaps.ui.wicket.components.split;

import java.util.UUID;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;

import org.efaps.ui.wicket.behaviors.dojo.ContentPaneBehavior;
import org.efaps.ui.wicket.behaviors.dojo.SplitContainerBehavior;
import org.efaps.ui.wicket.behaviors.dojo.SplitContainerBehavior.Orientation;
import org.efaps.ui.wicket.components.menutree.MenuTree;
import org.efaps.ui.wicket.components.tree.StructurBrowserTreePanel;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.resources.StaticHeaderContributor;

/**
 * @author jmox
 * @version $Id:StructBrowsSplitPanel.java 1510 2007-10-18 14:35:40Z jmox $
 */
public class StructBrowsSplitPanel extends Panel {

  /**
   * Reference to the stylesheet.
   */
  public static final EFapsContentReference CSS
                        = new EFapsContentReference(StructBrowsSplitPanel.class,
                                                   "StructBrowsSplitPanel.css");

  /**
   * Needed for serialization.
   */
  private static final long serialVersionUID = 1L;

  /**
   * Constructor.
   *
   * @param _wicketId     wicket id of this component
   * @param _commandUUID  UUID of the related command
   * @param _oid          oid
   * @param _listMenuKey  key to the list menu
   */
  public StructBrowsSplitPanel(final String _wicketId,
                               final UUID _commandUUID,
                               final String _oid,
                               final String _listMenuKey) {
    super(_wicketId);

    this.add(StaticHeaderContributor.forCss(CSS));

    final SplitContainerBehavior beh = new SplitContainerBehavior();
    beh.setOrientation(Orientation.VERTICAL);
    this.add(beh);

    final SplitHeaderPanel header = new SplitHeaderPanel("header", true);
    this.add(header);

    final WebMarkupContainer top = new WebMarkupContainer("top");
    top.add(new ContentPaneBehavior(50, 20));
    this.add(top);

    final StructurBrowserTreePanel stuctbrows
                                    = new StructurBrowserTreePanel("stuctbrows",
                                          _commandUUID,
                                          _oid,
                                          _listMenuKey);
    stuctbrows.setOutputMarkupId(true);
    top.add(stuctbrows);
    header.addHideComponent(stuctbrows);

    final WebMarkupContainer bottom = new WebMarkupContainer("bottom");
    bottom.add(new ContentPaneBehavior(50, 20));
    this.add(bottom);
    header.addHideComponent(bottom);

    final WebMarkupContainer menuact = new WebMarkupContainer("menuact");
    menuact.setOutputMarkupId(true);
    bottom.add(menuact);
    menuact.add(new MenuTree("menu", _commandUUID, _oid, _listMenuKey));

  }
}
