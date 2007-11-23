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
 * Revision:        $Rev:1510 $
 * Last Changed:    $Date:2007-10-18 09:35:40 -0500 (Thu, 18 Oct 2007) $
 * Last Changed By: $Author:jmox $
 */

package org.efaps.ui.wicket.models;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import org.apache.wicket.RestartResponseException;

import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.ui.AbstractCommand;
import org.efaps.admin.ui.Image;
import org.efaps.admin.ui.AbstractMenu;
import org.efaps.beans.ValueList;
import org.efaps.beans.valueparser.ValueParser;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.ui.wicket.pages.error.ErrorPage;

/**
 * @author tmo
 * @version $Id:MenuItemModel.java 1510 2007-10-18 14:35:40Z jmox $
 */
public class MenuItemModel extends AbstractModel {

  // ///////////////////////////////////////////////////////////////////////////
  // instance variables

  private static final long serialVersionUID = 505704924081527139L;

  /** Url to the image of this menu item. */
  private String image;

  /** Label of this menu item. */
  private String label;

  /** Description of this menu item. */
  private String description;

  private String reference;

  /**
   * All childs of this menu item.
   */
  private final List<MenuItemModel> childs = new ArrayList<MenuItemModel>();

  /** Url of this menu item. */
  private String url;

  private boolean header = false;

  private boolean defaultSelected = false;

  private boolean askUser = false;

  private int windowWidth;

  private int windowHeight;

  private boolean stepInto;

  private DefaultMutableTreeNode ancestor;

  /**
   * This is the getter method for the instance variable {@link #stepInto}.
   *
   * @return value of instance variable {@link #stepInto}
   */
  public boolean isStepInto() {
    return this.stepInto;
  }

  /**
   * This is the setter method for the instance variable {@link #stepInto}.
   *
   * @param stepInto
   *                the stepInto to set
   */
  public void setStepInto(boolean stepInto) {
    this.stepInto = stepInto;
  }

  public MenuItemModel(final UUID _uuid) {
    this(_uuid, null);
  }

  // ///////////////////////////////////////////////////////////////////////////
  // constructors / destructors

  public MenuItemModel(final UUID _uuid, final String _oid) {
    super(_uuid, _oid);
    initialise();
  }

  private void initialise() {
    AbstractCommand _command = super.getCommand();
    this.image = _command.getIcon();
    this.reference = _command.getReference();
    this.askUser = _command.isAskUser();
    this.windowHeight = _command.getWindowHeight();
    this.windowWidth = _command.getWindowWidth();
    this.defaultSelected = _command.isDefaultSelected();
    this.description = "";
    this.label = "";

    try {
      String label = DBProperties.getProperty(_command.getLabel());

      if (super.getOid() != null) {
        SearchQuery query = new SearchQuery();
        query.setObject(super.getOid());
        ValueParser parser = new ValueParser(new StringReader(label));
        ValueList list = parser.ExpressionString();
        list.makeSelect(query);
        if (query.selectSize() > 0) {
          query.execute();
          if (query.next()) {
            label = list.makeString(query);
          }
          query.close();
        }
      }
      this.label = label;

      if (_command instanceof AbstractMenu) {
        for (AbstractCommand subCmd : ((AbstractMenu) _command).getCommands()) {
          if (subCmd.hasAccess()) {
            this.childs
                .add(new MenuItemModel(subCmd.getUUID(), super.getOid()));
          }
        }
      }
    } catch (Exception e) {
      throw new RestartResponseException(new ErrorPage(e));
    }
  }

  public String getImage() {
    return this.image;
  }

  public String getTypeImage() {
    String ret = null;
    if (super.getOid() != null) {
      final Image image =
          Image.getTypeIcon(new Instance(super.getOid()).getType());
      if (image != null) {
        ret = image.getUrl();
      }
    }
    return ret;
  }

  public void setURL(String _url) {
    this.url = _url;
  }

  public List<MenuItemModel> getChilds() {
    return this.childs;
  }

  public boolean hasChilds() {
    return !this.childs.isEmpty();
  }

  public String getLabel() {
    return this.label;
  }

  /**
   * This is the getter method for the instance variable {@link #reference}.
   *
   * @return value of instance variable {@link #reference}
   */

  public String getReference() {
    return this.reference;
  }

  /**
   * This is the getter method for the instance variable {@link #url}.
   *
   * @return value of instance variable {@link #url}
   */

  public String getUrl() {
    return this.url;
  }

  /**
   * This is the setter method for the instance variable {@link #url}.
   *
   * @param url
   *                the url to set
   */
  public void setUrl(String url) {
    this.url = url;
  }

  /**
   * This is the getter method for the instance variable {@link #description}.
   *
   * @return value of instance variable {@link #description}
   */

  public String getDescription() {
    return this.description;
  }

  /**
   * This is the getter method for the instance variable {@link #header}.
   *
   * @return value of instance variable {@link #header}
   */

  public boolean isHeader() {
    return this.header;
  }

  /**
   * This is the setter method for the instance variable {@link #header}.
   *
   * @param header
   *                the header to set
   */
  public void setHeader(boolean header) {
    this.header = header;
  }

  /**
   * This is the getter method for the instance variable
   * {@link #defaultSelected}.
   *
   * @return value of instance variable {@link #defaultSelected}
   */
  public boolean isDefaultSelected() {
    return this.defaultSelected;
  }

  /**
   * This is the getter method for the instance variable {@link #askUser}.
   *
   * @return value of instance variable {@link #askUser}
   */

  public boolean isAskUser() {
    return this.askUser;
  }

  /**
   * This is the getter method for the instance variable {@link #windowWidth}.
   *
   * @return value of instance variable {@link #windowWidth}
   */

  public int getWindowWidth() {
    return this.windowWidth;
  }

  /**
   * This is the getter method for the instance variable {@link #windowHeight}.
   *
   * @return value of instance variable {@link #windowHeight}
   */

  public int getWindowHeight() {
    return this.windowHeight;
  }

  @Override
  public void resetModel() {

  }

  /**
   * get the TreeModel used in the Component to construct the actuall tree
   *
   * @see #addNode(DefaultMutableTreeNode, List)
   * @return TreeModel of this StructurBrowseModel
   */
  public TreeModel getTreeModel() {
    DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(this);
    this.setHeader(true);
    addNode(rootNode, this.childs);
    TreeModel model = new DefaultTreeModel(rootNode);
    return model;
  }

  public DefaultMutableTreeNode getNode() {
    DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(this);
    this.setHeader(true);
    addNode(rootNode, this.childs);
    return rootNode;
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
  private void addNode(DefaultMutableTreeNode parent, List<MenuItemModel> childs) {
    for (int i = 0; i < childs.size(); i++) {
      DefaultMutableTreeNode childNode =
          new DefaultMutableTreeNode(childs.get(i));
      parent.add(childNode);
      if (childs.get(i).hasChilds()) {
        addNode(childNode, childs.get(i).childs);
      }
    }
  }

  public void setAncestor(final DefaultMutableTreeNode _node) {
    this.ancestor = _node;
  }

  /**
   * This is the getter method for the instance variable {@link #ancestor}.
   *
   * @return value of instance variable {@link #ancestor}
   */
  public DefaultMutableTreeNode getAncestor() {
    return this.ancestor;
  }

  // ///////////////////////////////////////////////////////////////////////////

}
