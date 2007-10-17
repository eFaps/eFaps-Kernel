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

package org.efaps.maven.jetty.configuration;

import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.FilterHolder;

/**
 * @author tmo
 * @version $Id$
 * @todo description
 */
public class FilterDefinition extends AbstractDefinition  {

  /**
   * 
   * @param _handler
   */
  public void updateServer(final Context _handler)  {
    final FilterHolder filter = new FilterHolder();
    filter.setName(getName());
    filter.setClassName(getClassName());
    filter.setInitParameters(getIniParams());
    _handler.addFilter(filter, getPathSpec(), 1);
  }

}
