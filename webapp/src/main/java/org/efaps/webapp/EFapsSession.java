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

import org.apache.wicket.Request;
import org.apache.wicket.model.IModel;
import org.apache.wicket.protocol.http.WebSession;

import org.efaps.webapp.components.listmenu.ListMenuLinkComponent;

/**
 * @author jmo
 * @version $Id$
 * 
 */
public class EFapsSession extends WebSession {

  public EFapsSession(Request request) {
    super(request);
  }

  private int contentcontainerid;

  private int contentcontainerversion;

  private ListMenuLinkComponent sidemenuselected;

  private IModel model;

  private static final long serialVersionUID = 1884548064760514909L;

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

  public void setOpenerModel(final IModel _model) {
    this.model = _model;
  }

  public IModel getOpenerModel() {
    return this.model;
  }
}
