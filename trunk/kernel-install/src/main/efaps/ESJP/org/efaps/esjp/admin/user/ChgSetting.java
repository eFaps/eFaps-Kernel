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

import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.admin.user.Person;
import org.efaps.admin.user.Person.AttrName;
import org.efaps.db.Context;
import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;

/**
 * @author The eFaps Team
 *  TODO description , accesscheck
 */
@EFapsUUID("55bc2ba5-1797-469d-bb6c-1d3710f246dc")
@EFapsRevision("$Rev$")
public class ChgSetting implements EventExecution {

  public Return execute(final Parameter _parameter) throws EFapsException {

    final Context context = Context.getThreadContext();

    final Person person = context.getPerson();
    person.updateAttrValue(AttrName.TIMZONE,
                           context.getParameter("TimeZone4Edit"));
    person.updateAttrValue(AttrName.CHRONOLOGY,
                           context.getParameter("Chronology4Edit"));
    final String[] locale = context.getParameter("Locale").split(":");
    person.updateAttrValue(AttrName.LOCALE, locale[1], locale[0]);
    person.commitAttrValuesInDB();
    return new Return();
  }



  public Return getLocale4Setting(final Parameter _parameter)
    throws EFapsException {
    final StringBuilder strBld = new StringBuilder();

    final SearchQuery query = new SearchQuery();
    query.setQueryTypes("Admin_Language");
    query.addSelect("ID");
    query.addSelect("Language");
    query.execute();

    strBld.append("<select size=\"1\" name=\"Locale\">");
    final String actLang = Context.getThreadContext().getLocale().getLanguage();
    while (query.next()) {
      final String language = (String) query.get("Language");
      strBld.append("<option");
      if (actLang.equals(language)) {
        strBld.append(" selected=\"selected\" ");
      }
      strBld.append(" value=\"").append(query.get("ID") + ":" + language)
        .append("\">").append(language).append("</option>");
    }

    strBld.append("</select>");

    final Return retVal = new Return();
    retVal.put(ReturnValues.SNIPLETT, strBld);

  return retVal;
  }
}