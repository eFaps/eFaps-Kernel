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

package org.efaps.shell.method;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.efaps.importer.DataImport;
import org.efaps.util.EFapsException;

/**
 * Data defined in an XML-Struktur is imported into the eFaps-Database
 * 
 * @author jmo
 * @version $Id$
 * 
 */
public class ImportDataMethod extends AbstractMethod {

  private final static Option PROPERTY_BASENAME = OptionBuilder.withArgName(
                                                    "bname").hasArg()
                                                    .withDescription(
                                                        "defines the BaseName")
                                                    .isRequired().create(
                                                        "basename");

  public ImportDataMethod() {
    super("importData",
        "imports Data defined in an XML-Struktur into the DataBase",
        PROPERTY_BASENAME);
  }

  @Override
  protected void doMethod() throws EFapsException, Exception {
    super.reloadCache();
    super.startTransaction();
    String basename = getCommandLine().getOptionValue("basename");
    for (String fileName : getCommandLine().getArgs()) {
      DataImport dimport = new DataImport(basename);
//      dimport.initialise();
      dimport.readXMLFile(fileName);
      if (dimport.hasData()) {
        dimport.updateInDB();
      }
    }
    super.commitTransaction();

  }
}
