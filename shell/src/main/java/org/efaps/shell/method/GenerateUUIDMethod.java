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

import java.util.UUID;

import org.efaps.util.EFapsException;

/**
 * A new universally unique identifier (UUID) is created and printed out.
 *
 * @author tmo
 * @version $Id$
 */
public final class GenerateUUIDMethod extends AbstractMethod  {
  
  /////////////////////////////////////////////////////////////////////////////
  // constructors / desctructors
  
  /**
   *
   */
  public GenerateUUIDMethod()  {
    super("generateUUID", "generate unique univeral identifier");
  }

  /////////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * The new universally unique identifier is created and printed out with a 
   * normal call to the Java standard "System.out.println".
   *
   * @todo remove Exception
   */
  public void doMethod() throws EFapsException,Exception {
    UUID uuid = UUID.randomUUID();
    System.out.println("UUID = " + uuid.toString());
  }
}
