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

package org.efaps.ui.wicket.models.objects;

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
import org.efaps.admin.datamodel.ui.FieldValue;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.ui.AbstractCommand;
import org.efaps.admin.ui.Image;
import org.efaps.admin.ui.Menu;
import org.efaps.admin.ui.Table;
import org.efaps.admin.ui.AbstractCommand.SortDirection;
import org.efaps.admin.ui.field.Field;
import org.efaps.beans.ValueList;
import org.efaps.beans.valueparser.ValueParser;
import org.efaps.db.Instance;
import org.efaps.db.ListQuery;
import org.efaps.db.SearchQuery;
import org.efaps.ui.wicket.pages.error.ErrorPage;
import org.efaps.util.EFapsException;
import org.efaps.util.RequestHandler;

/**
 * This class is used to provide the Model for the StructurBrowser for eFpas.
 * <br>
 * It is used in tow different cases. In one case it is a TreeTable, where the
 * Table will be provided with additional information in columns. In the other
 * case a Tree only.<br>
 * The concept of this class is to provide a Model which connects through the
 * eFaps-kernel to the eFaps-DataBase and turn it to a Standard TreeModel from
 * <code>javax.swing.tree</code>. which will be used from the Component to
 * render the Tree and the Table. This leads to a very similar behavior of the
 * WebApp GUI to a swing GUI. <br>
 * This model works asyncron. That means only the actually in the GUI rendered
 * Nodes (and Columns) will be retrieved from the eFaps-DataBase. The next level
 * in a tree will be retrieved on the expand of a TreeNode. To achieve this and
 * to be able to render expand-links for every node it will only be checked if
 * it is a potential parent (if it has childs). In the case of expanding this
 * Node the children will be retrieved and rendered.<br>
 * To access the eFaps-Database a esjp is used, which will be used in three
 * different cases. To distinguish the use of the esjp some extra Parameters
 * will be passed to the esjp when calling it.
 *
 * @author jmox
 * @version $Id$
 */
public class UIStructurBrowser extends AbstractUIObject {

  /**
   * Needed for serialization.
   */
  private static final long serialVersionUID = 1L;

  /**
   * The instance variable stores the UUID for the table which must be shown.
   *
   * @see #getTable
   */
  private UUID tableuuid;

  /**
   * This instance variable holds the childs of this StructurBrowserModel.
   */
  private final List<UIStructurBrowser> childs
                                          = new ArrayList<UIStructurBrowser>();

  /**
   * Holds the columns in case of a TableTree.
   */
  private final List<String> columns = new ArrayList<String>();

  /**
   * Holds the label of the Node wich will be presented
   * in the GUI.
   *
   * @see #toString()
   * @see #getLabel()
   * @see #setLabel(String)
   * @see #requeryLabel()
   */
  private String label;

  /**
   * Holds the Name of the Field the StructurBrowser
   * should be in, in case of a TableTree.
   */
  private String browserFieldName;

  /**
   * Holds the headers for the Table, in case of a
   * TableTree.
   */
  private final List<UITableHeader> headers = new ArrayList<UITableHeader>();

  /**
   * Holds the SortDirection for the Headers, (right now
   * it is final but might be changed later).
   */
  private final SortDirection sortDirection = SortDirection.NONE;

  /**
   * This instance variable holds, if this StructurBrowserModel is the Root of a
   * tree.
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
   * Holds the Value for the Label as it is difined in
   * the DBProperties.
   */
  private String valueLabel;

  /**
   * Contains the url for the Image that will be presented
   * in GUI.
   */
  private String image;

  /**
   * this instrance variable is used as a <b>TriState</b>, to determine if the
   * Model should show the direction of this Model as Child in comparisment to
   * the parent.<br>
   * The tristate is used as follows:
   * <li><b>null</b>: no direction will be shown</li>
   * <li><b>true</b>: an arrow showing downwards, will be rendered</li>
   * <li><b>false</b>: an arrow showing upwards, will be rendered</li>
   */
  private Boolean direction = null;

  /**
   *  Constructor.
   *
   * @param _parameters   Page parameters
   */
  public UIStructurBrowser(final PageParameters _parameters) {
    super(_parameters);
    this.root = true;
    initialise();
  }

  /**
   * Standart constructor, if called this StructurBrowserModel will be defined
   * as root.
   *
   * @param _commandUUID  UUID of the calling command
   * @param _oid          oid
   *
   */
  public UIStructurBrowser(final UUID _commandUUID, final String _oid) {
    this(_commandUUID, _oid, true);
  }

  /**
   * Internal constructor, it is used to set that this StructurBrowserModel is
   * not a root.
   *
   * @param _commandUUID  UUID of the command
   * @param _oid          OID
   * @param _root         is this STrtucturbrowser the root
   */
  private UIStructurBrowser(final UUID _commandUUID, final String _oid,
                            final boolean _root) {
    super(_commandUUID, _oid);
    this.root = _root;
    initialise();
  }



  /**
   * Method used to initialize this StructurBrowserModel.
   */
  private void initialise() {
    final AbstractCommand command = getCommand();
    if ((command != null) && (command.getTargetTable() != null)) {
      this.tableuuid = command.getTargetTable().getUUID();
      this.browserFieldName = command.getProperty("TargetStructurBrowserField");
    } else {
      if ("true".equals(command.getProperty("TargetStructurBrowser"))) {
        final String tmplabel
                = Menu.getTypeTreeMenu(getCallInstance().getType()).getLabel();
        this.valueLabel = DBProperties.getProperty(tmplabel);
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
      if (this.tableuuid == null) {
        final List<List<Object[]>> list = new ArrayList<List<Object[]>>();
        final List<Object[]> instances = new ArrayList<Object[]>(1);
        instances.add(new Object[] { getCallInstance(), null });
        list.add(instances);
        executeTree(list);
      } else {
        ret =
            getCommand().executeEvents(EventType.UI_TABLE_EVALUATE,
                ParameterValues.OTHERS, "execute");
        final List<List<Object[]>> lists =
            (List<List<Object[]>>) ret.get(0).get(ReturnValues.VALUES);
        executeTreeTable(lists);
      }
    } catch (final EFapsException e) {
      throw new RestartResponseException(new ErrorPage(e));
    }
  }

  /**
   * This method is called in case of a Tree from the {@link #execute()}method
   * to fill this StructurBrowserModel with live.
   *
   * @param _lists  List of Object
   */
  private void executeTree(final List<List<Object[]>> _lists) {
    try {
      final List<Instance> instances = new ArrayList<Instance>();
      final Map<Instance, List<Object[]>> instMapper =
          new HashMap<Instance, List<Object[]>>();
      for (final List<Object[]> oneList : _lists) {
        final Object[] inst = oneList.get(oneList.size() - 1);
        instances.add((Instance) inst[0]);
        instMapper.put((Instance) inst[0], oneList);
      }

      final ValueParser parser =
          new ValueParser(new StringReader(this.valueLabel));
      final ValueList valuelist = parser.ExpressionString();
      final ListQuery query = new ListQuery(instances);
      valuelist.makeSelect(query);
      query.execute();
      while (query.next()) {
        Object value = null;
        final Instance instance = query.getInstance();
        value = valuelist.makeString(getCallInstance(), query);
        final UIStructurBrowser child =
            new UIStructurBrowser(Menu.getTypeTreeMenu(instance.getType())
                .getUUID(), instance.getOid(), false);
        this.childs.add(child);
        child.setDirection((Boolean) ((instMapper.get(instance).get(0))[1]));
        child.setLabel(value.toString());
        child.setParent(checkForChildren(instance));
        child.setImage(Image.getTypeIcon(instance.getType()).getUrl());
      }
    } catch (final Exception e) {
      throw new RestartResponseException(new ErrorPage(e));
    }
    super.setInitialised(true);

  }

  /**
   * This method is called in case of a TreeTable from the
   * {@link #execute()} method
   * to fill this StructurBrowserModel with live.
   *
   * @param _lists List of Objects
   */
  private void executeTreeTable(final List<List<Object[]>> _lists) {
    try {
      final List<Instance> instances = new ArrayList<Instance>();
      final Map<Instance, List<Object[]>> instMapper =
          new HashMap<Instance, List<Object[]>>();
      for (final List<Object[]> oneList : _lists) {
        final Object[] inst = oneList.get(oneList.size() - 1);
        instances.add((Instance) inst[0]);
        instMapper.put((Instance) inst[0], oneList);
      }

      // evaluate for all expressions in the table
      final ListQuery query = new ListQuery(instances);
      for (final Field field : getTable().getFields()) {
        if (field.getExpression() != null) {
          query.addSelect(field.getExpression());
        }
        if (field.getAlternateOID() != null) {
          query.addSelect(field.getAlternateOID());
        }
        if (this.root) {
          this.headers.add(new UITableHeader(field, this.sortDirection));
        }
      }

      query.execute();
      Attribute attr = null;
      while (query.next()) {
        final Instance instance = query.getInstance();
        final StringBuilder oids = new StringBuilder();
        boolean first = true;
        for (final Object[] oneInstance : instMapper.get(instance)) {
          if (first) {
            first = false;
          } else {
            oids.append("|");
          }
          oids.append(((Instance) oneInstance[0]).getOid());
        }

        String strValue = "";

        final UIStructurBrowser child = new UIStructurBrowser(getCommandUUID(),
                                                              instance.getOid(),
                                                              false);
        this.childs.add(child);
        child.setDirection((Boolean) ((instMapper.get(instance).get(0))[1]));
        for (final Field field : getTable().getFields()) {
          Object value = null;

          if (field.getExpression() != null) {
            value = query.get(field.getExpression());
            attr = query.getAttribute(field.getExpression());
          }

          final FieldValue fieldvalue =
                                  new FieldValue(field, attr, value, instance);
          if (value != null) {
            if (isCreateMode() && field.isEditable()) {
              strValue = fieldvalue.getCreateHtml(getCallInstance(), instance);
            } else if (isEditMode() && field.isEditable()) {
              strValue = fieldvalue.getEditHtml(getCallInstance(), instance);
            } else {
              strValue = fieldvalue.getViewHtml(getCallInstance(), instance);
            }
          } else {
            strValue = "";
          }

          if (field.getName().equals(this.browserFieldName)) {
            child.setLabel(strValue);
            child.setParent(checkForChildren(instance));
            child.setImage(Image.getTypeIcon(instance.getType()).getUrl());
          }
          child.getColumns().add(strValue);
        }
      }

    } catch (final Exception e) {
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
  public void setParent(final boolean _parent) {
    this.parent = _parent;
  }

  /**
   * This method is used to check if a node has potential children.
   *
   * @param _instance
   *                Instance of a Node to be checked
   * @return true if this Node has children, else false
   */
  private boolean checkForChildren(final Instance _instance) {

    try {
      final List<Return> ret =
          getCommand().executeEvents(EventType.UI_TABLE_EVALUATE,
                                      ParameterValues.INSTANCE,
                                      _instance,
                                      ParameterValues.OTHERS,
                                      "checkForChildren");
      return (ret.isEmpty() ? false
                            : ret.get(0).get(ReturnValues.TRUE) != null);
    } catch (final EFapsException e) {
      throw new RestartResponseException(new ErrorPage(e));
    }
  }

  /**
   * Method to reset the Model.
   *
   * @see org.efaps.ui.wicket.models.AbstractModel#resetModel()
   */
  @Override
  public void resetModel() {
    this.childs.clear();
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
   * Has this StructurBrowserModel childs.
   *
   * @return true if has children, else false
   */
  public boolean hasChilds() {
    return !this.childs.isEmpty();
  }

  /**
   * Get the TreeModel used in the Component to construct the actual tree.
   *
   * @see #addNode(DefaultMutableTreeNode, List)
   * @return TreeModel of this StructurBrowseModel
   */
  public TreeModel getTreeModel() {
    TreeModel model = null;
    final DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(this);
    addNode(rootNode, this.childs);
    model = new DefaultTreeModel(rootNode);
    return model;
  }

  /**
   * Recursive method used to fill the TreeModel.
   *
   * @see #getTreeModel()
   * @param _parent   ParentNode children should be added
   * @param _childs   to be added as childs
   */
  private void addNode(final DefaultMutableTreeNode _parent,
                       final List<UIStructurBrowser> _childs) {
    for (int i = 0; i < _childs.size(); i++) {
      final DefaultMutableTreeNode childNode =
          new DefaultMutableTreeNode(_childs.get(i));
      _parent.add(childNode);
      if (_childs.get(i).hasChilds()) {
        addNode(childNode, _childs.get(i).childs);
      } else if (_childs.get(i).isParent()) {
        childNode.add(new BogusNode());
      }
    }
  }

  /**
   * This method should be called to add children to a Node in the Tree.<br>
   * e.g. in a standard implementation the children would be added to the Tree
   * on the expand-Event of the tree. The children a retrieved from an esjp with
   * the EventType UI_TABLE_EVALUATE. To differ the different methods which can
   * call the same esjp, this method adds the ParameterValues.OTHERS with
   * "addChildren".
   *
   * @param _parent  the DefaultMutableTreeNode the new children should be added
   */
  @SuppressWarnings("unchecked")
  public void addChildren(final DefaultMutableTreeNode _parent) {
    _parent.removeAllChildren();
    List<Return> ret;
    try {
      ret =
          getCommand().executeEvents(EventType.UI_TABLE_EVALUATE,
              ParameterValues.INSTANCE, getCallInstance(),
              ParameterValues.OTHERS, "addChildren");
      final List<List<Object[]>> lists =
          (List<List<Object[]>>) ret.get(0).get(ReturnValues.VALUES);

      if (this.tableuuid == null) {
        executeTree(lists);
      } else {
        executeTreeTable(lists);
      }
      addNode(_parent, this.childs);
    } catch (final EFapsException e) {
      throw new RestartResponseException(new ErrorPage(e));
    }
  }

  /**
   * Get the Value of a Column identified by the index of the Column.
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
   * @param _label    the label to set
   */
  private void setLabel(final String _label) {
    this.label = _label;
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

  public List<UITableHeader> getHeaders() {
    return this.headers;
  }

  /**
   * This is the getter method for the instance variable {@link #image}.
   *
   * @return value of instance variable {@link #image}
   */

  public String getImage() {
    return this.image;
  }

  /**
   * This is the setter method for the instance variable {@link #image}.
   *
   * @param _url   the url of the image to set
   */
  private void setImage(final String _url) {
    if (_url != null) {
      this.image = RequestHandler.replaceMacrosInUrl(_url);
    }
  }

  /**
   * This is the getter method for the instance variable {@link #direction}.
   *
   * @return value of instance variable {@link #direction}
   */
  public Boolean getDirection() {
    return this.direction;
  }

  /**
   * This is the setter method for the instance variable {@link #direction}.
   *
   * @param _direction    the direction to set
   */
  public void setDirection(final Boolean _direction) {
    this.direction = _direction;
  }

  /**
   * (non-Javadoc).
   *
   * @see org.apache.wicket.model.Model#toString()
   * @return label
   */
  @Override
  public String toString() {
    return this.label;
  }

  /**
   * This method is updating the Label, by querying the eFaps-DataBase.
   */
  public void requeryLabel() {
    try {
      final ValueParser parser
                          = new ValueParser(new StringReader(this.valueLabel));
      final ValueList valList = parser.ExpressionString();

      final SearchQuery query = new SearchQuery();
      query.setObject(getCallInstance());
      valList.makeSelect(query);
      query.execute();
      if (query.next()) {
        setLabel(valList.makeString(new Instance(getOid()), query).toString());
      }
    } catch (final Exception e) {
      throw new RestartResponseException(new ErrorPage(e));
    }
  }

  /**
   * Method to add a new BogusNode to the given Node.
   *
   * @param _parent Parent a BogusNode should be added
   */
  public void addBogusNode(final DefaultMutableTreeNode _parent) {
    _parent.add(new BogusNode());
  }

  /**
   * This class is used to add a ChildNode under a ParentNode, if the ParentNode
   * actually has some children. By using this class it then can very easy be
   * distinguished between Nodes which where expanded and Nodes which still need
   * to be expanded.
   *
   * @author jmox
   * @version $Id$
   */
  public class BogusNode extends DefaultMutableTreeNode {

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

  }
}
