/*
 * Copyright 2006 The eFaps Team
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

package org.efaps.beans;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.util.EFapsException;

/*
 * @author tmo
 * 
 * @version $Id: AbstractBean.java 675 2007-02-14 20:56:25 +0000 (Wed, 14 Feb
 *          2007) jmo $ @todo description
 */
public abstract class AbstractBean implements AbstractBeanInterface {

  public AbstractBean() throws EFapsException {
    System.out.println("AbstractBean.constructor");
    String oid = getParameter("oid");
    if (oid != null) {
      this.instance = new Instance(getParameter("oid"));
    }
  }

  public void finalize() {
    System.out.println("AbstractBean.destructor");
  }

  /**
   * The instance method sets the object id for this bean. To set the object id
   * means to set the instance for this bean.
   * 
   * @param _oid
   *          object id
   * @see #instance
   */
  public void setOid(String _oid) throws EFapsException {
    if (_oid != null && _oid.length() > 0) {
      setInstance(new Instance(_oid));
    }
  }

  /**
   * The instance method returns the locale object from the response object.
   * 
   * @return locale object
   * @see #response
   */
  protected Locale getLocale() {
    return getResponse().getLocale();
  }

  /**
   * @return value for given parameter
   * @todo description
   */
  public String getParameter(String _name) throws EFapsException {
    String ret = null;

    String[] values = Context.getThreadContext().getParameters().get(_name);
    if (values != null) {
      ret = values[0];
    }
    return ret;
  }

  /**
   * @todo description
   */
  public FileItem getFileParameter(String _name) throws EFapsException {
    FileItem fileItem = null;
    Context context = Context.getThreadContext();
    if (context.getFileParameters() != null) {
      fileItem = context.getFileParameters().get(_name);
    }
    return fileItem;
  }

  // ///////////////////////////////////////////////////////////////////////////

  /**
   * The instance variable stores the instance object for which this bean is
   * created.
   * 
   * @see #getInstance
   * @see #setInstance
   */
  private Instance            instance    = null;

  /**
   * The instance variable stores the http servlet response.
   * 
   * @see #getResponse
   * @see #setResponse
   */
  private HttpServletResponse response    = null;

  /**
   * The instance variable stores the http servlet request.
   * 
   * @see #getRequest
   * @see #setRequest
   */
  private HttpServletRequest  request     = null;

  /**
   * The instance variable is the flag if this class instance is already
   * initialised.
   * 
   * @see #isInitialised
   * @see #setInitialised
   */
  private boolean             initialised = false;

  // ///////////////////////////////////////////////////////////////////////////

  /**
   * This is the getter method for the instance variable {@link #instance}.
   * 
   * @return value of instance variable {@link #instance}
   * @see #instance
   * @see #setInstance
   */
  public Instance getInstance() {
    return this.instance;
  }

  /**
   * This is the setter method for the instance variable {@link #instance}.
   * 
   * @param _instance
   *          new value for instance variable {@link #instance}
   * @see #instance
   * @see #getInstance
   */
  protected void setInstance(Instance _instance) {
    this.instance = _instance;
  }

  /**
   * This is the getter method for the response variable {@link #response}.
   * 
   * @return value of response variable {@link #response}
   * @see #response
   * @see #setResponse
   */
  public HttpServletResponse getResponse() {
    return this.response;
  }

  /**
   * This is the setter method for the response variable {@link #response}.
   * 
   * @param _response
   *          new value for response variable {@link #response}
   * @see #response
   * @see #getResponse
   */
  public void setResponse(HttpServletResponse _response) {
    this.response = _response;
  }

  /**
   * This is the getter method for the Request variable {@link #request}.
   * 
   * @return value of Request variable {@link #request}
   * @see #request
   * @see #setRequest
   */
  public HttpServletRequest getRequest() {
    return this.request;
  }

  /**
   * This is the setter method for the Request variable {@link #request}.
   * 
   * @param _Request
   *          new value for Request variable {@link #request}
   * @see #request
   * @see #getRequest
   */
  public void setRequest(HttpServletRequest _request) {
    this.request = _request;
  }

  /**
   * This is the getter method for the initialised variable {@link #initialised}.
   * 
   * @return value of initialised variable {@link #initialised}
   * @see #initialised
   * @see #setInitialised
   */
  public boolean isInitialised() {
    return this.initialised;
  }

  /**
   * This is the setter method for the initialised variable {@link #initialised}.
   * 
   * @param _initialised
   *          new value for initialised variable {@link #initialised}
   * @see #initialised
   * @see #isInitialised
   */
  public void setInitialised(boolean _initialised) {
    this.initialised = _initialised;
  }
}
