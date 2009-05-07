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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.plugin.MojoExecutionException;

import org.efaps.maven_java5.org.apache.maven.tools.plugin.Goal;
import org.efaps.maven_java5.org.apache.maven.tools.plugin.Parameter;


/**
 * Opens the console to the derby database. The Mojo uses the eFaps connection
 * information to connect directly to the derby database including the user
 * name and password.
 *
 * @author tmo
 * @version $Id$
 */
@Goal(name = "console")
public class IJMojo extends DerbyAbstractMojo
{
  /**
   * Holds all properties of the connection to the database. The properties
   * are separated by a comma.
   */
  @Parameter(expression = "${org.efaps.db.connection}",
             required = true)
  private String connection;

  /**
   * Executes the console of the derby database.
   */
  public void execute() throws MojoExecutionException
  {
    getLog().info("Console Derby Database");
    initSystemProperties();

    final Map<String,String> map = convertToMap(this.connection);

    System.setProperty("ij.driver",                   map.get("driverClassName"));
    System.setProperty("ij.connection.eFaps",         map.get("url"));
    System.setProperty("ij.user",                     map.get("username"));
    System.setProperty("ij.password",                 map.get("password"));
    System.setProperty("ij.showNoConnectionsAtStart", "true");

    try {
      org.apache.derby.tools.ij.main(new String[]{});
    } catch (final IOException e) {
      throw new MojoExecutionException("Execution of IJ failed", e);
    }
  }

  /**
   * Separates all key / value pairs of given text string.<br/>
   * Evaluation algorithm:<br/>
   * Separates the text by all found commas (only if in front of the comma is
   * no back slash). This are the key / value pairs. A key / value pair is
   * separated by the first equal ('=') sign.
   *
   * @param _text   text string to convert to a key / value map
   * @return Map of strings with all found key / value pairs
   */
  protected Map<String, String> convertToMap(final String _text)  {
    final Map<String, String> properties = new HashMap<String, String>();

    // separated all key / value pairs
    final Pattern pattern = Pattern.compile("(([^\\\\,])|(\\\\,)|(\\\\))*");
    final Matcher matcher = pattern.matcher(_text);

    while (matcher.find())  {
      final String group = matcher.group().trim();
      if (group.length() > 0)  {
        // separated key from value
        final int index = group.indexOf('=');
        final String key = (index > 0)
                           ? group.substring(0, index).trim()
                           : group.trim();
        final String value = (index > 0)
                             ? group.substring(index + 1).trim()
                             : "";
        properties.put(key, value);
      }
    }

    return properties;
  }
}
