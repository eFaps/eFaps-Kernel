/*
 * Copyright 2003-2007 The eFaps Team
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
import org.efaps.db.Context;
import org.efaps.db.Insert;
import org.efaps.db.SearchQuery;
import org.efaps.db.Update;
import org.efaps.util.EFapsException;

public class UserAttribute2Locale implements EventExecution {

  public Return execute(final Parameter _parameter) throws EFapsException {

    final Context context = Context.getThreadContext();
    final Return ret = new Return();

    final SearchQuery query = new SearchQuery();
    query.setQueryTypes("Admin_User_Attribute2Locale");
    query.addWhereExprEqValue("UserLink", context.getPersonId());
    query.addSelect("OID");
    query.execute();
    Update update;
    if (query.next()) {
      update = new Update(query.get("OID").toString());
    } else {
      update = new Insert("Admin_User_Attribute2Locale");
      update.add("UserLink", ((Long) context.getPersonId()).toString());
    }
    update.add("Locale", context.getParameter("locale"));
    update.execute();
    context.getUserAttributes().initialise();
    return ret;
  }
}
