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
import org.apache.wicket.Response;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebApplication;

import org.efaps.webapp.pages.HomePage;

/**
 * @author jmo
 * @version $Id$
 */
public class EFapsApplication extends WebApplication {

  @Override
  public Class<HomePage> getHomePage() {
    // TODO muss gegen echte Homepgae getauscht werden
    return HomePage.class;

  }

  @Override
  protected void init() {
    super.init();
    getMarkupSettings().setStripWicketTags(true);
    getMarkupSettings().setStripComments(true);
    getRequestCycleSettings().setGatherExtendedBrowserInfo(true);
    // getDebugSettings().setAjaxDebugModeEnabled(false);
  }

  public Session newSession(Request _request, Response _response) {
    return new EFapsSession(_request);
  }

}
