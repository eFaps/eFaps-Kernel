/*
 * Copyright 2003 - 2007 The eFaps Team
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

package org.efaps.maven.plugin.goal.efaps;

import org.apache.maven.plugin.MojoExecutionException;
import org.jfrog.maven.annomojo.annotations.MojoGoal;

import org.efaps.importer.DataImport;
import org.efaps.maven.plugin.goal.EFapsAbstractMojo;

/**
 * Data defined in an XML-Struktur is imported into the eFaps-Database
 * 
 * @author jmo
 * @version $Id$
 * @todo must be reworked!
 */
@MojoGoal("import-data")
public class ImportDataMojo extends EFapsAbstractMojo {

/*  private final static Option PROPERTY_BASENAME = OptionBuilder.withArgName(
                                                    "bname").hasArg()
                                                    .withDescription(
                                                        "defines the BaseName")
                                                    .isRequired().create(
                                                        "basename");
*/
/*
  public ImportDataMojo() {
    super("importData",
        "imports Data defined in an XML-Struktur into the DataBase",
        PROPERTY_BASENAME);
  }
*/

  public void execute() throws MojoExecutionException {
    try  {
      reloadCache();
      startTransaction();
//      String basename = getCommandLine().getOptionValue("basename");
//      for (String fileName : getCommandLine().getArgs()) {
//      DataImport dimport = new DataImport(basename);
//      dimport.initialise();
//      dimport.readXMLFile(fileName);
//      if (dimport.hasData()) {
//        dimport.updateInDB();
//      }
//      }
      commitTransaction();
    } catch (Exception e)  {
      throw new MojoExecutionException("data import failed", e);
    }
  }
}
