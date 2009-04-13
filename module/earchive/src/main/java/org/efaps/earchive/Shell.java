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

package org.efaps.earchive;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.codehaus.classworlds.ClassRealm;
import org.codehaus.classworlds.ClassWorld;
import org.mortbay.log.Log;

import org.efaps.admin.runlevel.RunLevel;
import org.efaps.db.Context;
import org.efaps.earchive.svn.SVNProxyServer;
import org.efaps.init.StartupDatabaseConnection;
import org.efaps.init.StartupException;
import org.efaps.util.EFapsException;


/**
 *
 * @author Jan Moxter
 * @version $Id$
 */
public class Shell {

  public static void main(final String[] _args)  {

    final XMLConfiguration config = new XMLConfiguration();
    config.setDelimiterParsingDisabled(true);

    for (final String arg : _args)  {
      if (arg.startsWith("-"))  {
        final int index = arg.indexOf('=');
        config.addProperty((index > 0) ? arg.substring(1, index) : arg.substring(1),
                           (index > 0) ? arg.substring(index + 1) : null);
      }
    }
    final String configUrl
                   = (String) config.getProperty("Dshell.parameter.configFile");
    final File file = new File(configUrl);
    if (configUrl != null) {
      final XMLConfiguration config2 = new XMLConfiguration();
      config2.setDelimiterParsingDisabled(true);
        try {
          config2.load(file);
        } catch (final ConfigurationException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        System.out.println(config2.getString("type"));
        final String type = config2.getString("type");
        final String factory = config2.getString("factory");
        final String connection =  config2.getString("connection");
        try {
          StartupDatabaseConnection.startup(type,
              factory,
              convertToMap(connection),
              "org.objectweb.jotm.Current");
          reloadCache();
        } catch (final StartupException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (final EFapsException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } final SVNProxyServer server = SVNProxyServer.get();
        server.start();
    } else {
      //TODO fehler
    }

  }



  public static void main(final String[] _args, final ClassWorld _world) {
  System.out.println("_world.getRealms()=" + _world.getRealms());
    final StringBuilder classpath = new StringBuilder();
    for (final Object realmObj : _world.getRealms()) {
      final ClassRealm realm = (ClassRealm) realmObj;
      for (final URL url : realm.getConstituents()) {
        if ("file".equals(url.getProtocol())) {
          classpath.append(url.getFile()).append(',');
        }
      }
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
  protected static Map<String, String> convertToMap(final String _text)  {
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
  /**
   * Reloads the internal eFaps cache.
   */
  protected static void reloadCache() throws EFapsException  {
    Context.begin();
    RunLevel.init("shell");
    RunLevel.execute();
    Context.rollback();
    Log.info("Cache reloaded");
  }


}
