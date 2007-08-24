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

package org.efaps.webapp;

import org.apache.wicket.PageParameters;
import org.apache.wicket.Request;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebSession;

import org.efaps.util.EFapsException;
import org.efaps.webapp.components.listmenu.ListMenuLinkComponent;
import org.efaps.webapp.models.FormModel;
import org.efaps.webapp.models.TableModel;

public class EFapsSession extends WebSession {

  private int contentcontainerid;

  private int contentcontainerversion;

  private ListMenuLinkComponent sidemenuselected;

  public EFapsSession(WebApplication application, Request request) {
    super(application, request);
  }

  private static final long serialVersionUID = 1884548064760514909L;

  public FormModel getIFormModel(PageParameters _parameters, final String _key)
      throws Exception {
    return new FormModel(_parameters);
  }

  public TableModel getITableModel(PageParameters _parameters,
      final String _key) throws EFapsException {
    return new TableModel(_parameters);

  }

  public void setContentContainer(int _id, int _version) {
    this.contentcontainerid = _id;
    this.contentcontainerversion = _version;
  }

  public int getContentContainerId() {
    return this.contentcontainerid;
  }

  public int getContentContainerVersion() {
    return this.contentcontainerversion;
  }

  public void setSideMenuSelected(ListMenuLinkComponent _selected) {
    this.sidemenuselected = _selected;
  }

  public ListMenuLinkComponent getSideMenuSelected() {
    return this.sidemenuselected;
  }

}
