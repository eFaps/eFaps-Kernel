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

package org.efaps.ui.wicket.behaviors;

import org.apache.wicket.ajax.AjaxEventBehavior;

/**
 * @author jmox
 * @version $Id:AbstractAjaxCallBackBehavior.java 1510 2007-10-18 14:35:40Z jmox $
 */
public abstract class AbstractAjaxCallBackBehavior extends AjaxEventBehavior {

  /**
   * Enum used to define the targets.
   */
  public enum Target {
    /** The target is the parent window. */
    PARENT("parent"),
    /** The target is the top window. */
    TOP("top"),
    /** The target is the self window. */
    SELF("");

    /**
     * stores the target.
     */
    public final String jstarget;

    /**
     * @param _target target to set
     */
    private Target(final String _target) {
      this.jstarget = _target;
    }

  }

  /**
   * Neeed for serialization.
   */
  private static final long serialVersionUID = 1L;

  /**
   * Target for this callback.
   */
  private final Target target;

  /**
   * @param _event    event the behavior should be executed on
   * @param _target   target of the javascript
   */
  public AbstractAjaxCallBackBehavior(final String _event,
                                      final Target _target) {
    super(_event);
    this.target = _target;
  }

  /**
  * Get the call back script.
  * @see org.apache.wicket.ajax.AbstractDefaultAjaxBehavior#getCallbackScript()
  * @return JavaScript
  */
  @Override
  protected CharSequence getCallbackScript() {
    final String str =
        super.getCallbackScript().toString().replace("return !wcall;", "");
    CharSequence ret = null;
    if (this.target != Target.SELF) {
      ret = this.target.jstarget + ".childCallBack(\"javascript:" + str + "\")";
    } else {
      ret = str.replace("'", "\"");
    }
    return ret;
  }

  /**
   * The precondition script must be overwritten to prevent JavaScript error.
   * @return null
   */
  @Override
  protected CharSequence getPreconditionScript() {
    return null;
  }
}
