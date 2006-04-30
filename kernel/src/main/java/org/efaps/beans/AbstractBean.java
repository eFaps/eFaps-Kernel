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
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;

import org.efaps.admin.user.Person;
import org.efaps.db.Context;
import org.efaps.db.Instance;

public abstract class AbstractBean implements AbstractBeanInterface  {

  public AbstractBean()  {
System.out.println("AbstractBean.constructor");
  }

  public void finalize()  {
System.out.println("AbstractBean.destructor");
  }

  /**
   * The instance method sets the object id for this bean. To set the
   * object id means to set the instance for this bean.
   *
   * @param _oid    object id
   * @see #instance
   */
  public void setOid(String _oid) throws Exception  {
    if (_oid!=null && _oid.length()>0)  {
setInstance(new Instance(null, _oid));
//      Context context = new Context();
//      try  {
//        setInstance(new Instance(context, _oid));
//      } catch (Exception e)  {
//        throw e;
//      } finally  {
//        try  {
//          context.close();
//        } catch (Exception e)  {
//        }
//      }
    }
  }

  /**
   * The instance method sets the user name who is actual logged in.
   *
   * @param _loginName
   * @see #loginPerson
   */
  public void setLoginName(String _loginName) throws Exception  {
    if (_loginName!=null)  {
      setLoginPerson(Person.get(_loginName));
    }
  }

  /**
   * The instance method returns the locale object from the response object.
   *
   * @return locale object
   * @see #response
   */
  protected Locale getLocale()  {
    return getResponse().getLocale();
  }

  /**
   * This is the setter method for the Parameters variable {@link #parameters}.
   *
   * @param _parameters     new value for Parameters variable
   *                        {@link #parameters}
   * @param _fileParameters new value for Parameters variable
   *                        {@link #fileParameters}
   * @see #parameters
   * @see #fileParameters
   */
  public void setParameters(Map<String,String[]> _parameters, Map<String,FileItem> _fileParameters) throws Exception  {
    setParameters(_parameters);
    setFileParameters(_fileParameters);
    setOid(getParameter("oid"));
  }

  /**
   * @return value for given parameter
   * @see #parameters
   * @todo description
   */
  public String getParameter(String _name)  {
    String ret = null;

    String[] values = getParameters().get(_name);
    if (values!=null)  {
      ret = values[0];
    }
    return ret;
  }

  /**
   * @see #fileParameters
   * @todo description
   */
  public FileItem getFileParameter(String _name)  {
    return getFileParameters().get(_name);
  }

  /////////////////////////////////////////////////////////////////////////////

  /**
   * The instance variable stores the instance object for which this bean is
   * created.
   *
   * @see #getInstance
   * @see #setInstance
   */
  private Instance instance = null;

  /**
   * The instance variable stores the http servlet response.
   *
   * @see #getResponse
   * @see #setResponse
   */
  private HttpServletResponse response = null;

  /**
   * The instance variable stores the http servlet request.
   *
   * @see #getRequest
   * @see #setRequest
   */
  private HttpServletRequest request = null;

  /**
   * The instance variable stores the http servlet request parameters.
   *
   * @see #getParameter
   * @see #getParameters
   * @see #setParameters
   */
  private Map<String,String[]> parameters = null;

  /**
   * The instance variable stores the http servlet file request parameters.
   *
   * @see #getFileParameters
   * @see #setFileParameters
   */
  private Map<String,FileItem> fileParameters = null;

  /**
   * The instance variable is the flag if this class instance is already
   * initialised.
   *
   * @see #isInitialised
   * @see #setInitialised
   */
  private boolean initialised = false;

  /**
   * The instance variable stores the logged in person object used to create
   * new context objects.
   *
   * @see #setLoginPerson
   * @see #getLoginPerson
   */
  private Person loginPerson = null;

  /////////////////////////////////////////////////////////////////////////////

  /**
   * This is the getter method for the instance variable {@link #instance}.
   *
   * @return value of instance variable {@link #instance}
   * @see #instance
   * @see #setInstance
   */
  public Instance getInstance()  {
    return this.instance;
  }

  /**
   * This is the setter method for the instance variable {@link #instance}.
   *
   * @param _instance  new value for instance variable {@link #instance}
   * @see #instance
   * @see #getInstance
   */
  protected void setInstance(Instance _instance)  {
    this.instance = _instance;
  }

  /**
   * This is the getter method for the response variable {@link #response}.
   *
   * @return value of response variable {@link #response}
   * @see #response
   * @see #setResponse
   */
  public HttpServletResponse getResponse()  {
    return this.response;
  }

  /**
   * This is the setter method for the response variable {@link #response}.
   *
   * @param _response  new value for response variable {@link #response}
   * @see #response
   * @see #getResponse
   */
  public void setResponse(HttpServletResponse _response)  {
    this.response = _response;
  }

  /**
   * This is the getter method for the Request variable {@link #request}.
   *
   * @return value of Request variable {@link #request}
   * @see #request
   * @see #setRequest
   */
  public HttpServletRequest getRequest()  {
    return this.request;
  }

  /**
   * This is the setter method for the Request variable {@link #request}.
   *
   * @param _Request  new value for Request variable {@link #request}
   * @see #request
   * @see #getRequest
   */
  public void setRequest(HttpServletRequest _request)  {
    this.request = _request;
  }

  /**
   * This is the getter method for the Parameters variable {@link #parameters}.
   *
   * @return value of Parameters variable {@link #parameters}
   * @see #parameters
   * @see #setParameters
   */
  public Map<String,String[]> getParameters()  {
    return this.parameters;
  }

  /**
   * This is the setter method for the Parameters variable {@link #parameters}.
   *
   * @param _Parameters  new value for Parameters variable {@link #parameters}
   * @see #parameters
   * @see #getParameters
   */
  private void setParameters(Map<String,String[]> _parameters)  {
    this.parameters = _parameters;
  }

  /**
   * This is the getter method for the FileParameters variable {@link #fileParameters}.
   *
   * @return value of FileParameters variable {@link #fileParameters}
   * @see #fileParameters
   * @see #setFileParameters
   */
  public Map<String,FileItem> getFileParameters()  {
    return this.fileParameters;
  }

  /**
   * This is the setter method for the FileParameters variable {@link #fileParameters}.
   *
   * @param _FileParameters  new value for FileParameters variable {@link #fileParameters}
   * @see #fileParameters
   * @see #getFileParameters
   */
  private void setFileParameters(Map<String,FileItem> _fileParameters)  {
    this.fileParameters = _fileParameters;
  }

  /**
   * This is the getter method for the initialised variable {@link #initialised}.
   *
   * @return value of initialised variable {@link #initialised}
   * @see #initialised
   * @see #setInitialised
   */
  public boolean isInitialised()  {
    return this.initialised;
  }

  /**
   * This is the setter method for the initialised variable {@link #initialised}.
   *
   * @param _initialised  new value for initialised variable {@link #initialised}
   * @see #initialised
   * @see #isInitialised
   */
  public void setInitialised(boolean _initialised)  {
    this.initialised = _initialised;
  }

  /**
   * This is the getter method for the loginPerson variable {@link #loginPerson}.
   *
   * @return value of loginPerson variable {@link #loginPerson}
   * @see #loginPerson
   * @see #setLoginPerson
   */
  public Person getLoginPerson()  {
    return this.loginPerson;
  }

  /**
   * This is the setter method for the loginPerson variable {@link #loginPerson}.
   *
   * @param _loginPerson  new value for loginPerson variable {@link #loginPerson}
   * @see #loginPerson
   * @see #getLoginPerson
   */
  public void setLoginPerson(Person _loginPerson)  {
    this.loginPerson = _loginPerson;
  }
}
