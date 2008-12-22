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
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.esjp.admin.user;

import java.util.Set;
import java.util.TreeSet;

import org.joda.time.DateTimeZone;

import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;

/**
 * TODO description
 *
 * @author jmox
 * @version $Id$
 */
@EFapsUUID("122112fd-9bd8-4855-8948-6837272195eb")
public class TimeZoneUI implements EventExecution
{

  public Return execute(final Parameter _parameter)
      throws EFapsException
  {
    final Instance instance = (Instance) _parameter.get(ParameterValues.CALL_INSTANCE);

    final SearchQuery query = new SearchQuery();
    query.setObject(instance);
    query.addSelect("TimeZone");
    query.execute();
    String actualTz = "UTC";
    if (query.next()) {
      actualTz = (String) query.get("TimeZone");
    }
    final Set<?> timezoneIds = DateTimeZone.getAvailableIDs();
    final TreeSet<String> sortedTimeZoneIds = new TreeSet<String>();

    for (final Object id : timezoneIds) {
      sortedTimeZoneIds.add((String) id);
    }

    final StringBuilder ret = new StringBuilder();
    ret.append("<select size=\"1\" name=\"TimeZone4Edit\">");
    for (final String tzId : sortedTimeZoneIds) {
      ret.append("<option");
      if (actualTz.equals(tzId)) {
        ret.append(" selected=\"selected\" ");
      }
      ret.append(" value=\"").append(tzId).append("\">")
        .append(tzId).append("</option>");
    }

    ret.append("</select>");
    final Return retVal = new Return();
    retVal.put(ReturnValues.VALUES, ret);

    return retVal;
  }
}
