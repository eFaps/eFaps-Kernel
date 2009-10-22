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

package org.efaps.esjp.admin.access;

import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.ui.AbstractUserInterfaceObject.TargetMode;
import org.efaps.util.EFapsException;

/**
 * Class is used as an esjp to give access on a field. It contains methods that
 * allows to hide fields when necessary.
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("216703cb-15e7-423e-9d71-46497b63e374")
@EFapsRevision("$Rev$")
public class AccessOnField implements EventExecution {

  /**
   * Not used here.
   * @param _parameter Parameter as passed from eFaps to an esjp
   * @throws EFapsException never
   * @return null
   */
  public Return execute(final Parameter _parameter) throws EFapsException {
    return null;
  }

  /**
   * Method is used to deny access in EDITMODE.
   *
   * @param _parameter Parameter as passed from eFaps to an esjp
   * @return Return with True if VIEW, else false
   */
  public Return hideOnEdit(final Parameter _parameter) {
    final TargetMode mode
                      = (TargetMode) _parameter.get(ParameterValues.ACCESSMODE);

    final Return ret = new Return();

    if (!mode.equals(TargetMode.EDIT)) {
      ret.put(ReturnValues.TRUE, true);
    }
    return ret;
  }

  /**
   * Method is used to deny access in SearchMode.
   *
   * @param _parameter Parameter as passed from eFaps to an esjp
   * @return Return with True if VIEW, else false
   */
  public Return hideOnSearch(final Parameter _parameter) {
    final TargetMode mode
                      = (TargetMode) _parameter.get(ParameterValues.ACCESSMODE);

    final Return ret = new Return();

    if (!mode.equals(TargetMode.SEARCH)) {
      ret.put(ReturnValues.TRUE, true);
    }
    return ret;
  }

  /**
   * Method is used to give access only on VIEWMODE.
   *
   * @param _parameter Parameter as passed from eFaps to an esjp
   * @return Return with True if VIEW, else false
   */
  public Return viewOnly(final Parameter _parameter) {
    final TargetMode mode
                      = (TargetMode) _parameter.get(ParameterValues.ACCESSMODE);

    final Return ret = new Return();

    if (mode.equals(TargetMode.VIEW)) {
      ret.put(ReturnValues.TRUE, true);
    }
    return ret;
  }
}
