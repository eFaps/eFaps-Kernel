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
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.model.IModel;

/**
 * Base class for all menu item inside the JSCookMenu using ajax.
 *
 * @author jmox
 * @version $Id$
 */
public abstract class AbstractMenuItemAjaxComponent extends WebComponent {

  /**
   * Needed for serialization.
   */
  private static final long serialVersionUID = 1L;

  /**
   * Constructor.
   *
   * @param _wicketId   wicket id of this component
   * @param _model      model for this component
   */
  public AbstractMenuItemAjaxComponent(final String _wicketId,
                                       final IModel<?> _model) {
    super(_wicketId, _model);
  }

  /**
   * For the JSCookMenu nothing must be rendered, because JavaScript is used to
   * create the Menu.
   *
   * @param _markupStream MarkupStream
   */
  @Override
  protected void onRender(final MarkupStream _markupStream) {
    _markupStream.next();
  }

  /**
   * This Method must return the JavaScript which is executed by the
   * JSCooKMenu.
   *
   * @return String with the JavaScript
   */
  public abstract String getJavaScript();
}
