/*
 * Copyright 2003-2008 The eFaps Team
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

package org.efaps.maven.jetty.configuration;

import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

/**
 * @author tmo
 * @version $Id$
 * @todo description
 */
public class ServletDefinition extends AbstractDefinition  {

  /**
   * Initialize order of the servlet.
   */
  private Integer initOrder = null;
  
  /**
   * Display name of the servlet.
   */
  private String displayName = null;

  /**
   * 
   * @param _handler
   */
  public void updateServer(final Context _handler)  {
    final ServletHolder servlet = new ServletHolder();
    servlet.setName(getName());
    servlet.setDisplayName(this.displayName);
    servlet.setClassName(getClassName());
    servlet.setInitParameters(getIniParams());
    if (this.initOrder != null)  {
      servlet.setInitOrder(this.initOrder);
    }
    _handler.addServlet(servlet, getPathSpec());
  }

  /**
   * Setter method for instance variable {@link #initOrder}.
   *
   * @param _initOrder    new init order to set
   * @see #initOrder
   */
  public void setInitOrder(final int _initOrder)  {
    this.initOrder = _initOrder;
  }

  /**
   * Setter method for instance variable {@link #displayName}.
   *
   * @param _displayName  new display name to set
   * @see #displayName
   */
  public void setDisplayName(final String _displayName)  {
    this.displayName = _displayName;
  }
}
