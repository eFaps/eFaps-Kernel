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

package org.efaps.maven.plugin.goal.efaps.install;

import static org.mozilla.javascript.Context.enter;
import static org.mozilla.javascript.Context.javaToJS;
import static org.mozilla.javascript.ScriptableObject.putProperty;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Scriptable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.efaps.admin.program.esjp.Compiler;
import org.efaps.admin.program.staticsource.AbstractSourceCompiler;
import org.efaps.admin.runlevel.RunLevel;
import org.efaps.db.Context;
import org.efaps.update.Install;
import org.efaps.util.EFapsException;

/**
 *
 * @author tmo
 * @version $Id$
 * @todo description
 */
public class ApplicationVersion implements Comparable /* < ApplicationVersion > */<Object>{

  /////////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used to give logging information of this class.
   */
  private final static Logger LOG = LoggerFactory.getLogger(ApplicationVersion.class);

  /////////////////////////////////////////////////////////////////////////////
  // instance variables

  /**
   * The number of the version is stored in this instance variable.
   *
   * @see #setNumber
   * @see #getNumber
   */
  private long number = 0;

  /**
   * Store the information weather a compile must be done after installing this
   * version.
   *
   * @see #setCompile
   */
  private boolean compile = false;

  /**
   * Is a login for this version needed? This means if a new transaction is
   * started, a login with given user is made. The default value is
   * <i>true</i>.
   *
   * @see #setLoginNeeded
   */
  private boolean loginNeeded = true;

  /**
   * Is a reload cache for this version needed? This means before the
   * installation of this version starts, a reload cache is done. The default
   * value is <i>true</i>.
   *
   * @see #setReloadCacheNeeded
   */
  private boolean reloadCacheNeeded = true;

  /**
   * Project class path.
   *
   * @see #setClasspathElements
   */
  private List<String> classpathElements = null;

  /**
   * List of all scripts for this version.
   *
   * @see #addScript
   */
  private final List<Script> scripts = new ArrayList<Script>();

  /**
   * Description of this version.
   */
  private final StringBuilder description = new StringBuilder();

  /////////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * Installs the XML update scripts of the schema definitions for this version
   * defined in {@link #number}.
   *
   * @param _install              install instance with all cached XML
   *                              definitions
   * @param _latestVersionNumber  latest version number (defined in the
   *                              version.xml file)
   * @param _userName             name of logged in user
   * @param _password             password of logged in user
   */
  public void install(final Install _install,
                      final long _latestVersionNumber,
                      final String _userName,
                      final String _password) throws EFapsException, Exception {
    // reload cache if needed
    if (this.reloadCacheNeeded)  {
      Context.begin();
      RunLevel.init("shell");
      RunLevel.execute();
      Context.rollback();
    }

    // start transaction (with username if needed)
    if (this.loginNeeded)  {
      Context.begin(_userName);
    } else  {
      Context.begin();
    }

    _install.install(this.number, _latestVersionNumber);

    // commit transaction
    Context.commit();

    // execute all scripts
    for (final Script script : this.scripts)  {
      script.execute(_userName, _password);
    }

    // Compile esjp's in the database (if the compile flag is set).
    if (this.compile)  {
      compileAll(_userName);
    }

  }

  /**
   * Compiles the ESJP's and all Cascade Styles Sheets within eFaps.
   *
   * @param _userName   name of logged in user for which the compile is done
   *                    (could be also <code>null</code>)
   * @throws EFapsException if compiled failed
   */
  public void compileAll(final String _userName) throws EFapsException  {
    Context.begin(_userName);
    RunLevel.init("shell");
    RunLevel.execute();
    Context.rollback();

    Context.begin(_userName);
    (new Compiler(this.classpathElements)).compile();
    AbstractSourceCompiler.compileAll();
    Context.commit();
  }

  /**
   * Adds a new Script to this version.
   *
   * @param _code       code of script to execute
   * @param _name       file name of the script
   * @param _function   name of function which is called
   */
  public void addScript(final String _code,
                        final String _name,
                        final String _function)
  {
    this.scripts.add(new Script(_code, _name, _function));
  }

  /**
   * Append a description for this version.
   *
   * @param _desc text of description to append
   * @see #description
   */
  public void appendDescription(final String _desc)
  {
    if (_desc != null)  {
      this.description.append(_desc.trim()).append("\n");
    }
  }

  /**
   * The description for this version is returned. If no description exists,
   * a zero length description is returned.
   *
   * @return string value of instance variable {@link #description}
   * @see #description
   */
  public String getDescription() {
    return this.description.toString().trim();
  }

  /**
   * Compares this application version with the specified application version.<br/>
   * The method compares the version number of the application version. To do
   * this, the method {@link java.lang.Long#compareTo} is called.
   *
   * @param _compareTo
   *          application version instance to compare to
   * @return a negative integer, zero, or a positive integer as this application
   *         version is less than, equal to, or greater than the specified
   *         application version
   * @see java.lang.Long#compareTo
   * @see java.lang.Comparable#compareTo
   */
  public int compareTo(final Object _compareTo) {
    return new Long(this.number)
        .compareTo(((ApplicationVersion) _compareTo).number);
  }



  /////////////////////////////////////////////////////////////////////////////
  // instance getter and setter methods

  /**
   * This is the setter method for instance variable {@link #number}.
   *
   * @param _number
   *          new value for instance variable {@link #number}
   * @see #number
   * @see #getNumber
   */
  public void setNumber(final long _number) {
    this.number = _number;
  }

  /**
   * This is the getter method for instance variable {@link #number}.
   *
   * @return value of instance variable {@link #number}
   * @see #number
   * @see #setNumber
   */
  public Long getNumber() {
    return this.number;
  }

  /**
   * This is the setter method for instance variable {@link #compile}.
   *
   * @param _compile new value for instance variable {@link #compile}
   * @see #compile
   */
  public void setCompile(final boolean _compile) {
    this.compile = _compile;
  }

  /**
   * This is the setter method for instance variable {@link #loginNeeded}.
   *
   * @param _compile new value for instance variable {@link #loginNeeded}
   * @see #loginNeeded
   */
  public void setLoginNeeded(final boolean _loginNeeded) {
    this.loginNeeded = _loginNeeded;
  }

  /**
   * This is the setter method for instance variable {@link #reloadCacheNeeded}.
   *
   * @param _compile new value for instance variable {@link #reloadCacheNeeded}
   * @see #reloadCacheNeeded
   */
  public void setReloadCacheNeeded(final boolean _reloadCacheNeeded) {
    this.reloadCacheNeeded = _reloadCacheNeeded;
  }

  /**
   * This is the setter method for instance variable {@link #classpathElements}.
   *
   * @param _compile new value for instance variable {@link #classpathElements}
   * @see #classpathElements
   */
  public void setClasspathElements(final List<String> _classpathElements) {
    this.classpathElements = _classpathElements;
  }

  /**
   * Returns a string representation with values of all instance variables.
   *
   * @return string representation of this Application
   */
  @Override
  public String toString() {
    return new ToStringBuilder(this)
            .append("number", this.number).toString();
  }

  /////////////////////////////////////////////////////////////////////////////

  /**
   * Class used to store information of needed called scripts within an
   * application version.
   */
  private class Script {

    /**
     * Script code to execute.
     */
    final String code;

    /**
     * File name of the script (within the class path).
     */
    final String fileName;

    /**
     * Name of called function.
     */
    final String function;

    /**
     * Constructor to initialize a script.
     *
     * @param _code       script code
     * @param _fileName   script file name
     * @param _function   called function name
     */
    private Script(final String _code,
                   final String _fileName,
                   final String _function)  {
      this.code = (_code == null) || ("".equals(_code.trim()))
                  ? null
                  : _code.trim();
      this.fileName = _fileName;
      this.function = _function;
    }

    /**
     * Executes this script.
     *
     * @param _userName   name of logged in user
     * @param _password   password of logged in user
     * @throws IOException
     */
    public void execute(final String _userName,
                        final String _password) throws IOException {

      // create new javascript context
      final org.mozilla.javascript.Context javaScriptContext = enter();

      final Scriptable scope = new ImporterTopLevel(javaScriptContext);;

      // define the context javascript property
      putProperty(scope, "javaScriptContext", javaScriptContext);

      // define the scope javascript property
      putProperty(scope, "javaScriptScope", scope);

      putProperty(scope, "EFAPS_LOGGER",   javaToJS(LOG, scope));
      putProperty(scope, "EFAPS_USERNAME", javaToJS(_userName, scope));
      putProperty(scope, "EFAPS_PASSWORD", javaToJS(_userName, scope));

      // evaluate java script file (if defined)
      if (this.fileName != null)  {
        LOG.info("Execute script file '" + this.fileName + "'");
        final Reader in = new InputStreamReader(
            getClass().getClassLoader().getResourceAsStream(this.fileName));
        javaScriptContext.evaluateReader(scope, in, this.fileName, 1, null);
        in.close();
      }

      // evaluate script code (if defined)
      if (this.code != null)  {
        javaScriptContext.evaluateReader(scope,
            new StringReader(this.code),
            "Executing script code of version " + ApplicationVersion.this.number,
            1,
            null);
      }

      // evalute script defined through the reader
      if (this.function != null)  {
        LOG.info("Execute script function '" + this.function + "'");
        javaScriptContext.evaluateReader(scope,
                                         new StringReader(this.function),
                                         this.function,
                                         1,
                                         null);
      }
    }
  }
}
