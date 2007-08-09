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

package org.efaps.webapp.models;

import org.apache.wicket.Request;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebSession;

import org.efaps.util.EFapsException;

public class EFapsApplicationSession extends WebSession {

  public EFapsApplicationSession(WebApplication application, Request request) {
    super(application, request);
    // TODO Auto-generated constructor stub
  }

  private static final long serialVersionUID = 1884548064760514909L;

  public IFormModel getIFormModel(final String _key) throws Exception {
    return new IFormModel();
  }

  public ITableModel getITableModel(final String _key) throws EFapsException {
    return new ITableModel();

  }
}
