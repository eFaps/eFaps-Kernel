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

package org.efaps.ui.wicket.components.footer;

import org.apache.wicket.markup.html.link.PopupCloseLink;
import org.apache.wicket.model.Model;

import org.efaps.ui.wicket.EFapsSession;
import org.efaps.ui.wicket.models.objects.AbstractUIObject;

/**
 * Class extends the standard PopupCloseLink due to the reason that the opener
 * must be removed from the cache in the session.
 *
 * @author jmox
 * @version $Id$
 */
public class ClosePopUpLink extends PopupCloseLink<AbstractUIObject> {

  /**
   * Needed for serialization.
   */
  private static final long serialVersionUID = 1L;

  /**
   * @param _wicketId   wicket id of this component
   * @param _uiObject   object for the model of this component
   */
  public ClosePopUpLink(final String _wicketId,
                        final AbstractUIObject _uiObject) {
    super(_wicketId, new Model<AbstractUIObject>(_uiObject));
  }

  /**
   * Action is done on click.
   */
  @Override
  public void onClick() {
    super.onClick();
    final AbstractUIObject uiObject
                                   = (AbstractUIObject) getDefaultModelObject();
    ((EFapsSession) getSession()).removeOpener(uiObject.getOpenerId());
  }

}
