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

package org.efaps.ui.wicket.components.tree;

import java.util.UUID;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Response;
import org.apache.wicket.extensions.markup.html.tree.table.AbstractRenderableColumn;
import org.apache.wicket.extensions.markup.html.tree.table.AbstractTreeColumn;
import org.apache.wicket.extensions.markup.html.tree.table.ColumnLocation;
import org.apache.wicket.extensions.markup.html.tree.table.IColumn;
import org.apache.wicket.extensions.markup.html.tree.table.IRenderable;
import org.apache.wicket.extensions.markup.html.tree.table.ColumnLocation.Alignment;
import org.apache.wicket.extensions.markup.html.tree.table.ColumnLocation.Unit;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import org.efaps.ui.wicket.models.StructurBrowserModel;
import org.efaps.ui.wicket.models.objects.UIStructurBrowser;

/**
 * @author jmox
 *
 * @version $Id$
 */
public class StructurBrowserTreeTablePanel extends Panel {

  /**
   * Needed for serialization.
   */
  private static final long serialVersionUID = 1L;

  /**
   * @param _wicketId   wicket id of this component
   * @param _uuid       uuid used to create a model
   * @param _oid        oid used to create a model
   */
  public StructurBrowserTreeTablePanel(final String _wicketId,
                                       final UUID _uuid, final String _oid) {
    this(_wicketId,
         new StructurBrowserModel(new UIStructurBrowser(_uuid, _oid)));
  }

  /**
   * @param _wicketId   wicket id of this component
   * @param _model      model for this component
   */
  public StructurBrowserTreeTablePanel(final String _wicketId,
                                       final IModel<UIStructurBrowser> _model) {
    super(_wicketId, _model);

    final UIStructurBrowser model
                            = (UIStructurBrowser) super.getDefaultModelObject();
    if (!model.isInitialised()) {
      model.execute();
    }

    final IColumn[] columns = new IColumn[model.getHeaders().size() + 1];

    columns[0] =
        new SelectColumn(new ColumnLocation(Alignment.LEFT, 16, Unit.PX), "");

    for (int i = 0; i < model.getHeaders().size(); i++) {
      if (model.getHeaders().get(i).getName().equals(
          model.getBrowserFieldName())) {
        columns[i + 1] =
            new TreeColumn(new ColumnLocation(Alignment.MIDDLE, 2,
              Unit.PROPORTIONAL), model.getHeaders().get(i).getLabel(), _model);
      } else {
        columns[i + 1] =
            new SimpleColumn(new ColumnLocation(Alignment.MIDDLE, 1,
                Unit.PROPORTIONAL), model.getHeaders().get(i).getLabel(), i);
      }
    }

    final StructurBrowserTreeTable tree =
       new StructurBrowserTreeTable("treeTable", model.getTreeModel(), columns);
    add(tree);
  }

  /**
   * Class for the column that contains the tree.
   */
  public class TreeColumn extends AbstractTreeColumn {

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Header for this column.
     */
    private final String header;

    /**
     * Model for this column.
     */
    private final IModel<UIStructurBrowser> model;

    /**
     * @param _location    column location
     * @param _header     header
     * @param _model      model
     */
    public TreeColumn(final ColumnLocation _location, final String _header,
                      final IModel<UIStructurBrowser> _model) {
      super(_location, _header);
      this.header = _header;
      this.model = _model;
    }

    /**
     * Render value for the node.
     * @param _node node to render
     * @return String with the value for the node
     */
    @Override
    public String renderNode(final TreeNode _node) {
      return _node.toString();
    }

    /**
     * Add the sortlink as a the header.
     * @param _parent      parent
     * @param _wicketId   wicket id for the sortlink
     * @return Component to be used as the header
     *
     */
    @Override
    public Component newHeader(final MarkupContainer _parent,
                               final String _wicketId) {
      return new SortHeaderColumnLink(_wicketId, this.header, this.model);
    }
  }

  /**
   * Class for the standard column.
   */
  public class SimpleColumn extends AbstractRenderableColumn {

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Index of this column.
     */
    private final int index;

    /**
     * @param _location   Location
     * @param _header     header
     * @param _index      index
     */
    public SimpleColumn(final ColumnLocation _location, final String _header,
                        final int _index) {
      super(_location, _header);
      this.index = _index;
      setContentAsTooltip(true);
    }

    /**
     * Method to get the value for the node.
     * @param _node node the value will be returned for
     * @return value for the node
     */
    @Override
    public String getNodeValue(final TreeNode _node) {
      String ret = "";
      ret = ((UIStructurBrowser) ((DefaultMutableTreeNode) _node)
              .getUserObject()).getColumnValue(this.index);
      return ret;
    }
  }

  /**
   * Class for the column containing a select box.
   */
  public class SelectColumn extends AbstractRenderableColumn {

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param _location   Location
     * @param _header     header
     */
    public SelectColumn(final ColumnLocation _location, final String _header) {
      super(_location, _header);
    }

    /**
     * Method to render a select box.
     * @param _node   Node the cell is rendered for
     * @param _level  level of the node
     * @return new Cell
     */
    @Override
    public IRenderable newCell(final TreeNode _node, final int _level) {
      return new IRenderable() {

        private static final long serialVersionUID = 1L;

        public void render(final TreeNode _node, final Response _response) {
          final String instanceKey =
              ((UIStructurBrowser) ((DefaultMutableTreeNode) _node)
                  .getUserObject()).getInstanceKey();
          final String checkbox =
              "<input "
                  + "type=\"checkbox\""
                  + "name=\"selectedRow\""
                  + "class=\"eFapsCheckboxCell\""
                  + "value=\""
                  + instanceKey
                  + "\"/>";

          _response.write(checkbox);
        }
      };
    }

    /**
     * Method to get the value for the node.
     *
     * @param _node node the value will be returned for
     * @return empty String
     */
    @Override
    public String getNodeValue(final TreeNode _node) {
      return "";
    }
  }
}

