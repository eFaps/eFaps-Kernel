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

import java.util.Map;
import java.util.TreeMap;

import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;

/**
 * This Class gets a Range from the Database.<br>
 * The Class makes a query against the Database, with the "Type" from the
 * Properties of the Parameters and returns a map sorted by the values. The key
 * returned is the value, as specified by the Property "Value". The value of the
 * map is the ID of the Objects.
 * The sorting is done by using a TreeMap, that means that the Objects are
 * sorted by their natural order.
 * Both value and key are String.
 *
 * @author jmox
 * @version $Id$
 */
@EFapsUUID("6e9283bb-06a7-40a5-8420-becc4cff72f5")
@EFapsRevision("$Rev$")
public class RangesValue implements EventExecution
{

  public Return execute(final Parameter _parameter)
      throws EFapsException
  {
    final Return ret = new Return();

    final String type =
        (String) ((Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES))
            .get("Type");

    final String value =
        (String) ((Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES))
            .get("Value");

    final SearchQuery query = new SearchQuery();
    query.setQueryTypes(type);
    query.addSelect("ID");
    query.addSelect(value);
    query.execute();

    final Map<String, String> map = new TreeMap<String, String>();

    while (query.next()) {
      map.put(query.get(value).toString(),query.get("ID").toString());
    }
    query.close();

    ret.put(ReturnValues.VALUES, map);

    return ret;
  }
}
