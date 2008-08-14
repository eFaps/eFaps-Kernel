/*
 * Copyright 2003-2008 The eFaps Team
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
 * This class turns a Component into a dojo SplitContainer.
 *
 * @author jmox
 * @version $Id$
 */
public class SplitContainerBehavior extends AbstractDojoBehavior {

  private static final long serialVersionUID = 1L;

  /**
   * This enum is used to set the Orientation of this Splitcontainer
   */
  public enum Orientation {
    HORIZONTAL("horizontal"),
    VERTICAL("vertical");

    private Orientation(String _value) {
      this.value = _value;
    }

    public String value;

  }

  /**
   * This instance variable contains the Orientation of the Splitcontainer
   */
  private Orientation orientation = Orientation.HORIZONTAL;

  /**
   * This instance variable contains the width of the sizer in Pixel
   */
  private int sizerWidth = 5;

  /**
   * this instance variable sets, if activeSizingis activated or not
   */
  private boolean activeSizing = false;

  /**
   * sets some additional Style
   */
  private String style = "width: 100%; height: 100%;";

  @Override
  public void onComponentTag(final Component _component, final ComponentTag _tag) {
    super.onComponentTag(_component, _tag);
    _tag.put("dojoType", "dijit.layout.SplitContainer");
    _tag.put("orientation", this.orientation.value);
    _tag.put("sizerWidth", this.sizerWidth);
    _tag.put("activeSizing", this.activeSizing);
    _tag.put("style", this.style);
  }

  /**
   * This is the setter method for the instance variable {@link #orientation}.
   *
   * @param _orientation
   *                the orientation to set
   */
  public void setOrientation(final Orientation _orientation) {
    this.orientation = _orientation;
  }

  /**
   * This is the setter method for the instance variable {@link #sizerWidth}.
   *
   * @param _sizerWidth
   *                the sizerWidth to set
   */
  public void setSizerWidth(final int _sizerWidth) {
    this.sizerWidth = _sizerWidth;
  }

  /**
   * This is the setter method for the instance variable {@link #activeSizing}.
   *
   * @param _activeSizing
   *                the activeSizing to set
   */
  public void setActiveSizing(final Boolean _activeSizing) {
    this.activeSizing = _activeSizing;
  }

  /**
   * This is the setter method for the instance variable {@link #style}.
   *
   * @param _style
   *                the style to set
   */
  public void setStyle(final String _style) {
    this.style = _style;
  }

}
