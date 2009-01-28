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
package org.efaps.maven.jetty.configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author tmo
 * @version $Id$
 * @todo description
 */
abstract class AbstractDefinition {
  
  /**
   * Path specification.
   */
  private String pathSpec = null;
  
  /**
   * Name.
   */
  private String name = null;

  /**
   * Class name.
   */
  private String className = null;

  /**
   * Map with all init parameters.
   *
   * @see #addIniParam
   */
  private final Map<String, String> iniParams = new HashMap<String, String>();

  /**
   * Setter method for instance variable {@link #pathSpec}.
   *
   * @param _pathSpec   path specification
   * @see #pathSpec
   */
  public void setPathSpec(final String _pathSpec)  {
    this.pathSpec = _pathSpec;
  }

  /**
   * Getter method for instance variable {@link #pathSpec}.
   *
   * @return value of instance variable pathSpec
   * @see #pathSpec
   */
  protected String getPathSpec()  {
    return this.pathSpec;
  }

  /**
   * Setter method for instance variable {@link #className}.
   *
   * @param _className  name of the class used from the filter
   * @see #className
   */
  public void setClassName(final String _className)  {
    this.className = _className;
  }

  /**
   * Getter method for instance variable {@link #className}.
   *
   * @return value of instance variable className
   * @see #className
   */
  protected String getClassName()  {
    return this.className;
  }

  /**
   * Setter method for instance variable {@link #name}.
   *
   * @param _name  new value for instance variable name
   * @see #name
   */
  public void setName(final String _name)  {
    this.name = _name;
  }

  /**
   * Getter method for instance variable name.
   *
   * @return value of instance variable name
   * @see #name
   */
  protected String getName()  {
    return this.name;
  }

  /**
   * Adds a new pair of init parameter (key / value).
   *
   * @param _key      key of the init parameter
   * @param _value    value of the init parameter
   * @see #iniParams
   */
  public void addIniParam(final String _key,
                          final String _value)  {
    this.iniParams.put(_key, _value);
  }

  /**
   * Getter method for instance variable {@link #iniParams}.
   *
   * @return value of instance variable iniParams
   * @see #iniParams
   */
  protected Map<String, String> getIniParams()  {
    return this.iniParams;
  }
}
