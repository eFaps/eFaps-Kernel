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

package org.efaps.webapp.components;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebComponent;

import org.efaps.webapp.models.IMenuItemModel;

public class MainMenuItem extends WebComponent {
  public MainMenuItem(String id, IMenuItemModel model) {
    super(id, model);
  }

  private static final long serialVersionUID = -3251324939633532963L;

  @Override
  protected void onComponentTagBody(MarkupStream _markupStream,
      ComponentTag _openTag) {

    super.replaceComponentTagBody(_markupStream, _openTag,
        ((IMenuItemModel) super.getModel()).getLabel());
   
  }

}
