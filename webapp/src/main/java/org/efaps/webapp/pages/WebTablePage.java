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

package org.efaps.webapp.pages;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.resources.StyleSheetReference;

import org.efaps.webapp.components.table.WebTableContainer;
import org.efaps.webapp.components.table.header.TableHeaderPanel;
import org.efaps.webapp.models.TableModel;

/**
 * @author jmo
 * @version $Id$
 */
public class WebTablePage extends ContentPage {

  private static final long serialVersionUID = 7564911406648729094L;

  public WebTablePage(final PageParameters _parameters) throws Exception {
    super.setModel(new TableModel(_parameters));
    this.addComponents();
  }

  protected void addComponents() {
    super.addComponents(null);
    try {

      add(new StyleSheetReference("WebTablePageCSS", getClass(),
          "webtablepage/WebTablePage.css"));
      TableModel model = (TableModel) super.getModel();
      model.execute();

      add(new TableHeaderPanel("eFapsTableHeader", model));
      add(new WebTableContainer("eFapsTable", model));
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

}
