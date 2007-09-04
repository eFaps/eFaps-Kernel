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

import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.Request;
import org.apache.wicket.model.IModel;
import org.apache.wicket.protocol.http.WebSession;

/**
 * @author jmo
 * @version $Id$
 */
public class EFapsSession extends WebSession {

  public EFapsSession(Request request) {
    super(request);
  }

  private int contentcontainerid;

  private int contentcontainerversion;

  private final Map<String, Component> componentmap =
      new HashMap<String, Component>();

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

  public void setSelectedComponent(final String _key, final Component _component) {
    this.componentmap.remove(_key);
    this.componentmap.put(_key, _component);
  }

  public Component getSelectedComponent(final String _key) {
    return this.componentmap.get(_key);
  }

  public void removeSelectedComponent(final String _key) {
    this.componentmap.remove(_key);
  }

  public void setOpenerModel(final IModel _model) {
    this.model = _model;
  }

  public IModel getOpenerModel() {
    return this.model;
  }
}
