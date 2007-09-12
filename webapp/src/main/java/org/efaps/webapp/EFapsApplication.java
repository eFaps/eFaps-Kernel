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

import org.apache.wicket.Component;
import org.apache.wicket.Request;
import org.apache.wicket.Response;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.Session;
import org.apache.wicket.authorization.Action;
import org.apache.wicket.authorization.IAuthorizationStrategy;
import org.apache.wicket.protocol.http.WebApplication;

import org.efaps.webapp.pages.LoginPage;
import org.efaps.webapp.pages.MainPage;

/**
 * @author jmo
 * @version $Id$
 */
public class EFapsApplication extends WebApplication {

  @Override
  public Class<MainPage> getHomePage() {
    return MainPage.class;
  }

  @Override
  protected void init() {
    super.init();
    getMarkupSettings().setStripWicketTags(true);
    getMarkupSettings().setStripComments(true);
    getRequestCycleSettings().setGatherExtendedBrowserInfo(true);
    // getDebugSettings().setAjaxDebugModeEnabled(false);
    super.getSecuritySettings().setAuthorizationStrategy(
        new EFapsFormBasedAuthorizationStartegy());


  }

  @Override
  public Session newSession(final Request _request, final Response _response) {
    return new EFapsSession(_request);

  }

  private class EFapsFormBasedAuthorizationStartegy implements
      IAuthorizationStrategy {

    public boolean isActionAuthorized(final Component _component,
                                      final Action _action) {
      return true;
    }

    @SuppressWarnings("unchecked")
    public boolean isInstantiationAuthorized(Class _componentClass) {

      if (EFapsAuthenticatedPage.class.isAssignableFrom(_componentClass)) {
        if (((EFapsSession) Session.get()).isLogedIn()) {
          return true;
        }
        throw new RestartResponseAtInterceptPageException(LoginPage.class);
      }

      return true;
    }
  }

}
