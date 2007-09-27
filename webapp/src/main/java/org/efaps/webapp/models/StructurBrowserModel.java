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

package org.efaps.webapp.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import org.apache.wicket.PageParameters;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.ui.FieldDefinition;
import org.efaps.admin.datamodel.ui.FieldValue;
import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.ui.CommandAbstract;
import org.efaps.admin.ui.Field;
import org.efaps.admin.ui.Table;
import org.efaps.db.Instance;
import org.efaps.db.ListQuery;
import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;
import org.efaps.webapp.models.TableModel.SortDirection;

public class StructurBrowserModel extends AbstractModel {

  private static final long serialVersionUID = 1L;

  /**
   * The instance variable stores the UUID for the table which must be shown.
   *
   * @see #getTable
   */
  private UUID tableuuid;

  private final List<StructurBrowserModel> childs =
      new ArrayList<StructurBrowserModel>();

  private final List<String> columns = new ArrayList<String>();

  private String label;

  private String browserFieldName;

  private final List<HeaderModel> headers = new ArrayList<HeaderModel>();

  private final SortDirection sortDirection = SortDirection.NONE;

  private final boolean root;

  private boolean parent;

  public StructurBrowserModel(PageParameters _parameters) {
    super(_parameters);
    initialise();
    this.root = true;
  }

  private StructurBrowserModel(final UUID _commandUUID, final String _oid) {
    super(_commandUUID, _oid);
    this.root = false;
    initialise();
  }

  private void initialise() {
    CommandAbstract command = getCommand();
    if (command != null) {
      this.tableuuid = command.getTargetTable().getUUID();
      this.browserFieldName = command.getProperty("TargetStructurBrowserField");
    }
  }

  public void execute() {
    List<Return> ret;
    try {
      ret =
          getCommand().executeEvents(EventType.UI_TABLE_EVALUATE,
              ParameterValues.INSTANCE, new Instance(super.getOid()));

      List<List<Instance>> lists =
          (List<List<Instance>>) ret.get(0).get(ReturnValues.VALUES);

      internalExecute(lists);
    } catch (EFapsException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private void internalExecute(List<List<Instance>> _lists) {
    try {
      List<Instance> instances = new ArrayList<Instance>();
      Map<Instance, List<Instance>> instMapper =
          new HashMap<Instance, List<Instance>>();
      for (List<Instance> oneList : _lists) {
        Instance inst = oneList.get(oneList.size() - 1);
        instances.add(inst);
        instMapper.put(inst, oneList);
      }

      // evaluate for all expressions in the table
      ListQuery query = new ListQuery(instances);
      for (Field field : this.getTable().getFields()) {
        if (field.getExpression() != null) {
          query.addSelect(field.getExpression());
        }
        if (field.getAlternateOID() != null) {
          query.addSelect(field.getAlternateOID());
        }
        if (this.root) {
          this.headers.add(new HeaderModel(field, this.sortDirection));
        }
      }

      query.execute();
      Attribute attr = null;
      while (query.next()) {
        Instance instance = query.getInstance();
        StringBuilder oids = new StringBuilder();
        boolean first = true;
        for (Instance oneInstance : instMapper.get(instance)) {
          if (first) {
            first = false;
          } else {
            oids.append("|");
          }
          oids.append(oneInstance.getOid());
        }
        String strValue = "";

        StructurBrowserModel child =
            new StructurBrowserModel(super.getCommandUUID(), instance.getOid());
        this.childs.add(child);
        for (Field field : this.getTable().getFields()) {
          Object value = null;

          if (field.getExpression() != null) {
            value = query.getValue(field.getExpression());
            attr = query.getAttribute(field.getExpression());
          }

          FieldValue fieldvalue =
              new FieldValue(new FieldDefinition("egal", field), attr, value,
                  instance);
          if (value != null) {
            if (this.isCreateMode() && field.isEditable()) {
              strValue = fieldvalue.getCreateHtml();
            } else if (this.isEditMode() && field.isEditable()) {
              strValue = fieldvalue.getEditHtml();
            } else {
              strValue = fieldvalue.getViewHtml();
            }
          } else {
            strValue = "";
          }

          if (field.getName().equals(this.browserFieldName)) {
            child.setLabel(strValue);
            child.setParent(checkForChilds(instance));
          }
          child.getColumns().add(strValue);

        }

      }

    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  protected void setParent(boolean _parent) {
    this.parent = _parent;
  }

  private boolean checkForChilds(final Instance _instance) {
    SearchQuery query = new SearchQuery();
    boolean ret = false;
    try {
      query.setExpand(_instance, "TeamWork_Abstract\\ParentCollectionLink");
      query.execute();
      if (query.next()) {
        ret = true;
      }

    } catch (EFapsException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return ret;

  }

  @Override
  public void resetModel() {
  }

  /**
   * This is the getter method for the instance variable {@link #table}.
   *
   * @return value of instance variable {@link #table}
   * @see #table
   */
  public Table getTable() {
    return Table.get(this.tableuuid);
  }

  public boolean hasChilds() {
    return !this.childs.isEmpty();
  }

  public boolean isParent() {
    return this.parent;
  }

  public TreeModel getTreeModel() {
    TreeModel model = null;
    DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(this);
    add(rootNode, this.childs);
    model = new DefaultTreeModel(rootNode);
    return model;
  }

  private void add(DefaultMutableTreeNode parent,
                   List<StructurBrowserModel> childs) {
    for (int i = 0; i < childs.size(); i++) {
      DefaultMutableTreeNode childNode =
          new DefaultMutableTreeNode(childs.get(i));
      parent.add(childNode);
      if (childs.get(i).hasChilds()) {
        add(childNode, childs.get(i).childs);
      } else if (childs.get(i).isParent()) {
        childNode.add(new BogusNode());
      }

    }
  }

  public String getId() {
    String ret = this.getOid();
    return ret.substring(ret.indexOf(".") + 1);
  }

  public void addChildren(DefaultMutableTreeNode parent) {
    parent.removeAllChildren();
    SearchQuery query = new SearchQuery();
    try {
      query.setQueryTypes("TeamWork_Abstract");

      query.setExpandChildTypes(true);
      query.addSelect("OID");
      query.addWhereExprEqValue("ParentCollectionLink", this.getId());
      query.execute();

      List<List<Instance>> lists = new ArrayList<List<Instance>>();
      while (query.next()) {
        List<Instance> instances = new ArrayList<Instance>(1);
        instances.add(new Instance((String) query.get("OID")));
        lists.add(instances);
      }

      internalExecute(lists);
      add(parent, this.childs);
    } catch (EFapsException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  public String getColumnValue(final int _index) {
    return this.columns.get(_index);
  }

  /*
   * (non-Javadoc)
   *
   * @see org.apache.wicket.model.Model#toString()
   */
  @Override
  public String toString() {
    return this.label;
  }

  /**
   * This is the getter method for the instance variable {@link #label}.
   *
   * @return value of instance variable {@link #label}
   */

  public String getLabel() {
    return this.label;
  }

  /**
   * This is the setter method for the instance variable {@link #label}.
   *
   * @param label
   *                the label to set
   */
  public void setLabel(String label) {
    this.label = label;
  }

  /**
   * This is the getter method for the instance variable {@link #columns}.
   *
   * @return value of instance variable {@link #columns}
   */

  public List<String> getColumns() {
    return this.columns;
  }

  public List<HeaderModel> getHeaders() {
    return this.headers;
  }

  /**
   * This is the getter method for the instance variable
   * {@link #browserFieldName}.
   *
   * @return value of instance variable {@link #browserFieldName}
   */

  public String getBrowserFieldName() {
    return this.browserFieldName;
  }

  public class BogusNode extends DefaultMutableTreeNode {

    private static final long serialVersionUID = 1L;

  }
}
