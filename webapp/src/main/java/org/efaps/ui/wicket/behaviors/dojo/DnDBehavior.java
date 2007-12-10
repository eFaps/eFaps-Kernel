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

package org.efaps.ui.wicket.behaviors.dojo;

import org.apache.wicket.Component;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.IHeaderResponse;


/**
 * This class renders the drag and drop abbility from the DojoToolKit to a
 * component.<br>
 * It is used for all tags wich can be part of the Dojo-dnd. The handles, items
 * and the Source.
 *
 * @author jmox
 * @version $Id$
 */
public class DnDBehavior extends AbstractDojoBehavior {

  private static final long serialVersionUID = 1L;

  /**
   * enum used to set the Type of Drag and Drop this Behavior should render
   */
  public enum BehaviorType {
    HANDLE,
    ITEM,
    SOURCE;
  }

  /**
   * this instance variable stores what kind should be rendered
   */
  private final BehaviorType type;

  /**
   * this instance variable stores if the orientation for drag and drop is
   * horizontal or vertical. It is only used in case of BehaviorType.SOURCE
   */
  private boolean horizontal = false;

  /**
   * this instance variable stores if the items inside the source can only by
   * draged by an handle. It is only used in case of BehaviorType.SOURCE
   */
  private boolean handles = false;

  /**
   * this instance variable stores if it is allowed to copy items instead of
   * drag and drop. It is only used in case of BehaviorType.SOURCE
   */
  private boolean allowCopy = false;

  /**
   * this instance variable stores a javascript wich will be executed after the
   * drag and drop. It is only used in case of BehaviorType.SOURCE
   */
  private String appendJavaScript;

  private CharSequence dndType = "eFapsdnd";

  /**
   * Constuctor setting the Type of the DnDBehavior. Instead of using this
   * constructor it can be used on e of the static methods.
   * <li>{@link #getHandleBehavior()} </li>
   * <li>{@link #getItemBehavior()} </li>
   * <li>{@link #getSourceBehavior()} </li>
   *
   * @param _type
   *                BehaviorType of this DnDBehavior
   */
  public DnDBehavior(BehaviorType _type) {
    this.type = _type;
  }

  public DnDBehavior(BehaviorType _type, final String _dndType) {
    this.type = _type;
    this.dndType = _dndType;
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
      _tag.put("dndType", this.dndType);
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
      _tag.put("accept", this.dndType);
    }

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

  /**
   * This is the getter method for the instance variable {@link #allowCopy}.
   *
   * @return value of instance variable {@link #allowCopy}
   */
  public boolean isAllowCopy() {
    return this.allowCopy;
  }

  /**
   * This is the setter method for the instance variable {@link #allowCopy}.
   *
   * @param _allowCopy
   *                the allowCopy to set
   */
  public void setAllowCopy(final boolean _allowCopy) {
    this.allowCopy = _allowCopy;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.efaps.ui.wicket.components.dojo.AbstractDojoBehavior#renderHead(org.apache.wicket.markup.html.IHeaderResponse)
   */
  @Override
  public void renderHead(final IHeaderResponse _response) {
    super.renderHead(_response);
    if (this.type == BehaviorType.SOURCE) {

      final String varName =
          "subcription" + ((Long) System.currentTimeMillis()).toString();
      final StringBuilder builder = new StringBuilder();
      if (this.allowCopy
          || (this.appendJavaScript != null && this.appendJavaScript.length() > 0)) {

        builder
            .append("  var ")
            .append(varName)
            .append(";\n")
            .append(
                "  dojo.subscribe(\"/dnd/start\", function(source,nodes,iscopy){\n");

        if (!this.allowCopy) {
          builder
              .append("    source.copyState = function(keyPressed){ return false};\n");
        }

        builder.append("    ").append(varName).append(
            " = dojo.subscribe(\"/dnd/drop\", function(source,nodes,iscopy){\n"
                + "    var jsnode = source.getItem(nodes[0].id);\n"
                + "    var dndType  = jsnode.type;\n"
                + "    if(dndType ==\"").append(this.dndType).append(
            "\" ){\n" + "").append(this.appendJavaScript).append(
            "      dojo.unsubscribe(").append(varName).append(");\n").append(
            "   }\n });\n").append("  });\n").append(
            "  dojo.subscribe(\"/dnd/cancel\", function(){\n").append(
            "    dojo.unsubscribe(").append(varName).append(");\n  });\n");

        _response.renderJavascript(builder.toString(), DnDBehavior.class
            .toString());

      }
    }
  }

  /**
   * This is the getter method for the instance variable
   * {@link #appendJavaScript}.
   *
   * @return value of instance variable {@link #appendJavaScript}
   */
  public String getAppendJavaScript() {
    return this.appendJavaScript;
  }

  /**
   * This is the setter method for the instance variable
   * {@link #appendJavaScript}.
   *
   * @param _appendJavaScript
   *                the appendJavaScript to set
   */
  public void setAppendJavaScript(final String _appendJavaScript) {
    this.appendJavaScript = _appendJavaScript;
  }

  public static DnDBehavior getSourceBehavior() {
    return new DnDBehavior(BehaviorType.SOURCE);
  }

  public static DnDBehavior getSourceBehavior(final String _dndType) {
    return new DnDBehavior(BehaviorType.SOURCE, _dndType);
  }

  public static DnDBehavior getItemBehavior(final String _dndType) {
    return new DnDBehavior(BehaviorType.ITEM, _dndType);
  }

  public static DnDBehavior getItemBehavior() {
    return new DnDBehavior(BehaviorType.ITEM);
  }

  public static DnDBehavior getHandleBehavior() {
    return new DnDBehavior(BehaviorType.HANDLE);
  }

  /**
   * This is the getter method for the instance variable {@link #dndType}.
   *
   * @return value of instance variable {@link #dndType}
   */
  public CharSequence getDndType() {
    return this.dndType;
  }

  /**
   * This is the setter method for the instance variable {@link #dndType}.
   *
   * @param dndType
   *                the dndType to set
   */
  public void setDndType(CharSequence dndType) {
    this.dndType = dndType;
  }

}
