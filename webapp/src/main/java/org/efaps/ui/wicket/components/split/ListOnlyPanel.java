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

package org.efaps.ui.wicket.components.split;

import java.util.UUID;

import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;

import org.efaps.db.Context;
import org.efaps.ui.wicket.behaviors.dojo.ContentPaneBehavior;
import org.efaps.ui.wicket.behaviors.dojo.ContentPaneBehavior.Region;
import org.efaps.ui.wicket.components.menutree.MenuTree;
import org.efaps.ui.wicket.components.split.header.SplitHeaderPanel;
import org.efaps.ui.wicket.components.split.header.SplitHeaderPanel.PositionUserAttribute;
import org.efaps.ui.wicket.resources.EFapsContentReference;
import org.efaps.ui.wicket.resources.StaticHeaderContributor;
import org.efaps.util.EFapsException;

/**
 * Class is used to render a Panel which contains a ListMenu.
 *
 * @author jmox
 * @version $Id$
 */
public class ListOnlyPanel extends Panel {

  /**
   * Reference to the StyleSheet.
   */
  public static final EFapsContentReference CSS
          = new EFapsContentReference(ListOnlyPanel.class, "ListOnlyPanel.css");

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
   * @throws EFapsException
   */
  public ListOnlyPanel(final String _wicketId,
                       final UUID _commandUUID, final String _oid,
                       final String _listMenuKey) {
    super(_wicketId);
    this.add(StaticHeaderContributor.forCss(CSS));
    String position = null;
    String hiddenStr = null;
    try {
      position = Context.getThreadContext().getUserAttribute(
          PositionUserAttribute.HORIZONTAL.getKey());
      hiddenStr = Context.getThreadContext().getUserAttribute(
          PositionUserAttribute.HORIZONTAL_COLLAPSED.getKey());
    } catch (final EFapsException e) {
      // error is catch because its only user attributes
      e.printStackTrace();
    }
    final boolean hidden = "true".equalsIgnoreCase(hiddenStr);
    if (hidden) {
      position = "20";
    } else if (position == null) {
      position = "200";
    }
    this.add(new ContentPaneBehavior(Region.LEADING,
                                     true,
                                     position + "px",
                                     null));

    final SplitHeaderPanel header
                         = new SplitHeaderPanel("header", false, hidden, false);
    this.add(header);

    final WebMarkupContainer overflow = new WebMarkupContainer("overflow");
    overflow.setOutputMarkupId(true);
    overflow.add(new MenuTree("menu", _commandUUID, _oid, _listMenuKey)
        .setOutputMarkupId(true));
    this.add(overflow);
    if (hidden) {
      overflow.add(new SimpleAttributeModifier("style", "display:none;"));
    }
    header.addHideComponent(overflow);
  }
}
