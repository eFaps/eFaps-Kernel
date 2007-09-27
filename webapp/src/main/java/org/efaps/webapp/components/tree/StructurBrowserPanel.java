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

package org.efaps.webapp.components.tree;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.apache.wicket.PageParameters;
import org.apache.wicket.extensions.markup.html.tree.table.AbstractRenderableColumn;
import org.apache.wicket.extensions.markup.html.tree.table.AbstractTreeColumn;
import org.apache.wicket.extensions.markup.html.tree.table.ColumnLocation;
import org.apache.wicket.extensions.markup.html.tree.table.IColumn;
import org.apache.wicket.extensions.markup.html.tree.table.ColumnLocation.Alignment;
import org.apache.wicket.extensions.markup.html.tree.table.ColumnLocation.Unit;
import org.apache.wicket.markup.html.panel.Panel;

import org.efaps.webapp.models.StructurBrowserModel;

public class StructurBrowserPanel extends Panel {

  private static final long serialVersionUID = 1L;

  public StructurBrowserPanel(final String _id, final PageParameters _parameters) {
    this(_id, new StructurBrowserModel(_parameters));
  }

  public StructurBrowserPanel(final String _id, StructurBrowserModel _model) {
    super(_id, _model);

    StructurBrowserModel model = (StructurBrowserModel) super.getModel();
    if (!model.isInitialised()) {
      model.execute();
    }

    IColumn[] columns = new IColumn[model.getHeaders().size()];

    for (int i = 0; i < model.getHeaders().size(); i++) {

      if (model.getHeaders().get(i).getName().equals(
          model.getBrowserFieldName())) {
        columns[i] =
            new TreeColumn(new ColumnLocation(Alignment.MIDDLE, 2,
                Unit.PROPORTIONAL), model.getHeaders().get(i).getLabel());
      } else {
        columns[i] =
            new TreeTableColumn(new ColumnLocation(Alignment.MIDDLE, 1,
                Unit.PROPORTIONAL), model.getHeaders().get(i).getLabel(), i);
      }

    }

    StructurTreeTable tree =
        new StructurTreeTable("treeTable", model.getTreeModel(), columns);
    add(tree);

  }

  public class TreeColumn extends AbstractTreeColumn {

    private static final long serialVersionUID = 1L;

    public TreeColumn(ColumnLocation location, String header) {
      super(location, header);
    }

    @Override
    public String renderNode(TreeNode arg0) {

      return arg0.toString();
    }

  }

  public class TreeTableColumn extends AbstractRenderableColumn {

    private static final long serialVersionUID = 1L;

    private final int index;

    public TreeTableColumn(final ColumnLocation _location,
                           final String _header, int _index) {
      super(_location, _header);
      this.index = _index;
    }

    @Override
    public String getNodeValue(TreeNode arg0) {
      String ret = "";
      ret =
          ((StructurBrowserModel) ((DefaultMutableTreeNode) arg0)
              .getUserObject()).getColumnValue(this.index);

      return ret;
    }
  }
}
