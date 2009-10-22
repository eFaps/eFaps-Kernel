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

package org.efaps.esjp.admin.datamodel;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.efaps.admin.EFapsClassNames;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
@EFapsUUID("cd9958cf-f8f4-4013-955e-f426e6139c65")
@EFapsRevision("$Rev$")
public class PersonLink {

  public Return personValuePickerUI(final Parameter _parameter)
      throws EFapsException {
    final String input = (String) _parameter.get(ParameterValues.OTHERS);
    final TreeMap<String, String[]> map = new TreeMap<String, String[]>();

    final SearchQuery query = new SearchQuery();
    query.setQueryTypes(Type.get(EFapsClassNames.USER_PERSON).getName());
    query.addWhereExprMatchValue("Name", input + "*");
    query.addSelect("ID");
    query.addSelect("Name");
    query.addSelect("FirstName");
    query.addSelect("LastName");
    query.execute();
    while (query.next()) {
      final String id = ((Long) query.get("ID")).toString();
      final String name = (String) query.get("Name");
      final String first = (String) query.get("FirstName");
      final String last = (String) query.get("LastName");
      map.put(name, new String[]{id, name, last, first});
    }
    final List<String[]> ret = new ArrayList<String[]>();
    for (final Entry<String, String[]> entry : map.entrySet()) {
      ret.add(entry.getValue());
    }
    final Return retVal = new Return();
    retVal.put(ReturnValues.VALUES, ret);
    return retVal;
  }


}
