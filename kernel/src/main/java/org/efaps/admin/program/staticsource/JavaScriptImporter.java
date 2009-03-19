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

package org.efaps.admin.program.staticsource;

import static org.efaps.admin.EFapsClassNames.ADMIN_PROGRAM_JAVASCRIPT;

import java.net.URL;

import org.efaps.admin.datamodel.Type;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;

/**
 * Class used to import javascript programs into eFaps.
 *
 * @author jmox
 * @version $Id$
 */
public class JavaScriptImporter extends AbstractSourceImporter {

  /**
   * @param _url URL of the javascript file
   * @throws EFapsException on error
   */
  public JavaScriptImporter(final URL _url) throws EFapsException {
    super(_url);
  }

  /**
   * Searches for the given name the css in eFaps. If exists, the instance
   * is returned.
   *
   * @return found instance (or null if not found)
   * @throws EFapsException if search query could not be executed
   * @see #programName
   */
  @Override
  public Instance searchInstance() throws EFapsException {
    Instance instance = null;

    final Type esjpType = Type.get(ADMIN_PROGRAM_JAVASCRIPT);
    final SearchQuery query = new SearchQuery();
    query.setQueryTypes(esjpType.getName());
    query.addWhereExprEqValue("Name", getProgramName());
    query.addSelect("OID");
    query.executeWithoutAccessCheck();
    if (query.next()) {
      instance = Instance.get((String) query.get("OID"));
    }
    query.close();

    return instance;
  }

}
