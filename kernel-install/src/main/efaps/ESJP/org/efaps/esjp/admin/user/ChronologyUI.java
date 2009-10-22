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
import org.efaps.util.ChronologyType;
import org.efaps.util.EFapsException;

/**
 * ESJP is used to get the value, and to render the fields for chronology.
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("d24a6606-95ac-427d-9689-31182dd71cd8")
@EFapsRevision("$Rev$")
public class ChronologyUI implements EventExecution {

  /**
   * Method is called from within the form Admin_User_Person to retieve the
   * value for the chronology.
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

    String actualChrono = ChronologyType.ISO8601.getKey();

    if (instance != null && instance.getType().getUUID().equals(
                                    EFapsClassNames.USER_PERSON.getUuid())) {
      final SearchQuery query = new SearchQuery();
      query.setObject(instance);
      query.addSelect("Chronology");
      query.execute();
      if (query.next()) {
        actualChrono = (String) query.get("Chronology");
      }
    }
    retVal.put(ReturnValues.SNIPLETT,
               ChronologyType.getByKey(actualChrono).getLabel());

    return retVal;
  }

  /**
   * Method is called from within the form Admin_User_Person to render
   * a drop down field with all Chronologies.
   *
   * @param _parameter Parameters as passed from eFaps
   * @return Return containing a drop down
   * @throws EFapsException on error
   */
  public Return get4Edit(final Parameter _parameter) throws EFapsException {
    final Return retVal = new Return();

    final Instance instance
                    = (Instance) _parameter.get(ParameterValues.CALL_INSTANCE);
    //set a default
    String actualChrono = ChronologyType.ISO8601.getKey();
    if (instance != null && instance.getType().getUUID().equals(
                                      EFapsClassNames.USER_PERSON.getUuid())) {
      final SearchQuery query = new SearchQuery();
      query.setObject(instance);
      query.addSelect("Chronology");
      query.execute();
      if (query.next()) {
        actualChrono = (String) query.get("Chronology");
      }
    }
    retVal.put(ReturnValues.SNIPLETT, getField(actualChrono));
    return retVal;

  }

  /**
   * Method is called from within the form Admin_User_SettingChgForm to render
   * a drop down field with all Chronologies.
   *
   * @param _parameter Parameters as passed from eFaps
   * @return Return containing a drop down
   * @throws EFapsException on error
   */
  public Return get4Setting(final Parameter _parameter)
      throws EFapsException {

    final Return retVal = new Return();
    final String actualChrono = Context.getThreadContext()
                                    .getPerson().getChronologyType().getKey();
    retVal.put(ReturnValues.SNIPLETT,
               getField(actualChrono));

    return retVal;
  }

  /**
   * Method to build a drop down field for html containing all chronology.
   *
   * @param _actualChrono actual selected chronology
   * @return StringBuilder with drop down
   */
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
}
