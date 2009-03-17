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

package org.efaps.esjp.earchive.node;

import java.util.ArrayList;
import java.util.List;

import org.efaps.admin.datamodel.Type;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.esjp.earchive.repository.Repository;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author jmox
 * @version $Id$
 */
@EFapsUUID("d6cb382d-a8e8-4b35-9d28-6b344c53f676")
@EFapsRevision("$Rev$")
public class NodeUI {

  public Return getTableUI(final Parameter _parameter) throws EFapsException {
    final Return ret = new Return();
     final Instance instance = _parameter.getInstance();
    Long nodeid = null;
    if ("eArchive_Repository".equals(instance.getType().getName())) {
      nodeid = Node.getRootNodeFromDB(new Repository(instance)).getId();
    } else {
      nodeid = instance.getId();
    }
    final SearchQuery query = new SearchQuery();
    query.setQueryTypes("eArchive_Node2NodeView");
    query.setExpandChildTypes(true);
    query.addWhereExprEqValue("Parent", nodeid);
    query.addSelect("NodeType");
    query.addSelect("Child");
    query.addSelect("OID");
    query.execute();

    final List<List<Instance>> list = new ArrayList<List<Instance>>();
    while (query.next()) {
      final List<Instance> instances = new ArrayList<Instance>(1);
      instances.add(new Instance(Type.get((Long) query.get("NodeType")) ,
                                ((Long) query.get("Child")).toString()));
      list.add(instances);
    }

    ret.put(ReturnValues.VALUES, list);

    return ret;
  }

  public Return createDirectory(final Parameter _parameter)
      throws EFapsException {
    final String name = _parameter.getParameterValue("name");
    final Instance instance = _parameter.getInstance();
    final Node node;
    if ("eArchive_Repository".equals(instance.getType().getName())) {
      node = Node.getRootNodeFromDB(new Repository(instance));
    } else {
      node = Node.getNodeFromDB(instance.getId());
    }
    final Node newDir = Node.createNewNode(name);
    newDir.connectRevise(node);
    return new Return();
}
}
