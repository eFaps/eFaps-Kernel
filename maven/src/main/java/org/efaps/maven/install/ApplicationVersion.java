/*
 * Copyright 2003-2006 The eFaps Team
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

package org.efaps.maven.install;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

import org.efaps.admin.program.esjp.Compiler;
import org.efaps.update.Install;
import org.efaps.util.EFapsException;

/**
 * 
 * @author tmo
 * @version $Id$
 * @todo description
 */
public class ApplicationVersion implements Comparable /* < ApplicationVersion > */<Object>{

  // ///////////////////////////////////////////////////////////////////////////
  // instance variables

  /**
   * The number of the version is stored in this instance variable.
   * 
   * @see #setNumber
   * @see #getNumber
   */
  private long number = 0;

  /**
   * Store the information wether a compile must be done after installing this
   * version.
   *
   * @see #setCompile
   */
  private boolean compile = false;

  /**
   * Project classpath.
   *
   * @see #setClasspathElements
   */
  private List<String> classpathElements = null;

  /////////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * Installs the xml update scripts of the schema definitions for this version
   * defined in {@link #number}.
   */
  public void install(final Install _install) throws EFapsException, Exception {

    _install.install(this.number);

    compile();
  }

  /**
   * Compile esjp's in the database (if the compile flag is set).
   */
  private void compile() throws EFapsException  {
    if (this.compile)  {
      (new Compiler(this.classpathElements)).compile();
    }
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
  public String toString() {
    return new ToStringBuilder(this)
            .append("number", this.number).toString();
  }
}
