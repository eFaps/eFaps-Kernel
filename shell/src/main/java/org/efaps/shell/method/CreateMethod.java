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

package org.efaps.shell.method;

import java.io.StringReader;

import org.mozilla.javascript.tools.shell.Main;

import org.efaps.db.Context;
import org.efaps.util.EFapsException;

/**
 *
 * @author tmo
 * @version $Id$
 */
public final class CreateMethod extends AbstractJavaScriptMethod  {
  
  /////////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * The import of persons is started. First the cache is reloaded and then
   * the import itself is done.
   *
   * @todo remove Exception
   */
  public void doMethod() throws EFapsException,Exception {
    StringReader reader = new StringReader("eFapsCreateAll();");

    Main.evaluateScript(getJavaScriptContext(), 
                        Main.getGlobal(), 
                        reader, 
                        null, 
                        "<stdin>", 
                        0, 
                        null);
  }
}
