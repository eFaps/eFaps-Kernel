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

package org.efaps.ui.wicket.components;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.model.IModel;

/**
 * @author jmox
 * @version $Id:LabelComponent.java 1510 2007-10-18 14:35:40Z jmox $
 */
public class LabelComponent extends WebComponent {

  private static final long serialVersionUID = 1L;

  public LabelComponent(final String _wicketId, final IModel _model) {
    super(_wicketId, _model);
  }

  @Override
  protected void onComponentTagBody(final MarkupStream _markupStream,
                                    final ComponentTag _openTag) {
    super.replaceComponentTagBody(_markupStream, _openTag, (String) super
        .getModelObject());
  }
}
