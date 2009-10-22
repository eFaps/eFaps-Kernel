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

package org.efaps.esjp.admin.user;

import java.util.Set;
import java.util.TreeSet;

import org.joda.time.DateTimeZone;

import org.efaps.admin.EFapsClassNames;
import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;

/**
 * ESJP is used to get the value, and to render the fields for timezone.
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("122112fd-9bd8-4855-8948-6837272195eb")
@EFapsRevision("$Rev$")
public class TimeZoneUI implements EventExecution {

  /**
   * Method is called from within the form Admin_User_Person to retieve the
   * value for the timezone.
   *
   * @param _parameter Parameters as passed from eFaps
   * @return Return containing the timezone
   * @throws EFapsException on error
   */
  public Return execute(final Parameter _parameter)
      throws EFapsException {
    final Return retVal = new Return();

    final Instance instance
                    = (Instance) _parameter.get(ParameterValues.CALL_INSTANCE);
    //set a default value
    String actualTz = "UTC";

    if (instance != null && instance.getType().getUUID().equals(
                                      EFapsClassNames.USER_PERSON.getUuid())) {
      final SearchQuery query = new SearchQuery();
      query.setObject(instance);
      query.addSelect("TimeZone");
      query.execute();
      if (query.next()) {
        actualTz = (String) query.get("TimeZone");
      }
    }
    retVal.put(ReturnValues.SNIPLETT, getField(actualTz));

    return retVal;
  }

  /**
   * Method is called from within the form Admin_User_SettingChgForm to render
   * a drop down field with all Timezones.
   *
   * @param _parameter Parameters as passed from eFaps
   * @return Return containing a drop down
   * @throws EFapsException on error
   */
  public Return get4Setting(final Parameter _parameter)
      throws EFapsException {

    final Return retVal = new Return();
    retVal.put(ReturnValues.SNIPLETT,
               getField(Context.getThreadContext().getTimezone().getID()));

    return retVal;
  }

  /**
   * Method to build a drop down field for html containing all timezone.
   *
   * @param _actualTz actual timezone
   * @return StringBuilder with drop down
   */
  private StringBuilder getField(final String _actualTz) {
    final StringBuilder ret = new StringBuilder();

    final Set<?> timezoneIds = DateTimeZone.getAvailableIDs();
    final TreeSet<String> sortedTimeZoneIds = new TreeSet<String>();

    for (final Object id : timezoneIds) {
      sortedTimeZoneIds.add((String) id);
    }

    ret.append("<select size=\"1\" name=\"TimeZone4Edit\">");
    for (final String tzId : sortedTimeZoneIds) {
      ret.append("<option");
      if (_actualTz.equals(tzId)) {
        ret.append(" selected=\"selected\" ");
      }
      ret.append(" value=\"").append(tzId).append("\">")
        .append(tzId).append("</option>");
    }

    ret.append("</select>");
    return ret;
  }
}
