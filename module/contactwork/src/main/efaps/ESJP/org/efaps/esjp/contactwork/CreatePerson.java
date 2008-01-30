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

package org.efaps.esjp.contactwork;

import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.db.Context;
import org.efaps.db.Insert;
import org.efaps.util.EFapsException;

/**
 * @author jmox
 * @version $Id$
 */
public class CreatePerson {

  /**
   * @param _parameter
   */
  public Return execute(final Parameter _parameter) throws EFapsException {
    Return ret = new Return();

    Context context = Context.getThreadContext();

    Insert insert = new Insert("ContactWork_Person");
    insert.add("Name", context.getParameter("forename") + " "
        + context.getParameter("surname"));
    insert.add("ForeName", context.getParameter("forename"));
    insert.add("SurName", context.getParameter("surname"));
    insert.execute();
    return ret;
  }
}
