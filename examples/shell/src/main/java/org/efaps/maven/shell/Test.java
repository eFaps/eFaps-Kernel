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

package org.efaps.maven.shell;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.classworlds.ClassRealm;
import org.codehaus.classworlds.ClassWorld;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;

import org.efaps.maven.run.MavenLoggerOverSLF4J;
import org.efaps.maven.run.Plugin;

/**
 * 
 * @author tim
 * @version $Id$
 */
public class Test {

  public static void main(final String[] _args, final ClassWorld _world) 
  throws Exception,ClassNotFoundException, InstantiationException, IllegalAccessException, ConfigurationException  {

System.out.println("_world.getRealms()="+_world.getRealms());
StringBuilder classpath = new StringBuilder();
for (Object realmObj : _world.getRealms())  {
  ClassRealm realm = (ClassRealm) realmObj;
  for (URL url : realm.getConstituents())  {
    if ("file".equals(url.getProtocol()))  {
      classpath.append(url.getFile()).append(',');
    }
  }
}

/*
String value = "${basedir}/src/main/${tempdir}/versions.xml";

Pattern pattern = Pattern.compile("\\$\\{[^\\}]*\\}");
Matcher matcher = pattern.matcher(value);
StringBuffer sb = new StringBuffer();
while (matcher.find())  {
System.out.println(""+matcher.group());
  matcher.appendReplacement(sb, "dog");
}
matcher.appendTail(sb);

System.out.println(""+sb);
*/
    XMLConfiguration config = new XMLConfiguration();
    config.setDelimiterParsingDisabled(true);
    config.load("/Users/tim/Daten/eFaps/eFapsEclipse/eFaps/examples/shell/config.xml");
//    Properties props = config.getProperties("connection");

    config.setProperty("project.compileClasspathElements", classpath.substring(0, classpath.length() - 1));
//    config.addProperty("test1", "${connection}");
//    config.addProperty("test2", "${classpathElements}");

/*BaseConfiguration baseConf = new BaseConfiguration();
baseConf.setProperty("connection", config.getString("connection"));
baseConf.setProperty("test1", config.getString("test1"));
baseConf.setProperty("test2", config.getString("test2"));
    
    System.out.println("connection="+baseConf.getProperties("connection"));
    System.out.println("test=1"+baseConf.getProperties("test1"));
    System.out.println("test=2"+baseConf.getList("test2"));
*/

    final List<Plugin> pluginsHelp = new ArrayList<Plugin>();
    final List<Plugin.Goal> goals = new ArrayList<Plugin.Goal>();
    for (final String arg : _args)  {
      if (arg.startsWith("-"))  {
        final int index = arg.indexOf('=');
        config.addProperty((index > 0) ? arg.substring(1, index) : arg.substring(1),
                           (index > 0) ? arg.substring(index + 1) : null);
      } else  {
        final int index = arg.indexOf(':');
        final Plugin plugin = Plugin.getPlugin((index > 0) ? arg.substring(0, index) : arg);
        if (plugin == null)  {
System.out.println("plugin '" + arg.substring(0, index) + "' not found");
        } else if (index <= 0) {
          pluginsHelp.add(plugin);
        } else  {
          final Plugin.Goal goal = plugin.getGoal(arg.substring(index + 1));
          if (goal == null)  {
System.out.println("plugin '" + arg.substring(0, index) + "' goal '" + arg.substring(index + 1) + "' not found");
            pluginsHelp.add(plugin);
          } else  {
            goals.add(goal);
          }
        }
      }
    }
//System.out.println("---props=\n"+props+"\n---");
    if (pluginsHelp.isEmpty() && goals.isEmpty())  {
      Plugin.printAllPluginsHelp();
    } else  {
      for (final Plugin plugin : pluginsHelp)  {
        plugin.printHelp();
      }

      MavenLoggerOverSLF4J mavenLogger = new MavenLoggerOverSLF4J();

      for (Plugin.Goal goal : goals)  {
        goal.execute(config, mavenLogger);
      }
    }

  }
}
