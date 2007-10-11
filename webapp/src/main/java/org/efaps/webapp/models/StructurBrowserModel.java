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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import org.apache.wicket.PageParameters;
import org.apache.wicket.RestartResponseException;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.ui.FieldDefinition;
import org.efaps.admin.datamodel.ui.FieldValue;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.ui.CommandAbstract;
import org.efaps.admin.ui.Field;
import org.efaps.admin.ui.Menu;
import org.efaps.admin.ui.Table;
import org.efaps.beans.ValueList;
import org.efaps.beans.valueparser.ValueParser;
import org.efaps.db.Instance;
import org.efaps.db.ListQuery;
import org.efaps.util.EFapsException;
import org.efaps.webapp.models.TableModel.SortDirection;
import org.efaps.webapp.pages.ErrorPage;

/**
 * This class is used to provide the Model for the StructurBrowser for eFpas.<br>
 * It is used in tow different cases. In one case it is a TreeTable, where the
 * Table will be provided with aditional information in columns. In the other
 * case a Tree only.<br>
 * The concept of this class is to provide a Model wich connects throught the
 * eFaps-kernel to the eFaps-DataBase and turn it to a Standart TreeModel from
 * <code>javax.swing.tree</code>. wich will be used from the Component to
 * render the Tree and the Table. This leads to a very similar behavior of the
 * WebApp GUI to a swing GUI. <br>
 * This model works asyncron. That means only the actually in the GUI rendered
 * Nodes (and Columns) will be retrieved from the eFaps-DataBase. The next level
 * in a tree will be retrieved on the expand of a TreeNode. To achieve this and
 * to be able to render expand-links for every node it will only be checked if
 * it is a potential parent (if it has childs). In the case of expanding this
 * Node the children will be retrieved and rendered.<br>
 * To access the eFaps-Database a esjp is used, wich will be used in three
 * different cases. To distinguish the use of the esjp some extra Parameters
 * will be passed to the esjp when calling it.
 *
 * @author jmox
 * @version $Id$
 */
public class StructurBrowserModel extends AbstractModel {

  private static final long serialVersionUID = 1L;

  /**
   * The instance variable stores the UUID for the table which must be shown.
   *
   * @see #getTable
   */
  private UUID tableuuid;

  /**
   * this instance variable holds the childs of this StructurBrowserModel
   */
  private final List<StructurBrowserModel> childs =
      new ArrayList<StructurBrowserModel>();

  /**
   * this instance variable holds the columns in case of a TableTree
   */
  private final List<String> columns = new ArrayList<String>();

  /**
   * this instance variable holds the label of the Node wich will be presented
   * in the GUI
   *
   * @see #toString()
   * @see #getLabel()
   * @see #setLabel(String)
   */
  private String label;

  /**
   * this instance variable holds the Name of the Field the StructurBrowser
   * should be in, in case of a TableTree
   */
  private String browserFieldName;

  /**
   * this instance variable holds the headers for the Table, in case of a
   * TableTree
   */
  private final List<HeaderModel> headers = new ArrayList<HeaderModel>();

  /**
   * this instance variable holds the SortDirection for the Headers, (right now
   * it is final but might be changed later)
   */
  private final SortDirection sortDirection = SortDirection.NONE;

  /**
   * this instance variable holds, if this StructurBrowserModel is the Root of a
   * tree
   */
  private final boolean root;

  /**
   * this instance variable holds if this StructurBrowserModel is a potential
   * parent, this is needed because, first it will be only determined if a node
   * is a potential parent, and later on the childs will be retrieved from the
   * eFpas-DataBase.
   *
   * @see #isParent()
   */
  private boolean parent;

  /**
   * this instance variable holds the Value for the Label as it is difined in
   * the DBProperties
   */
  private String valueLabel;

  /**
   * standart constructor, if called this StructurBrowserModel will be defined
   * as root
   *
   * @param _parameters
   *                PageParameters needed to initialise this
   *                StructurBrowserModel
   */
  public StructurBrowserModel(PageParameters _parameters) {
    super(_parameters);
    initialise();
    this.root = true;
  }

  /**
   * internal constructor, it is used to set that this StructurBrowserModel is
   * not a root
   *
   * @param _commandUUID
   * @param _oid
   */
  private StructurBrowserModel(final UUID _commandUUID, final String _oid) {
    super(_commandUUID, _oid);
    this.root = false;
    initialise();
  }

  /**
   * method used to initialise this StructurBrowserModel
   */
  private void initialise() {
    CommandAbstract command = getCommand();
    if (command != null && command.getTargetTable() != null) {
      this.tableuuid = command.getTargetTable().getUUID();
      this.browserFieldName = command.getProperty("TargetStructurBrowserField");
    } else {
      if ("true".equals(command.getProperty("TargetStructurBrowser"))) {
        String label =
            Menu.getTypeTreeMenu(new Instance(getOid()).getType()).getLabel();
        this.valueLabel = DBProperties.getProperty(label);
      }
    }

  }

  /**
   * This method should be called to actually execute this StructurBrowserModel,
   * that means to retrieve the values from the eFaps-DataBase, create the
   * TreeModel etc. This method actually calls depending if we have a Tree or a
   * TreeTabel the Methodes {@link #executeTree(List)} or
   * {@link #executeTreeTable(List)}
   *
   * @see #executeTree(List)
   * @see #executeTreeTable(List)
   */
  @SuppressWarnings("unchecked")
  public void execute() {
    List<Return> ret;
    try {
      if (this.tableuuid != null) {
        ret =
            getCommand().executeEvents(EventType.UI_TABLE_EVALUATE,
                ParameterValues.OTHERS, "execute");
        List<List<Instance>> lists =
            (List<List<Instance>>) ret.get(0).get(ReturnValues.VALUES);
        executeTreeTable(lists);
      } else {
        List<List<Instance>> list = new ArrayList<List<Instance>>();
        List<Instance> instances = new ArrayList<Instance>(1);
        instances.add(new Instance(getOid()));
        list.add(instances);
        executeTree(list);
      }
    } catch (EFapsException e) {
      throw new RestartResponseException(new ErrorPage(e));
    }
  }

  /**
   * This method is called in case of a Tree from the {@link #execute()}method
   * to fill this StructurBrowserModel with live
   *
   * @param _lists
   */
  private void executeTree(List<List<Instance>> _lists) {
    try {
      List<Instance> instances = new ArrayList<Instance>();
      Map<Instance, List<Instance>> instMapper =
          new HashMap<Instance, List<Instance>>();
      for (List<Instance> oneList : _lists) {
        Instance inst = oneList.get(oneList.size() - 1);
        instances.add(inst);
        instMapper.put(inst, oneList);
      }
      ValueParser parser = new ValueParser(new StringReader(this.valueLabel));
      ValueList valuelist = parser.ExpressionString();
      ListQuery query = new ListQuery(instances);
      valuelist.makeSelect(query);
      query.execute();
      while (query.next()) {
        Object value = null;
        Instance instance = query.getInstance();
        value = valuelist.makeString(query);
        StructurBrowserModel child =
            new StructurBrowserModel(super.getCommandUUID(), instance.getOid());
        this.childs.add(child);

        child.setLabel(value.toString());
        child.setParent(checkForChildren(instance));

      }
    } catch (Exception e) {
      throw new RestartResponseException(new ErrorPage(e));
    }
    super.setInitialised(true);

  }

  /**
   * This method is called in case of a TreeTable from the {@link #execute()}method
   * to fill this StructurBrowserModel with live
   *
   * @param _lists
   */
  private void executeTreeTable(List<List<Instance>> _lists) {
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
            value = query.get(field.getExpression());
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
            child.setParent(checkForChildren(instance));
          }
          child.getColumns().add(strValue);
        }
      }

    } catch (Exception e) {
      throw new RestartResponseException(new ErrorPage(e));
    }
    super.setInitialised(true);
  }

  /**
   * This is the getter method for the instance variable {@link #parent}.
   *
   * @return value of instance variable {@link #parent}
   */
  private boolean isParent() {
    return this.parent;
  }

  /**
   * This is the setter method for the instance variable {@link #parent}.
   *
   * @param _parent
   *                the parent to set
   */
  private void setParent(boolean _parent) {
    this.parent = _parent;
  }

  /**
   * this method is used to check if a node has potential children
   *
   * @param _instance
   *                Instance of a Node to be checked
   * @return true if this Node has children, else false
   */
  private boolean checkForChildren(final Instance _instance) {

    try {
      List<Return> ret =
          getCommand().executeEvents(EventType.UI_TABLE_EVALUATE,
              ParameterValues.INSTANCE, _instance, ParameterValues.OTHERS,
              "checkForChildren");
      return ret.get(0).get(ReturnValues.TRUE) != null;
    } catch (EFapsException e) {
      throw new RestartResponseException(new ErrorPage(e));
    }

  }

  @Override
  public void resetModel() {
    // not needed here
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

  /**
   * has this StructurBrowserModel childs
   *
   * @return
   */
  public boolean hasChilds() {
    return !this.childs.isEmpty();
  }

  /**
   * get the TreeModel used in the Component to construct the actuall tree
   *
   * @see #addNode(DefaultMutableTreeNode, List)
   * @return TreeModel of this StructurBrowseModel
   */
  public TreeModel getTreeModel() {
    TreeModel model = null;
    DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(this);
    addNode(rootNode, this.childs);
    model = new DefaultTreeModel(rootNode);
    return model;
  }

  /**
   * recursive method used to fill the TreeModel
   *
   * @see #getTreeModel()
   * @param parent
   *                ParentNode children schould be added
   * @param childs
   *                List<StructurBrowserModel>to be added as childs
   */
  private void addNode(DefaultMutableTreeNode parent,
                       List<StructurBrowserModel> childs) {
    for (int i = 0; i < childs.size(); i++) {
      DefaultMutableTreeNode childNode =
          new DefaultMutableTreeNode(childs.get(i));
      parent.add(childNode);
      if (childs.get(i).hasChilds()) {
        addNode(childNode, childs.get(i).childs);
      } else if (childs.get(i).isParent()) {
        childNode.add(new BogusNode());
      }
    }
  }

  /**
   * This method should be called to add children to a Node in the Tree.<br>
   * e.g. in a standart implementation the children would be added to the Tree
   * on the expand-Event of the tree. The children a retrieved from an esjp with
   * the EventType UI_TABLE_EVALUATE. To differ the different methods wich can
   * call the same esjp, this method adds the ParameterValues.OTHERS with
   * "addChildren".
   *
   * @param parent
   *                the DefaultMutableTreeNode the new Children schould be added
   */
  @SuppressWarnings("unchecked")
  public void addChildren(DefaultMutableTreeNode parent) {
    parent.removeAllChildren();
    List<Return> ret;
    try {
      ret =
          getCommand().executeEvents(EventType.UI_TABLE_EVALUATE,
              ParameterValues.INSTANCE, new Instance(super.getOid()),
              ParameterValues.OTHERS, "addChildren");
      List<List<Instance>> lists =
          (List<List<Instance>>) ret.get(0).get(ReturnValues.VALUES);

      if (this.tableuuid != null) {
        executeTreeTable(lists);
      } else {
        this.executeTree(lists);
      }
      addNode(parent, this.childs);
    } catch (EFapsException e) {
      throw new RestartResponseException(new ErrorPage(e));
    }
  }

  /**
   * get the Value of a Column identified by the index of the Column
   *
   * @param _index
   *                index of the Column
   * @return String with the Value of the Column
   */
  public String getColumnValue(final int _index) {
    return this.columns.get(_index);
  }

  /**
   * This is the setter method for the instance variable {@link #label}.
   *
   * @param label
   *                the label to set
   */
  private void setLabel(String label) {
    this.label = label;
  }

  /**
   * This is the getter method for the instance variable {@link #columns}.
   *
   * @return value of instance variable {@link #columns}
   */
  private List<String> getColumns() {
    return this.columns;
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

  /**
   * This is the getter method for the instance variable {@link #headers}.
   *
   * @return value of instance variable {@link #headers}
   */

  public List<HeaderModel> getHeaders() {
    return this.headers;
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
   * This class is used to add a ChildNode under a ParentNode, if the ParentNode
   * actually has some children. By using this class it then can very easy be
   * distinguished between Nodes wich where expanded and Nodes wich still need
   * to be expanded.
   */
  public class BogusNode extends DefaultMutableTreeNode {

    private static final long serialVersionUID = 1L;

  }

}
