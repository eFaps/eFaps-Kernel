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
 * @version $Id$
 */
public class StructurBrowserTreeTablePanel extends Panel {

  private static final long serialVersionUID = 1L;

  public StructurBrowserTreeTablePanel(final String _id,
                                       final UUID _uuid, final String _oid) {
    this(_id,  new StructurBrowserModel(new UIStructurBrowser(_uuid, _oid)));
  }

  public StructurBrowserTreeTablePanel(final String _id,
                                       final IModel<UIStructurBrowser> _model) {
    super(_id, _model);

    final UIStructurBrowser model = (UIStructurBrowser) super.getDefaultModelObject();
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
                Unit.PROPORTIONAL), model.getHeaders().get(i).getLabel());
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

  public class TreeColumn extends AbstractTreeColumn {

    private static final long serialVersionUID = 1L;

    public TreeColumn(final ColumnLocation location, final String header) {
      super(location, header);
    }

    @Override
    public String renderNode(final TreeNode _node) {

      return _node.toString();
    }

  }

  public class SimpleColumn extends AbstractRenderableColumn {

    private static final long serialVersionUID = 1L;

    private final int index;

    public SimpleColumn(final ColumnLocation _location, final String _header,
                        final int _index) {
      super(_location, _header);
      this.index = _index;
      setContentAsTooltip(true);
    }

    @Override
    public String getNodeValue(final TreeNode _node) {
      String ret = "";
      ret =
          ((UIStructurBrowser) ((DefaultMutableTreeNode) _node)
              .getUserObject()).getColumnValue(this.index);

      return ret;
    }
  }

  public class SelectColumn extends AbstractRenderableColumn {

    private static final long serialVersionUID = 1L;

    public SelectColumn(final ColumnLocation location, final String header) {
      super(location, header);
    }

    @Override
    public IRenderable newCell(final TreeNode _node, final int _level) {
      return new IRenderable() {

        private static final long serialVersionUID = 1L;

        public void render(final TreeNode _node, final Response _response) {
          final String oid =
              ((UIStructurBrowser) ((DefaultMutableTreeNode) _node)
                  .getUserObject()).getOid();
          final String checkbox =
              "<input "
                  + "type=\"checkbox\""
                  + "name=\"selectedRow\""
                  + "class=\"eFapsCheckboxCell\""
                  + "value=\""
                  + oid
                  + "\"/>";

          _response.write(checkbox);
        }
      };
    }

    @Override
    public String getNodeValue(final TreeNode _node) {
      return "";
    }

  }
}
