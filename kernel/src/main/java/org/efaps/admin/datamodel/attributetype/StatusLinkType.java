/*
 * Copyright 2006 The eFaps Team
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

package org.efaps.admin.datamodel.attributetype;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;

import org.efaps.admin.lifecycle.Status;
import org.efaps.db.Context;

/**
 *
 */
public class StatusLinkType extends AbstractLinkType  {


  /**
   * The method returns a string as the viewable value of the attribute type.
   * Here, the id of the status is converted in the status string name with
   * the method {@link Status.getViewableName}.
   *
   * @param _locale locale object
   * @return status string
   * @see #getStatus
   */
  public String getViewableString(Locale _locale)  {
    return getStatus(null).getViewableName(null);
  }

  /**
   * The instance method returns the status object for the given value. The
   * value is an id.
   *
   * @return status object
   */
  public Status getStatus(Context _context)  {
try  {
    return Status.get(_context, getValue());
} catch (Throwable e)  {
  e.printStackTrace();
}
return null;
  }
}