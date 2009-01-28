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

package org.efaps.maven.derby;

import java.util.Properties;
import java.util.Map.Entry;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.tools.plugin.Parameter;

/**
 * The class is used to initialize the system properties depending on given
 * property values for the maven goal settings.
 *
 * @author tmo
 * @version $Id$
 */
abstract class DerbyAbstractMojo extends AbstractMojo
{
  /**
   * Derby specific Properties which will be defined as system properties.
   */
  @Parameter
  private Properties properties;

  /**
   * Initialize system properties with all values defined in the
   * {@link #properties}.
   *
   * @see #properties
   */
  protected void initSystemProperties()
  {
    if (this.properties != null)  {
      for (final Entry<Object,Object> entry : this.properties.entrySet())  {
        final String name = (String) entry.getKey();
        final String value = (String) entry.getValue();
        if (getLog().isInfoEnabled())  {
          getLog().info("- using " + name + " = " + value);
        }
        System.setProperty(name, value);
      }
    }
  }
}
