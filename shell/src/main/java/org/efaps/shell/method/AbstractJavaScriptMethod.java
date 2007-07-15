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

package org.efaps.shell.method;

import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Reader;

import org.apache.commons.cli.Option;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.ImporterTopLevel;

import org.efaps.util.EFapsException;

/**
 *
 * @todo descriptionâ
 * @author tmo
 * @version $Id$
 */
public abstract class AbstractJavaScriptMethod extends AbstractMethod  {
  
  /////////////////////////////////////////////////////////////////////////////
  // instance variables

  /**
   * The mozilla javascript context is stored in this instance variable.
   *
   * @see #execute
   # @see #getJavaScriptContext
   */
  private Context javaScriptContext = null;

  /**
   *
   */
  private org.mozilla.javascript.Scriptable scope = null;

  /////////////////////////////////////////////////////////////////////////////
  // constructors / desctructors

  /**
   * @param _optionName name of the option used to call this method
   * @param _optionDesc description of the option to call this method
   * @param _options  all allowed options implemented by this method
   */
  protected AbstractJavaScriptMethod(final String _optionName,
                                     final String _optionDesc,
                                     final Option... _options)  {
    super(_optionName, _optionDesc, _options);
  }

  /////////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * @todo remove Exception
   * @todo description
   */
  public void execute() throws EFapsException,Exception  {
    // create new javascript context
    this.javaScriptContext = Context.enter();

    this.scope = new ImporterTopLevel(javaScriptContext);;

    // define the context javascript property
    Object wrappedContext = Context.javaToJS(this.javaScriptContext, this.scope);
    ScriptableObject.putProperty(scope, "javaScriptContext", wrappedContext);

    // define the scope javascript property
    Object wrappedScope = Context.javaToJS(this.scope , this.scope);
    ScriptableObject.putProperty(scope, "javaScriptScope", wrappedScope);

    // run init javascript file
//    ClassLoader classLoader = getClass().getClassLoader();
//    Reader in = new InputStreamReader(
//                      classLoader.getResourceAsStream("org/efaps/js/Init.js"));
//
//    evaluate(in, "init"); 

    // execute the doMethod method
    super.execute();
  }

  /**
   * Evalutes given script.
   *
   * @param _in   reader with the script to execute
   * @param _name name
   */
  protected void evaluate(final Reader _in,
                          final String _name) throws IOException
  {
    this.javaScriptContext.evaluateReader(this.scope,
                                          _in, 
                                          _name, 1, null); 
  }

  /////////////////////////////////////////////////////////////////////////////
  // instance getter and setter methods

  /**
   * This is the getter method for instance variable 
   * {@link #javaScriptContext}.
   *
   * @return the value of the instance variable {@link #javaScriptContext}.
   * @see #javaScriptContext
   */
  protected Context getJavaScriptContext()  {
    return this.javaScriptContext;
  }
}
