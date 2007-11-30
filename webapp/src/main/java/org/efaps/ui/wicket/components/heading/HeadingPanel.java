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
 * Revision:        $Rev:1510 $
 * Last Changed:    $Date:2007-10-18 09:35:40 -0500 (Thu, 18 Oct 2007) $
 * Last Changed By: $Author:jmox $
 */
package org.efaps.ui.wicket.components.heading;

import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

/**
 * @author jmox
 * @version $Id:TitelPanel.java 1510 2007-10-18 14:35:40Z jmox $
 */
public class HeadingPanel extends Panel {

  private static final long serialVersionUID = 1L;

  /**
   * instance variable storing the level of the Heading, if the level is 0
   * (default) than a Titel will be rendered
   */
  private int level = 0;

  public HeadingPanel(final String _wicketId, final String _heading) {
    super(_wicketId);
    addComponents(_heading);
  }

  public HeadingPanel(final String _wicketId, final String _heading,
                      final int _level) {
    super(_wicketId);
    this.level = _level;
    addComponents(_heading);
  }

  private void addComponents(final String _heading) {
    final WebMarkupContainer container = new WebMarkupContainer("container");
    this.add(container);
    if (this.level == 0) {
      container.add(new SimpleAttributeModifier("class", "eFapsFrameTitle"));
    } else {
      container.add(new SimpleAttributeModifier("class", "eFapsHeading"
          + this.level));
    }
    container.add(new Label("heading", _heading));
  }

}
