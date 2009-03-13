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

package org.efaps.esjp.earchive;

import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author jmox
 * @version $Id$
 */
@EFapsUUID("28f7fa7e-0687-499d-9355-783038019331")
@EFapsRevision("$Rev$")
public class Revision {


  public static String getNewRevisionId(final long _repositoryId)
      throws EFapsException {
    final SearchQuery query = new SearchQuery();
    query.setQueryTypes("eArchive_RevisionIdMax");
    query.addWhereExprEqValue("RepositoryId", _repositoryId);
    query.addSelect("Revision");
    query.execute();
    final Long value;
    if (query.next()) {
      value = (Long) query.get("Revision") + 1;
    } else {
      value = new Long(1);
    }
    return value.toString();
  }

  public static String getLastRevisionNode(final long _repositoryId)
      throws EFapsException {
    final SearchQuery query = new SearchQuery();
    query.setQueryTypes("eArchive_RevisionIdMax");
    query.addWhereExprEqValue("RepositoryId", _repositoryId);
    query.addSelect("NodeId");
    query.execute();
    Long value = null;
    if (query.next()) {
      value = (Long) query.get("NodeId");
    }
    return value != null ? value.toString() : null;
  }

}
