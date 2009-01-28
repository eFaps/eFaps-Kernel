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

package org.efaps.maven.run;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.digester.CallMethodRule;
import org.apache.commons.digester.Digester;
import org.apache.commons.digester.ExtendedBaseRules;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * @author tmo
 * @version $Id$
 */
public class Plugin {

  /**
   * Logger instance used to log error thrown by this plugin.
   */
  private final static Logger LOGGER
          = LoggerFactory.getLogger(MavenLoggerOverSLF4J.class);

  /**
   * Prefix used to get parameter values from system properties.
   */
  private final static String PREFIX_PROPERTIES = "shell.parameter";

  /**
   * All known plugins are stored in this instance variable.
   */
  private final static Map<String, Plugin> PLUGINS
          = new TreeMap<String, Plugin>();

  /** Name of this plugin */
  private final String name;

  /** Map of all goals (key is name of goal, value is the goal itself) */
  private final Map<String, Goal> goals = new TreeMap<String, Goal>();

  /**
   * 
   * @param _name name of the searched plugin
   * @return related plugin instance for given name; or <code>null</code> if
   *         not found
   */
  public static Plugin getPlugin(final String _name)  {
    return PLUGINS.get(_name);
  }

  public static void printAllPluginsHelp()  {
    System.out.println("Following plugins are known:");
    for (Plugin plugin: PLUGINS.values())  {
      System.out.println(plugin.name);
    }
  }

  /**
   * z
   */
  static  {
    try {
      Enumeration<URL> urls = Plugin.class.getClassLoader().getResources("META-INF/maven/plugin.xml");
      while (urls.hasMoreElements())  {
        URL url = urls.nextElement();
        Plugin plugin = new Plugin(url);
        PLUGINS.put(plugin.getName(), plugin);
      }
    } catch (IOException e)  {
e.printStackTrace();
    } catch (SAXException e)  {
e.printStackTrace();
    } catch (ParserConfigurationException e) {
e.printStackTrace();
    }
  }
   
  /**
   *
   * @param _name name of the plugin
   * @see #name
   */
  private Plugin(final URL _url) throws IOException, SAXException, ParserConfigurationException  {
    System.out.println("read plugin "+_url);

    Digester digester = new Digester();
    digester.setValidating(false);
    digester.setRules(new ExtendedBaseRules());

    // plugin name
    PluginRule pluginRule = new PluginRule();
    digester.addRule("plugin", pluginRule);
    digester.addCallParam("plugin/goalPrefix", 0);

    // goal
    digester.addRule("plugin/mojos/mojo", new GoalRule());
    digester.addCallParam("plugin/mojos/mojo/goal", 0);
    digester.addCallParam("plugin/mojos/mojo/implementation", 1);
    digester.addCallParam("plugin/mojos/mojo/description", 2);

    // goal parameter
    digester.addCallMethod("plugin/mojos/mojo/parameters/parameter", "addParameter", 6, 
            new Class[]{String.class, String.class, String.class, String.class, Boolean.class, Boolean.class});
    digester.addCallParam("plugin/mojos/mojo/parameters/parameter/name", 0);
    digester.addCallParam("plugin/mojos/mojo/parameters/parameter/alias", 1);
    digester.addCallParam("plugin/mojos/mojo/parameters/parameter/type", 2);
    digester.addCallParam("plugin/mojos/mojo/parameters/parameter/description", 3);
    digester.addCallParam("plugin/mojos/mojo/parameters/parameter/required", 4);
    digester.addCallParam("plugin/mojos/mojo/parameters/parameter/editable", 5);

    // goal configuration (default values)
    DefaultValueRule.init(digester);

    digester.parse(_url.openStream());

    this.name = pluginRule.name;
  }

  public void printHelp()  {
    final int nameMaxLength = 20;
    final int descMaxLength = 53;
    System.out.print("\n+-");
    for (int i = 0; i < nameMaxLength; i++)  {
      System.out.print('-');
    }
    System.out.print("-+-");
    for (int i = 0; i < descMaxLength; i++)  {
      System.out.print('-');
    }
    System.out.print("-+\n");
    for (Goal goal: this.goals.values())  {
      final String[] nameLines = WordUtils.wrap(this.name + ":" + goal.name, nameMaxLength).split("\n");
      final String[] descLines = WordUtils.wrap(goal.description, descMaxLength).split("\n");
      for (int i = 0; (i < nameLines.length) || (i < descLines.length); i++)  {
        final String nameLine = (i < nameLines.length) ? nameLines[i] : "";
        final String descLine = (i < descLines.length) ? descLines[i] : "";
        System.out.print("| ");
        System.out.print(nameLine);
        for (int j = nameMaxLength - nameLine.length(); j > 0; j--)  {
          System.out.print(" ");
        }
        System.out.print(" | ");
        System.out.print(descLine);
        for (int j = descMaxLength - descLine.length(); j > 0; j--)  {
          System.out.print(" ");
        }
        System.out.print(" |\n");
      }
      System.out.print("+-");
      for (int i = 0; i < nameMaxLength; i++)  {
        System.out.print('-');
      }
      System.out.print("-+-");
      for (int i = 0; i < descMaxLength; i++)  {
        System.out.print('-');
      }
      System.out.print("-+\n");
    }
  }

  /**
   * 
   * @param _goalName     name of goal
   * @param _className    class implementing the goal
   * @param _description  description of the goal
   */
  public void addGoal(final Goal _goal)  {
    this.goals.put(_goal.getName(), _goal);
  }

  public Goal getGoal(final String _goalName)  {
    return this.goals.get(_goalName);
  }

  /**
   * @return the name
   * @see #name
   */
  public String getName() {
    return this.name;
  }

  /**
   * Returns a string representation with values of all instance variables
   * of a plugin.
   *
   * @return string representation of this plugin
   */
  public String toString()  {
    return new ToStringBuilder(this)
      .appendSuper(super.toString())
      .append("name",         this.name)
      .append("goals",        this.goals)
      .toString();
  }

  /////////////////////////////////////////////////////////////////////////////

  /**
   * 
   */
  public class Goal  {
    /** Name of goal. */
    private String name = null;

    /** Name of Class implementing this goal. */
    private String className = null;
    
    /** Description of this goal. */
    private String description = null;

    /** Parameters of this goal by parameter name / alias. */
    private final Map<String, Parameter> parameters
            = new TreeMap<String, Parameter>();

    /** Parameter of this goal by parameter name (field name inside class) */
    private final Map<String, Parameter> fieldParameters
            =  new HashMap<String, Parameter>();

    /** 
     * Stores all predefined parameter values (configurations)
     * 
     * @see #addConfiguration(String, String)
     */
    private final Map<String, String> configurations
            = new HashMap<String, String>();

    private Goal()  {
    }

    /**
     * Evaluation of parameter values:
     * <ol>
     * <li>check, if goal parameter is defined as parameter of the application
     *     </li>
     * <li>if not defined, check if a system property is defined (with syntax 
     *     {@link #PREFIX_PROPERTIES} plus point plus name of parameter, e.g.
     *     <code>shell.parameter.port</code>)</li>
     * <li>if not defined, check if an environment variable with the same
     *     name as the system property exists</li>
     * <li>if not defined, check if a default value of the parameter is
     *     defined</li>
     * </ol>
     *
     * @param _config         xml configuration instance with all parameters of
     *                        the application
     * @param _mavenLogger    maven logger instance
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException   mojo class could not be instanciated
     * @throws MojoExecutionException
     * @throws MojoFailureException
     * @see #PREFIX_PROPERTIES
     * @todo use on exceptions instead!
     */
    @SuppressWarnings("unchecked")
    public void execute(final XMLConfiguration _config,
                        final MavenLoggerOverSLF4J _mavenLogger) 
                        throws ClassNotFoundException,
                               InstantiationException, IllegalAccessException,
                               MojoExecutionException, MojoFailureException  {

      final Class<Mojo> mojoClass = (Class<Mojo>) Class.forName(this.className);
      final Mojo mojo = mojoClass.newInstance();
      final Set<Parameter> filled = new HashSet<Parameter>();

      // fill instance variables
      Class clazz = mojoClass;
      while (clazz != null)  {
        for (final Field field : clazz.getDeclaredFields())  {
          final Plugin.Parameter param = this.fieldParameters.get(field.getName());
          if (param != null)   {
            if (!_config.containsKey(param.getParamName()))  {
              // system property
              final String propName = new StringBuilder()
                  .append(PREFIX_PROPERTIES).append('.')
                  .append(param.getParamName()).toString();
              final String propValue = System.getProperty(propName);
              if (propValue != null)  {
                _config.setProperty(param.getParamName(), propValue);
              } else  {
                // environment variable
                final String envValue = System.getenv(propName);
                if (envValue != null)  {
                  _config.setProperty(param.getParamName(), envValue);
                // default value
                } else if (param.getDefaultValue() != null)  {
                  _config.setProperty(param.getParamName(),
                                      param.getDefaultValue());
                }
              }
            }
            if (_config.containsKey(param.getParamName()))  {
              filled.add(param);
              param.setObjectParameter(mojo, field, _config);
            }
          }
        }
        clazz = clazz.getSuperclass();
      }

      // check for all required fields
      boolean allFilled = true;
      for (final Parameter param : this.fieldParameters.values())  {
        if (!filled.contains(param) && param.isRequired())  {
          allFilled = false;
System.out.println("Parameter " + param + " not defined");
        }
      }

      // execute only if all required attributes are filled
      if (allFilled)  {
        mojo.setLog(_mavenLogger);
        mojo.execute();
      }
    }

    /**
     * 
     * @param _name         name of goal
     * @param _className    name of class implementing goal
     * @param _description  description of goal
     */
    protected void endParsing(final String _name, 
                              final String _className,
                              final String _description)  {
      this.name = _name;
      this.className = _className;
      this.description = _description;
      for (Map.Entry<String, String> conf : this.configurations.entrySet())  {
        Parameter param = this.fieldParameters.get(conf.getKey());
        param.defaultValue = conf.getValue();
      }
    }

    /**
     * 
     * @param _name         name of parameter
     * @param _alias        alias name of parameter
     * @param _className    name of class implementing parameter
     * @param _description  description of parameter
     * @param _required     is parameter required?
     * @param _editable     is parameter editable?
     */
    public void addParameter(final String _name, final String _alias, final String _className, final String _description, final boolean _required, final boolean _editable)  {
      final Parameter para = new Parameter(_name, _alias,
              _className, _description, _required, _editable);
      this.parameters.put(para.getParamName(), para);
      this.fieldParameters.put(para.getFieldName(), para);
    }

    /**
     * 
     * @param _paramName  name of parameter
     * @param _value      configuration value of parameter
     */
    protected void addConfiguration(final String _paramName,
                                    final String _value)  {
      this.configurations.put(_paramName, _value);
    }

    /**
     * 
     */
    public Parameter getParameter(final String _name)  {
      return this.parameters.get(_name);
    }

    /**
     * 
     */
    public Parameter getParameterByFieldName(final String _fieldName)  {
      return this.fieldParameters.get(_fieldName);
    }

    /**
     * @return the name
     */
    public String getName() {
      return name;
    }

    /**
     * @return the className
     */
    public String getClassName() {
      return className;
    }

    /**
     * @return the description
     */
    public String getDescription() {
      return description;
    }

    /**
     * Returns a string representation with values of all instance variables
     * of goal instance.
     *
     * @return string representation of this goal
     */
    @Override
    public String toString()  {
      return new ToStringBuilder(this)
          .appendSuper(super.toString())
          .append("name",           this.name)
          .append("className",      this.className)
          .append("configurations", this.configurations)
          .append("description",    this.description)
          .toString();
    }
  }

  /////////////////////////////////////////////////////////////////////////////
  
  /**
   * 
   * 
   */
  public static class Parameter  {
    /** Field Name of parameter (used inside classes. */
    private final String fieldName;

    /** Parameter name of parameter (maybe differ from {@link #fieldName}). */
    private final String paramName;

    /** Class Name of parameter. */
    private final String className;

    /** Description of parameter. */
    private final String description;

    /** Is this parameter required? */
    private final boolean required;

    /** Is this parameter editable? */
    private final boolean editable;

    /** Stores the configuration value (default value of this parameter). */
    private String defaultValue = null;

    Parameter(final String _name, final String _alias, final String _className, final String _description, final boolean _required, final boolean _editable)  {
      this.fieldName = _name;
      if ((_alias != null) && !"".equals(_alias))  {
        this.paramName = _alias;
      } else  {
        this.paramName = _name;
      }
      this.className = _className;
      this.description = _description;
      this.required = _required;
      this.editable = _editable;
    }

    /**
     * 
     * @param _mojo     mojo instance which should be updated
     * @param _field    field of the mojo instance which should be updated
     * @param _config   XML configuration with property
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    protected void setObjectParameter(final Mojo _mojo,
                                      final Field _field,
                                      final XMLConfiguration _config) 
    throws IllegalArgumentException  {
      boolean accessible = _field.isAccessible();
      _field.setAccessible(true);
      MapClass2Update class2Update = MapClass2Update.get(this.className);
      if (class2Update == null)  {
System.out.println("no mapping for '" + this.className + "' found");
      } else  {
        try {
          class2Update.setObjectParameter(_mojo, _field, _config, this);
        // the illegal access exception is catched, because the field itself
        // is set accessible
        } catch (IllegalAccessException e) {
          LOGGER.error("could not access field '" + _field.getName() + "'", e);
        }
      }
      _field.setAccessible(accessible);
    }

    /**
     * @return the name
     */
    public String getFieldName() {
      return this.fieldName;
    }

    /**
     * @return the alias
     */
    public String getParamName() {
      return this.paramName;
    }

    /**
     * @return the className
     */
    public String getClassName() {
      return this.className;
    }

    /**
     * @return the description
     */
    public String getDescription() {
      return this.description;
    }

    /**
     * @return the description
     */
    public String getDefaultValue() {
      return this.defaultValue;
    }

    /**
     * @return the required
     */
    public boolean isRequired() {
      return this.required;
    }

    /**
     * @return the editable
     */
    public boolean isEditable() {
      return this.editable;
    }

    /**
     * Returns a string representation with values of all instance variables
     * of goal instance.
     *
     * @return string representation of this goal
     */
    @Override
    public String toString()  {
      return new ToStringBuilder(this)
        .appendSuper(super.toString())
        .append("name",           this.fieldName)
        .append("alias",          this.paramName)
        .append("className",      this.className)
        .append("description",    this.description)
        .append("required",       this.required)
        .append("editable",       this.editable)
        .append("configuration",  this.defaultValue)
        .toString();
    }
  }
  
  /////////////////////////////////////////////////////////////////////////////
  protected class PluginRule extends CallMethodRule  {

    /** Stores the name of the plugin. */
    private String name = null;

    /**
     * @throws ParserConfigurationException
     */
    protected PluginRule() throws ParserConfigurationException {
      super(null, 1);
    }

    @Override
    public void end() throws java.lang.Exception  {
      final Object[] params = (Object[])getDigester().popParams();
      this.name = (String) params[0];
    }
  }

  /////////////////////////////////////////////////////////////////////////////
  protected class GoalRule extends CallMethodRule  {
    /**
     * @throws ParserConfigurationException
     */
    protected GoalRule() throws ParserConfigurationException {
      super(null, 3);
    }

    @Override
    public void begin(final Attributes _attributes) throws Exception {
      super.begin(_attributes);
      Goal goal = new Goal();
      getDigester().push(goal);
    }

    @Override
    public void end() throws java.lang.Exception  {
      final Goal goal = (Goal) getDigester().peek();
      final Object[] params = (Object[])getDigester().popParams();
      goal.endParsing((String) params[0], (String) params[1], (String) params[2]);
      addGoal(goal);
    }
  }

  /////////////////////////////////////////////////////////////////////////////

  /**
   * Digester Rule to evaluate configurations within the plugin.xml file, e.g.:
   * <br/>
   * <code>&lt;port default-value="8888"/&gt;${org.efaps.db.port}&lt;/port&gt;
   * </code><br/>
   * The rule is used to define the default values for parameters. If an
   * expression (the <code>${org.efaps.db.port}</code> stuff) is defined, this
   * is the default value, otherwise if a default value (the <code>8888</code>
   * stuff) is defined and length of the expression is zero, the default value
   * is used.<br/>
   * The is defined as tag name (the <code>&lt;port&gt;</code> stuff).
   * 
   * @see #Plugin using this rule to evaluate default values for a gaol of a
   *              plugin
   */
  protected static class DefaultValueRule extends CallMethodRule  {

    /**
     * Adds an instance of this rule to the given digester instance.
     *
     * @param _digester digester instance
     * @throws ParserConfigurationException from called methods
     */
    static void init(final Digester _digester) throws ParserConfigurationException  {
      _digester.addRule("plugin/mojos/mojo/configuration/*", new DefaultValueRule());
      _digester.addCallParam("plugin/mojos/mojo/configuration/*", 0);
      _digester.addCallParam("plugin/mojos/mojo/configuration/*", 1, "default-value");
    }

    
    /**
     * The constructor initialize the rule with two parameters.
     *
     * @throws ParserConfigurationException from called constructor in
     *         {@link CallMethodRule#CallMethodRule(String, int)}
     */
    private DefaultValueRule() throws ParserConfigurationException {
      super(null, 2);
    }

    /**
     * End of parsing. Get both defined parameters, evaluate which is the
     * default value (expression or default value) and stores the default value
     * for given key at the goal.
     */
    @Override
    public void end() throws java.lang.Exception  {
      final Object[] params = (Object[])getDigester().popParams();
      final Goal goal = (Goal) getDigester().peek();
      final String expr = (String) params[0];
      final String def = (String) params[1];
      final String value = (def != null) && (expr.trim().length() == 0)
                           ? def
                           : expr;
      goal.addConfiguration(getDigester().getCurrentElementName(), value);
    }
  }
}
