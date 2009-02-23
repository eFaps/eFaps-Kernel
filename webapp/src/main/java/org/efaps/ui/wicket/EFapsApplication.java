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

package org.efaps.ui.wicket;

import org.apache.wicket.Component;
import org.apache.wicket.Request;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.Response;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.Session;
import org.apache.wicket.authorization.Action;
import org.apache.wicket.authorization.IAuthorizationStrategy;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebRequest;

import org.efaps.ui.wicket.pages.login.LoginPage;
import org.efaps.ui.wicket.pages.main.MainPage;

/**
 * This Class presents the WebApplication for eFaps usinf the Wicket-Framework.
 * <br/>
 * It is the first class wich is instanciated from the WicketServlet. Here the
 * Sessioins for each user a created and basic Settings are set.
 *
 * @author jmox
 * @version $Id$
 */
public class EFapsApplication extends WebApplication {

  /*
   * (non-Javadoc)
   *
   * @see org.apache.wicket.protocol.http.WebApplication#newRequestCycle(org.apache.wicket.Request,
   *      org.apache.wicket.Response)
   */
  @Override
  public RequestCycle newRequestCycle(final Request _request,
                                      final Response _response) {
    return new EFapsWebRequestCycle(this, (WebRequest) _request, _response);
  }

  /*
   * (non-Javadoc)
   *
   * @see org.apache.wicket.Application#getHomePage()
   */
  @Override
  public Class<MainPage> getHomePage() {
    return MainPage.class;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.apache.wicket.protocol.http.WebApplication#init()
   */
  @Override
  protected void init() {
    super.init();
    getMarkupSettings().setStripWicketTags(true);
    getMarkupSettings().setStripComments(true);
    getMarkupSettings().setCompressWhitespace(true);
    getRequestCycleSettings().setGatherExtendedBrowserInfo(true);
    getDebugSettings().setAjaxDebugModeEnabled(false);
    super.getSecuritySettings().setAuthorizationStrategy(
        new EFapsFormBasedAuthorizationStartegy());
  }

  /*
   * (non-Javadoc)
   *
   * @see org.apache.wicket.protocol.http.WebApplication#newSession(org.apache.wicket.Request,
   *      org.apache.wicket.Response)
   */
  @Override
  public Session newSession(final Request _request, final Response _response) {
    return new EFapsSession(_request);

  }

  /**
   * The Class presents the Strategy to authorize pages in this WebApplication.
   */
  private class EFapsFormBasedAuthorizationStartegy implements
      IAuthorizationStrategy {

    /*
     * (non-Javadoc)
     *
     * @see org.apache.wicket.authorization.IAuthorizationStrategy#isActionAuthorized(org.apache.wicket.Component,
     *      org.apache.wicket.authorization.Action)
     */
    public boolean isActionAuthorized(final Component _component,
                                      final Action _action) {
      return true;
    }

    /**
     * For all Pages it will be checked if a User is logged in or if the Page
     * implements the EFapsNoAuthendPageInterface, if non of both we will
     * redirect to the LoginPage.
     * @param _componentClass class to be checked
     *
     */
    @SuppressWarnings("unchecked")
    public boolean isInstantiationAuthorized(final Class _componentClass) {

      if (WebPage.class.isAssignableFrom(_componentClass)) {
        if (((EFapsSession) Session.get()).isLogedIn()
            || EFapsNoAuthorizationNeededInterface.class
                .isAssignableFrom(_componentClass)) {
          return true;
        }
        throw new RestartResponseAtInterceptPageException(LoginPage.class);
      }
      return true;
    }
  }

}
