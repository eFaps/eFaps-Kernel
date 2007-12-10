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

package org.efaps.ui.wicket.behaviors.dojo;

import org.apache.wicket.Component;
import org.apache.wicket.markup.ComponentTag;


/**
 * This clas turns a Component into a Dojo-ContentPane
 *
 * @author jmox
 * @version $Id$
 */
public class ContentPaneBehavior extends AbstractDojoBehavior {

  private static final long serialVersionUID = 1L;

  /**
   * size of this ContentPane as a weighted with
   */
  private int sizeshare = 50;

  /**
   * minimum size of this ContentPane as a weighted with
   */
  private int sizemin = 20;

  /**
   * Constructor using the default values for {@link #sizeshare} and
   * {@link #sizemin}
   */
  public ContentPaneBehavior() {
    super();
  }

  /**
   * Constructor
   *
   * @param _sizeshare
   *                sets the value for {@link #sizeshare}
   * @param _sizemin
   *                sets the value for {@link #sizemin}
   */
  public ContentPaneBehavior(final int _sizeshare, final int _sizemin) {
    super();
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
  public void onComponentTag(final Component _component, final ComponentTag _tag) {
    super.onComponentTag(_component, _tag);
    _tag.put("dojoType", "dijit.layout.ContentPane");
    _tag.put("sizeshare", this.sizeshare);
    _tag.put("sizemin", this.sizemin);
  }

  /**
   * This is the setter method for the instance variable {@link #sizeshare}.
   *
   * @param _sizeshare
   *                the sizeshare to set
   */
  public void setSizeshare(final int _sizeshare) {
    this.sizeshare = _sizeshare;
  }

  /**
   * This is the setter method for the instance variable {@link #sizemin}.
   *
   * @param _sizemin
   *                the sizemin to set
   */
  public void setSizemin(final int _sizemin) {
    this.sizemin = _sizemin;
  }

}
