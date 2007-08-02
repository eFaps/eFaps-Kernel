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

package org.efaps.maven;

import java.io.IOException;
import java.io.Reader;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 *
 * @todo descriptionâ
 * @author tmo
 * @version $Id$
 */
public abstract class AbstractJavaScriptMojo extends EFapsAbstractMojo  {
  
  /////////////////////////////////////////////////////////////////////////////
  // instance variables

  /**
   * The mozilla javascript context is stored in this instance variable.
   *
   * @see #execute
   # @see #getJavaScriptContext
   */
  private final Context javaScriptContext;

  /**
   *
   */
  private final Scriptable scope;

  /////////////////////////////////////////////////////////////////////////////
  // constructor / destructors

  protected AbstractJavaScriptMojo()  {
    // create new javascript context
    this.javaScriptContext = Context.enter();

    this.scope = new ImporterTopLevel(this.javaScriptContext);;

    // define the context javascript property
    putPropertyInJS("javaScriptContext", this.javaScriptContext);

    // define the scope javascript property
    putPropertyInJS("javaScriptScope", this.scope);
  }

  /////////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * Evalutes given script.
   *
   * @param _in   reader with the script to execute
   * @param _name name
   */
  protected void evaluate(final Reader _in,
                          final String _name) throws IOException  {

    // evalute script defined through the reader
    this.javaScriptContext.evaluateReader(this.scope,
                                          _in, 
                                          _name, 1, null); 
  }

  /**
   * Sets a property in the JavaScript Context.
   *
   * @param _name   name of the property
   * @param _object value of the property
   */
  protected void putPropertyInJS(final String _name,
                                 final Object _object)  {
    Object wrapped = Context.javaToJS(_object, this.scope);
    ScriptableObject.putProperty(this.scope, _name, wrapped);
  }
}
