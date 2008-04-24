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

import java.util.Map;

import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;

/**
 * @author jmox
 * @version $Id$
 */
@EFapsUUID("1b777261-6da1-4003-87e4-2937e44ff269")
public class Person
{

  public Return connectPerson2RoleUI(final Parameter _parameter)
      throws EFapsException
  {
    final Return ret = new Return();

    final Map<?, ?> properties = (Map<?, ?>) _parameter.get(ParameterValues.PROPERTIES);
    final Instance parent = (Instance) _parameter.get(ParameterValues.INSTANCE);
    final String childOids[] = (String[]) _parameter.get(ParameterValues.OTHERS);

    final String type = (String) properties.get("ConnectType");

    for (String childOid : childOids) {
      final Instance child = new Instance(childOid);
      final Insert insert = new Insert(type);
      insert.add("UserFromLink", "" + parent.getId());
      insert.add("UserToLink", "" + child.getId());
      insert.add("UserJAASSystem", "" + getJAASSystemID());
      insert.execute();
    }

    return ret;

  }

  /**
   * This method inserts the JAASSystem for a User into the eFaps-Database.<br>
   * It is executed on a INSERT_POST Trigger on the Type User_Person.
   *
   * @param _parameter
   * @return null
   * @throws EFapsException
   */
  public Return insertJaaskeyTrg(final Parameter _parameter)
      throws EFapsException
  {
    final Instance instance = (Instance) _parameter.get(ParameterValues.INSTANCE);
    final Map<?, ?> values =(Map<?, ?>) _parameter.get(ParameterValues.NEW_VALUES);

    final String jaassystemid = getJAASSystemID();
    if (jaassystemid != null) {
      final Insert insert = new Insert("Admin_User_JAASKey");
      insert.add("Key", values.get(instance.getType().getAttribute("Name")).toString());
      insert.add("JAASSystemLink", getJAASSystemID());
      insert.add("UserLink", ((Long) instance.getId()).toString());
      insert.execute();
    }
    return null;
  }

  /**
   * get the ID of the JAASSYstem for eFaps
   *
   * @return ID of the JAASSYSTEM, NULL if not found
   * @throws EFapsException
   */
  private String getJAASSystemID()
      throws EFapsException
  {
    String objId = null;

    final SearchQuery query = new SearchQuery();
    query.setQueryTypes("Admin_User_JAASSystem");
    query.addWhereExprEqValue("Name", "eFaps");
    query.addSelect("ID");
    query.execute();
    if (query.next()) {
      objId = query.get("ID").toString();
    }
    query.close();

    return objId;
  }
}
