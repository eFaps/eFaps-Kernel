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

package org.efaps.webapp.components.dojo;

import org.apache.wicket.Component;
import org.apache.wicket.markup.ComponentTag;

public class ContentPaneBehavior extends AbstractDojoBehavior {

  private static final long serialVersionUID = 1L;

  private int sizeshare = 50;

  private int sizemin = 20;

  public ContentPaneBehavior() {
  }

  public ContentPaneBehavior(final int _sizeshare, final int _sizemin) {
    this.sizeshare = _sizeshare;
    this.sizemin = _sizemin;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.apache.wicket.behavior.AbstractBehavior#onComponentTag(org.apache.wicket.Component,
   *      org.apache.wicket.markup.ComponentTag)
   */
  @Override
  public void onComponentTag(Component component, ComponentTag tag) {
    super.onComponentTag(component, tag);
    tag.put("dojoType", "dijit.layout.ContentPane");
    tag.put("sizeshare", this.sizeshare);
    tag.put("sizemin", this.sizemin);
  }

  /**
   * This is the setter method for the instance variable {@link #sizeshare}.
   *
   * @param sizeshare
   *                the sizeshare to set
   */
  public void setSizeshare(int sizeshare) {
    this.sizeshare = sizeshare;
  }

  /**
   * This is the setter method for the instance variable {@link #sizemin}.
   *
   * @param sizemin
   *                the sizemin to set
   */
  public void setSizemin(int sizemin) {
    this.sizemin = sizemin;
  }

}
