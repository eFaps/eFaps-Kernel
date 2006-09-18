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

import javax.servlet.http.HttpServletResponse;

import org.efaps.util.EFapsException;

/**
 * @author tmo
 * @version $Id$
 * @todo decription
 */
public interface AbstractBeanInterface  {

  /**
   * The instance method sets the object id for the this implementation of a
   * bean (e.g. for a web table the object id represents the object, from
   * which the expands starts; for a web form the object id is the object,
   * which is shown in the web form).
   *
   * @param _oid    object id
   */
  public void setOid(String _oid) throws EFapsException;

  /**
   * The response of the http servlet is set for the implementation of this
   * web user interface instance.
   *
   * @param _response  new value of the response variable
   */
  public void setResponse(HttpServletResponse _response);
}
