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

import java.util.UUID;

import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.esjp.earchive.node.Node;
import org.efaps.esjp.earchive.revision.Revision;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author jmox
 * @version $Id$
 */
public class RepositoryUI {

  public Return create(final Parameter _parameter) throws EFapsException {
    //create new repository
    final String name = _parameter.getParameterValue("name");
    final String msg = _parameter.getParameterValue("commitMessage");

    final Insert insert = new Insert("eArchive_Repository");
    insert.add("Name", name);
    insert.add("LastRevision", "0");
    insert.add("UUID", UUID.randomUUID().toString());
    insert.execute();
    final Instance instance = insert.getInstance();
    final Repository repository = new Repository(instance);
    final Node node = Node.createNewNode(repository, name,
                                         Node.TYPE_NODEDIRECTORY, null);
    Revision.getNewRevision(repository, node, msg);

    return new Return();
  }
}
