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

package org.efaps.webapp.components.table;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;

import org.efaps.webapp.components.table.cell.formcell.FormCellPanel;
import org.efaps.webapp.models.FormModel;
import org.efaps.webapp.models.FormModel.FormCellModel;
import org.efaps.webapp.models.FormModel.FormRowModel;
import org.efaps.webapp.pages.contentcontainer.ContentContainerPage;


/**
 * @author jmox
 * @version $Id$
 */
public class WebFormContainer extends WebMarkupContainer {

  private static final long serialVersionUID = 1550111712776698728L;

  private static String ROWID = "eFapsRow";

  private final List<FormCellPanel> requiredComponents =
      new ArrayList<FormCellPanel>();

  public WebFormContainer(String id, IModel model, final Page _page) {
    super(id, model);
    initialise(_page);
  }

  private void initialise(final Page _page) {
    int i = 0;
    int j = 0;
    FormModel model = (FormModel) super.getModel();
    model.execute();

    RowContainer row = null;
    for (FormRowModel rowmodel : model.getValues()) {
      row = new RowContainer(ROWID + "_" + i);
      this.add(row);
      i++;
      for (FormCellModel cellmodel : rowmodel.getValues()) {
        FormCellPanel formcellpanel =
            new FormCellPanel("cell" + "_" + i + "_" + j, cellmodel, model
                .getMaxGroupCount(), rowmodel.getGroupCount(),
                ContentContainerPage.IFRAME_PAGEMAP_NAME.equals(_page
                    .getPageMapName()));
        row.add(formcellpanel);
        if (cellmodel.isRequired()
            && (model.isCreateMode() || model.isEditMode())) {
          this.requiredComponents.add(formcellpanel);
        }
        j++;
      }

    }

  }

  @Override
  protected final void onRender(final MarkupStream markupStream) {
    final int markupStart = markupStream.getCurrentIndex();
    Iterator<?> childs = this.iterator();
    if (childs.hasNext()) {
      while (childs.hasNext()) {
        markupStream.setCurrentIndex(markupStart);
        Component child = (Component) childs.next();
        child.render(getMarkupStream());
      }
    } else {
      markupStream.next();
    }
  }

  /**
   * This is the getter method for the instance variable
   * {@link #requiredComponents}.
   *
   * @return value of instance variable {@link #requiredComponents}
   */

  public List<FormCellPanel> getRequiredComponents() {
    return this.requiredComponents;
  }

}
