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

import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.util.ChronologyType;
import org.efaps.util.EFapsException;

/**
 * TODO description
 *
 * @author jmox
 * @version $Id$
 */
@EFapsUUID("d24a6606-95ac-427d-9689-31182dd71cd8")
public class ChronologyUI implements EventExecution
{

  public Return execute(final Parameter _parameter)
      throws EFapsException
  {
    final Instance instance
                    = (Instance) _parameter.get(ParameterValues.CALL_INSTANCE);

    final SearchQuery query = new SearchQuery();
    query.setObject(instance);
    query.addSelect("Chronology");
    query.execute();
    String actualChrono = ChronologyType.ISO8601.getKey();
    if (query.next()) {
      actualChrono = (String) query.get("Chronology");
    }

    final Return retVal = new Return();
    retVal.put(ReturnValues.VALUES,
               ChronologyType.getByKey(actualChrono).getLabel());

    return retVal;
  }

  private StringBuilder getField(final String _actualChrono) {
    final StringBuilder ret = new StringBuilder();

    ret.append("<select size=\"1\" name=\"Chronology4Edit\">");
    for (final ChronologyType chronoType : ChronologyType.values()) {
      ret.append("<option");
      if (_actualChrono.equals(chronoType.getKey())) {
        ret.append(" selected=\"selected\" ");
      }
      ret.append(" value=\"").append(chronoType.getKey()).append("\">")
        .append(chronoType.getLabel()).append("</option>");
    }

    ret.append("</select>");
    return ret;
  }

  public Return get4Edit(final Parameter _parameter) throws EFapsException {
    final Instance instance
      = (Instance) _parameter.get(ParameterValues.CALL_INSTANCE);

    final SearchQuery query = new SearchQuery();
    query.setObject(instance);
    query.addSelect("Chronology");
    query.execute();
    String actualChrono = ChronologyType.ISO8601.getKey();
    if (query.next()) {
      actualChrono = (String) query.get("Chronology");
    }

    final Return retVal = new Return();
    retVal.put(ReturnValues.VALUES, getField(actualChrono));

    return retVal;

  }

  public Return get4Setting(final Parameter _parameter)
      throws EFapsException {

    final Return retVal = new Return();
    final String actualChrono = Context.getThreadContext()
                                    .getPerson().getChronologyType().getKey();
    retVal.put(ReturnValues.VALUES,
               getField(actualChrono));

    return retVal;
  }
}
