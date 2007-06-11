/*
 * Copyright 2003 - 2007 The eFaps Team
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
 * Revision:        $Rev:851 $
 * Last Changed:    $Date:2007-06-02 12:36:03 -0500 (Sat, 02 Jun 2007) $
 * Last Changed By: $Author:jmo $
 */

package org.efaps.webapp.programs;

import java.util.HashMap;
import java.util.Map;

import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.ParameterInterface;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.ReturnInterface;
import org.efaps.admin.event.ParameterInterface.ParameterValues;
import org.efaps.admin.event.ReturnInterface.ReturnValues;
import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;

public class RangesValue implements EventExecution {

  public ReturnInterface execute(ParameterInterface _parameter) {
    Return ret = new Return();
    try {

      String type = (String) ((Map) _parameter.get(ParameterValues.PROPERTIES))
          .get("Type");

      String value = (String) ((Map) _parameter.get(ParameterValues.PROPERTIES))
          .get("Value");
      SearchQuery query = new SearchQuery();
      
      Map<String, String> map = new HashMap<String, String>();

      query.setQueryTypes(type);
      query.addSelect("ID");
      query.addSelect(value);
      query.execute();

      while (query.next()) {
        map.put(query.get("ID").toString(), query.get(value).toString());
      }

      ret.put(ReturnValues.VALUES, map);
    } catch (EFapsException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    System.out.print("gehtdoch");
    return ret;
  }
}
