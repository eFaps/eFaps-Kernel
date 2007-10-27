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

package org.efaps.maven.plugin.install;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.tools.ant.DirectoryScanner;

/**
 * This Class reads all Files from a given Directory, including the
 * Subdirectories and their Files.<br/>
 * 
 * 
 * @author tmo
 * @version $Id$
 */
public class FileSet  {

  /**
   * Stores the starting directory for which this file set is defined. Default
   * value is current directory.
   * 
   * @see #setRootDirectory
   */
  private String rootDirectory = ".";

  /**
   * Stores all includes of this fileset.
   * 
   * @see #addInclude
   */
  private Set<String> includes  = new HashSet<String>();

  /**
   * Stores all excludes of this fileset.
   * 
   * @see #addExclude
   */
  private Set<String> excludes  = new HashSet<String>();

  /**
   * This sets instance variable {@link #rootDirectory} depending on the value of
   * the expression and value:
   * <ul>
   * <li>if value is not null, the starting directory is set to this value</li>
   * <li>if value is null, the expression is not null and not a string with
   * 	 with zero length, the expression is evaluted and used as starting 
   * 	 directory</li>
   * </ul>
   * 
   * @param _expr	expression used to evalute the starting directory (expression is
   *          		evaluted if not null or zero string)
   * @param _value 	hard coded value (is used if not null)
   * @see #rootDirectory
   */
  public void setRootDirectory(final String _value)  {
    this.rootDirectory = _value;
  }

  /**
   * Adds an include expression for file names.
   * 
   * @param _include  include
   * @see #includes
   */
  public void addInclude(final String _include) {
    this.includes.add(_include);
  }

  /**
   * Adds a collection of include expressions of file names,
   *
   * @param _includes collection of includes to add
   * @see #includes
   */
  public void addIncludes(final Collection<String> _includes)  {
    this.includes.addAll(_includes);
  }

  /**
   * Adds an exclude expression for file names.
   * 
   * @param _exclude  include
   * @see #excludes
   */
  public void addExclude(final String _exclude) {
    this.excludes.add(_exclude);
  }

  /**
   * Adds a collection of exclude expressions of file names,
   *
   * @param _excludes collection of includes to add
   * @see #excludes
   */
  public void addExcludes(final Collection<String> _excludes)  {
    this.excludes.addAll(_excludes);
  }

  /**
   * Returns the set of files represented by this file set.
   *
   * @return set of found files
   */
  public Set<String> getFiles() {
    // scan
    final DirectoryScanner ds = new DirectoryScanner();
    final String[] includes = this.includes.toArray(new String[this.includes.size()]);
    final String[] excludes = this.excludes.toArray(new String[this.excludes.size()]);
    ds.setIncludes(includes);
    ds.setExcludes(excludes);
    ds.setBasedir(this.rootDirectory);
    ds.setCaseSensitive(true);
    ds.scan();

    // convert to set with files
    final Set<String> files = new HashSet<String>();
    final String[] fileNames = ds.getIncludedFiles();
    for (final String fileName : fileNames) {
      files.add(fileName);
    }

    return files;
  }

  /**
   * Returns a string representation with values of all instance variables.
   * 
   * @return string representation of this Application
   */
  public String toString() {
    return new ToStringBuilder(this)
          .append("directory", this.rootDirectory)
          .append("includes", this.includes)
          .append("excludes", this.excludes)
          .append("files", this.getFiles()).toString();
  }
}
