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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.wicket.Application;
import org.apache.wicket.Page;
import org.apache.wicket.Response;
import org.apache.wicket.Session;
import org.apache.wicket.authorization.AuthorizationException;
import org.apache.wicket.protocol.http.PageExpiredException;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebRequestCycle;

import org.efaps.webapp.pages.ErrorPage;
import org.efaps.webapp.pages.LoginPage;

/**
 * This class extends the
 * {@link org.apache.wicket.protocol.http.WebRequestCycle} only to throw a own
 * ErrorPage.
 *
 * @author jmo
 * @version $Id$
 */
public class EFapsWebRequestCycle extends WebRequestCycle {

  /**
   * Logger for this class
   */
  private static final Log LOG = LogFactory.getLog(EFapsWebRequestCycle.class);

  public EFapsWebRequestCycle(WebApplication application, WebRequest request,
                              Response response) {
    super(application, request, response);
  }

  /*
   * (non-Javadoc)
   *
   * @see org.apache.wicket.RequestCycle#onRuntimeException(org.apache.wicket.Page,
   *      java.lang.RuntimeException)
   */
  @Override
  public Page onRuntimeException(final Page _page,
                                 final RuntimeException _exception) {

    if (_exception instanceof AuthorizationException) {
      return super.onRuntimeException(_page, _exception);
    } else if (_exception instanceof PageExpiredException) {
      EFapsSession session = (EFapsSession) Session.get();
      if (session.isTemporary() || !session.isLogedIn()) {
        // this was an actual session expiry
        LOG.info("session expired and request cannot be honored, cycleId: ");
        return new LoginPage();
      } else {
        LOG.error("unable to find page for an active session!");
        // we have a logged in user, but the page could not be
        // found. Fall through to display an error page or exception
        // page
      }
    }
    // we don't make a difference between deployment and development, do we?
    if (Application.DEPLOYMENT.equals(Application.get().getConfigurationType())) {
      return new ErrorPage(_exception);
    } else {
      return new ErrorPage(_exception);
    }
  }
}
