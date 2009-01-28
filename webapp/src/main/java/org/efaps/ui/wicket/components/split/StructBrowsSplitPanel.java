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

import org.efaps.db.Context;
import org.efaps.ui.wicket.behaviors.dojo.BorderBehavior;
import org.efaps.ui.wicket.behaviors.dojo.ContentPaneBehavior;
import org.efaps.ui.wicket.behaviors.dojo.BorderBehavior.Design;
import org.efaps.ui.wicket.behaviors.dojo.ContentPaneBehavior.Region;
import org.efaps.ui.wicket.components.menutree.MenuTree;
import org.efaps.ui.wicket.components.split.header.SplitHeaderPanel;
import org.efaps.ui.wicket.components.split.header.SplitHeaderPanel.PositionUserAttribute;
import org.efaps.ui.wicket.components.tree.StructurBrowserTreePanel;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.resources.StaticHeaderContributor;
import org.efaps.util.EFapsException;

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
    String positionH = null;
    String hiddenStrH = null;
    String positionV = null;
    String hiddenStrV = null;
    try {
      positionH = Context.getThreadContext().getUserAttribute(
          PositionUserAttribute.HORIZONTAL.getKey());
      hiddenStrH = Context.getThreadContext().getUserAttribute(
          PositionUserAttribute.HORIZONTAL_COLLAPSED.getKey());
      positionV = Context.getThreadContext().getUserAttribute(
          PositionUserAttribute.VERTICAL.getKey());
      hiddenStrV = Context.getThreadContext().getUserAttribute(
          PositionUserAttribute.VERTICAL_COLLAPSED.getKey());
    } catch (final EFapsException e) {
      e.printStackTrace();
    }
    final boolean hiddenH = "true".equalsIgnoreCase(hiddenStrH);
    if (hiddenH) {
      positionH = "20";
    } else if (positionH == null) {
      positionH = "200";
    }


    this.add(new ContentPaneBehavior(Region.LEADING,
                                     true,
                                     positionH + "px",
                                     null));
    //overwrite the contentpane
    this.add(new BorderBehavior(Design.HEADLINE));
    final boolean hiddenV = "true".equalsIgnoreCase(hiddenStrV);

    final SplitHeaderPanel header
                      = new SplitHeaderPanel("header", true, hiddenH, hiddenV);
    this.add(header);

    if (hiddenV) {
      positionV = "20px";
    } else if (positionV == null) {
      positionV = "50%";
    } else {
      positionV += "px";
    }
    final WebMarkupContainer top = new WebMarkupContainer("top");
    top.add(new ContentPaneBehavior(Region.TOP, true, null, positionV));
    this.add(top);
    header.addHideComponent(top);
    final StructurBrowserTreePanel stuctbrows
                                    = new StructurBrowserTreePanel("stuctbrows",
                                          _commandUUID,
                                          _oid,
                                          _listMenuKey);
    stuctbrows.setOutputMarkupId(true);
    top.add(stuctbrows);
    header.addHideComponent(stuctbrows);

    final WebMarkupContainer bottom = new WebMarkupContainer("bottom");
    bottom.add(new ContentPaneBehavior(Region.CENTER, true));
    this.add(bottom);
    header.addHideComponent(bottom);

    final WebMarkupContainer menuact = new WebMarkupContainer("menuact");
    menuact.setOutputMarkupId(true);
    bottom.add(menuact);
    menuact.add(new MenuTree("menu", _commandUUID, _oid, _listMenuKey));

  }
}
