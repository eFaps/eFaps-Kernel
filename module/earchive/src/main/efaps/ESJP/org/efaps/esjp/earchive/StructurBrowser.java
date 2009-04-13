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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.efaps.admin.datamodel.Type;
import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.esjp.earchive.node.Node;
import org.efaps.esjp.earchive.repository.Repository;
import org.efaps.ui.wicket.models.objects.UIStructurBrowser;
import org.efaps.ui.wicket.models.objects.UIStructurBrowser.ExecutionStatus;
import org.efaps.util.EFapsException;

/**
 * TODO description!
 *
 * @author jmox
 * @version $Id$
 */
@EFapsUUID("c3967e69-82e1-4b16-9172-ef97fbeb3f54")
@EFapsRevision("$Rev$")
public class StructurBrowser implements EventExecution, INames
  {

  /**
   * @param _parameter Parameter
   * @throws EFapsException on error
   * @return Return
   */
  public Return execute(final Parameter _parameter) throws EFapsException {
    Return ret = null;
    final Map<?, ?> properties
                      = (Map<? , ?>) _parameter.get(ParameterValues.PROPERTIES);
    final UIStructurBrowser strBro
                    = (UIStructurBrowser) _parameter.get(ParameterValues.CLASS);
    final ExecutionStatus status = strBro.getExecutionStatus();
    if (status.equals(ExecutionStatus.EXECUTE)) {
      ret = internalExecute((String) properties.get("Types"),
          "true".equalsIgnoreCase((String) properties.get("ExpandChildTypes")));
    } else if (status.equals(ExecutionStatus.CHECKFORCHILDREN)) {
      ret = checkForChildren(_parameter.getInstance());
    } else if (status.equals(ExecutionStatus.ADDCHILDREN)) {
      ret = addChildren(_parameter.getInstance());
    } else if (status.equals(ExecutionStatus.SORT)) {
      ret = sort(strBro);
    }
    return ret;
  }

  /**
   * Method to get a list of instances the structurbrowser will be filled with.
   * @param _types              types
   * @param _expandChildTypes   must the query be expanded
   * @return  Return with instances
   * @throws EFapsException on error
   */
  private Return internalExecute(final String _types,
                                 final boolean _expandChildTypes)
      throws EFapsException {
    final Return ret = new Return();

    final SearchQuery query = new SearchQuery();
    query.setQueryTypes(_types);
    query.setExpandChildTypes(_expandChildTypes);
    query.addSelect("OID");
    query.execute();

    final List<List<Object[]>> list = new ArrayList<List<Object[]>>();
    while (query.next()) {
      final List<Object[]> instances = new ArrayList<Object[]>(1);
      instances.add(new Object[] { Instance.get((String) query.get("OID")),
          null });
      list.add(instances);
    }
    ret.put(ReturnValues.VALUES, list);
    return ret;
  }

  /**
   * Method to check if an instance has children. It is used in the tree to
   * determine if a "plus" to open the children must be rendered.
   *
   * @param _instance   Instance to check for children
   * @return  Return with true or false
   * @throws EFapsException on error
   */
  private Return checkForChildren(final Instance _instance)
      throws EFapsException {
    final Return ret = new Return();
    Long nodeid = null;
     if ("eArchive_Repository".equals(_instance.getType().getName())) {
      nodeid = Node.getRootNodeFromDB(new Repository(_instance)).getId();
    } else {
      nodeid =  _instance.getId();
    }
    final SearchQuery query = new SearchQuery();
    query.setQueryTypes("eArchive_Node2Node");
    query.setExpandChildTypes(true);
    query.addWhereExprEqValue("Parent", nodeid);
    query.execute();

    if (query.next()) {
      ret.put(ReturnValues.TRUE, true);
     }
    return ret;
  }

  /**
   * Method to add the children to an instance. It is used to expand the
   * children of a node in the tree.
   *
   * @param _instance    Instance the children must be retrieved for.
   * @return Return with instances
   * @throws EFapsException on error
   */
  private Return addChildren(final Instance _instance) throws EFapsException {
    final Return ret = new Return();
    Node node = null;
    if ("eArchive_Repository".equals(_instance.getType().getName())) {
      node = Node.getRootNodeFromDB(new Repository(_instance));
    }
    final String parentInstanceKey;
    final long parentId;
    if (node == null) {
      parentId = _instance.getId();
      parentInstanceKey = _instance.getKey();
    } else {
      parentId = node.getId();
      parentInstanceKey = node.getHistoryId() + SEPERATOR_IDS
                                                            + node.getCopyId();
    }
    final SearchQuery query = new SearchQuery();
    query.setQueryTypes("eArchive_Node2NodeView");
    query.setExpandChildTypes(true);
    query.addWhereExprEqValue("Parent", parentId);
    query.addSelect("NodeType");
    query.addSelect("Child");
    query.addSelect("HistoryId");
    query.addSelect("CopyId");
    query.execute();

    final List<List<Object[]>> lists = new ArrayList<List<Object[]>>();

    while (query.next()) {
      final List<Object[]> instances = new ArrayList<Object[]>(1);
      final StringBuilder instanceKey = new StringBuilder()
        .append(parentInstanceKey).append(SEPERATOR_INSTANCE)
        .append(query.get("HistoryId")).append(".").append(query.get("CopyId"));

      instances.add(new Object[] {
          Instance.get(Type.get((Long) query.get("NodeType")),
          ((Long) query.get("Child")).toString(),
          instanceKey.toString()),
          null });
      lists.add(instances);
    }
    ret.put(ReturnValues.VALUES, lists);
    return ret;
  }

  /**
   * Method to sort the values of the StructurBrowser.
   *
   * @param _structurBrowser _sructurBrowser to be sorted
   * @return empty Return;
   */
  private Return sort(final UIStructurBrowser _structurBrowser) {
    Collections.sort(_structurBrowser.getChilds(),
                     new Comparator<UIStructurBrowser>() {

      public int compare(final UIStructurBrowser _structurBrowser1,
                         final UIStructurBrowser _structurBrowser2) {

        final String value1 = getSortString(_structurBrowser1);
        final String value2 = getSortString(_structurBrowser2);

        return value1.compareTo(value2);
      }

      private String getSortString(final UIStructurBrowser _structurBrowser) {
        final StringBuilder ret = new StringBuilder();
        try {
          if (_structurBrowser.getInstance() != null) {
            final Type type = _structurBrowser.getInstance().getType();
            if (type.equals(Type.get(TYPE_NODEDIRECTORY))) {
              ret.append(0);
            } else if (type.equals(Type.get(TYPE_NODEFILE))) {
              ret.append(1);
            }
          }
        } catch (final EFapsException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        ret.append(_structurBrowser.getLabel());
        return ret.toString();
      }
    });
    return new Return();
  }

}
