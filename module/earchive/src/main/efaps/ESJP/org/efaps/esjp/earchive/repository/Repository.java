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

package org.efaps.esjp.earchive.repository;

import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.esjp.earchive.Node;
import org.efaps.esjp.earchive.Revision;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author jmox
 * @version $Id$
 */
@EFapsUUID("de8eaa7f-154a-40c3-ae6a-6ad061f3cffa")
@EFapsRevision("$Rev$")
public class Repository {

  public Return create(final Parameter _parameter) throws EFapsException {
    //create new repository
    final Insert insert = new Insert("eArchive_Repository");
    insert.add("Name", _parameter.getParameterValue("name"));
    insert.execute();
    final Instance rep = insert.getInstance();
    final Insert nodeInsert = new Insert("eArchive_NodeDirectory");
    nodeInsert.add("HistoryId", Node.getNewHistoryId());
    nodeInsert.execute();
    final Instance node = nodeInsert.getInstance();

    final Insert revisionInsert = new Insert("eArchive_Revision");
    revisionInsert.add("Revision", Revision.getNewRevisionId(rep.getId()));
    revisionInsert.add("RepositoryLink", ((Long) rep.getId()).toString());
    revisionInsert.add("NodeLink", ((Long) node.getId()).toString());
    revisionInsert.execute();

    return new Return();
  }
}

