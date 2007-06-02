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

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.jexl.JexlHelper;
import org.apache.commons.jexl.JexlContext;
import org.apache.commons.lang.builder.ToStringBuilder;

import org.efaps.update.access.AccessSetUpdate;
import org.efaps.update.access.AccessTypeUpdate;
import org.efaps.update.datamodel.SQLTableUpdate;
import org.efaps.update.datamodel.TypeUpdate;
import org.efaps.update.integration.WebDAVUpdate;
import org.efaps.update.program.JavaUpdate;
import org.efaps.update.ui.CommandUpdate;
import org.efaps.update.ui.FormUpdate;
import org.efaps.update.ui.ImageUpdate;
import org.efaps.update.ui.MenuUpdate;
import org.efaps.update.ui.SearchUpdate;
import org.efaps.update.ui.TableUpdate;
import org.efaps.update.user.JAASSystemUpdate;
import org.efaps.update.user.RoleUpdate;
import org.efaps.util.EFapsException;

/**
 *
 * @author tmo
 * @version $Id$
 */
public class ApplicationVersion implements Comparable /*< ApplicationVersion >*/  {
                              
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
   * All defined file sets for this application version.
   *
   * @see #addFileSet
   */
  private final Set < FileSet > fileSets = new HashSet < FileSet > ();
  
  /////////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * Installs the xml update scripts of the schema definitions for this
   * version defined in {@link #number}.
   */
  public void install() throws EFapsException, Exception  {
    
    Set < File > files  = getFiles();
    
    JexlContext jexlContext = JexlHelper.createContext();
    jexlContext.getVars().put("version", this.number);
    
    for (File file : files)  {
      RoleUpdate update = RoleUpdate.readXMLFile(file);
      if (update != null)  {
        update.updateInDB(jexlContext);
      }
    }
    for (File file : files)  {
      SQLTableUpdate update = SQLTableUpdate.readXMLFile(file);
      if (update != null)  {
        update.updateInDB(jexlContext);
      }
    }
    for (File file : files)  {
      TypeUpdate update = TypeUpdate.readXMLFile(file);
      if (update != null)  {
        update.updateInDB(jexlContext);
      }
    }
    for (File file : files)  {
      JAASSystemUpdate update = JAASSystemUpdate.readXMLFile(file);
      if (update != null)  {
        update.updateInDB(jexlContext);
      }
    }
    for (File file : files)  {
      AccessTypeUpdate update = AccessTypeUpdate.readXMLFile(file);
      if (update != null)  {
        update.updateInDB(jexlContext);
      }
    }
    for (File file : files)  {
      AccessSetUpdate update = AccessSetUpdate.readXMLFile(file);
      if (update != null)  {
        update.updateInDB(jexlContext);
      }
    }
    for (File file : files)  {
      ImageUpdate update = ImageUpdate.readXMLFile(file);
      if (update != null)  {
        update.updateInDB(jexlContext);
      }
    }
    for (File file : files)  {
      FormUpdate update = FormUpdate.readXMLFile(file);
      if (update != null)  {
        update.updateInDB(jexlContext);
      }
    }
    for (File file : files)  {
      TableUpdate update = TableUpdate.readXMLFile(file);
      if (update != null)  {
        update.updateInDB(jexlContext);
      }
    }
    for (File file : files)  {
      SearchUpdate update = SearchUpdate.readXMLFile(file);
      if (update != null)  {
        update.updateInDB(jexlContext);
      }
    }
    for (File file : files)  {
      MenuUpdate update = MenuUpdate.readXMLFile(file);
      if (update != null)  {
        update.updateInDB(jexlContext);
      }
    }
    for (File file : files)  {
      CommandUpdate update = CommandUpdate.readXMLFile(file);
      if (update != null)  {
        update.updateInDB(jexlContext);
      }
    }
    for (File file : files)  {
      WebDAVUpdate update = WebDAVUpdate.readXMLFile(file);
      if (update != null)  {
        update.updateInDB(jexlContext);
      }
    }
    for (File file : files)  {
      JavaUpdate update = JavaUpdate.readXMLFile(file);
      if (update != null)  {
        update.updateInDB(jexlContext);
      }
    }
    
    
  }

  private Set < File > getFiles()  {
    Set < File > ret = new HashSet < File > ();
    for (FileSet fileSet : this.fileSets)  {
      ret.addAll(fileSet.getFiles());
    }
    return ret;
  }
  
  /**
   * Appends a new file set for this application version.
   *
   * @param _fileSet  file set to append to this application version
   * @see #fileSets
   */
  public void addFileSet(final FileSet _fileSet)  {
    this.fileSets.add(_fileSet);
  }
  
  /**
   * Compares this application version with the specified application 
   * version.<br/>
   * The method compares the version number of the application version. To
   * do this, the method {@link java.lang.Long#compareTo} is called.
   *
   * @param _compareTo  application version instance to compare to
   * @return a negative integer, zero, or a positive integer as this 
   *         application version is less than, equal to, or greater than the 
   *         specified  application version
   * @see java.lang.Long#compareTo
   * @see java.lang.Comparable#compareTo
   */
  public int compareTo(final Object _compareTo)  {
    return new Long(this.number)
                          .compareTo(((ApplicationVersion) _compareTo).number);
  }

  /////////////////////////////////////////////////////////////////////////////
  // instance getter and setter methods

  /**
   * This is the setter method for instance variable {@link #number}.
   *
   * @param _number new value for instance variable {@link #number}
   * @see #number
   * @see #getNumber
   */
  public void setNumber(final long _number)  {
    this.number = _number;
  }

  /**
   * This is the getter method for instance variable {@link #number}.
   *
   * @return value of instance variable {@link #number}
   * @see #number
   * @see #setNumber
   */
  public Long getNumber()  {
    return this.number;
  }

  /**
   * Returns a string representation with values of all instance variables.
   *
   * @return string representation of this Application
   */
  public String toString()  {
    return new ToStringBuilder(this)
      .append("number",         this.number)
      .append("fileSets",       this.fileSets)
      .toString();
  }
}

