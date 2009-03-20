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

package org.efaps.ui.wicket.behaviors.update;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;


/**
 * This Interface is used for Behaviors which are stored in
 * {@link #org.efaps.ui.wicket.EFapsSession()}. For an example of implementing
 * this Interface
 * {@link #rg.efaps.ui.wicket.behaviors.update.AbstractAjaxUpdateBehavior()}
 *
 * @see #org.efaps.ui.wicket.behaviors.update.AbstractAjaxUpdateBehavior
 * @author jmox
 * @version $Id$
 */
public interface UpdateInterface {

  /**
   * This method should return if the class should respond on a Ajaxrequest.
   *
   * @return true if ajax
   */
  boolean isAjaxCallback();

  /**
   * Thi smethod should return the CallBackScript of the Behavior.
   *
   * @return ajax callback
   */
  String getAjaxCallback();

  /**
   * Method used to set the Oid.
   *
   * @param _instanceKey oid to set
   */
  void setInstanceKey(final String _instanceKey);

  /**
   * Method used to set the TargetMode.
   *
   * @param _mode mode to set
   */
  void setMode(final TargetMode _mode);

  /**
   * returns the Id of this Behavior.
   *
   * @return id
   */
  String getId();

}
