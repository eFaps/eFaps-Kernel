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

package org.efaps.ui.wicket.components;

import org.apache.wicket.ajax.AjaxEventBehavior;

/**
 * @author jmo
 * @version $Id$
 */
public abstract class AbstractAjaxCallBackBehavior extends AjaxEventBehavior {

  private static final long serialVersionUID = 1L;

  private final Target target;

  public enum Target {
    PARENT("parent"),
    TOP("top"),
    SELF("");

    public final String jstarget;

    private Target(final String _target) {
      this.jstarget = _target;
    }

  }

  public AbstractAjaxCallBackBehavior(final String _event, final Target _target) {
    super(_event);
    this.target = _target;
  }

  @Override
  protected CharSequence getCallbackScript() {
    String str =
        super.getCallbackScript().toString().replace("return !wcall;", "");
    CharSequence ret = null;
    if (this.target != Target.SELF) {
      ret =
          this.target.jstarget
              + ".childCallBack(\"javascript:"
              + str
              + "\")";
    } else {
      ret = str.replace("'", "\"");
    }
    return ret;
  }

}
