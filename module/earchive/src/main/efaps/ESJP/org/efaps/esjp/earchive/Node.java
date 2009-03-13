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

import java.util.ArrayList;
import java.util.List;

import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author jmox
 * @version $Id$
 */
@EFapsUUID("4108effb-988f-42c0-a143-9b15ae57d4d9")
@EFapsRevision("$Rev$")
public class Node {


  public static String getNewHistoryId() throws EFapsException {
    final SearchQuery query = new SearchQuery();
    query.setQueryTypes("eArchive_NodeHistoryIdMax");
    query.addSelect("MaxHistoryID");
    query.execute();
    query.next();
    final Long value = query.get("MaxHistoryID") == null
                       ? 1
                       : (Long) query.get("MaxHistoryID") + 1;

    return value.toString();
  }


  public Return getTableUI(final Parameter _parameter) throws EFapsException {
    final Return ret = new Return();
    final Instance instance = _parameter.getInstance();
    String nodeid = null;
     if ("eArchive_Repository".equals(instance.getType().getName())) {
      nodeid = Revision.getLastRevisionNode(instance.getId());
    }
    final SearchQuery query = new SearchQuery();
    query.setQueryTypes("eArchive_Node2Node");
    query.setExpandChildTypes(true);
    query.addWhereExprEqValue("Parent", nodeid);
    query.addSelect("Name");
    query.addSelect("OID");
    query.execute();

    final List<List<Instance>> list = new ArrayList<List<Instance>>();
    while (query.next()) {
      final List<Instance> instances = new ArrayList<Instance>(1);
      instances.add(new Instance((String) query.get("OID")));
      list.add(instances);
    }

    ret.put(ReturnValues.VALUES, list);

    return ret;
  }

  public Return createDirectory(final Parameter _parameter)
      throws EFapsException {
    final String name = _parameter.getParameterValue("name");
    final Instance instance = _parameter.getInstance();
    String nodeid = null;
     if ("eArchive_Repository".equals(instance.getType().getName())) {
      nodeid = Revision.getLastRevisionNode(instance.getId());
    }
    final Insert insert = new Insert("eArchive_NodeDirectory");
    insert.add("HistoryId", getNewHistoryId());
    insert.execute();

    reviseParent(insert.getInstance(), nodeid, name);

    return new Return();
  }

  private void reviseParent(final Instance _child,
                            final String _parentId,
                            final String _name) throws EFapsException {
    final SearchQuery query = new SearchQuery();
    query.setQueryTypes("eArchive_NodeDirectory");
    query.addSelect("HistoryId");
    query.addWhereExprEqValue("ID", _parentId);
    query.execute();
    query.next();

    final String historyId = ((Long) query.get("HistoryId")).toString();
    final Insert insert = new Insert("eArchive_NodeDirectory");
    insert.add("HistoryId", historyId);
    insert.execute();

    final Instance newNode = insert.getInstance();

    final Insert node2node = new Insert("eArchive_NodeDirectory2Directory");
    node2node.add("Parent", ((Long) newNode.getId()).toString());
    node2node.add("Child", ((Long) _child.getId()).toString());
    node2node.add("Name", _name);
    node2node.execute();

    reconnetChildren(_parentId, _child.getId(),
                     ((Long) newNode.getId()).toString());

    final SearchQuery parentquery = new SearchQuery();
    parentquery.setQueryTypes("eArchive_Node2NodeHighestRev");
    parentquery.addWhereExprEqValue("Child", _parentId);
    parentquery.addSelect("Parent");
    parentquery.addSelect("Name");
    parentquery.execute();
    if (parentquery.next()) {
      final Long parentId = (Long) parentquery.get("Parent");
      final String name = (String) parentquery.get("Name");
      reviseParent(newNode, parentId.toString(), name);
    } else {
      connect2Revision(_parentId, ((Long) newNode.getId()).toString());
    }
  }

  final void reconnetChildren(final String _parentId, final long _childId,
                              final String _newParentId) throws EFapsException {
    final SearchQuery query = new SearchQuery();
    query.setQueryTypes("eArchive_Node2Node");
    query.setExpandChildTypes(true);
    query.addWhereExprEqValue("Parent", _parentId);
    query.addSelect("Child");
    query.addSelect("Name");
    query.execute();
    while (query.next()) {
      final long child = (Long) query.get("Child");
      if (child != _childId) {
        final Insert insert = new Insert("eArchive_NodeDirectory2Directory");
        insert.add("Parent", _newParentId);
        insert.add("Child", ((Long) child).toString());
        insert.add("Name", (String) query.get("Name"));
        insert.execute();
      }
    }
  }


  private void connect2Revision(final String _oldId, final String _newId)
      throws EFapsException {
    final SearchQuery query = new SearchQuery();
    query.setQueryTypes("eArchive_RevisionIdMax");
    query.addWhereExprEqValue("NodeId", _oldId);
    query.addSelect("RepositoryId");
    query.addSelect("Revision");
    query.execute();
    query.next();
    final Long repos = (Long) query.get("RepositoryId");
    final Long revision = (Long) query.get("Revision") + 1;

    final Insert revisionInsert = new Insert("eArchive_Revision");
    revisionInsert.add("Revision", revision.toString());
    revisionInsert.add("RepositoryLink", repos.toString());
    revisionInsert.add("NodeLink", _newId);
    revisionInsert.execute();
  }

}
