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

package org.efaps.ui.wicket.components.table.header;

import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebComponent;

/**
 * This class renders a Span wich can be used to resize columns in a Header. It
 * is placed between the cells in the Header.
 *
 * @author jmox
 * @version $Id$
 */
public class Seperator extends WebComponent {

  private static final long serialVersionUID = 1L;

  private final int id;

  public Seperator(final String _wicketId, final int _outputid,
                   final String _propId) {
    super(_wicketId);
    this.id = _outputid;
    this.add(new SimpleAttributeModifier("class", "eFapsTableHeaderSeperator"));
    this.add(new SimpleAttributeModifier("onmousedown",
        "beginColumnSize(this,event)"));

    this.add(new SimpleAttributeModifier("onmouseup",
        "endColumnSize(this,event," + _propId + ")"));

  }

  /*
   * (non-Javadoc)
   *
   * @see org.apache.wicket.Component#onComponentTag(org.apache.wicket.markup.ComponentTag)
   */
  @Override
  protected void onComponentTag(final ComponentTag _tag) {
    super.onComponentTag(_tag);
    _tag.put("id", this.id + "eFapsHeaderSeperator");
    _tag.setName("span");
  }

}
