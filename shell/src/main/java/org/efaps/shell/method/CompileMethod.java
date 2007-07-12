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

import org.efaps.admin.program.esjp.Compiler;
import org.efaps.util.EFapsException;

/**
 *
 * @author tmo
 * @version $Id$
 */
public final class CompileMethod extends AbstractMethod  {
  
  /////////////////////////////////////////////////////////////////////////////
  // static variables
/*
  private final static Option PROPERTY_VERSION  = OptionBuilder
        .withArgName("number")
        .hasArg()
        .withDescription("Defines global import version")
        .create("version");
*/
  /////////////////////////////////////////////////////////////////////////////
  // constructors / desctructors
  
  /**
   *
   */
  public CompileMethod()  {
    super("compile", "compile eFaps Java programs");
  }

  /////////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   *
   * @todo remove Exception
   */
  public void doMethod() throws EFapsException,Exception {
    login("Administrator", "");
    reloadCache();
    startTransaction();
 
    (new Compiler()).compile();

    commitTransaction();
  }
}
