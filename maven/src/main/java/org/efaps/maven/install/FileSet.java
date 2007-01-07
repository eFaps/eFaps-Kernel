/*
 * Copyright 2006 The eFaps Team
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

import java.io.File;
import java.io.FileFilter;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.ExpressionFactory;
import org.apache.commons.jexl.JexlHelper;
import org.apache.commons.jexl.JexlContext;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 *
 * @author tmo
 * @version $Id$
 */
public class FileSet implements FileFilter  {
    
  /**
   * Stores the starting directory for which this file set is defined. Default
   * value is current directory.
   *
   * @see #setDirectory
   */
  private String directory = ".";
  
  /**
   * Store regular expression of directory names which are included.
   *
   * @see #addIncludeDir
   * @see #accept
   */
  private Set < String > includeDirs = new HashSet < String > ();
  
  /**
   * Store regular expression of file names which are included.
   *
   * @see #addIncludeFile
   * @see #accept
   */
  private Set < String > includeFiles = new HashSet < String > ();

  /**
   * This sets instance variable {@link #directory} depending on the value of
   * the expression and value:
   * <ul>
   * <li>if value is not null, the starting directory is set to this value</li>
   * <li>if value is null, the expression is not null and not a string with
   *     with zero length, the expression is evaluted and used as starting
   *     directory</li>
   * </ul>
   *
   * @param _expr   expression used to evalute the starting directory 
   *                (expression is evaluted if not null or zero string)
   * @param _value  hard coded value (is used if not null)
   * @see #directory
   */
  public void setDirectory(final String _expr, final String _value) throws Exception {
    if (_value != null)  {
      this.directory = _value;
    } else if ((_expr != null) && (_expr.length() > 0)) {
      JexlContext jexlContext = JexlHelper.createContext();
      jexlContext.getVars().put("System", System.class);
      Expression jexlExpr = ExpressionFactory.createExpression(_expr);
      this.directory = jexlExpr.evaluate(jexlContext).toString();
    }
  }

  /**
   * Adds an regular expression for the directory names.
   *
   * @param _directory  regular expression for directories
   * @see #includeDirs
   */
  public void addIncludeDir(final String _directory)  {
    this.includeDirs.add(_directory);
  }

  /**
   * Adds an regular expression for the file names.
   *
   * @param _file  regular expression for files
   * @see #includeFiles
   */
  public void addIncludeFile(final String _file)  {
    this.includeFiles.add(_file);
  }

  /**
   * Returns the set of files represented by this file set.
   *
   * @return set of found files
   * @see #getFiles(File)
   */
  public Set < File > getFiles()  {
    Set < File > files = new HashSet < File > ();
    File file = new File(this.directory);

    if (file.isDirectory())  {
      files.addAll(getFiles(file));
    } else if (file.isFile())  {
      files.add(file);
    }
    
    return files;
  }

  /**
   * @param _directory directory for which the files should be returned
   * @return set of found files for this directory
   */
  private Set < File > getFiles(final File _directory)  {
    Set < File > files = new HashSet < File > ();

    for (File file : _directory.listFiles(this))  {
      if (file.isDirectory())  {
        files.addAll(getFiles(file));
      } else if (file.isFile())  {
        files.add(file);
      }
    }
    return files;
  }

  /**
   * Test, if the given file /directiry implements the defined rules for this
   * fileset.
   *
   * @param _file file to set
   * @return <i>true</i> if the file / directory
   */
  public boolean accept(final File _file)  {
    boolean ret = false;
    if (_file.isDirectory())  {
      for (String includeDir : this.includeDirs)  {
        if (_file.getName().matches(includeDir))  {
          ret = true;
          break;
        }
      }
    } else if (_file.isFile())  {
      for (String includeFile : this.includeFiles)  {
        if (_file.getName().matches(includeFile))  {
          ret = true;
          break;
        }
      }
    }
    return ret;
  }

  /**
   * Returns a string representation with values of all instance variables.
   *
   * @return string representation of this Application
   */
  public String toString()  {
    return new ToStringBuilder(this)
      .append("directory",      this.directory)
      .append("includeDirs",    this.includeDirs)
      .append("includeFiles",   this.includeFiles)
      .append("files",          this.getFiles())
      .toString();
  }
}
