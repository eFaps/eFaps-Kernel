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

package org.efaps.ui.wicket.behaviors.update;

import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;

import org.efaps.admin.ui.AbstractCommand.TargetMode;

/**
 * @author jmox
 * @version $Id$
 */
public abstract class AbstractAjaxUpdateBehavior extends
    AbstractDefaultAjaxBehavior implements UpdateInterface {

  /**
   * this instance variable stores the TargetMode of this behavior
   */
  private TargetMode mode;

  /**
   * this instance variable stores the oid of this behavior
   */
  private String oid;

  public String getAjaxCallback() {
    return getCallbackScript().toString();
  }

  public String getId() {
    return getClass().toString();
  }

  public boolean isAjaxCallback() {
    return true;
  }

  public void setMode(final TargetMode _mode) {
    this.mode = _mode;
  }

  public void setOid(final String _oid) {
    this.oid = _oid;
  }

  /**
   * This is the getter method for the instance variable {@link #mode}.
   *
   * @return value of instance variable {@link #mode}
   */
  public TargetMode getMode() {
    return this.mode;
  }

  /**
   * This is the getter method for the instance variable {@link #oid}.
   *
   * @return value of instance variable {@link #oid}
   */
  public String getOid() {
    return this.oid;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.apache.wicket.ajax.AbstractDefaultAjaxBehavior#getPreconditionScript()
   */
  @Override
  protected CharSequence getPreconditionScript() {
    return null;
  }

  @Override
  protected CharSequence getCallbackScript() {
    return "function findFrame(_current, _target)  {"
        + "  var ret = _current.frames[_target];"
        + "  if (!ret) {"
        + "    for (var i=0; i < _current.frames.length && !ret; i++)  {"
        + "      ret = findFrame(_current.frames[i], _target);"
        + "    }"
        + "  }"
        + "  return ret;"
        + "}"
        + "var fen = findFrame(top,\"eFapsFrameContent\");"
        + "if(!fen){"
        + "  fen = top;"
        + "}"
        + "fen.setTimeout(function(){ fen.childCallBack(\"javascript:"
        + generateCallbackScript("wicketAjaxGet('"
            + getCallbackUrl(false)
            + "'")
        + "\");},0);";
    // the timeout is needed due to a bug in firefox, that does not close the
    // nsIXMLHttpRequest and therfore throws an error that disables any
    // further javascript. The timeout is a workaround for this bug.
  }
}
