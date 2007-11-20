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

package org.efaps.ui.wicket.components.dojo;

import org.apache.wicket.Component;
import org.apache.wicket.markup.ComponentTag;

/**
 * @author jmox
 * @version $Id$
 */
public class DnDBehavior extends AbstractDojoBehavior {

  private static final long serialVersionUID = 1L;

  public enum BehaviorType {
    HANDLE,
    ITEM,
    SOURCE;
  }

  private final BehaviorType type;

  private boolean horizontal = false;

  private boolean handles = false;

  public DnDBehavior(BehaviorType _type) {
    this.type = _type;
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

    if (this.type == BehaviorType.ITEM) {
      String value = "dojoDndItem ";
      if (_tag.getString("class") != null) {
        value += _tag.getString("class");
      }
      _tag.put("class", value);
    } else if (this.type == BehaviorType.HANDLE) {
      String value = "dojoDndHandle ";
      if (_tag.getString("class") != null) {
        value += _tag.getString("class");
      }
      _tag.put("class", value);
    } else if (this.type == BehaviorType.SOURCE) {
      _tag.put("dojoType", "dojo.dnd.Source");
      if (this.horizontal) {
        _tag.put("horizontal", "true");
      }
      if (this.handles) {
        _tag.put("withHandles", "true");
      }
    }

  }

  public static DnDBehavior getSourceBehavior() {
    return new DnDBehavior(BehaviorType.SOURCE);
  }

  public static DnDBehavior getItemBehavior() {
    return new DnDBehavior(BehaviorType.ITEM);
  }

  public static DnDBehavior getHandleBehavior() {
    return new DnDBehavior(BehaviorType.HANDLE);
  }

  /**
   * This is the getter method for the instance variable {@link #horizontal}.
   *
   * @return value of instance variable {@link #horizontal}
   */
  public boolean isHorizontal() {
    return this.horizontal;
  }

  /**
   * This is the setter method for the instance variable {@link #horizontal}.
   *
   * @param _horizontal
   *                the horizontal to set
   */
  public void setHorizontal(final boolean _horizontal) {
    this.horizontal = _horizontal;
  }

  /**
   * This is the getter method for the instance variable {@link #handles}.
   *
   * @return value of instance variable {@link #handles}
   */
  public boolean isHandles() {
    return this.handles;
  }

  /**
   * This is the setter method for the instance variable {@link #handles}.
   *
   * @param _handles
   *                the widthHandles to set
   */
  public void setHandles(final boolean _handles) {
    this.handles = _handles;
  }

}
