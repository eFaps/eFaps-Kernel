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

import org.apache.wicket.model.IModel;

import org.efaps.ui.wicket.EFapsSession;
import org.efaps.ui.wicket.models.objects.UIMenuItem;
import org.efaps.ui.wicket.pages.login.LoginPage;

/**
 * @author jmox
 * @version $Id$
 */
public class LogOutLink extends StandardLink {

  private static final long serialVersionUID = 1L;

  public LogOutLink(final String _id, final IModel<UIMenuItem> _model) {
    super(_id, _model);
  }

  @Override
  public void onClick() {
    this.setResponsePage(new LoginPage());
    ((EFapsSession) this.getSession()).logout();
  }
}
