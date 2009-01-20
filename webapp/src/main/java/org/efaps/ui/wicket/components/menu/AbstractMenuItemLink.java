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

package org.efaps.ui.wicket.components.menu;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;

import org.efaps.ui.wicket.models.objects.UIMenuItem;


/**
 * Class is used as the base for links in the JSCookMenu.
 *
 * @author jmox
 * @version $Id$
 */
public abstract class AbstractMenuItemLink extends Link<UIMenuItem> {

  /**
   * Needed for serialization.
   */
  private static final long serialVersionUID = 1L;

  /**
   * Constructor.
   *
   * @param _wicketId   wicket id to set
   * @param _model      model for this component
   */
  public AbstractMenuItemLink(final String _wicketId,
                              final IModel<UIMenuItem> _model) {
    super(_wicketId, _model);
  }

  /**
   * For the JSCookMenu nothing must be rendered, because JavaScript is used to
   * create the Menu.
   *
   * @param _markupStream Markup Stream
   */
  @Override
  protected void onRender(final MarkupStream _markupStream) {
    _markupStream.next();
  }
}
